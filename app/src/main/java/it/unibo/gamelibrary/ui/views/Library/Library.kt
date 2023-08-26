package it.unibo.gamelibrary.ui.views.Library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.views.destinations.GameViewNavDestination
import it.unibo.gamelibrary.utils.TopAppBarState
import me.vponomarenko.compose.shimmer.shimmer
import proto.Game

@Destination
@Composable
fun Library(viewModel: LibraryViewModel = hiltViewModel(), navController: NavController) {
    TopAppBarState.title = "Library"
    LibraryContent(viewModel, navController)
}

@Composable
fun LibraryContent(viewModel: LibraryViewModel, navController: NavController) {
    val state = rememberLazyGridState()
    var modifier: Modifier = Modifier;

    if (viewModel.loading) {
        modifier = modifier.shimmer()
    }

    if (viewModel.libraryEntries.isEmpty()) {
        Text(
            text = "No games in your library",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = state,
        modifier = modifier
    ) {
        items(viewModel.libraryEntries, key = { it.id }) {
            val game = viewModel.games[it.gameId.toLong()] ?: Game.getDefaultInstance()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        navController.navigate(
                            GameViewNavDestination(it.gameId)
                        )
                    }
            ) {
                GameCoverImage(
                    game = game,
                    modifier = Modifier.height(175.dp),
                    status = it.status
                )
                Text(
                    text = game.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}