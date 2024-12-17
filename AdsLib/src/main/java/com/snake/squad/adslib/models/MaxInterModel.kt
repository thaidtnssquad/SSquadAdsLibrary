package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.applovin.mediation.ads.MaxInterstitialAd

data class MaxInterModel(val adsID: String) {
    var interstitialAd: MutableLiveData<MaxInterstitialAd?> = MutableLiveData()
}
