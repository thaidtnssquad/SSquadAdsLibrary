package com.snake.squad.adslibrary

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.ApplovinLib
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.rates.RatingDialog
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLoadAndShowInter.setOnClickListener {
            ApplovinLib.loadAndShowInterstitial(
                this,
                AdsConstants.maxInterModelTest,
                10000
            ) {

            }
        }

        binding.btnLoadInter.setOnClickListener {
            ApplovinLib.loadInterstitial(
                this,
                AdsConstants.maxInterModelTest
            )
        }

        binding.btnShowInter.setOnClickListener {
            ApplovinLib.showInterstitial(
                this,
                AdsConstants.maxInterModelTest
            ) {

            }
        }

        binding.btnLoadAndShowNative.setOnClickListener {
            setupViewBannerOrNative(false)
            ApplovinLib.loadAndShowNative(
                this,
                AdsConstants.maxNativeModelTest,
                binding.frNative
            )
        }

        binding.btnLoadNative.setOnClickListener {
            setupViewBannerOrNative(false)
            ApplovinLib.loadNative(this, AdsConstants.maxNativeModelTest)
        }

        binding.btnShowNative.setOnClickListener {
            setupViewBannerOrNative(false)
            ApplovinLib.showNative(this, AdsConstants.maxNativeModelTest, binding.frNative, false)
        }

        binding.btnLoadAndShowBanner.setOnClickListener {
            setupViewBannerOrNative(true)
            ApplovinLib.loadAndShowBanner(
                this,
                AdsConstants.MAX_BANNER_TEST,
                binding.frBanner,
                binding.viewLine
            )
        }

        binding.btnLoadAndShowBannerCollapse.setOnClickListener {
            setupViewBannerOrNative(true)
            AdmobLib.loadAndShowBannerCollapsible(
                this,
                AdsConstants.ADMOB_BANNER_COLLAPSE_TEST,
                binding.frBanner,
                binding.viewLine
            )
        }

        binding.btnLoadAndShowRewarded.setOnClickListener {
            ApplovinLib.loadAndShowRewarded(this, AdsConstants.maxRewardedModelTest, 10000) {
                Toast.makeText(this, if (it) "Earned" else "Not Earned", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoadRewarded.setOnClickListener {
            ApplovinLib.loadRewarded(this, AdsConstants.maxRewardedModelTest)
        }

        binding.btnShowRewarded.setOnClickListener {
            ApplovinLib.showRewarded(this, AdsConstants.maxRewardedModelTest) {
                Toast.makeText(this, if (it) "Earned" else "Not Earned", Toast.LENGTH_SHORT).show()
            }
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