package com.snake.squad.adslib.billing

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.snake.squad.adslib.utils.SharedPrefManager
import com.unity3d.services.store.gpbl.BillingResultResponseCode
import java.text.NumberFormat
import java.util.Currency
import java.util.concurrent.atomic.AtomicBoolean

class PurchaseHelper {
    companion object {
        private const val IS_PREMIUM = "app.iap.premium.is"
        var isPremium: Boolean
            get() = SharedPrefManager.getBoolean(IS_PREMIUM, false)
            private set(value) = SharedPrefManager.putBoolean(IS_PREMIUM, value)

        val isEnabledAds: Boolean
            get() = !isPremium
    }

    private var billingClient: BillingClient? = null
    private var productDetailsInApp: ProductDetails? = null
    private var priceCurrencyCode = "USD"
    private var price = 0.0
    private val pendingPurchasesParams by lazy {
        PendingPurchasesParams.newBuilder().enableOneTimeProducts()
    }

    fun initInAppPurchase(
        context: Context,
        listener: (result: BillingResult, purchases: List<Purchase>) -> Unit
    ) {
        val client = BillingClient.newBuilder(context)
            .setListener { _, _ -> }
            .enablePendingPurchases(pendingPurchasesParams.build())
            .build()
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode != BillingResultResponseCode.OK.responseCode) {
                    Log.e(
                        "PurchaseHelperTAG",
                        "onBillingSetupFinished: ${billingResult.responseCode}",
                    )
                }

