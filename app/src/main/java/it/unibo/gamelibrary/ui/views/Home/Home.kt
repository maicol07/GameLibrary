package it.unibo.gamelibrary.ui.views.Home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.api.igdb.apicalypse.APICalypse
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.views.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.destinations.SignupPageDestination
import proto.Game

//placeholder, no idea on how to give the post for now
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Post(id: Int) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,//TODO placeholder
                    contentDescription = "user profile image"
                )
                Text(text = "Username")//TODO placeholder
            }
            Image(
                imageVector = Icons.Filled.Photo,
                contentDescription = "post main image"
            )//TODO placeholder
            Text(text = "post number: $id")//TODO placeholder
        }
    }
}

//esempio section 'new games'
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
            fontWeight = FontWeight.Bold
        )
        LazyRow {
            items(list) { game ->
                Log.d("HomeSection", game.getArtworks(0).id.toString())
                Column {
                    GlideImage(
                        {
                            if(game.hasCover()) {
                                viewModel.getCoverUrl(APICalypse().where("id =" + game.cover.imageId))
                            }
                        },
                        imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
                        previewPlaceholder = R.drawable.ffviirebirth,
                        modifier = Modifier
                            .size(240.dp, 300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(100.dp, RoundedCornerShape(16.dp))
                            .clickable { navigator.navigate(GameViewNavDestination(gameId = 3)) }
                    )
                    Text(text = game.name)
                }
            }
        }
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun Home(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column() {
        Text(text = "Hello World!")
        Button(onClick = { navigator.navigate(GameViewNavDestination(gameId = 3)) }) {
            Text(text = "vai a game!")
        }
        Button(onClick = { navigator.navigate(SignupPageDestination()) }) {
            Text(text = "vai a Signup!")
        }

        HomeSection(
            title = "New Games",
            viewModel.newGames,
            navigator
        )
        LazyColumn() {
            items(count = 2) {
                Post(5)
            }
        }
    }

}



