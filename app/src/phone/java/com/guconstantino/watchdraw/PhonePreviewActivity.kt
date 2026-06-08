package com.guconstantino.watchdraw

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.guconstantino.watchdraw.data.AuthManager
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.UserProfile
import com.guconstantino.watchdraw.presentation.WatchDrawApp
import com.guconstantino.watchdraw.presentation.theme.WatchDrawTheme

/**
 * Dev-only harness (the `phone` flavor). Runs the **exact same** watch UI
 * ([WatchDrawApp]) inside a round, resizable frame on a dark-gray background,
 * simulating the Galaxy Watch screen — but on a phone, for fast iteration.
 *
 * A zoom control (− / +) changes the simulated screen size, so you can preview
 * the layout from small to large watches. Same applicationId as the watch build,
 * so Google Sign-In / Photos sync work here too. Never published.
 */
class PhonePreviewActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signInLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.onSignedIn(
                    UserProfile(
                        name = account.displayName ?: account.givenName ?: "",
                        email = account.email ?: "",
                        photoUrl = account.photoUrl?.toString()
                    )
                )
            } catch (e: ApiException) {
                // Sign-in cancelled or failed.
            }
        }

        setContent {
            WatchDrawTheme {
                // Persist the chosen size across rotation/process death.
                var watchSizeDp by rememberSaveable { mutableFloatStateOf(300f) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF202124))
                ) {
                    // The round "watch", centered in the space above the controls.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(watchSizeDp.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        ) {
                            WatchDrawApp(
                                viewModel = viewModel,
                                onGoogleSignIn = {
                                    signInLauncher.launch(
                                        AuthManager.client(this@PhonePreviewActivity).signInIntent
                                    )
                                }
                            )
                        }
                    }

                    // Simulated physical BACK button (dispatches a back press,
                    // which the screens' BackHandlers handle, like the watch).
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF3A3A3C))
                                .clickable { onBackPressedDispatcher.onBackPressed() }
                                .padding(horizontal = 22.dp, vertical = 10.dp)
                        ) {
                            BasicText(
                                text = "↩ Voltar",
                                style = TextStyle(color = Color.White, fontSize = 15.sp)
                            )
                        }
                    }

                    // Zoom control: shrink/grow the simulated watch screen.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZoomButton("−") {
                            watchSizeDp = (watchSizeDp - SIZE_STEP).coerceAtLeast(SIZE_MIN)
                        }
                        BasicText(
                            text = "${watchSizeDp.toInt()} dp",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = TextStyle(color = Color(0xFFBDBDBD), fontSize = 14.sp)
                        )
                        ZoomButton("+") {
                            watchSizeDp = (watchSizeDp + SIZE_STEP).coerceAtMost(SIZE_MAX)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SIZE_MIN = 150f
        private const val SIZE_MAX = 400f
        private const val SIZE_STEP = 20f
    }
}

@androidx.compose.runtime.Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF3A3A3C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = label,
            style = TextStyle(color = Color.White, fontSize = 22.sp, textAlign = TextAlign.Center)
        )
    }
}
