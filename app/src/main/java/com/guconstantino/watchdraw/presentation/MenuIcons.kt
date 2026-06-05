package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser

/**
 * Exact action-menu icons, taken verbatim from the Figma prototype SVGs
 * (node 145007:371). Each icon's viewBox dimensions match its content area inside
 * the 24dp icon grid, so we draw the path centered on the 24-unit grid at 1:1.
 */
private class IconSpec(pathData: String, val vbW: Float, val vbH: Float) {
    val data = pathData
}

// viewBox 14.4 x 16.8
private val TrashSpec = IconSpec(
    "M3 16.8C2.5 16.8 2.075 16.625 1.725 16.275C1.375 15.925 1.2 15.5 1.2 15V3H0.9C0.65 3 0.433333 2.91667 0.25 2.75C0.0833333 2.56667 0 2.35 0 2.1C0 1.85 0.0833333 1.64167 0.25 1.475C0.433333 1.29167 0.65 1.2 0.9 1.2H4.8V0.899999C4.8 0.649999 4.88333 0.441666 5.05 0.275C5.23333 0.0916667 5.45 0 5.7 0H8.7C8.95 0 9.15833 0.0916667 9.325 0.275C9.50833 0.441666 9.6 0.649999 9.6 0.899999V1.2H13.5C13.75 1.2 13.9583 1.29167 14.125 1.475C14.3083 1.64167 14.4 1.85 14.4 2.1C14.4 2.35 14.3083 2.56667 14.125 2.75C13.9583 2.91667 13.75 3 13.5 3H13.2V15C13.2 15.5 13.025 15.925 12.675 16.275C12.325 16.625 11.9 16.8 11.4 16.8H3ZM11.4 3H3V15H11.4V3ZM5.7 13.2C5.95 13.2 6.15833 13.1167 6.325 12.95C6.50833 12.7667 6.6 12.55 6.6 12.3V5.7C6.6 5.45 6.50833 5.24167 6.325 5.075C6.15833 4.89167 5.95 4.8 5.7 4.8C5.45 4.8 5.23333 4.89167 5.05 5.075C4.88333 5.24167 4.8 5.45 4.8 5.7V12.3C4.8 12.55 4.88333 12.7667 5.05 12.95C5.23333 13.1167 5.45 13.2 5.7 13.2ZM8.7 13.2C8.95 13.2 9.15833 13.1167 9.325 12.95C9.50833 12.7667 9.6 12.55 9.6 12.3V5.7C9.6 5.45 9.50833 5.24167 9.325 5.075C9.15833 4.89167 8.95 4.8 8.7 4.8C8.45 4.8 8.23333 4.89167 8.05 5.075C7.88333 5.24167 7.8 5.45 7.8 5.7V12.3C7.8 12.55 7.88333 12.7667 8.05 12.95C8.23333 13.1167 8.45 13.2 8.7 13.2Z",
    14.4f, 16.8f
)

// viewBox 16 x 16
private val DownloadSpec = IconSpec(
    "M8 11.575C7.86667 11.575 7.74167 11.5583 7.625 11.525C7.50833 11.475 7.4 11.4 7.3 11.3L3.7 7.7C3.5 7.5 3.4 7.26667 3.4 7C3.41667 6.73333 3.51667 6.5 3.7 6.3C3.9 6.1 4.13333 6 4.4 6C4.68333 5.98333 4.925 6.075 5.125 6.275L7 8.15V1C7 0.716667 7.09167 0.483333 7.275 0.3C7.475 0.0999999 7.71667 0 8 0C8.28333 0 8.51667 0.0999999 8.7 0.3C8.9 0.483333 9 0.716667 9 1V8.15L10.875 6.275C11.075 6.075 11.3083 5.98333 11.575 6C11.8583 6 12.1 6.1 12.3 6.3C12.4833 6.5 12.575 6.73333 12.575 7C12.5917 7.26667 12.5 7.5 12.3 7.7L8.7 11.3C8.6 11.4 8.49167 11.475 8.375 11.525C8.25833 11.5583 8.13333 11.575 8 11.575ZM2 16C1.45 16 0.975 15.8083 0.575 15.425C0.191667 15.025 0 14.55 0 14V12C0 11.7167 0.0916667 11.4833 0.275 11.3C0.475 11.1 0.716667 11 1 11C1.28333 11 1.51667 11.1 1.7 11.3C1.9 11.4833 2 11.7167 2 12V14H14V12C14 11.7167 14.0917 11.4833 14.275 11.3C14.475 11.1 14.7167 11 15 11C15.2833 11 15.5167 11.1 15.7 11.3C15.9 11.4833 16 11.7167 16 12V14C16 14.55 15.8 15.025 15.4 15.425C15.0167 15.8083 14.55 16 14 16H2Z",
    16f, 16f
)

