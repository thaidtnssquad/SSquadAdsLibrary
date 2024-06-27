package com.snake.squad.adslib.models

import com.applovin.mediation.ads.MaxInterstitialAd

data class MaxInterModel(val adsID: String) {
    var interstitialAd: MaxInterstitialAd? = null
}
