package it.unibo.gamelibrary.ui.views.Home.UserReview

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudalim.compose_rating_bar.RatingBarView
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.ui.common.Game.GameArtwork
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.views.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.destinations.ProfileDestination

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserReview(
    review: LibraryEntry,
    navigator: DestinationsNavigator,
    showUser: Boolean = true,
    viewModel: UserReviewViewModel = hiltViewModel(),
) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {
        viewModel.getGame(review.gameId)
        viewModel.getUser(review.uid)

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            if (showUser) {
                Row( //user image and username
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { navigator.navigate(ProfileDestination(review.uid)) },
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.user[review.uid]?.image != null) {
                        GlideImage(
                            {
                                Uri.parse(viewModel.user[review.uid]?.image)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    } else {
                        Image(
                            Icons.Outlined.AccountCircle,
                            "profile image is not set",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = viewModel.user[review.uid]?.username ?: "loading...",
                    )
                }
            }
            //game name
            Text(
                text = viewModel.game[review.gameId]?.name ?: "",
                modifier = Modifier.padding(4.dp),
                fontWeight = FontWeight.Bold
            )

            //game image
            if (viewModel.game[review.gameId] != null) {
                GameArtwork(
                    viewModel.game[review.gameId]!!,
                    "",
                    Modifier
                        .height(200.dp)
                        .padding(8.dp)
                        .combinedClickable(
                            onClick = { navigator.navigate(GameViewNavDestination(review.gameId)) },
                        )
                        .clip(RoundedCornerShape(8.dp)),
                )
            }

            var rating = remember { mutableIntStateOf(review.rating ?: 0) }
            RatingBarView(
                rating = rating,
                isRatingEditable = false,
                isViewAnimated = false,
                starIcon = rememberVectorPainter(Icons.Rounded.Star),
                ratedStarsColor = MaterialTheme.colorScheme.primary,
                numberOfStars = 10,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                starSize = 26.dp
            )
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = review.notes ?: "")
            }
        }
    }
}