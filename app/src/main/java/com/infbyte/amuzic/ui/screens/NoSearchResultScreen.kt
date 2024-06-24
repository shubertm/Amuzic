package com.infbyte.amuzic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun NoSearchResultScreen() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = "",
            Modifier.padding(top = 96.dp).size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Box(
            Modifier.padding(8.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(5.dp))
        ) {
            Text(
                stringResource(R.string.amuzic_no_matches_found),
                Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
fun PreviewNoSearchResultScreen() {
    AmuzicTheme {
        NoSearchResultScreen()
    }
}
