package com.snake.squad.adslib.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails

object AdsHelper {

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    fun getReferrer(context: Context, callback: (String?) -> Unit) {
        val referrerClient = InstallReferrerClient.newBuilder(context).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer
                            callback(referrerUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callback(null)
                        } finally {
                            referrerClient.endConnection()
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        callback(null)
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        callback(null)
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {

            }
        })
    }

    fun isOrganicUser(referrerUrl: String): Boolean {
        val lowerRef = referrerUrl.lowercase()

        return !(lowerRef.contains("gclid=")
                || lowerRef.contains("gbraid=")
                || lowerRef.contains("gad_source=")
                || lowerRef.contains("apps.facebook.com")
                || lowerRef.contains("apps.instagram.com"))
    }

}