package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Root composable: the drawing canvas with the bottom arc controls, plus any
 * active menu rendered as a centered modal overlay on top (matching the prototype).
 */
@Composable
fun WatchDrawApp(viewModel: DrawingViewModel) {
    when (viewModel.currentScreen) {
        AppScreen.Home -> HomeScreen(viewModel)
        AppScreen.MyDraws -> MyDrawsScreen(viewModel)
        AppScreen.Favorites -> FavoritesScreen(viewModel)
        AppScreen.Trash -> TrashScreen(viewModel)
        else -> Box(modifier = Modifier.fillMaxSize()) {
            DrawingCanvasScreen(viewModel)
            when (viewModel.currentScreen) {
                AppScreen.StrokePicker -> StrokeMenu(viewModel)
                AppScreen.ColorPicker -> ColorMenu(viewModel)
                AppScreen.Actions -> ActionsMenu(viewModel)
                AppScreen.ClearConfirm -> ClearConfirmMenu(viewModel)
                AppScreen.DeleteConfirm -> DeleteConfirmMenu(viewModel)
                else -> {} // Canvas: no overlay
            }
        }
    }
}

@Composable
fun DrawingCanvasScreen(viewModel: DrawingViewModel) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewModel.canvasSize = it }
                .pointerInput(Unit) {
                    coroutineScope {
                        var lastTwoFingerTapTime = 0L
                        var undoJob: Job? = null

                        awaitEachGesture {
                            // Wait for the first finger down
                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            viewModel.startStroke(firstDown.position)

                            while (true) {
                                val event = awaitPointerEvent()
                                val fingers = event.changes.filter { it.pressed }

                                if (fingers.size >= 2) {
                                    viewModel.cancelStroke()
                                    
                                    // Consume everything to stop other interactions
                                    event.changes.forEach { it.consume() }

                                    var moved = false
                                    val slop = viewConfiguration.touchSlop
                                    
                                    // Wait for all fingers to be released
                                    var currentEvent = event
                                    while (currentEvent.changes.any { it.pressed }) {
                                        currentEvent = awaitPointerEvent()
                                        currentEvent.changes.forEach { it.consume() }
                                        if (currentEvent.changes.any { (it.position - it.previousPosition).getDistance() > slop }) {
                                            moved = true
                                        }
                                    }

                                    if (!moved) {
                                        val now = System.currentTimeMillis()
                                        if ((now - lastTwoFingerTapTime) < 300) {
                                            undoJob?.cancel()
                                            viewModel.redo()
                                            hapticRedo(context)   // quick double tick
                                            lastTwoFingerTapTime = 0
                                        } else {
                                            lastTwoFingerTapTime = now
                                            undoJob = launch {
                                                delay(300)
                                                viewModel.undo()
                                                hapticUndo(context)   // single tick
                                            }
                                        }
                                    }
                                    break 
                                } else if (fingers.size == 1) {
                                    val change = event.changes.first { it.pressed }
                                    viewModel.addPoint(change.position)
                                    change.consume()
                                } else {
                                    // No fingers pressed anymore
                                    viewModel.endStroke()
                                    break
                                }
                            }
                        }
                    }
                }
        ) {
            viewModel.drawnPaths.forEach { drawnPath ->
                drawSmoothPath(drawnPath.points, drawnPath.color, drawnPath.strokeWidth)
            }
            if (viewModel.currentPoints.size > 1) {
                drawSmoothPath(viewModel.currentPoints, viewModel.currentColor, viewModel.currentStrokeWidth)
            }
        }

        // Bottom controls laid out in a downward arc that hugs the round bezel.
        // Side buttons sit higher; the prominent color button sits lower-center.
        // (Figma: side buttons centered at y=144, center button at y=168 on a 192dp watch.)

        // Left — stroke width (small dot indicator)
        ArcButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-48).dp, y = (-30).dp),
            onClick = { viewModel.currentScreen = AppScreen.StrokePicker }
        ) {
            val dot = (4f + viewModel.currentStrokeWidth / 18f * 10f).coerceIn(5f, 14f)
            Box(
                modifier = Modifier
                    .size(dot.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD4D4D4))
            )
        }

        // Center — current color (large colored swatch), prominent
        ArcButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp),
            onClick = { viewModel.currentScreen = AppScreen.ColorPicker }
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(viewModel.currentColor)
            )
        }

        // Right — actions / more (three vertical dots)
        ArcButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = 48.dp, y = (-30).dp),
            onClick = { viewModel.currentScreen = AppScreen.Actions }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(IconOnSurface)
                    )
                }
            }
        }
    }
}

/**
 * A circular surface-container button with a 48dp touch target and a 32dp visual
 * circle, matching the M3 Wear OS icon-button in the prototype.
 */
@Composable
private fun ArcButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

internal fun DrawScope.drawSmoothPath(
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
