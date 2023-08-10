package it.unibo.gamelibrary.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.google.protobuf.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Timestamp.toInstant() = java.time.Instant.ofEpochSecond(seconds, nanos.toLong())

val snackbarHostState = SnackbarHostState()

var notificationId by mutableIntStateOf(0)

suspend fun <T> IGDBApiRequest(apiRequest: () -> T): T = withContext(Dispatchers.IO) {
    apiRequest()
}

fun Context.findActivity(): FragmentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}