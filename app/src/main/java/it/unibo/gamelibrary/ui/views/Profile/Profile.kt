package it.unibo.gamelibrary.ui.views.Profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.glide.GlideImage
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.views.Home.UserReview.UserReview
import it.unibo.gamelibrary.utils.TopAppBarState

@Destination
@Composable
fun Profile(
    viewModel: ProfileViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    uid: String? //se uid = null è il profilo dell'utente loggato
) {
    TopAppBarState.customTitle ={ //TODO a volte l'immagine si vede a volte no?? Errore di permessi strani (Glide): java.lang.SecurityException: Calling uid ( 10585 ) does not have permission to access picker uri: content://media/picker/0/com.android.providers.media.photopicker/media/1000014111
        // https://oguzhanaslann.medium.com/new-photo-picker-api-no-permissions-5c500aa2391e-
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Log.i("Profile", "image: ${viewModel.user?.image}")
            if(viewModel.user?.image != null) {
                GlideImage(
                    {
                        Uri.parse(viewModel.user?.image)
                    },
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                )
            }
            else {
                Image(
                    Icons.Outlined.AccountCircle,
                    "profile image is not set",
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                )
            }
            Text(text= viewModel.user?.username ?: "??")
        }
    }

    TopAppBarState.actions = {if(uid != null && Firebase.auth.currentUser?.uid != uid){ EditButton(viewModel) } }
    //uid = uid == null ?
    if(uid != null){
        viewModel.getUser(uid)
    }
    LazyColumn() {
        item {
            Row {

                Column {
                    Text(viewModel.user?.bio.toString(), modifier = Modifier.padding(8.dp))
                }
            }
            if(uid != null && Firebase.auth.currentUser?.uid != uid){
                FollowButton(viewModel, Firebase.auth.currentUser!!.uid, uid)
            }

            Spacer(Modifier.size(16.dp))
        }
        //reviews di questo user
        //Log.i("PROFILE USER REVIEW", viewModel.userLibrary.toString())

        items(
            viewModel.userLibrary,
            key = {it.id})
        {
            UserReview(it, navigator, showUser = false)
        }
    }
}

@Composable
private fun FollowButton(
    viewModel: ProfileViewModel,
    me: String,
    other: String) {
    Button(
        onClick = {
            if(viewModel.followed?.contains(other) == true){
                //TODO
            }
            else{

            }
        },
        Modifier.size(128.dp)
    ){
        if(viewModel.followed?.contains(other) == true){
            Text(text = "Stop following")
        }
        else{
            Text(text = "Follow")

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditButton(
    viewModel: ProfileViewModel
){
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        viewModel.imagePickerCallback(uri)
    }
    IconButton(onClick = {
        viewModel.showProfileEditDialog = true;
    }){
        Image(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit Profile"
        )
    }

    if (viewModel.showProfileEditDialog) {
        CustomDialog(
            onDismissRequest = { viewModel.showProfileEditDialog = false },
            title = {Text("Edit Profile")},
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
                if(viewModel.newImage.value != "") {
                    GlideImage(
                        {
                            Uri.parse(viewModel.newImage.value)
                        },
                        modifier = Modifier.size(256.dp)
                    )
                }
                else {
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
                            contentDescription = "biografia"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )



            }
        }
    }
}
