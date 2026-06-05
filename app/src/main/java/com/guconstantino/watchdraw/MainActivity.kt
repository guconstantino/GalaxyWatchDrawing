package com.guconstantino.watchdraw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.presentation.WatchDrawApp
import com.guconstantino.watchdraw.presentation.theme.WatchDrawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchDrawTheme {
                val viewModel: DrawingViewModel = viewModel()
                WatchDrawApp(viewModel)
            }
        }
    }
}
