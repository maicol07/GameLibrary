package it.unibo.gamelibrary.ui.views.GameView

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class GameViewViewModel @Inject constructor(): ViewModel() {
    var game by mutableStateOf<Game>(Game.getDefaultInstance())

    private fun getGame(query: APICalypse) = viewModelScope.launch {
        val games = IGDBWrapper.games(query)
        game = games[0]
    }
}