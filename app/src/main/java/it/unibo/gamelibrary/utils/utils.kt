package it.unibo.gamelibrary.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val snackbarHostState = SnackbarHostState()

suspend fun <T> IGDBApiRequest(apiRequest: () -> T): T = withContext(Dispatchers.IO) {
    apiRequest()
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}