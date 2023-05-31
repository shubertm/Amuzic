package com.infbyte.amuzic.data.model

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    val id: Long,
    val name: String,
    val artist: String,
    val album: String,
    val folder: String,
    val uri: Uri,
    val thumbnail: Bitmap?
)
