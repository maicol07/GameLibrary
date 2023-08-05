package it.unibo.gamelibrary.ui.views.Profile
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.FollowRepository
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val followRepository: FollowRepository,
    private val libraryRepository: LibraryRepository

): ViewModel() {
    var user by mutableStateOf<User?>(null)
//     var userLibrary: List<LibraryEntry> = listOf()
    var userLibrary = mutableStateListOf<LibraryEntry>()
    var newImage = mutableStateOf("")
    var newBio = mutableStateOf("")
    var newUsername = mutableStateOf("")
    var followed: List<String>? = null
    var followers: List<String>? = null

    var showProfileEditDialog by mutableStateOf(false)

    init {
        if (Firebase.auth.currentUser != null){
            getUser(Firebase.auth.currentUser!!.uid)
            getLibrary(Firebase.auth.currentUser!!.uid)
        }
        getFollowed(Firebase.auth.currentUser!!.uid)//sono i seguiti e i seguaci di currentuser!!
        getFollowers(Firebase.auth.currentUser!!.uid)
    }

    fun getUser(uid: String) {
        viewModelScope.launch {
            user = repository.getUserByUid(uid)
            newBio.value = user?.bio ?: ""
            newImage.value = user?.image ?: ""
            newUsername.value = user?.username ?: ""
        }
    }

    fun getLibrary(uid: String) {
        viewModelScope.launch {
            userLibrary.addAll(libraryRepository.getUserLibraryEntries(uid))
        }
    }

    fun imagePickerCallback(uri: Uri?){
        if (uri != null) {
            newImage.value = uri.toString()
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    fun applyChanges(){
        viewModelScope.launch {

            if(newImage.value != user?.image){
                repository.setImage(user!!.uid, newImage.value)
            }
            if(newBio.value != ( user?.bio ?: "" )) {
                repository.setBio(user!!.uid, newBio.value)
            }
            if(newUsername.value != (user?.username ?: "" )){
                repository.setUsername(user!!.uid, newUsername.value)
            }
           getUser(uid = user!!.uid)
        }
    }

    fun getFollowed(uid: String){
        viewModelScope.launch {
            followed = followRepository.getFollowed(uid).map { it.followed }
        }
    }
    fun getFollowers(uid: String){
        viewModelScope.launch {
            followers = followRepository.getFollowers(uid).map { it.follower }
        }
    }
}