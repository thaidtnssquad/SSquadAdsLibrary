package com.snake.squad.adslib.cmp

import android.app.Activity
import java.util.concurrent.atomic.AtomicBoolean

object CMPUtils {

    fun setupCMP(activity: Activity, onConsentGranted: () -> Unit) {
        val isMobileAdsInitializeCalled = AtomicBoolean(false)
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(activity)
        googleMobileAdsConsentManager.gatherConsent { error ->
            error?.let {
                initializeMobileAdsSdk(isMobileAdsInitializeCalled, onConsentGranted)
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk(isMobileAdsInitializeCalled, onConsentGranted)
            }
        }
    }

    private fun initializeMobileAdsSdk(isMobileAdsInitializeCalled: AtomicBoolean, onConsentGranted: () -> Unit) {
        if (isMobileAdsInitializeCalled.get()) {
            return
        }
        isMobileAdsInitializeCalled.set(true)
        onConsentGranted.invoke()
    }

}