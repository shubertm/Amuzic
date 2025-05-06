package com.infbyte.amuzic.data.repo

import com.infbyte.amuzic.data.model.Playlist
import java.nio.file.Path

interface PlaylistsRepo {
    suspend fun init(path: Path)

    suspend fun add(list: Playlist)

    suspend fun get(name: String): Playlist

    suspend fun getAll(): List<Playlist>
}
