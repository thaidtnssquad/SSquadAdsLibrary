package com.snake.squad.adslib.cmp

import android.app.Activity
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

object CMPUtils {

    // Giữ một biến toàn cục để App có thể gọi lại sau này (cho Privacy Options button)
    var consentManager: GoogleMobileAdsConsentManager? = null
        private set

    fun setupCMP(activity: Activity, onConsentGranted: () -> Unit) {
        val isMobileAdsInitializeCalled = AtomicBoolean(false)
        val manager = GoogleMobileAdsConsentManager(activity)
        consentManager = manager

        manager.gatherConsent { error ->
            error?.let {
                // Chỉ log lỗi, KHÔNG khởi tạo ads ở đây để tránh vi phạm GDPR nếu chưa có consent
                Log.w("CMPUtils", "Consent gathering failed: ${it.message}")
            }

            // Callback khi user vừa chọn xong form hoặc form check update xong
            if (manager.canRequestAds) {
                initializeMobileAdsSdk(isMobileAdsInitializeCalled, onConsentGranted)
            }
        }

        // QUAN TRỌNG TỐI ƯU HÓA: 
        // Dành cho returning users (đã consent từ trước hoặc ngoài vùng EEA).
        // Khởi tạo SDK ngay lập tức song song với việc check update form.
        if (manager.canRequestAds) {
            initializeMobileAdsSdk(isMobileAdsInitializeCalled, onConsentGranted)
        }
    }

    private fun initializeMobileAdsSdk(isMobileAdsInitializeCalled: AtomicBoolean, onConsentGranted: () -> Unit) {
        // Sử dụng AtomicBoolean để đảm bảo SDK chỉ được khởi tạo và callback đúng 1 lần
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        
        onConsentGranted.invoke()
    }
}