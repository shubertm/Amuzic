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
import com.infbyte.amuzic.ui.screens.ArtistOrAlbumSongsScreen
import com.infbyte.amuzic.ui.screens.MainScreen
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
                installSplashScreen().setKeepOnScreenCondition {
                    !songsViewModel.isLoaded.value
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
                    NavHost(navController, startDestination = Screens.MAIN) {
                        composable(Screens.MAIN) {
                            MainScreen(
                                songsViewModel,
                                onNavigate = { route -> navController.navigate(route) },
                                onExit = {
                                    if (!songsViewModel.confirmExit) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.amuzic_confirm_exit),
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        songsViewModel.confirmExit()
                                    } else {
                                        finish()
                                    }
                                }
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
        stopService(Intent(this, AmuzicPlayerService::class.java))
        songsViewModel.onExit()
    }
}
