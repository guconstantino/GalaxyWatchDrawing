package com.guconstantino.watchdraw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.guconstantino.watchdraw.data.AuthManager
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.UserProfile
import com.guconstantino.watchdraw.presentation.WatchDrawApp
import com.guconstantino.watchdraw.presentation.theme.WatchDrawTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var consentLauncher: ActivityResultLauncher<Intent>

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
                // Sign-in cancelled or failed — stay on the login screen.
            }
        }

        // Result of the Google Photos consent screen, launched lazily when a
        // sync needs the scope the Wear native sign-in didn't grant.
        consentLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            viewModel.onConsentResult(result.resultCode == Activity.RESULT_OK)
        }

        setContent {
            WatchDrawTheme {
                // When a sync surfaces a consent screen, launch it once.
                val consentIntent = viewModel.pendingConsentIntent
                LaunchedEffect(consentIntent) {
                    if (consentIntent != null) {
                        viewModel.onConsentLaunched()
                        consentLauncher.launch(consentIntent)
                    }
                }

                WatchDrawApp(
                    viewModel = viewModel,
                    onGoogleSignIn = { signInLauncher.launch(AuthManager.client(this).signInIntent) }
                )
            }
        }
    }
}
