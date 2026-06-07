package com.guconstantino.watchdraw.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Tests for the offline sync queue persistence. */
@RunWith(RobolectricTestRunner::class)
class SyncQueueTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun reset() {
        SyncQueue.clear(context)
    }

    @Test
    fun enqueue_persists_item_and_bytes() {
        val item = SyncQueue.enqueue(context, byteArrayOf(1, 2, 3), drawingId = "d1")

        val loaded = SyncQueue.load(context)
        assertEquals(1, loaded.size)
        assertEquals(item.id, loaded[0].id)
        assertEquals("d1", loaded[0].drawingId)
        assertArrayEquals(byteArrayOf(1, 2, 3), SyncQueue.readBytes(context, item))
    }

    @Test
    fun synced_ids_roundtrip() {
        SyncQueue.saveSyncedIds(context, setOf("x", "y"))
        assertEquals(setOf("x", "y"), SyncQueue.loadSyncedIds(context))
    }

    @Test
    fun delete_file_removes_bytes_only() {
        val item = SyncQueue.enqueue(context, byteArrayOf(7), drawingId = "d2")
        SyncQueue.deleteFile(context, item)
        assertNull(SyncQueue.readBytes(context, item))
    }

    @Test
    fun clear_wipes_queue_and_synced_ids() {
        SyncQueue.enqueue(context, byteArrayOf(9), drawingId = "d1")
        SyncQueue.saveSyncedIds(context, setOf("x"))

        SyncQueue.clear(context)

        assertTrue(SyncQueue.load(context).isEmpty())
        assertTrue(SyncQueue.loadSyncedIds(context).isEmpty())
    }
}
