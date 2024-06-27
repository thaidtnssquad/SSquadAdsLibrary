package com.snake.squad.adslib.models

import com.google.android.gms.ads.nativead.NativeAd

data class AdmobNativeModel(val adsID: String) {
    var nativeAd: NativeAd? = null
}
