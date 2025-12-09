package com.snake.squad.adslib.tiktok

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.snake.squad.adslib.utils.AdType
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.TTAdRevenueEvent
import org.json.JSONException
import org.json.JSONObject


object TiktokUtils {

    fun initTiktokSDK(context: Context, accessToken: String, packageName: String, tiktokKey: String) {
        val ttConfig = TikTokBusinessSdk.TTConfig(context, accessToken)
            .setAppId(packageName)
            .setTTAppId(tiktokKey)
        TikTokBusinessSdk.initializeSdk(ttConfig, object: TikTokBusinessSdk.TTInitCallback {
            override fun success() {
                Log.d("TAG=====", "TiktokSDK Initialize Success!")
            }

            override fun fail(code: Int, msg: String?) {
                Log.d("TAG=====", "TiktokSDK Initialize Success!")
            }

        })
        TikTokBusinessSdk.startTrack()
    }

    fun postRevenueTiktok(
        adValue: AdValue,
        adType: AdType,
        adUnitId: String,
        interAd: InterstitialAd? = null,
        nativeAd: NativeAd? = null,
        rewardAd: RewardedAd? = null,
        appOpenAd: AppOpenAd? = null,
        adView: AdView? = null
    ) {

        val loadedAdapterResponseInfo: AdapterResponseInfo? = when(adType) {
            AdType.INTERSTITIAL -> interAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.NATIVE -> nativeAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.REWARDED -> rewardAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.APP_OPEN -> appOpenAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.BANNER -> adView?.responseInfo?.loadedAdapterResponseInfo
        }

        val responseExtras: Bundle? = when(adType) {
            AdType.INTERSTITIAL -> interAd?.responseInfo?.responseExtras
            AdType.NATIVE -> nativeAd?.responseInfo?.responseExtras
            AdType.REWARDED -> rewardAd?.responseInfo?.responseExtras
            AdType.APP_OPEN -> appOpenAd?.responseInfo?.responseExtras
            AdType.BANNER -> adView?.responseInfo?.responseExtras
        }
        val adRevenueJson = JSONObject()

        val value = adValue.valueMicros
        val currencyCode: String? = adValue.currencyCode
        val precisionType = adValue.precisionType
        var adSourceName = ""
        var adSourceId = ""
        var adSourceInstanceName = ""
        var adSourceInstanceId = ""
        var mediationGroupName: String? = ""
        var mediationAbTestName: String? = ""
        var mediationAbTestVariant: String? = ""
        loadedAdapterResponseInfo?.let {
            adSourceName = it.adSourceName
            adSourceId = it.adSourceId
            adSourceInstanceName = it.adSourceInstanceName
            adSourceInstanceId = it.adSourceInstanceId
        }
        responseExtras?.let {
            mediationGroupName = it.getString("mediation_group_name")
            mediationAbTestName = it.getString("mediation_ab_test_name")
            mediationAbTestVariant = it.getString("mediation_ab_test_variant")
        }
        try {
            adRevenueJson.put("value", value)
            adRevenueJson.put("currency_code", currencyCode)
            adRevenueJson.put("precision", precisionType)
            adRevenueJson.put("ad_unit_id", adUnitId)
            adRevenueJson.put("ad_source_name", adSourceName)
            adRevenueJson.put("ad_source_id", adSourceId)
            adRevenueJson.put("ad_source_instance_name", adSourceInstanceName)
            adRevenueJson.put("ad_source_instance_id", adSourceInstanceId)
            adRevenueJson.put("mediation_group_name", mediationGroupName)
            adRevenueJson.put("mediation_ab_test_name", mediationAbTestName)
            adRevenueJson.put("mediation_ab_test_variant", mediationAbTestVariant)
            adRevenueJson.put("device_ad_mediation_platform", "admob_sdk")

            //banner/interstitial/rewarded/rewarded interstitial/native/splash
            adRevenueJson.put("ad_format", when(adType) {
                AdType.INTERSTITIAL -> "interstitial"
                AdType.NATIVE -> "native"
                AdType.REWARDED -> "rewarded"
                AdType.APP_OPEN -> "splash"
                AdType.BANNER -> "banner"
            })
        } catch (e: JSONException) {
            e.message
        }

        // Make sure the App Events SDK has been initialized before calling this
        val adRevenueInfo = TTAdRevenueEvent.newBuilder(adRevenueJson).build()
        TikTokBusinessSdk.trackTTEvent(adRevenueInfo)

    }

}