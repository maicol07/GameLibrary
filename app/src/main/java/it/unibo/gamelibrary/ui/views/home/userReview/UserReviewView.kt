package it.unibo.gamelibrary.ui.views.home.userReview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.fade
import io.github.fornewid.placeholder.material3.placeholder
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.ui.common.components.UserBar
import it.unibo.gamelibrary.ui.common.game.GameCoverImage
import it.unibo.gamelibrary.ui.destinations.GameViewNavDestination
import ru.pixnews.igdbclient.model.Game

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserReview(
    review: LibraryEntry,
    navController: NavController,
    showUser: Boolean = true,
    viewModel: UserReviewViewModel = hiltViewModel(),
) {
    Box {
        Card(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 8.dp, end = 8.dp)
                .wrapContentHeight()
        ) {
            if (viewModel.game[review.gameId] == null) {
                viewModel.getGame(review.gameId)
            }
            if (viewModel.user[review.uid] == null) {
                viewModel.getUser(review.uid)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.size(170.dp))//creates space for game Image (below)

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(//game name
                            text = viewModel.game[review.gameId]?.name ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            val rating = remember { mutableIntStateOf(review.rating ?: 0) }
                            Icon(Icons.Outlined.StarBorder, "Star icon")
                            Text(
                                text = rating.intValue.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Row(Modifier.padding(top = 6.dp)) {
                            Icon(
                                imageVector = review.status.unselectedIcon,
                                contentDescription = review.status.text
                            )
                            Text(text = review.status.text)
                        }
                    }

                }

                Row(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(text = review.notes ?: "")
                }

                if (showUser) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.user[review.uid]?.let {
                            UserBar(
                                user = it,
                                link = true,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier.offset(16.dp, (13).dp)
        ) {
            GameCoverImage(
                viewModel.game[review.gameId] ?: Game(),
                contentDescription = "",
                modifier = if (viewModel.game[review.gameId] != null) {
                    Modifier
                        .width(150.dp)
                        .height(200.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(GameViewNavDestination(review.gameId)) },
                        )
                        .clip(RoundedCornerShape(8.dp))
                } else {
                    Modifier
                        .width(150.dp)
                        .height(200.dp)
                        .placeholder(visible = true, highlight = PlaceholderHighlight.fade())
                }
            )
        }
    }

}