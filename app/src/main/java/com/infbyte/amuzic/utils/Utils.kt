package com.infbyte.amuzic.utils

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.infbyte.amuzic.ui.views.FullBannerAdView

fun calcScroll(state: LazyListState): Int {
    val itemHeight = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
    return state.firstVisibleItemIndex * itemHeight
}

fun String.getInitialChar(): String {
    return first { it.isLetterOrDigit() }.uppercase()
}

fun <C> List<C>.tryGetFirst(default: () -> C): C {
    return if (isNotEmpty()) {
        first()
    } else {
        default()
    }
}

fun Context.openWebLink(
    @StringRes linkRes: Int,
) {
    val link = getString(linkRes)
    startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(link.toUri()),
    )
}

fun Modifier.navigationBarsPadding(condition: Boolean): Modifier = if (condition) navigationBarsPadding() else this

fun <T> LazyListScope.accommodateFullBannerAds(
    items: List<T>,
    bannerInitialPosition: Int = 9,
    showOnTopWithFewItems: Boolean = true,
    itemView: @Composable (T) -> Unit,
) {
    if (items.size > bannerInitialPosition) {
        val numberOfGroups = items.size / bannerInitialPosition
        var remainingItems = items

        for (group in 0..numberOfGroups) {
            items(remainingItems.take(bannerInitialPosition)) { item ->
                itemView(item)
            }
            if (remainingItems.isNotEmpty()) {
                if (remainingItems.size >= bannerInitialPosition) {
                    item {
                        FullBannerAdView()
                    }
                }
                remainingItems = remainingItems.drop(bannerInitialPosition)
            }
        }
        return
    }
    if (showOnTopWithFewItems) {
        item {
            FullBannerAdView()
        }
    }
    items(items) { item ->
        itemView(item)
    }
}
