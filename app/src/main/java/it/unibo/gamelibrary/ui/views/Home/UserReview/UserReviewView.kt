package it.unibo.gamelibrary.ui.views.Home.UserReview

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudalim.compose_rating_bar.RatingBarView
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.ui.common.Game.GameArtwork
import it.unibo.gamelibrary.ui.common.Game.GameCoverImage
import it.unibo.gamelibrary.ui.common.components.UserBar
import it.unibo.gamelibrary.ui.views.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.destinations.ProfileDestination

//TODO refactor come da immagine maic
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserReview(
    review: LibraryEntry,
    navigator: DestinationsNavigator,
    showUser: Boolean = true,
    viewModel: UserReviewViewModel = hiltViewModel(),
) {
    Box(){
        Card(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 8.dp, end = 8.dp)
                .wrapContentHeight()
        ) {
            viewModel.getGame(review.gameId)
            viewModel.getUser(review.uid)

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {

                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){

                    Spacer(modifier = Modifier.size(170.dp))//creates space for game Image (below)

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(//game name
                            text = viewModel.game[review.gameId]?.name ?: "",
                            modifier = Modifier.fillMaxWidth().padding(4.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Row (//rating
//                            horizontalArrangement = Arrangement.Center,
//                            modifier = Modifier.fillMaxWidth()
                        ){
                            var rating = remember { mutableIntStateOf(review.rating ?: 0) }
                            Icon(Icons.Outlined.StarBorder, "Star icon")
                            Text(
                                text = rating.value.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                        }
                    }

                }

                //reviewText
                Row(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = review.notes ?: "")
                }

                //user image and username
                if (showUser) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //username and profile image
                        viewModel.user[review.uid]?.let { UserBar(user = it, link = true, navigator = navigator) }

                    }
                }
            }
        }
        //game image
        Box(modifier = Modifier.offset(16.dp, (13).dp) ){
            if (viewModel.game[review.gameId] != null) {
                GameCoverImage(
                    viewModel.game[review.gameId]!!,
                    contentDescription = "",
                    modifier = Modifier
                        .width(150.dp)
                        .combinedClickable(
                            onClick = { navigator.navigate(GameViewNavDestination(review.gameId)) },
                        )
                        .clip(RoundedCornerShape(8.dp)),
                )

            }
        }
    }

}