package com.guconstantino.watchdraw.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Thin wrapper around the Google Play Billing Library for a single **one-time,
 * non-consumable** product: the WatchDraw Pro unlock.
 *
 * Lifecycle (owned by the Activity, which has the lifecycle + an Activity for the
 * purchase UI):
 *   connect()  → on setup OK, query the product (price) and restore purchases.
 *   launchPurchase(activity) → opens Google's buy sheet.
 *   onPurchasesUpdated(...)  → result of a purchase; we must ACKNOWLEDGE it.
 *   release()  → end the connection.
 *
 * Entitlement source of truth = [refreshPurchases] (what Google says the user
 * owns), which also works offline via Play's local cache. Results are pushed out
 * via [onProChanged] / [onPriceChanged] (both fire on the main thread).
 */
class BillingManager(
    context: Context,
    private val onProChanged: (Boolean) -> Unit,
    private val onPriceChanged: (String?) -> Unit
) : PurchasesUpdatedListener, BillingClientStateListener {

    private var productDetails: ProductDetails? = null

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    /** Connect to Play. Safe to call repeatedly. */
    fun connect() {
        if (client.connectionState == BillingClient.ConnectionState.CONNECTED) return
        client.startConnection(this)
    }

    fun release() {
        client.endConnection()
    }

    /* ----- BillingClientStateListener ----- */

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            queryProduct()
            refreshPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Connection dropped; it will reconnect lazily on the next connect() call.
    }

    /* ----- Product details (price) ----- */

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = list.firstOrNull()
                onPriceChanged(productDetails?.oneTimePurchaseOfferDetails?.formattedPrice)
            }
        }
    }

    /* ----- Entitlement: what the user already owns (restore) ----- */

    fun refreshPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { _, purchases ->
            // Acknowledge anything not yet acknowledged (else Google refunds it).
            purchases.filter { isProPurchase(it) && !it.isAcknowledged }.forEach { acknowledge(it) }
            onProChanged(purchases.any { isProPurchase(it) })
        }
    }

    /* ----- Buy ----- */

    fun launchPurchase(activity: Activity) {
        val details = productDetails ?: run {
            // Not loaded yet — (re)connect; the user can tap again once it loads.
            connect()
            return
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        client.launchBillingFlow(activity, flowParams)
    }

    /* ----- PurchasesUpdatedListener (result of a buy) ----- */

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        }
        // USER_CANCELED or any error: nothing to grant.
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!isProPurchase(purchase)) return
        if (!purchase.isAcknowledged) acknowledge(purchase)
        onProChanged(true)
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { /* best-effort; refreshPurchases re-checks */ }
    }

    private fun isProPurchase(purchase: Purchase): Boolean =
        purchase.products.contains(PRODUCT_ID) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED

    companion object {
        /** Must match the managed product id created in the Play Console. */
        const val PRODUCT_ID = "watchdraw_pro"
    }
}
