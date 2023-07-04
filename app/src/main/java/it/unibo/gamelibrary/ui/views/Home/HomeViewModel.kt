package it.unibo.gamelibrary.ui.views.Home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
): ViewModel() {

    var newGames = mutableListOf<Game>()
    var popularGames = mutableListOf<Game>()
    //ecc..

    init {
        fetchNewGames()
        fetchPopularGames()
    }

    fun fetchPopularGames(){ //TODO la query è la stessa di new, cambiala
        fetchList(APICalypse()
            .fields("*")
            //.exclude("*")
            .limit(10)
            .offset(0)
            .sort("release_dates.date", Sort.DESCENDING),
            popularGames
        )
    }

    fun fetchNewGames(){ //TODO controlla se la query funziona, anche su nightingale
        fetchList(APICalypse()
            .fields("*")
            .exclude("*")
            .limit(10)
            .offset(0)
            .sort("release_dates.date", Sort.DESCENDING),
            newGames
        )
    }

    private fun fetchList(query: APICalypse, list: MutableList<Game>) = viewModelScope.launch {
        try {
            list.clear()
             list.addAll(IGDBWrapper.games(query))
        }
        catch (e: RequestException){
            Log.e("HomeViewModel", e.message ?: "")
        }
    }

}