package com.infbyte.amuzic.data.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.MediaItem

data class Song(
    val item: MediaItem = MediaItem.fromUri(Uri.EMPTY),
    val folder: String = "",
    val thumbnail: Bitmap? = null,
    val duration: Long = 0L,
) {
    val title = item.mediaMetadata.title.toString()
    val artist = item.mediaMetadata.artist.toString()
    val album = item.mediaMetadata.albumTitle.toString()
    val uri = item.requestMetadata.mediaUri
    val id = item.mediaId
    var isPlaying: Boolean = false
        private set

    fun updateIsPlaying(value: Boolean) {
        isPlaying = value
    }

    companion object {
        val EMPTY = Song()
    }
}
