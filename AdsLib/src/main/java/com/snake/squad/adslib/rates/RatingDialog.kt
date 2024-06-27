package com.snake.squad.adslib.rates

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orhanobut.hawk.Hawk
import com.snake.squad.adslib.R
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.databinding.LayoutRatingDialogBinding

class RatingDialog : DialogFragment() {

    private lateinit var binding: LayoutRatingDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = LayoutRatingDialogBinding.inflate(layoutInflater)

        binding.btnLater.setOnClickListener {
            dismiss()
        }

        binding.btnRate.setOnClickListener {
            handleRating(binding.ratingBar.rating)
        }

        val title = getString(R.string.dialog_five_star_title, getString(R.string.app_name))
        binding.tvTitle.text = title

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
            .setCancelable(false).create()
    }

    private fun handleRating(rating: Float) {
        if (rating >= 4) {
            Hawk.put(KEY_COUNT_RATE, Hawk.get(KEY_COUNT_RATE, 0) + 1)
            openPlayStoreForRating()
        } else {
            sendEmailFeedback()
        }
        dismiss()
    }

    private fun openPlayStoreForRating() {
        val packageName = requireActivity().packageName
        try {
            registerRated.launch(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            registerRated.launch(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun sendEmailFeedback() {
        val subject = getString(R.string.subject_email, getString(R.string.app_name))
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_FEEDBACK))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        registerRated.launch(Intent.createChooser(emailIntent, getString(R.string.send_mail)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.setDismissMessage(null)
    }

    private val registerRated = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        AppOnResumeAdsManager.getInstance().disableForActivity(requireActivity().javaClass)
    }

    companion object {
        const val KEY_COUNT_RATE = "count_rate"
        private const val PREF_KEY_APP_LAUNCH_COUNT = "app_launch_count"
        private var EMAIL_FEEDBACK = ""

        fun showRateAppDialog(activity: Activity, fragmentManager: FragmentManager, email: String) {
            EMAIL_FEEDBACK = email
            RatingDialog().show(fragmentManager, "RatingDialog")
            AppOnResumeAdsManager.getInstance().disableForActivity(activity.javaClass)
        }

        fun showRateAppDialogAuto(activity: Activity, fragmentManager: FragmentManager, time: Int, email: String) {
            var appLaunchCount = Hawk.get(PREF_KEY_APP_LAUNCH_COUNT, 0)
            val ratingCount = Hawk.get(KEY_COUNT_RATE, 0)
            if (appLaunchCount >= time && ratingCount < 2) {
                showRateAppDialog(activity, fragmentManager, email)
            } else {
                Hawk.put(PREF_KEY_APP_LAUNCH_COUNT, ++appLaunchCount)
            }
        }
    }
}
