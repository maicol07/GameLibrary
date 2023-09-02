package it.unibo.gamelibrary.ui.views.HomePublisher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.getCompanies
import ru.pixnews.igdbclient.model.Company
import ru.pixnews.igdbclient.model.Game
import javax.inject.Inject

@HiltViewModel
class HomePublisherViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    var games = mutableStateListOf<Game>()
    private var publisher by mutableStateOf<Company?>(null)
    var posts = mutableStateListOf<LibraryEntry>()

    init {
        fetchGames().invokeOnCompletion {
            fetchPosts()
        }
    }

    private fun fetchGames(): Job = viewModelScope.launch {
        val publisherName =
            userRepository.getUserByUid(auth.currentUser?.uid!!).first()?.publisherName
        val response = SafeRequest {
            IGDBClient.getCompanies {
                fields(
                    "slug",
                    "published.name",
                    "published.cover.image_id",
                    "published.first_release_date"
                )
                where("slug = \"${publisherName}\"")
                limit(1)
            }
        }
        publisher = response?.companies?.firstOrNull()
        games.clear()
        games.addAll(publisher?.published?.sortedByDescending {
            it.first_release_date?.epochSecond ?: 0
        } ?: emptyList())
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            libraryRepository.getLibraryEntriesByGames(games).collectLatest {
                posts.clear()
                posts.addAll(it)
            }
        }
    }
}