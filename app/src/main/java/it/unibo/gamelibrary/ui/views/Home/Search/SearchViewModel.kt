package it.unibo.gamelibrary.ui.views.Home.Search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.wire.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.apicalypse.ApicalypseQueryBuilder
import ru.pixnews.igdbclient.apicalypse.SortOrder
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.getGenres
import ru.pixnews.igdbclient.getPlatforms
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.Genre
import ru.pixnews.igdbclient.model.Platform
import javax.inject.Inject

enum class SearchType(val text: String) {
    GAMES("Games"),
    USERS("Users")
}

data class FilterState<out T>(
    var values: SnapshotStateList<@UnsafeVariance T>,
    var selected: SnapshotStateList<@UnsafeVariance T> = mutableStateListOf()
)

data class SearchTypeObject<R, F>(
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

    val gamesSearch = SearchTypeObject<Game, Message<out Message<*, Nothing>, Nothing>>(
        filters = mapOf(
            FilterType.PLATFORMS to FilterState(platforms),
            FilterType.GENRES to FilterState(genres),
        )
    )
    val usersSearch = SearchTypeObject<User, Any>()

    init {
        fetchPlaforms()
        fetchGenres()
        search()
    }

    fun search() {
        fetchSearchedGames {
            fields("id", "name", "cover.image_id")
            limit(500)

            if (gamesSearch.query.isNotEmpty()) {
                search(gamesSearch.query)
            }

            val whereFragments = mutableListOf<String>()

            for ((filter, state) in gamesSearch.filters) {
                if (state.selected.isNotEmpty()) {
                    val ids = state.selected.joinToString(",") {
                        when (it) {
                            is Platform -> it.id.toString()
                            is Genre -> it.id.toString()
                            else -> ""
                        }
                    }
                    whereFragments.add("${filter.apiField} = ($ids)")
                }
            }

            if (!gamesSearch.showDLCs) {
                whereFragments.add("parent_game = null")
            }

            if (whereFragments.isNotEmpty()) {
                where(whereFragments.joinToString(" & "))
            }
        }
        fetchSearchedUsers(usersSearch.query)
    }

    private fun fetchSearchedGames(query: ApicalypseQueryBuilder.() -> Unit) {
        gamesSearch.inProgress = true
        viewModelScope.launch {
            gamesSearch.results.clear()
            val result = SafeRequest { IGDBClient.getGames(query) }
            if (result != null) {
                gamesSearch.results.addAll(result.games)
            }
            gamesSearch.inProgress = false
        }
    }

    private fun fetchSearchedUsers(query: String) {
        usersSearch.inProgress = true
        viewModelScope.launch {
            usersSearch.results.clear()
            userRepository.searchUser(query).collectLatest {
                usersSearch.results.addAll(it)
                usersSearch.inProgress = false
            }
        }
    }

    private fun fetchPlaforms() {
        viewModelScope.launch {
            platforms.clear()
            val result = SafeRequest {
                IGDBClient.getPlatforms {
                    fields("slug", "name", "platform_logo.url")
                    sort("generation", SortOrder.DESC)
                    where("generation != null")
                    limit(30)
                }
            }
            if (result != null) {
                platforms.addAll(result.platforms)
            }
        }
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            genres.clear()
            val result = SafeRequest {
                IGDBClient.getGenres {
                    fields("slug", "name")
                    limit(25)
                }
            }
            if (result != null) {
                genres.addAll(result.genres)
            }
        }
    }
}
