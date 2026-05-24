package com.infbyte.amuzic.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.NotificationMessage
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun NotificationBar(message: NotificationMessage = NotificationMessage.Success()) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            message.info.isNotEmpty(),
            enter = expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                Modifier.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10))
                    .fillMaxWidth().height(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                var icon = painterResource(R.drawable.ic_close)
                var color = MaterialTheme.colorScheme.error
                when (message) {
                    is NotificationMessage.Success -> {
                        icon = painterResource(R.drawable.ic_check)
                        color = MaterialTheme.colorScheme.primary
                    }

                    is NotificationMessage.Error -> {}
                }
                Icon(icon, null, tint = color)
                Text(message.info)
            }
        }
    }
}

@Preview
@Composable
fun PreviewNotificationBar() {
    AmuzicTheme {
        Surface {
            NotificationBar()
        }
    }
}
