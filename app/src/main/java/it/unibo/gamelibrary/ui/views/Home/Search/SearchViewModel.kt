package it.unibo.gamelibrary.ui.views.Home.Search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import com.api.igdb.request.genres
import com.api.igdb.request.platforms
import com.google.protobuf.GeneratedMessageV3
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Game
import proto.Genre
import proto.Platform
import javax.inject.Inject

enum class SearchType(val text: String) {
    GAMES("Games"),
    USERS("Users")
}

data class FilterState<out T>(
    var values: SnapshotStateList<@UnsafeVariance T>,
    var selected: SnapshotStateList<@UnsafeVariance T> = mutableStateListOf())

data class SearchTypeObject<R, F> (
    private val defaultQuery: String = "",
    private val defaultInProgress: Boolean = false,
    var results: SnapshotStateList<R> = mutableStateListOf(),
    val filters: Map<FilterType, FilterState<F>> = mapOf()
) {
    var query: String by mutableStateOf(defaultQuery)
    var inProgress: Boolean by mutableStateOf(false)
    var showDLCs: Boolean by mutableStateOf(true)
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val platforms = mutableStateListOf<Platform>()
    val genres = mutableStateListOf<Genre>()

    var searchType by mutableStateOf(SearchType.GAMES)

    val gamesSearch = SearchTypeObject<Game, GeneratedMessageV3>(
        filters = mapOf(
            FilterType.PLATFORMS to FilterState(platforms),
            FilterType.GENRES to FilterState(genres),
        )
    )
    val usersSearch = SearchTypeObject<User, Any>()

    init {
        fetchPlaforms()
        fetchGenres()
    }

    fun search() {
        val query = APICalypse()
            .fields("id,name,cover.image_id")
            .search(gamesSearch.query)
            .limit(500)

        val where = mutableListOf<String>()

        for ((filter, state) in gamesSearch.filters) {
            if (state.selected.isNotEmpty()) {
                where.add("${filter.apiField} = (${state.selected.joinToString(",") { when (it) {
                    is Platform -> it.id.toString()
                    is Genre -> it.id.toString()
                    else -> ""
                }} })")
            }
        }

        if (!gamesSearch.showDLCs) {
            where.add("parent_game = null")
        }

        if (where.isNotEmpty()) {
            query.where(where.joinToString(" & "))
        }

        fetchSearchedGames(query)
        fetchSearchedUsers(usersSearch.query)
    }

    private fun fetchSearchedGames(query: APICalypse) {
        gamesSearch.inProgress = true
        viewModelScope.launch {
            try {
                gamesSearch.results.clear()
                gamesSearch.results.addAll(IGDBApiRequest { IGDBWrapper.games(query) })
                gamesSearch.inProgress = false
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }

    private fun fetchSearchedUsers(query: String) {
        usersSearch.inProgress = true
        viewModelScope.launch {
            try {
                usersSearch.results.clear()
                usersSearch.results.addAll(userRepository.searchUser(query))
                usersSearch.inProgress = false
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }

    private fun fetchPlaforms() {
        viewModelScope.launch {
            try {
                platforms.clear()
                platforms.addAll(IGDBApiRequest { IGDBWrapper.platforms(APICalypse()
                    .fields("slug,name,platform_logo.url")
                    .sort("generation", Sort.DESCENDING)
                    .where("generation != null")
                    .limit(30)
                ) })
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                genres.clear()
                genres.addAll(IGDBApiRequest { IGDBWrapper.genres(APICalypse().fields("slug,name").limit(25)) })
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }
}
