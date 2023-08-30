package it.unibo.gamelibrary.ui.views.Library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.model.Game
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private var libraryRepository: LibraryRepository
) : ViewModel() {
    var libraryEntries = mutableStateListOf<LibraryEntry>()
    val games = mutableStateMapOf<Long, Game>()
    var loading by mutableStateOf(true)
    init {
        fetchLibraryEntries()
    }

    fun fetchLibraryEntries() = viewModelScope.launch {
        libraryRepository.getUserLibraryEntries(Firebase.auth.currentUser!!.uid).collectLatest {
            libraryEntries.clear()
            libraryEntries.addAll(it)
            fetchGames(libraryEntries.map { it.gameId })
        }
    }

    fun fetchGames(ids: List<Int>) = viewModelScope.launch {
        if (ids.isEmpty()) return@launch
        val result = SafeRequest {
            IGDBClient.getGames {
                fields(
                    "id",
                    "name",
                    "cover.image_id"
                )
                where("id = (${ids.joinToString(",")})")
            }
        }
        val allGames = result?.games?.associateBy { it.id }
        games.clear()
        games.putAll(allGames ?: emptyMap())
        loading = false
    }
}