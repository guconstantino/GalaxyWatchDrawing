package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.StrokeWidths

@Composable
fun StrokePickerScreen(viewModel: DrawingViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StrokeWidths.forEach { width ->
                val isSelected = width == viewModel.currentStrokeWidth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((width.coerceIn(2f, 20f) + 16f).dp)
                        .clickable { viewModel.setStrokeWidth(width) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                        drawLine(
                            color = if (isSelected) viewModel.currentColor else Color.White,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = width,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
