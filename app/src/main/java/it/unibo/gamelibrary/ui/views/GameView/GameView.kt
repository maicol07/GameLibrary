package it.unibo.gamelibrary.ui.views.GameView

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.OutlinedFlag
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudalim.compose_rating_bar.RatingBarView
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.ui.common.Game.GameArtwork
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.common.Game.GameScreenshot
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.views.GameView.preview.GameParameterProvider
import it.unibo.gamelibrary.utils.BottomBar
import it.unibo.gamelibrary.utils.TopAppBarState
import me.vponomarenko.compose.shimmer.shimmer
import proto.Game

private val dateFormatter = SimpleDateFormat.getDateInstance()

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
fun GameViewNav(gameId: Int, viewModel: GameViewViewModel = hiltViewModel()) {
    TopAppBarState.title = "Loading..."
    var modifier: Modifier = Modifier
    if (viewModel.game == null) {
        viewModel.getGame(gameId)
        modifier = modifier
            .fillMaxWidth()
            .shimmer()
    }
    GameView(game = (viewModel.game ?: Game.getDefaultInstance()), modifier)
}

@Composable
@Preview
fun GameView(
    @PreviewParameter(GameParameterProvider::class) game: Game,
    modifier: Modifier = Modifier,
    viewModel: GameViewViewModel = hiltViewModel()
) {
    TopAppBarState.title = game.name
    BottomBar = {
        GameViewBottomBar(viewModel)
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        GameHeader(game)
        GameDetails(game)
    }
   if (viewModel.isGameLibraryEditOpen) {
        GameViewGameLibraryEditDialog(game)
    }
}

