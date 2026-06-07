package com.guconstantino.watchdraw.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [DrawingViewModel]'s sync-queue processing, using an injected fake
 * uploader and a test dispatcher — no network, auth or device. The queue is
 * pre-seeded on disk, then sign-in triggers processing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SyncProcessingTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SyncQueue.clear(app)
    }

    private class FakeUploader(var result: UploadResult) : PhotoUploader {
        var calls = 0
        override suspend fun upload(context: Context, pngBytes: ByteArray): UploadResult {
            calls++
            return result
        }
    }

    private fun seedQueue(vararg drawingIds: String) {
        SyncQueue.clear(app)
        drawingIds.forEachIndexed { i, id ->
            SyncQueue.enqueue(app, byteArrayOf(i.toByte()), drawingId = id)
        }
    }

    @Test
    fun successful_upload_clears_queue_and_marks_synced() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        seedQueue("d1", "d2")
        val fake = FakeUploader(UploadResult.Success)
        val vm = DrawingViewModel(app, fake, dispatcher)

        vm.onSignedIn(UserProfile("name", "e@x.com"))
        advanceUntilIdle()

        assertEquals(2, fake.calls)
        assertEquals(0, vm.syncPendingCount)
        assertEquals(DrawingSyncStatus.SYNCED, vm.syncStatusFor("d1"))
        assertEquals(DrawingSyncStatus.SYNCED, vm.syncStatusFor("d2"))
        // Persisted to disk.
        assertTrue(SyncQueue.load(app).isEmpty())
        assertEquals(setOf("d1", "d2"), SyncQueue.loadSyncedIds(app))
    }

    @Test
    fun failed_upload_keeps_item_and_marks_failed() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        seedQueue("d1")
        val fake = FakeUploader(UploadResult.Failed("boom"))
        val vm = DrawingViewModel(app, fake, dispatcher)

        vm.onSignedIn(UserProfile("name", "e@x.com"))
        advanceUntilIdle()

        assertEquals(1, vm.syncPendingCount)
        assertEquals(DrawingSyncStatus.FAILED, vm.syncStatusFor("d1"))
    }

    @Test
    fun needs_consent_stops_batch_and_leaves_items_queued() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        seedQueue("d1", "d2")
        val fake = FakeUploader(UploadResult.NeedsConsent)
        val vm = DrawingViewModel(app, fake, dispatcher)

        vm.onSignedIn(UserProfile("name", "e@x.com"))
        advanceUntilIdle()

        assertEquals(1, fake.calls) // stopped after the first auth failure
        assertEquals(2, vm.syncPendingCount)
    }
}
