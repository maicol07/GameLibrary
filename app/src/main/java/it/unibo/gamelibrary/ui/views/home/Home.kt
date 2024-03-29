package it.unibo.gamelibrary.ui.views.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.fade
import io.github.fornewid.placeholder.material3.placeholder
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.ui.common.components.NoInternetConnection
import it.unibo.gamelibrary.ui.common.components.checkInternetConnection
import it.unibo.gamelibrary.ui.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.home.search.SearchBar
import it.unibo.gamelibrary.ui.views.home.userReview.UserReview
import it.unibo.gamelibrary.ui.views.homePublisher.HomePublisher
import it.unibo.gamelibrary.utils.TopAppBarState
import ru.pixnews.igdbclient.model.IgdbImageSize
import ru.pixnews.igdbclient.util.igdbImageUrl

@RootNavGraph(start = true)
@Destination
@Composable
fun Home(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    TopAppBarState.show = false
    Column {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SearchBar(navController)
        }
        if (checkInternetConnection(LocalContext.current)) {
            Spacer(modifier = Modifier.size(8.dp))
            if (Firebase.auth.currentUser != null) {
                if (viewModel.user == null) {
                    viewModel.getUser()
                } else {
                    if (viewModel.user?.isPublisher == true) {
                        HomePublisher(navController = navController)
                    } else {
                        LazyColumn {
                            item {
                                HomeSection(
                                    title = "Popular Games",
                                    viewModel.popularGames,
                                    navController
                                )
                                Spacer(Modifier.size(8.dp))
                                HomeSection(
                                    title = "Most Loved Games",
                                    viewModel.mostLovedGames,
                                    navController
                                )
                                Spacer(Modifier.size(8.dp))
                                HomeSection(
                                    title = "Recently Released Games",
                                    viewModel.newGames,
                                    navController
                                )
                                Spacer(Modifier.size(8.dp))
                                HomeSection(
                                    title = "Upcoming Games",
                                    viewModel.upcomingGames,
                                    navController
                                )
                            }

                            items(
                                viewModel.posts.filter { entry: LibraryEntry -> entry.uid != viewModel.user?.uid },
                                key = { it.id })
                            {
                                UserReview(it, navController, showUser = true)
                            }
                        }
                    }
                }
            }
        } else {
            NoInternetConnection()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeSection(
    title: String,
    list: MutableList<ru.pixnews.igdbclient.model.Game>,
    navController: NavController
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
        LazyRow {

            val loadingList: MutableList<ru.pixnews.igdbclient.model.Game> = mutableListOf()
            for(i in 1..5){
                loadingList.add(ru.pixnews.igdbclient.model.Game())
            }


            items(items = if(list.isEmpty()){loadingList}else{list}) { game ->

                Column {
                    CoilImage(
                        imageModel = {
                            if (game.cover != null) igdbImageUrl(
                                game.cover!!.image_id,
                                IgdbImageSize.COVER_BIG
                            ) else R.drawable.no_image
                        },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center
                        ),
                        previewPlaceholder = R.drawable.ffviirebirth,
                        modifier = Modifier
                            .width(150.dp)
                            .height(200.dp)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .placeholder(visible = list.isEmpty(), highlight = PlaceholderHighlight.fade())
                            .combinedClickable(
                                onClick = { navController.navigate(GameViewNavDestination(gameId = game.id.toInt())) },
                            ),
                    )
                    Text(
                        text = game.name,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(153.dp)
                            .padding(start = 5.dp, top = 2.dp)
                            .placeholder(visible = list.isEmpty(), highlight = PlaceholderHighlight.fade()),
                        maxLines = 1
                    )
                }
            }
        }
    }
}