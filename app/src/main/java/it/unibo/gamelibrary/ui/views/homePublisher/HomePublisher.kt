package it.unibo.gamelibrary.ui.views.homePublisher

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import it.unibo.gamelibrary.ui.views.home.HomeSection
import it.unibo.gamelibrary.ui.views.home.userReview.UserReview
import it.unibo.gamelibrary.utils.TopAppBarState

@Composable
fun HomePublisher(
    navController: NavController,
    viewModel: HomePublisherViewModel = hiltViewModel()
) {
    TopAppBarState.actions = { }
    TopAppBarState.title = "Home"

    LazyColumn {
        item {
            HomeSection(
                title = "My games",
                viewModel.games,
                navController
            )
        }

        items(
            viewModel.posts,
            key = { it.id })
        {
            UserReview(it, navController)
        }
    }
}