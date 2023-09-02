package it.unibo.gamelibrary.ui.common.Game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.ui.destinations.FullScreenImageViewDestination
import it.unibo.gamelibrary.ui.views.GameView.preview.GameParameterProvider
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.IgdbImageSize
import ru.pixnews.igdbclient.util.igdbImageUrl

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameImage(
    imageId: String?,
    modifier: Modifier = Modifier,
    imageSize: IgdbImageSize = IgdbImageSize.H1080P,
    imageScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    fullscreenable: Boolean = false,
    navController: NavController? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp
) {
    val imageUrl = if (imageId != null) igdbImageUrl(imageId, imageSize) else null
    val clickableModifier = if (imageUrl !== null && fullscreenable && navController != null) {
        modifier
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    navController.navigate(FullScreenImageViewDestination(imageUrl))
                }
            )
    } else {
        modifier
    }
    CoilImage(
        imageModel = { imageUrl ?: R.drawable.no_image },
        imageOptions = ImageOptions(
            contentScale = imageScale,
            contentDescription = contentDescription,
            alignment = alignment
        ),
        previewPlaceholder = R.drawable.ffviirebirth,
        modifier = clickableModifier
            .clip(shape)
            .shadow(shadowElevation, shape),
        component = rememberImageComponent {
            CrossfadePlugin()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GameCoverImage(
    @PreviewParameter(provider = GameParameterProvider::class) game: Game,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fullscreenable: Boolean = false,
    navController: NavController? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp,
    status: LibraryEntryStatus? = null
) {
    BadgedBox(badge = {
        if (status != null) {
            Badge(Modifier.offset(x = (-32).dp, y = 12.dp), containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Icon(status.selectedIcon, contentDescription = status.text)
            }
        }
    }) {
        GameImage(
            imageId = game.cover?.image_id,
            imageSize = IgdbImageSize.COVER_BIG,
            modifier = modifier,
            contentDescription = contentDescription,
            fullscreenable = fullscreenable,
            navController = navController,
            shape = shape,
            shadowElevation = shadowElevation
        )
    }
}


@Composable
fun GameArtwork(
    game: Game,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fullscreenable: Boolean = false,
    navController: NavController? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp,
) {
    GameImage(
        imageId = if (game.artworks.isNotEmpty()) game.artworks[0].image_id else null,
        modifier = modifier,
        contentDescription = contentDescription,
        fullscreenable = fullscreenable,
        navController = navController,
        shape = shape,
        shadowElevation = shadowElevation
    )
}

@Composable
fun GameScreenshot(
    game: Game,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fullscreenable: Boolean = false,
    navController: NavController? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    shadowElevation: Dp = 16.dp,
) {
    GameImage(
        imageId = if (game.screenshots.isNotEmpty()) game.screenshots[0].image_id else null,
        imageSize = IgdbImageSize.SCREENSHOT_HUGE,
        modifier = modifier,
        contentDescription = contentDescription,
        fullscreenable = fullscreenable,
        navController = navController,
        shape = shape,
        shadowElevation = shadowElevation
    )
}