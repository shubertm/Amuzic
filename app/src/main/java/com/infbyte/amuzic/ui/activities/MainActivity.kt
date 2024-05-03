package com.infbyte.amuzic.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.infbyte.amuzic.BuildConfig
import com.infbyte.amuzic.contracts.AmuzicContracts
import com.infbyte.amuzic.ui.screens.LoadingSongsProgress
import com.infbyte.amuzic.ui.screens.MainScreen
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val songsViewModel: SongsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            songsViewModel.init(this)
        } else {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val permissionLauncherApi30 = registerForActivityResult(
        AmuzicContracts.RequestPermissionApi30()
    ) { isGranted ->
        if (isGranted) {
            songsViewModel.init(this)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!songsViewModel.isLoaded.value) {
            if (!isReadPermissionGranted(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    permissionLauncherApi30.launch(
                        "package:${BuildConfig.APPLICATION_ID}"
                    )
                } else {
                    permissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            } else {
                songsViewModel.init(this)
            }
        }
        setContent {
            AmuzicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (songsViewModel.isLoadingSongs.value) {
                        LoadingSongsProgress()
                    } else {
                        val navController = rememberNavController()
                        MainScreen()
                        /*{
                            LaunchedEffect(UI_CONTROLS_HINT) {
                                songsViewModel.showAndDelayHideTopBar()
                                songsViewModel.showAndDelayHidePlayBar()
                            }
                            SideEffect {
                                if (songsViewModel.showPopup.value) {
                                    songsViewModel.hideTopBar()
                                    return@SideEffect
                                }
                                songsViewModel.showAndDelayHideTopBar()
                            }
                            NavHost(
                                navController,
                                startDestination = Screens.ALL_SONGS
                            ) {
                                composable(Screens.ALL_SONGS) {
                                    SongsScreen(
                                        songsViewModel.showSongs.value,
                                        songsViewModel.showPopup,
                                        songsViewModel.songs,
                                        onScroll = { value ->
                                            songsViewModel.toggleBarsByScroll(value)
                                        },
                                        onSongClick = { song ->
                                            songsViewModel.onSongClicked(song)
                                        }
                                    )
                                }
                                composable(Screens.ARTISTS) {
                                    ArtistsScreen(
                                        songsViewModel.showArtists.value,
                                        songsViewModel.showPopup,
                                        songsViewModel.artists,
                                        onScroll = { value ->
                                            songsViewModel.toggleBarsByScroll(value)
                                        },
                                        onArtistClick = { artistPos ->
                                            songsViewModel.onArtistClicked(artistPos)
                                            songsViewModel.hideBars()
                                            navController
                                                .navigate(Screens.ALL_SONGS)
                                        }
                                    )
                                }
                                composable(Screens.ALBUMS) {
                                    AlbumsScreen(
                                        songsViewModel.showAlbums.value,
                                        songsViewModel.showPopup,
                                        songsViewModel.albums,
                                        onScroll = { value ->
                                            songsViewModel.toggleBarsByScroll(value)
                                        },
                                        onAlbumClicked = { albumPos ->
                                            songsViewModel.onAlbumClicked(albumPos)
                                            songsViewModel.hideBars()
                                            navController
                                                .navigate(Screens.ALL_SONGS)
                                        }
                                    )
                                }
                                composable(Screens.FOLDERS) {
                                    FoldersScreen(
                                        songsViewModel.showFolders.value,
                                        songsViewModel.showPopup,
                                        songsViewModel.folders,
                                        onScroll = { value ->
                                            songsViewModel.toggleBarsByScroll(value)
                                        },
                                        onFolderClicked = { folderPos ->
                                            songsViewModel.onFolderClicked(folderPos)
                                            songsViewModel.hideBars()
                                            navController
                                                .navigate(Screens.ALL_SONGS)
                                        }
                                    )
                                }
                            }
                        }*/
                        /* songsViewModel.currentSong.value?.let {
                            Box(
                                Modifier
                                    .fillMaxSize()
                            ) {
                                PlayBar(
                                    songsViewModel.showPlayBar,
                                    songsViewModel.isPlaying(),
                                    it,
                                    songsViewModel.progress,
                                    songsViewModel.playbackMode,
                                    onPlayClick = {
                                        songsViewModel.onPlayClicked()
                                    },
                                    onNextClick = {
                                        songsViewModel.onNextClicked()
                                    },
                                    onPrevClick = {
                                        songsViewModel.onPrevClicked()
                                    },
                                    onTogglePlaybackMode = {
                                        songsViewModel.onTogglePlaybackMode()
                                    },
                                    onSeekTo = { songsViewModel.onSeekTouch(it) }
                                )
                            }
                        }*/
                        /*BackHandler {
                            if (songsViewModel.showPopup.value) {
                                songsViewModel.showPopup.value = false
                                return@BackHandler
                            }
                            if (navController.popBackStack()) {
                                navController.currentDestination
                                    ?.route?.let { screen ->
                                        songsViewModel.onScreenSelected(screen)
                                    }
                                return@BackHandler
                            }
                            if (!songsViewModel.confirmExit) {
                                Toast.makeText(
                                    this,
                                    getString(R.string.confirm_exit),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                songsViewModel.confirmExit()
                            } else {
                                finish()
                            }
                        }*/
                    }
                }
            }
        }
    }
}
