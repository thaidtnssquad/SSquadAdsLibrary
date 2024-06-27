package com.snake.squad.adslibrary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.ApplovinLib
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.aoa.AppOpenAdsManager
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslibrary.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAds()

    }

    private fun initAds() {
        AdmobLib.initialize(this)
        AppOnResumeAdsManager.initialize(application, AdsConstants.APP_OPEN_TEST)
        AppOnResumeAdsManager.getInstance().disableForActivity(SplashActivity::class.java)
        ApplovinLib.initialize(
            application,
            "05TMDQ5tZabpXQ45_UTbmEGNUtVAzSTzT6KmWQc5_CuWdzccS4DCITZoL3yIWUG3bbq60QC_d4WF28tUC4gVTF",
            isDebug = true,
            isShowAds = true
        ) {
            loadAndShowAOA()
        }
    }

    private fun loadAndShowAOA() {
        AppOpenAdsManager(
            this,
            AdsConstants.APP_OPEN_TEST,
            20000
        ) {
            replaceActivity()
        }.loadAndShowAoA()
    }

    private fun replaceActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}