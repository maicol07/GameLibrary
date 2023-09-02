package it.unibo.gamelibrary.ui.views.GameView

import android.Manifest
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alorma.compose.settings.storage.datastore.GenericPreferenceDataStoreSettingValueState
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mahmoudalim.compose_rating_bar.RatingBarView
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.fade
import io.github.fornewid.placeholder.material3.placeholder
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.ui.common.Game.GameArtwork
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.common.Game.GameScreenshot
import it.unibo.gamelibrary.ui.common.Game.icon
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.common.components.NoInternetConnection
import it.unibo.gamelibrary.ui.common.components.checkInternetConnection
import it.unibo.gamelibrary.ui.views.GameView.preview.GameParameterProvider
import it.unibo.gamelibrary.ui.views.Home.UserReview.UserReview
import it.unibo.gamelibrary.utils.BottomBar
import it.unibo.gamelibrary.utils.TopAppBarState
import ru.pixnews.igdbclient.model.Game

private val dateFormatter = SimpleDateFormat.getDateInstance()
private lateinit var notShowAgain: GenericPreferenceDataStoreSettingValueState<Boolean>

@Destination(
    deepLinks = [
        DeepLink(
            uriPattern = "https://game-library.app/game/{gameId}"
        ),
        DeepLink(
            uriPattern = "app://game-library/game/{gameId}"
        )
    ]
)
@Composable
fun GameViewNav(gameId: Int, viewModel: GameViewViewModel = hiltViewModel(), navigator: DestinationsNavigator) {
    if (checkInternetConnection(LocalContext.current)) {
        TopAppBarState.title = "Loading..."
        var modifier: Modifier = Modifier
        if (viewModel.game == null) {
            viewModel.getGame(gameId)
            viewModel.getLibraryEntries(gameId)
            modifier = modifier
                .fillMaxWidth()
        }
        GameView(game = (viewModel.game ?: Game()), modifier, navigator = navigator)
        notShowAgain = rememberPreferenceDataStoreBooleanSettingState(
            key = "notShowAgain",
            defaultValue = false
        )
        Log.i("NotShowAgain", notShowAgain.value.toString())
        if (!notShowAgain.value && viewModel.openNotificationDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationPermissionDialog()
        }
    } else {
        NoInternetConnection()
    }
}

@Composable
//@Preview
fun GameView(
    @PreviewParameter(GameParameterProvider::class) game: Game,
    modifier: Modifier = Modifier,
    viewModel: GameViewViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    TopAppBarState.title = game.name
    BottomBar = {
        GameViewBottomBar(viewModel)
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        GameHeader(game, isLoading = viewModel.isLoading)
        GameDetails(game, isLoading = viewModel.isLoading, libraryEntries = viewModel.libraryEntries, navigator = navigator)
    }
    if (viewModel.isGameLibraryEditOpen) {
        GameViewGameLibraryEditDialog(game)
    }
}

