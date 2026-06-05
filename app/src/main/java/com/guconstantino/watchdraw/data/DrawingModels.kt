package com.guconstantino.watchdraw.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class DrawnPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

// Exact palette from the Figma "Color Selector" (node 145007:380), in grid order
// (row by row, left→right). These are the ONLY colors the app draws with.
val DrawingColors = listOf(
    Color(0xFFF21B3F), // red
    Color(0xFFFF9914), // orange
    Color(0xFF08BDBD), // teal
    Color(0xFF29BF12), // green
    Color(0xFFFF14B1), // magenta
    Color(0xFFFFFFFF), // white
)

// Three brush sizes (thin, medium, thick). Default selection is the medium one.
val StrokeWidths = listOf(3f, 9f, 18f)

enum class AppScreen {
    Canvas, StrokePicker, ColorPicker, Actions, ClearConfirm
}
