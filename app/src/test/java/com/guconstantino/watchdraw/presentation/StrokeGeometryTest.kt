package com.guconstantino.watchdraw.presentation

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the Catmull-Rom spline used to render strokes. */
class StrokeGeometryTest {

    @Test
    fun fewer_than_two_points_returns_empty() {
        assertTrue(catmullRomSegments(emptyList()).isEmpty())
        assertTrue(catmullRomSegments(listOf(Offset(1f, 1f))).isEmpty())
    }

    @Test
    fun curve_passes_through_every_real_point() {
        val pts = listOf(Offset(0f, 0f), Offset(10f, 5f), Offset(20f, 0f), Offset(30f, 8f))
        val segs = catmullRomSegments(pts)
        assertEquals(3, segs.size)
        // Each segment ends exactly on the next input point (precision guarantee).
        assertEquals(pts[1], segs[0].end)
        assertEquals(pts[2], segs[1].end)
        assertEquals(pts[3], segs[2].end)
    }

    @Test
    fun collinear_points_stay_on_the_line() {
        val pts = listOf(Offset(0f, 0f), Offset(1f, 0f), Offset(2f, 0f), Offset(3f, 0f))
        for (seg in catmullRomSegments(pts)) {
            assertEquals(0f, seg.c1.y, 1e-4f)
            assertEquals(0f, seg.c2.y, 1e-4f)
            assertEquals(0f, seg.end.y, 1e-4f)
        }
    }
}
