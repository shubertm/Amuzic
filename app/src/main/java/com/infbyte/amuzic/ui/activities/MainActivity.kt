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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.infbyte.amuze.ui.screens.AboutScreen
import com.infbyte.amuze.ui.screens.LoadingScreen
import com.infbyte.amuze.ui.screens.NoMediaAvailableScreen
import com.infbyte.amuze.ui.screens.NoMediaPermissionScreen
import com.infbyte.amuzic.BuildConfig
import com.infbyte.amuzic.R
import com.infbyte.amuzic.contracts.AppSettingsContract
import com.infbyte.amuzic.data.TERMS_ACCEPTED_KEY
import com.infbyte.amuzic.data.readBoolean
import com.infbyte.amuzic.data.writeBoolean
import com.infbyte.amuzic.playback.AmuzicPlayerService
import com.infbyte.amuzic.ui.dialogs.PrivacyPolicyDialog
import com.infbyte.amuzic.ui.dialogs.AppSettingsRedirectDialog
import com.infbyte.amuzic.ui.screens.ArtistOrAlbumSongsScreen
import com.infbyte.amuzic.ui.screens.MainScreen
import com.infbyte.amuzic.ui.screens.PlayBar
import com.infbyte.amuzic.ui.screens.PlayListScreen
import com.infbyte.amuzic.ui.screens.Screens
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted
import com.infbyte.amuzic.utils.AmuzicPermissions.showReqPermRationale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val songsViewModel: SongsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        songsViewModel.setReadPermGranted(isGranted)
        if (isGranted) {
            songsViewModel.init()
            return@registerForActivityResult
        }
    }

    private val appSettingsLauncher = registerForActivityResult(
        AppSettingsContract()
    ) { isGranted ->
        songsViewModel.setReadPermGranted(isGranted)
        if (isGranted) {
            songsViewModel.init()
            return@registerForActivityResult
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            MobileAds.initialize(this@MainActivity)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                songsViewModel.refreshController(this@MainActivity)
            }
        }

        if (!songsViewModel.state.isLoaded) {
            songsViewModel.setReadPermGranted(isReadPermissionGranted(this@MainActivity))

            if (!songsViewModel.state.isReadPermGranted) {
                lifecycleScope.launch {
                    readBoolean(TERMS_ACCEPTED_KEY) { accepted ->
                        songsViewModel.setTermsAccepted(accepted)
                        if (accepted) {
                            launchPermRequest()
                            return@readBoolean
                        }
                        songsViewModel.showPrivacyDialog()
                    }
                }
            } else { songsViewModel.init() }
        }

        songsViewModel.onCloseSplash()

        installSplashScreen().setKeepOnScreenCondition {
            songsViewModel.sideEffect.showSplash
        }

        setContent {
            AmuzicTheme {
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    if (songsViewModel.sideEffect.showPrivacyDialog) {
                        PrivacyPolicyDialog(
                            onAccept = {
                                songsViewModel.hidePrivacyDialog()
                                lifecycleScope.launch {
                                    writeBoolean(TERMS_ACCEPTED_KEY, true)
                                }
                            },
                            onDismiss = {
                                songsViewModel.hidePrivacyDialog()
                            }
                        )
                        return@Surface
                    }

                    if (songsViewModel.sideEffect.showAppSettingsRedirect) {
                        AppSettingsRedirectDialog(
                            onAccept = {
                                songsViewModel.hideAppSettingsRedirect()
                                appSettingsLauncher.launch(packageName)
                            },
                            onDismiss = { songsViewModel.hideAppSettingsRedirect() }
                        )
                    }

                    if (
                        (songsViewModel.state.isReadPermGranted && !songsViewModel.state.isLoaded) ||
                        songsViewModel.state.isRefreshing
                    ) {
                        LoadingScreen()
                        return@Surface
                    }

                    if (!songsViewModel.state.isReadPermGranted) {
                        NoMediaPermissionScreen(
                            R.drawable.ic_amuzic_intro,
                            R.string.amuzic_listen,
                            onStartAction = {
                                if (!songsViewModel.state.isTermsAccepted) {
                                    songsViewModel.showPrivacyDialog()
                                    return@NoMediaPermissionScreen
                                }

                                if (!showReqPermRationale()) {
                                    songsViewModel.showAppSettingsRedirect()
                                    return@NoMediaPermissionScreen
                                }

                                launchPermRequest()
                            },
                            aboutApp = { navigateBack ->
                                AboutScreen(
                                    stringResource(R.string.app_name),
                                    BuildConfig.VERSION_NAME,
                                    R.drawable.ic_amuzic_foreground,
                                    R.string.amuzic_privacy_policy_link,
                                    onNavigateBack = { navigateBack() }
                                )
                            },
                            onExit = { onExit() }
                        )
                        return@Surface
                    }

                    if (!songsViewModel.state.hasMusic) {
                        NoMediaAvailableScreen(
                            R.string.amuzic_no_muzic,
                            onRefresh = {
                                if (!songsViewModel.state.isReadPermGranted) {
                                    launchPermRequest()
                                } else {
                                    songsViewModel.setIsRefreshing(true)
                                    songsViewModel.init()
                                }
                            },
                            onExit = { onExit() },
                            aboutApp = { navigateBack ->
                                AboutScreen(
                                    stringResource(R.string.app_name),
                                    BuildConfig.VERSION_NAME,
                                    R.drawable.ic_amuzic_foreground,
                                    R.string.amuzic_privacy_policy_link,
                                    onNavigateBack = { navigateBack() }
                                )
                            }
                        )
                        return@Surface
                    }

                    NavHost(navController, startDestination = Screens.MAIN) {
                        composable(Screens.MAIN) {
                            MainScreen(
                                songsViewModel,
                                onNavigate = { route -> navController.navigate(route) },
                                onExit = { onExit() },
                                about = { navigateBack ->
                                    AboutScreen(
                                        stringResource(R.string.app_name),
                                        BuildConfig.VERSION_NAME,
                                        R.drawable.ic_amuzic_foreground,
                                        R.string.amuzic_privacy_policy_link,
                                        onNavigateBack = { navigateBack() }
                                    )
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

    override fun onStop() {
        super.onStop()
        songsViewModel.releaseMediaControllerFuture()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            stopService(Intent(this, AmuzicPlayerService::class.java))
            songsViewModel.onExit()
        }
    }

    private fun launchPermRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return
        }
        permissionLauncher.launch(
            Manifest.permission.READ_MEDIA_AUDIO
        )
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
