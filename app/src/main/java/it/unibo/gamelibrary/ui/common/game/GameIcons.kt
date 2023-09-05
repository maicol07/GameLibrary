package it.unibo.gamelibrary.ui.common.game

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.ViewInAr
import it.unibo.gamelibrary.R
import ru.pixnews.igdbclient.model.Genre

val Genre.icon: Any?
    get() = when (this.slug) {
            "fighting" -> Icons.Default.SportsMma
            "shooter" -> R.drawable.pistol
            "music" -> Icons.Default.MusicNote
            "platform" -> Icons.Default.ViewInAr
            "puzzle" -> Icons.Default.Extension
            "racing" -> Icons.Default.SportsMotorsports
            "real-time-strategy-rts", "strategy", "turn-based-strategy-tbs", "tactical" -> R.drawable.strategy
            "role-playing-rpg" -> R.drawable.wizard_hat
            "adventure" -> Icons.Default.Explore
            "simulator" -> Icons.Default.Diamond
            "sport" -> Icons.Default.SportsTennis
            "quiz-trivia" -> Icons.Default.QuestionAnswer
            "hack-and-slash-beat-em-up" -> R.drawable.fencing
            "pinball" -> Icons.Default.SportsBaseball
            "arcade" -> Icons.Default.VideogameAsset
            "visual-novel" -> Icons.Default.Book
            "indie" -> Icons.Default.Build
            "card-and-board-game" -> Icons.Default.Casino
            "moba" -> Icons.Default.SportsEsports
            "point-and-click" -> Icons.Default.Mouse
            else -> null
        }