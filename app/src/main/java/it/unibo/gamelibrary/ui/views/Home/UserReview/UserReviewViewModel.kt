package it.unibo.gamelibrary.ui.views.Home.UserReview

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.getGames
import ru.pixnews.igdbclient.model.Game
import javax.inject.Inject

@HiltViewModel
class UserReviewViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var game = mutableStateMapOf<Int, Game?>()
    var user = mutableStateMapOf<String, User?>()

    fun getUser(uid: String) {
        viewModelScope.launch {
            userRepository.getUserByUid(uid).collectLatest {
                user[uid] = it
            }
        }
    }

    fun getGame(gameId: Int) = viewModelScope.launch {
        val result = SafeRequest {
            IGDBClient.getGames {
                fields(
                    "name",
                    "cover.image_id"
                )
                where("id = $gameId")
            }
        }
        if (result != null) {
            game[gameId] = result.games.firstOrNull()
        }
    }
}