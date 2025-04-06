package com.infbyte.amuzic.utils

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.core.net.toUri

fun calcScroll(state: LazyListState): Int {
    val itemHeight = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
    return state.firstVisibleItemIndex * itemHeight
}

fun String.getInitialChar(): String {
    return first { it.isLetterOrDigit() }.uppercase()
}

fun <C> List<C>.tryGetFirst(default: () -> C): C {
    return if (isNotEmpty()) { first() } else default()
}

fun Context.openWebLink(@StringRes linkRes: Int) {
    val link = getString(linkRes)
    startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(link.toUri())
    )
}

fun Modifier.navigationBarsPadding(condition: Boolean): Modifier = if (condition) navigationBarsPadding() else this
