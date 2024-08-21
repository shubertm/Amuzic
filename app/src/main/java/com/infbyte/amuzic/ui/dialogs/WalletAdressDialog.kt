package com.infbyte.amuzic.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun WalletAddressDialog(
    address: String,
    currencyName: String,
    currencyIcon: Painter,
    onDismiss: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { onDismiss(false) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(currencyIcon, contentDescription = "", Modifier.size(32.dp))
                Text(currencyName, Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val addressScrollState = rememberScrollState()
                Text(
                    address,
                    Modifier
                        .fillMaxWidth(.735f)
                        .padding(end = 8.dp)
                        .horizontalScroll(addressScrollState),
                    maxLines = 1
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboardManager.setPrimaryClip(
                                ClipData.newPlainText(
                                    context.getString(R.string.amuzic_wallet_address),
                                    address
                                )
                            )
                        }
                    ) {
                        Icon(painterResource(R.drawable.ic_content_copy), contentDescription = "")
                        Text(stringResource(R.string.amuzic_copy))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWalletAddressDialog() {
    AmuzicTheme {
        WalletAddressDialog(
            stringResource(R.string.btc_address),
            stringResource(R.string.amuzic_btc),
            painterResource(R.drawable.ic_btc)
        ) {}
    }
}
