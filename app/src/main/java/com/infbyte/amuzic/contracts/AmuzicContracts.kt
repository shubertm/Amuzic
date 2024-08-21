package com.infbyte.amuzic.contracts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted

object AmuzicContracts {

    @RequiresApi(Build.VERSION_CODES.R)
    class RequestPermissionApi30 : ActivityResultContract<String, Boolean>() {

        private var context: Context? = null

        override fun createIntent(context: Context, input: String): Intent {
            val packageUri = Uri.parse(input)
            this.context = context
            return Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                packageUri
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            val granted = isReadPermissionGranted(context!!)
            context = null
            return granted
        }
    }
}
