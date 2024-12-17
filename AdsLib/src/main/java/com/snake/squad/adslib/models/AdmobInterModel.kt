package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.interstitial.InterstitialAd

data class AdmobInterModel(val adsID: String) {
    var interstitialAd: MutableLiveData<InterstitialAd?> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
}
