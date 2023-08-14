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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val followRepository: FollowRepository,
    private val libraryRepository: LibraryRepository

) : ViewModel() {
    var user by mutableStateOf<User?>(null)
    var userLibrary = mutableStateListOf<LibraryEntry>()
    var newImage = mutableStateOf("")
    var newBio = mutableStateOf("")
    var newUsername = mutableStateOf("")
    var followed =
        mutableStateListOf<String>()//seguaci e seguiti dell'utente di cui si viualizza il profilo
    var followers = mutableStateListOf<String>()
    var users = mutableStateListOf<User>()

    var showProfileEditDialog by mutableStateOf(false)
    var showFollowDialog by mutableStateOf(false)

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
            userLibrary.clear()
            userLibrary.addAll(libraryRepository.getUserLibraryEntries(uid))
        }
    }

    fun imagePickerCallback(uri: Uri?) {
        if (uri != null) {
            newImage.value = uri.toString()
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    fun applyChanges() {
        viewModelScope.launch {
            if (newImage.value != user?.image) {
                repository.setImage(user!!.uid, newImage.value)
            }
            if (newBio.value != (user?.bio ?: "")) {
                repository.setBio(user!!.uid, newBio.value)
            }
            if (newUsername.value != (user?.username ?: "")) {
                repository.setUsername(user!!.uid, newUsername.value)
            }
            getUser(uid = user!!.uid)
        }
    }

    fun getFollowed(uid: String): Job {
        return viewModelScope.launch {
            followed.clear()
            followed.addAll(followRepository.getFollowed(uid).map { it.followed })
        }
    }

    fun getFollowers(uid: String): Job {
        return viewModelScope.launch {
            followers.clear()
            followers.addAll(followRepository.getFollowers(uid).map { it.follower })
        }
    }

    fun toggleFollow(uid1: String, uid2: String) {
        viewModelScope.launch {
            if (amIFollowing()) {
                followRepository.unfollow(uid1, uid2)
            } else {
                followRepository.follow(uid1, uid2)
            }
            getFollowers(uid2)
        }
    }

    fun amIFollowing(): Boolean {
        return followers.contains(Firebase.auth.currentUser?.uid)
    }

    fun getUsers(uids: List<String>){
        users.clear()
        viewModelScope.launch {
            uids.forEach{
                users.add(repository.getUserByUid(it)!!)
            }

        }
    }

}