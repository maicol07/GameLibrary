package it.unibo.gamelibrary.ui.views.Profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonRemoveAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.BuildConfig
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.common.components.GameCardView.GameCardView
import it.unibo.gamelibrary.ui.common.components.UserBar
import it.unibo.gamelibrary.ui.views.Home.UserReview.UserReview
import it.unibo.gamelibrary.utils.TopAppBarState
import proto.Game
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects


//TODO se sei sul profilo di qualcun'altro la bottom app bar seleziona sia home che profile
//TODO
@Destination
@Composable()
fun Profile(
    viewModel: ProfileViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    userID: String?,
) {
    val uid = userID ?: Firebase.auth.currentUser!!.uid
    viewModel.getUser(uid)
    viewModel.getLibrary(uid)
    viewModel.getFollowers(uid)
    viewModel.getFollowed(uid)

    TopAppBarState.actions = {if(Firebase.auth.currentUser?.uid == uid){ EditButton(viewModel) } }
    TopAppBarState.customTitle = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            if(viewModel.user != null){
                UserBar(user = viewModel.user!!, link = false, navigator = null)
            }
        }
    }

    LazyColumn {
        item {//bio, follow
            Row {
                Text(
                    if(viewModel.user?.bio != null || viewModel.user?.bio == ""){
                        viewModel.user?.bio.toString()
                    } else {
                        "nothing here yet"
                    }, modifier = Modifier.padding(24.dp))

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FollowList(viewModel = viewModel, followers = false, navigator)
                FollowList(viewModel = viewModel, followers = true, navigator)
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

        //reviews di questo user
        if(viewModel.user?.isPublisher == false){
            items(viewModel.userLibrary)
            {
                UserReview(it, navigator, showUser = false)
            }
        }
        else{
            items(viewModel.publisherGames){game: Game ->
                GameCardView(game = game, navigator = navigator)
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
            if (viewModel.amIFollowing()) {//seguo già, se clicco = unfollow
                viewModel.toggleFollow(me, other)//unfollow
            } else {
                viewModel.toggleFollow(me, other)//follow
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
    navigator: DestinationsNavigator
){
    var showDialog by remember{ mutableStateOf(false) }

    TextButton(onClick = {
        if(followers){
            viewModel.getUsers(viewModel.followers)
        }
        else{
            viewModel.getUsers(viewModel.followed)
        }
        showDialog = true;
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
            LazyColumn {
                items(viewModel.users){ user ->
                    UserBar(user = user, true, navigator = navigator)
                }
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
        viewModel.showProfileEditDialog = true;
    }){
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit Profile"
        )
    }

    if (viewModel.showProfileEditDialog) {

        //image from camera
        val context = LocalContext.current
        val file = context.createImageFile()
        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            BuildConfig.APPLICATION_ID + ".provider", file
        )

        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                viewModel.newImage.value = uri.toString()
            }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(uri)
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
                    Text(text = "Done")
                }
                TextButton(
                    onClick = {
                        viewModel.applyChanges()
                    },
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(text = "Apply")
                }
            }
        ) {
            Column {
                if (viewModel.newImage.value != "") {
                    GlideImage(
                        {
                            Uri.parse(viewModel.newImage.value)
                        },
                        modifier = Modifier.size(256.dp)
                    )
                } else {
                    Image(
                        Icons.Outlined.AccountCircle,
                        "profile image is not set",
                        modifier = Modifier.size(256.dp)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Select New Profile Image")
                }

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = {

                        val permissionCheckResult =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(uri)
                        } else {
                            // Request a permission
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Take a picture")
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
                    singleLine = true,
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

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}