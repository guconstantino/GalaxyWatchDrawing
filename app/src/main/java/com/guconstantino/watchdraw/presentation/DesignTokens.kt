package com.guconstantino.watchdraw.presentation

import androidx.compose.ui.graphics.Color

// Design tokens from the "M3 Wear OS Apps Design Kit" prototype, GRAY (neutral)
// theme. Hex values sampled from the Figma nodes in gray mode.
val WatchBlack = Color(0xFF000000)
val SurfaceContainer = Color(0xFF303131) // menu cards + side icon-buttons
val SurfaceCard = Color(0xFF303131)      // modal/menu card background
val PrimaryOrange = Color(0xFFFF9914)    // prominent/center action (brand accent)
val IconOnSurface = Color(0xFFE2E2E2)    // default icon color on surfaces
val IconOnContainer = Color(0xFFE0E0E0)  // icon color on container buttons
val Scrim = Color(0xCC000000)            // dimming behind modal menus

// Filled icon-button / pill background + line, in the gray theme.
val PrimaryContainer = Color(0xFF454747) // compact-button / pill background
val OnPrimaryLine = Color(0xFFE2E2E2)    // stroke-preview line color

// Gray-theme "primary" (filled emphasis button, e.g. Home > New draw).
val PrimaryButton = Color(0xFFE2E2E2)    // primary button background
val OnPrimary = Color(0xFF3E3F40)        // content on primary button