@Composable
fun GameHeader(game: Game, modifier: Modifier = Modifier) {
    Box(modifier) {
        val backgroundModifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        if (game.artworksCount > 0) {
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
                    .size(100.dp, 150.dp),
                contentDescription = "${game.name} cover",
                fullscreenable = true
            )
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.offset(8.dp, 100.dp).padding(end = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun GameDetails(game: Game, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp, 65.dp, 16.dp, 0.dp)) {
        LazyRow {
            items(game.involvedCompaniesList, key = { it.id }) {
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
                    label = { Text(text = "${it.company.name} (${roles.filter { it.value }.keys.joinToString(", ")})") },
                    modifier = Modifier.padding(end = 8.dp),
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
        if (game.releaseDatesCount == 0) {
            Text(text = "Game hasn't been released yet", style = MaterialTheme.typography.bodySmall)
            // TODO: Get future platforms
        }
        LazyRow {
            items(game.releaseDatesList) {
                AssistChip(
                    label = { Text(text = "${it.platform.name} (${it.human})") },
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { /*TODO*/ },
                    leadingIcon = {
                        if (it.platform.hasPlatformLogo()) {
                            GlideImage(
                                { "https://${it.platform.platformLogo.url}" },
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
        if (game.genresCount == 0) {
            Text(text = "No genres", style = MaterialTheme.typography.bodySmall)
        }
        LazyRow {
            items(game.genresList) {
                val icon: Any? = when (it.slug) {
                    "fighting" -> Icons.Default.SportsMma
                    "shooter" -> R.drawable.pistol
                    "music" -> Icons.Default.MusicNote
                    "platform" -> Icons.Default.ViewInAr // TODO: Change with
                    "puzzle" -> Icons.Default.Extension
                    "racing" -> Icons.Default.SportsMotorsports
                    "real-time-strategy-rts", "strategy", "turn-based-strategy-tbs", "tactical" -> R.drawable.strategy
                    "role-playing-rpg" -> R.drawable.wizard_hat
                    "adventure" -> Icons.Default.Explore
                    "simulator" -> Icons.Default.Diamond
                    "sport" -> Icons.Default.SportsTennis
                    "quiz-trivia" -> Icons.Default.QuestionAnswer
                    "hack-and-slash-beat-em-up" -> R.drawable.fencing
                    "pinball" -> Icons.Default.SportsBaseball
                    "arcade" -> Icons.Default.VideogameAsset
                    "visual-novel" -> Icons.Default.Book
                    "indie" -> Icons.Default.Build
                    "card-board-game" -> Icons.Default.Casino
                    "moba" -> Icons.Default.SportsEsports
                    "point-and-click" -> Icons.Default.Mouse
                    else -> null
                }
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
//        Text(text = "Other info", style = MaterialTheme.typography.headlineSmall)
//        LazyColumn {
//
//        }


        Text(text = game.summary)
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
                    putExtra(Intent.EXTRA_TEXT, "https://game-library.app/game/${viewModel.game?.id}")
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
            IconButton(onClick = {
                viewModel.libraryEntry.status = LibraryEntryStatus.WANTED;
                viewModel.saveGameToLibrary()
            }) {
                Icon(
                    if (viewModel.libraryEntry.status === LibraryEntryStatus.WANTED) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Wanted",
                    tint = if (viewModel.libraryEntry.status === LibraryEntryStatus.WANTED) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                viewModel.libraryEntry.status = LibraryEntryStatus.PLAYING;
                viewModel.saveGameToLibrary()
            }) {
                Icon(
                    if (viewModel.libraryEntry.status === LibraryEntryStatus.PLAYING) Icons.Default.VideogameAsset else Icons.Outlined.VideogameAsset,
                    contentDescription = "Playing",
                    tint = if (viewModel.libraryEntry.status === LibraryEntryStatus.PLAYING) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                viewModel.libraryEntry.status = LibraryEntryStatus.FINISHED;
                viewModel.saveGameToLibrary()
            }) {
                Icon(
                    if (viewModel.libraryEntry.status === LibraryEntryStatus.FINISHED) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                    contentDescription = "Finished",
                    tint = if (viewModel.libraryEntry.status === LibraryEntryStatus.FINISHED) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
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
        title = {Text(text = if (viewModel.libraryEntry.entry !== null) "Edit ${game.name} library entry" else "Add ${game.name} to library")},
        buttons = {
            TextButton(onClick = { viewModel.isGameLibraryEditOpen = false }) {
                Text(text = "Cancel")
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
                SegmentedButton(
                    shape = RoundedCornerShape(16.dp, 0.dp, 0.dp, 16.dp),
                    selected = viewModel.libraryEntry.status == LibraryEntryStatus.WANTED,
                    onClick = { viewModel.libraryEntry.status = LibraryEntryStatus.WANTED },
                    icon = {
                        SegmentedButtonDefaults.SegmentedButtonIcon(viewModel.libraryEntry.status == LibraryEntryStatus.WANTED) {
                            Icon(
                                Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    }
                ) {
                    Text(text = "Wanted")
                }
                SegmentedButton(
                    selected = viewModel.libraryEntry.status == LibraryEntryStatus.PLAYING,
                    onClick = { viewModel.libraryEntry.status = LibraryEntryStatus.PLAYING },
                    icon = {
                        SegmentedButtonDefaults.SegmentedButtonIcon(viewModel.libraryEntry.status == LibraryEntryStatus.PLAYING) {
                            Icon(
                                Icons.Outlined.VideogameAsset,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    }
                ) {
                    Text(text = "Playing")
                }
                SegmentedButton(
                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp),
                    selected = viewModel.libraryEntry.status == LibraryEntryStatus.FINISHED,
                    onClick = { viewModel.libraryEntry.status = LibraryEntryStatus.FINISHED },
                    icon = {
                        SegmentedButtonDefaults.SegmentedButtonIcon(viewModel.libraryEntry.status == LibraryEntryStatus.FINISHED) {
                            Icon(
                                Icons.Outlined.OutlinedFlag,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    }
                ) {
                    Text(text = "Finished")
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
            Text(text = text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))

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