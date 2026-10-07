package com.snake.squad.adslib.models.nativee.sequence

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.AdmobLib.load
import com.snake.squad.adslib.AdmobLib.loadAndShowInterstitial
import com.snake.squad.adslib.AdmobLib.show
import com.snake.squad.adslib.AdmobLib.showInterstitial
import com.snake.squad.adslib.AdmobLib.showInterstitialNewAPI
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.utils.AdsHelper.isNetworkConnected
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal object NativeSequenceAfterInter {
    fun loadAndShowInterWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: NativeSequenceAds,
        adRequest: AdRequest? = null,
        timeout: Long = 10000,
        vShowInterAds: View?,
        showNativeAfter: Boolean = true,
        showOnTestDevice: Boolean = false,
        onInterCloseOrFailed: (Boolean) -> Unit = {},
        navAction: () -> Unit
    ) {
        if (!AdmobLib.getShowAds() || (AdmobLib.getCheckTestDevice() && !showOnTestDevice) || !isNetworkConnected(mActivity)) {
            navAction()
            onInterCloseOrFailed(false)
            return
        }

        var isNativeAfterCalled = false

        var isInterClosedOrFailed = false
        var isNativeAfterClosed = !showNativeAfter

        fun showNativeAfter() {
            if (isNativeAfterCalled) return
            isNativeAfterCalled = true

            if (!showNativeAfter) return

            if (mActivity.isFinishing || mActivity.isDestroyed) return

            show(
                mActivity,
                nativeModel,
                showOnTestDevice = showOnTestDevice,
                onAdsCloseOrFailed = { _, _ ->
                    isNativeAfterClosed = true
                    if (isInterClosedOrFailed) navAction()
                    true
                }
            )
        }

        vShowInterAds?.visibility = View.VISIBLE
        load(mActivity, nativeModel, showOnTestDevice)
        loadAndShowInterstitial(
            activity = mActivity,
            admobInterModel = interModel,
            adRequest = adRequest,
            timeout = timeout,
            isShowOnTestDevice = showOnTestDevice,
            onAdsShowed = {
                mActivity.lifecycleScope.launch {
                    delay(1000.milliseconds)
                    if (!isActive) return@launch
                    showNativeAfter()
                }
            },
            onAdsCloseOrFailed = {
                isInterClosedOrFailed = true
                if (isNativeAfterClosed) navAction() else showNativeAfter()
                onInterCloseOrFailed(it)
            }
        )
    }

    fun loadAndShowInterSplashWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: NativeSequenceAds,
        adRequest: AdRequest? = null,
        timeout: Long = 10000,
        showNativeAfter: Boolean = true,
        onInterCloseOrFailed: (Boolean) -> Unit = {},
        navAction: () -> Unit
    ) {
        if (!AdmobLib.getShowAds() || !isNetworkConnected(mActivity)) {
            navAction()
            onInterCloseOrFailed(false)
            return
        }

        var isNativeAfterCalled = false

        var isInterClosedOrFailed = false
        var isNativeAfterClosed = !showNativeAfter

        fun showNativeAfter() {
            if (isNativeAfterCalled) return
            isNativeAfterCalled = true

            if (!showNativeAfter) return

            if (mActivity.isFinishing || mActivity.isDestroyed) return

            show(
                mActivity,
                nativeModel,
                showOnTestDevice = true,
                onAdsCloseOrFailed = { _, _ ->
                    isNativeAfterClosed = true
                    if (isInterClosedOrFailed) navAction()
                    true
                }
            )
        }

        load(mActivity, nativeModel, true)
        loadAndShowInterstitial(
            activity = mActivity,
            admobInterModel = interModel,
            adRequest = adRequest,
            timeout = timeout,
            isShowOnTestDevice = true,
            onAdsShowed = {
                mActivity.lifecycleScope.launch {
                    delay(1000.milliseconds)
                    if (!isActive) return@launch
                    showNativeAfter()
                }
            },
            onAdsCloseOrFailed = {
                isInterClosedOrFailed = true
                if (isNativeAfterClosed) navAction() else showNativeAfter()
                onInterCloseOrFailed(it)
            }
        )
    }

    fun showInterNewAPIWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: NativeSequenceAds,
        vShowInterAds: View?,
        isPreload: Boolean = true,
        showNativeAfter: Boolean = true,
        showOnTestDevice: Boolean = false,
        onInterCloseOrFailed: (Boolean) -> Unit = {},
        navAction: () -> Unit
    ) {
        if (!AdmobLib.getShowAds() || (AdmobLib.getCheckTestDevice() && !showOnTestDevice) || !isNetworkConnected(mActivity)) {
            navAction()
            onInterCloseOrFailed(false)
            return
        }

        var isNativeAfterCalled = false

        var isInterClosedOrFailed = false
        var isNativeAfterClosed = !showNativeAfter

        fun showNativeAfter() {
            if (isNativeAfterCalled) return
            isNativeAfterCalled = true

            if (!showNativeAfter) return

            if (mActivity.isFinishing || mActivity.isDestroyed) return

            show(
                mActivity,
                nativeModel,
                showOnTestDevice = showOnTestDevice,
                onAdsCloseOrFailed = { _, _ ->
                    isNativeAfterClosed = true
                    if (isInterClosedOrFailed) navAction()
                    true
                }
            )
        }

        vShowInterAds?.visibility = View.VISIBLE
        load(mActivity, nativeModel, showOnTestDevice)
        showInterstitialNewAPI(
            activity = mActivity,
            admobInterModel = interModel,
            isShowOnTestDevice = showOnTestDevice,
            isPreload = isPreload,
            onAdsShowed = {
                mActivity.lifecycleScope.launch {
                    delay(1000.milliseconds)
                    if (!isActive) return@launch
                    showNativeAfter()
                }
            },
            onAdsCloseOrFailed = {
                isInterClosedOrFailed = true
                if (isNativeAfterClosed) navAction() else showNativeAfter()
                onInterCloseOrFailed(it)
            }
        )
    }

    fun showInterWithNativeAfter(
        mActivity: AppCompatActivity,
        interModel: AdmobInterModel,
        nativeModel: NativeSequenceAds,
        vShowInterAds: View?,
        isPreload: Boolean = true,
        showNativeAfter: Boolean = true,
        showOnTestDevice: Boolean = false,
        onInterCloseOrFailed: (Boolean) -> Unit = {},
        navAction: () -> Unit
    ) {
        if (!AdmobLib.getShowAds() || (AdmobLib.getCheckTestDevice() && !showOnTestDevice) || !isNetworkConnected(mActivity)) {
            navAction()
            onInterCloseOrFailed(false)
            return
        }

        var isNativeAfterCalled = false

        var isInterClosedOrFailed = false
        var isNativeAfterClosed = !showNativeAfter

        fun showNativeAfter() {
            if (isNativeAfterCalled) return
            isNativeAfterCalled = true

            if (!showNativeAfter) return

            if (mActivity.isFinishing || mActivity.isDestroyed) return

            show(
                mActivity,
                nativeModel,
                showOnTestDevice = showOnTestDevice,
                onAdsCloseOrFailed = { _, _ ->
                    isNativeAfterClosed = true
                    if (isInterClosedOrFailed) navAction()
                    true
                }
            )
        }

        vShowInterAds?.visibility = View.VISIBLE
        load(mActivity, nativeModel, showOnTestDevice)
        showInterstitial(
            activity = mActivity,
            admobInterModel = interModel,
            isShowOnTestDevice = showOnTestDevice,
            isPreload = isPreload,
            onAdsShowed = {
                mActivity.lifecycleScope.launch {
                    delay(1000.milliseconds)
                    if (!isActive) return@launch
                    showNativeAfter()
                }
            },
            onAdsCloseOrFailed = {
                isInterClosedOrFailed = true
                if (isNativeAfterClosed) navAction() else showNativeAfter()
                onInterCloseOrFailed(it)
            }
        )
    }
}