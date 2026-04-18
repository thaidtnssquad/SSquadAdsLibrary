package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.google.ads.noninterruptive.pictureinpicturead.PictureInPictureAd

data class AdmobPictureInPictureModel(val adsID: String) {
    var pipAd: MutableLiveData<PictureInPictureAd?> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
}
