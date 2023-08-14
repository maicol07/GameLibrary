package it.unibo.gamelibrary.ui.views.Home

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var newGames = mutableStateListOf<Game>()
    var mostLovedGames = mutableStateListOf<Game>()
    var popularGames = mutableStateListOf<Game>()
    var upcomingGames = mutableStateListOf<Game>()
    var posts = mutableStateListOf<LibraryEntry>()

    //"users" used in testing.
    var users = mutableStateListOf<User>()

    init {
        fetchNewGames()
        fetchMostLoved()
        fetchPopularGames()
        fetchPosts()
        fetchUpcoming()
        fetchUsers()
    }

    fun fetchMostLoved() {
        fetchList(
            APICalypse()
                .fields("*,cover.image_id")
                .sort("rating", Sort.DESCENDING)
                .where("parent_game = null & follows > 200")
                .limit(50),
            mostLovedGames
        )
    }

    fun fetchUpcoming() {
        fetchList(
            APICalypse()
                .fields("*,cover.image_id")
                .sort("first_release_date", Sort.ASCENDING)
                .where(
                    "parent_game = null & first_release_date > " + java.time.Instant.now()
                        .toEpochMilli() / 1000
                )
                .limit(50),
            upcomingGames
        )
    }

    fun fetchPopularGames() {//giochi rilasciati nell'ultimo anno
        val yearSec = 31556926
        fetchList(
            APICalypse()
                .fields("*,cover.image_id")
                .sort("rating", Sort.DESCENDING)
                .where("parent_game = null & follows > 5 & first_release_date < " + java.time.Instant.now().toEpochMilli() / 1000
                        + "& first_release_date > " + (java.time.Instant.now().toEpochMilli() / 1000).minus(yearSec))
                .limit(50),
            popularGames
        )
    }

    fun fetchNewGames() {
        fetchList(
            APICalypse()
                .fields("*,cover.image_id")
                .sort("first_release_date", Sort.DESCENDING)
                .where(
                    "parent_game = null & first_release_date < " + java.time.Instant.now()
                        .toEpochMilli() / 1000
                )
                .limit(50),
            newGames
        )
    }

    private fun fetchList(query: APICalypse, list: MutableList<Game>) {
        viewModelScope.launch {
            try {
                list.clear()
                list.addAll(IGDBApiRequest { IGDBWrapper.games(query) })
                //Log.i("fetch list, list =", list[0].toString())
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            posts.addAll(libraryRepository.getAll())
        }
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            users.addAll(userRepository.getAll())
        }
    }

}
