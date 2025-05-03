package com.infbyte.amuzic.data.model

data class Playlist(
    val name: String = "Playlist 1",
    val songs: List<Song> = emptyList(),
)
