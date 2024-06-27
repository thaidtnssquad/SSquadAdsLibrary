package com.snake.squad.adslib.rates

import android.util.Log

object Utils {
    private const val IS_DEBUG = false // Renamed for clarity

    fun logError(e: Throwable?) {
        if (IS_DEBUG) {
            e?.printStackTrace()
        }
    }

    fun logDebug(msg: String) {
        if (IS_DEBUG) {
            println("Debug :: $msg")
        }
    }
}