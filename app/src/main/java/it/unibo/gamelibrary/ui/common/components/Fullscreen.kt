package it.unibo.gamelibrary.ui.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun Fullscreen(
    fullscreenState: MutableState<Boolean> = remember { mutableStateOf(false) },
    onDismissRequest: () -> Unit = { fullscreenState.value = false },
    dialogProperties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit) {
    if (fullscreenState.value) {
        Dialog(onDismissRequest = onDismissRequest, properties = dialogProperties) {
            content()
        }
    }
}