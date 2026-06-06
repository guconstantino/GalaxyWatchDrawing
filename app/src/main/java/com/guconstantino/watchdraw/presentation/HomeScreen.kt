package com.guconstantino.watchdraw.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.guconstantino.watchdraw.data.DrawingViewModel

/**
 * Home menu (Figma node 144969:299): a vertical, scrollable list of action
 * buttons. Only "New draw" is wired for now — the rest are placeholders to be
 * implemented feature by feature.
 */
@Composable
fun HomeScreen(viewModel: DrawingViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBlack)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeButton(
            label = "New draw",
            background = PrimaryButton,
            content = OnPrimary,
            icon = { IconHomePlus(Modifier.size(26.dp), OnPrimary) },
            onClick = { viewModel.newDrawing() }
        )
        HomeButton(
            label = "My draws",
            background = PrimaryContainer,
            content = IconOnContainer,
            icon = { IconHomeMyDraws(Modifier.size(26.dp), IconOnContainer) },
            onClick = {
                if (viewModel.myDraws.isEmpty()) {
                    Toast.makeText(context, "My draws is empty", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.openMyDraws()
                }
            }
        )
        HomeButton(
            label = "Favorites",
            background = PrimaryContainer,
            content = IconOnContainer,
            icon = { IconHomeFavorite(Modifier.size(26.dp), IconOnContainer) },
            onClick = {
                if (viewModel.favorites.isEmpty()) {
                    Toast.makeText(context, "Favorites is empty", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.openFavorites()
                }
            }
        )
        HomeButton(
            label = "Trash",
            background = PrimaryContainer,
            content = IconOnContainer,
            icon = { IconHomeTrash(Modifier.size(26.dp), IconOnContainer) },
            onClick = {
                if (viewModel.trash.isEmpty()) {
                    Toast.makeText(context, "Trash is empty", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.openTrash()
                }
            }
        )
        HomeButton(
            label = "Settings",
            background = PrimaryContainer,
            content = IconOnContainer,
            icon = { IconHomeSettings(Modifier.size(26.dp), IconOnContainer) },
            onClick = { /* TODO: feature */ }
        )
    }
}

@Composable
private fun HomeButton(
    label: String,
    background: Color,
    content: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(160.dp)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()
        Text(
            text = label,
            color = content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
