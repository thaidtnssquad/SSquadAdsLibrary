package com.snake.squad.adslib.aoa

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.IntDef
import com.google.android.gms.ads.AdRequest
import com.snake.squad.adslib.aoa.onresume.BaseOnResumeManager
import com.snake.squad.adslib.aoa.onresume.OnResumeManager
import com.snake.squad.adslib.aoa.onresume.OnResumeWithInterManager

object AppOnResumeAdsManager {

    @IntDef(AOA, INTER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    const val AOA = 0
    const val INTER = 1

    @SuppressLint("StaticFieldLeak")
    private var instance: BaseOnResumeManager? = null

    fun initialize(
        application: Application,
        adsId: String,
        adRequest: AdRequest? = null,
        timeout: Long = 10000,
        @Type type: Int
    ) {
        val old = instance
        old?.detach()
        instance = null

        instance = when(type) {
            AOA -> OnResumeManager(adsId, adRequest, timeout, application)
            INTER -> OnResumeWithInterManager(adsId, adRequest, timeout, application)
            else -> null
        }
    }

    fun disableForActivity(activityClass: Class<*>) {
        instance?.disableForActivity(activityClass)
    }

    fun enableForActivity(activityClass: Class<*>) {
        instance?.enableForActivity(activityClass)
    }

    fun setAppResumeEnabled(enabled: Boolean) {
        instance?.setAppResumeEnabled(enabled)
    }

    fun isShowingAd(): Boolean {
        return instance?.isShowingAd() ?: false
    }
}