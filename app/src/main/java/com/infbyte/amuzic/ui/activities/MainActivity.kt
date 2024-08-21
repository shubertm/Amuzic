package com.infbyte.amuzic.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.infbyte.amuzic.BuildConfig
import com.infbyte.amuzic.R
import com.infbyte.amuzic.contracts.AmuzicContracts
import com.infbyte.amuzic.playback.AmuzicPlayerService
import com.infbyte.amuzic.ui.screens.AboutScreen
import com.infbyte.amuzic.ui.screens.ArtistOrAlbumSongsScreen
import com.infbyte.amuzic.ui.screens.LoadingScreen
import com.infbyte.amuzic.ui.screens.MainScreen
import com.infbyte.amuzic.ui.screens.NoMediaPermissionScreen
import com.infbyte.amuzic.ui.screens.NoMusicAvailableScreen
import com.infbyte.amuzic.ui.screens.PlayBar
import com.infbyte.amuzic.ui.screens.PlayListScreen
import com.infbyte.amuzic.ui.screens.Screens
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
        songsViewModel.setReadPermGranted(isGranted)
        if (isGranted) {
            songsViewModel.init(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val permissionLauncherApi30 = registerForActivityResult(
        AmuzicContracts.RequestPermissionApi30()
    ) { isGranted ->
        songsViewModel.setReadPermGranted(isGranted)
        if (isGranted) {
            songsViewModel.init(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!songsViewModel.state.isLoaded) {
            songsViewModel.setReadPermGranted(isReadPermissionGranted(this))
            if (!songsViewModel.state.isReadPermGranted) { launchPermRequest() } else {
                songsViewModel.init(this)
                installSplashScreen().setKeepOnScreenCondition {
                    !songsViewModel.state.isLoaded
                }
            }
        }

        setContent {
            AmuzicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    if (!songsViewModel.state.isReadPermGranted) {
                        NoMediaPermissionScreen(
                            onStartListening = { launchPermRequest() },
                            about = { navigateBack -> AboutScreen(onNavigateBack = { navigateBack() }) },
                            onExit = { onExit() }
                        )
                        return@Surface
                    }
                    if (
                        (songsViewModel.state.isReadPermGranted && !songsViewModel.state.isLoaded) ||
                        songsViewModel.state.isRefreshing
                    ) {
                        LoadingScreen()
                        return@Surface
                    }
                    if (!songsViewModel.state.hasMusic) {
                        NoMusicAvailableScreen(
                            onRefresh = {
                                if (!songsViewModel.state.isReadPermGranted) {
                                    launchPermRequest()
                                } else {
                                    songsViewModel.setIsRefreshing(true)
                                    songsViewModel.init(this)
                                }
                            },
                            onExit = { onExit() },
                            about = { navigateBack -> AboutScreen(onNavigateBack = { navigateBack() }) }
                        )
                        return@Surface
                    }
                    NavHost(navController, startDestination = Screens.MAIN) {
                        composable(Screens.MAIN) {
                            MainScreen(
                                songsViewModel,
                                onNavigate = { route -> navController.navigate(route) },
                                onExit = { onExit() },
                                about = { navigateBack -> AboutScreen(onNavigateBack = { navigateBack() }) }
                            )
                        }
                        composable(Screens.SONGS) {
                            ArtistOrAlbumSongsScreen(
                                songsViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                    Box(Modifier.fillMaxSize()) {
                        PlayBar(
                            songsViewModel.showPlayBar,
                            songsViewModel.state,
                            songsViewModel.state,
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
                            onSeekTo = { songsViewModel.onSeekTouch(it) },
                            onShowPlayListClick = { songsViewModel.onTogglePlayList(true) }
                        )

                        PlayListScreen(
                            show = songsViewModel.state.showPlayList,
                            songs = songsViewModel.state.currentPlaylist,
                            onSongClick = { songIndex ->
                                songsViewModel.onSongClicked(songIndex)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            stopService(Intent(this, AmuzicPlayerService::class.java))
            songsViewModel.onExit()
        }
    }

    private fun launchPermRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionLauncherApi30.launch(
                "package:${BuildConfig.APPLICATION_ID}"
            )
        } else {
            permissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun onExit() {
        if (!songsViewModel.confirmExit) {
            Toast.makeText(
                this,
                getString(R.string.amuzic_confirm_exit),
                Toast.LENGTH_SHORT
            )
                .show()
            songsViewModel.confirmExit()
        } else {
            finish()
        }
    }
}
