package com.guconstantino.watchdraw.data

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs

/**
 * 1€ (One-Euro) filter for 2D touch input — the de-facto standard for smoothing
 * pointer paths. It alisa heavily when the finger is slow/still (kills jitter)
 * and almost not at all when fast (low lag, preserves precision), unlike a fixed
 * smoothing that always rounds corners.
 *
 * Reference: Casiez, Roussel, Vogel — "1€ Filter" (CHI 2012).
 * Tune [minCutoff] (lower = smoother when still) and [beta] (higher = less lag
 * when fast). Defaults are a reasonable starting point for a watch; adjust on
 * device if needed.
 */
class OneEuroFilter(
    private val minCutoff: Float = 1.0f,
    private val beta: Float = 0.01f,
    private val dCutoff: Float = 1.0f
) {
    private var initialized = false
    private var xPrev = 0f
    private var yPrev = 0f
    private var dxPrev = 0f
    private var dyPrev = 0f
    private var tPrev = 0L

    fun reset() {
        initialized = false
    }

    private fun alpha(cutoff: Float, dt: Float): Float {
        val tau = 1f / (2f * PI.toFloat() * cutoff)
        return 1f / (1f + tau / dt)
    }

    /** Returns the filtered point for a raw [point] sampled at [timeMillis]. */
    fun filter(point: Offset, timeMillis: Long): Offset {
        if (!initialized) {
            initialized = true
            xPrev = point.x
            yPrev = point.y
            dxPrev = 0f
            dyPrev = 0f
            tPrev = timeMillis
            return point
        }

        var dt = (timeMillis - tPrev) / 1000f
        if (dt <= 0f) dt = 1f / 90f // fallback (~90 Hz) for equal/!monotonic stamps
        tPrev = timeMillis

        // Filtered derivative.
        val dx = (point.x - xPrev) / dt
        val dy = (point.y - yPrev) / dt
        val aD = alpha(dCutoff, dt)
        val edx = dxPrev + aD * (dx - dxPrev)
        val edy = dyPrev + aD * (dy - dyPrev)
        dxPrev = edx
        dyPrev = edy

        // Speed-adaptive cutoff → position filter.
        val aX = alpha(minCutoff + beta * abs(edx), dt)
        val aY = alpha(minCutoff + beta * abs(edy), dt)
        val ex = xPrev + aX * (point.x - xPrev)
        val ey = yPrev + aY * (point.y - yPrev)
        xPrev = ex
        yPrev = ey
        return Offset(ex, ey)
    }
}
