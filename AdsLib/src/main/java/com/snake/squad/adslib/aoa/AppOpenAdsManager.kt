package com.snake.squad.adslib.aoa

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.R
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.utils.AdsConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppOpenAdsManager(
    private val activity: Activity,
    private val adsID: String,
    private val timeOut: Long,
    private val onAdsCloseOrFailed: (Boolean) -> Unit
) {

    var isLoadingAd = true
    var isShowingAd = true
    private var appOpenAd: AppOpenAd? = null
    private var dialogFullScreen: Dialog? = null

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    fun loadAndShowAoA() {
        if (!AdmobLib.getShowAds()) {
            onAoaDestroyed()
            onAdsCloseOrFailed.invoke(false)
            return
        }
        val appOpenID = if (AdmobLib.getDebugAds()){
            AdsConstants.APP_OPEN_TEST
        } else {
            adsID
        }
        val job = CoroutineScope(Dispatchers.Main).launch {
            delay(timeOut)
            if (isLoadingAd) {
                isLoadingAd = false
                onAoaDestroyed()
                onAdsCloseOrFailed.invoke(false)
            }
        }
        if (isAdAvailable) {
            job.cancel()
            onAdsCloseOrFailed.invoke(false)
            return
        } else {
            isShowingAd = false
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                activity,
                appOpenID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        job.cancel()
                        isLoadingAd = false
                        onAdsCloseOrFailed.invoke(false)
                        super.onAdFailedToLoad(p0)
                    }

                    override fun onAdLoaded(ad: AppOpenAd) {
                        super.onAdLoaded(ad)
                        appOpenAd = ad
                        ad.setOnPaidEventListener { adValue ->
                            AdjustUtils.postRevenueAdjust(adValue, ad.adUnitId)
                        }
                        job.cancel()
                        if (!isShowingAd) {
                            showAdIfAvailable()
                        }
                    }
                })
        }
    }

    fun showAdIfAvailable() {
        if (AdmobLib.getShowAds() && !isShowingAd && isAdAvailable && isLoadingAd) {
            isLoadingAd = false
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        dismissDialogFullScreen()
                        appOpenAd = null
                        isShowingAd = true
                        onAdsCloseOrFailed.invoke(true)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        dismissDialogFullScreen()
                        isShowingAd = true
                        onAdsCloseOrFailed.invoke(false)
                        Log.d("====Timeout", "Failed... $p0")
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                this.fullScreenContentCallback = fullScreenContentCallback
                showDialogFullScreen()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isShowingAd) {
                        setOnPaidEventListener { adValue ->
                            AdjustUtils.postRevenueAdjust(adValue, adUnitId)
                        }
                        show(activity)
                    } else {
                        onAdsCloseOrFailed.invoke(false)
                    }
                }, 800)
            }
        } else {
            onAdsCloseOrFailed.invoke(false)
        }
    }

    fun onAoaDestroyed() {
        isShowingAd = true
        isLoadingAd = false
        dismissDialogFullScreen()
        appOpenAd?.fullScreenContentCallback?.onAdDismissedFullScreenContent()
    }

    init {
        initDialogFullScreen()
    }

    private fun initDialogFullScreen() {
        dialogFullScreen = Dialog(activity)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_loading_ads_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    private fun showDialogFullScreen() {
        try {
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