package it.unibo.gamelibrary.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import it.unibo.gamelibrary.utils.TopAppBarState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * @source https://github.com/axiel7/AniHyou-android/blob/master/app/src/main/java/com/axiel7/anihyou/ui/composables/FullScreenImageView.kt
 */
@Destination
@Composable
fun FullScreenImageView(
    imageUrl: String?,
    navController: NavController,
    onDismiss: () -> Unit = {
        navController.popBackStack()
    },
) {
    TopAppBarState.show = false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .zoomable(rememberZoomableState()),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "close")
            }
        }
        CoilImage(
            imageModel = { imageUrl },
            imageOptions = ImageOptions(
                contentDescription = "image",
                contentScale = ContentScale.FillWidth,
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}