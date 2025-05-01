package com.infbyte.amuzic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Preview
@Composable
fun PreviewPrivacyPolicyDialog() {
    AmuzicTheme {
        PrivacyPolicyDialog()
    }
}

@Composable
fun PrivacyPolicyDialog(
    onAccept: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val string = stringResource(R.string.amuzic_agree_rationale)
    val privacyPolicyLink = stringResource(R.string.amuzic_privacy_policy_link)

    val annotatedString =
        buildAnnotatedString {
            append(string)
            val link =
                LinkAnnotation.Url(
                    privacyPolicyLink,
                    styles = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary)),
                )
            addLink(link, string.lastIndex - 14, string.lastIndex + 1)
        }

    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(10))) {
            Text(stringResource(R.string.amuzic_service_explain), Modifier.padding(16.dp))
            Row(
                Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.amuzic_cancel)) }
                Button(onClick = onAccept) { Text(stringResource(R.string.amuzic_agree)) }
            }
            Text(
                annotatedString,
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
