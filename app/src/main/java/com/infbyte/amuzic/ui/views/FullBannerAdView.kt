package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.infbyte.amuzic.R

@Composable
fun FullBannerAdView() {
    val adRequest = AdRequest.Builder().build()
    Box(
        Modifier.padding(start = 8.dp, end = 8.dp),
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    adUnitId = context.getString(R.string.banner_1_ad_unit_id)
                    setAdSize(AdSize.FULL_BANNER)
                }
            },
            update = { adView ->
                adView.loadAd(adRequest)
            },
        )
    }
}
