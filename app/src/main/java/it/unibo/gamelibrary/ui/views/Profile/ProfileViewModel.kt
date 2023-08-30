package it.unibo.gamelibrary.ui.views.Profile

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
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
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.getCompanies
import ru.pixnews.igdbclient.model.Game
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val followRepository: FollowRepository,
    private val libraryRepository: LibraryRepository

) : ViewModel() {
    var user by mutableStateOf<User?>(null)
    var userLibrary = mutableStateListOf<LibraryEntry>()
    var newImage = mutableStateOf<Uri>(Uri.EMPTY)
    var newBio = mutableStateOf("")
    var newUsername = mutableStateOf("")
    var followed =
        mutableStateListOf<String>()//seguaci e seguiti dell'utente di cui si viualizza il profilo
    var followers = mutableStateListOf<String>()
    var users = mutableStateListOf<User>()
    var publisherGames = mutableStateListOf<Game>()
    var showProfileEditDialog by mutableStateOf(false)
    var tempUriNewImage: Uri? = null

    fun setUser(uid: String) {
        viewModelScope.launch {
            user = repository.getUserByUid(uid).first()
            newBio.value = user?.bio ?: ""
            newImage.value = Uri.parse(user?.image ?: "")
            newUsername.value = user?.username ?: ""
            if(user?.isPublisher == true){
                getPublisherGames()
            }
        }

        getLibrary(uid)
        getFollowers(uid)
        getFollowed(uid)
    }

    fun getPublisherGames(){
        viewModelScope.launch {
            val result = SafeRequest {
                IGDBClient.getCompanies {
                    fields(
                        "published, slug, published.name, published.cover.image_id",
                        "published.name",
                        "published.artworks.image_id",
                        "published.cover.image_id",
                        "published.first_release_date",
                        "published.involved_companies.*",
                        "published.involved_companies.company.name",
                        "published.genres.name",
                        "published.genres.slug",
                        "published.screenshots.image_id",
                        "published.summary",
                        "published.release_dates.human",
                        "published.release_dates.platform.name",
                        "published.release_dates.platform.platform_logo.url"
                    )
                    where("slug = \"${user?.publisherName}\"")
                    limit(1)
                }
            }
            val publisher = result?.companies?.firstOrNull()
            publisherGames.clear()
            publisher?.published?.let { publisherGames.addAll(it) }
            publisherGames.sortByDescending { it.first_release_date?.epochSecond }
        }
    }

    fun getLibrary(uid: String) {
        viewModelScope.launch {
            libraryRepository.getUserLibraryEntries(uid).collectLatest {
                userLibrary.clear()
                userLibrary.addAll(it)
            }
        }
    }

    fun imagePickerCallback(uri: Uri?) {
        if (uri != null) {
            newImage.value = uri
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    fun applyChanges(context: Context) {

        val inputStream: InputStream? = context.contentResolver.openInputStream(newImage.value)
        val yourDrawable = Drawable.createFromStream(inputStream, newImage.toString())
        inputStream?.close()

        val cw = ContextWrapper(context.applicationContext)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, System.currentTimeMillis().toString() + ".jpg")

        val savedUri = file.toUri()

        if (!file.exists()) {
            Log.d("path", file.toString())
            var fos: FileOutputStream?
            try {
                fos = FileOutputStream(file)
                //TODO controlla se sui telefoni degli altrir funziona Jpeg o se serve png
                yourDrawable?.toBitmap()?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            if (newImage.value != Uri.parse(user?.image)) {

                Uri.parse(user?.image).path?.let { File(it).delete() }// delete old image from internal storage

                repository.setImage(user!!.uid, savedUri.toString())
            }
            if (newBio.value != (user?.bio ?: "")) {
                repository.setBio(user!!.uid, newBio.value)
            }
            if (newUsername.value != (user?.username ?: "")) {
                repository.setUsername(user!!.uid, newUsername.value)
            }
            setUser(uid = user!!.uid)
        }
    }

    fun getFollowed(uid: String): Job {
        return viewModelScope.launch {
            followRepository.getFollowed(uid).collectLatest {
                followed.clear()
                followed.addAll(it.map { it.followed })
            }
        }
    }

    fun getFollowers(uid: String): Job {
        return viewModelScope.launch {
            followRepository.getFollowers(uid).collectLatest {
                followed.clear()
                followed.addAll(it.map { it.follower })
            }
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
                users.add(repository.getUserByUid(it).first()!!)
            }
        }
    }

}