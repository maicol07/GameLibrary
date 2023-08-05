package it.unibo.gamelibrary.ui.views.Home.UserReview

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudalim.compose_rating_bar.RatingBarView
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.ui.views.destinations.ProfileDestination

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserReview(
    review: LibraryEntry,
    navigator: DestinationsNavigator,
    showUser: Boolean = true,
    viewModel: UserReviewViewModel = hiltViewModel(),
){
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {

        viewModel.getGame(review.gameId)
        viewModel.getUser(review.uid)

        Column(modifier = Modifier.fillMaxWidth()) {
            if(showUser) {
                Row( //user image and username
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { navigator.navigate(ProfileDestination(review.uid)) },
                        )
                ) {
                    if (viewModel.user[review.uid]?.image != null) {
                        GlideImage(
                            {
                                Uri.parse(viewModel.user[review.uid]?.image)
                            },
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                        )
                    } else {
                        Image(
                            Icons.Outlined.AccountCircle,
                            "profile image is not set",
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                        )
                    }
                    Text(
                        text = viewModel.user[review.uid]?.username ?: "loading...",

                    )
                }
            }
            Image(
                imageVector = Icons.Filled.Photo,
                contentDescription = "post main image"
            )

            Text(text = viewModel.game[review.gameId]?.name ?: "")
            var rating = remember { mutableStateOf(review.rating ?: 0) }
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
            Text(text = review.notes ?: "" )
        }
    }
}