package it.unibo.gamelibrary.ui.views.Home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.views.Home.UserReview.UserReview
import it.unibo.gamelibrary.ui.views.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.destinations.LoginPageDestination
import it.unibo.gamelibrary.ui.views.destinations.SignupPageDestination
import it.unibo.gamelibrary.utils.TopAppBarState
import proto.Game

@RootNavGraph(start = true)
@Destination
@Composable
fun Home(
    navigator: DestinationsNavigator,
    viewModel: HomeViewModel = hiltViewModel()
) {
    TopAppBarState.actions = {}
    TopAppBarState.title = "Home"

    LazyColumn() {
        item {
            HomeSection(
                title = "Popular Games",
                viewModel.popularGames,
                navigator
            )
            Spacer(Modifier.size(8.dp))
            HomeSection(
                title = "Most Loved Games",
                viewModel.mostLovedGames,
                navigator
            )
            Spacer(Modifier.size(8.dp))
            HomeSection(
                title = "New Games",
                viewModel.newGames,
                navigator
            )
        }

        items(
            viewModel.posts,
            key = {it.id})
        {
            UserReview(it, navigator, showUser = true)
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeSection(title: String,
                list: MutableList<Game>,
                navigator: DestinationsNavigator,
                viewModel: HomeViewModel = hiltViewModel())
{
    Column() {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
        LazyRow {
            items(list) { game ->
                Column {
                    GlideImage(
                        {
                            if (game.hasCover()) imageBuilder(game.cover.imageId, ImageSize.COVER_BIG, ImageType.PNG) else R.drawable.no_image
                        },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center
                        ),
                        previewPlaceholder = R.drawable.ffviirebirth,
                        modifier = Modifier
                            .size(200.dp, 250.dp)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(100.dp, RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = { navigator.navigate(GameViewNavDestination(gameId = game.id.toInt())) },
                            )
                    )
                    Text(
                        text = game.name,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}



