package it.unibo.gamelibrary.ui.views.HomePublisher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.companies
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Company
import proto.Game
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

    fun fetchGamesAndPosts() {
        viewModelScope.launch {
            val publisherName = userRepository.getUserByUid(auth.currentUser?.uid!!)?.publisherName
            publisher = IGDBApiRequest {
                IGDBWrapper.companies(
                    APICalypse()
                        .fields(
                            "published," +
                                    " slug," +
                                    " published.name," +
                                    " published.cover.image_id," +
                                    " published.first_release_date"
                        )
                        .where("slug = \"$publisherName\"")
                        .limit(1)
                )
            }?.get(0)
            games.clear()
            games.addAll(publisher!!.publishedList.sortedByDescending { it.firstReleaseDate.seconds })

            fetchPosts()
        }
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            posts.clear()
            for (game in games) {
                posts.addAll(libraryRepository.getCollectionsByGame(game))
            }
        }
    }
}