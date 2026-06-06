package com.guconstantino.watchdraw.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel

class DrawingViewModel(app: Application) : AndroidViewModel(app) {

    private val _drawnPaths = mutableStateListOf<DrawnPath>()
    val drawnPaths: List<DrawnPath> get() = _drawnPaths

    private val _undonePaths = mutableStateListOf<DrawnPath>()

    var currentColor by mutableStateOf(Color.White)
        private set

    var currentStrokeWidth by mutableStateOf(StrokeWidths[1])
        private set

    var currentScreen by mutableStateOf(AppScreen.Home)

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

    /* --------------------------------------------------------------------- *
     * Saved drawings (My draws) + Trash
     * --------------------------------------------------------------------- */

    private val _myDraws = mutableStateListOf<Drawing>()
    val myDraws: List<Drawing> get() = _myDraws

    private val _trash = mutableStateListOf<Drawing>()
    val trash: List<Drawing> get() = _trash

    /** Index of the drawing shown in the My draws gallery. */
    var galleryIndex by mutableStateOf(0)
        private set

    /** Id of the drawing currently being edited (null = a brand new drawing). */
    private var editingId: String? = null

    init {
        _myDraws.addAll(DrawingStore.load(app, DRAWS_FILE))
        _trash.addAll(DrawingStore.load(app, TRASH_FILE))
        cleanupTrash()
    }

    private fun cleanupTrash() {
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val toRemove = _trash.filter { d ->
            val deletedAt = d.deletedAt ?: 0L
            deletedAt > 0 && (now - deletedAt) > thirtyDaysMs
        }
        if (toRemove.isNotEmpty()) {
            _trash.removeAll(toRemove)
            persist(TRASH_FILE, _trash)
        }
    }

    /* --------------------------------------------------------------------- *
     * Drawing interactions
     * --------------------------------------------------------------------- */

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

    /* --------------------------------------------------------------------- *
     * Navigation + persistence
     * --------------------------------------------------------------------- */

    /** Starts a fresh drawing from the Home screen (clears the canvas). */
    fun newDrawing() {
        editingId = null
        clearCanvas()
        currentScreen = AppScreen.Canvas
    }

    /** Leaves the canvas back to Home, auto-saving the current drawing. */
    fun exitToHome() {
        saveCurrentDrawing()
        currentScreen = AppScreen.Home
    }

    /** Opens the My draws gallery (no-op if there are no saved drawings yet). */
    fun openMyDraws() {
        if (_myDraws.isEmpty()) return
        galleryIndex = galleryIndex.coerceIn(0, _myDraws.lastIndex)
        currentScreen = AppScreen.MyDraws
    }

    val currentGalleryDrawing: Drawing? get() = _myDraws.getOrNull(galleryIndex)

    fun galleryNext() {
        if (_myDraws.size > 1) galleryIndex = (galleryIndex + 1) % _myDraws.size
    }

    fun galleryPrev() {
        if (_myDraws.size > 1) galleryIndex = (galleryIndex - 1 + _myDraws.size) % _myDraws.size
    }

    /** Opens the selected gallery drawing in the canvas for editing. */
    fun editCurrentGallery() {
        val d = currentGalleryDrawing ?: return
        editingId = d.id
        _drawnPaths.clear()
        _drawnPaths.addAll(d.paths)
        _undonePaths.clear()
        currentPoints = emptyList()
        currentScreen = AppScreen.Canvas
    }

    /** Moves the selected gallery drawing to the Trash. */
    fun deleteCurrentGalleryToTrash() {
        if (galleryIndex !in _myDraws.indices) return
        val d = _myDraws.removeAt(galleryIndex).copy(deletedAt = System.currentTimeMillis())
        _trash.add(0, d)
        persist(DRAWS_FILE, _myDraws)
        persist(TRASH_FILE, _trash)
        if (_myDraws.isEmpty()) {
            currentScreen = AppScreen.Home
        } else {
            galleryIndex = galleryIndex.coerceIn(0, _myDraws.lastIndex)
        }
    }

    /* --------------------------------------------------------------------- *
     * Trash interactions
     * --------------------------------------------------------------------- */

    fun openTrash() {
        cleanupTrash()
        if (_trash.isEmpty()) return
        galleryIndex = galleryIndex.coerceIn(0, _trash.lastIndex)
        currentScreen = AppScreen.Trash
    }

    val currentTrashDrawing: Drawing? get() = _trash.getOrNull(galleryIndex)

    fun trashNext() {
        if (_trash.size > 1) galleryIndex = (galleryIndex + 1) % _trash.size
    }

    fun trashPrev() {
        if (_trash.size > 1) galleryIndex = (galleryIndex - 1 + _trash.size) % _trash.size
    }

    fun restoreCurrentTrash() {
        val d = currentTrashDrawing ?: return
        _trash.removeAt(galleryIndex)
        _myDraws.add(0, d.copy(deletedAt = null))
        persist(DRAWS_FILE, _myDraws)
        persist(TRASH_FILE, _trash)
        if (_trash.isEmpty()) {
            // Screen handles "No files" state, but logic-wise we prepare for navigation
        } else {
            galleryIndex = galleryIndex.coerceIn(0, _trash.lastIndex)
        }
    }

    fun permanentDeleteCurrentTrash() {
        if (galleryIndex !in _trash.indices) return
        _trash.removeAt(galleryIndex)
        persist(TRASH_FILE, _trash)
        if (_trash.isNotEmpty()) {
            galleryIndex = galleryIndex.coerceIn(0, _trash.lastIndex)
        }
    }

    /** Opens a trash drawing for editing. It will be saved as a NEW drawing. */
    fun editTrashAsNew() {
        val d = currentTrashDrawing ?: return
        editingId = null // Force "new drawing" logic on save
        _drawnPaths.clear()
        _drawnPaths.addAll(d.paths)
        _undonePaths.clear()
        currentPoints = emptyList()
        currentScreen = AppScreen.Canvas
    }

    private fun saveCurrentDrawing() {
        if (_drawnPaths.isEmpty()) {
            editingId = null
            return
        }
        val snapshot = _drawnPaths.toList()
        val id = editingId
        if (id == null) {
            // Cap the collection: drop the oldest when the limit is reached.
            if (_myDraws.size >= MAX_DRAWS) _myDraws.removeAt(_myDraws.lastIndex)
            _myDraws.add(0, Drawing(newId(), System.currentTimeMillis(), snapshot))
            galleryIndex = 0
        } else {
            val idx = _myDraws.indexOfFirst { it.id == id }
            if (idx >= 0) {
                _myDraws[idx] = _myDraws[idx].copy(paths = snapshot)
            } else {
                _myDraws.add(0, Drawing(id, System.currentTimeMillis(), snapshot))
            }
        }
        editingId = null
        persist(DRAWS_FILE, _myDraws)
    }

    private fun persist(fileName: String, list: List<Drawing>) {
        DrawingStore.save(getApplication(), fileName, list)
    }

    private fun newId(): String =
        System.currentTimeMillis().toString() + "_" + (0..9_999).random()

    companion object {
        private const val DRAWS_FILE = "my_draws.json"
        private const val TRASH_FILE = "trash.json"
        private const val MAX_DRAWS = 100
    }
}
