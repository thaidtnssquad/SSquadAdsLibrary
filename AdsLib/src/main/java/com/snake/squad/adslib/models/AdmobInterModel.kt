package com.snake.squad.adslib.models

import com.google.android.gms.ads.interstitial.InterstitialAd

data class AdmobInterModel(val adsID: String) {
    var interstitialAd: InterstitialAd? = null
}
