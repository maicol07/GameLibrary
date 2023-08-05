package it.unibo.gamelibrary

import SecurePreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreIntSettingState
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import dagger.hilt.android.AndroidEntryPoint
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme
import it.unibo.gamelibrary.ui.views.NavGraphs
import it.unibo.gamelibrary.ui.views.appCurrentDestinationAsState
import it.unibo.gamelibrary.ui.views.destinations.*
import it.unibo.gamelibrary.ui.views.startAppDestination
import it.unibo.gamelibrary.utils.BottomBar
import it.unibo.gamelibrary.utils.TopAppBarState
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val auth: FirebaseAuth = Firebase.auth

    // Just a state to trigger recomposition
    private val loggedIn = mutableStateOf(false)
    private val secrets = Secrets()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start coroutine to get token from Twitch
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = SecurePreferences(this@MainActivity)
            var token = prefs.getString("token", "")
            if (prefs.getString("token", "") == "") {
                Log.i("TwitchAuthenticator", "Local Token is null")
                //in a real application it is better to use twitchAuthenticator only once, serverside.
                val twitchToken = TwitchAuthenticator.requestTwitchToken(
                    secrets.getIGDBClientId(packageName),
                    secrets.getIGDBClientSecret(packageName)
                )

                // The instance stores the token in the object until a new one is requested
                if (twitchToken != null) {
                    prefs.putString("token", twitchToken.access_token)
                    token = twitchToken.access_token
                } else {
                    Log.e("TwitchAuthenticator", "Token is null")
                }
            }
            IGDBWrapper.setCredentials(secrets.getIGDBClientId(packageName), token)
        }

        setContent {
            val theme = rememberPreferenceDataStoreIntSettingState(key = "dark theme", defaultValue = 0)
            val darkTheme = when (theme.value) {
                0 -> isSystemInDarkTheme()
                1 -> true
                else -> false
            }
            GameLibraryTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                navController.addOnDestinationChangedListener { controller, destination, arguments ->
                    TopAppBarState.title = "";
                    TopAppBarState.customTitle = null
                    TopAppBarState.actions = {}
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
                            if (currentDestination in NavBarDestinations.values().map { it.direction }) {
                                NavBar(navController = navController)
                                BottomBar = {}
                            } else {
                                BottomBar()
                            }
                        },
                        topBar = {
                            if (currentDestination != SignupPageDestination && currentDestination != LoginPageDestination) {
                                TopBar(
                                    currentScreen = "Game Library",
                                    canNavigateBack = navController.previousBackStackEntry != null && currentDestination != HomeDestination,
                                    navigateUp = { navController.navigateUp() }
                                )
                            }
                        })
                    {
                        Column(modifier = Modifier.fillMaxSize().padding(it)) {
                            DestinationsNavHost(
                                navController = navController,
                                navGraph = NavGraphs.root,
                                startRoute = if (auth.currentUser === null) LoginPageDestination else HomeDestination
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
            title = TopAppBarState.customTitle ?: { Text(text = TopAppBarState.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
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
        Settings(SettingsPageDestination, Icons.Default.Settings, R.string.bottom_bar_settings_label),
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
}
