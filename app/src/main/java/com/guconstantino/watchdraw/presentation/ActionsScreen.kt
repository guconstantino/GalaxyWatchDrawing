package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.CompactButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
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
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Undo
                CompactButton(
                    onClick = {
                        viewModel.undo()
                        viewModel.currentScreen = AppScreen.Canvas
                    },
                    modifier = Modifier.size(44.dp),
                    colors = CompactButtonDefaults.compactButtonColors(
                        containerColor = Color.DarkGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clear canvas
                CompactButton(
                    onClick = { viewModel.currentScreen = AppScreen.ClearConfirm },
                    modifier = Modifier.size(44.dp),
                    colors = CompactButtonDefaults.compactButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Close
            CompactButton(
                onClick = { viewModel.currentScreen = AppScreen.Canvas },
                modifier = Modifier.size(36.dp),
                colors = CompactButtonDefaults.compactButtonColors(
                    containerColor = Color.DarkGray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
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
                text = "Clear Canvas?",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Cancel
                CompactButton(
                    onClick = { viewModel.currentScreen = AppScreen.Canvas },
                    modifier = Modifier.size(44.dp),
                    colors = CompactButtonDefaults.compactButtonColors(
                        containerColor = Color.DarkGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Confirm clear
                CompactButton(
                    onClick = {
                        viewModel.clearCanvas()
                        viewModel.currentScreen = AppScreen.Canvas
                    },
                    modifier = Modifier.size(44.dp),
                    colors = CompactButtonDefaults.compactButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Confirm Clear",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
