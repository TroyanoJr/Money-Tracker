package net.micode.spendingtracker.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Helper utility to manage the lifecycle of AdMob Interstitial Ads.
 * Handles background loading and conditional display of ads to avoid interrupting the user flow.
 */
object InterstitialAdHelper {
    private var mInterstitialAd: InterstitialAd? = null

    /**
     * Loads an interstitial ad in the background.
     * This should be called preemptively (e.g., when entering a screen) to ensure the ad 
     * is ready when [showAd] is invoked.
     * 
     * @param context The context used to load the ad.
     */
    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        // Sample Interstitial Ad Unit ID for testing purposes
        val adUnitId = "ca-app-pub-3940256099942544/1033173712"

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    /**
     * Shows the interstitial ad if it has been successfully loaded.
     * If the ad is not ready, it immediately triggers the [onAdDismissed] callback.
     * 
     * @param activity The activity context required to display the full-screen ad.
     * @param onAdDismissed Callback executed when the ad is closed, fails to show, or is not loaded.
     */
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            onAdDismissed()
        }
    }
}
