package com.infbyte.amuzic.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.infbyte.amuze.ads.GoogleMobileAdsConsentManager
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
import com.infbyte.amuzic.ui.dialogs.AppSettingsRedirectDialog
import com.infbyte.amuzic.ui.dialogs.PrivacyPolicyDialog
import com.infbyte.amuzic.ui.screens.ArtistOrAlbumSongsScreen
import com.infbyte.amuzic.ui.screens.MainScreen
import com.infbyte.amuzic.ui.screens.PlayBar
import com.infbyte.amuzic.ui.screens.PlayListScreen
import com.infbyte.amuzic.ui.screens.Screens
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.ui.views.NotificationBar
import com.infbyte.amuzic.ui.views.PlaylistsBottomSheet
import com.infbyte.amuzic.utils.AmuzicPermissions.isReadPermissionGranted
import com.infbyte.amuzic.utils.AmuzicPermissions.showReqPermRationale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val isMobileAdsInitialized = AtomicBoolean(false)

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private val songsViewModel: SongsViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            songsViewModel.setReadPermGranted(isGranted)
            if (isGranted) {
                songsViewModel.init(this)
                return@registerForActivityResult
            }
        }

    private val appSettingsLauncher =
        registerForActivityResult(
            AppSettingsContract(),
        ) { isGranted ->
            songsViewModel.setReadPermGranted(isGranted)
            if (isGranted) {
                songsViewModel.init(this)
                return@registerForActivityResult
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmuzicTheme {
                LaunchedEffect("") {
                    googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this@MainActivity)

                    googleMobileAdsConsentManager.checkConsent(this@MainActivity) {
                        if (googleMobileAdsConsentManager.canRequestAds) {
                            initMobileAds()
                        }
                    }

                    if (googleMobileAdsConsentManager.canRequestAds) {
                        initMobileAds()
                    }

                    if (!songsViewModel.state.isLoaded) {
                        songsViewModel.setReadPermGranted(isReadPermissionGranted(this@MainActivity))
                        if (!songsViewModel.state.isReadPermGranted) {
                            readBoolean(TERMS_ACCEPTED_KEY) { accepted ->
                                songsViewModel.setTermsAccepted(accepted)
                                if (accepted) {
                                    launchPermRequest()
                                    return@readBoolean
                                }
                                songsViewModel.showPrivacyDialog()
                            }
                        } else {
                            songsViewModel.init(this@MainActivity)
                        }
                    }
                }

                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var initialScreen by rememberSaveable { mutableStateOf(Screens.MAIN) }
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
                            },
                        )
                        return@Surface
                    }

                    if (songsViewModel.sideEffect.showAppSettingsRedirect) {
                        AppSettingsRedirectDialog(
                            onAccept = {
                                songsViewModel.hideAppSettingsRedirect()
                                appSettingsLauncher.launch(packageName)
                            },
                            onDismiss = { songsViewModel.hideAppSettingsRedirect() },
                        )
                    }

                    if (
                        (songsViewModel.state.isReadPermGranted && !songsViewModel.state.isLoaded) ||
                        songsViewModel.state.isRefreshing
                    ) {
                        LoadingScreen(stringResource(R.string.amuzic_preparing))
                        return@Surface
                    }

                    initialScreen =
                        when {
                            !songsViewModel.state.isReadPermGranted -> {
                                Screens.NO_PERMISSION
                            }

                            !songsViewModel.state.hasMusic -> {
                                Screens.NO_MEDIA
                            }
                            else -> Screens.MAIN
                        }

                    NavHost(navController, startDestination = initialScreen) {
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
                                        adsConsentManager = googleMobileAdsConsentManager,
                                    )
                                },
                            )
                        }
                        composable(Screens.SONGS) {
                            ArtistOrAlbumSongsScreen(
                                songsViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                            )
                        }
                        composable(Screens.ABOUT) {
                            AboutScreen(
                                stringResource(R.string.app_name),
                                BuildConfig.VERSION_NAME,
                                R.drawable.ic_amuzic_foreground,
                                R.string.amuzic_privacy_policy_link,
                                adsConsentManager = googleMobileAdsConsentManager,
                            )
                        }
                        composable(Screens.NO_MEDIA) {
                            NoMediaAvailableScreen(
                                R.string.amuzic_no_muzic,
                                onRefresh = {
                                    if (!songsViewModel.state.isReadPermGranted) {
                                        launchPermRequest()
                                    } else {
                                        songsViewModel.setIsRefreshing(true)
                                        songsViewModel.init(this@MainActivity)
                                    }
                                },
                                onExit = { onExit() },
                                aboutApp = { navController.navigate(Screens.ABOUT) },
                            )
                        }
                        composable(Screens.NO_PERMISSION) {
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
                                aboutApp = { navController.navigate(Screens.ABOUT) },
                                onExit = { onExit() },
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
                            onShowPlayListClick = { songsViewModel.onTogglePlayList(true) },
                        )

                        PlayListScreen(
                            show = songsViewModel.state.showPlayList,
                            songs = songsViewModel.state.currentSongs,
                            songsViewModel.state.currentSong,
                            onSongClick = { song ->
                                songsViewModel.onSongClicked(song)
                            },
                        )

                        AnimatedVisibility(
                            songsViewModel.sideEffect.showPlaylists,
                            Modifier.align(Alignment.BottomCenter),
                            enter = expandVertically(expandFrom = Alignment.Bottom),
                        ) {
                            PlaylistsBottomSheet(
                                songsViewModel.state.playlists,
                                onAddPlaylist = { name ->
                                    if (name.isNotEmpty()) {
                                        songsViewModel.enableSelecting()
                                        songsViewModel.updateNewPlaylist(name)
                                        songsViewModel.hidePlaylists()
                                    }
                                },
                                onClickPlaylist = { list ->
                                    songsViewModel.onPlaylistClicked(list)
                                },
                                onDeletePlaylist = { list ->
                                    songsViewModel.onDeletePlaylist(list)
                                },
                            ) { songsViewModel.hidePlaylists() }
                        }
                    }

                    NotificationBar(songsViewModel.sideEffect.notificationMessage)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            songsViewModel.onExit()
            stopService(Intent(this, AmuzicPlayerService::class.java))
        }
    }

    private fun launchPermRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            return
        }
        permissionLauncher.launch(
            Manifest.permission.READ_MEDIA_AUDIO,
        )
    }

    private fun initMobileAds() {
        if (isMobileAdsInitialized.getAndSet(true)) return
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity)
        }
    }

    private fun onExit() {
        if (!songsViewModel.confirmExit) {
            Toast.makeText(
                this,
                getString(R.string.amuzic_confirm_exit),
                Toast.LENGTH_SHORT,
            )
                .show()
            songsViewModel.confirmExit()
        } else {
            finish()
        }
    }
}
