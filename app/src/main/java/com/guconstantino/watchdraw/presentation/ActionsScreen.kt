package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel

@Composable
fun ActionsScreen(viewModel: DrawingViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircleAction(label = "↶", background = Color.DarkGray) {
                    viewModel.undo()
                    viewModel.currentScreen = AppScreen.Canvas
                }
                CircleAction(label = "🗑", background = Color(0xFFB00020)) {
                    viewModel.currentScreen = AppScreen.ClearConfirm
                }
            }

            CircleAction(label = "✕", background = Color.DarkGray, size = 36) {
                viewModel.currentScreen = AppScreen.Canvas
            }
        }
    }
}

@Composable
fun ClearConfirmScreen(viewModel: DrawingViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Clear canvas?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircleAction(label = "✕", background = Color.DarkGray) {
                    viewModel.currentScreen = AppScreen.Canvas
                }
                CircleAction(label = "🗑", background = Color(0xFFB00020)) {
                    viewModel.clearCanvas()
                    viewModel.currentScreen = AppScreen.Canvas
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    label: String,
    background: Color,
    size: Int = 48,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 18.sp)
    }
}
