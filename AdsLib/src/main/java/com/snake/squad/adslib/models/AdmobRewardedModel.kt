package com.snake.squad.adslib.models

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.rewarded.RewardedAd

data class AdmobRewardedModel(val adsID: String) {
    var rewardAd: MutableLiveData<RewardedAd?> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
}
