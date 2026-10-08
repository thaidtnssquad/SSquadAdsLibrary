package com.snake.squad.adslib.models.nativee.sequence

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.R
import com.snake.squad.adslib.databinding.DialogNativeSequenceBinding
import com.snake.squad.adslib.utils.GoogleENative
import kotlin.math.roundToInt

class NativeSequenceDialog(
    private val mActivity: AppCompatActivity,
    private var mAds: NativeSequenceAds? = null,
    private var waitingInter: Boolean = false,
    private var waitingNativeDuration: Long,
    private val mLayout: Int = R.layout.admob_ad_template_full_screen,
    private val onDismissed: () -> Unit = {}
): Dialog(mActivity, R.style.mTheme_Dialog) {

    private val binding = DialogNativeSequenceBinding.inflate(layoutInflater)

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setFullScreenVisibility()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        initView()
        initActionView()

        setOnDismissListener { onDismissed() }
    }

    override fun show() {
        super.show()
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300L)
            .start()
    }

    private fun initView() {
        if (!waitingInter) showNative00()
    }

    private fun initActionView() {
        binding.btnNext.setOnClickListener { nextOrDone() }
    }

    private fun showNative00() {
        val ads = mAds
        if (ads == null) {
            startCountdown(0)
            return
        }

        binding.btnNext.visibility = View.INVISIBLE
        binding.tvCountdown.visibility = View.INVISIBLE

        binding.lNative00.translationX = binding.lNative00.width.toFloat()
        binding.lNative00.alpha = 0f
        binding.lNative00.visibility = View.VISIBLE
        binding.lNative00.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(200L)
            .start()

        binding.lNative01.visibility = View.INVISIBLE
        AdmobLib.showNative(
            activity = mActivity,
            admobNativeModel = ads.native00,
            viewGroup = binding.lNative00,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            layout = mLayout,
            onAdsShowed = { startCountdown(waitingNativeDuration * 1_000) },
            onAdsShowFail = { startCountdown(waitingNativeDuration * 1_000) }
        )
    }

    private fun showNative01() {
        val ads = mAds
        if (ads == null) {
            startCountdown(0)
            return
        }

        binding.btnNext.visibility = View.INVISIBLE
        binding.tvCountdown.visibility = View.INVISIBLE

        binding.lNative00.visibility = View.INVISIBLE

        binding.lNative01.translationX = binding.lNative01.width.toFloat()
        binding.lNative01.alpha = 0f
        binding.lNative01.visibility = View.VISIBLE
        binding.lNative01.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(200L)
            .start()

        AdmobLib.showNative(
            activity = mActivity,
            admobNativeModel = ads.native01,
            viewGroup = binding.lNative01,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            layout = mLayout,
            onAdsShowed = { startCountdown(waitingNativeDuration * 1_000) },
            onAdsShowFail = { startCountdown(waitingNativeDuration * 1_000) }
        )
    }

    // region Countdown
    private var mCountdown: CountDownTimer? = null

    private fun startCountdown(duration: Long) {
        if (duration <= 0) {
            doneCountdown()
            return
        }

        binding.btnNext.visibility = View.INVISIBLE
        binding.tvCountdown.visibility = View.VISIBLE

        mCountdown?.cancel()
        mCountdown = object : CountDownTimer(duration, 50) {
            override fun onFinish() {
                doneCountdown()
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${millisUntilFinished / 1000 + 1}"
                val p = (((duration - millisUntilFinished) / duration.toFloat()) * 100).roundToInt()
                if (index == 0) {
                    if (p > binding.pbNative00.progress) binding.pbNative00.progress = p
                } else {
                    if (p > binding.pbNative01.progress) binding.pbNative01.progress = p
                }
            }
        }
        mCountdown?.start()
    }

    private fun doneCountdown() {
        binding.tvCountdown.visibility = View.INVISIBLE
        if (index == 0) {
            binding.btnNext.setImageResource(R.drawable.ic_next_sequence)
            binding.pbNative00.progress = 100
        } else {
            binding.btnNext.setImageResource(R.drawable.ic_close)
            binding.pbNative01.progress = 100
        }
        binding.btnNext.visibility = View.VISIBLE
    }
    // endregion

    private fun nextOrDone() {
        if (index == 0) {
            index = 1
            showNative01()
            return
        }

        dismiss()
    }

    fun canShow(can: Boolean) {
        if (!waitingInter) return

        if (!can) return

        waitingInter = false
        showNative00()
    }

}

@Suppress("DEPRECATION")
private fun Window.setFullScreenVisibility() {
    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
}