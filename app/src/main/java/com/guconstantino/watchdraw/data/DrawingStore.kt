package com.guconstantino.watchdraw.data

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Simple file-based persistence for the user's drawings, using JSON (org.json,
 * bundled with Android — no extra dependency). Each collection (My draws, Trash)
 * is stored in its own file under the app's internal storage.
 */
object DrawingStore {

    fun load(context: Context, fileName: String): MutableList<Drawing> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return mutableListOf()
        return try {
            val root = JSONObject(file.readText())
            val arr = root.optJSONArray("draws") ?: JSONArray()
            val out = ArrayList<Drawing>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val pathsArr = o.getJSONArray("paths")
                val paths = ArrayList<DrawnPath>(pathsArr.length())
                for (j in 0 until pathsArr.length()) {
                    val po = pathsArr.getJSONObject(j)
                    val pts = po.getJSONArray("pts")
                    val points = ArrayList<Offset>(pts.length() / 2)
                    var k = 0
                    while (k + 1 < pts.length()) {
                        points.add(Offset(pts.getDouble(k).toFloat(), pts.getDouble(k + 1).toFloat()))
                        k += 2
                    }
                    paths.add(
                        DrawnPath(
                            points = points,
                            color = Color(po.getInt("color")),
                            strokeWidth = po.getDouble("w").toFloat()
                        )
                    )
                }
                out.add(Drawing(
                    o.getString("id"),
                    o.getLong("createdAt"),
                    paths,
                    if (o.has("deletedAt")) o.getLong("deletedAt") else null
                ))
            }
            out
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(context: Context, fileName: String, draws: List<Drawing>) {
        val arr = JSONArray()
        for (d in draws) {
            val o = JSONObject()
            o.put("id", d.id)
            o.put("createdAt", d.createdAt)
            if (d.deletedAt != null) o.put("deletedAt", d.deletedAt)
            val pathsArr = JSONArray()
            for (p in d.paths) {
                val po = JSONObject()
                po.put("color", p.color.toArgb())
                po.put("w", p.strokeWidth.toDouble())
                val pts = JSONArray()
                for (pt in p.points) {
                    pts.put(pt.x.toDouble())
                    pts.put(pt.y.toDouble())
                }
                po.put("pts", pts)
                pathsArr.put(po)
            }
            o.put("paths", pathsArr)
            arr.put(o)
        }
        try {
            File(context.filesDir, fileName).writeText(JSONObject().put("draws", arr).toString())
        } catch (e: Exception) {
            // best-effort persistence
        }
    }
}
