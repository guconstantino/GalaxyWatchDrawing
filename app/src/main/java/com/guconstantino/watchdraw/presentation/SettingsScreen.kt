package com.guconstantino.watchdraw.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.SyncState

// Danger (Reset / Delete All) colors, per the Figma "Caution" prototype.
private val DangerContainer = Color(0xFFFFB4AB)
private val OnDanger = Color(0xFF690005)

/* ------------------------------------------------------------------------- *
 * Settings — logged out: Login Google + Reset All
 * ------------------------------------------------------------------------- */

@Composable
fun SettingsScreen(
    viewModel: DrawingViewModel,
    onGoogleSignIn: () -> Unit,
    onBuyPro: () -> Unit = {}
) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBlack)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 56.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoogleSignInButton(onClick = onGoogleSignIn)
        ProRow(isPro = viewModel.isPro, price = viewModel.proPriceText, onBuyPro = onBuyPro)
        SettingsPill(
            label = "Reset All",
            background = PrimaryContainer,
            content = IconOnContainer,
            onClick = { viewModel.openResetConfirm() }
        )
    }
}

/* ------------------------------------------------------------------------- *
 * Profile — logged in: avatar + name + email + Sync Now + Reset All + Logout
 * ------------------------------------------------------------------------- */

@Composable
fun ProfileScreen(viewModel: DrawingViewModel, onBuyPro: () -> Unit = {}) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }
    val user = viewModel.userProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBlack)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (user?.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.take(1)?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "Hello, ${user?.name.orEmpty()}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = user?.email.orEmpty(),
            color = IconOnSurface,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val pending = viewModel.syncPendingCount
        val syncState = viewModel.syncState
        if (syncState is SyncState.Syncing) {
            // While uploading, the pill itself becomes the progress indicator.
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PrimaryButton)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniSpinner(color = OnPrimary)
                    Text(
                        text = "Syncing ${syncState.done}/${syncState.total}",
                        color = OnPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            SettingsPill(
                label = if (pending > 0) "Sync Now ($pending)" else "Sync Now",
                background = PrimaryButton,
                content = OnPrimary,
                enabled = pending > 0,
                onClick = { viewModel.syncNow() }
            )
        }
        val syncStatus: String? = when (syncState) {
            is SyncState.Syncing -> null // the pill already shows progress
            is SyncState.Finished -> when {
                syncState.failed > 0 -> "${syncState.uploaded} synced · ${syncState.failed} failed"
                syncState.uploaded > 0 -> "All synced ✓"
                else -> if (pending > 0) "$pending pending" else null
            }
            SyncState.Idle -> if (pending > 0) "$pending pending" else null
        }
        syncStatus?.let {
            Text(
                text = it,
                color = IconOnSurface,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
        ProRow(isPro = viewModel.isPro, price = viewModel.proPriceText, onBuyPro = onBuyPro)
        SettingsPill(
            label = "Reset All",
            background = PrimaryContainer,
            content = IconOnContainer,
            onClick = { viewModel.openResetConfirm() }
        )
        SettingsPill(
            label = "Logout",
            background = PrimaryContainer,
            content = IconOnContainer,
            onClick = { viewModel.signOut() }
        )
    }
}

/* ------------------------------------------------------------------------- *
 * WatchDraw Pro row — buy when not owned, badge when owned.
 * ------------------------------------------------------------------------- */

@Composable
private fun ProRow(isPro: Boolean, price: String?, onBuyPro: () -> Unit) {
    when {
        isPro -> SettingsPill(
            label = "WatchDraw Pro ✓",
            background = PrimaryButton,
            content = OnPrimary,
            onClick = {}
        )
        // Only offer to buy when the product actually loaded from Play (price
        // known). Until the Play Console product exists, show nothing — no dead
        // button.
        price != null -> SettingsPill(
            label = "Unlock Pro · $price",
            background = PrimaryButton,
            content = OnPrimary,
            onClick = onBuyPro
        )
    }
}

/* ------------------------------------------------------------------------- *
 * Reset confirmation — "Caution / This action is irreversible"
 * ------------------------------------------------------------------------- */

@Composable
fun ResetConfirmScreen(viewModel: DrawingViewModel) {
    BackHandler { viewModel.cancelReset() }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Caution",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This action is irreversible",
                color = IconOnSurface,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            SettingsPill(
                label = "Cancel",
                background = PrimaryContainer,
                content = IconOnContainer,
                onClick = { viewModel.cancelReset() }
            )
            SettingsPill(
                label = "Delete All",
                background = DangerContainer,
                content = OnDanger,
                onClick = {
                    viewModel.resetAllData()
                    hapticWarning(context)
                }
            )
        }
    }
}

/* ------------------------------------------------------------------------- *
 * Reset success — "All files have been removed."
 * ------------------------------------------------------------------------- */

@Composable
fun ResetSuccessScreen(viewModel: DrawingViewModel) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.currentScreen = AppScreen.Home }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "All files have\nbeen removed.",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        )
    }
}

/* ------------------------------------------------------------------------- */

/** A small, subtle indeterminate spinner (rotating arc) used during sync. */
@Composable
private fun MiniSpinner(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sync-spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800, easing = LinearEasing)),
        label = "sync-spinner-angle"
    )
    Canvas(modifier = modifier.size(14.dp)) {
        val stroke = 2.dp.toPx()
        val inset = stroke / 2f
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun SettingsPill(
    label: String,
    background: Color,
    content: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (enabled) background else background.copy(alpha = 0.4f))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) content else content.copy(alpha = 0.5f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
