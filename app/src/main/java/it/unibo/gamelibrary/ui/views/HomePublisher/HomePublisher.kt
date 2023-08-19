package it.unibo.gamelibrary.ui.views.HomePublisher

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import it.unibo.gamelibrary.ui.views.Home.HomeSection
import it.unibo.gamelibrary.ui.views.Home.UserReview.UserReview
import it.unibo.gamelibrary.utils.TopAppBarState

@Composable
fun HomePublisher(
    navigator: DestinationsNavigator,
    viewModel: HomePublisherViewModel = hiltViewModel()
){
    TopAppBarState.actions = { }
    TopAppBarState.title = "Home"

    viewModel.fetchGamesAndPosts()
    LazyColumn {
        item {
            HomeSection(
                title = "My games",
                viewModel.games,
                navigator
            )
        }

        items(
            viewModel.posts,
            key = { it.id })
        {
            UserReview(it, navigator)
        }
    }
}