package com.infbyte.amuzic.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.infbyte.amuzic.BuildConfig
import com.infbyte.amuzic.R
import com.infbyte.amuzic.contracts.AmuzicContracts
import com.infbyte.amuzic.data.viewmodel.SongsViewModel
import com.infbyte.amuzic.ui.screens.AlbumsScreen
import com.infbyte.amuzic.ui.screens.ArtistsScreen
import com.infbyte.amuzic.ui.screens.FoldersScreen
import com.infbyte.amuzic.ui.screens.MainScreen
import com.infbyte.amuzic.ui.screens.LoadingSongsProgress
import com.infbyte.amuzic.ui.screens.PlayBar
import com.infbyte.amuzic.ui.screens.Screens
import com.infbyte.amuzic.ui.screens.SongsScreen
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted
import com.infbyte.amuzic.utils.UI_CONTROLS_HINT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val songsViewModel: SongsViewModel by viewModels()
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            songsViewModel.loadSongs()
        } else {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val permissionLauncherApi30 = registerForActivityResult(
        AmuzicContracts.RequestPermissionApi30()
    ) { isGranted ->
        if (isGranted) {
            songsViewModel.loadSongs()
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
                songsViewModel.loadSongs()
            }
        }
        setContent {
            AmuzicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    if (songsViewModel.isLoadingSongs.value) {
                        LoadingSongsProgress()
                    } else {
                        val navController = rememberNavController()
                        MainScreen(
                            songsViewModel.showTopBar,
                            songsViewModel.showPopup.value,
                            songsViewModel.currentScreen.value,
                            onTogglePopup = {
                                songsViewModel.onTogglePopup()
                            },
                            onNavigateToScreen = { screen ->
                                songsViewModel.onScreenSelected(screen)
                                navController.navigate(screen)
                            }
                        ) {
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
                                            navController
                                                .navigate(Screens.ALL_SONGS)
                                        }
                                    )
                                }
                            }
                        }
                        songsViewModel.currentSong.value?.let {
                            Box(
                                Modifier
                                    .fillMaxSize()
                            ) {
                                PlayBar(
                                    songsViewModel.showPlayBar,
                                    songsViewModel.isPlaying,
                                    it,
                                    songsViewModel.progress,
                                    onPlayClick = {
                                        songsViewModel.onPlayClicked()
                                    },
                                    onNextClick = {
                                        songsViewModel.onNextClicked()
                                    },
                                    onPrevClick = {
                                        songsViewModel.onPrevClicked()
                                    }
                                )
                            }
                        }
                        BackHandler {
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
                        }
                    }
                }
            }
        }
    }
}
