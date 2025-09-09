package com.infbyte.amuzic.utils

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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

@Composable
fun Int.toDp(): Dp {
    return with(LocalDensity.current) {
        toDp()
    }
}
