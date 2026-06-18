package net.micode.moneytracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A Composable that displays an Anchored Adaptive AdMob Banner Ad.
 * This component automatically calculates the appropriate ad size based on the screen width.
 * 
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                // Determine the screen width to get an adaptive ad size
                val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    ctx,
                    ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density.toInt()
                )
                
                setAdSize(adSize)
                // Sample Ad Unit ID for Testing
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
