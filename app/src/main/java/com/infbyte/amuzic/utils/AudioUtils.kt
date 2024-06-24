package com.infbyte.amuzic.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import com.infbyte.amuzic.data.repo.SongsRepo.Companion.ALBUM_PROJECTION
import java.io.FileNotFoundException

fun ContentResolver.loadThumbnail(albumId: Long): Bitmap? {
    var path: String? = null
    val selection = "${MediaStore.Audio.Albums._ID} = ?"
    val selectionArgs = arrayOf("$albumId")
    val cursor = this.query(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        ALBUM_PROJECTION,
        selection,
        selectionArgs,
        null
    )
    cursor?.let {
        if (it.moveToFirst()) {
            val albumArtCOLUMN = cursor
                .getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)
            path = cursor.getString(albumArtCOLUMN)
        }
        it.close()
    }
    return Uri.parse(path).decodeImage()
}

@RequiresApi(Build.VERSION_CODES.Q)
fun ContentResolver.loadThumbnail(uri: Uri): Bitmap? {
    val size = Size(60, 60)
    return try {
        this.loadThumbnail(
            uri,
            size,
            null
        )
    } catch (e: FileNotFoundException) {
        null
    }
}

fun Uri.decodeImage(): Bitmap? = BitmapFactory.decodeFile(toString())
