package it.unibo.gamelibrary.ui.views.Home

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.games
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.utils.IGDBApiRequest
import kotlinx.coroutines.launch
import proto.Game
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
): ViewModel() {

    var newGames = mutableStateListOf<Game>()
    var popularGames = mutableStateListOf<Game>()
    var posts = mutableStateListOf<LibraryEntry>()

    init {
        fetchNewGames()
        //fetchPopularGames()
        fetchPosts()
//        posts.sortedByDescending { it. }
    }

//    fun fetchPopularGames() { //TODO la query è la stessa di new, cambiala
//        fetchList(
//            APICalypse()
//                .fields("*,cover.image_id")
//                .sort("", Sort.DESCENDING)
//                .limit(25),
//            popularGames
//        )
//    }

    fun fetchNewGames() {
        fetchList( //fields *; where game.platforms = 48 & date < 1538129354; sort date desc;
            APICalypse()
                .fields("*,cover.image_id")
                .where("first_release_date < " + java.time.Instant.now().toEpochMilli() / 1000)//TODO filtra per edizoni per togliere le deluxe, premium ecc
                .sort("first_release_date", Sort.DESCENDING)
                .limit(25),
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

    private fun fetchPosts(){
        viewModelScope.launch {
            posts.addAll(libraryRepository.getAll())
        }
    }

}
//    suspend fun APIImage(query: APICalypse): List<Cover> = withContext(Dispatchers.IO) {
//        IGDBWrapper.covers(query)
//    }
//    fun getCoverUrl(query: APICalypse): String{
//        var url = ""
//        viewModelScope.launch {
//            try {
//                url = APIImage(query)[0].url
//            } catch (e: RequestException) {
//                Log.e("ERR_FETCH_IMAGE_LIST_HOME", "${e.statusCode} , ${e.message}")
//            }
//        }
//        return url;
//    }
