package com.guconstantino.watchdraw.data

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for the pure state logic of [DrawingViewModel]. State is seeded by writing
 * the persistence files before the ViewModel is constructed (its `init` loads them),
 * which avoids needing the canvas/UI or the network.
 */
@RunWith(RobolectricTestRunner::class)
class DrawingViewModelTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun reset() {
        SyncQueue.clear(app)
        DrawingStore.save(app, "my_draws.json", emptyList())
        DrawingStore.save(app, "trash.json", emptyList())
    }

    private fun drawing(
        id: String,
        createdAt: Long,
        favorite: Boolean = false,
        deletedAt: Long? = null
    ) = Drawing(
        id = id,
        createdAt = createdAt,
        paths = listOf(DrawnPath(listOf(Offset(0f, 0f), Offset(1f, 1f)), Color.White, 9f)),
        deletedAt = deletedAt,
        isFavorite = favorite
    )

    @Test
    fun gallery_navigation_wraps_around() {
        DrawingStore.save(
            app, "my_draws.json",
            listOf(drawing("c", 3000), drawing("b", 2000), drawing("a", 1000))
        )
        val vm = DrawingViewModel(app)
        vm.openMyDraws()

        // Sorted newest-first: c, b, a → index 0 = c.
        assertEquals("c", vm.currentGalleryDrawing?.id)
        vm.galleryPrev() // wraps to the last
        assertEquals("a", vm.currentGalleryDrawing?.id)
        vm.galleryNext() // wraps back to the first
        assertEquals("c", vm.currentGalleryDrawing?.id)
    }

    @Test
    fun trash_items_older_than_30_days_are_cleaned_on_init() {
        val now = System.currentTimeMillis()
        val old = now - 31L * 24 * 60 * 60 * 1000
        val recent = now - 5L * 24 * 60 * 60 * 1000
        DrawingStore.save(
            app, "trash.json",
            listOf(drawing("old", 1000, deletedAt = old), drawing("recent", 2000, deletedAt = recent))
        )

        val vm = DrawingViewModel(app)

        assertEquals(1, vm.trash.size)
        assertEquals("recent", vm.trash[0].id)
    }

    @Test
    fun syncStatusFor_reflects_pending_synced_and_none() {
        SyncQueue.saveSyncedIds(app, setOf("synced1"))
        SyncQueue.enqueue(app, byteArrayOf(1), drawingId = "pending1")

        val vm = DrawingViewModel(app)

        assertEquals(DrawingSyncStatus.SYNCED, vm.syncStatusFor("synced1"))
        assertEquals(DrawingSyncStatus.PENDING, vm.syncStatusFor("pending1"))
        assertEquals(DrawingSyncStatus.NONE, vm.syncStatusFor("unknown"))
    }
}
