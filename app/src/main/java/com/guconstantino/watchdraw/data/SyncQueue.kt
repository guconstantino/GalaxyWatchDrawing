package com.guconstantino.watchdraw.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistent queue of drawings waiting to be uploaded to Google Photos.
 *
 * Each queued item is a PNG file under `filesDir/sync_queue/`, indexed by a small
 * JSON file. Keeping the bytes on disk (instead of in memory) means an upload that
 * failed while offline survives an app restart and is retried later. Decoupling the
 * queue from the drawing models also lets us queue brand-new canvas drawings that
 * were never saved to My draws.
 */
object SyncQueue {

    data class Item(
        val id: String,
        val fileName: String,
        val createdAt: Long,
        var attempts: Int = 0,
        /** Id of the [Drawing] this upload belongs to, for the per-drawing badge. */
        val drawingId: String? = null
    )

    private const val DIR = "sync_queue"
    private const val INDEX = "sync_queue_index.json"
    private const val SYNCED = "synced_ids.json"

    private fun dir(context: Context): File =
        File(context.filesDir, DIR).apply { mkdirs() }

    /** Writes [pngBytes] to disk and appends an entry to the index. */
    fun enqueue(context: Context, pngBytes: ByteArray, drawingId: String? = null): Item {
        val id = System.currentTimeMillis().toString() + "_" + (0..9_999).random()
        val item = Item(id, "$id.png", System.currentTimeMillis(), drawingId = drawingId)
        File(dir(context), item.fileName).writeBytes(pngBytes)
        val items = load(context)
        items.add(item)
        saveIndex(context, items)
        return item
    }

    fun load(context: Context): MutableList<Item> {
        val file = File(context.filesDir, INDEX)
        if (!file.exists()) return mutableListOf()
        return try {
            val arr = JSONObject(file.readText()).optJSONArray("items") ?: JSONArray()
            val out = ArrayList<Item>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    Item(
                        id = o.getString("id"),
                        fileName = o.getString("fileName"),
                        createdAt = o.getLong("createdAt"),
                        attempts = o.optInt("attempts", 0),
                        drawingId = o.optString("drawingId", "").ifEmpty { null }
                    )
                )
            }
            out
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveIndex(context: Context, items: List<Item>) {
        val arr = JSONArray()
        for (it in items) {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("fileName", it.fileName)
                put("createdAt", it.createdAt)
                put("attempts", it.attempts)
                if (it.drawingId != null) put("drawingId", it.drawingId)
            })
        }
        try {
            File(context.filesDir, INDEX).writeText(JSONObject().put("items", arr).toString())
        } catch (e: Exception) {
            // best-effort persistence
        }
    }

    /** Reads the queued PNG bytes, or null if the file is missing/unreadable. */
    fun readBytes(context: Context, item: Item): ByteArray? =
        try {
            File(dir(context), item.fileName).readBytes()
        } catch (e: Exception) {
            null
        }

    /** Deletes a single item's PNG file (call after a successful upload). */
    fun deleteFile(context: Context, item: Item) {
        try {
            File(dir(context), item.fileName).delete()
        } catch (e: Exception) {
            // ignore
        }
    }

    /* ----- Ids of drawings already uploaded to Google Photos (for the badge) ----- */

    fun loadSyncedIds(context: Context): MutableSet<String> {
        val file = File(context.filesDir, SYNCED)
        if (!file.exists()) return mutableSetOf()
        return try {
            val arr = JSONObject(file.readText()).optJSONArray("ids") ?: JSONArray()
            val out = HashSet<String>(arr.length())
            for (i in 0 until arr.length()) out.add(arr.getString(i))
            out
        } catch (e: Exception) {
            mutableSetOf()
        }
    }

    fun saveSyncedIds(context: Context, ids: Set<String>) {
        val arr = JSONArray()
        for (id in ids) arr.put(id)
        try {
            File(context.filesDir, SYNCED).writeText(JSONObject().put("ids", arr).toString())
        } catch (e: Exception) {
            // best-effort persistence
        }
    }

    /** Wipes the whole queue (files + index + synced ids) — used by Reset All. */
    fun clear(context: Context) {
        try {
            dir(context).deleteRecursively()
        } catch (e: Exception) {
            // ignore
        }
        try {
            File(context.filesDir, INDEX).delete()
        } catch (e: Exception) {
            // ignore
        }
        try {
            File(context.filesDir, SYNCED).delete()
        } catch (e: Exception) {
            // ignore
        }
    }
}
