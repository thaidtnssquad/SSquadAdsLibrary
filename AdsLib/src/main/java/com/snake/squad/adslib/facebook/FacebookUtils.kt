package com.snake.squad.adslib.facebook

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsConstants.EVENT_NAME_AD_IMPRESSION
import com.facebook.appevents.AppEventsConstants.EVENT_PARAM_CURRENCY
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue


object FacebookUtils {

    fun adImpressionFacebookRevenue(context: Context, adValue: AdValue) {
        val logger = AppEventsLogger.newLogger(context)
        val params = Bundle()
        params.putString(EVENT_PARAM_CURRENCY, "USD")
        logger.logEvent(EVENT_NAME_AD_IMPRESSION, adValue.valueMicros / 1000000.0, params)
    }

}