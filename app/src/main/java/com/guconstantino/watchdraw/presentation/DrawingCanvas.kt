package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Root composable: the drawing canvas with the bottom arc controls, plus any
 * active menu rendered as a centered modal overlay on top (matching the prototype).
 */
@Composable
fun WatchDrawApp(viewModel: DrawingViewModel, onGoogleSignIn: () -> Unit = {}) {
    when (viewModel.currentScreen) {
        AppScreen.Home -> HomeScreen(viewModel)
        AppScreen.MyDraws -> MyDrawsScreen(viewModel)
        AppScreen.Favorites -> FavoritesScreen(viewModel)
        AppScreen.Trash -> TrashScreen(viewModel)
        AppScreen.Settings -> SettingsScreen(viewModel, onGoogleSignIn)
        AppScreen.Profile -> ProfileScreen(viewModel)
        AppScreen.ResetConfirm -> ResetConfirmScreen(viewModel)
        AppScreen.ResetSuccess -> ResetSuccessScreen(viewModel)
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
                            viewModel.startStroke(firstDown.position, firstDown.uptimeMillis)

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
                                    // Include sub-frame samples the system batched
                                    // for accurate fast strokes, then the latest.
                                    change.historical.forEach { h ->
                                        viewModel.addPoint(h.position, h.uptimeMillis)
                                    }
                                    viewModel.addPoint(change.position, change.uptimeMillis)
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

        // Floating tool button: a single draggable handle that keeps the canvas
        // clear while drawing. Tap expands it into the 3 tool buttons; tapping
        // outside collapses it back. Position is remembered in the ViewModel.
        var expanded by remember { mutableStateOf(false) }
        val density = LocalDensity.current
        val size = viewModel.canvasSize
        val fabPx = with(density) { 40.dp.toPx() }

        if (expanded) {
            // Transparent catcher: tapping anywhere outside the buttons collapses.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { expanded = false } }
            )

            // The 3 tool buttons in the bottom arc (Figma layout, node 144969:301).
            ArcButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-48).dp, y = (-30).dp),
                onClick = { viewModel.currentScreen = AppScreen.StrokePicker }
            ) {
                // Horizontal line; its thickness reflects the current stroke width.
                val h = (2f + viewModel.currentStrokeWidth / 18f * 4f).coerceIn(3f, 6f)
                Box(
                    Modifier
                        .size(width = 18.dp, height = h.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD4D4D4))
                )
            }
            ArcButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp),
                onClick = { viewModel.currentScreen = AppScreen.ColorPicker }
            ) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(viewModel.currentColor))
            }
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
                        Box(Modifier.size(3.dp).clip(CircleShape).background(IconOnSurface))
                    }
                }
            }
        } else if (size.width > 0 && size.height > 0) {
            // Collapsed: single draggable floating button.
            val defaultPos = Offset(
                x = size.width / 2f - fabPx / 2f,
                y = size.height - fabPx - with(density) { 16.dp.toPx() }
            )
            Box(
                modifier = Modifier
                    // Read the position inside offset{} so dragging moves it on the
                    // layout phase (no recomposition per frame).
                    .offset {
                        val p = viewModel.fabOffset ?: defaultPos
                        IntOffset(p.x.roundToInt(), p.y.roundToInt())
                    }
                    .size(40.dp)
                    // Tap expands into the tool buttons.
                    .pointerInput(size) {
                        detectTapGestures { expanded = true }
                    }
                    // Drag moves the button, using per-event deltas (fluid).
                    .pointerInput(size) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val cur = viewModel.fabOffset ?: defaultPos
                            viewModel.fabOffset = Offset(
                                (cur.x + dragAmount.x).coerceIn(0f, size.width - fabPx),
                                (cur.y + dragAmount.y).coerceIn(0f, size.height - fabPx)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(viewModel.currentColor)
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
        if (points.size == 2) {
            lineTo(points[1].x, points[1].y)
        } else {
            // Catmull-Rom cubic that passes through every real point.
            for (seg in catmullRomSegments(points)) {
                cubicTo(seg.c1.x, seg.c1.y, seg.c2.x, seg.c2.y, seg.end.x, seg.end.y)
            }
        }
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
