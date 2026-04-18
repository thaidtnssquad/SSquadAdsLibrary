package com.snake.squad.adslib.utils

import com.google.ads.noninterruptive.squeezebackad.SqueezeBackAdEventCallback
import com.google.android.gms.ads.AdValue

interface EventCallback: SqueezeBackAdEventCallback {
    override fun onAdShown() {

    }

    override fun onAdImpression() {

    }

    override fun onAdClicked() {

    }

    override fun onAdHidden() {

    }

    override fun onAdDestroyed() {

    }

    override fun onAdPaid(adValue: AdValue?) {

    }
}