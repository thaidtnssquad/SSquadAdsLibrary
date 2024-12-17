package com.snake.squad.adslibrary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.AdsConstants
import com.snake.squad.adslib.utils.GoogleENative
import com.snake.squad.adslibrary.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {

    private val binding by lazy { ActivityThirdBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AdmobLib.loadAndShowNative(this, AdsConstants.admobNativeModelTest, binding.frNative, GoogleENative.UNIFIED_FULL_SCREEN)

    }
}