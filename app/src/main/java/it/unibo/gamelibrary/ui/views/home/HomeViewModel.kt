package it.unibo.gamelibrary.ui.views.home

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
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.IgdbEndpoint
import ru.pixnews.igdbclient.apicalypse.SortOrder
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.multiquery
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var newGames = mutableStateListOf<Game>()
    var mostLovedGames = mutableStateListOf<Game>()
    var popularGames = mutableStateListOf<Game>()
    var upcomingGames = mutableStateListOf<Game>()
    var posts = mutableStateListOf<LibraryEntry>()

    var user by mutableStateOf<User?>(null)

    init {
        viewModelScope.launch {
            val response = SafeRequest {
                IGDBClient.multiquery {
                    query(IgdbEndpoint.GAME, "Most loved") {
                        fields("name", "cover.image_id")
                        sort("rating", SortOrder.DESC)
                        where("parent_game = null & follows > 200")
                        limit(50)
                    }
                    query(IgdbEndpoint.GAME, "Upcoming") {
                        fields("name", "cover.image_id")
                        sort("first_release_date", SortOrder.ASC)
                        where(
                            "parent_game = null & first_release_date > " + java.time.Instant.now()
                                .toEpochMilli() / 1000
                        )
                        limit(50)
                    }
                    query(IgdbEndpoint.GAME, "Popular") {
                        fields("name", "cover.image_id")
                        sort("rating", SortOrder.DESC)
                        val yearSec = 31556926
                        where(
                            "parent_game = null & follows > 5 & first_release_date < " + java.time.Instant.now()
                                .toEpochMilli() / 1000
                                    + "& first_release_date > " + (java.time.Instant.now()
                                .toEpochMilli() / 1000).minus(yearSec)
                        )
                        limit(50)
                    }
                    query(IgdbEndpoint.GAME, "New") {
                        fields("name", "cover.image_id")
                        sort("first_release_date", SortOrder.DESC)
                        where(
                            "parent_game = null & first_release_date < " + java.time.Instant.now()
                                .toEpochMilli() / 1000
                        )
                        limit(50)
                    }
                }
            }
            listOf(mostLovedGames, upcomingGames, popularGames, newGames).forEachIndexed { index, value ->
                @Suppress("UNCHECKED_CAST")
                (response?.get(index)?.results as List<Game>?)?.let { value.addAll(it) }
            }
            fetchPosts()
        }
    }
    fun getUser() {
        viewModelScope.launch {
            user = userRepository.getUserByUid(
                Firebase.auth.currentUser?.uid!!
            ).first()
        }
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            libraryRepository.getAll().collectLatest {
                posts.clear()
                posts.addAll(it)
            }
        }
    }
}
