package com.snake.squad.adslib.tenjin

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.snake.squad.adslib.utils.AdType
import com.tenjin.android.TenjinSDK
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.TTAdRevenueEvent
import org.json.JSONException
import org.json.JSONObject


object TenjinUtils {

    var mTenjinKey: MutableLiveData<String?> = MutableLiveData()

    fun initTenjin(context: Context, tenjinKey: String) {
        mTenjinKey.value = tenjinKey
        val instance = TenjinSDK.getInstance(context, tenjinKey)
        instance.connect()
    }

    fun postRevenueTenjin(
        context: Context,
        adValue: AdValue,
        adType: AdType,
        adUnitId: String,
        interAd: InterstitialAd? = null,
        nativeAd: NativeAd? = null,
        rewardAd: RewardedAd? = null,
        appOpenAd: AppOpenAd? = null,
        adView: AdView? = null
    ) {

        val responseInfo: ResponseInfo? = when(adType) {
            AdType.INTERSTITIAL -> interAd?.responseInfo
            AdType.NATIVE -> nativeAd?.responseInfo
            AdType.REWARDED -> rewardAd?.responseInfo
            AdType.APP_OPEN -> appOpenAd?.responseInfo
            AdType.BANNER -> adView?.responseInfo
        }

        val json = JSONObject().apply {
            put("value_micros", adValue.valueMicros)
            put("currency_code", adValue.currencyCode)
            put("precision_type", adValue.precisionType)
            put("ad_unit_id", adUnitId)

            responseInfo?.let { info ->
                put("response_id", info.responseId)
                info.loadedAdapterResponseInfo?.let { adapter ->
                    put("mediation_adapter_class_name", adapter.adapterClassName)
                }
            }
        }

        mTenjinKey.value?.let {
            val instance = TenjinSDK.getInstance(context, it)
            instance.eventAdImpressionAdMob(json)
        }

    }

}