package com.guconstantino.watchdraw.data

import android.content.Context
import android.content.Intent

/** Result of attempting to upload a single image. */
sealed class UploadResult {
    object Success : UploadResult()
    object NotSignedIn : UploadResult()
    /**
     * Sign-in succeeded but the Google Photos scope hasn't been granted yet.
     * [recoveryIntent] is the system consent screen to launch so the user can
     * grant it (from a [com.google.android.gms.auth.UserRecoverableAuthException]);
     * null when no recovery intent is available.
     */
    data class NeedsConsent(val recoveryIntent: Intent?) : UploadResult()
    data class Failed(val reason: String) : UploadResult()
}

/**
 * Abstraction over "send these PNG bytes to the user's photo library".
 * Production uses [GooglePhotosUploader]; tests inject a fake so the sync queue
 * logic can be exercised without network, auth or a device.
 */
interface PhotoUploader {
    suspend fun upload(context: Context, pngBytes: ByteArray): UploadResult
}
