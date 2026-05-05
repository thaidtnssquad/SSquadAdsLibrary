package com.snake.squad.adslib.aoa.onresume

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.ApplovinLib
import com.snake.squad.adslib.R

internal abstract class BaseOnResumeManager(application: Application): ActivityLifecycleCallbacks {

    protected abstract val logTag: String

    protected val context: Context = application

    protected var isLoadingAd = false
    protected var currentActivity: Activity? = null
        private set

    private var isShowingAd = false
    private var isAppResumeEnabled = true
    private var dialogFullScreen: Dialog? = null
    private val disabledActivities = mutableSetOf<Class<*>>()

    private val lifecycleEventObserver by lazy {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && isAppResumeEnabled) {
                onStartEvent()
            }
        }
    }

    private fun onStartEvent() {
        currentActivity?.let { showAdIfAvailable(it) }
    }

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(lifecycleEventObserver)
    }

    fun detach() {
        ProcessLifecycleOwner.get()
            .lifecycle
            .removeObserver(lifecycleEventObserver)
        (context as Application).unregisterActivityLifecycleCallbacks(this)
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

    fun isShowingAd(): Boolean {
        return isShowingAd
    }

    protected fun validAndLoadAd() {
        if (isLoadingAd || isAdAvailable() || !AdmobLib.getShowAds() || AdmobLib.getCheckTestDevice()) {
            return
        }

        loadAd()
    }

    protected abstract fun loadAd()

    protected abstract fun isAdAvailable(): Boolean

    private fun showAdIfAvailable(activity: Activity) {
        if (
            !AdmobLib.getShowAds()
            || AdmobLib.getShowInterAds()
            || AdmobLib.getShowRewardAds()
            || ApplovinLib.getShowInterAds()
            || ApplovinLib.getShowRewardAds()
            || disabledActivities.contains(activity.javaClass)
            || AdmobLib.getCheckTestDevice()
        ) return

        if (!isAdAvailable()) {
            loadAd()
            return
        }

        if (isShowingAd) return
        isShowingAd = true

        showAd(
            activity,
            onShowed = { isShowingAd = true },
            onCloseOrFail = { isShowingAd = false }
        )
    }

    protected abstract fun showAd(activity: Activity, onShowed: () -> Unit, onCloseOrFail: () -> Unit)

    protected fun showDialogFullScreen(activity: Activity) {
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
                isShowingAd = true
                dialogFullScreen?.show()
            }
        } catch (e: Exception) {
            e.message
        }
    }

    protected fun dismissDialogFullScreen() {
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
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}