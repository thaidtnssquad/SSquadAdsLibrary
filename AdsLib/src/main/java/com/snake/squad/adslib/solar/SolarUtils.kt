package com.snake.squad.adslib.solar

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.reyun.solar.engine.SolarEngineConfig
import com.reyun.solar.engine.SolarEngineManager
import com.reyun.solar.engine.infos.SEAdImpEventModel
import com.snake.squad.adslib.utils.AdType


object SolarUtils {

    fun initSolarEngine(context: Context, solarKey: String) {
        SolarEngineManager.getInstance().preInit(context, solarKey)
        val config = SolarEngineConfig.Builder().build()
        SolarEngineManager.getInstance()
            .initialize(context, solarKey, config) { code ->
                if (code == 0) {
                    Log.d("TAG=====", "SolarEngine Initialize Success!")
                } else {
                    Log.d("TAG=====", "SolarEngine Initialize Failure!")
                }
            }
    }

    fun postRevenueSolar(
        adValue: AdValue,
        adType: AdType,
        adUnitId: String,
        interAd: InterstitialAd? = null,
        nativeAd: NativeAd? = null,
        rewardAd: RewardedAd? = null,
        appOpenAd: AppOpenAd? = null,
        adView: AdView? = null
    ) {
        val valueMicros: Double = adValue.valueMicros.toDouble()
        val currencyCode: String? = adValue.currencyCode

        val loadedAdapterResponseInfo: AdapterResponseInfo? = when(adType) {
            AdType.INTERSTITIAL -> interAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.NATIVE -> nativeAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.REWARDED -> rewardAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.APP_OPEN -> appOpenAd?.responseInfo?.loadedAdapterResponseInfo
            AdType.BANNER -> adView?.responseInfo?.loadedAdapterResponseInfo
        }
        val adSourceName = loadedAdapterResponseInfo?.adSourceName
        val adSourceId = loadedAdapterResponseInfo?.adSourceId

        //SE SDK processing logic
        val seAdImpEventModel = SEAdImpEventModel()

        //Monetization Platform Name
        seAdImpEventModel.setAdNetworkPlatform(adSourceName)

        //Mediation Platform Name (e.g. admob SDK as "admob")
        seAdImpEventModel.setMediationPlatform("admob")

        //Displayed Ad Type (Taking Rewarded Ad as an example, adType = 1)
        seAdImpEventModel.setAdType(1)

        //Monetization Platform App ID
        seAdImpEventModel.setAdNetworkAppID(adSourceId)

        //Monetization Platform Ad Unit ID
        seAdImpEventModel.setAdNetworkADID(adUnitId)

        //Ad eCPM
        seAdImpEventModel.setEcpm(valueMicros / 1000)

        //Monetization Platform Currency Type
        seAdImpEventModel.setCurrencyType(currencyCode)

        //True: rendered success
        seAdImpEventModel.setRenderSuccess(true)

        //You can add custom properties as needed. Here we do not give examples.
        SolarEngineManager.getInstance().trackAdImpression(seAdImpEventModel)
    }


}