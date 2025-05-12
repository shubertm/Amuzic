package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectionCount(
    number: Int,
    onCancel: () -> Unit,
) {
    Row(
        Modifier.padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onCancel,
        ) {
            Icon(Icons.Outlined.Close, "")
        }
        if (number > 0) {
            Text(number.toString())
        }
    }
}
