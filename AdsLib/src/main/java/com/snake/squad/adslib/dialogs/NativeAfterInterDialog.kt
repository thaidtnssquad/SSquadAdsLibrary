package com.snake.squad.adslib.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.R
import com.snake.squad.adslib.databinding.DialogNativeAfterInterBinding
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.utils.GoogleENative
import java.util.Timer
import java.util.TimerTask

class NativeAfterInterDialog(
    private val mActivity: AppCompatActivity,
    private val nativeModel: AdmobNativeModel,
    private val layout: Int = R.layout.admob_ad_template_full_screen,
    private val isStartNow: Boolean = false,
    private var counter: Int = DEFAULT_COUNTER,
    private val onClose: () -> Unit,
    private val onFailure: () -> Unit
): Dialog(mActivity) {

    private val binding by lazy { DialogNativeAfterInterBinding.inflate(layoutInflater) }

    companion object {
        const val DEFAULT_COUNTER = 3
    }

    var isClosedOrFail: Boolean = false
        set(value) {
            field = value
            startCounter()
        }
    private var isShowed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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
    }

    private fun initView() {
        if (counter == 0) {
            binding.tvCounter.visibility = View.INVISIBLE
            binding.ivClose.visibility = View.VISIBLE
            binding.btnClose.isClickable = true
        } else {
            binding.btnClose.isClickable = false
        }

        AdmobLib.showNative(
            mActivity,
            nativeModel,
            binding.frNative,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            layout = layout,
            onAdsShowed = {
//                Log.d("TAG", "initView: ${this.javaClass} showed")
                mActivity.runOnUiThread {
                    isShowed = true
                    startCounter()
                }
            },
            onAdsShowFail = {
//                Log.e("TAG", "initView: ${this.javaClass} failure")
                Handler(Looper.getMainLooper()).post {
                    dismiss()
                    if (isClosedOrFail) {
                        onClose()
                    } else {
                        onFailure()
                    }
                }
                return@showNative Unit
            }
        )
    }

    private fun initActionView() {
        binding.btnClose.setOnClickListener {
            dismiss()
            onClose()
        }
    }

    private fun startCounter() {
        if (counter == 0) return

        if (isStartNow || isClosedOrFail && isShowed && isShowing) {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mActivity.runOnUiThread {
                        if (counter == 0) {
                            binding.tvCounter.visibility = View.INVISIBLE
                            binding.ivClose.visibility = View.VISIBLE
                            binding.btnClose.isClickable = true
                            timer.cancel()
                        } else {
                            binding.tvCounter.text = counter.toString()
                            counter--
                        }
                    }
                }
            }, 500, 1000)
        }
    }

}