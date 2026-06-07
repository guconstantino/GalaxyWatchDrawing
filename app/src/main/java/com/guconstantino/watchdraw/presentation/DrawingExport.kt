package com.guconstantino.watchdraw.presentation

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import com.guconstantino.watchdraw.data.AuthManager
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.DrawnPath
import java.io.File
import java.io.FileOutputStream

/**
 * Rasterizes the current drawing (black background + all committed strokes) into a
 * Bitmap matching the on-screen canvas size. Mirrors [drawSmoothPath] using the
 * Android graphics API so the exported image looks identical to the canvas.
 */
fun renderDrawingBitmap(viewModel: DrawingViewModel, size: IntSize): Bitmap =
    renderPathsBitmap(viewModel.drawnPaths, size)

/** Rasterizes an arbitrary list of strokes (black background) into a Bitmap. */
fun renderPathsBitmap(paths: List<DrawnPath>, size: IntSize): Bitmap {
    val w = size.width.coerceAtLeast(1)
    val h = size.height.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.BLACK)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    paths.forEach { drawn ->
        drawAndroidPath(canvas, paint, drawn.points, drawn.color.toArgb(), drawn.strokeWidth)
    }
    return bitmap
}

private fun drawAndroidPath(
    canvas: android.graphics.Canvas,
    paint: Paint,
    points: List<Offset>,
    color: Int,
    strokeWidth: Float
) {
    if (points.isEmpty()) return
    paint.color = color
    paint.strokeWidth = strokeWidth

    if (points.size == 1) {
        val saved = paint.style
        paint.style = Paint.Style.FILL
        canvas.drawCircle(points[0].x, points[0].y, strokeWidth / 2f, paint)
        paint.style = saved
        return
    }

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        if (points.size == 2) {
            lineTo(points[1].x, points[1].y)
        } else {
            // Same Catmull-Rom curve as the on-screen renderer (WYSIWYG export).
            for (seg in catmullRomSegments(points)) {
                cubicTo(seg.c1.x, seg.c1.y, seg.c2.x, seg.c2.y, seg.end.x, seg.end.y)
            }
        }
    }
    canvas.drawPath(path, paint)
}

/** Writes [bitmap] to the share cache and launches the system share sheet. */
fun shareDrawing(context: Context, bitmap: Bitmap) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "watchdraw.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, "Share drawing")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

/**
 * Handles the Download action everywhere: always saves [bitmap] to the local
 * gallery, and when the user is signed in also queues it for upload to their
 * Google Photos. The queue ([DrawingViewModel.enqueueForSync]) persists the image
 * and retries later if the watch is offline, so this returns immediately.
 */
fun downloadDrawing(
    context: Context,
    viewModel: DrawingViewModel,
    bitmap: Bitmap,
    drawingId: String? = null
) {
    val savedLocally = saveDrawingToGallery(context, bitmap)

    // Logged-out users keep the local-only behavior; nothing leaves the device.
    if (AuthManager.account(context) == null) {
        Toast.makeText(
            context,
            if (savedLocally) "Saved to gallery" else "Save failed",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    viewModel.enqueueForSync(bitmap, drawingId)
    Toast.makeText(
        context,
        if (savedLocally) "Saved · syncing to Photos…" else "Save failed",
        Toast.LENGTH_SHORT
    ).show()
}

/** Saves [bitmap] to the device gallery (Pictures/WatchDraw) via MediaStore. */
fun saveDrawingToGallery(context: Context, bitmap: Bitmap): Boolean {
    val name = "watchdraw_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WatchDraw")
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: return false
    return try {
        resolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        } != null
    } catch (e: Exception) {
        false
    }
}
