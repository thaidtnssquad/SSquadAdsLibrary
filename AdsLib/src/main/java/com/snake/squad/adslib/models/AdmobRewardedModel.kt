package com.snake.squad.adslib.models

import com.google.android.gms.ads.rewarded.RewardedAd

data class AdmobRewardedModel(val adsID: String) {
    var rewardAd: RewardedAd? = null
}
