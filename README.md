# S-SQUAD Ads Library



## Implementation
In settings.gradle
```sh
    maven (url = "https://jitpack.io")
    maven (url = "https://maven.google.com")
    maven (url = "https://android-sdk.is.com/")
```
In build.gradle :app
```sh
    implementation 'com.github.thaidtnssquad:SSquadAdsLibrary:{new_version}'
```

## Init
### Create MyApplication Class and extend AdsApplication
```sh
    class MyApplication: AdsApplication("ADJUST_SDK_KEY", true)
```
### In AndroidManifest.xml
#### Permission:
```sh
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```
#### In <application/> card:
```sh
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="@string/app_id"/>
```
```sh
    <meta-data
            android:name="com.facebook.sdk.ApplicationId"
            android:value="${facebook_id}" />
```
```sh
    <meta-data
            android:name="com.facebook.sdk.ClientToken"
            android:value="${facebook_client_token}" />
```
### In Splash Screen:
#### ADMOB
```sh
    AdmobLib.setEnabledCheckTestDevice(true)
    AdmobLib.initialize(application, isDebug = true, isShowAds = true, onInitializedAds = {
          if(it) {
                //AppOnResumeInitialized
                AppOnResumeAdsManager.initialize(application, "ADS_ID")
                AppOnResumeAdsManager.getInstance().disableForActivity(SplashActivity::class.java)
                //Next Step Logic Ads
          } else {
            //NextActivity
          }
    })
```
#### MAX APPLOVIN
```sh
    ApplovinLib.initialize(application, "ADS_ID", isDebug = true, isShowAds = true) {
        //Initialized
    }
```

## App Open Ads
```sh
    AppOpenAdsManager(activity, "ADS_ID", 20000) {
        //Logic
    }.loadAndShowAoA()
```


## Interstitials Ads

### ADMOB
#### Load And Show Interstitials Ads
```sh
    val admobInterModel = AdmobInterModel("ADS_ID")
    AdmobLib.loadAndShowInterstitial(activity, admobInterModel) {
        //Logic
    }
```
#### Load Interstitials Ads
```sh
    val admobInterModel = AdmobInterModel("ADS_ID")
    AdmobLib.loadInterstitial(activity, admobInterModel)
```
#### Show Interstitials Ads
```sh
    val admobInterModel = AdmobInterModel("ADS_ID")
    AdmobLib.showInterstitial(activity, admobInterModel) {
      //Logic
    }
```

### MAX APPLOVIN
#### Load And Show Interstitials Ads
```sh
    val maxInterModel = MaxInterModel("ADS_ID")
    ApplovinLib.loadAndShowInterstitial(activity, maxInterModel) {
        //Logic
    }
```
#### Load Interstitials Ads
```sh
    val maxInterModel = MaxInterModel("ADS_ID")
    ApplovinLib.loadInterstitial(activity, maxInterModel)
```
#### Show Interstitials Ads
```sh
    val maxInterModel = MaxInterModel("ADS_ID")
    ApplovinLib.showInterstitial(activity, maxInterModel) {
      //Logic
    }
```


## Banner Ads

In bottom of activity.xml:
```sh
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/black"
        android:id="@+id/viewLine"
        app:layout_constraintBottom_toTopOf="@+id/frBanner"/>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        android:id="@+id/frBanner"/>
```

### ADMOB
#### Load And Show Banner
```sh
    AdmobLib.loadAndShowBanner(
                activity,
                "BANNER_ID",
                binding.frBanner,
                binding.viewLine
            )
```
#### Load And Show Banner Collapsible
```sh
    val admobBannerCollapsibleModel = AdmobBannerCollapsibleModel("BANNER_COLLAPSIBLE_ID")
    AdmobLib.loadAndShowBannerCollapsible(
                activity,
                admobBannerCollapsibleModel,
                binding.frBanner,
                binding.viewLine
            )
```
### MAX APPLOVIN
#### Load And Show Banner
```sh
    Applovin.loadAndShowBanner(
                activity,
                "BANNER_ID",
                binding.frBanner,
                binding.viewLine
            )
```

## Native Ads

In activity.xml:
```sh
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        android:id="@+id/frNative"/>
```

### ADMOB
#### Load And Show Native Ads
```sh
    val admobNativeModel = AdmobNativeModel("ADS_ID")
    AdmobLib.loadAndShowNative(
                activity,
                admobNativeModel,
                binding.frNative
            )
```
#### Load Native Ads
```sh
    val admobNativeModel = AdmobNativeModel("ADS_ID")
    AdmobLib.loadNative(activity, admobNativeModel)
```
#### Show Native Ads
```sh
    val admobNativeModel = AdmobNativeModel("ADS_ID")
    AdmobLib.showNative(activity, admobNativeModel, binding.frNative, isMedium = false)
```

### MAX APPLOVIN
#### Load And Show Native Ads
```sh
    val maxNativeModel = MaxNativeModel("ADS_ID")
    Applovin.loadAndShowNative(
                activity,
                maxNativeModel,
                binding.frNative
            )
```
#### Load Native Ads
```sh
    val maxNativeModel = MaxNativeModel("ADS_ID")
    Applovin.loadNative(activity, maxNativeModel)
```
#### Show Native Ads
```sh
    val maxNativeModel = MaxNativeModel("ADS_ID")
    Applovin.showNative(activity, maxNativeModel, binding.frNative, isMedium = false)
```


## Rewarded Ads

### ADMOB
#### Load And Show Rewarded Ads
```sh
    val admobRewardedModel = AdmobRewardedModel("ADS_ID")
    AdmobLib.loadAndShowRewarded(activity, admobRewardedModel, 10000) {
        //Logic
    }
```
#### Load Rewarded Ads
```sh
    val admobRewardedModel = AdmobRewardedModel("ADS_ID")
    AdmobLib.loadRewarded(activity, admobRewardedModel)
```
#### Show Rewarded Ads
```sh
    val admobRewardedModel = AdmobRewardedModel("ADS_ID")
    AdmobLib.showRewarded(activity, admobRewardedModel) {
        //Logic
    }
```

### MAX APPLOVIN
#### Load And Show Rewarded Ads
```sh
    val maxRewardedModel = ApplovinRewardedModel("ADS_ID")
    ApplovinLib.loadAndShowRewarded(activity, maxRewardedModel, 10000) {
        //Logic
    }
```
#### Load Rewarded Ads
```sh
    val maxRewardedModel = ApplovinRewardedModel("ADS_ID")
    ApplovinLib.loadRewarded(activity, maxRewardedModel)
```
#### Show Rewarded Ads
```sh
    val maxRewardedModel = ApplovinRewardedModel("ADS_ID")
    ApplovinLib.showRewarded(activity, maxRewardedModel) {
        //Logic
    }
```


