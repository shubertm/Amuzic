package com.infbyte.amuzic.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi

object AmuzicPermissions {
    fun isReadPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
                .checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        }

        return  context
            .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
