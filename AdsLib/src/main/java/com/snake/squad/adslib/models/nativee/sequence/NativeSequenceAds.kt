package com.snake.squad.adslib.models.nativee.sequence

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.utils.GoogleENative

class NativeSequenceAds(
    native00Id: String,
    native01Id: String,
    internal val config: NativeSequenceConfig
) {

    internal val native00 = AdmobNativeModel(native00Id)
    internal val native01 = AdmobNativeModel(native01Id)

    private val adsRequest by lazy {
        val request = AdRequest.Builder()
            .setHttpTimeoutMillis(config.timeout)

        if (config.keywordsForAdRequest.isNotEmpty()) config.keywordsForAdRequest.forEach {
            request.addKeyword(it)
        }

        request.build()
    }

    internal fun load(
        activity: AppCompatActivity,
    ) {
        AdmobLib.loadNative(
            activity = activity,
            admobNativeModel = native00,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            adRequest = adsRequest,
        )

        AdmobLib.loadNative(
            activity = activity,
            admobNativeModel = native01,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            adRequest = adsRequest,
        )
    }

    internal fun show(
        activity: AppCompatActivity,
        waiting: Boolean,
        layout: Int,
        onClosedOrFailed: (Boolean, Throwable?) -> Unit
    ): NativeSequenceDialog? {
        if ((native00.nativeAd.value == null && native00.isLoading.value == false) && (native01.nativeAd.value == null && native01.isLoading.value == false)) {
            onClosedOrFailed(false, Exception("No ads loading or ready!"))
            return null
        }

        val dialog = NativeSequenceDialog(
            mActivity = activity,
            mAds = this,
            waiting = waiting,
            mLayout = layout,
            onDismissed = { onClosedOrFailed(true, null) }
        )
        dialog.show()
        return dialog
    }

    internal fun loadAndShow(
        activity: AppCompatActivity,
        layout: Int,
        onClosedOrFailed: (Boolean, Throwable?) -> Unit
    ) {
        load(activity)
        show(activity, false, layout, onClosedOrFailed)
    }

}