package it.unibo.gamelibrary.ui.views.Library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private var libraryRepository: LibraryRepository
) : ViewModel() {
    var libraryEntries = mutableStateListOf<LibraryEntry>()
    val games = mutableStateMapOf<Long, Game>()
    var loading by mutableStateOf(true)
    init {
        fetchLibraryEntries().invokeOnCompletion {
            fetchGames(libraryEntries.map { it.gameId })
        }
    }

    fun fetchLibraryEntries() = viewModelScope.launch {
        libraryEntries.clear()
        libraryEntries.addAll(libraryRepository.getUserLibraryEntries(Firebase.auth.currentUser!!.uid))
    }

    fun fetchGames(ids: List<Int>) = viewModelScope.launch {
        if (ids.isEmpty()) return@launch
        val allGames = IGDBApiRequest {
            IGDBWrapper.games(
                APICalypse()
                    .fields("id,name,cover.image_id")
                    .where("id = (${ids.joinToString(",")})")
            )
        }?.associate { it.id to it }
        games.clear()
        games.putAll(allGames ?: emptyMap())
        loading = false
    }
}