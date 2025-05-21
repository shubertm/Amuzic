package com.infbyte.amuzic.data.model

sealed class NotificationMessage(val info: String) {
    class Success(info: String = "") : NotificationMessage(info)

    class Error(info: String = "") : NotificationMessage(info)
}
