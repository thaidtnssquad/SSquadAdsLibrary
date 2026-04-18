package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.google.ads.noninterruptive.squeezebackad.SqueezeBackAd

data class AdmobSqueezeBackModel(val adsID: String) {
    var squeezeBackAd: MutableLiveData<SqueezeBackAd?> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
}
