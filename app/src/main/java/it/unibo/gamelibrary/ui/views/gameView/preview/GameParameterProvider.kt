package it.unibo.gamelibrary.ui.views.gameView.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ru.pixnews.igdbclient.model.Company
import ru.pixnews.igdbclient.model.Game
import ru.pixnews.igdbclient.model.Genre
import ru.pixnews.igdbclient.model.InvolvedCompany
import ru.pixnews.igdbclient.model.PlayerPerspective
import java.time.Instant

class GameParameterProvider : PreviewParameterProvider<Game> {
    override val values: Sequence<Game>
        get() = sequenceOf(
            Game(
                id = 1025,
                name = "Final Fantasy VII Rebirth",
                involved_companies = listOf(
                    InvolvedCompany(
                        company = Company(
                            name = "Square Enix"
                        )
                    )
                ),
                first_release_date = Instant.ofEpochSecond(1587552000),
                genres = listOf(
                    Genre(
                        name = "RPG"
                    )
                ),
                player_perspectives = listOf(
                    PlayerPerspective(
                        name = "Single player"
                    )
                )
            )
        )
}