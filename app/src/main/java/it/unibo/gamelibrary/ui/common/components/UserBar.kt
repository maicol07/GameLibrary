package it.unibo.gamelibrary.ui.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.data.model.User

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserBar(user: User, link: Boolean, navigator: DestinationsNavigator?){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (link) {
                        navigator?.navigate(
                            it.unibo.gamelibrary.ui.views.destinations.ProfileDestination(
                                user.uid
                            )
                        )
                    }
                },
            ),
    ){
        if(user.hasImage()) {
            GlideImage(
                {
                    user.image
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }
        else {
            Icon(
                Icons.Outlined.AccountCircle,
                "profile image is not set",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }
        Spacer(Modifier.size(16.dp))
        Text(text = user.username, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

}