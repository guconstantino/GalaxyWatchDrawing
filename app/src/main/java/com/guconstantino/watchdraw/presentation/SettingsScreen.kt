package com.guconstantino.watchdraw.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// Danger (Reset / Delete All) colors, per the Figma "Caution" prototype.
private val DangerContainer = Color(0xFFFFB4AB)
private val OnDanger = Color(0xFF690005)

/* ------------------------------------------------------------------------- *
 * Settings — logged out: Login Google + Reset All
 * ------------------------------------------------------------------------- */

@Composable
fun SettingsScreen(viewModel: DrawingViewModel, onGoogleSignIn: () -> Unit) {
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
fun ProfileScreen(viewModel: DrawingViewModel) {
    BackHandler { viewModel.currentScreen = AppScreen.Home }
    val context = LocalContext.current
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

        SettingsPill(
            label = "Sync Now",
            background = PrimaryButton,
            content = OnPrimary,
            onClick = { Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show() }
        )
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

@Composable
private fun SettingsPill(
    label: String,
    background: Color,
    content: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
