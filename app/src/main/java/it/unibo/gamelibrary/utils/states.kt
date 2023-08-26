package it.unibo.gamelibrary.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TopAppBarState {
    var title by mutableStateOf("")
    var customTitle by mutableStateOf<(@Composable () -> Unit)?>(null)
    var actions by mutableStateOf<@Composable RowScope.() -> Unit>({})
    var show by mutableStateOf(true)

    fun restoreDefaults() {
        title = ""
        customTitle = null
        actions = {}
        show = true
    }
}

var BottomBar by mutableStateOf<@Composable () -> Unit>({})
