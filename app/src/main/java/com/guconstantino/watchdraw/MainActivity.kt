package com.guconstantino.watchdraw

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.guconstantino.watchdraw.data.AuthManager
import com.guconstantino.watchdraw.data.BillingManager
import com.guconstantino.watchdraw.data.DrawingViewModel
import com.guconstantino.watchdraw.data.UserProfile
import com.guconstantino.watchdraw.presentation.WatchDrawApp
import com.guconstantino.watchdraw.presentation.theme.WatchDrawTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var billing: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Play Billing: push the entitlement + price into the ViewModel for the UI.
        billing = BillingManager(
            context = this,
            onProChanged = { viewModel.updatePro(it) },
            onPriceChanged = { viewModel.updateProPrice(it) }
        )
        billing.connect()

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

        setContent {
            WatchDrawTheme {
                WatchDrawApp(
                    viewModel = viewModel,
                    onGoogleSignIn = { signInLauncher.launch(AuthManager.client(this).signInIntent) },
                    onBuyPro = { billing.launchPurchase(this) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check entitlement when returning to the app (e.g. after a purchase
        // completed elsewhere, or a refund).
        if (::billing.isInitialized) billing.refreshPurchases()
    }

    override fun onDestroy() {
        if (::billing.isInitialized) billing.release()
        super.onDestroy()
    }
}
