package com.plcoding.bma.util

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.plcoding.bma.BuildConfig
import timber.log.Timber

fun InterstitialAd.show(
    activity: Activity,
    onNewAdLoaded: (InterstitialAd) -> Unit,
    onAdDismissed: () -> Unit = {},
    onFullScreenFailed: (AdError) -> Unit = {},
    shouldShow: Boolean = true
) {
    if(!shouldShow) {
        onAdDismissed()
        return
    }
    fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdFailedToShowFullScreenContent(err: AdError) {
            super.onAdFailedToShowFullScreenContent(err)
            onFullScreenFailed(err)
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            onAdDismissed()
        }
    }
    show(activity)

    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(activity, Constants.ADMOB_INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
            super.onAdLoaded(ad)
            onNewAdLoaded(ad)
        }

        override fun onAdFailedToLoad(err: LoadAdError) {
            super.onAdFailedToLoad(err)

        }
    })
}