// viewBox 20 x 17.675 — outer contour only (solid heart)
private val HeartFilledSpec = IconSpec(
    "M10 17.675C9.76667 17.675 9.525 17.6333 9.275 17.55C9.04167 17.4667 8.83333 17.3333 8.65 17.15L6.925 15.575C5.15833 13.9583 3.55833 12.3583 2.125 10.775C0.708334 9.175 0 7.41667 0 5.5C0 3.93333 0.525 2.625 1.575 1.575C2.625 0.525 3.93333 0 5.5 0C6.38333 0 7.21667 0.191667 8 0.575001C8.78333 0.941668 9.45 1.45 10 2.1C10.55 1.45 11.2167 0.941668 12 0.575001C12.7833 0.191667 13.6167 0 14.5 0C16.0667 0 17.375 0.525 18.425 1.575C19.475 2.625 20 3.93333 20 5.5C20 7.41667 19.2917 9.175 17.875 10.775C16.4583 12.375 14.85 13.9833 13.05 15.6L11.35 17.15C11.1667 17.3333 10.95 17.4667 10.7 17.55C10.4667 17.6333 10.2333 17.675 10 17.675Z",
    20f, 17.675f
)

// viewBox 20 x 17.675 — outer + inner contour (outline heart, via fill hole)
private val HeartOutlineSpec = IconSpec(
    HeartFilledSpec.data.removeSuffix("Z") +
        "ZM9.05 4.1C8.56667 3.41667 8.05 2.9 7.5 2.55C6.95 2.18333 6.28333 2 5.5 2C4.5 2 3.66667 2.33333 3 3C2.33333 3.66667 2 4.5 2 5.5C2 6.36667 2.30833 7.29167 2.925 8.275C3.54167 9.24167 4.275 10.1833 5.125 11.1C5.99167 12.0167 6.875 12.875 7.775 13.675C8.69167 14.475 9.43333 15.1333 10 15.65C10.5667 15.1333 11.3 14.475 12.2 13.675C13.1167 12.875 14 12.0167 14.85 11.1C15.7167 10.1833 16.4583 9.24167 17.075 8.275C17.6917 7.29167 18 6.36667 18 5.5C18 4.5 17.6667 3.66667 17 3C16.3333 2.33333 15.5 2 14.5 2C13.7167 2 13.05 2.18333 12.5 2.55C11.95 2.9 11.4333 3.41667 10.95 4.1C10.8333 4.26667 10.6917 4.39167 10.525 4.475C10.3583 4.55833 10.1833 4.6 10 4.6C9.81667 4.6 9.64167 4.55833 9.475 4.475C9.30833 4.39167 9.16667 4.26667 9.05 4.1Z",
    20f, 17.675f
)

// viewBox 11.3 x 11.3
private val CloseSpec = IconSpec(
    "M5.65 6.925L1.55 11.025C1.36667 11.2083 1.15833 11.3 0.925 11.3C0.691667 11.2833 0.483333 11.1833 0.3 11C0.116666 10.8167 0.0249996 10.6083 0.0249996 10.375C0.0249996 10.125 0.116666 9.90833 0.3 9.725L4.375 5.65L0.275 1.55C0.0916667 1.36667 0 1.15833 0 0.925C0.0166664 0.675 0.116666 0.458334 0.3 0.275C0.483333 0.0916667 0.691667 0 0.925 0C1.175 0 1.39167 0.0916667 1.575 0.275L5.65 4.375L9.75 0.275C9.93333 0.0916667 10.1417 0 10.375 0C10.625 0 10.8417 0.0916667 11.025 0.275C11.2083 0.458334 11.3 0.675 11.3 0.925C11.3 1.15833 11.2083 1.36667 11.025 1.55L6.925 5.65L11.025 9.75C11.2083 9.93334 11.3 10.1417 11.3 10.375C11.3 10.6083 11.2083 10.8167 11.025 11C10.8417 11.1833 10.625 11.275 10.375 11.275C10.1417 11.275 9.93333 11.1833 9.75 11L5.65 6.925Z",
    11.3f, 11.3f
)

@Composable
fun IconTrash(modifier: Modifier = Modifier, color: Color = IconOnContainer) =
    VectorIcon(TrashSpec, modifier, color)

@Composable
fun IconDownload(modifier: Modifier = Modifier, color: Color = IconOnContainer) =
    VectorIcon(DownloadSpec, modifier, color)

@Composable
fun IconClose(modifier: Modifier = Modifier, color: Color = IconOnContainer) =
    VectorIcon(CloseSpec, modifier, color)

@Composable
fun IconHeart(modifier: Modifier = Modifier, filled: Boolean = false, color: Color = IconOnContainer) =
    VectorIcon(if (filled) HeartFilledSpec else HeartOutlineSpec, modifier, color)

@Composable
private fun VectorIcon(spec: IconSpec, modifier: Modifier, color: Color) {
    val path = remember(spec.data) { PathParser().parsePathString(spec.data).toPath() }
    Canvas(modifier = modifier) {
        val unit = size.minDimension / 24f
        val offX = (24f - spec.vbW) / 2f
        val offY = (24f - spec.vbH) / 2f
        translate(offX * unit, offY * unit) {
            scale(unit, unit, pivot = Offset.Zero) {
                drawPath(path, color)
            }
        }
    }
}
