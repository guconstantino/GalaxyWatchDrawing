package com.guconstantino.watchdraw.presentation

import androidx.compose.ui.geometry.Offset

/** A cubic Bézier segment: two control points and the on-curve end point. */
data class CubicSegment(val c1: Offset, val c2: Offset, val end: Offset)

/**
 * Converts a polyline into a Catmull-Rom spline expressed as cubic Bézier
 * segments. The resulting curve **passes through every input point** (unlike the
 * old midpoint-quadratic smoothing, which only used the points as control points
 * and drifted off the real finger path).
 *
 * Shared by the on-screen renderer and the bitmap export so what you see is what
 * you get. Returns empty for fewer than 2 points (handled separately by callers).
 */
fun catmullRomSegments(points: List<Offset>): List<CubicSegment> {
    if (points.size < 2) return emptyList()
    val out = ArrayList<CubicSegment>(points.size - 1)
    for (i in 0 until points.size - 1) {
        val p0 = points[if (i - 1 < 0) 0 else i - 1]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[if (i + 2 > points.lastIndex) points.lastIndex else i + 2]
        // Uniform Catmull-Rom → Bézier control points (tension 1/6).
        val c1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
        val c2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
        out.add(CubicSegment(c1, c2, p2))
    }
    return out
}
