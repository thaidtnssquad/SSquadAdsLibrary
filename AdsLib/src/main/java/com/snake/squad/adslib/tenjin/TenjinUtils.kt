package com.snake.squad.adslib.tenjin

import android.content.Context
import com.tenjin.android.TenjinSDK



object TenjinUtils {

    fun setAppStore(context: Context, tenjinKey: String) {
        val instance = TenjinSDK.getInstance(context, tenjinKey)
        instance.setAppStore(TenjinSDK.AppStoreType.googleplay)
    }

    fun initTenjin(context: Context, tenjinKey: String) {
        val instance = TenjinSDK.getInstance(context, tenjinKey)
        instance.connect()
    }

}