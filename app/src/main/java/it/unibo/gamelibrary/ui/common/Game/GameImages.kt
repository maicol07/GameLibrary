package it.unibo.gamelibrary.ui.common.Game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.imageBuilder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.common.components.Fullscreen
import proto.Game

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCoverImage(
    game: Game,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fullscreenable: Boolean = false,
    fullscreenModifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
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

    GlideImage(
        {
            if (game.cover.imageId != "") imageBuilder(
                game.cover.imageId,
                ImageSize.COVER_BIG
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
            .shadow(16.dp, shape)
            .then(clickableModifier)
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameArtwork(
    game: Game,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fullscreenable: Boolean = false,
    fullscreenModifier: Modifier = Modifier,
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

    GlideImage(
        {
            if (game.artworksCount > 0) {
                imageBuilder(game.getArtworks(0).imageId, ImageSize.FHD)
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
            .shadow(16.dp)
            .then(clickableModifier),
    )
}

@Composable
fun GameScreenshot(game: Game, contentDescription: String, modifier: Modifier = Modifier) {
    GlideImage(
        {
            if (game.screenshotsCount > 0) {
                imageBuilder(game.getScreenshots(0).imageId, ImageSize.SCREENSHOT_BIG)
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
            .shadow(16.dp),
    )
}