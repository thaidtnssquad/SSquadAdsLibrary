package com.snake.squad.adslib

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.adjust.sdk.Adjust
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.utils.SharedPrefManager

open class AdsApplication(private val adjustKey: String, private val isProduction: Boolean) : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        SharedPrefManager.init(this)
        AdjustUtils.initAdjust(this,adjustKey, isProduction)
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
    }

    private class AdjustLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }
        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}