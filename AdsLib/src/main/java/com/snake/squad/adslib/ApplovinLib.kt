package com.snake.squad.adslib

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import com.applovin.sdk.AppLovinSdkUtils.runOnUiThread
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.models.MaxInterModel
import com.snake.squad.adslib.models.MaxNativeModel
import com.snake.squad.adslib.models.MaxRewardedModel
import com.snake.squad.adslib.utils.AdsHelper.isNetworkConnected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

object ApplovinLib {

    private var isDebug = false
    private var isShowAds = true
    private var isShowInterAds = false
    private var isShowRewardAds = false
    private var dialogFullScreen: Dialog? = null
    private var applovinSdk: AppLovinSdk? = null

    fun initialize(
        application: Application,
        sdkKey: String,
        isDebug: Boolean,
        isShowAds: Boolean,
        onInitializationComplete: ((AppLovinSdkConfiguration) -> Unit)? = null
    ) {
        this.isDebug = isDebug
        this.isShowAds = isShowAds
        CoroutineScope(Dispatchers.IO).launch {
            val initConfig = AppLovinSdkInitializationConfiguration.builder(sdkKey, application)
                .setMediationProvider(AppLovinMediationProvider.MAX)
            if (isDebug) {
                val currentID = AdvertisingIdClient.getAdvertisingIdInfo(application).id
                if (currentID != null) {
                    initConfig.testDeviceAdvertisingIds = Collections.singletonList(currentID)
                }
            }
            applovinSdk = AppLovinSdk.getInstance(application)
            applovinSdk?.initialize(initConfig.build()) { sdkConfig ->
                runOnUiThread {
                    onInitializationComplete?.invoke(sdkConfig)
                }
            }
        }
    }

