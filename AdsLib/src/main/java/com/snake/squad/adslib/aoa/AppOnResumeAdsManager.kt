package com.snake.squad.adslib.aoa

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.ApplovinLib
import com.snake.squad.adslib.R
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.utils.AdsConstants

class AppOnResumeAdsManager : ActivityLifecycleCallbacks {

    companion object {

        private var instance: AppOnResumeAdsManager? = null
        private var application: Application? = null
        private var adsID: String? = null

        fun initialize(application: Application, adUnitId: String) {
            this.application = application
            this.adsID = adUnitId
            instance = AppOnResumeAdsManager()
        }

        fun getInstance(): AppOnResumeAdsManager {
            return instance ?: AppOnResumeAdsManager()
        }
    }

    private val logTag = "AppOpenAdManager"
    private var appOpenAd: AppOpenAd? = null
    private var adRequest: AdRequest? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var isAppResumeEnabled = true
    private var dialogFullScreen: Dialog? = null
    private var currentActivity: Activity? = null
    private val disabledActivities = mutableSetOf<Class<*>>()

    init {
        initAdRequest()
        application?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && isAppResumeEnabled) {
                currentActivity?.let {
                    showAdIfAvailable(it)
                }
            }
        })
    }

    fun disableForActivity(activityClass: Class<*>) {
        Log.d(logTag, "Disabling App Open Ads for ${activityClass.simpleName}")
        disabledActivities.add(activityClass)
    }

    fun enableForActivity(activityClass: Class<*>) {
        Log.d(logTag, "Enabling App Open Ads for ${activityClass.simpleName}")
        disabledActivities.remove(activityClass)
    }

    fun setAppResumeEnabled(enabled: Boolean) {
        isAppResumeEnabled = enabled
    }

    // get AdRequest
    private fun initAdRequest() {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(5000)
            .build()
        loadAd()
    }

    private fun loadAd() {
        if (isLoadingAd || isAdAvailable() || !AdmobLib.getShowAds() || AdmobLib.getEnabledCheckTestDevice()) {
            return
        }
        val appOnResumeID = if (AdmobLib.getDebugAds()) {
            AdsConstants.APP_OPEN_TEST
        } else {
            adsID
        }
        if (application != null && appOnResumeID != null) {
            adRequest?.let {
                isLoadingAd = true
                AppOpenAd.load(
                    application!!,
                    appOnResumeID,
                    it,
                    object : AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            Log.d(logTag, "Ad loaded successfully")
                            appOpenAd = ad
                            ad.setOnPaidEventListener { adValue ->
                                AdjustUtils.postRevenueAdjust(adValue, ad.adUnitId)
                            }
                            isLoadingAd = false
                            loadTime = System.currentTimeMillis()
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(logTag, "Ad failed to load: ${loadAdError.message}")
                            isLoadingAd = false
                        }
                    }
                )
            }
        }
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < 4 * 3600000
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (
            !AdmobLib.getShowAds()
            || AdmobLib.getShowInterAds()
            || AdmobLib.getShowRewardAds()
            || ApplovinLib.getShowInterAds()
            || ApplovinLib.getShowRewardAds()
            || disabledActivities.contains(activity.javaClass)
            || AdmobLib.getEnabledCheckTestDevice()
        ) {
            return
        }
        if (!isAdAvailable()) {
            loadAd()
            return
        }
        if (isShowingAd) {
            dismissDialogFullScreen()
            appOpenAd?.fullScreenContentCallback?.onAdDismissedFullScreenContent()
        }
        showDialogFullScreen(activity)
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(logTag, "Ad dismissed")
                appOpenAd = null
                isShowingAd = false
                loadAd()
                dismissDialogFullScreen()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(logTag, "Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd()
                dismissDialogFullScreen()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                Log.d(logTag, "Ad shown")
            }
        }
        appOpenAd?.show(activity)
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}