@Composable
fun GameHeader(game: Game, modifier: Modifier = Modifier, isLoading: Boolean = false) {
    Box(modifier) {
        val backgroundModifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
        if (game.artworks.isNotEmpty()) {
            GameArtwork(game, "", backgroundModifier)
        } else {
            GameScreenshot(game, "", backgroundModifier)
        }
        Row(
            Modifier
                .align(BottomStart)
                .offset(12.dp, 60.dp), //apply an offset
        ) {
            GameCoverImage(
                game,
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()),
                contentDescription = "${game.name} cover",
                fullscreenable = true
            )
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .offset(8.dp, 100.dp)
                    .padding(end = 8.dp)
                    .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun GameDetails(
    game: Game,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    libraryEntries: List<LibraryEntry> = listOf(),
    navigator: DestinationsNavigator
) {
    Column(modifier.padding(16.dp, 65.dp, 16.dp, 0.dp)) {
        LazyRow {
            items(game.involved_companies, key = { it.id }) {
                val roles = mapOf(
                    "Developer" to it.developer,
                    "Publisher" to it.publisher,
                    "Porting" to it.porting,
                    "Supporting" to it.supporting
                )
                val icon: Any = when {
                    it.porting -> Icons.Default.Business
                    it.developer -> Icons.Default.Code
                    it.publisher -> Icons.Default.Business
                    it.supporting -> R.drawable.lifebuoy
                    else -> Icons.Default.Business
                }
                AssistChip(
                    label = {
                        Text(
                            text = "${it.company?.name} (${
                                roles.filter { it.value }.keys.joinToString(
                                    ", "
                                )
                            })"
                        )
                    },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()),
                    leadingIcon = {
                        if (icon is ImageVector) {
                            Icon(
                                icon,
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                painterResource(icon as Int),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = { /* TODO: Publisher page */ })
            }
        }
        Text(text = "Platforms", style = MaterialTheme.typography.headlineSmall)
        if (game.release_dates.isEmpty()) {
            Text(
                text = "Game hasn't been released yet",
                modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
            )
            // TODO: Get future platforms
        }
        LazyRow {
            items(game.release_dates) {
                AssistChip(
                    label = { Text(text = "${it.platform?.name} (${it.human})") },
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { /*TODO*/ },
                    leadingIcon = {
                        if (it.platform?.platform_logo != null) {
                            CoilImage(
                                imageModel = { "https://${it.platform?.platform_logo!!.url}" },
                                previewPlaceholder = R.drawable.square_enix_logo,
                                imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
                                modifier = Modifier
                                    .size(24.dp, 24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                )
            }
        }
        Text(text = "Genres", style = MaterialTheme.typography.headlineSmall)
        if (game.genres.isEmpty()) {
            Text(
                text = "No genres",
                modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
            )
        }
        LazyRow {
            items(game.genres) {
                val icon: Any? = it.icon
                AssistChip(
                    label = { Text(text = it.name) },
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { /*TODO*/ },
                    leadingIcon = {
                        if (icon is Int) {
                            Icon(
                                painterResource(icon),
                                contentDescription = null
                            )
                        }
                        if (icon is ImageVector) {
                            Icon(
                                icon,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }

        Text(text = game.summary, modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()))

        Text(text = "Screenshots", style = MaterialTheme.typography.headlineSmall)
        if (game.screenshots.isEmpty()) {
            Text(
                text = "No screenshots",
                modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
            )
        }
        LazyRow {
            items(game.screenshots) {
                GameScreenshot(
                    game,
                    it.image_id,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(300.dp, 150.dp)
                        .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
                )
            }
        }

        Text(text = "Artworks", style = MaterialTheme.typography.headlineSmall)
        if (game.artworks.isEmpty()) {
            Text(
                text = "No artworks",
                modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
            )
        }
        LazyRow {
            items(game.artworks) {
                GameArtwork(
                    game,
                    it.image_id,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(300.dp, 150.dp)
                        .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
                )
            }
        }

//        Text(text = "Other info", style = MaterialTheme.typography.headlineSmall)

        Text(text = "Users reviews", style = MaterialTheme.typography.headlineSmall)
        if (libraryEntries.isEmpty()) {
            Text(
                text = "No reviews",
                modifier = Modifier.placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
            )
        }
        LazyRow {
            items(libraryEntries) {
                UserReview(review = it, navigator = navigator)
            }
        }
    }
}

@Composable
fun GameViewBottomBar(viewModel: GameViewViewModel) {
    val context = LocalContext.current
    BottomAppBar(
        actions = {
            IconButton(onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "https://game-library.app/game/${viewModel.game?.id}"
                    )
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share"
                )
            }

            for (status in LibraryEntryStatus.entries) {
                IconToggleButton(
                    checked = viewModel.libraryEntry.status === status,
                    onCheckedChange = {
                        if (it) {
                            viewModel.libraryEntry.status = status
                            viewModel.saveGameToLibrary()
                        } else {
                            viewModel.removeGameFromLibrary()
                        }
                    }) {
                    Icon(
                        if (viewModel.libraryEntry.status === status) status.selectedIcon else status.unselectedIcon,
                        contentDescription = status.text,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.isGameLibraryEditOpen = true },
            ) {
                if (viewModel.libraryEntry.entry === null) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add to library"
                    )
                } else {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit library entry"
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameViewGameLibraryEditDialog(game: Game, viewModel: GameViewViewModel = hiltViewModel()) {
    CustomDialog(
        onDismissRequest = { viewModel.isGameLibraryEditOpen = false },
        title = { Text(text = if (viewModel.libraryEntry.entry !== null) "Edit ${game.name} library entry" else "Add ${game.name} to library") },
        buttons = {
            TextButton(onClick = { viewModel.isGameLibraryEditOpen = false }) {
                Text(text = "Cancel")
            }
            if (viewModel.libraryEntry.entry !== null) {
                TextButton(
                    onClick = {
                        viewModel.removeGameFromLibrary()
                        viewModel.isGameLibraryEditOpen = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Remove")
                }
            }
            TextButton(onClick = {
                viewModel.saveGameToLibrary()
            }) {
                Text(text = if (viewModel.libraryEntry.entry !== null) "Edit" else "Add")
            }
        }
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Status: ")
            SingleChoiceSegmentedButtonRow {
                for ((index, status) in LibraryEntryStatus.entries.withIndex()) {
                    SegmentedButton(
                        shape = RoundedCornerShape(
                            if (index == 0) 16.dp else 0.dp,
                            if (index == LibraryEntryStatus.entries.lastIndex) 16.dp else 0.dp,
                            if (index == LibraryEntryStatus.entries.lastIndex) 16.dp else 0.dp,
                            if (index == 0) 16.dp else 0.dp
                        ),
                        selected = viewModel.libraryEntry.status == status,
                        onClick = { viewModel.libraryEntry.status = status },
                        icon = {
                            SegmentedButtonDefaults.SegmentedButtonIcon(viewModel.libraryEntry.status == status) {
                                Icon(
                                    status.unselectedIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        }
                    ) {
                        Text(text = status.text)
                    }
                }
            }
            val icon = rememberVectorPainter(Icons.Rounded.Star)
            RatingBarView(
                rating = viewModel.libraryEntry.rating,
                isRatingEditable = true,
                isViewAnimated = false,
                starIcon = icon,
                ratedStarsColor = MaterialTheme.colorScheme.primary,
                numberOfStars = 10,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                starSize = 26.dp
            )
            val text = when (viewModel.libraryEntry.rating.intValue) {
                0 -> "No rating"
                1 -> "1/10 - Awful"
                2 -> "2/10 - Bad"
                3 -> "3/10 - Poor"
                4 -> "4/10 - Mediocre"
                5 -> "5/10 - Average"
                6 -> "6/10 - Fine"
                7 -> "7/10 - Good"
                8 -> "8/10 - Great"
                9 -> "9/10 - Superb"
                10 -> "10/10 - Masterpiece"
                else -> ""
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = viewModel.libraryEntry.notes,
                onValueChange = { viewModel.libraryEntry.notes = it },
                singleLine = false,
                label = { Text(text = "Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionDialog(viewModel: GameViewViewModel = hiltViewModel()) {
    val notificationPermissionsState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    if (notificationPermissionsState.status.isGranted) {
        viewModel.openNotificationDialog = false
    } else {
        val textToShow = if (notificationPermissionsState.status.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "The notification is important for this app. Please grant the permission."
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "Notification permission required for this feature to be available. " +
                    "Please grant the permission"
        }
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                notShowAgain.value = viewModel.notShowAgainNotification
                viewModel.openNotificationDialog = false
            },
            title = {
                Text(text = "Permissions required")
            },
            text = {
                Column {
                    Text(text = textToShow)
                    Spacer(modifier = Modifier.size(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Checkbox(
                            checked = viewModel.notShowAgainNotification,
                            onCheckedChange = {
                                viewModel.notShowAgainNotification = !viewModel.notShowAgainNotification
                            }
                        )
                        Text(text = "Don't show again")
                    }
                }
            },
            confirmButton = {
                if (notificationPermissionsState.status.shouldShowRationale) {
                    TextButton(
                        onClick = {
                            notificationPermissionsState.launchPermissionRequest()
                            notShowAgain.value = viewModel.notShowAgainNotification
                            viewModel.openNotificationDialog = false
                        }
                    ) {
                        Text("Request permission")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        notShowAgain.value = viewModel.notShowAgainNotification
                        viewModel.openNotificationDialog = false
                    }
                ) {
                    Text(if (notificationPermissionsState.status.shouldShowRationale) "Dismiss" else "Ok")
                }
            }
        )
    }
}

