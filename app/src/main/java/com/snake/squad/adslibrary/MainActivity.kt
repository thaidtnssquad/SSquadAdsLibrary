package com.snake.squad.adslibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.rates.RatingDialog
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLoadAndShowInter.setOnClickListener {
            AdmobLib.loadAndShowInterstitial(
                this,
                AdsConstants.admobInterModelTest,
                onAdsCloseOrFailed = {
                    startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                })
        }

        binding.btnLoadInter.setOnClickListener {
            AdmobLib.loadInterstitial(
                this,
                AdsConstants.admobInterModelTest
            )
        }

        binding.btnShowInter.setOnClickListener {
            AdmobLib.showInterstitial(
                this,
                AdsConstants.admobInterModelTest,
                onAdsCloseOrFailed = {
                    startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                })
        }

        binding.btnInterWithNativeAfter.setOnClickListener {
            AdmobLib.loadAndShowInterWithNativeAfter(
                this,
                AdsConstants.admobInterModelTest,
                AdsConstants.admobNativeModelTest,
                null,
            ) {
                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
            }
        }

        binding.btnLoadAndShowNative.setOnClickListener {
            setupViewBannerOrNative(false)
            AdmobLib.loadAndShowNativeCollapsible(
                this,
                AdsConstants.admobNativeModelTest,
                viewGroupExpanded = binding.frNativeExpanded,
                viewGroupCollapsed = binding.frNative,
            )
        }

        binding.btnLoadNative.setOnClickListener {
            setupViewBannerOrNative(false)
            AdmobLib.loadNative(this, AdsConstants.admobNativeModelTest)
        }

        binding.btnShowNative.setOnClickListener {
            setupViewBannerOrNative(false)
            AdmobLib.showNative(
                this,
                AdsConstants.admobNativeModelTest,
                binding.frNative,
                GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON
            )
        }

        binding.btnLoadAndShowBanner.setOnClickListener {
            setupViewBannerOrNative(true)
            AdmobLib.loadAndShowBanner(
                this,
                AdsConstants.ADMOB_BANNER_TEST,
                binding.frBanner,
                binding.viewLine
            )
        }

        binding.btnLoadAndShowBannerCollapse.setOnClickListener {
            setupViewBannerOrNative(true)
            AdmobLib.loadAndShowBannerCollapsible(
                this,
                AdsConstants.admobBannerCollapsibleModel,
                binding.frBanner,
                binding.viewLine
            )
        }

        binding.btnLoadAndShowRewarded.setOnClickListener {
            AdmobLib.loadAndShowRewarded(
                this,
                AdsConstants.admobRewardedModelTest,
                onAdsCloseOrFailed = {
                    Toast.makeText(this, if (it) "Earned!" else "Not Earned!", Toast.LENGTH_SHORT)
                        .show()
                })
        }

        binding.btnLoadRewarded.setOnClickListener {
            AdmobLib.loadRewarded(this, AdsConstants.admobRewardedModelTest)
        }

        binding.btnShowRewarded.setOnClickListener {
            AdmobLib.showRewarded(this, AdsConstants.admobRewardedModelTest, onAdsCloseOrFailed = {
                Toast.makeText(this, if (it) "Earned!" else "Not Earned!", Toast.LENGTH_SHORT)
                    .show()
            })
        }

        binding.btnShowRating.setOnClickListener {
            RatingDialog.showRateAppDialog(
                this,
                supportFragmentManager,
                "john.tyler@examplepetstore.com"
            )
        }

    }

    private fun setupViewBannerOrNative(isBanner: Boolean) {
        if (isBanner) {
            binding.frBanner.visibility = View.VISIBLE
            binding.viewLine.visibility = View.VISIBLE
            binding.frNative.visibility = View.GONE
        } else {
            binding.frBanner.visibility = View.GONE
            binding.viewLine.visibility = View.GONE
            binding.frNative.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        AppOnResumeAdsManager.getInstance().enableForActivity(MainActivity::class.java)
    }

}