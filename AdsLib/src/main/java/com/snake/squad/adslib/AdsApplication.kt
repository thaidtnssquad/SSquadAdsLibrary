package com.snake.squad.adslib

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.snake.squad.adslib.solar.SolarUtils
import com.snake.squad.adslib.tiktok.TiktokUtils
import com.snake.squad.adslib.utils.SharedPrefManager

open class AdsApplication(
    private val appPackageName: String? = null,
    private val solarKey: String? = null,
    private val isEnabledSolar: Boolean? = true,
    private val accessTokenTiktok: String? = null,
    private val tiktokKey: String? = null,
    private val isEnabledTiktok: Boolean? = true
) : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        SharedPrefManager.init(this)

        isEnabledSolar?.let { isEnabled ->
            if (isEnabled && solarKey != null) {
                SolarUtils.initSolarEngine(this, solarKey)
            }
        }

        isEnabledTiktok?.let { isEnabled ->
            if (isEnabled && accessTokenTiktok != null && tiktokKey != null && appPackageName != null) {
                TiktokUtils.initTiktokSDK(this, accessTokenTiktok, appPackageName, tiktokKey)
            }
        }
    }



}