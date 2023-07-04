package it.unibo.gamelibrary.ui.views.GameView

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.R
import proto.Game

private val dateFormatter = SimpleDateFormat.getDateInstance();

@Destination
@Composable
fun GameViewNav(gameId: Int, viewModel: GameViewViewModel = hiltViewModel()) {
    GameView(game = viewModel.game)
}

@Composable
@Preview
fun GameView(@PreviewParameter(GameViewParameterPreviewProvider::class) game: Game) {
    Column {
        GameImage(game.cover.url)
        GameDetails(game)
    }
}

@Composable
fun GameImage(url: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            { url },
            imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
            previewPlaceholder = R.drawable.ffviirebirth,
            modifier = Modifier
                .size(240.dp, 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(100.dp, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun GameDetails(game: Game) {
    Column(Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = game.name, style = MaterialTheme.typography.titleLarge)
        }
        LazyRow {
            items(game.involvedCompaniesList) {
                val icon: Any = when {
                    it.porting -> Icons.Default.Business;
                    it.developer -> Icons.Default.Code;
                    it.publisher -> Icons.Default.Business;
                    it.supporting -> R.drawable.lifebuoy;
                    else -> Icons.Default.Business
                }
                AssistChip(
                    label = { Text(text = it.company.name) },
//                    leadingIcon = {
//                        GlideImage(
//                            { it.company.logo.url },
//                            previewPlaceholder = R.drawable.square_enix_logo,
//                            imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
//                            modifier = Modifier
//                                .size(24.dp, 24.dp)
//                                .clip(RoundedCornerShape(4.dp))
//                        )
//                    },
                    leadingIcon = {
                        if (icon is ImageVector) {
                            Icon(
                                icon,
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                painterResource(icon as Int),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = { /* TODO: Publisher page */ })
            }
        }
        AssistChip(
            label = {Text(text = dateFormatter.format(game.firstReleaseDate.seconds))},
            onClick = { /*TODO*/ }
        )
        Text(text = "Genres", style = MaterialTheme.typography.headlineSmall)
        Text(text = game.getGenres(0).name)
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(text = "Final Fantasy VII Rebirth", style = MaterialTheme.typography.titleLarge)
//        }
//        Text(text = "Square Enix")
//        Text(text = "2021")
//        Text(text = "RPG")
//        Text(text = "Single player")
    }
}