    fun loadAndShowInterstitialSplash(
        activity: AppCompatActivity,
        maxInterModel: MaxInterModel,
        timeout: Long,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        val handle = Handler(Looper.getMainLooper())
        maxInterModel.interstitialAd.postValue(MaxInterstitialAd(maxInterModel.adsID, activity))
        maxInterModel.interstitialAd.value?.let {
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsLoaded?.invoke()
                    handle.postDelayed({
                        it.showAd(activity)
                    }, 800)
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    isShowInterAds = true
                    onAdsShowed?.invoke()
                    handle.postDelayed({
                        dismissDialogFullScreen()
                    }, 800)
                }

                override fun onAdHidden(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(true)
                    onAdsClose?.invoke()
                    handle.removeCallbacksAndMessages(0)
                }

                override fun onAdClicked(ad: MaxAd) {
                    onAdsClicked?.invoke()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    isShowInterAds = false
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                }

            })
            it.loadAd()
            activity.lifecycleScope.launch(Dispatchers.Main) {
                delay(timeout)
                if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                    isShowInterAds = false
                    dismissDialogFullScreen()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }
            }
        }
    }

    fun loadAndShowInterstitial(
        activity: AppCompatActivity,
        maxInterModel: MaxInterModel,
        timeout: Long = 15000,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        maxInterModel.interstitialAd.postValue(MaxInterstitialAd(maxInterModel.adsID, activity))
        maxInterModel.interstitialAd.value?.let {
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    isShowInterAds = false
                    it.showAd(activity)
                    onAdsLoaded?.invoke()
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    isShowInterAds = true
                    onAdsShowed?.invoke()
                    handle.postDelayed({
                        dismissDialogFullScreen()
                    }, 800)
                    handle.removeCallbacksAndMessages(0)
                }

                override fun onAdHidden(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(true)
                    onAdsClose?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onAdClicked(ad: MaxAd) {
                    onAdsClicked?.invoke()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

            })
            it.loadAd()
            activity.lifecycleScope.launch(Dispatchers.Main) {
                delay(timeout)
                if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                    isShowInterAds = false
                    dismissDialogFullScreen()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }
            }
        }
    }

    fun loadInterstitial(
        activity: AppCompatActivity,
        maxInterModel: MaxInterModel,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsFail?.invoke()
            return
        }
        if (maxInterModel.interstitialAd.value?.isReady == true) {
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsFail?.invoke()
            return
        }
        maxInterModel.interstitialAd.postValue(MaxInterstitialAd(maxInterModel.adsID, activity))
        maxInterModel.interstitialAd.value?.let {
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsLoaded?.invoke()
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    isShowInterAds = true
                }

                override fun onAdHidden(ad: MaxAd) {
                    isShowInterAds = false
                    maxInterModel.interstitialAd.postValue(null)
                }

                override fun onAdClicked(ad: MaxAd) {}

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    isShowInterAds = false
                    maxInterModel.interstitialAd.postValue(null)
                    onAdsFail?.invoke()
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    isShowInterAds = false
                    maxInterModel.interstitialAd.postValue(null)
                }

            })
            it.loadAd()
        }
    }

    fun showInterstitial(
        activity: AppCompatActivity,
        maxInterModel: MaxInterModel,
        timeout: Long = 10000,
        isPreload: Boolean = true,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (maxInterModel.interstitialAd.value == null || maxInterModel.interstitialAd.value?.isReady == false) {
            loadInterstitial(activity, maxInterModel)
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        maxInterModel.interstitialAd.value?.let {
            showDialogFullScreen(activity)
            val handle = Handler(Looper.getMainLooper())
            AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsLoaded?.invoke()
                }

                override fun onAdDisplayed(ad: MaxAd) {
                    isShowInterAds = true
                    onAdsShowed?.invoke()
                    handle.postDelayed({
                        dismissDialogFullScreen()
                    }, 800)
                }

                override fun onAdHidden(ad: MaxAd) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(true)
                    onAdsClose?.invoke()
                    maxInterModel.interstitialAd.postValue(null)
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    if (isPreload) {
                        loadInterstitial(activity, maxInterModel)
                    }
                }

                override fun onAdClicked(ad: MaxAd) {
                    onAdsClicked?.invoke()
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    maxInterModel.interstitialAd.postValue(null)
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    if (isPreload) {
                        loadInterstitial(activity, maxInterModel)
                    }
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    isShowInterAds = false
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    maxInterModel.interstitialAd.postValue(null)
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    if (isPreload) {
                        loadInterstitial(activity, maxInterModel)
                    }

                }
            })
            handle.postDelayed({
                it.showAd(activity)
            }, 800)
            activity.lifecycleScope.launch(Dispatchers.Main) {
                delay(timeout)
                if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                    isShowInterAds = false
                    dismissDialogFullScreen()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }
            }
        }
    }

    fun loadAndShowBanner(
        activity: Activity,
        bannerID: String,
        viewGroup: ViewGroup,
        viewLine: View,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsExpanded: (() -> Unit)? = null,
        onAdsCollapsed: (() -> Unit)? = null,
    ) {
        if (applovinSdk == null ||
            applovinSdk?.isInitialized == false ||
            !isShowAds ||
            !isNetworkConnected(activity)
        ) {
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            onAdsLoadFail?.invoke()
            return
        }

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val heightPx = AppLovinSdkUtils.dpToPx(activity, 50)

        val banner = MaxAdView(bannerID, activity)
        banner.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        banner.setExtraParameter("adaptive_banner", "true")

        val shimmerLoadingView: View =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, viewGroup, false)

        try {
            viewGroup.removeAllViews()
            viewGroup.addView(shimmerLoadingView, 0)
            viewGroup.addView(banner, 1)
        } catch (e: Exception) {
            e.message
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            onAdsLoadFail?.invoke()
            return
        }

        val shimmerFrameLayout: ShimmerFrameLayout =
            shimmerLoadingView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        banner.setRevenueListener { ad ->
        }

        banner.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                onAdsLoaded?.invoke()
            }

            override fun onAdDisplayed(p0: MaxAd) {
                onAdsShowed?.invoke()
            }

            override fun onAdHidden(p0: MaxAd) {
                onAdsClose?.invoke()
            }

            override fun onAdClicked(p0: MaxAd) {
                onAdsClicked?.invoke()
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                viewGroup.removeAllViews()
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                viewGroup.removeAllViews()
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdExpanded(p0: MaxAd) {
                onAdsExpanded?.invoke()
            }

            override fun onAdCollapsed(p0: MaxAd) {
                onAdsCollapsed?.invoke()
            }

        })
        banner.loadAd()
    }

    private fun createNativeAdView(context: Context, layout: Int): MaxNativeAdView {
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(layout)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_text_view)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setOptionsContentViewGroupId(R.id.options_view)
            .setStarRatingContentViewGroupId(R.id.star_rating_view)
            .setCallToActionButtonId(R.id.cta_button)
            .build()
        return MaxNativeAdView(binder, context)
    }

    fun loadAndShowNative(
        activity: Activity,
        nativeModel: MaxNativeModel,
        viewGroup: ViewGroup,
        isMedium: Boolean = true,
        layout: Int? = null,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsExpired: (() -> Unit)? = null
    ) {
        if (applovinSdk == null ||
            applovinSdk?.isInitialized == false ||
            !isShowAds ||
            !isNetworkConnected(activity)
        ) {
            viewGroup.visibility = View.GONE
            return
        }

        val shimmerLoadingView: View = if (isMedium) {
            activity.layoutInflater.inflate(R.layout.native_medium_shimmer_layout, viewGroup, false)
        } else {
            activity.layoutInflater.inflate(R.layout.native_small_shimmer_layout, viewGroup, false)
        }
        viewGroup.removeAllViews()
        viewGroup.addView(shimmerLoadingView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout =
            shimmerLoadingView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        nativeModel.nativeAdLoader = MaxNativeAdLoader(nativeModel.adsID, activity)
        nativeModel.nativeAdLoader?.setRevenueListener { ad ->
        }
        nativeModel.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, maxAd: MaxAd) {
                nativeModel.maxAd?.let { nativeModel.nativeAdLoader?.destroy(it) }
                nativeModel.maxAd = maxAd
                val layoutNative = layout
                    ?: if (isMedium) {
                        R.layout.max_ad_template_medium
                    } else {
                        R.layout.max_ad_template_small
                    }
                val adView = createNativeAdView(activity, layoutNative)
                nativeModel.nativeAdLoader?.render(adView, nativeModel.maxAd)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                viewGroup.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                onAdsClicked?.invoke()
            }

            override fun onNativeAdExpired(p0: MaxAd) {
                nativeModel.nativeAdLoader?.loadAd()
                onAdsExpired?.invoke()
            }
        })
        nativeModel.nativeAdLoader?.loadAd()
    }

    fun loadNative(
        activity: Activity,
        nativeModel: MaxNativeModel,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsExpired: (() -> Unit)? = null
    ) {
        if (applovinSdk == null ||
            applovinSdk?.isInitialized == false ||
            !isShowAds ||
            !isNetworkConnected(activity)
        ) {
            onAdsLoadFail?.invoke()
            return
        }
        nativeModel.nativeAdLoader = MaxNativeAdLoader(nativeModel.adsID, activity)
        nativeModel.nativeAdLoader?.setRevenueListener { ad ->
        }
        nativeModel.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                nativeModel.maxAd?.let { nativeModel.nativeAdLoader?.destroy(it) }
                nativeModel.maxAd = ad
                onAdsLoaded?.invoke()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                onAdsLoadFail?.invoke()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                onAdsClicked?.invoke()
            }

            override fun onNativeAdExpired(p0: MaxAd) {
                nativeModel.nativeAdLoader?.loadAd()
                onAdsExpired?.invoke()
            }
        })
        nativeModel.nativeAdLoader?.loadAd()
    }

    fun showNative(
        activity: Activity,
        nativeModel: MaxNativeModel,
        viewGroup: ViewGroup,
        isMedium: Boolean = true,
        layout: Int? = null,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null
    ) {
        if (applovinSdk == null ||
            applovinSdk?.isInitialized == false ||
            !isShowAds ||
            !isNetworkConnected(activity)
        ) {
            viewGroup.visibility = View.GONE
            return
        }

        val shimmerLoadingView: View = if (isMedium) {
            activity.layoutInflater.inflate(R.layout.native_medium_shimmer_layout, viewGroup, false)
        } else {
            activity.layoutInflater.inflate(R.layout.native_small_shimmer_layout, viewGroup, false)
        }
        viewGroup.removeAllViews()
        viewGroup.addView(shimmerLoadingView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout =
            shimmerLoadingView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        val layoutNative = layout
            ?: if (isMedium) {
                R.layout.max_ad_template_medium
            } else {
                R.layout.max_ad_template_small
            }
        val adView = createNativeAdView(activity, layoutNative)
        if (nativeModel.maxAd != null) {
            if (nativeModel.maxAd?.nativeAd != null) {
                nativeModel.nativeAdLoader?.render(adView, nativeModel.maxAd)
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                viewGroup.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
            } else {
                shimmerFrameLayout.stopShimmer()
                viewGroup.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }
        } else {
            shimmerFrameLayout.stopShimmer()
            viewGroup.visibility = View.GONE
            onAdsLoadFail?.invoke()
        }

    }

    fun loadAndShowRewarded(
        activity: AppCompatActivity,
        rewardedModel: MaxRewardedModel,
        timeout: Long = 15000L,
        onAdsCloseOrFailed: (Boolean) -> Unit,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onUserRewarded: (() -> Unit)? = null
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsCloseOrFailed.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed.invoke(false)
            onAdsFail?.invoke()
            return
        }
        var isEarned = false
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        rewardedModel.rewardAd = MaxRewardedAd.getInstance(rewardedModel.adsID, activity)
        rewardedModel.rewardAd?.let {
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(maxAd: MaxAd) {
                    onAdsLoaded?.invoke()
                    isShowRewardAds = false
                    it.showAd(activity)
                }

                override fun onAdDisplayed(maxAd: MaxAd) {
                    isShowRewardAds = true
                    onAdsShowed?.invoke()
                    handle.postDelayed({
                        dismissDialogFullScreen()
                    }, 800)
                    handle.removeCallbacksAndMessages(0)
                }

                override fun onAdHidden(maxAd: MaxAd) {
                    isShowRewardAds = false
                    onAdsCloseOrFailed.invoke(isEarned)
                    onAdsClose?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onAdClicked(maxAd: MaxAd) {
                    onAdsClicked?.invoke()
                }

                override fun onAdLoadFailed(maxAd: String, maxError: MaxError) {
                    isShowRewardAds = false
                    onAdsCloseOrFailed.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                    isShowRewardAds = false
                    onAdsCloseOrFailed.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onUserRewarded(maxAd: MaxAd, maxError: MaxReward) {
                    isEarned = true
                    onUserRewarded?.invoke()
                }
            })
            it.loadAd()
            activity.lifecycleScope.launch(Dispatchers.Main) {
                delay(timeout)
                if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                    isShowRewardAds = false
                    dismissDialogFullScreen()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }
            }
        }
    }

    fun loadRewarded(
        activity: AppCompatActivity,
        rewardedModel: MaxRewardedModel,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsFail?.invoke()
            return
        }
        if (rewardedModel.rewardAd?.isReady == true) {
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsFail?.invoke()
            return
        }
        rewardedModel.rewardAd = MaxRewardedAd.getInstance(rewardedModel.adsID, activity)
        rewardedModel.rewardAd?.let {
            it.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(maxAd: MaxAd) {
                    isShowRewardAds = false
                    onAdsLoaded?.invoke()
                }

                override fun onAdDisplayed(maxAd: MaxAd) {
                    isShowRewardAds = true
                }

                override fun onAdHidden(maxAd: MaxAd) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                }

                override fun onAdClicked(maxAd: MaxAd) {

                }

                override fun onAdLoadFailed(maxAd: String, maxError: MaxError) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                    onAdsFail?.invoke()
                }

                override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                    onAdsFail?.invoke()
                }

                override fun onUserRewarded(maxAd: MaxAd, maxError: MaxReward) {

                }

            })
            it.loadAd()
        }
    }

    fun showRewarded(
        activity: AppCompatActivity,
        rewardedModel: MaxRewardedModel,
        timeout: Long = 10000L,
        isPreload: Boolean = true,
        onAdsCloseOrFailed: (Boolean) -> Unit,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onUserRewarded: (() -> Unit)? = null
    ) {
        if (applovinSdk == null || applovinSdk?.isInitialized == false) {
            onAdsCloseOrFailed.invoke(false)
            onAdsFail?.invoke()
            return
        }
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed.invoke(false)
            onAdsFail?.invoke()
            return
        }
        val handle = Handler(Looper.getMainLooper())
        rewardedModel.rewardAd?.let {
            if (!it.isReady) {
                onAdsCloseOrFailed.invoke(false)
                onAdsFail?.invoke()
                return
            }
            var isEarned = false
            showDialogFullScreen(activity)
            AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
            it.setRevenueListener { ad ->
            }
            it.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(maxAd: MaxAd) {
                    isShowRewardAds = false
                    onAdsLoaded?.invoke()
                }

                override fun onAdDisplayed(maxAd: MaxAd) {
                    isShowRewardAds = true
                    onAdsShowed?.invoke()
                    handle.postDelayed({
                        dismissDialogFullScreen()
                    }, 800)
                    handle.removeCallbacksAndMessages(0)
                }

                override fun onAdHidden(maxAd: MaxAd) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                    onAdsCloseOrFailed.invoke(isEarned)
                    onAdsClose?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    if (isPreload) {
                        loadRewarded(activity, rewardedModel)
                    }
                }

                override fun onAdClicked(maxAd: MaxAd) {
                    onAdsClicked?.invoke()
                }

                override fun onAdLoadFailed(maxAd: String, maxError: MaxError) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                    onAdsCloseOrFailed.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                    isShowRewardAds = false
                    rewardedModel.rewardAd = null
                    onAdsCloseOrFailed.invoke(false)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                }

                override fun onUserRewarded(maxAd: MaxAd, maxError: MaxReward) {
                    isEarned = true
                    onUserRewarded?.invoke()
                }
            })
            handle.postDelayed({
                it.showAd(activity)
            }, 800)
        }
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                isShowRewardAds = false
                dismissDialogFullScreen()
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    private fun showDialogFullScreen(activity: Activity) {
        try {
            dialogFullScreen = Dialog(activity)
            dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogFullScreen?.setContentView(R.layout.dialog_loading_ads_full_screen)
            dialogFullScreen?.setCancelable(false)
            dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            dialogFullScreen?.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun dismissDialogFullScreen() {
        try {
            dialogFullScreen?.dismiss()
        } catch (e: Exception) {
            e.message
        }
    }

    fun getDebugAds(): Boolean {
        return isDebug
    }

    fun setDebugAds(isDebug: Boolean) {
        ApplovinLib.isDebug = isDebug
    }

    fun getShowAds(): Boolean {
        return isShowAds
    }

    fun setShowAds(isShowAds: Boolean) {
        ApplovinLib.isShowAds = isShowAds
    }

    fun getShowInterAds(): Boolean {
        return isShowInterAds
    }

    fun setShowInterAds(isShowInterAds: Boolean) {
        ApplovinLib.isShowInterAds = isShowInterAds
    }

    fun getShowRewardAds(): Boolean {
        return isShowRewardAds
    }

    fun setShowRewardAds(isShowRewardAds: Boolean) {
        ApplovinLib.isShowRewardAds = isShowRewardAds
    }

}