package com.guconstantino.watchdraw.data

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
object GooglePhotosUploader : PhotoUploader {

    private const val UPLOAD_URL = "https://photoslibrary.googleapis.com/v1/uploads"
    private const val BATCH_CREATE_URL =
        "https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate"
    private const val ALBUM_DESCRIPTION = "WatchDraw"

    /** Outcome of a single upload attempt with a given access token. */
    private enum class Attempt { SUCCESS, AUTH_FAILED, FAILED }

    override suspend fun upload(context: Context, pngBytes: ByteArray): UploadResult =
        withContext(Dispatchers.IO) {
            val account = AuthManager.account(context) ?: return@withContext UploadResult.NotSignedIn
            val androidAccount = account.account ?: return@withContext UploadResult.NotSignedIn
            val scope = "oauth2:${AuthManager.PHOTOS_APPEND_SCOPE}"

            try {
                // Fetching a token for the Photos scope is what triggers the
                // consent screen: if the user has never granted it (the Wear
                // native sign-in only grants identity), getToken throws a
                // UserRecoverableAuthException carrying the consent Intent.
                var token = GoogleAuthUtil.getToken(context, androidAccount, scope)
                var attempt = tryUpload(token, pngBytes)

                if (attempt == Attempt.AUTH_FAILED) {
                    // The cached token is stale or the grant was revoked while it
                    // was still cached. Drop it and fetch a fresh one — if the
                    // grant is really gone, this throws UserRecoverableAuthException
                    // and the consent screen is surfaced below.
                    GoogleAuthUtil.clearToken(context, token)
                    token = GoogleAuthUtil.getToken(context, androidAccount, scope)
                    attempt = tryUpload(token, pngBytes)
                }

                when (attempt) {
                    Attempt.SUCCESS -> UploadResult.Success
                    Attempt.AUTH_FAILED -> UploadResult.NeedsConsent(null)
                    Attempt.FAILED -> UploadResult.Failed("upload failed")
                }
            } catch (e: UserRecoverableAuthException) {
                // Scope not granted (or just revoked) — hand the consent screen back.
                UploadResult.NeedsConsent(e.intent)
            } catch (e: Exception) {
                UploadResult.Failed(e.message ?: "unknown error")
            }
        }

    /** Runs both upload steps with [token], classifying the outcome. */
    private fun tryUpload(token: String, bytes: ByteArray): Attempt {
        val (upCode, upBody) = postBytes(token, bytes)
        if (upCode == 401 || upCode == 403) return Attempt.AUTH_FAILED
        val uploadToken = upBody?.takeIf { upCode in 200..299 && it.isNotEmpty() }
            ?: return Attempt.FAILED
        val (createCode, created) = batchCreate(token, uploadToken)
        return when {
            createCode == 401 || createCode == 403 -> Attempt.AUTH_FAILED
            created -> Attempt.SUCCESS
            else -> Attempt.FAILED
        }
    }

    /** Step 1 — upload raw bytes; returns (httpStatus, responseBody-or-null). */
    private fun postBytes(accessToken: String, bytes: ByteArray): Pair<Int, String?> {
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
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }.trim()
            } else {
                null
            }
            code to body
        } catch (e: Exception) {
            -1 to null
        } finally {
            conn.disconnect()
        }
    }

    /** Step 2 — create the media item; returns (httpStatus, created?). */
    private fun batchCreate(accessToken: String, uploadToken: String): Pair<Int, Boolean> {
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
            val code = conn.responseCode
            if (code !in 200..299) return code to false
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            // newMediaItemResults[].status.message == "Success" (code 0/absent on OK)
            val results = JSONObject(response).optJSONArray("newMediaItemResults")
            val status = results?.optJSONObject(0)?.optJSONObject("status")
            // A successful create has status code 0 (i.e. no "code" field) or message OK.
            val created = status == null || status.optInt("code", 0) == 0
            code to created
        } catch (e: Exception) {
            -1 to false
        } finally {
            conn.disconnect()
        }
    }
}
