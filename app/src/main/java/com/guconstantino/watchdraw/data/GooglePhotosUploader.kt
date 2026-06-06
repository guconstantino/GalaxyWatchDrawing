package com.guconstantino.watchdraw.data

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.auth.GoogleAuthUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Uploads drawings to the signed-in user's Google Photos library using the
 * REST API and the `photoslibrary.appendonly` scope.
 *
 * No extra dependency is needed: we obtain an OAuth access token via
 * [GoogleAuthUtil] (part of play-services-auth) and talk to the two Photos
 * endpoints with [HttpURLConnection] + org.json.
 *
 * Flow (per the official docs):
 *   1. POST raw PNG bytes to /v1/uploads  -> returns an upload token (plain text)
 *   2. POST that token to /v1/mediaItems:batchCreate -> creates the media item,
 *      which "is always added to the user's library" and thus shows up in the
 *      Google Photos app on the user's phone.
 *
 * Everything runs on [Dispatchers.IO]; callers should launch from a coroutine.
 */
object GooglePhotosUploader {

    private const val UPLOAD_URL = "https://photoslibrary.googleapis.com/v1/uploads"
    private const val BATCH_CREATE_URL =
        "https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate"
    private const val ALBUM_DESCRIPTION = "WatchDraw"

    sealed class Result {
        object Success : Result()
        object NotSignedIn : Result()
        /** Sign-in succeeded but the Photos scope is missing — needs re-consent. */
        object NeedsConsent : Result()
        data class Failed(val reason: String) : Result()
    }

    suspend fun upload(context: Context, bitmap: Bitmap): Result = withContext(Dispatchers.IO) {
        val account = AuthManager.account(context) ?: return@withContext Result.NotSignedIn
        if (!AuthManager.hasPhotosScope(context)) return@withContext Result.NeedsConsent
        val androidAccount = account.account ?: return@withContext Result.NotSignedIn

        try {
            val token = GoogleAuthUtil.getToken(
                context,
                androidAccount,
                "oauth2:${AuthManager.PHOTOS_APPEND_SCOPE}"
            )

            val pngBytes = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }

            val uploadToken = postBytes(token, pngBytes)
                ?: return@withContext Result.Failed("upload failed")

            val created = batchCreate(token, uploadToken)
            if (created) Result.Success else Result.Failed("media item creation failed")
        } catch (e: Exception) {
            // UserRecoverableAuthException (consent revoked/needed) lands here too.
            Result.Failed(e.message ?: "unknown error")
        }
    }

    /** Step 1 — upload raw bytes, return the upload token (response body), or null. */
    private fun postBytes(accessToken: String, bytes: ByteArray): String? {
        val conn = (URL(UPLOAD_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 60_000
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/octet-stream")
            setRequestProperty("X-Goog-Upload-Content-Type", "image/png")
            setRequestProperty("X-Goog-Upload-Protocol", "raw")
        }
        return try {
            conn.outputStream.use { it.write(bytes) }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    /** Step 2 — create the media item from the upload token. */
    private fun batchCreate(accessToken: String, uploadToken: String): Boolean {
        val fileName = "watchdraw_${System.currentTimeMillis()}.png"
        val body = JSONObject().apply {
            put("newMediaItems", JSONArray().apply {
                put(JSONObject().apply {
                    put("description", ALBUM_DESCRIPTION)
                    put("simpleMediaItem", JSONObject().apply {
                        put("fileName", fileName)
                        put("uploadToken", uploadToken)
                    })
                })
            })
        }.toString()

        val conn = (URL(BATCH_CREATE_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 60_000
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            conn.outputStream.use { it.write(body.toByteArray()) }
            if (conn.responseCode !in 200..299) return false
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            // newMediaItemResults[].status.message == "Success" (code 0/absent on OK)
            val results = JSONObject(response).optJSONArray("newMediaItemResults")
            val status = results?.optJSONObject(0)?.optJSONObject("status")
            // A successful create has status code 0 (i.e. no "code" field) or message OK.
            status == null || status.optInt("code", 0) == 0
        } catch (e: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }
}
