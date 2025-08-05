package com.snake.squad.adslib

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.adjust.sdk.Adjust
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.snake.squad.adslib.adjust.AdjustUtils
import com.snake.squad.adslib.solar.SolarUtils
import com.snake.squad.adslib.utils.SharedPrefManager

open class AdsApplication(
    private val adjustKey: String? = null,
    private val isProduction: Boolean? = true,
    private val isEnabledAdjust: Boolean? = true,
    private val solarKey: String? = null,
    private val isEnabledSolar: Boolean? = true,
) : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        SharedPrefManager.init(this)
        isEnabledAdjust?.let { isEnabled ->
            if (isEnabled && adjustKey != null) {
                AdjustUtils.initAdjust(this, adjustKey, isProduction ?: false)
                registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
            }
        }

        isEnabledSolar?.let { isEnabled ->
            if (isEnabled && solarKey != null) {
                SolarUtils.initSolarEngine(this, solarKey)
            }
        }
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