package com.infbyte.amuzic.utils

import androidx.compose.foundation.lazy.LazyListState

fun calcScroll(state: LazyListState): Int {
    val itemHeight = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
    return state.firstVisibleItemIndex * itemHeight
}
