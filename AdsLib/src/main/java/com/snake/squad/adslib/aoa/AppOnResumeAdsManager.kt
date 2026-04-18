package com.snake.squad.adslib.aoa

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.IntDef
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
        @Type type: Int
    ) {
        val old = instance
        old?.detach()
        instance = null

        instance = when(type) {
            AOA -> OnResumeManager(adsId, application)
            INTER -> OnResumeWithInterManager(adsId, application)
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