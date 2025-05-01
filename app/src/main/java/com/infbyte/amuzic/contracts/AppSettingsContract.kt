package com.infbyte.amuzic.contracts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted

class AppSettingsContract : ActivityResultContract<String, Boolean>() {
    private var context: Context? = null

    override fun createIntent(
        context: Context,
        input: String,
    ): Intent {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", input, null),
            )
        this.context = context
        return intent
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return if (context != null) {
            val granted = isReadPermissionGranted(context!!)
            context = null
            granted
        } else {
            false
        }
    }
}
