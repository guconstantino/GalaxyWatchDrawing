package com.guconstantino.watchdraw.data

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DrawingViewModel @JvmOverloads constructor(
    app: Application,
    private val uploader: PhotoUploader = GooglePhotosUploader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(app) {

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

    // Position (px) of the floating tool button on the canvas. Null = not placed
    // yet (a default is computed once the canvas size is known). Kept in the
    // ViewModel so the parked position survives opening/closing the tool pickers.
    var fabOffset by mutableStateOf<Offset?>(null)

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
     * Saved drawings (My draws) + Favorites + Trash
     * --------------------------------------------------------------------- */

    // All non-deleted drawings (both regular and favorites).
    private val _myDraws = mutableStateListOf<Drawing>()

    // My draws: non-favorite drawings ordered newest-first.
    val myDraws: List<Drawing>
        get() = _myDraws.filter { !it.isFavorite }.sortedByDescending { it.createdAt }

    // Favorites: favorite drawings ordered newest-first.
    val favorites: List<Drawing>
        get() = _myDraws.filter { it.isFavorite }.sortedByDescending { it.createdAt }

    private val _trash = mutableStateListOf<Drawing>()
    val trash: List<Drawing> get() = _trash

    /** Shared gallery index — reset by each screen's open function. */
    var galleryIndex by mutableStateOf(0)
        private set

    /** Id of the drawing currently being edited (null = a brand new drawing). */
    private var editingId: String? = null

    /* --------------------------------------------------------------------- *
     * Settings / Google account
     * --------------------------------------------------------------------- */

    /** The signed-in Google user, or null if nobody is signed in. */
    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    /* --------------------------------------------------------------------- *
     * Google Photos sync queue
     * --------------------------------------------------------------------- */

    private val _syncQueue = mutableStateListOf<SyncQueue.Item>()

    /** How many downloaded drawings are still waiting to reach Google Photos. */
    val syncPendingCount: Int get() = _syncQueue.size

    /** Current sync activity, observed by the Profile screen. */
    var syncState by mutableStateOf<SyncState>(SyncState.Idle)
        private set

    /** Ids of drawings already uploaded to Google Photos (for the cloud badge). */
    var syncedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    private var syncing = false
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        _myDraws.addAll(DrawingStore.load(app, DRAWS_FILE))
        _trash.addAll(DrawingStore.load(app, TRASH_FILE))
        cleanupTrash()
        userProfile = AuthManager.lastProfile(app)
        _syncQueue.addAll(SyncQueue.load(app))
        syncedIds = SyncQueue.loadSyncedIds(app)
        // Retry any uploads that were left pending (e.g. offline last time).
        if (userProfile != null) processSyncQueue()
    }

    /** Cloud-sync status of a given drawing, for the gallery badge. */
    fun syncStatusFor(drawingId: String): DrawingSyncStatus {
        val queued = _syncQueue.firstOrNull { it.drawingId == drawingId }
        return when {
            queued != null -> if (queued.attempts > 0) DrawingSyncStatus.FAILED else DrawingSyncStatus.PENDING
            drawingId in syncedIds -> DrawingSyncStatus.SYNCED
            else -> DrawingSyncStatus.NONE
        }
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

    /** Opens the My draws gallery (no-op if there are no non-favorite drawings). */
    fun openMyDraws() {
        val draws = myDraws
        if (draws.isEmpty()) return
        galleryIndex = galleryIndex.coerceIn(0, draws.lastIndex)
        currentScreen = AppScreen.MyDraws
    }

    val currentGalleryDrawing: Drawing? get() = myDraws.getOrNull(galleryIndex)

    fun galleryNext() {
        val size = myDraws.size
        if (size > 1) galleryIndex = (galleryIndex + 1) % size
    }

    fun galleryPrev() {
        val size = myDraws.size
        if (size > 1) galleryIndex = (galleryIndex - 1 + size) % size
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

    /** Moves the selected My draws drawing to the Trash. */
    fun deleteCurrentGalleryToTrash() {
        val d = currentGalleryDrawing ?: return
        val rawIdx = _myDraws.indexOfFirst { it.id == d.id }
        if (rawIdx < 0) return
        val deleted = _myDraws.removeAt(rawIdx).copy(deletedAt = System.currentTimeMillis(), isFavorite = false)
        _trash.add(0, deleted)
        persist(DRAWS_FILE, _myDraws)
        persist(TRASH_FILE, _trash)
        val remaining = myDraws
        if (remaining.isEmpty()) {
            currentScreen = AppScreen.Home
        } else {
            galleryIndex = galleryIndex.coerceIn(0, remaining.lastIndex)
        }
    }

    /* --------------------------------------------------------------------- *
     * Favorites interactions
     * --------------------------------------------------------------------- */

    /** Opens the Favorites gallery (no-op if there are no favorites). */
    fun openFavorites() {
        val favs = favorites
        if (favs.isEmpty()) return
        galleryIndex = 0
        currentScreen = AppScreen.Favorites
    }

    val currentFavoriteDrawing: Drawing? get() = favorites.getOrNull(galleryIndex)

    fun favoriteNext() {
        val size = favorites.size
        if (size > 1) galleryIndex = (galleryIndex + 1) % size
    }

    fun favoritePrev() {
        val size = favorites.size
        if (size > 1) galleryIndex = (galleryIndex - 1 + size) % size
    }

    /** Marks the current My draws drawing as favorite (moves it to Favorites view). */
    fun favoriteCurrentGallery() {
        val d = currentGalleryDrawing ?: return
        val rawIdx = _myDraws.indexOfFirst { it.id == d.id }
        if (rawIdx < 0) return
        _myDraws[rawIdx] = _myDraws[rawIdx].copy(isFavorite = true)
        persist(DRAWS_FILE, _myDraws)
        val remaining = myDraws
        if (remaining.isEmpty()) {
            currentScreen = AppScreen.Home
        } else {
            galleryIndex = galleryIndex.coerceIn(0, remaining.lastIndex)
        }
    }

    /** Unmarks the current Favorites drawing (moves it back to My draws view). */
    fun unfavoriteCurrentFavorite() {
        val d = currentFavoriteDrawing ?: return
        val rawIdx = _myDraws.indexOfFirst { it.id == d.id }
        if (rawIdx < 0) return
        _myDraws[rawIdx] = _myDraws[rawIdx].copy(isFavorite = false)
        persist(DRAWS_FILE, _myDraws)
        val remaining = favorites
        if (remaining.isEmpty()) {
            currentScreen = AppScreen.Home
        } else {
            galleryIndex = galleryIndex.coerceIn(0, remaining.lastIndex)
        }
    }

    /** Opens the current Favorites drawing in the canvas for editing. */
    fun editCurrentFavorite() {
        val d = currentFavoriteDrawing ?: return
        editingId = d.id
        _drawnPaths.clear()
        _drawnPaths.addAll(d.paths)
        _undonePaths.clear()
        currentPoints = emptyList()
        currentScreen = AppScreen.Canvas
    }

    /** Moves the current Favorites drawing to the Trash. */
    fun deleteCurrentFavoriteToTrash() {
        val d = currentFavoriteDrawing ?: return
        val rawIdx = _myDraws.indexOfFirst { it.id == d.id }
        if (rawIdx < 0) return
        val deleted = _myDraws.removeAt(rawIdx).copy(deletedAt = System.currentTimeMillis(), isFavorite = false)
        _trash.add(0, deleted)
        persist(DRAWS_FILE, _myDraws)
        persist(TRASH_FILE, _trash)
        val remaining = favorites
        if (remaining.isEmpty()) {
            currentScreen = AppScreen.Home
        } else {
            galleryIndex = galleryIndex.coerceIn(0, remaining.lastIndex)
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

    /**
     * Saves the current canvas drawing and returns its id (or null if empty),
     * keeping [editingId] set so a later exit updates the same drawing instead of
     * creating a duplicate. Used by the Download action on the canvas so the
     * upload can be linked to a concrete drawing for the cloud badge.
     */
    fun commitCurrentDrawingForDownload(): String? {
        if (_drawnPaths.isEmpty()) return null
        val snapshot = _drawnPaths.toList()
        val id = editingId
        return if (id == null) {
            if (_myDraws.size >= MAX_DRAWS) _myDraws.removeAt(_myDraws.lastIndex)
            val created = Drawing(newId(), System.currentTimeMillis(), snapshot)
            _myDraws.add(0, created)
            editingId = created.id
            galleryIndex = 0
            persist(DRAWS_FILE, _myDraws)
            created.id
        } else {
            val idx = _myDraws.indexOfFirst { it.id == id }
            if (idx >= 0) _myDraws[idx] = _myDraws[idx].copy(paths = snapshot)
            else _myDraws.add(0, Drawing(id, System.currentTimeMillis(), snapshot))
            persist(DRAWS_FILE, _myDraws)
            id
        }
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

    /* --------------------------------------------------------------------- *
     * Settings navigation + account + reset
     * --------------------------------------------------------------------- */

    /** Opens Settings: the Profile card when signed in, otherwise the login screen. */
    fun openSettings() {
        currentScreen = if (userProfile != null) AppScreen.Profile else AppScreen.Settings
    }

    /** Called by the activity after a successful Google sign-in. */
    fun onSignedIn(profile: UserProfile) {
        userProfile = profile
        currentScreen = AppScreen.Profile
        // Flush anything queued while logged out.
        processSyncQueue()
    }

    /** Signs out and returns to the (logged-out) Settings screen. */
    fun signOut() {
        AuthManager.signOut(getApplication()) {
            userProfile = null
            currentScreen = AppScreen.Settings
        }
    }

    fun openResetConfirm() {
        currentScreen = AppScreen.ResetConfirm
    }

    /** Cancels the reset, returning to wherever the user came from. */
    fun cancelReset() {
        currentScreen = if (userProfile != null) AppScreen.Profile else AppScreen.Settings
    }

    /** Permanently deletes every drawing (My draws, Favorites, Trash and sync queue). */
    fun resetAllData() {
        _myDraws.clear()
        _trash.clear()
        _syncQueue.clear()
        syncedIds = emptySet()
        galleryIndex = 0
        editingId = null
        clearCanvas()
        persist(DRAWS_FILE, _myDraws)
        persist(TRASH_FILE, _trash)
        SyncQueue.clear(getApplication())
        syncState = SyncState.Idle
        currentScreen = AppScreen.ResetSuccess
    }

    /* --------------------------------------------------------------------- *
     * Google Photos sync
     * --------------------------------------------------------------------- */

    /**
     * Queues [bitmap] for upload to Google Photos and kicks off processing.
     * Called by the Download action when the user is signed in. The PNG is
     * persisted immediately so it survives a restart if the upload fails.
     */
    fun enqueueForSync(bitmap: Bitmap, drawingId: String? = null) {
        syncScope.launch {
            val item = withContext(ioDispatcher) {
                val bytes = ByteArrayOutputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.toByteArray()
                }
                SyncQueue.enqueue(getApplication(), bytes, drawingId)
            }
            // A fresh upload supersedes any previous "synced" state for this drawing.
            if (drawingId != null) syncedIds = syncedIds - drawingId
            _syncQueue.add(item)
            processSyncQueue()
        }
    }

    /** Manual "Sync Now": force-process whatever is in the queue. */
    fun syncNow() {
        if (_syncQueue.isEmpty()) {
            syncState = SyncState.Finished(uploaded = 0, failed = 0)
            return
        }
        processSyncQueue()
    }

    /** Uploads every queued item, removing the ones that succeed. */
    private fun processSyncQueue() {
        if (syncing || userProfile == null || _syncQueue.isEmpty()) return
        syncing = true
        syncScope.launch {
            val snapshot = _syncQueue.toList()
            val total = snapshot.size
            var uploaded = 0
            var failed = 0
            syncState = SyncState.Syncing(done = 0, total = total)
            for (item in snapshot) {
                val bytes = withContext(ioDispatcher) { SyncQueue.readBytes(getApplication(), item) }
                val result = if (bytes == null) {
                    UploadResult.Failed("missing file")
                } else {
                    uploader.upload(getApplication(), bytes)
                }
                when (result) {
                    is UploadResult.Success -> {
                        withContext(ioDispatcher) { SyncQueue.deleteFile(getApplication(), item) }
                        _syncQueue.removeAll { it.id == item.id }
                        item.drawingId?.let { syncedIds = syncedIds + it }
                        uploaded++
                    }
                    is UploadResult.NotSignedIn,
                    is UploadResult.NeedsConsent -> {
                        // Auth problem affects every item — stop and leave the rest queued.
                        failed++
                        break
                    }
                    is UploadResult.Failed -> {
                        item.attempts++
                        failed++
                    }
                }
                syncState = SyncState.Syncing(done = uploaded, total = total)
            }
            val ids = syncedIds
            withContext(ioDispatcher) {
                SyncQueue.saveIndex(getApplication(), _syncQueue)
                SyncQueue.saveSyncedIds(getApplication(), ids)
            }
            syncState = SyncState.Finished(uploaded = uploaded, failed = failed)
            syncing = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncScope.cancel()
    }

    companion object {
        private const val DRAWS_FILE = "my_draws.json"
        private const val TRASH_FILE = "trash.json"
        private const val MAX_DRAWS = 100
    }
}
