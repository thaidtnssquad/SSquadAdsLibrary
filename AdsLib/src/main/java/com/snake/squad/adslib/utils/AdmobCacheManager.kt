package com.snake.squad.adslib.utils

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import java.util.concurrent.ConcurrentHashMap

object AdmobCacheManager {
    // Interstitial Cache
    val interCache = ConcurrentHashMap<String, InterstitialAd>()
    val interLoading = ConcurrentHashMap<String, Boolean>()

    // Native Cache
    val nativeCache = ConcurrentHashMap<String, NativeAd>()
    val nativeLoading = ConcurrentHashMap<String, Boolean>()

    // Rewarded Cache
    val rewardedCache = ConcurrentHashMap<String, RewardedAd>()
    val rewardedLoading = ConcurrentHashMap<String, Boolean>()

    fun clearInterCache() {
        interCache.clear()
        interLoading.clear()
    }

    fun clearCache() {
        clearInterCache()

        nativeCache.values.forEach { it.destroy() }
        nativeCache.clear()
        nativeLoading.clear()

        rewardedCache.clear()
        rewardedLoading.clear()
    }
}
