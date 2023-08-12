package it.unibo.gamelibrary.ui.views.Home.UserReview

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class UserReviewViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var game = mutableStateMapOf<Int, Game?>()
    var user = mutableStateMapOf<String, User?>()

    fun getUser(uid: String) {
        viewModelScope.launch {
            user[uid] = userRepository.getUserByUid(uid)
        }
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
        game[gameId] = games[0]
    }
}