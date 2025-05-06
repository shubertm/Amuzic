package com.infbyte.amuzic.data.repo

import android.os.Build
import android.util.Log
import com.infbyte.amuzic.data.model.Playlist
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

typealias MediaItemId = String

class PlaylistsRepoImpl
    @Inject
    constructor() : PlaylistsRepo {
        private val playlists = mutableMapOf<String, List<MediaItemId>>()

        private lateinit var storageFile: Path

        override suspend fun init(path: Path) {
            storageFile =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    path.resolve("playlists")
                } else {
                    Path("$path/playlists")
                }

            if (storageFile.notExists()) {
                Log.d(LOG_TAG, "creating playlists storage")
                storageFile.createFile()
            }

            read()
        }

        override suspend fun add(list: Playlist) {
            playlists[list.name] = list.songs
            write()
        }

        override suspend fun get(name: String): Playlist {
            return Playlist(name = name, songs = playlists[name] ?: emptyList())
        }

        override suspend fun getAll(): List<Playlist> {
            return playlists.map { Playlist(name = it.key, songs = it.value) }
        }

        private fun write() {
            val lines =
                playlists.map {
                    val name = it.key
                    val ids = it.value.joinToString(",")
                    "$name$KEY_VALUE_SEPARATOR$ids"
                }
            Log.d(LOG_TAG, "writing playlists to storage")

            storageFile.writeLines(lines)
        }

        private fun read() {
            Log.d(LOG_TAG, "reading playlists from storage")
            val lines = storageFile.readLines()
            lines.map { line ->
                val parts = line.split(KEY_VALUE_SEPARATOR)
                val name = parts[0]
                val ids = parts[1].split(",")
                playlists[name] = ids
            }
        }

        companion object {
            private const val LOG_TAG = "Playlist Repo"
            private const val KEY_VALUE_SEPARATOR = ":"
        }
    }
