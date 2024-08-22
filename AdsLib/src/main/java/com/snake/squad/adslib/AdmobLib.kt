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
import androidx.lifecycle.lifecycleScope
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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.models.AdmobRewardedModel
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslib.utils.AdsHelper.isNetworkConnected
import com.snake.squad.adslib.utils.BannerCollapsibleType
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslib.utils.NativeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdmobLib {

    private var isDebug = false
    private var isShowAds = true
    private var isShowInterAds = false
    private var isShowRewardAds = false
    private var adRequest: AdRequest? = null
    private var dialogFullScreen: Dialog? = null

    fun initialize(
        context: Context,
        timeout: Int = 10000,
        isDebug: Boolean = true,
        isShowAds: Boolean = true
    ) {
        AdmobLib.isDebug = isDebug
        AdmobLib.isShowAds = isShowAds
        MobileAds.initialize(context) { Log.d("TAG=====", "initAds") }
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf())
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        initAdRequest(timeout)
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
        onAdsCloseOrFailed: (Boolean) -> Unit
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed.invoke(false)
            return
        }
        val handle = Handler(Looper.getMainLooper())
        val interAdRequest = adRequest ?: AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed.invoke(false)
                    admobInterModel.interstitialAd = null
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    showDialogFullScreen(activity)
                    AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
                    handle.postDelayed({
                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    isShowInterAds = false
                                    onAdsCloseOrFailed.invoke(true)
                                    admobInterModel.interstitialAd = null
                                    handle.removeCallbacksAndMessages(0)
                                    AppOnResumeAdsManager.getInstance()
                                        .setAppResumeEnabled(true)
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    isShowInterAds = false
                                    dismissDialogFullScreen()
                                    onAdsCloseOrFailed.invoke(false)
                                    admobInterModel.interstitialAd = null
                                    handle.removeCallbacksAndMessages(0)
                                    AppOnResumeAdsManager.getInstance()
                                        .setAppResumeEnabled(true)
                                }

                                override fun onAdClicked() {}
                                override fun onAdImpression() {}
                                override fun onAdShowedFullScreenContent() {
                                    isShowInterAds = true
                                    handle.postDelayed({
                                        dismissDialogFullScreen()
                                    }, 800)
                                }
                            }
                        interstitialAd.setOnPaidEventListener {
                            AdjustUtils.postRevenueAdjustInter(interstitialAd, it, admobInterModel.adsID)
                        }
                        interstitialAd.show(activity)
                    }, 800)
                }
            })
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((admobInterModel.interstitialAd == null) || (!isShowInterAds)) {
                isShowInterAds = false
                dismissDialogFullScreen()
                onAdsCloseOrFailed.invoke(false)
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadAndShowInterstitial(
        activity: AppCompatActivity,
        admobInterModel: AdmobInterModel,
        timeout: Long = 15000,
        onAdsCloseOrFailed: (Boolean) -> Unit
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed.invoke(false)
            return
        }
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        val interAdRequest = adRequest ?: AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissDialogFullScreen()
                    onAdsCloseOrFailed.invoke(false)
                    admobInterModel.interstitialAd = null
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isShowInterAds = false
                                onAdsCloseOrFailed.invoke(true)
                                admobInterModel.interstitialAd = null
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isShowInterAds = false
                                dismissDialogFullScreen()
                                onAdsCloseOrFailed.invoke(false)
                                admobInterModel.interstitialAd = null
                                handle.removeCallbacksAndMessages(0)
                                AppOnResumeAdsManager.getInstance()
                                    .setAppResumeEnabled(true)
                            }

                            override fun onAdClicked() {}
                            override fun onAdImpression() {}
                            override fun onAdShowedFullScreenContent() {
                                isShowInterAds = true
                                handle.postDelayed({
                                    dismissDialogFullScreen()
                                }, 800)
                            }
                        }
                    interstitialAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustInter(interstitialAd, it, admobInterModel.adsID)
                    }
                    interstitialAd.show(activity)
                }
            })
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((admobInterModel.interstitialAd == null) || (!isShowInterAds)) {
                isShowInterAds = false
                dismissDialogFullScreen()
                onAdsCloseOrFailed.invoke(false)
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadInterstitial(activity: Activity, admobInterModel: AdmobInterModel) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity) || admobInterModel.interstitialAd != null) {
            return
        }
        val interAdRequest = adRequest ?: AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            if (isDebug) AdsConstants.admobInterModelTest.adsID else admobInterModel.adsID,
            interAdRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isShowInterAds = false
                    admobInterModel.interstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isShowInterAds = false
                    admobInterModel.interstitialAd = interstitialAd
                    interstitialAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustInter(interstitialAd, it, admobInterModel.adsID)
                    }
                }
            })
    }

    fun showInterstitial(
        activity: AppCompatActivity,
        admobInterModel: AdmobInterModel,
        timeout: Long = 10000,
        isPreload: Boolean = true,
        onAdsCloseOrFailed: (Boolean) -> Unit
    ) {
        if (!isShowAds || isShowInterAds || !isNetworkConnected(activity) || admobInterModel.interstitialAd == null) {
            if (admobInterModel.interstitialAd == null) loadInterstitial(activity, admobInterModel)
            onAdsCloseOrFailed.invoke(false)
            return
        }
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        handle.postDelayed({
            admobInterModel.interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowInterAds = false
                        onAdsCloseOrFailed.invoke(true)
                        admobInterModel.interstitialAd = null
                        handle.removeCallbacksAndMessages(0)
                        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                        if (isPreload) {
                            loadInterstitial(activity, admobInterModel)
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowInterAds = false
                        dismissDialogFullScreen()
                        onAdsCloseOrFailed.invoke(false)
                        admobInterModel.interstitialAd = null
                        handle.removeCallbacksAndMessages(0)
                        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
                        if (isPreload) {
                            loadInterstitial(activity, admobInterModel)
                        }
                    }

                    override fun onAdClicked() {}
                    override fun onAdImpression() {}
                    override fun onAdShowedFullScreenContent() {
                        isShowInterAds = true
                        handle.postDelayed({
                            dismissDialogFullScreen()
                        }, 800)
                    }
                }
            admobInterModel.interstitialAd?.show(activity)
        }, 800)
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((admobInterModel.interstitialAd == null) || (!isShowInterAds)) {
                isShowInterAds = false
                dismissDialogFullScreen()
                onAdsCloseOrFailed.invoke(false)
                handle.removeCallbacksAndMessages(0)
                AppOnResumeAdsManager.getInstance().setAppResumeEnabled(true)
            }
        }
    }

    fun loadAndShowBanner(
        activity: Activity,
        bannerID: String,
        viewGroup: ViewGroup,
        viewLine: View
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            return
        }
        val adView = AdView(activity)
        adView.adUnitId = if (isDebug) AdsConstants.ADMOB_BANNER_TEST else bannerID
        adView.setAdSize(getAdSize(activity))
        val shimmerLoadingView =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
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
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}
        }
        if (adRequest != null) {
            adView.loadAd(adRequest!!)
        }
    }

    fun loadAndShowBannerCollapsible(
        activity: Activity,
        bannerID: String,
        viewGroup: ViewGroup,
        viewLine: View,
        collapsibleType: BannerCollapsibleType? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            viewLine.visibility = View.GONE
            return
        }
        val adView = AdView(activity)
        adView.adUnitId =
            if (isDebug) AdsConstants.ADMOB_BANNER_COLLAPSE_TEST else bannerID
        val adSize = getAdSize(activity)
        adView.setAdSize(adSize)
        val shimmerLoadingView =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(shimmerLoadingView, 0)
            viewGroup.addView(adView, 1)
        } catch (e: Exception) {
            e.message
        }
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.setOnPaidEventListener {
                    AdjustUtils.postRevenueAdjust(it, adView.adUnitId)
                }
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                val params: ViewGroup.LayoutParams = viewGroup.layoutParams
                params.height = adSize.getHeightInPixels(activity)
                viewGroup.layoutParams = params
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeView(shimmerLoadingView)
                viewGroup.visibility = View.GONE
                viewLine.visibility = View.GONE
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}
        }
        val extras = Bundle()
        extras.putString("collapsible", collapsibleType?.value ?: BannerCollapsibleType.BOTTOM.value)
        val adRequestCollapsible =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        adView.loadAd(adRequestCollapsible)
    }

    fun loadAndShowNative(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        viewGroup: ViewGroup,
        isMedium: Boolean = true,
        layout: Int? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val shimmerLoadingView = if (isMedium) {
            activity.layoutInflater.inflate(R.layout.native_medium_shimmer_layout, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.native_small_shimmer_layout, null, false)
        }
        viewGroup.removeAllViews()
        viewGroup.addView(shimmerLoadingView, 0)
        val shimmerFrameLayout =
            shimmerLoadingView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        val nativeAdRequest = adRequest ?: AdRequest.Builder().build()
        val adLoader = AdLoader.Builder(
            activity,
            if (isDebug) activity.getString(R.string.native_id_test) else admobNativeModel.adsID
        )
            .forNativeAd { nativeAd ->
                val layoutNative = layout
                    ?: if (isMedium) {
                        R.layout.admob_ad_template_medium
                    } else {
                        R.layout.admob_ad_template_small
                    }
                val adView = activity.layoutInflater.inflate(layoutNative, null) as NativeAdView
                NativeUtils.populateNativeAdView(
                    nativeAd,
                    adView,
                    if (isMedium) GoogleENative.UNIFIED_MEDIUM else GoogleENative.UNIFIED_SMALL
                )
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    AdjustUtils.postRevenueAdjustNative(nativeAd, adValue, admobNativeModel.adsID)
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    viewGroup.visibility = View.VISIBLE
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        adLoader.loadAd(nativeAdRequest)
    }

    fun loadNative(activity: Activity, admobNativeModel: AdmobNativeModel) {
        if (!isShowAds || !isNetworkConnected(activity) || admobNativeModel.nativeAd != null) {
            return
        }
        val nativeAdRequest = adRequest ?: AdRequest.Builder().build()
        val adLoader = AdLoader.Builder(
            activity,
            if (isDebug) activity.getString(R.string.native_id_test) else admobNativeModel.adsID
        ).forNativeAd { nativeAd ->
            admobNativeModel.nativeAd = nativeAd
            nativeAd.setOnPaidEventListener { adValue: AdValue ->
                AdjustUtils.postRevenueAdjustNative(nativeAd, adValue, admobNativeModel.adsID)
            }
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                admobNativeModel.nativeAd = null
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        adLoader.loadAd(nativeAdRequest)
    }

    fun showNative(
        activity: Activity,
        admobNativeModel: AdmobNativeModel,
        viewGroup: ViewGroup,
        isMedium: Boolean = true,
        layout: Int? = null
    ) {
        if (!isShowAds || !isNetworkConnected(activity) || admobNativeModel.nativeAd == null) {
            viewGroup.visibility = View.GONE
            return
        }
        val layoutNative = layout
            ?: if (isMedium) {
                R.layout.admob_ad_template_medium
            } else {
                R.layout.admob_ad_template_small
            }
        val adView = activity.layoutInflater.inflate(layoutNative, null) as NativeAdView
        NativeUtils.populateNativeAdView(
            admobNativeModel.nativeAd!!,
            adView,
            if (isMedium) GoogleENative.UNIFIED_MEDIUM else GoogleENative.UNIFIED_SMALL
        )
        viewGroup.removeAllViews()
        viewGroup.addView(adView)
    }

    fun loadAndShowRewarded(
        activity: Activity,
        admobRewardedModel: AdmobRewardedModel,
        onAdsCloseOrFailed: (isEarned: Boolean) -> Unit
    ) {
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity)) {
            onAdsCloseOrFailed.invoke(false)
            return
        }
        var isEarnedReward = false
        showDialogFullScreen(activity)
        val handle = Handler(Looper.getMainLooper())
        AppOnResumeAdsManager.getInstance().setAppResumeEnabled(false)
        val rewardedAdRequest = adRequest ?: AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            admobRewardedModel.adsID,
            rewardedAdRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isShowRewardAds = false
                    dismissDialogFullScreen()
                    admobRewardedModel.rewardAd = null
                    onAdsCloseOrFailed.invoke(isEarnedReward)
                    handle.removeCallbacksAndMessages(0)
                    AppOnResumeAdsManager.getInstance()
                        .setAppResumeEnabled(true)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {

                        }

                        override fun onAdDismissedFullScreenContent() {
                            isShowRewardAds = false
                            admobRewardedModel.rewardAd = null
                            onAdsCloseOrFailed.invoke(isEarnedReward)
                            handle.removeCallbacksAndMessages(0)
                            AppOnResumeAdsManager.getInstance()
                                .setAppResumeEnabled(true)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isShowRewardAds = false
                            admobRewardedModel.rewardAd = null
                            onAdsCloseOrFailed.invoke(isEarnedReward)
                            handle.removeCallbacksAndMessages(0)
                            AppOnResumeAdsManager.getInstance()
                                .setAppResumeEnabled(true)
                        }

                        override fun onAdImpression() {}

                        override fun onAdShowedFullScreenContent() {
                            isShowRewardAds = true
                            handle.postDelayed({
                                dismissDialogFullScreen()
                            }, 800)
                        }
                    }
                    ad.onPaidEventListener = OnPaidEventListener { adValue ->
                        AdjustUtils.postRevenueAdjustRewarded(ad, adValue, ad.adUnitId)
                    }
                    ad.show(activity) { _ ->
                        isEarnedReward = true
                    }
                }
            })
    }

    fun loadRewarded(activity: Activity, admobRewardedModel: AdmobRewardedModel) {
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity) || admobRewardedModel.rewardAd != null) {
            return
        }
        val rewardedAdRequest = adRequest ?: AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            admobRewardedModel.adsID,
            rewardedAdRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isShowRewardAds = false
                    admobRewardedModel.rewardAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    isShowRewardAds = false
                    admobRewardedModel.rewardAd = rewardedAd
                    rewardedAd.setOnPaidEventListener {
                        AdjustUtils.postRevenueAdjustRewarded(rewardedAd, it, rewardedAd.adUnitId)
                    }
                }
            })
    }

    fun showRewarded(
        activity: Activity,
        admobRewardedModel: AdmobRewardedModel,
        isPreload: Boolean = true,
        onAdsCloseOrFailed: (isEarned: Boolean) -> Unit
    ) {
        if (!isShowAds || isShowRewardAds || !isNetworkConnected(activity) || admobRewardedModel.rewardAd == null) {
            onAdsCloseOrFailed.invoke(false)
            return
        }
        var isEarnedReward = false
        val handle = Handler(Looper.getMainLooper())
        showDialogFullScreen(activity)
        handle.postDelayed({
            admobRewardedModel.rewardAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowRewardAds = false
                        admobRewardedModel.rewardAd = null
                        onAdsCloseOrFailed.invoke(isEarnedReward)
                        handle.removeCallbacksAndMessages(0)
                        AppOnResumeAdsManager.getInstance()
                            .setAppResumeEnabled(true)
                        if (isPreload) {
                            loadRewarded(activity, admobRewardedModel)
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowRewardAds = false
                        admobRewardedModel.rewardAd = null
                        onAdsCloseOrFailed.invoke(isEarnedReward)
                        handle.removeCallbacksAndMessages(0)
                        AppOnResumeAdsManager.getInstance()
                            .setAppResumeEnabled(true)
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowRewardAds = true
                        handle.postDelayed({
                            dismissDialogFullScreen()
                        }, 800)
                    }

                    override fun onAdImpression() {}
                    override fun onAdClicked() {}
                }
            admobRewardedModel.rewardAd?.show(activity) { _ ->
                isEarnedReward = true
            }
        }, 800)
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

    fun setShowInterAds(isShowInterAds: Boolean) {
        AdmobLib.isShowInterAds = isShowInterAds
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

    private fun showDialogFullScreen(activity: Activity) {
        try {
            dialogFullScreen = Dialog(activity)
            dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogFullScreen?.setContentView(R.layout.dialog_loading_ads_full_screen)
            dialogFullScreen?.setCancelable(false)
            dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
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