package com.snake.squad.adslib.utils

import com.google.ads.noninterruptive.squeezebackad.SqueezeBackAd
import com.google.ads.noninterruptive.squeezebackad.SqueezeBackAdLoadCallback
import com.google.android.gms.ads.LoadAdError

interface LoadCallback: SqueezeBackAdLoadCallback {
    override fun onAdFailedToLoad(loadAdError: LoadAdError) {

    }

    override fun onAdLoaded(squeezeBackAd: SqueezeBackAd) {

    }
}