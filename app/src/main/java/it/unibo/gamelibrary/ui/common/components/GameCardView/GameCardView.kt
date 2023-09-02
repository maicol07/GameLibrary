package it.unibo.gamelibrary.ui.common.components.GameCardView

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import it.unibo.gamelibrary.ui.destinations.GameViewNavDestination
import it.unibo.gamelibrary.ui.views.GameView.GameDetails
import it.unibo.gamelibrary.ui.views.GameView.GameHeader
import ru.pixnews.igdbclient.model.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCardView(
    game: Game,
    navController: NavController,
) {
    Card(
        onClick = {
            navController.navigate(GameViewNavDestination(game.id.toInt()))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {
        GameHeader(game = game)
        GameDetails(game = game, navController = navController)
    }
}