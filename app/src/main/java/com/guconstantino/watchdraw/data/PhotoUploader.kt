package com.guconstantino.watchdraw.data

import android.content.Context

/** Result of attempting to upload a single image. */
sealed class UploadResult {
    object Success : UploadResult()
    object NotSignedIn : UploadResult()
    /** Sign-in succeeded but the Photos scope is missing — needs re-consent. */
    object NeedsConsent : UploadResult()
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
