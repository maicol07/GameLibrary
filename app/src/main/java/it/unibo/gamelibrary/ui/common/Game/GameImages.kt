package it.unibo.gamelibrary.ui.common.Game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.ui.common.components.Fullscreen
import it.unibo.gamelibrary.ui.views.GameView.preview.GameParameterProvider
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.IgdbImageSize
import ru.pixnews.igdbclient.util.igdbImageUrl

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GameCoverImage(
    @PreviewParameter(provider = GameParameterProvider::class) game: Game,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fullscreenable: Boolean = false,
    fullscreenModifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp,
    status: LibraryEntryStatus? = null
) {
    val fullscreenState = remember { mutableStateOf(false) }

    Fullscreen(fullscreenState) {
        GameCoverImage(
            game,
            modifier = Modifier
                .then(fullscreenModifier),
            contentDescription = "${game.name} cover",
            shape = RoundedCornerShape(0.dp)
        )
    }
    val clickableModifier = if (fullscreenable) {
        modifier
            .combinedClickable(
                onClick = {},
                onLongClick = { fullscreenState.value = true }
            )
    } else {
        modifier
    }

    BadgedBox(badge = {
        if (status != null) {
            Badge(Modifier.offset(x = (-32).dp, y = 12.dp), containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Icon(status.selectedIcon, contentDescription = status.text)
            }
        }
    }) {
        CoilImage(
            imageModel = {
                if (game.cover != null && game.cover!!.image_id != "") igdbImageUrl(
                    game.cover!!.image_id,
                    IgdbImageSize.COVER_BIG
                ) else R.drawable.no_image
            },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                contentDescription = contentDescription
            ),
            previewPlaceholder = R.drawable.ffviirebirth,
            component = rememberImageComponent {
                CrossfadePlugin()
            },
            modifier = Modifier
                .clip(shape)
                .shadow(shadowElevation, shape)
                .then(clickableModifier)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameArtwork(
    game: Game,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fullscreenable: Boolean = false,
    fullscreenModifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp,
) {
    val fullscreenState = remember { mutableStateOf(false) }

    Fullscreen(fullscreenState) {
        GameArtwork(
            game,
            modifier = Modifier
                .fillMaxWidth()
                .then(fullscreenModifier),
            contentDescription = "${game.name} cover"
        )
    }
    val clickableModifier = if (fullscreenable) {
        modifier
            .combinedClickable(
                onClick = {},
                onLongClick = { fullscreenState.value = true }
            )
    } else {
        modifier
    }

    CoilImage(
        imageModel = {
            if (game.artworks.isNotEmpty()) {
                igdbImageUrl(game.artworks[0].image_id, IgdbImageSize.H1080P)
            } else {
                R.drawable.no_image
            }
        },
        // Small black inner shadow
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            contentDescription = contentDescription
        ),
        component = rememberImageComponent {
            CrossfadePlugin()
        },
        previewPlaceholder = R.drawable.ffviirebirth,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(shape)
            .shadow(shadowElevation, shape)
            .then(clickableModifier),
    )
}

@Composable
fun GameScreenshot(
    game: Game,
    contentDescription: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp
) {
    CoilImage(
        imageModel = {
            if (game.screenshots.isNotEmpty()) {
                igdbImageUrl(game.screenshots[0].image_id, IgdbImageSize.SCREENSHOT_BIG)
            } else {
                R.drawable.no_image
            }
        },
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillBounds,
            contentDescription = contentDescription
        ),
        previewPlaceholder = R.drawable.ffviirebirth,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(shape)
            .shadow(shadowElevation, shape),
    )
}