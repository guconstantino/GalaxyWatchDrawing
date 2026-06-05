package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.CompactButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel

@Composable
fun DrawingCanvasScreen(viewModel: DrawingViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { viewModel.startStroke(it) },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.addPoint(change.position)
                        },
                        onDragEnd = { viewModel.endStroke() },
                        onDragCancel = { viewModel.endStroke() }
                    )
                }
        ) {
            // Draw completed paths
            viewModel.drawnPaths.forEach { drawnPath ->
                drawSmoothPath(drawnPath.points, drawnPath.color, drawnPath.strokeWidth)
            }
            // Draw live current path
            if (viewModel.currentPoints.size > 1) {
                drawSmoothPath(viewModel.currentPoints, viewModel.currentColor, viewModel.currentStrokeWidth)
            }
        }

        // Bottom toolbar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stroke width button
            CompactButton(
                onClick = { viewModel.currentScreen = AppScreen.StrokePicker },
                modifier = Modifier.size(32.dp),
                colors = CompactButtonDefaults.compactButtonColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.7f)
                )
            ) {
                StrokeIcon(strokeWidth = viewModel.currentStrokeWidth)
            }

            // Color picker button
            CompactButton(
                onClick = { viewModel.currentScreen = AppScreen.ColorPicker },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp),
                colors = CompactButtonDefaults.compactButtonColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.7f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(viewModel.currentColor)
                )
            }

            // Actions button
            CompactButton(
                onClick = { viewModel.currentScreen = AppScreen.Actions },
                modifier = Modifier.size(32.dp),
                colors = CompactButtonDefaults.compactButtonColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.7f)
                )
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 2.dp)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothPath(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float
) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        drawCircle(color = color, radius = strokeWidth / 2, center = points[0])
        return
    }

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 0 until points.size - 1) {
            val mid = Offset(
                (points[i].x + points[i + 1].x) / 2f,
                (points[i].y + points[i + 1].y) / 2f
            )
            quadraticBezierTo(points[i].x, points[i].y, mid.x, mid.y)
        }
        lineTo(points.last().x, points.last().y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Composable
private fun StrokeIcon(strokeWidth: Float) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = strokeWidth.coerceIn(1f, 6f)
        drawLine(
            color = Color.White,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = w,
            cap = StrokeCap.Round
        )
    }
}
