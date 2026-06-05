package com.guconstantino.watchdraw.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class DrawnPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

val DrawingColors = listOf(
    Color.White,
    Color(0xFFFF5252),  // Red
    Color(0xFFFF9800),  // Orange
    Color(0xFFFFEB3B),  // Yellow
    Color(0xFF69F0AE),  // Green neon
    Color(0xFF40C4FF),  // Cyan
    Color(0xFF7C4DFF),  // Purple
    Color(0xFFFF4081),  // Pink
)

val StrokeWidths = listOf(2f, 5f, 10f, 18f)

enum class AppScreen {
    Canvas, StrokePicker, ColorPicker, Actions, ClearConfirm
}
