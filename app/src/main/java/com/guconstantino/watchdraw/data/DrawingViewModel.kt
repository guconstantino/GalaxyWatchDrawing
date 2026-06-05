package com.guconstantino.watchdraw.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {

    private val _drawnPaths = mutableStateListOf<DrawnPath>()
    val drawnPaths: List<DrawnPath> get() = _drawnPaths

    var currentColor by mutableStateOf(Color.White)
        private set

    var currentStrokeWidth by mutableStateOf(StrokeWidths[1])
        private set

    var currentScreen by mutableStateOf(AppScreen.Canvas)

    // Live drawing path (updates every touch move)
    var currentPoints by mutableStateOf<List<Offset>>(emptyList())
        private set

    fun startStroke(offset: Offset) {
        currentPoints = listOf(offset)
    }

    fun addPoint(offset: Offset) {
        currentPoints = currentPoints + offset
    }

    fun endStroke() {
        if (currentPoints.size > 1) {
            _drawnPaths.add(
                DrawnPath(
                    points = currentPoints,
                    color = currentColor,
                    strokeWidth = currentStrokeWidth
                )
            )
        }
        currentPoints = emptyList()
    }

    fun undo() {
        if (_drawnPaths.isNotEmpty()) _drawnPaths.removeLast()
    }

    fun clearCanvas() {
        _drawnPaths.clear()
        currentPoints = emptyList()
    }

    fun setColor(color: Color) {
        currentColor = color
        currentScreen = AppScreen.Canvas
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
        currentScreen = AppScreen.Canvas
    }

    fun isEmpty(): Boolean = _drawnPaths.isEmpty() && currentPoints.isEmpty()
}
