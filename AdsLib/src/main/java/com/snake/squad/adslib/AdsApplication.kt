package com.snake.squad.adslib

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.snake.squad.adslib.utils.SharedPrefManager

open class AdsApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        SharedPrefManager.init(this)
    }



}