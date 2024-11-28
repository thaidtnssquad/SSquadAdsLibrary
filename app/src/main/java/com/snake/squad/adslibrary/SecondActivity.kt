package com.snake.squad.adslibrary

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslibrary.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySecondBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnNextActivity.setOnClickListener {
            startActivity(Intent(this@SecondActivity, ThirdActivity::class.java))
            finish()
        }

        binding.btnLoadAndShowBannerCollapse.setOnClickListener {
            AdmobLib.loadAndShowBannerCollapsible(
                this,
                AdsConstants.admobBannerCollapsibleModel,
                binding.frBanner,
                binding.viewLine
            )
        }

    }

}