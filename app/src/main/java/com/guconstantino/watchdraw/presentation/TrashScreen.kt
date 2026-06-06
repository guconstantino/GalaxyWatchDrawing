package com.guconstantino.watchdraw.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

private val CrownActive = Color(0xFFE2E2E2)
private val CrownInactive = Color(0xFF5A5C5C)
private val TrashIconBg = Color(0xFFFFDAD4) // Light pink/red for delete button

@Composable
fun TrashScreen(viewModel: DrawingViewModel) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }

    val drawing = viewModel.currentTrashDrawing
    val trashEmpty = viewModel.trash.isEmpty()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (drawing != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { viewModel.canvasSize = it }
                    .pointerInput(Unit) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val pi = PI.toFloat()
                        val twoPi = (2 * PI).toFloat()
                        val rotStep = pi / 6f
                        val vStep = 64.dp.toPx()
                        val slop = 12.dp.toPx()
                        var mode = 0
                        var start = Offset.Zero
                        var accum = 0f
                        detectDragGestures(
                            onDragStart = { start = it; mode = 0; accum = 0f },
                            onDrag = { change, _ ->
                                if (mode == 0) {
                                    val tot = change.position - start
                                    if (tot.getDistance() > slop) {
                                        mode = if (abs(tot.y) >= abs(tot.x)) 1 else 2
                                    }
                                }
                                when (mode) {
                                    1 -> {
                                        accum += change.position.y - change.previousPosition.y
                                        while (accum <= -vStep) {
                                            viewModel.trashNext()
                                            hapticScrollTick(context)
                                            accum += vStep
                                        }
                                        while (accum >= vStep) {
                                            viewModel.trashPrev()
                                            hapticScrollTick(context)
                                            accum -= vStep
                                        }
                                    }
                                    2 -> {
                                        val a1 = atan2(change.previousPosition.y - cy, change.previousPosition.x - cx)
                                        val a2 = atan2(change.position.y - cy, change.position.x - cx)
                                        var d = a2 - a1
                                        if (d > pi) d -= twoPi else if (d < -pi) d += twoPi
                                        accum += d
                                        while (accum >= rotStep) {
                                            viewModel.trashNext()
                                            hapticScrollTick(context)
                                            accum -= rotStep
                                        }
                                        while (accum <= -rotStep) {
                                            viewModel.trashPrev()
                                            hapticScrollTick(context)
                                            accum += rotStep
                                        }
                                    }
                                }
                                change.consume()
                            }
                        )
                    }
            ) {
                drawing.paths.forEach { p ->
                    drawSmoothPath(p.points, p.color, p.strokeWidth)
                }
            }

            // Crown
            val count = viewModel.trash.size
            val index = viewModel.galleryIndex
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (count <= 0) return@Canvas
                val strokeW = 7.dp.toPx()
                val inset = 5.dp.toPx() + strokeW / 2f
                val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
                val topLeft = Offset(inset, inset)
                val gapBottom = 160f
                val totalSweep = 360f - gapBottom
                val startAngle = 90f + gapBottom / 2f
                val segGap = if (count > 1) 4f else 0f
                val segSweep = (totalSweep - segGap * count) / count
                for (i in 0 until count) {
                    drawArc(
                        color = if (i == index) CrownActive else CrownInactive,
                        startAngle = startAngle + i * (segSweep + segGap),
                        sweepAngle = segSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            // Controls — outer buttons at (±60, -48), center at (0, 0) from BottomCenter.
            ArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-60).dp, y = (-48).dp),
                background = TrashIconBg,
                onClick = { viewModel.currentScreen = AppScreen.DeleteConfirm }
            ) { IconDeletePermanent(Modifier.size(24.dp)) }

            ArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 0.dp, y = 0.dp),
                onClick = {
                    viewModel.restoreCurrentTrash()
                    hapticSuccess(context)
                }
            ) { IconRestore(Modifier.size(24.dp)) }

            ArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 60.dp, y = (-48).dp),
                onClick = { viewModel.editTrashAsNew() }
            ) { IconEdit(Modifier.size(24.dp)) }
        }

        // "No files" state
        if (trashEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.currentScreen = AppScreen.Home }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "No files",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcIconButton(
    modifier: Modifier = Modifier,
    background: Color = SurfaceContainer,
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
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
