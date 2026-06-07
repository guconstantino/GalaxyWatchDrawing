package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingColors
import com.guconstantino.watchdraw.data.DrawingViewModel

/* ----------------------------------------------------------------------------
 * Modal scaffolding — every bottom-bar control opens a centered card over the
 * canvas (matching the prototype), dimming the rest with a scrim.
 * ------------------------------------------------------------------------- */

@Composable
private fun MenuOverlay(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    // Subtle haptic tick the moment any menu opens (respects system haptic settings).
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim — tap outside the card to dismiss.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
        content()
    }
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    background: Color = SurfaceCard,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(background)
            // Swallow taps so clicking the card doesn't dismiss the overlay.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SurfaceIconButton(
    onClick: () -> Unit,
    background: Color = PrimaryContainer,
    content: @Composable () -> Unit
) {
    // 48dp touch target with a 32dp visual circle (matches the M3 icon-button).
    Box(
        modifier = Modifier
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

/* ----------------------------------------------------------------------------
 * Stroke width menu (left button)
 * ------------------------------------------------------------------------- */

@Composable
fun StrokeMenu(viewModel: DrawingViewModel) {
    // Top -> bottom: thick, medium, thin.
    // Each entry = (brush width applied when tapped, preview line height per Figma).
    val options = listOf(
        18f to 8.dp,
        9f to 4.dp,
        3f to 2.dp
    )
    MenuOverlay(onDismiss = { viewModel.currentScreen = AppScreen.Canvas }) {
        MenuCard(
            modifier = Modifier.width(150.dp),
            padding = 12.dp,
            background = SurfaceContainer,
            cornerRadius = 26.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                options.forEach { (brushWidth, lineHeight) ->
                    // Flat tappable row (no fill) — gray theme shows only the line.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { viewModel.setStrokeWidth(brushWidth) },
                        contentAlignment = Alignment.Center
                    ) {
                        // Stroke preview: a rounded line (24dp wide) of varying height.
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(lineHeight)
                                .clip(CircleShape)
                                .background(OnPrimaryLine)
                        )
                    }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------------
 * Color menu (center button)
 * ------------------------------------------------------------------------- */

@Composable
fun ColorMenu(viewModel: DrawingViewModel) {
    MenuOverlay(onDismiss = { viewModel.currentScreen = AppScreen.Canvas }) {
        MenuCard(
            padding = 12.dp,
            background = SurfaceContainer,
            cornerRadius = 26.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DrawingColors.chunked(2).forEach { rowColors ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .clickable { viewModel.setColor(color) },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------------
 * Actions menu (right button) — 2x2 grid: clear, share, save, close
 * ------------------------------------------------------------------------- */

@Composable
fun ActionsMenu(viewModel: DrawingViewModel) {
    val context = LocalContext.current

    MenuOverlay(onDismiss = { viewModel.currentScreen = AppScreen.Canvas }) {
        MenuCard(padding = 12.dp, background = SurfaceContainer, cornerRadius = 26.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Trash -> clear confirmation
                    SurfaceIconButton(onClick = {
                        viewModel.currentScreen = AppScreen.ClearConfirm
                    }) { IconTrash(Modifier.size(24.dp)) }

                    // Download -> save locally + (if signed in) sync to Google Photos
                    SurfaceIconButton(onClick = {
                        val bmp = renderDrawingBitmap(viewModel, viewModel.canvasSize)
                        val id = viewModel.commitCurrentDrawingForDownload()
                        downloadDrawing(context, viewModel, bmp, id)
                        viewModel.currentScreen = AppScreen.Canvas
                    }) { IconDownload(Modifier.size(24.dp)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Heart -> visual toggle only (filled <-> outline); keeps the menu open
                    SurfaceIconButton(onClick = {
                        viewModel.toggleFavorite()
                    }) { IconHeart(Modifier.size(24.dp), filled = viewModel.favorite) }

                    // Close -> save and go to Home (tapping outside returns to the drawing)
                    SurfaceIconButton(onClick = {
                        viewModel.exitToHome()
                    }) { IconClose(Modifier.size(24.dp)) }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------------
 * Clear-canvas confirmation
 * ------------------------------------------------------------------------- */

@Composable
fun ClearConfirmMenu(viewModel: DrawingViewModel) {
    val context = LocalContext.current
    MenuOverlay(onDismiss = { viewModel.currentScreen = AppScreen.Canvas }) {
        MenuCard(padding = 12.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryContainer)
                        .clickable {
                            viewModel.clearCanvas()
                            hapticWarning(context)
                            viewModel.currentScreen = AppScreen.Canvas
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Clear Canvas",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                SurfaceIconButton(onClick = {
                    viewModel.currentScreen = AppScreen.Canvas
                }) { IconClose(Modifier.size(24.dp)) }
            }
        }
    }
}

@Composable
fun DeleteConfirmMenu(viewModel: DrawingViewModel) {
    MenuOverlay(onDismiss = { viewModel.currentScreen = AppScreen.Trash }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                modifier = Modifier.width(160.dp),
                padding = 12.dp,
                background = SurfaceContainer,
                cornerRadius = 28.dp
            ) {
                val context = LocalContext.current
                Column(
                    modifier = Modifier.clickable {
                        viewModel.permanentDeleteCurrentTrash()
                        hapticWarning(context)
                        viewModel.currentScreen = AppScreen.Trash
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Permanent",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "delete",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            SurfaceIconButton(
                onClick = { viewModel.currentScreen = AppScreen.Trash },
                background = SurfaceContainer
            ) {
                IconX(Modifier.size(24.dp))
            }
        }
    }
}
