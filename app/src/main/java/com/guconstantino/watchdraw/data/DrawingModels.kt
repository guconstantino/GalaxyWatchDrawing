package com.guconstantino.watchdraw.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class DrawnPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

/** A saved drawing: an ordered set of strokes plus metadata. */
data class Drawing(
    val id: String,
    val createdAt: Long,
    val paths: List<DrawnPath>,
    val deletedAt: Long? = null
)

// Exact palette from the Figma "Color Selector" (node 145007:380), in grid order
// (row by row, left→right). These are the ONLY colors the app draws with.
val DrawingColors = listOf(
    Color(0xFFDA0505), // Red (Top Left)
    Color(0xFF128AE6), // Blue (Top Right)
    Color(0xFFED3F1C), // Orange-Red (Mid Left)
    Color(0xFF14AA60), // Green (Mid Right)
    Color(0xFFFF9914), // Orange (Bottom Left)
    Color(0xFFE6E6E6), // White/Gray (Bottom Right)
)

// Three brush sizes (thin, medium, thick). Default selection is the medium one.
val StrokeWidths = listOf(3f, 9f, 18f)

enum class AppScreen {
    Home, MyDraws, Trash, Canvas, StrokePicker, ColorPicker, Actions, ClearConfirm, DeleteConfirm
}
