package com.snake.squad.adslib.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.snake.squad.adslib.R

object NativeUtils {

    fun populateNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView,
        size: GoogleENative,
        isExpandedLayout: Boolean = false,
        onCollapsedLayoutClick: (() -> Unit)? = null
    ) {
        if (nativeAd == null || adView == null || size == null) {
            return
        }

        if (size == GoogleENative.UNIFIED_MEDIUM || size == GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON || size == GoogleENative.UNIFIED_FULL_SCREEN) {
            adView.findViewById<MediaView>(R.id.ad_media)?.let {
                adView.mediaView = it
            }
        }
        adView.findViewById<TextView>(R.id.ad_headline)?.let {
            adView.headlineView = it
        }
        adView.findViewById<TextView>(R.id.ad_body)?.let {
            adView.bodyView = it
        }
        adView.findViewById<Button>(R.id.ad_call_to_action)?.let {
            adView.callToActionView = it
        }
        adView.findViewById<ImageView>(R.id.ad_app_icon)?.let {
            adView.iconView = it
        }
        adView.findViewById<RatingBar>(R.id.ad_stars)?.let {
            adView.starRatingView = it
        }
        if (isExpandedLayout) {
            adView.findViewById<ImageView>(R.id.iv_close)?.let {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    onCollapsedLayoutClick?.invoke()
                }
            }
        }
        if (nativeAd.mediaContent != null) {
            if (size == GoogleENative.UNIFIED_MEDIUM || size == GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON || size == GoogleENative.UNIFIED_FULL_SCREEN) {
                adView.mediaView?.let {
                    it.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    val mediaContent = nativeAd.mediaContent
                    if (mediaContent != null && mediaContent.hasVideoContent()) {
                        val mediaView = MediaView(it.context)
                        mediaView.mediaContent = mediaContent
                        it.addView(mediaView)
                    }
                }
            }
        }

        if (nativeAd.headline != null) {
            (adView.headlineView as TextView).text = nativeAd.headline
        }
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE

        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }


        if (adView.iconView != null) {
            if (nativeAd.icon == null) {
                adView.iconView!!.visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.icon!!.drawable
                )
                adView.iconView!!.visibility = View.VISIBLE
            }
        }

        if (nativeAd.starRating != null) {
            (adView.starRatingView as RatingBar).rating = 5f
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent!!.videoController
        if (vc.hasVideoContent()) {
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            }
        }
    }

}