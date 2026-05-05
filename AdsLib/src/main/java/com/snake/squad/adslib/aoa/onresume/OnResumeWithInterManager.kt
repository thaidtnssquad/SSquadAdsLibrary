package com.snake.squad.adslib.aoa.onresume

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.facebook.FacebookUtils
import com.snake.squad.adslib.solar.SolarUtils
import com.snake.squad.adslib.tenjin.TenjinUtils
import com.snake.squad.adslib.tiktok.TiktokUtils
import com.snake.squad.adslib.utils.AdType
import com.snake.squad.adslib.utils.AdsConstants

internal class OnResumeWithInterManager(
    private val adsId: String,
    private val adRequest: AdRequest? = null,
    private val timeout: Long = 10000,
    application: Application
) : BaseOnResumeManager(application) {

    override val logTag = "OnResumeWithInterManager"

    private var interstitialAd: InterstitialAd? = null

    init {
        validAndLoadAd()
    }

    override fun loadAd() {
        val id = if (AdmobLib.getDebugAds()) AdsConstants.admobInterModelTest.adsID else adsId
        val interAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(timeout.toInt()).build()
        isLoadingAd = true
        InterstitialAd.load(
            context,
            id,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(logTag, "Ad loaded successfully")
                    interstitialAd = ad
                    ad.setOnPaidEventListener { adValue ->
                        currentActivity?.let { activity ->
                            FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                            TenjinUtils.postRevenueTenjin(
                                activity,
                                adValue,
                                AdType.INTERSTITIAL,
                                id,
                                interAd = ad
                            )
                        }

                        SolarUtils.postRevenueSolar(adValue, AdType.INTERSTITIAL, id, interAd = ad)
                        TiktokUtils.postRevenueTiktok(adValue, AdType.INTERSTITIAL, id, interAd = ad)
                    }
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(logTag, "Ad failed to load: ${error.code} ${error.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    override fun isAdAvailable(): Boolean {
        return interstitialAd != null
    }

    override fun showAd(activity: Activity, onShowed: () -> Unit, onCloseOrFail: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            onCloseOrFail()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(logTag, "Ad dismissed")

                onCloseOrFail()
                interstitialAd = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(logTag, "Ad failed to show: ${adError.code} ${adError.message}")

                onCloseOrFail()
                if (adError.code != 3) {
                    interstitialAd = null
                    loadAd()
                }
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(logTag, "Ad shown")

                onShowed()
            }
        }
        ad.show(activity)
    }
}