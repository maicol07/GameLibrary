package it.unibo.gamelibrary

import SecurePreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
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
import it.unibo.gamelibrary.ui.views.destinations.Destination
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.ui.views.destinations.ProfileDestination
import it.unibo.gamelibrary.ui.views.destinations.SignupPageDestination
import it.unibo.gamelibrary.ui.views.startAppDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    public val auth: FirebaseAuth = Firebase.auth

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
                Log.i("TwitchAuthenticator", "Token is null")
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
            Log.i("TwitchAuthenticator", "Token is $token")
        }

        setContent {
            GameLibraryTheme {
                val navController = rememberNavController()
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
                            //if (currentDestination != SignupPageDestination) {  // currentDestination != LoginPageDestination
                                BottomBar(navController = navController)
                            //}
                        },
                        topBar = {
                            if (currentDestination != SignupPageDestination) {  // currentDestination != LoginPageDestination
                                TopBar(
                                    currentScreen = "Game Library",
                                    canNavigateBack = navController.previousBackStackEntry != null,
                                    navigateUp = { navController.navigateUp() }
                                )
                            }
                        })
                    {
                        Column(modifier = Modifier.padding(it)) {
                            Log.i("user id", auth.currentUser?.uid ?: "utente non loggato")
                            DestinationsNavHost(
                                navController = navController,
                                navGraph = NavGraphs.root,
                                startRoute = if (auth.currentUser === null) SignupPageDestination else HomeDestination
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
            title = { Text(currentScreen) },
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
            }
        )
    }

    enum class NavBarDestinations(
        val direction: Destination,
        val icon: ImageVector,
        @StringRes val label: Int
    ) {
        Home(HomeDestination, Icons.Default.Home, R.string.bottom_bar_home_label),
        Profile(ProfileDestination, Icons.Default.AccountCircle, R.string.bottom_bar_profile_label)
    }

    @Composable
    fun BottomBar(
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
