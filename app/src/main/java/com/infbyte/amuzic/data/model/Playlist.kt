package com.infbyte.amuzic.data.model

import com.infbyte.amuzic.data.repo.MediaItemId

data class Playlist(
    val name: String = "",
    val songs: List<MediaItemId> = emptyList(),
)
