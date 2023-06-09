package it.unibo.gamelibrary.ui.views.Home

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.artworks
import com.api.igdb.request.covers
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proto.Cover
import proto.Game
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
): ViewModel() {

    var newGames = mutableStateListOf<Game>()
    var popularGames = mutableStateListOf<Game>()
    //ecc..

    init {
//        games = IGDBWrapper.games(APICalypse()
//            .fields("*")
//            //.exclude("*")
//            .limit(10)
//            .offset(0)
//            .sort("release_dates.date", Sort.DESCENDING))
        fetchNewGames()
        //fetchPopularGames()
        //Log.i("HomeViewModel, game",newGames.toString())
    }

    fun fetchPopularGames() { //TODO la query è la stessa di new, cambiala
        fetchList(
            APICalypse()
                .fields("*")
                //.exclude("*")
                .limit(10)
                .offset(0)
                .sort("release_dates.date", Sort.DESCENDING),
            popularGames
        )
    }

    fun fetchNewGames() { //TODO controlla se la query funziona, anche su nightingale
        fetchList(
            APICalypse()
                .fields("*")
                .sort("release_dates.date", Sort.DESCENDING)
                .limit(10),
            newGames
        )
    }

    private fun fetchList(query: APICalypse, list: MutableList<Game>) {
        viewModelScope.launch {
            try {
                list.clear()
                list.addAll(IGDBApiRequest { IGDBWrapper.games(query) })
                //Log.i("fetch list, list =", list[0].toString())
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_GAME_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
    }

    fun getCoverUrl(query: APICalypse): String{
        var url = ""
        viewModelScope.launch {
            try {
                url = APIImage(query)[0].url
            } catch (e: RequestException) {
                Log.e("ERR_FETCH_IMAGE_LIST_HOME", "${e.statusCode} , ${e.message}")
            }
        }
        return url;
    }

    suspend fun APIImage(query: APICalypse): List<Cover> = withContext(Dispatchers.IO) {
        IGDBWrapper.covers(query)
    }
}