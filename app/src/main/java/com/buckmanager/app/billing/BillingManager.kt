package com.buckmanager.app.billing

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Billing helper for one-time Lifetime Premium.
 * Product ID must exist in Play Console: [PRODUCT_PREMIUM_LIFETIME]
 */
class BillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onPremiumOwned: (owned: Boolean) -> Unit,
    private val onMessage: (String) -> Unit,
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_PREMIUM_LIFETIME = "premium_lifetime"
    }

    private val appContext = context.applicationContext

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun start() {
        if (billingClient.isReady) {
            queryProducts()
            refreshPurchases()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isReady.value = true
                    queryProducts()
                    refreshPurchases()
                } else {
                    _isReady.value = false
                    // Stay quiet on setup failure (common on sideloaded debug builds); surface errors on purchase attempt.
                }
            }

            override fun onBillingServiceDisconnected() {
                _isReady.value = false
            }
        })
    }

    fun end() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
        _isReady.value = false
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PREMIUM_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
            }
        }
    }

    fun refreshPurchases(notifyIfMissing: Boolean = false) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                if (notifyIfMissing) {
                    onMessage("Could not check purchases. Try again on a Play-installed build.")
                }
                return@queryPurchasesAsync
            }
            val owned = purchases.any { purchase ->
                purchase.products.contains(PRODUCT_PREMIUM_LIFETIME) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            purchases.filter {
                it.products.contains(PRODUCT_PREMIUM_LIFETIME) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    !it.isAcknowledged
            }.forEach { acknowledge(it) }
            onPremiumOwned(owned)
            if (notifyIfMissing) {
                onMessage(
                    if (owned) "Lifetime Premium restored. Tap the gold lock, then the pencil on any card."
                    else "No Lifetime Premium found on this Google account."
                )
            }
        }
    }

    fun launchPremiumPurchase(activity: Activity) {
        val details = _productDetails.value
        if (!_isReady.value) {
            onMessage("Play Billing not ready. Use a Play-installed build / check network.")
            start()
            return
        }
        if (details == null) {
            onMessage("Premium product not loaded. Create `$PRODUCT_PREMIUM_LIFETIME` (one-time) in Play Console.")
            queryProducts()
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            onMessage("Could not open Play purchase UI (${launchResult.debugMessage})")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { purchase ->
                    if (purchase.products.contains(PRODUCT_PREMIUM_LIFETIME) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        acknowledge(purchase) {
                            onPremiumOwned(true)
                            onMessage("Premium unlocked. Tap the gold lock, then the pencil on any card to customize.")
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onMessage("Purchase canceled.")
            }
            else -> onMessage("Purchase failed (${result.debugMessage})")
        }
    }

    private fun acknowledge(purchase: Purchase, onDone: (() -> Unit)? = null) {
        if (purchase.isAcknowledged) {
            onDone?.invoke()
            return
        }
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { ackResult ->
            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onDone?.invoke()
            } else {
                onMessage("Purchase needs acknowledge retry (${ackResult.debugMessage})")
            }
        }
    }

    fun formattedPrice(): String? {
        val offer = _productDetails.value?.oneTimePurchaseOfferDetails
        return offer?.formattedPrice
    }
}
