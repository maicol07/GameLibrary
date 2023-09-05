package it.unibo.gamelibrary.ui.views.profile

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonRemoveAlt1
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.neovisionaries.i18n.CountryCode
import com.ramcosta.composedestinations.annotation.Destination
import com.skydoves.landscapist.coil.CoilImage
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.common.components.GameCardView
import it.unibo.gamelibrary.ui.common.components.NoInternetConnection
import it.unibo.gamelibrary.ui.common.components.UserBar
import it.unibo.gamelibrary.ui.common.components.checkInternetConnection
import it.unibo.gamelibrary.ui.views.home.userReview.UserReview
import it.unibo.gamelibrary.utils.TopAppBarState
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.util.igdbImageUrl

@Destination
@Composable
fun Profile(
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavController,
    userID: String?,
) {
    val context = LocalContext.current
    val uid = userID ?: Firebase.auth.currentUser!!.uid
    if (viewModel.user == null) {
        viewModel.setUser(uid)
    }

    TopAppBarState.actions = {if(Firebase.auth.currentUser?.uid == uid){ EditButton(viewModel) } }
    TopAppBarState.customTitle = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            if(viewModel.user != null){
                UserBar(user = viewModel.user!!, link = false, navController = null)
            }
        }
    }

    LazyColumn (Modifier.fillMaxHeight()){
        item {//bio, follow
            Row {
                Text(
                    if(viewModel.user?.bio != null || viewModel.user?.bio == ""){
                        viewModel.user?.bio.toString()
                    } else {
                        "nothing here yet"
                    }, modifier = Modifier.padding(12.dp))
            }
            if(viewModel.user?.isPublisher == true){
                PublisherInfo(viewModel)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FollowList(viewModel = viewModel, followers = false, navController)
                FollowList(viewModel = viewModel, followers = true, navController)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (Firebase.auth.currentUser?.uid != uid) {
                    FollowButton(viewModel, Firebase.auth.currentUser!!.uid, uid)
                }
            }
            Spacer(Modifier.size(16.dp))
        }

        if (checkInternetConnection(context)) {
            if(viewModel.user?.isPublisher == false) {
                if (viewModel.userLibrary.isNotEmpty()) {
                    items(viewModel.userLibrary, key = { it.id })
                    {
                        UserReview(it, navController, showUser = false)
                    }
                } else {
                    item {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillParentMaxHeight(0.7F)
                        ) {
                            Text(
                                text = "No reviews yet. You can find games you played in Home or by searching them by name!",
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, bottom = 50.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(viewModel.publisherGames) { game: Game ->
                    GameCardView(game = game, navController = navController)
                }
            }
        }
    }
    if (!checkInternetConnection(context)) {
        NoInternetConnection()
    }
}

@Composable
private fun PublisherInfo(viewModel: ProfileViewModel){
    Column(Modifier.padding(8.dp)) {
        Text(text = "Publisher info:", fontWeight = FontWeight.Bold)
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(4.dp).fillMaxWidth()
        ){
            Text(text = "Name: ")
            Text(text = viewModel.publisher?.name.toString())
        }

        if(viewModel.publisher?.country != 0){
            Row (horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            ){
                Text(text = "Country: " )
                Text(text = viewModel.publisher?.let { CountryCode.getByCode(it.country).getName() }.toString())
            }

        }
        if(viewModel.publisher?.logo != null){
            Row (horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            ){
                Text(text = "Company logo: ")
                CoilImage(
                    imageModel = {
                        viewModel.publisher?.logo?.image_id?.let { igdbImageUrl(it) }
                    },
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
        if(viewModel.publisher?.websites?.isNotEmpty() == true){
            Row (horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            ){
                Text(text = "Website: ")
                Text(text = viewModel.publisher?.websites?.get(0)?.url.toString())
            }
        }
    }

}

@Composable
private fun FollowButton(
    viewModel: ProfileViewModel,
    me: String,
    other: String
) {
    TextButton(
        onClick = {
            if (viewModel.amIFollowing()) {
                viewModel.toggleFollow(me, other)
            } else {
                viewModel.toggleFollow(me, other)
            }
        }
    ) {
        if (viewModel.amIFollowing()) {
            Text(text = "Unfollow ")
            Icon(Icons.Outlined.PersonRemoveAlt1, null)
        } else {
            Text(text = "Follow ")
            Icon(Icons.Outlined.PersonAddAlt1, null)
        }
    }
}

@Composable
fun FollowList(
    viewModel: ProfileViewModel,
    followers: Boolean,
    navController: NavController
){
    var showDialog by remember{ mutableStateOf(false) }

    TextButton(onClick = {
        if(followers){
            viewModel.getUsers(viewModel.followers)
        }
        else{
            viewModel.getUsers(viewModel.followed)
        }
        showDialog = true
    }){
        Text(text =
            if(followers){ viewModel.followers.count().toString() + " followers"}
            else{viewModel.followed.count().toString() + " following"}
        )
    }

    if (showDialog) {
        CustomDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text =
                    if(followers) {
                        "Followers"
                    } else {
                        "Following"
                    }
                )
            },
            buttons = {
                TextButton(
                    onClick = {
                        showDialog = false
                    },
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(text = "Close")
                }
            }
        ) {
            if(viewModel.users.isNotEmpty()){
                LazyColumn {
                    items(viewModel.users){ user ->
                        UserBar(user = user, true, navController = navController)
                    }
                }
            }else{
                Text(text = "No one here yet!")
            }
        }
    }
}

@Composable
private fun EditButton(
    viewModel: ProfileViewModel
) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            viewModel.imagePickerCallback(uri)
        }

    IconButton(onClick = {
        viewModel.showProfileEditDialog = true
    }){
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit Profile"
        )
    }

    if (viewModel.showProfileEditDialog) {
        val context = LocalContext.current
        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                if(it){
                    viewModel.tempUriNewImage?.let { uri ->
                        viewModel.newImage.value = uri
                    }
                }
            }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(viewModel.tempUriNewImage)
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        CustomDialog(
            onDismissRequest = { viewModel.showProfileEditDialog = false },
            title = { Text("Edit Profile") },
            buttons = {
                TextButton(
                    onClick = {
                        viewModel.showProfileEditDialog = false
                    },
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(text = "Cancel")
                }

                TextButton(
                    onClick = {
                        viewModel.applyChanges(context)
                        viewModel.showProfileEditDialog = false
                    },
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(text = "Save")
                }
            }
        ) {
            Column {
                if (viewModel.newImage.value != Uri.EMPTY) {
                    CoilImage(
                        imageModel = {
                            viewModel.newImage.value
                        },
                        modifier = Modifier
                            .size(256.dp)
                            .clip(RoundedCornerShape(128.dp))
                    )
                } else {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        "profile image is not set",
                        modifier = Modifier.size(256.dp)
                    )
                }

                Row (
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ){
                    TextButton(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(64.dp),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onBackground, containerColor = MaterialTheme.colorScheme.background),
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Select Image",
                            modifier = Modifier.scale(1.75f)
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.tempUriNewImage = createImageFile(context)

                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(viewModel.tempUriNewImage)
                            }
                            else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onBackground, containerColor = MaterialTheme.colorScheme.background),
                        ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Take a picture",
                            modifier = Modifier.scale(1.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                TextField(
                    value = viewModel.newUsername.value,
                    onValueChange = { viewModel.newUsername.value = it },
                    label = { Text("Username") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "username"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.size(16.dp))

                TextField(
                    value = viewModel.newBio.value,
                    onValueChange = { viewModel.newBio.value = it },
                    label = { Text("Bio") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Book,
                            contentDescription = "bio"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun createImageFile(context: Context): Uri {
    val imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg"
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }

    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: Uri.EMPTY
}