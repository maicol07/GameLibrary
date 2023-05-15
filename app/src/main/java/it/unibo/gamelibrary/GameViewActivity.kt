package it.unibo.gamelibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.ui.SuperScaffold
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme

class GameViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameView()
                }
            }
        }
    }
}

@Composable
@Preview
fun GameView() {
    SuperScaffold(title = "Game view") {
        Column(Modifier.padding(it)) {
            GameImage()
            GameDetails()
        }
    }
}

@Composable
fun GameImage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            { "https://th.bing.com/th/id/R.bdbbc8680cd4e305911545366a0b8dd4?rik=ityKiz4SVJxw%2bQ&pid=ImgRaw&r=0" },
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
fun GameDetails() {
    Column {
        Text(text = "Final Fantasy VII Rebirth", style = MaterialTheme.typography.titleLarge)
        Text(text = "Square Enix")
        Text(text = "2021")
        Text(text = "RPG")
        Text(text = "Single player")
    }
}
