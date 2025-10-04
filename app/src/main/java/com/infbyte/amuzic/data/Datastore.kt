package com.infbyte.amuzic.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.collectLatest

private const val NAME = "AmuzicDatastore"
private val Context.Datastore by preferencesDataStore(NAME)

val TERMS_ACCEPTED_KEY = booleanPreferencesKey("terms_accepted_key")
val LAST_PLAYED_SONG = stringPreferencesKey("last_played_song")

suspend fun Context.writeBoolean(
    key: Preferences.Key<Boolean>,
    value: Boolean,
) {
    Datastore.edit {
        it[key] = value
    }
}

suspend fun Context.readBoolean(
    key: Preferences.Key<Boolean>,
    onSuccess: (Boolean) -> Unit,
) {
    Datastore.data.collectLatest { pref ->
        val boolean = pref[key]
        onSuccess(boolean ?: false)
    }
}

suspend fun Context.writeInt(
    key: Preferences.Key<Int>,
    value: Int,
) {
    Datastore.edit {
        it[key] = value
    }
}

suspend fun Context.readInt(
    key: Preferences.Key<Int>,
    onSuccess: (Int) -> Unit,
) {
    Datastore.data.collectLatest { pref ->
        val int = pref[key]
        onSuccess(int ?: 0)
    }
}

suspend fun Context.writeLong(
    key: Preferences.Key<Long>,
    value: Long,
) {
    Datastore.edit {
        it[key] = value
    }
}

suspend fun Context.readLong(
    key: Preferences.Key<Long>,
    onSuccess: (Long) -> Unit,
) {
    Datastore.data.collectLatest { pref ->
        val long = pref[key]
        onSuccess(long ?: 0)
    }
}

suspend fun Context.writeString(
    key: Preferences.Key<String>,
    value: String,
) {
    Datastore.edit {
        it[key] = value
    }
}

suspend fun Context.readString(
    key: Preferences.Key<String>,
    onSuccess: (String) -> Unit,
) {
    Datastore.data.collectLatest { pref ->
        val string = pref[key]
        onSuccess(string ?: "")
    }
}
