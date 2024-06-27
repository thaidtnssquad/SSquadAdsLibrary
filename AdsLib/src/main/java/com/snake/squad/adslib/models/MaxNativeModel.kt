package com.snake.squad.adslib.models

import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader

data class MaxNativeModel(val adsID: String) {
    var nativeAdLoader: MaxNativeAdLoader? = null
    var maxAd: MaxAd? = null
}
