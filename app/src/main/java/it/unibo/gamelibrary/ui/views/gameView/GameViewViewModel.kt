package it.unibo.gamelibrary.ui.views.gameView

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.unibo.gamelibrary.NotificationWorker
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.LibraryEntryStatus
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.safeRequest
import it.unibo.gamelibrary.utils.snackBarHostState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.model.Game
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") // False positive - https://stackoverflow.com/questions/66216839/inject-context-with-hilt-this-field-leaks-a-context-object
class GameViewViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
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
    val libraryEntries = mutableStateListOf<LibraryEntry>()

    var openNotificationDialog by mutableStateOf(true)
    var notShowAgainNotification by mutableStateOf(false)
    var isLoading by mutableStateOf(true)
    var currentUser by mutableStateOf<User?>(null)

    init {
        viewModelScope.launch {
            userRepository.getUserByUid(Firebase.auth.currentUser!!.uid).collectLatest {
                currentUser = it
            }
        }
    }

    fun getGame(gameId: Int) = viewModelScope.launch {
        val result = safeRequest {
            IGDBClient.getGames {
                fields(
                    "name",
                    "artworks.image_id",
                    "cover.image_id",
                    "first_release_date",
                    "involved_companies.*",
                    "involved_companies.company.name",
                    "genres.name",
                    "genres.slug",
                    "screenshots.image_id",
                    "summary",
                    "release_dates.human",
                    "release_dates.platform.name",
                    "release_dates.platform.platform_logo.url"
                )
                where("id = $gameId")
            }
        }
        game = result?.games?.firstOrNull()
        getUserLibraryEntry(gameId, Firebase.auth.currentUser!!.uid)
        isLoading = false
    }

    private fun getUserLibraryEntry(gameId: Int, userId: String) = viewModelScope.launch {
        libraryRepository.getLibraryEntryByUserAndGame(userId, gameId.toString()).collectLatest {
            libraryEntry.entry = it
            libraryEntry.status = it?.status
            libraryEntry.rating.intValue = it?.rating ?: 0
            libraryEntry.notes = it?.notes ?: ""
        }
    }

    fun getLibraryEntries(gameId: Int) = viewModelScope.launch {
        libraryRepository.getLibraryEntriesByGame(Game(gameId.toLong()), "last_modified")
            .collectLatest {
                libraryEntries.clear()
                libraryEntries.addAll(it)
            }
    }

    fun saveGameToLibrary() {
        if (libraryEntry.status === null) {
            viewModelScope.launch { snackBarHostState.showSnackbar("Please select a status!") }
            return
        }
        viewModelScope.launch {
            if (libraryEntry.entry?.id != null && libraryEntry.entry?.id != 0) {
                libraryEntry.entry!!.status = libraryEntry.status!!
                libraryEntry.entry!!.rating = libraryEntry.rating.intValue
                libraryEntry.entry!!.notes = libraryEntry.notes
                libraryEntry.entry!!.lastModified = Instant.now().epochSecond
                libraryRepository.updateEntry(libraryEntry.entry!!)
                viewModelScope.launch { snackBarHostState.showSnackbar("Game in library updated!") }
            } else {
                libraryEntry.entry = LibraryEntry(
                    uid = Firebase.auth.currentUser!!.uid,
                    gameId = game!!.id.toInt(),
                    status = libraryEntry.status!!,
                    rating = libraryEntry.rating.intValue,
                    notes = libraryEntry.notes,
                    lastModified = Instant.now().epochSecond
                )
                libraryRepository.insertEntry(libraryEntry.entry!!)
                viewModelScope.launch { snackBarHostState.showSnackbar("Game added to library!") }
            }

            enableNotification()
            isGameLibraryEditOpen = false
        }
    }

    fun removeGameFromLibrary() = viewModelScope.launch {
        libraryRepository.deleteEntry(libraryEntry.entry!!)
        libraryEntry.entry = null
        libraryEntry.status = null
        libraryEntry.rating.intValue = 0
        libraryEntry.notes = ""
        viewModelScope.launch { snackBarHostState.showSnackbar("Game removed from library!") }
    }

    private fun enableNotification() {
        val releaseDate = game!!.first_release_date
        val secondDate = releaseDate?.minusSeconds(Instant.now().epochSecond)
        Log.i("GameId", libraryEntry.entry?.gameId!!.toString())
        if (secondDate != null && secondDate.epochSecond >= 0) {
            val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(Duration.ofSeconds(secondDate.epochSecond))
                .setInputData(
                    Data.Builder()
                        .putInt("gameId", libraryEntry.entry?.gameId!!)
                        .putString("gameName", game?.name)
                        .build()
                )
                .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    libraryEntry.entry?.gameId!!.toString(),
                    ExistingWorkPolicy.REPLACE,
                    notificationWorker
                )

        }
    }

    fun shareGame(context: Context) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "https://game-library.app/game/${game?.id}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
}