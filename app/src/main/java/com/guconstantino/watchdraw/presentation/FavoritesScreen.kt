package com.guconstantino.watchdraw.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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

private val FavCrownActive = Color(0xFFE2E2E2)
private val FavCrownInactive = Color(0xFF5A5C5C)

/**
 * Favorites gallery. Identical layout to MyDrawsScreen but shows only favorited drawings.
 * Buttons: Trash | Unfavorite (filled heart) | Download | Edit.
 * Rotating or dragging navigates between favorites; back returns to Home.
 */
@Composable
fun FavoritesScreen(viewModel: DrawingViewModel) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }

    val drawing = viewModel.currentFavoriteDrawing
    val context = LocalContext.current
    var showUnfavMsg by remember { mutableStateOf(false) }
    var showDeletedMsg by remember { mutableStateOf(false) }

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
                                            viewModel.favoriteNext()
                                            hapticScrollTick(context)
                                            accum += vStep
                                        }
                                        while (accum >= vStep) {
                                            viewModel.favoritePrev()
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
                                            viewModel.favoriteNext()
                                            hapticScrollTick(context)
                                            accum -= rotStep
                                        }
                                        while (accum <= -rotStep) {
                                            viewModel.favoritePrev()
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

            // Segmented crown
            val count = viewModel.favorites.size
            val index = viewModel.galleryIndex
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (count <= 0) return@Canvas
                val strokeW = 7.dp.toPx()
                val inset = 5.dp.toPx() + strokeW / 2f
                val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
                val topLeft = Offset(inset, inset)
                val gapBottom = 64f
                val totalSweep = 360f - gapBottom
                val startAngle = 90f + gapBottom / 2f
                val segGap = if (count > 1) 4f else 0f
                val segSweep = (totalSweep - segGap * count) / count
                for (i in 0 until count) {
                    drawArc(
                        color = if (i == index) FavCrownActive else FavCrownInactive,
                        startAngle = startAngle + i * (segSweep + segGap),
                        sweepAngle = segSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            // Controls: Trash | Unfavorite | Download | Edit
            FavArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-66).dp, y = (-16).dp),
                onClick = {
                    viewModel.deleteCurrentFavoriteToTrash()
                    hapticWarning(context)
                    showDeletedMsg = true
                }
            ) { IconTrash(Modifier.size(22.dp)) }

            FavArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-28).dp, y = (-38).dp),
                onClick = {
                    viewModel.unfavoriteCurrentFavorite()
                    hapticSuccess(context)
                    showUnfavMsg = true
                }
            ) { IconHeart(Modifier.size(22.dp), filled = true) }

            FavArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 28.dp, y = (-38).dp),
                onClick = {
                    val bmp = renderPathsBitmap(drawing.paths, viewModel.canvasSize)
                    val ok = saveDrawingToGallery(context, bmp)
                    Toast.makeText(
                        context,
                        if (ok) "Saved to gallery" else "Save failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) { IconDownload(Modifier.size(22.dp)) }

            FavArcIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 66.dp, y = (-16).dp),
                onClick = { viewModel.editCurrentFavorite() }
            ) { IconEdit(Modifier.size(22.dp)) }
        }

        // "Unfavorited" overlay
        AnimatedVisibility(visible = showUnfavMsg, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Unfavorited",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            LaunchedEffect(Unit) {
                delay(800)
                showUnfavMsg = false
                if (viewModel.favorites.isEmpty()) {
                    viewModel.currentScreen = AppScreen.Home
                }
            }
        }

        // "Deleted" overlay
        AnimatedVisibility(visible = showDeletedMsg, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Deleted",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            LaunchedEffect(Unit) {
                delay(800)
                showDeletedMsg = false
                if (viewModel.favorites.isEmpty()) {
                    viewModel.currentScreen = AppScreen.Home
                }
            }
        }
    }
}

@Composable
private fun FavArcIconButton(
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
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
