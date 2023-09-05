package it.unibo.gamelibrary

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Games
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alorma.compose.settings.storage.base.getValue
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreIntSettingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import dagger.hilt.android.AndroidEntryPoint
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.NavGraphs
import it.unibo.gamelibrary.ui.appCurrentDestinationAsState
import it.unibo.gamelibrary.ui.destinations.Destination
import it.unibo.gamelibrary.ui.destinations.HomeDestination
import it.unibo.gamelibrary.ui.destinations.LibraryDestination
import it.unibo.gamelibrary.ui.destinations.LoginPageDestination
import it.unibo.gamelibrary.ui.destinations.ProfileDestination
import it.unibo.gamelibrary.ui.destinations.SettingsPageDestination
import it.unibo.gamelibrary.ui.destinations.SignupPageDestination
import it.unibo.gamelibrary.ui.startAppDestination
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme
import it.unibo.gamelibrary.ui.views.biometricLock.BiometricLockScreen
import it.unibo.gamelibrary.utils.BottomBar
import it.unibo.gamelibrary.utils.Http
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.ScaffoldFab
import it.unibo.gamelibrary.utils.ScaffoldFabPosition
import it.unibo.gamelibrary.utils.TopAppBarState
import it.unibo.gamelibrary.utils.channel_id
import it.unibo.gamelibrary.utils.snackBarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.IgdbClient
import ru.pixnews.igdbclient.ktor.IgdbKtorEngine
import java.util.UUID

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    val auth: FirebaseAuth = Firebase.auth

    private val secrets = Secrets()
    private var currentDestination: NavDestination? = null

    private val navControllerListener =
        NavController.OnDestinationChangedListener { controller, destination, _ ->
            if (controller.currentDestination?.route !== currentDestination?.route) {
                currentDestination = destination
                TopAppBarState.restoreDefaults()
                BottomBar = {}
                ScaffoldFab = {}
            }
        }
    private lateinit var userRepository: UserRepository
    private var user by mutableStateOf<User?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(
            (application as GameLibraryApplication).database.userDao()
        )

        IGDBClient = IgdbClient(IgdbKtorEngine) {
            twitchAuth {
                clientId = secrets.getIGDBClientId(packageName)
                clientSecret = secrets.getIGDBClientSecret(packageName)
                storage = TwitchTokenSecurePreferencesStorage(this@MainActivity)
            }
            httpClient {
                httpClient = Http
                backgroundDispatcher = Dispatchers.IO
            }
        }

        auth.addAuthStateListener {
            setCurrentUser(it)
        }

        setCurrentUser(auth)

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

                navController.removeOnDestinationChangedListener(navControllerListener)
                navController.addOnDestinationChangedListener(navControllerListener)

                val biometricLockEnabled by rememberPreferenceDataStoreBooleanSettingState(
                    key = "biometric",
                    defaultValue = false
                )
                val locked = rememberSaveable { mutableStateOf(biometricLockEnabled) }

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_CREATE -> {
                                locked.value = true
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
                    val isLocked = auth.currentUser !== null && biometricLockEnabled && locked.value
                    if (isLocked) {
                        BiometricLockScreen(locked = locked)
                    }

                    AnimatedVisibility(visible = !isLocked) {
                        Scaffold(
                            snackbarHost = {
                                SnackbarHost(snackBarHostState)
                            },
                            bottomBar = {
                                if (currentDestination in NavBarDestinations.entries
                                        .map { it.direction }
                                ) {
                                    NavBar(navController = navController)
                                    BottomBar = {}
                                } else {
                                    BottomBar()
                                }
                            },
                            topBar = {
                                if (TopAppBarState.show && (currentDestination != SignupPageDestination && currentDestination != LoginPageDestination)) {
                                    TopBar(
                                        canNavigateBack = navController.previousBackStackEntry != null && !NavBarDestinations.entries
                                            .map { it.direction }.contains(currentDestination),
                                        navigateUp = { navController.navigateUp() }
                                    )
                                }
                            },
                            floatingActionButton = {
                                ScaffoldFab()
                            },
                            floatingActionButtonPosition = ScaffoldFabPosition
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it)
                            ) {
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(
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
        Library(
            LibraryDestination,
            Icons.Default.Games,
            R.string.bottom_bar_library_label
        ),
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
        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        NavigationBar {
            for (destination in NavBarDestinations.entries) {
                if (destination != NavBarDestinations.Library || user?.isPublisher == false) {
                    val isCurrentDestOnBackStack =
                        navController.isRouteOnBackStack(destination.direction)
                    val userIdArgument = currentBackStackEntry?.arguments?.getString("userID")
                    NavigationBarItem(
                        selected = currentDestination == destination.direction && when (destination) {
                            NavBarDestinations.Profile -> {
                                userIdArgument === null || userIdArgument == auth.currentUser?.uid
                            }
                            else -> true
                        },
                        onClick = {
                            if (isCurrentDestOnBackStack && when (destination) {
                                    NavBarDestinations.Profile -> {
                                        userIdArgument === null || userIdArgument === auth.currentUser?.uid
                                    }
                                    else -> true
                                }
                            ) {
                                // When we click again on a bottom bar item and it was already selected
                                // we want to pop the back stack until the initial destination of this bottom bar item
                                navController.popBackStack(destination.direction, false)
                                return@NavigationBarItem
                            }

                            navController.navigate(destination.direction.route) {
                                // Pop up to the root of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(NavGraphs.root) {
                                    saveState = true
                                }

                                // Avoid multiple copies of the same destination when
                                // reSelecting the same item
                                launchSingleTop = true
                                // Restore state when reSelecting a previously selected item
                                restoreState = true
                            }
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        channel_id = UUID.randomUUID().toString()
        val name = "Game notification"
        val descriptionText = "Get notification when your game is published"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channel_id, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setCurrentUser(auth: FirebaseAuth) {
        CoroutineScope(Dispatchers.Main).launch {
            val uid = auth.currentUser?.uid
            user = if (uid != null) userRepository.getUserByUid(uid).first() else null
        }
    }

}
