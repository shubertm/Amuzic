package com.infbyte.amuzic.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Size
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException

fun loadThumbnail(path: String): Bitmap? {
    return try {
        with(MediaMetadataRetriever()) {
            setDataSource(path)
            val imageByteArray = embeddedPicture
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray?.size!!)
            release()
            bitmap
        }
    } catch (_: Exception) { null }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun ContentResolver.loadThumbnail(uri: Uri): Bitmap? {
    val size = Size(60, 60)
    return try {
        loadThumbnail(
            uri,
            size,
            null
        )
    } catch (e: FileNotFoundException) {
        null
    }
}

fun Uri.decodeImage(): Bitmap? = BitmapFactory.decodeFile(toString())

fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}
