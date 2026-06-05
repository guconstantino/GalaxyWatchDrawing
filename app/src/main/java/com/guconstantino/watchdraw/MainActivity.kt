package com.guconstantino.watchdraw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.SwipeToDismissBox
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import com.guconstantino.watchdraw.data.AppScreen
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.presentation.ActionsScreen
import com.guconstantino.watchdraw.presentation.ClearConfirmScreen
import com.guconstantino.watchdraw.presentation.ColorPickerScreen
import com.guconstantino.watchdraw.presentation.DrawingCanvasScreen
import com.guconstantino.watchdraw.presentation.StrokePickerScreen
import com.guconstantino.watchdraw.presentation.theme.WatchDrawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchDrawTheme {
                val viewModel: DrawingViewModel = viewModel()

                // Swipe-to-dismiss navigates back to canvas from any sub-screen
                val swipeState = rememberSwipeToDismissBoxState()

                SwipeToDismissBox(
                    state = swipeState,
                    onDismissed = {
                        if (viewModel.currentScreen != AppScreen.Canvas) {
                            viewModel.currentScreen = AppScreen.Canvas
                        }
                    }
                ) { isBackground ->
                    if (isBackground) {
                        DrawingCanvasScreen(viewModel)
                    } else {
                        when (viewModel.currentScreen) {
                            AppScreen.Canvas       -> DrawingCanvasScreen(viewModel)
                            AppScreen.StrokePicker -> StrokePickerScreen(viewModel)
                            AppScreen.ColorPicker  -> ColorPickerScreen(viewModel)
                            AppScreen.Actions      -> ActionsScreen(viewModel)
                            AppScreen.ClearConfirm -> ClearConfirmScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
