package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.nativead.NativeAd

data class AdmobNativeModel(val adsID: String) {
    var nativeAd: MutableLiveData<NativeAd?> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
}
