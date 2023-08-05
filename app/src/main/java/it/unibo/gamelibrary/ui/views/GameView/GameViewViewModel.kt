package it.unibo.gamelibrary.ui.views.GameView

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class GameViewViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {
    interface LibraryEntryDetails {
        var status: LibraryEntryStatus?
        var rating: MutableIntState
        var notes: String
        var entry: LibraryEntry?
    }

    var game by mutableStateOf<Game?>(null)
    var isGameLibraryEditOpen by mutableStateOf(false)
    val libraryEntry = object : LibraryEntryDetails {
        override var status by mutableStateOf<LibraryEntryStatus?>(null)
        override var rating = mutableIntStateOf(0)
        override var notes by mutableStateOf("")
        override var entry by mutableStateOf<LibraryEntry?>(null)
    }

    fun getGame(gameId: Int) = viewModelScope.launch {
        val games = IGDBApiRequest {
            IGDBWrapper.games(
                APICalypse()
                    .fields(
                        listOf(
                            "name",
                            "artworks.image_id",
                            "cover.image_id",
                            "involved_companies.*",
                            "involved_companies.company.name",
                            "genres.name",
                            "genres.slug",
                            "screenshots.image_id",
                            "summary",
                            "release_dates.human",
                            "release_dates.platform.name",
                            "release_dates.platform.platform_logo.url"
                        ).joinToString(",")
                    )
                    .where("id = $gameId")
            )
        }
        game = games[0]
        getUserLibraryEntry(gameId, Firebase.auth.currentUser!!.uid)
    }

    fun getUserLibraryEntry(gameId: Int, userId: String) = viewModelScope.launch {
        libraryEntry.entry = libraryRepository.getLibraryEntryByUserAndGame(userId, gameId.toString())
        libraryEntry.status = libraryEntry.entry?.status
        libraryEntry.rating.intValue = libraryEntry.entry?.rating ?: 0
        libraryEntry.notes = libraryEntry.entry?.notes ?: ""
    }

    fun saveGameToLibrary() = viewModelScope.launch {
//        Log.d("GameViewViewModel", libraryEntry.entry.toString())
        if (libraryEntry.entry?.id != null && libraryEntry.entry?.id != 0) {
            libraryEntry.entry!!.status = libraryEntry.status
            libraryEntry.entry!!.rating = libraryEntry.rating.intValue
            libraryEntry.entry!!.notes = libraryEntry.notes
            libraryRepository.updateEntry(libraryEntry.entry!!)
            viewModelScope.launch { snackbarHostState.showSnackbar("Game in library updated!") }
        } else {
            libraryEntry.entry = LibraryEntry(
                uid = Firebase.auth.currentUser!!.uid,
                gameId = game!!.id.toInt(),
                status = libraryEntry.status,
                rating = libraryEntry.rating.intValue,
                notes = libraryEntry.notes
            )
            libraryRepository.insertEntry(libraryEntry.entry!!)
            getUserLibraryEntry(game!!.id.toInt(), Firebase.auth.currentUser!!.uid)
            viewModelScope.launch { snackbarHostState.showSnackbar("Game added to library!") }
        }
        isGameLibraryEditOpen = false
//        Log.d("GameViewViewModel AFTER", libraryEntry.entry.toString())
    }
}