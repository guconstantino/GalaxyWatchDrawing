package com.guconstantino.watchdraw.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {

    private val _drawnPaths = mutableStateListOf<DrawnPath>()
    val drawnPaths: List<DrawnPath> get() = _drawnPaths

    private val _undonePaths = mutableStateListOf<DrawnPath>()

    var currentColor by mutableStateOf(Color.White)
        private set

    var currentStrokeWidth by mutableStateOf(StrokeWidths[1])
        private set

    var currentScreen by mutableStateOf(AppScreen.Canvas)

    // Size of the on-screen drawing canvas in pixels; used to rasterize exports.
    var canvasSize by mutableStateOf(IntSize.Zero)

    // Heart toggle in the actions menu — purely visual for now (filled <-> outline).
    var favorite by mutableStateOf(false)
        private set

    fun toggleFavorite() {
        favorite = !favorite
    }

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
            _undonePaths.clear()
        }
        currentPoints = emptyList()
    }

    fun cancelStroke() {
        currentPoints = emptyList()
    }

    fun undo() {
        if (_drawnPaths.isNotEmpty()) {
            val last = _drawnPaths.removeAt(_drawnPaths.size - 1)
            _undonePaths.add(last)
        }
    }

    fun redo() {
        if (_undonePaths.isNotEmpty()) {
            val last = _undonePaths.removeAt(_undonePaths.size - 1)
            _drawnPaths.add(last)
        }
    }

    fun clearCanvas() {
        _drawnPaths.clear()
        _undonePaths.clear()
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
