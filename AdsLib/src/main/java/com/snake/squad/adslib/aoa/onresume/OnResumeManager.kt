package com.snake.squad.adslib.aoa.onresume

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.facebook.FacebookUtils
import com.snake.squad.adslib.solar.SolarUtils
import com.snake.squad.adslib.tenjin.TenjinUtils
import com.snake.squad.adslib.tiktok.TiktokUtils
import com.snake.squad.adslib.utils.AdType
import com.snake.squad.adslib.utils.AdsConstants

internal class OnResumeManager(
    private val adsId: String,
    private val adRequest: AdRequest? = null,
    private val timeout: Long = 10000,
    application: Application
) : BaseOnResumeManager(application) {

    override val logTag = "OnResumeManager"

    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0

    init {
        validAndLoadAd()
    }

    override fun loadAd() {
        val id = if (AdmobLib.getDebugAds()) AdsConstants.APP_OPEN_TEST else adsId
        val appOpenAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(timeout.toInt()).build()
        isLoadingAd = true
        AppOpenAd.load(
            context,
            id,
            appOpenAdRequest,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(logTag, "Ad loaded successfully")
                    appOpenAd = ad
                    ad.setOnPaidEventListener { adValue ->
                        currentActivity?.let { activity ->
                            FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                            TenjinUtils.postRevenueTenjin(
                                activity,
                                adValue,
                                AdType.APP_OPEN,
                                id,
                                appOpenAd = ad
                            )
                        }

                        SolarUtils.postRevenueSolar(adValue, AdType.APP_OPEN, id, appOpenAd = ad)
                        TiktokUtils.postRevenueTiktok(adValue, AdType.APP_OPEN, id, appOpenAd = ad)
                    }
                    isLoadingAd = false
                    loadTime = System.currentTimeMillis()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(logTag, "Ad failed to load: ${error.code} ${error.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    override fun isAdAvailable(): Boolean {
        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < 4 * 3600000
    }

    override fun showAd(activity: Activity, onShowed: () -> Unit, onCloseOrFail: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) {
            onCloseOrFail()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(logTag, "Ad dismissed")

                onCloseOrFail()
                appOpenAd = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(logTag, "Ad failed to show: ${adError.code} ${adError.message}")

                onCloseOrFail()
                if (adError.code != 3) {
                    appOpenAd = null
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