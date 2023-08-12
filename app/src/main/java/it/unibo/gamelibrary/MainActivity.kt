package it.unibo.gamelibrary

import SecurePreferences
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.alorma.compose.settings.storage.base.getValue
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreIntSettingState
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import dagger.hilt.android.AndroidEntryPoint
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme
import it.unibo.gamelibrary.ui.views.NavGraphs
import it.unibo.gamelibrary.ui.views.appCurrentDestinationAsState
import it.unibo.gamelibrary.ui.views.destinations.BiometricLockScreenDestination
import it.unibo.gamelibrary.ui.views.destinations.Destination
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.ui.views.destinations.LoginPageDestination
import it.unibo.gamelibrary.ui.views.destinations.ProfileDestination
import it.unibo.gamelibrary.ui.views.destinations.SettingsPageDestination
import it.unibo.gamelibrary.ui.views.destinations.SignupPageDestination
import it.unibo.gamelibrary.ui.views.startAppDestination
import it.unibo.gamelibrary.utils.BottomBar
import it.unibo.gamelibrary.utils.TopAppBarState
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant


@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    val auth: FirebaseAuth = Firebase.auth

    private val secrets = Secrets()
    private var biometricStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start coroutine to get token from Twitch
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = SecurePreferences(this@MainActivity)
            var token = prefs.getString("twitch_token", "")
            val tokenExpiration = prefs.getString("twitch_token_expiration", "")
            if (token == "" || tokenExpiration == "" || Instant.parse(tokenExpiration)
                    .isBefore(Instant.now())
            ) {
                Log.i("TwitchAuthenticator", "Local Token is null")
                //in a real application it is better to use twitchAuthenticator only once, serverside.
                val twitchToken = TwitchAuthenticator.requestTwitchToken(
                    secrets.getIGDBClientId(packageName),
                    secrets.getIGDBClientSecret(packageName)
                )

                // The instance stores the token in the object until a new one is requested
                if (twitchToken != null) {
                    prefs.putString("twitch_token", twitchToken.access_token)
                    prefs.putString(
                        "twitch_token_expiration",
                        Instant.now().plusSeconds(twitchToken.expires_in).toString()
                    )
                    token = twitchToken.access_token
                    Log.i("TwitchAuthenticator", "Got new token")
                } else {
                    Log.e("TwitchAuthenticator", "Token is null")
                }
            }
            IGDBWrapper.setCredentials(secrets.getIGDBClientId(packageName), token)
        }
        createNotificationChannel()

        setContent {
            val theme =
                rememberPreferenceDataStoreIntSettingState(key = "dark theme", defaultValue = 0)
            val darkTheme = when (theme.value) {
                0 -> isSystemInDarkTheme()
                1 -> true
                else -> false
            }
            GameLibraryTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                navController.addOnDestinationChangedListener { controller, destination, arguments ->
                    TopAppBarState.title = ""
                    TopAppBarState.customTitle = null
                    TopAppBarState.actions = {}
                    TopAppBarState.hide = false
                }

                var startRoute =
                    if (auth.currentUser === null) LoginPageDestination else HomeDestination

                val biometricLockEnabled by rememberPreferenceDataStoreBooleanSettingState(
                    key = "biometric",
                    defaultValue = false
                )
                if (biometricLockEnabled && !biometricStarted) {
                    startRoute = BiometricLockScreenDestination
                    biometricStarted = true
                }

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    var locked = false
                    val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> {
                                locked = true
                            }

                            Lifecycle.Event.ON_CREATE -> {
                                locked = true
                            }

                            Lifecycle.Event.ON_RESUME -> {
                                if (biometricLockEnabled && locked && biometricStarted) {
                                    try {
                                        navController.navigate(BiometricLockScreenDestination) {
                                            if (navController.currentDestination?.route != null) {
                                                popUpTo(navController.currentDestination!!.route!!) {
                                                    inclusive = true
                                                }
                                            }
                                            locked = false
                                            biometricStarted = false
                                        }
                                    } catch (exception: IllegalArgumentException) {
                                        // Do nothing (navigation graph not initialized)
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
                    }
                }

                val currentDestination: Destination =
                    navController.appCurrentDestinationAsState().value
                        ?: NavGraphs.root.startAppDestination
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(snackbarHostState)
                        },
                        bottomBar = {
                            if (currentDestination in NavBarDestinations.values()
                                    .map { it.direction }
                            ) {
                                NavBar(navController = navController)
                                BottomBar = {}
                            } else {
                                BottomBar()
                            }
                        },
                        topBar = {
                            if (TopAppBarState.hide || (currentDestination != SignupPageDestination && currentDestination != LoginPageDestination)) {
                                TopBar(
                                    currentScreen = "Game Library",
                                    canNavigateBack = navController.previousBackStackEntry != null && !NavBarDestinations.values()
                                        .map { it.direction }.contains(currentDestination),
                                    navigateUp = { navController.navigateUp() }
                                )
                            }
                        })
                    {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {
                            DestinationsNavHost(
                                navController = navController,
                                navGraph = NavGraphs.root,
                                startRoute = startRoute
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(//TopAppBar
        currentScreen: String,
        canNavigateBack: Boolean,
        navigateUp: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        TopAppBar(
            title = TopAppBarState.customTitle ?: {
                Text(
                    text = TopAppBarState.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = modifier,
            navigationIcon = {
                //se si può navigare indietro (non home screen) allora appare la freccetta
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back button"
                        )
                    }
                }
            },
            actions = TopAppBarState.actions
        )
    }

    enum class NavBarDestinations(
        val direction: Destination,
        val icon: ImageVector,
        @StringRes val label: Int
    ) {
        Home(HomeDestination, Icons.Default.Home, R.string.bottom_bar_home_label),
        Profile(ProfileDestination, Icons.Default.AccountCircle, R.string.bottom_bar_profile_label),
        Settings(
            SettingsPageDestination,
            Icons.Default.Settings,
            R.string.bottom_bar_settings_label
        ),
    }

    @Composable
    fun NavBar(
        navController: NavController
    ) {
        val currentDestination: Destination = (navController.currentDestinationAsState().value
            ?: NavGraphs.root.startAppDestination) as Destination

        NavigationBar {
            NavBarDestinations.values().forEach { destination ->
                NavigationBarItem(
                    selected = currentDestination == destination.direction,
                    onClick = {
                        navController.navigate(
                            destination.direction.route,
                            fun NavOptionsBuilder.() {
                                launchSingleTop = true
                            })
                    },
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = stringResource(destination.label)
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Prova"
            val descriptionText = "Questa è una prova"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channel_id", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
