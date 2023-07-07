package it.unibo.gamelibrary.utils

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val snackbarHostState = SnackbarHostState()

suspend fun <T> IGDBApiRequest(apiRequest: () -> T): T = withContext(Dispatchers.IO) {
    apiRequest()
}