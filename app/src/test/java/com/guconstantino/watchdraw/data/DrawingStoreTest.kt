package com.guconstantino.watchdraw.data

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Round-trip tests for the JSON persistence of drawings. */
@RunWith(RobolectricTestRunner::class)
class DrawingStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun save_then_load_roundtrips_all_fields() {
        val drawings = listOf(
            Drawing(
                id = "a1",
                createdAt = 1000L,
                paths = listOf(
                    DrawnPath(listOf(Offset(1f, 2f), Offset(3f, 4f)), Color(0xFFDA0505), 9f)
                ),
                isFavorite = true
            ),
            Drawing(id = "b2", createdAt = 2000L, paths = emptyList(), deletedAt = 5000L)
        )

        DrawingStore.save(context, "test.json", drawings)
        val loaded = DrawingStore.load(context, "test.json")

        assertEquals(2, loaded.size)

        val a = loaded.first { it.id == "a1" }
        assertEquals(1000L, a.createdAt)
        assertTrue(a.isFavorite)
        assertEquals(1, a.paths.size)
        assertEquals(2, a.paths[0].points.size)
        assertEquals(9f, a.paths[0].strokeWidth)
        assertEquals(Offset(1f, 2f), a.paths[0].points[0])

        val b = loaded.first { it.id == "b2" }
        assertEquals(5000L, b.deletedAt)
        assertTrue(b.paths.isEmpty())
    }

    @Test
    fun load_missing_file_returns_empty() {
        assertTrue(DrawingStore.load(context, "does_not_exist.json").isEmpty())
    }

    @Test
    fun load_corrupt_file_returns_empty() {
        context.openFileOutput("corrupt.json", Context.MODE_PRIVATE).use {
            it.write("not valid json".toByteArray())
        }
        assertTrue(DrawingStore.load(context, "corrupt.json").isEmpty())
    }
}