                queryAndSetStateBilling(BillingClient.ProductType.INAPP, onEmpty = {
                    queryAndSetStateBilling(BillingClient.ProductType.SUBS, listener = listener)
                }, listener)
            }
        })
    }

    fun restorePurchase(listener: (result: BillingResult, purchases: List<Purchase>) -> Unit): Boolean {
        val client = billingClient ?: return false

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                queryAndSetStateBilling(BillingClient.ProductType.INAPP, onEmpty = {
                    queryAndSetStateBilling(BillingClient.ProductType.SUBS, listener = listener)
                }, listener)
            }
        })
        return true
    }

    fun getProductDetailsInApp(mActivity: AppCompatActivity, keys: List<String>): ProductDetails? {
        if (productDetailsInApp == null) {
            queryDetailPurchase(
                mActivity, keys, onPurchaseUpdated = {}, onQueryFailed = {},
                onQuerySuccess = { billingResult, productDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        productDetailsInApp = productDetailsList?.firstOrNull()
                    }
                },
                isInApp = true
            )
        }

        return productDetailsInApp
    }

    fun queryDetailPurchase(
        mActivity: AppCompatActivity,
        purchaseKeys: List<String>,
        onPurchaseUpdated: () -> Unit,
        onQueryFailed: () -> Unit,
        onQuerySuccess: (billingResult: BillingResult, productDetailsList: List<ProductDetails>?) -> Unit,
        isInApp: Boolean
    ) {
        val isQueryFailedCalled = AtomicBoolean(false)

        val client = BillingClient.newBuilder(mActivity)
            .enablePendingPurchases(pendingPurchasesParams.build())
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                    isPremium = true
                    mActivity.runOnUiThread(onPurchaseUpdated)
                } else if (!isQueryFailedCalled.get()) {
                    isQueryFailedCalled.set(true)
                    onQueryFailed()
                }
            }.build()

        // CHECKME: 4/18/2026 need disconnect old client?, really need new client?
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                if (!isQueryFailedCalled.get()) {
                    isQueryFailedCalled.set(true)
                    onQueryFailed()
                }
            }

            override fun onBillingSetupFinished(br: BillingResult) {
                if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                    val type =
                        if (isInApp) BillingClient.ProductType.INAPP else BillingClient.ProductType.SUBS
                    val products = purchaseKeys.map { key ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(key)
                            .setProductType(type)
                            .build()
                    }
                    val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(products)
                        .build()

                    billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, queryResult ->
                        onQuerySuccess(billingResult, queryResult.productDetailsList)
                    }
                } else if (!isQueryFailedCalled.get()) {
                    isQueryFailedCalled.set(true)
                    onQueryFailed()
                }
            }
        })
    }

    fun subscribePurchase(
        mActivity: AppCompatActivity,
        productDetails: ProductDetails,
        isInApp: Boolean
    ): Boolean {
        return try {
            val productDetailsParams = if (isInApp) {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            } else {
                productDetails.subscriptionOfferDetails?.get(0)?.offerToken?.let { token ->
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(token)
                        .build()
                }
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            billingClient?.launchBillingFlow(mActivity, billingFlowParams)

            val fPricingPase = productDetails.subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()

            priceCurrencyCode = fPricingPase?.priceCurrencyCode.toString()
            price = fPricingPase?.priceAmountMicros.let(::getLongPrice)
            true
        } catch (e: Exception) {
            Log.e("PurchaseHelperTAG", "subscribePurchase: ", e)
            false
        }
    }

    fun stopConnect() {
        val client = billingClient ?: return
        billingClient = null
        client.endConnection()
    }

    fun openSubscriptionManage(mContext: Context): Boolean {
        return try {
            val uri =
                "https://play.google.com/store/account/subscriptions?&package=${mContext.packageName}".toUri()
            mContext.startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun formatPriceIap(purchasePrice: String): String {
        return try {
            val subPrice = purchasePrice.trimSubstring(purchasePrice.length - 1, purchasePrice.length)
            val price = purchasePrice.substring(0, purchasePrice.length - 1)
            subPrice + "" + price
        } catch (_: Exception) {
            val subPrice = purchasePrice.trimSubstring(0, 1);
            val price = purchasePrice.substring(1, purchasePrice.length);
            subPrice + "" + price
        }
    }

    /**
     * @param ratio Example: year -> week = 365 / 7.0, month -> week = 30 / 7.0
     */
    fun formatPriceIap(
        pricingPhase: ProductDetails.PricingPhase?,
        ratio: Double,
        default: String = "-"
    ): String {
        if (pricingPhase == null) return default

        return try {
            val per = pricingPhase.priceAmountMicros / ratio / 1_000_000

            NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(pricingPhase.priceCurrencyCode)
            }.format(per)
        } catch (e: Exception) {
            Log.e("PurchaseHelperTAG", "formatPriceIap: ", e)
            default
        }
    }

    private fun queryAndSetStateBilling(
        @BillingClient.ProductType type: String,
        onEmpty: (() -> Unit)? = null,
        listener: (result: BillingResult, purchases: List<Purchase>) -> Unit,
    ) {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(type)
            .build()

        billingClient?.queryPurchasesAsync(queryPurchasesParams) { result, purchases ->
            if (purchases.isNotEmpty()) {
                setStateBilling(true, result, purchases, listener)
            } else if (onEmpty == null) {
                setStateBilling(false, result, purchases, listener)
            } else {
                onEmpty()
            }
        }
    }

    private fun setStateBilling(
        isPremium: Boolean = false,
        result: BillingResult,
        purchases: List<Purchase>,
        listener: (result: BillingResult, purchases: List<Purchase>) -> Unit
    ) {
        PurchaseHelper.isPremium = isPremium
        if (isPremium) purchases.forEach(::handlePurchase)
        listener(result, purchases)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
//                    logEventPurchase(
//                        price,
//                        purchase.products[0].toString(),
//                        purchase.purchaseToken,
//                        priceCurrencyCode,
//                        purchase.orderId.toString()
//                    )
            }
        }
    }

//    private fun logEventPurchase(money: Double, id: String, token: String, current: String, orderId: String) {
//        val adjustEvent = AdjustEvent("yhdz41")
//        adjustEvent.setRevenue(money, current)
//        adjustEvent.setOrderId(orderId)
//        adjustEvent.setProductId(id)
//        adjustEvent.setPurchaseToken(token)
//        Adjust.trackEvent(adjustEvent)
//    }

    private fun getLongPrice(skuDetails: Long?): Double {
        val priceMicro = skuDetails?.div((1_000_000f))
        return priceMicro!!.toDouble()
    }
}