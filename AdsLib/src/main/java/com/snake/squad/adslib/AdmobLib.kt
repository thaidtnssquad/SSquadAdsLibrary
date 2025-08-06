package com.snake.squad.adslib

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.applovin.sdk.AppLovinSdkUtils.runOnUiThread
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.dialogs.NativeAfterInterDialog
import com.snake.squad.adslib.facebook.FacebookUtils
import com.snake.squad.adslib.models.AdmobBannerCollapsibleModel
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.models.AdmobRewardedModel
import com.snake.squad.adslib.solar.SolarUtils
import com.snake.squad.adslib.utils.AdType
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslib.utils.AdsHelper.isNetworkConnected
import com.snake.squad.adslib.utils.BannerCollapsibleType
import com.snake.squad.adslib.utils.BannerType
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslib.utils.NativeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdmobLib {

    private var isInitAds: Boolean = false
    private var isDebug = false
    private var isShowAds = true
    private var isShowInterAds = false
    private var isShowRewardAds = false
    private var adRequest: AdRequest? = null
    private var dialogFullScreen: Dialog? = null
    private var isTestDevice = false
    private var isEnabledCheckTestDevice = false

    fun initialize(
        context: Context,
        timeout: Int = 10000,
        isDebug: Boolean = true,
        isShowAds: Boolean = true,
        onInitializedAds: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                this@AdmobLib.isDebug = isDebug
                this@AdmobLib.isShowAds = isShowAds
                MobileAds.initialize(context) {
                    Log.d("TAG=====", "initAds")
                    val requestConfiguration = RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf())
                        .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)
                    initAdRequest(timeout)
                    runOnUiThread {
                        this@AdmobLib.isInitAds = true
                        onInitializedAds.invoke(true)
                    }
                }
            } catch (e: Exception) {
                e.message
                this@AdmobLib.isInitAds = false
                this@AdmobLib.isShowAds = false
                runOnUiThread {
                    onInitializedAds.invoke(false)
                }
            }
        }
    }

    private fun initAdRequest(timeOut: Int) {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    fun loadAndShowInterstitialSplash(
        activity: AppCompatActivity,
        admobInterModel: AdmobInterModel,
        timeout: Long = 15000,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsImpression: (() -> Unit)? = null
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        val handle = Handler(Looper.getMainLooper())
        val interAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(timeout.toInt()).build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    admobInterModel.interstitialAd.value = null
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    onAdsLoaded?.invoke()
                    showDialogFullScreen(activity)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
                    handle.postDelayed({
                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    isShowInterAds = false
                                    onAdsCloseOrFailed?.invoke(true)
                                    onAdsClose?.invoke()
                                    admobInterModel.interstitialAd.value = null
                                    handle.removeCallbacksAndMessages(0)
                                    AppOnResumeAdsManager.getInstance()
                                        .setAppResumeEnabled(true)
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    isShowInterAds = false
                                    dismissDialogFullScreen()
                                    onAdsCloseOrFailed?.invoke(false)
                                    onAdsFail?.invoke()
                                    admobInterModel.interstitialAd.value = null
                                    handle.removeCallbacksAndMessages(0)
                                    AppOnResumeAdsManager.getInstance()
                                        .setAppResumeEnabled(true)
                                }

                                override fun onAdClicked() {
                                    onAdsClicked?.invoke()
                                }

                                override fun onAdImpression() {
                                    onAdsImpression?.invoke()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    isShowInterAds = true
                                    handle.postDelayed({
                                        dismissDialogFullScreen()
                                    }, 800)
                                    onAdsShowed?.invoke()
                                }
                            }
                        interstitialAd.setOnPaidEventListener {
                            AdjustUtils.postRevenueAdjustInter(
                                interstitialAd,
                                it,
                                admobInterModel.adsID
                            )
                            FacebookUtils.adImpressionFacebookRevenue(activity, it)
                            SolarUtils.postRevenueSolar(
                                it,
                                AdType.INTERSTITIAL,
                                admobInterModel.adsID,
                                interAd = interstitialAd
                            )
                        }
                        interstitialAd.show(activity)
                    }, 800)
                }
            })
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                isShowInterAds = false
                dismissDialogFullScreen()
                admobInterModel.interstitialAd.value = null
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadAndShowInterstitial(
        activity: AppCompatActivity,
        admobInterModel: AdmobInterModel,
        timeout: Long = 15000,
        isShowOnTestDevice: Boolean = false,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsImpression: (() -> Unit)? = null
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        val interAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(timeout.toInt()).build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    admobInterModel.interstitialAd.value = null
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    onAdsLoaded?.invoke()
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isShowInterAds = false
                                onAdsCloseOrFailed?.invoke(true)
                                onAdsClose?.invoke()
                                admobInterModel.interstitialAd.value = null
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isShowInterAds = false
                                dismissDialogFullScreen()
                                onAdsCloseOrFailed?.invoke(false)
                                onAdsFail?.invoke()
                                admobInterModel.interstitialAd.value = null
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                            }

                            override fun onAdClicked() {
                                onAdsClicked?.invoke()
                            }

                            override fun onAdImpression() {
                                onAdsImpression?.invoke()
                            }

                            override fun onAdShowedFullScreenContent() {
                                isShowInterAds = true
                                handle.postDelayed({
                                    dismissDialogFullScreen()
                                }, 800)
                                onAdsShowed?.invoke()
                            }
                        }
                    interstitialAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustInter(
                            interstitialAd,
                            it,
                            admobInterModel.adsID
                        )
                        FacebookUtils.adImpressionFacebookRevenue(activity, it)
                        SolarUtils.postRevenueSolar(
                            it,
                            AdType.INTERSTITIAL,
                            admobInterModel.adsID,
                            interAd = interstitialAd
                        )
                    }
                    interstitialAd.show(activity)
                }
            })
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                isShowInterAds = false
                dismissDialogFullScreen()
                admobInterModel.interstitialAd.value = null
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadInterstitial(
        activity: Activity,
        admobInterModel: AdmobInterModel,
        isShowOnTestDevice: Boolean = false,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null
    ) {
        if (!isShowAds
            || isShowInterAds
            || !isNetworkConnected(activity)
            || admobInterModel.interstitialAd.value != null
            || admobInterModel.isLoading.value == true
            || (!isShowOnTestDevice && isTestDevice)
        ) {
            onAdsFail?.invoke()
            return
        }
        admobInterModel.isLoading.postValue(true)
        val interAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isShowInterAds = false
                    admobInterModel.interstitialAd.value = null
                    admobInterModel.isLoading.postValue(false)
                    onAdsFail?.invoke()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isShowInterAds = false
                    admobInterModel.interstitialAd.value = interstitialAd
                    admobInterModel.isLoading.postValue(false)
                    onAdsLoaded?.invoke()
                    interstitialAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustInter(
                            interstitialAd,
                            it,
                            admobInterModel.adsID
                        )
                        FacebookUtils.adImpressionFacebookRevenue(activity, it)
                        SolarUtils.postRevenueSolar(
                            it,
                            AdType.INTERSTITIAL,
                            admobInterModel.adsID,
                            interAd = interstitialAd
                        )
                    }
                }
            })
    }

    fun showInterstitial(
        activity: AppCompatActivity,
        admobInterModel: AdmobInterModel,
        isPreload: Boolean = true,
        isShowOnTestDevice: Boolean = false,
        onAdsCloseOrFailed: ((Boolean) -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsImpression: (() -> Unit)? = null
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            if (admobInterModel.interstitialAd.value == null) loadInterstitial(
                activity,
                admobInterModel
            )
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        admobInterModel.isLoading.observe(activity as LifecycleOwner) { isLoading ->
            if (isLoading) {
                showDialogFullScreen(activity)
            } else {
                if (admobInterModel.interstitialAd.value != null) {
                    val handle = Handler(Looper.getMainLooper())
                    admobInterModel.interstitialAd.value?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isShowInterAds = false
                                onAdsCloseOrFailed?.invoke(true)
                                onAdsClose?.invoke()
                                admobInterModel.interstitialAd.value = null
                                admobInterModel.isLoading.removeObservers(activity as LifecycleOwner)
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                                if (isPreload) {
                                    loadInterstitial(activity, admobInterModel, isShowOnTestDevice)
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isShowInterAds = false
                                dismissDialogFullScreen()
                                onAdsCloseOrFailed?.invoke(false)
                                onAdsFail?.invoke()
                                admobInterModel.interstitialAd.value = null
                                admobInterModel.isLoading.removeObservers(activity as LifecycleOwner)
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                                if (isPreload) {
                                    loadInterstitial(activity, admobInterModel, isShowOnTestDevice)
                                }
                            }

                            override fun onAdClicked() {
                                onAdsClicked?.invoke()
                            }

                            override fun onAdImpression() {
                                onAdsImpression?.invoke()
                            }

                            override fun onAdShowedFullScreenContent() {
                                isShowInterAds = true
                                handle.postDelayed({
                                    dismissDialogFullScreen()
                                }, 800)
                                onAdsShowed?.invoke()
                            }
                        }
                    admobInterModel.interstitialAd.value?.show(activity)
                } else {
                    isShowInterAds = false
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed?.invoke(false)
                    onAdsFail?.invoke()
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    admobInterModel.isLoading.removeObservers(activity as LifecycleOwner)
                    if (isPreload) {
                        loadInterstitial(activity, admobInterModel, isShowOnTestDevice)
                    }
                }
            }
        }
    }

    fun loadAndShowBanner(
        activity: Activity,
        bannerID: String,
        viewGroup: ViewGroup,
        viewLine: View,
        bannerType: BannerType? = null,
        isShowOnTestDevice: Boolean = false,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            onAdsLoadFail?.invoke()
            return
        }
        val adView = AdView(activity)
        adView.adUnitId = if (isDebug) AdsConstants.ADMOB_BANNER_TEST else bannerID
        adView.setAdSize(
            when (bannerType) {
                BannerType.BANNER -> AdSize.BANNER
                BannerType.LARGE_BANNER -> AdSize.LARGE_BANNER
                BannerType.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
                BannerType.FULL_BANNER -> AdSize.FULL_BANNER
                BannerType.LEADERBOARD -> AdSize.LEADERBOARD
                else -> getAdSize(activity)
            }
        )
        val shimmerLoadingView =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, viewGroup, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(shimmerLoadingView, 0)
            viewGroup.addView(adView, 1)
        } catch (e: Exception) {
            e.message
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            return
        }
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        adView.setOnPaidEventListener {
            AdjustUtils.postRevenueAdjust(it, adView.adUnitId)
            FacebookUtils.adImpressionFacebookRevenue(activity, it)
            SolarUtils.postRevenueSolar(it, AdType.BANNER, bannerID, adView = adView)
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                onAdsLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}
        }
        (adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()).let {
            adView.loadAd(it)
        }
    }

    fun loadAndShowBannerCollapsible(
        activity: Activity,
        admobBannerCollapsibleModel: AdmobBannerCollapsibleModel,
        viewGroup: ViewGroup,
        viewLine: View,
        collapsibleType: BannerCollapsibleType? = null,
        isShowOnTestDevice: Boolean = false,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsOpened: (() -> Unit?)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsClosed: (() -> Unit)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            onAdsLoadFail?.invoke()
            return
        }
        admobBannerCollapsibleModel.adView?.destroy()
        admobBannerCollapsibleModel.adView?.let {
            viewGroup.removeView(it)
        }
        admobBannerCollapsibleModel.adView = AdView(activity)
        admobBannerCollapsibleModel.adView?.adUnitId =
            if (isDebug) AdsConstants.admobBannerCollapsibleModel.adsID else admobBannerCollapsibleModel.adsID
        val adSize = getAdSize(activity)
        admobBannerCollapsibleModel.adView?.setAdSize(adSize)
        val shimmerLoadingView =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, viewGroup, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(shimmerLoadingView, 0)
            viewGroup.addView(admobBannerCollapsibleModel.adView, 1)
        } catch (e: Exception) {
            e.message
        }
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        admobBannerCollapsibleModel.adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                try {
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        admobBannerCollapsibleModel.adView?.setOnPaidEventListener {
                            AdjustUtils.postRevenueAdjust(
                                it,
                                admobBannerCollapsibleModel.adView?.adUnitId
                            )
                            FacebookUtils.adImpressionFacebookRevenue(activity, it)
                            SolarUtils.postRevenueSolar(
                                it,
                                AdType.BANNER,
                                admobBannerCollapsibleModel.adsID,
                                adView = admobBannerCollapsibleModel.adView
                            )
                        }
                        shimmerFrameLayout.stopShimmer()
                        viewGroup.removeView(shimmerLoadingView)
                        val params: ViewGroup.LayoutParams = viewGroup.layoutParams
                        params.height = adSize.getHeightInPixels(activity)
                        viewGroup.layoutParams = params
                        onAdsLoaded?.invoke()
                    }
                } catch (e: Exception) {
                    e.message
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdOpened() {
                onAdsOpened?.invoke()
            }

            override fun onAdClicked() {
                onAdsClicked?.invoke()
            }

            override fun onAdClosed() {
                onAdsClosed?.invoke()
            }
        }
        val extras = Bundle()
        extras.putString(
            "collapsible",
            collapsibleType?.value ?: BannerCollapsibleType.BOTTOM.value
        )
        val adRequestCollapsible =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        admobBannerCollapsibleModel.adView?.loadAd(adRequestCollapsible)
    }

    fun loadAndShowNative(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        viewGroup: ViewGroup,
        size: GoogleENative = GoogleENative.UNIFIED_MEDIUM,
        layout: Int? = null,
        shimmerLayout: Int? = null,
        mediaViewRatio: Int = MediaAspectRatio.SQUARE,
        isShowOnTestDevice: Boolean = false,
        isCheckTestAds: Boolean = false,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsLoadFail?.invoke()
            viewGroup.visibility = View.GONE
            return
        }
        val shimmerInflate = shimmerLayout ?: when (size) {
            GoogleENative.UNIFIED_MEDIUM -> R.layout.native_medium_shimmer_layout
            GoogleENative.UNIFIED_SMALL -> R.layout.native_small_shimmer_layout
            GoogleENative.UNIFIED_SMALL_LIKE_BANNER -> R.layout.native_small_like_banner_shimmer_layout
            GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON -> R.layout.native_medium_like_button_shimmer_layout
            GoogleENative.UNIFIED_FULL_SCREEN -> R.layout.native_full_screen_shimmer
        }

        val shimmerLoadingView = activity.layoutInflater.inflate(
            shimmerInflate,
            null,
            false
        )

        viewGroup.removeAllViews()
        viewGroup.addView(shimmerLoadingView, 0)
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        val nativeAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()
        val adLoader = AdLoader.Builder(
            activity,
            if (isDebug) AdsConstants.admobNativeModelTest.adsID else admobNativeModel.adsID
        )
        if (size == GoogleENative.UNIFIED_FULL_SCREEN) {
            val videoOptions =
                VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(false)
                    .build()
            val adOptions = NativeAdOptions.Builder()
                .setMediaAspectRatio(mediaViewRatio)
                .setVideoOptions(videoOptions)
                .build()
            adLoader.withNativeAdOptions(adOptions)
        } else {
            adLoader.withNativeAdOptions(NativeAdOptions.Builder().build())
        }
        adLoader.forNativeAd { nativeAd ->
            if (isCheckTestAds) checkTestDevice(isEnabledCheckTestDevice, nativeAd)
            val layoutNative = layout
                ?: when (size) {
                    GoogleENative.UNIFIED_MEDIUM -> R.layout.admob_ad_template_medium
                    GoogleENative.UNIFIED_SMALL -> R.layout.admob_ad_template_small
                    GoogleENative.UNIFIED_SMALL_LIKE_BANNER -> R.layout.admob_ad_template_small_like_banner
                    GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON -> R.layout.admob_ad_template_medium_like_button
                    GoogleENative.UNIFIED_FULL_SCREEN -> R.layout.admob_ad_template_full_screen
                }
            val adView = activity.layoutInflater.inflate(layoutNative, null) as NativeAdView
            NativeUtils.populateNativeAdView(
                nativeAd,
                adView,
                size
            )
            shimmerFrameLayout.stopShimmer()
            viewGroup.removeAllViews()
            viewGroup.addView(adView)
            nativeAd.setOnPaidEventListener { adValue: AdValue ->
                AdjustUtils.postRevenueAdjustNative(nativeAd, adValue, admobNativeModel.adsID)
                FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                SolarUtils.postRevenueSolar(
                    adValue,
                    AdType.NATIVE,
                    admobNativeModel.adsID,
                    nativeAd = nativeAd
                )
            }
        }
        adLoader.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                viewGroup.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
            }
        })
        adLoader.build().loadAd(nativeAdRequest)
    }

    fun loadAndShowNativeCollapsible(
        activity: Activity,
        admobNativeModelExpanded: AdmobNativeModel,
        admobNativeModelCollapsed: AdmobNativeModel? = null,
        viewGroupExpanded: ViewGroup,
        viewGroupCollapsed: ViewGroup,
        layoutExpanded: Int? = null,
        layoutCollapsed: Int? = null,
        shimmerLayout: Int? = null,
        isShowOnTestDevice: Boolean = false,
        isCheckTestAds: Boolean = false,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsClosed: (() -> Unit?)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsLoadFail?.invoke()
            viewGroupExpanded.visibility = View.GONE
            viewGroupCollapsed.visibility = View.GONE
            return
        }

        val shimmerInflate = shimmerLayout ?: R.layout.native_small_like_banner_shimmer_layout
        val shimmerLoadingView = activity.layoutInflater.inflate(
            shimmerInflate,
            null,
            false
        )

        val nativeAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()

        viewGroupExpanded.removeAllViews()
        viewGroupCollapsed.removeAllViews()

        //Collapsed
        viewGroupCollapsed.addView(shimmerLoadingView, 0)
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        val adLoaderCollapsed = AdLoader.Builder(
            activity,
            if (isDebug) AdsConstants.admobNativeModelTest.adsID else admobNativeModelCollapsed?.adsID
                ?: admobNativeModelExpanded.adsID
        )
        adLoaderCollapsed.withNativeAdOptions(NativeAdOptions.Builder().build())
        adLoaderCollapsed.forNativeAd { nativeAd ->
            if (isCheckTestAds) checkTestDevice(isEnabledCheckTestDevice, nativeAd)
            val layoutNativeCollapsed =
                layoutCollapsed ?: R.layout.admob_ad_template_small_like_banner
            val adView =
                activity.layoutInflater.inflate(layoutNativeCollapsed, null) as NativeAdView
            NativeUtils.populateNativeAdView(
                nativeAd,
                adView,
                GoogleENative.UNIFIED_SMALL_LIKE_BANNER
            )
            shimmerFrameLayout.stopShimmer()
            viewGroupCollapsed.removeAllViews()
            viewGroupCollapsed.addView(adView)
            nativeAd.setOnPaidEventListener { adValue: AdValue ->
                AdjustUtils.postRevenueAdjustNative(
                    nativeAd,
                    adValue,
                    admobNativeModelCollapsed?.adsID ?: admobNativeModelExpanded.adsID
                )
                FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                SolarUtils.postRevenueSolar(
                    adValue,
                    AdType.NATIVE,
                    admobNativeModelCollapsed?.adsID ?: admobNativeModelExpanded.adsID,
                    nativeAd = nativeAd
                )
            }
        }
        adLoaderCollapsed.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroupCollapsed.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                viewGroupCollapsed.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
            }
        })

        val adLoaderExpanded = AdLoader.Builder(
            activity,
            if (isDebug) AdsConstants.admobNativeModelTest.adsID else admobNativeModelExpanded.adsID
        )
        adLoaderExpanded.withNativeAdOptions(NativeAdOptions.Builder().build())
        adLoaderExpanded.forNativeAd { nativeAd ->
            if (isCheckTestAds) checkTestDevice(isEnabledCheckTestDevice, nativeAd)
            val layoutNativeExpanded = layoutExpanded ?: R.layout.admob_ad_template_medium
            val adView = activity.layoutInflater.inflate(layoutNativeExpanded, null) as NativeAdView
            NativeUtils.populateNativeAdView(
                nativeAd,
                adView,
                GoogleENative.UNIFIED_MEDIUM,
                true
            ) {
                viewGroupExpanded.visibility = View.GONE
                onAdsClosed?.invoke()
            }
            shimmerFrameLayout.stopShimmer()
            viewGroupExpanded.removeAllViews()
            viewGroupExpanded.addView(adView)
            nativeAd.setOnPaidEventListener { adValue: AdValue ->
                AdjustUtils.postRevenueAdjustNative(
                    nativeAd,
                    adValue,
                    admobNativeModelExpanded.adsID
                )
                FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                SolarUtils.postRevenueSolar(
                    adValue,
                    AdType.NATIVE,
                    admobNativeModelExpanded.adsID,
                    nativeAd = nativeAd
                )
            }
        }
        adLoaderExpanded.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroupExpanded.visibility = View.GONE
                onAdsLoadFail?.invoke()
                adLoaderCollapsed.build().loadAd(nativeAdRequest)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                viewGroupExpanded.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
                adLoaderCollapsed.build().loadAd(nativeAdRequest)
            }
        })
        adLoaderExpanded.build().loadAd(nativeAdRequest)
    }

    fun loadAndShowNativeCollapsibleSingle(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        viewGroupExpanded: ViewGroup,
        viewGroupCollapsed: ViewGroup,
        layoutExpanded: Int? = null,
        layoutCollapsed: Int? = null,
        shimmerLayout: Int? = null,
        isShowOnTestDevice: Boolean = false,
        isCheckTestAds: Boolean = false,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        onAdsClosed: (() -> Unit?)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsLoadFail?.invoke()
            viewGroupExpanded.visibility = View.GONE
            viewGroupCollapsed.visibility = View.GONE
            return
        }

        val shimmerInflate = shimmerLayout ?: R.layout.native_small_like_banner_shimmer_layout
        val shimmerLoadingView = activity.layoutInflater.inflate(
            shimmerInflate,
            null,
            false
        )

        val nativeAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()

        viewGroupExpanded.removeAllViews()
        viewGroupCollapsed.removeAllViews()
        viewGroupCollapsed.addView(shimmerLoadingView, 0)

        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        val adLoaderExpanded = AdLoader.Builder(
            activity,
            if (isDebug) AdsConstants.admobNativeModelTest.adsID else admobNativeModel.adsID
        )
        adLoaderExpanded.withNativeAdOptions(NativeAdOptions.Builder().build())
        adLoaderExpanded.forNativeAd { nativeAd ->
            if (isCheckTestAds) checkTestDevice(isEnabledCheckTestDevice, nativeAd)
            val layoutNativeExpanded = layoutExpanded ?: R.layout.admob_ad_template_medium
            val adView = activity.layoutInflater.inflate(layoutNativeExpanded, null) as NativeAdView
            NativeUtils.populateNativeAdView(
                nativeAd,
                adView,
                GoogleENative.UNIFIED_MEDIUM,
                true
            ) {
                viewGroupExpanded.visibility = View.GONE
                onAdsClosed?.invoke()
                val layoutNativeCollapsed =
                    layoutCollapsed ?: R.layout.admob_ad_template_small_like_banner
                val adView =
                    activity.layoutInflater.inflate(layoutNativeCollapsed, null) as NativeAdView
                NativeUtils.populateNativeAdView(
                    nativeAd,
                    adView,
                    GoogleENative.UNIFIED_SMALL_LIKE_BANNER
                )
                viewGroupCollapsed.removeAllViews()
                viewGroupCollapsed.addView(adView)
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    AdjustUtils.postRevenueAdjustNative(
                        nativeAd, adValue,
                        admobNativeModel.adsID
                    )
                    FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                    SolarUtils.postRevenueSolar(
                        adValue,
                        AdType.NATIVE,
                        admobNativeModel.adsID,
                        nativeAd = nativeAd
                    )
                }
            }
            shimmerFrameLayout.stopShimmer()
            viewGroupExpanded.removeAllViews()
            viewGroupExpanded.addView(adView)
        }
        adLoaderExpanded.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroupExpanded.visibility = View.GONE
                viewGroupCollapsed.visibility = View.GONE
                onAdsLoadFail?.invoke()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                viewGroupExpanded.visibility = View.VISIBLE
                viewGroupCollapsed.visibility = View.VISIBLE
                onAdsLoaded?.invoke()
            }
        })
        adLoaderExpanded.build().loadAd(nativeAdRequest)
    }

    fun loadNative(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        size: GoogleENative = GoogleENative.UNIFIED_MEDIUM,
        mediaViewRatio: Int = MediaAspectRatio.SQUARE,
        onAdsLoaded: (() -> Unit?)? = null,
        onAdsLoadFail: (() -> Unit?)? = null,
        isCheckTestAds: Boolean = false,
    ) {
        if (!isShowAds
            || !isNetworkConnected(activity)
            || admobNativeModel.nativeAd.value != null
            || admobNativeModel.isLoading.value == true
        ) {
            onAdsLoadFail?.invoke()
            return
        }
        if (isEnabledCheckTestDevice && isTestDevice) {
            onAdsLoadFail?.invoke()
            return
        }
        admobNativeModel.isLoading.postValue(true)
        val nativeAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()
        val adLoader = AdLoader.Builder(
            activity,
            if (isDebug) AdsConstants.admobNativeModelTest.adsID else admobNativeModel.adsID
        )
        if (size == GoogleENative.UNIFIED_FULL_SCREEN) {
            val videoOptions =
                VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(false)
                    .build()
            val adOptions = NativeAdOptions.Builder()
                .setMediaAspectRatio(mediaViewRatio)
                .setVideoOptions(videoOptions)
                .build()
            adLoader.withNativeAdOptions(adOptions)
        } else {
            adLoader.withNativeAdOptions(NativeAdOptions.Builder().build())
        }
        adLoader.forNativeAd { nativeAd ->
            if (isCheckTestAds) checkTestDevice(isEnabledCheckTestDevice, nativeAd)
            admobNativeModel.nativeAd.value = nativeAd
            nativeAd.setOnPaidEventListener { adValue: AdValue ->
                AdjustUtils.postRevenueAdjustNative(nativeAd, adValue, admobNativeModel.adsID)
                FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                SolarUtils.postRevenueSolar(
                    adValue,
                    AdType.NATIVE,
                    admobNativeModel.adsID,
                    nativeAd = nativeAd
                )
            }
        }
        adLoader.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdsLoadFail?.invoke()
                admobNativeModel.nativeAd.value = null
                admobNativeModel.isLoading.postValue(false)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                onAdsLoaded?.invoke()
                admobNativeModel.isLoading.postValue(false)
            }
        })
        adLoader.build().loadAd(nativeAdRequest)
    }

    fun showNative(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        viewGroup: ViewGroup,
        size: GoogleENative = GoogleENative.UNIFIED_MEDIUM,
        layout: Int? = null,
        shimmerLayout: Int? = null,
        onAdsShowed: (() -> Unit?)? = null,
        onAdsShowFail: (() -> Unit?)? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || isTestDevice) {
            viewGroup.visibility = View.GONE
            onAdsShowFail?.invoke()
            return
        }
        val layoutNative = layout
            ?: when (size) {
                GoogleENative.UNIFIED_MEDIUM -> R.layout.admob_ad_template_medium
                GoogleENative.UNIFIED_SMALL -> R.layout.admob_ad_template_small
                GoogleENative.UNIFIED_SMALL_LIKE_BANNER -> R.layout.admob_ad_template_small_like_banner
                GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON -> R.layout.admob_ad_template_medium_like_button
                GoogleENative.UNIFIED_FULL_SCREEN -> R.layout.admob_ad_template_full_screen
            }

        val shimmerInflate = shimmerLayout ?: when (size) {
            GoogleENative.UNIFIED_MEDIUM -> R.layout.native_medium_shimmer_layout
            GoogleENative.UNIFIED_SMALL -> R.layout.native_small_shimmer_layout
            GoogleENative.UNIFIED_SMALL_LIKE_BANNER -> R.layout.native_small_like_banner_shimmer_layout
            GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON -> R.layout.native_medium_like_button_shimmer_layout
            GoogleENative.UNIFIED_FULL_SCREEN -> R.layout.native_full_screen_shimmer
        }
        val shimmerLoadingView = activity.layoutInflater.inflate(
            shimmerInflate,
            null,
            false
        )
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)

        admobNativeModel.isLoading.observe(activity as LifecycleOwner) { isLoading ->
            try {
                viewGroup.removeAllViews()
                if (isLoading) {
                    viewGroup.addView(shimmerLoadingView, 0)
                    shimmerFrameLayout.startShimmer()
                } else {
                    if (admobNativeModel.nativeAd.value != null) {
                        val adView =
                            activity.layoutInflater.inflate(layoutNative, null) as NativeAdView
                        NativeUtils.populateNativeAdView(
                            admobNativeModel.nativeAd.value!!,
                            adView,
                            size
                        )
                        viewGroup.addView(adView)
                        onAdsShowed?.invoke()
                    } else {
                        viewGroup.visibility = View.GONE
                        onAdsShowFail?.invoke()
                    }
                    shimmerFrameLayout.stopShimmer()
                    admobNativeModel.isLoading.removeObservers(activity as LifecycleOwner)
                }
            } catch (e: Exception) {
                e.message
                onAdsShowFail?.invoke()
                viewGroup.visibility = View.GONE
                admobNativeModel.isLoading.removeObservers(activity as LifecycleOwner)
            }
        }
    }

    fun loadAndShowRewarded(
        activity: AppCompatActivity,
        admobRewardedModel: AdmobRewardedModel,
        timeout: Long = 15000L,
        isShowOnTestDevice: Boolean = false,
        onAdsCloseOrFailed: ((isEarned: Boolean) -> Unit)? = null,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsImpression: (() -> Unit)? = null
    ) {
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsCloseOrFailed?.invoke(false)
            onAdsFail?.invoke()
            return
        }
        var isEarnedReward = false
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        val rewardedAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(timeout.toInt()).build()
        RewardedAd.load(
            activity,
            if (isDebug) AdsConstants.admobRewardedModelTest.adsID else admobRewardedModel.adsID,
            rewardedAdRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isShowRewardAds = false
                    dismissDialogFullScreen()
                    admobRewardedModel.rewardAd.value = null
                    onAdsCloseOrFailed?.invoke(isEarnedReward)
                    onAdsFail?.invoke()
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    onAdsLoaded?.invoke()
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            onAdsClicked?.invoke()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            isShowRewardAds = false
                            admobRewardedModel.rewardAd.value = null
                            onAdsCloseOrFailed?.invoke(isEarnedReward)
                            onAdsClose?.invoke()
                            handle.removeCallbacksAndMessages(0)
                            AppOnResumeAdsManager.getInstance()
                                .setAppResumeEnabled(true)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isShowRewardAds = false
                            admobRewardedModel.rewardAd.value = null
                            onAdsCloseOrFailed?.invoke(isEarnedReward)
                            onAdsFail?.invoke()
                            handle.removeCallbacksAndMessages(0)
                            AppOnResumeAdsManager.getInstance()
                                .setAppResumeEnabled(true)
                        }

                        override fun onAdImpression() {
                            onAdsImpression?.invoke()
                        }

                        override fun onAdShowedFullScreenContent() {
                            isShowRewardAds = true
                            onAdsShowed?.invoke()
                            handle.postDelayed({
                                dismissDialogFullScreen()
                            }, 800)
                        }
                    }
                    ad.onPaidEventListener = OnPaidEventListener { adValue ->
                        AdjustUtils.postRevenueAdjustRewarded(ad, adValue, ad.adUnitId)
                        FacebookUtils.adImpressionFacebookRevenue(activity, adValue)
                        SolarUtils.postRevenueSolar(
                            adValue,
                            AdType.REWARDED,
                            admobRewardedModel.adsID,
                            rewardAd = ad
                        )
                    }
                    ad.show(activity) { _ ->
                        isEarnedReward = true
                    }
                }
            })
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                isShowRewardAds = false
                dismissDialogFullScreen()
                admobRewardedModel.rewardAd.value = null
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadRewarded(
        activity: Activity,
        admobRewardedModel: AdmobRewardedModel,
        isShowOnTestDevice: Boolean = false,
        onAdsLoaded: (() -> Unit)? = null,
        onAdsFail: (() -> Unit)? = null
    ) {
        if (!isShowAds
            || isShowRewardAds
            || !isNetworkConnected(activity)
            || admobRewardedModel.rewardAd.value != null
            || admobRewardedModel.isLoading.value == true
            || (!isShowOnTestDevice && isTestDevice)
        ) {
            onAdsFail?.invoke()
            return
        }
        admobRewardedModel.isLoading.postValue(true)
        val rewardedAdRequest =
            adRequest ?: AdRequest.Builder().setHttpTimeoutMillis(10000).build()
        RewardedAd.load(
            activity,
            if (isDebug) AdsConstants.admobRewardedModelTest.adsID else admobRewardedModel.adsID,
            rewardedAdRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onAdsFail?.invoke()
                    isShowRewardAds = false
                    admobRewardedModel.rewardAd.value = null
                    admobRewardedModel.isLoading.postValue(false)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    onAdsLoaded?.invoke()
                    isShowRewardAds = false
                    admobRewardedModel.rewardAd.value = rewardedAd
                    admobRewardedModel.isLoading.postValue(false)
                    rewardedAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustRewarded(rewardedAd, it, rewardedAd.adUnitId)
                        FacebookUtils.adImpressionFacebookRevenue(activity, it)
                        SolarUtils.postRevenueSolar(
                            it,
                            AdType.REWARDED,
                            admobRewardedModel.adsID,
                            rewardAd = rewardedAd
                        )
                    }
                }
            })
    }

    fun showRewarded(
        activity: AppCompatActivity,
        admobRewardedModel: AdmobRewardedModel,
        isPreload: Boolean = true,
        isShowOnTestDevice: Boolean = false,
        onAdsCloseOrFailed: (isEarned: Boolean) -> Unit,
        onAdsFail: (() -> Unit)? = null,
        onAdsClose: (() -> Unit)? = null,
        onAdsShowed: (() -> Unit)? = null,
        onAdsClicked: (() -> Unit)? = null,
        onAdsImpression: (() -> Unit)? = null
    ) {
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity) || (!isShowOnTestDevice && isTestDevice)) {
            onAdsCloseOrFailed.invoke(false)
            onAdsFail?.invoke()
            return
        }
        var isEarnedReward = false
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)

        admobRewardedModel.isLoading.observe(activity as LifecycleOwner) { isLoading ->
            if (isLoading) {
                showDialogFullScreen(activity)
            } else {
                if (admobRewardedModel.rewardAd.value != null) {
                    val handle = Handler(Looper.getMainLooper())
                    admobRewardedModel.rewardAd.value?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isShowRewardAds = false
                                admobRewardedModel.rewardAd.value = null
                                admobRewardedModel.isLoading.removeObservers(activity as LifecycleOwner)
                                onAdsCloseOrFailed.invoke(isEarnedReward)
                                onAdsClose?.invoke()
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                                if (isPreload) {
                                    loadRewarded(activity, admobRewardedModel)
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isShowRewardAds = false
                                admobRewardedModel.rewardAd.value = null
                                admobRewardedModel.isLoading.removeObservers(activity as LifecycleOwner)
                                onAdsCloseOrFailed.invoke(isEarnedReward)
                                onAdsFail?.invoke()
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                            }

                            override fun onAdShowedFullScreenContent() {
                                isShowRewardAds = true
                                onAdsShowed?.invoke()
                                handle.postDelayed({
                                    dismissDialogFullScreen()
                                }, 800)
                            }

                            override fun onAdImpression() {
                                onAdsImpression?.invoke()
                            }

                            override fun onAdClicked() {
                                onAdsClicked?.invoke()
                            }
                        }
                    admobRewardedModel.rewardAd.value?.show(activity) { _ ->
                        isEarnedReward = true
                    }
                } else {
                    isShowRewardAds = false
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed.invoke(false)
                    onAdsFail?.invoke()
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                    admobRewardedModel.isLoading.removeObservers(activity as LifecycleOwner)
                    if (isPreload) {
                        loadRewarded(activity, admobRewardedModel)
                    }
                }
            }
        }
    }

    // region show inter with native after
    private fun createNativeFullScreen(
        mActivity: AppCompatActivity,
        model: AdmobNativeModel,
        layout: Int = R.layout.admob_ad_template_full_screen,
        isShowNative: Boolean = true,
        isStartNow: Boolean = false,
        counter: Int = NativeAfterInterDialog.DEFAULT_COUNTER,
        navAction: () -> Unit,
        onFailure: () -> Unit = navAction
    ): NativeAfterInterDialog? {
        if (!isShowNative) return null

        return NativeAfterInterDialog(
            mActivity, model, layout, isStartNow, counter, navAction, onFailure
        )
    }

    private fun loadNativeFullScreen(
        mActivity: AppCompatActivity,
        model: AdmobNativeModel,
        isShowNative: Boolean = true,
    ) {
        if (!isShowNative) return

        loadNative(mActivity, model, GoogleENative.UNIFIED_FULL_SCREEN, MediaAspectRatio.ANY)
    }

    fun loadAndShowInterWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: AdmobNativeModel,
        vShowInterAds: View?,
        isShowNativeAfter: Boolean = true,
        nativeLayout: Int = R.layout.admob_ad_template_full_screen,
        counter: Int = NativeAfterInterDialog.DEFAULT_COUNTER,
        isShowOnTestDevice: Boolean = false,
        onInterCloseOrFailed: (isDone: Boolean) -> Unit = {},
        navAction: () -> Unit
    ) {
        vShowInterAds?.visibility = View.VISIBLE
        loadNativeFullScreen(mActivity, nativeModel, isShowNativeAfter)
        var nativeDialog: NativeAfterInterDialog? = null
        var isNativeFail = false
        loadAndShowInterstitial(
            mActivity,
            interModel,
            isShowOnTestDevice = isShowOnTestDevice,
            onAdsShowed = {
//            Log.d("TAG", "loadAndShowInterWithNativeAfter: on showed")
                mActivity.lifecycleScope.launch {
                    delay(1000)
                    nativeDialog = createNativeFullScreen(
                        mActivity,
                        nativeModel,
                        layout = nativeLayout,
                        isShowNative = isShowNativeAfter,
                        navAction = navAction,
                        counter = counter,
                        onFailure = {
                            isNativeFail = true
                        }
                    )
                    nativeDialog?.show()
                }
            },
            onAdsFail = {
//            Log.d("TAG", "loadAndShowInterWithNativeAfter: on fail")
                mActivity.lifecycleScope.launch {
                    createNativeFullScreen(
                        mActivity,
                        nativeModel,
                        layout = nativeLayout,
                        isShowNative = isShowNativeAfter,
                        isStartNow = true,
                        counter = counter,
                        navAction = navAction
                    )?.show()
                }
            },
            onAdsCloseOrFailed = {
                mActivity.lifecycleScope.launch {
                    if (!isShowNativeAfter || isNativeFail) {
                        navAction()
                    } else {
                        nativeDialog?.isClosedOrFail = true
                    }
                }
                onInterCloseOrFailed(it)
            }
        )
    }

    fun loadAndShowInterSplashWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: AdmobNativeModel,
        isShowNativeAfter: Boolean = true,
        nativeLayout: Int = R.layout.admob_ad_template_full_screen,
        counter: Int = NativeAfterInterDialog.DEFAULT_COUNTER,
        vShowInterAds: View? = null,
        navAction: () -> Unit
    ) {
        vShowInterAds?.visibility = View.VISIBLE
        loadNativeFullScreen(mActivity, nativeModel, isShowNativeAfter)
        var nativeDialog: NativeAfterInterDialog? = null
        var isNativeFail = false
        loadAndShowInterstitialSplash(
            mActivity,
            interModel,
            onAdsShowed = {
//            Log.d("TAG", "loadAndShowInterWithNativeAfter: on showed")
                mActivity.lifecycleScope.launch {
                    delay(1000)
                    nativeDialog = createNativeFullScreen(
                        mActivity,
                        nativeModel,
                        layout = nativeLayout,
                        isShowNative = isShowNativeAfter,
                        counter = counter,
                        navAction = navAction,
                        onFailure = {
                            isNativeFail = true
                        }
                    )
                    nativeDialog?.show()
                }
            },
            onAdsFail = {
//            Log.d("TAG", "loadAndShowInterWithNativeAfter: on fail")
                mActivity.lifecycleScope.launch {
                    createNativeFullScreen(
                        mActivity,
                        nativeModel,
                        layout = nativeLayout,
                        isShowNative = isShowNativeAfter,
                        isStartNow = true,
                        counter = counter,
                        navAction = navAction
                    )?.show()
                }
            },
            onAdsCloseOrFailed = {
                mActivity.lifecycleScope.launch {
                    if (!isShowNativeAfter || isNativeFail) {
                        navAction()
                    } else {
                        nativeDialog?.isClosedOrFail = true
                    }
                }
            }
        )
    }

    // endregion

    fun getInitAds(): Boolean {
        return isInitAds
    }

    fun getDebugAds(): Boolean {
        return isDebug
    }

    fun setDebugAds(isDebug: Boolean) {
        AdmobLib.isDebug = isDebug
    }

    fun getShowAds(): Boolean {
        return isShowAds
    }

    fun setShowAds(isShowAds: Boolean) {
        AdmobLib.isShowAds = isShowAds
    }

    fun getShowInterAds(): Boolean {
        return isShowInterAds
    }

    fun getShowRewardAds(): Boolean {
        return isShowRewardAds
    }

    fun getEnabledCheckTestDevice(): Boolean {
        return isEnabledCheckTestDevice
    }

    fun setEnabledCheckTestDevice(isEnabledCheckTestDevice: Boolean) {
        AdmobLib.isEnabledCheckTestDevice = isEnabledCheckTestDevice
    }

    fun getCheckTestDevice(): Boolean {
        return isTestDevice
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("is_init_ads", isInitAds)
        outState.putBoolean("is_debug", isDebug)
        outState.putBoolean("is_show_ads", isShowAds)
        outState.putBoolean("is_enable_check_test_device", isEnabledCheckTestDevice)
        outState.putBoolean("is_test_device", isTestDevice)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey("is_init_ads")) {
            isInitAds = savedInstanceState.getBoolean("is_init_ads")
        }
        if (savedInstanceState.containsKey("is_debug")) {
            isDebug = savedInstanceState.getBoolean("is_debug")
        }
        if (savedInstanceState.containsKey("is_show_ads")) {
            isShowAds = savedInstanceState.getBoolean("is_show_ads")
        }
        if (savedInstanceState.containsKey("is_enable_check_test_device")) {
            isEnabledCheckTestDevice = savedInstanceState.getBoolean("is_enable_check_test_device")
        }
        if (savedInstanceState.containsKey("is_test_device")) {
            isInitAds = savedInstanceState.getBoolean("is_test_device")
        }
    }


    private fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    private fun checkTestDevice(isEnabledCheck: Boolean, ad: NativeAd?) {
        if (!isEnabledCheck) {
            isTestDevice = false
        } else {
            try {
                val testAdResponse = ad?.headline.toString().replace(" ", "").split(":")[0]
                Log.d("TAG=====", testAdResponse)
                val testAdResponses = arrayOf(
                    "TestAd",
                    "Anunciodeprueba",
                    "Annoncetest",
                    "테스트광고",
                    "Annuncioditesto",
                    "Testanzeige",
                    "TesIklan",
                    "Anúnciodeteste",
                    "Тестовоеобъявление",
                    "পরীক্ষামূলকবিজ্ঞাপন",
                    "जाँचविज्ञापन",
                    "إعلانتجريبي",
                    "Quảngcáothửnghiệm",
                    "IklanUjian",
                    "Reklamatestowa",
                    "Testannoncer",
                    "Testadvertenties",
                    "Testannonser",
                    "Testimainokset",
                    "TestReklamları"
                )
                isTestDevice = testAdResponses.contains(testAdResponse)
            } catch (e: Exception) {
                isTestDevice = true
                Log.d("TAG=====", "Error: ${e.message}")
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
}