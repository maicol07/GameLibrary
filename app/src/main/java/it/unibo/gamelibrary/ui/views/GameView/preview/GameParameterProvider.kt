package it.unibo.gamelibrary.ui.views.GameView.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.protobuf.Timestamp
import proto.Company
import proto.Cover
import proto.Game
import proto.Genre
import proto.InvolvedCompany
import proto.PlayerPerspective

class GameParameterProvider : PreviewParameterProvider<Game> {
    override val values: Sequence<Game>
        get() = sequenceOf(
            Game.newBuilder()
                .setId(1025)
                .setName("Final Fantasy VII Rebirth")
                .setCover(
                    Cover.newBuilder()
                        .setUrl("https://images.igdb.com/igdb/image/upload/t_cover_big/co2t3f.jpg")
                        .build()
                )
                .addInvolvedCompanies(
                    InvolvedCompany.newBuilder()
                        .setCompany(
                            Company.newBuilder()
                                .setName("Square Enix")
                                .build()
                        )
                        .build()
                )
                .setFirstReleaseDate(Timestamp.newBuilder().setSeconds(1587552000).build())
                .addGenres(
                    Genre.newBuilder()
                        .setName("RPG")
                        .build()
                )
                .addPlayerPerspectives(
                    PlayerPerspective.newBuilder()
                        .setName("Single player")
                        .build()
                )
                .build()
        )
}