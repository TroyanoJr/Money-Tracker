package net.micode.spendingtracker.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Helper class to manage loading and showing AdMob Interstitial Ads.
 */
object InterstitialAdHelper {
    private var mInterstitialAd: InterstitialAd? = null

    /**
     * Loads an interstitial ad in the background.
     * Call this when entering a screen where you might show an ad later.
     */
    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        // Sample Interstitial Ad Unit ID for testing
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
     * Shows the interstitial ad if it has been loaded.
     * @param activity The activity context required to show the ad.
     * @param onAdDismissed Callback executed when the ad is closed or if it fails to show.
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
