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
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.skydoves.landscapist.coil.CoilImage
import it.unibo.gamelibrary.data.model.User

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserBar(user: User, link: Boolean, navController: NavController?){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (link) {
                        navController?.navigate(
                            it.unibo.gamelibrary.ui.destinations.ProfileDestination(
                                user.uid
                            )
                        )
                    }
                },
            ),
    ){
        if(user.hasImage()) {
            CoilImage(
                imageModel = {
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
        if(user.isPublisher){
            Spacer(modifier = Modifier.size(2.dp))
            Badge (containerColor = Color.Green, contentColor = Color.White){
                Text(text = "P")
            }
        }

    }

}