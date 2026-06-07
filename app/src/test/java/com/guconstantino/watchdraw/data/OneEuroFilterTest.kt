package com.guconstantino.watchdraw.data

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for the 1€ input filter. */
class OneEuroFilterTest {

    @Test
    fun first_sample_is_returned_raw() {
        val f = OneEuroFilter()
        val p = Offset(12f, 34f)
        assertEquals(p, f.filter(p, 0L))
    }

    @Test
    fun constant_input_stays_constant() {
        val f = OneEuroFilter()
        val p = Offset(50f, 50f)
        f.filter(p, 0L)
        var out = p
        for (i in 1..20) out = f.filter(p, i * 11L)
        assertEquals(50f, out.x, 0.01f)
        assertEquals(50f, out.y, 0.01f)
    }

    @Test
    fun converges_toward_a_constant_target() {
        val f = OneEuroFilter()
        f.filter(Offset(0f, 0f), 0L)
        var out = Offset(0f, 0f)
        for (i in 1..150) out = f.filter(Offset(100f, 100f), i * 11L)
        assertEquals(100f, out.x, 1.0f)
        assertEquals(100f, out.y, 1.0f)
    }

    @Test
    fun reset_reinitializes_to_raw() {
        val f = OneEuroFilter()
        f.filter(Offset(0f, 0f), 0L)
        f.filter(Offset(99f, 99f), 11L)
        f.reset()
        val p = Offset(7f, 7f)
        assertEquals(p, f.filter(p, 0L))
    }
}
