package it.unibo.gamelibrary.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.api.igdb.exceptions.RequestException
import com.google.protobuf.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Timestamp.toInstant() = java.time.Instant.ofEpochSecond(seconds, nanos.toLong())

val snackbarHostState = SnackbarHostState()

var notificationId by mutableIntStateOf(0)
var channel_id by mutableStateOf("")

val requestAttempts = mutableMapOf<Int, Int>()

suspend fun <T> IGDBApiRequest(requestId: Int? = null, apiRequest: () -> T): T? = withContext(Dispatchers.IO) {
    try {
        apiRequest()
    } catch (e: RequestException) {
        val response = e.request.response().second;
        Log.e("IGDBApiRequest", e.request.toString())
        Log.e("IGDBApiRequest", response.toString())
        val key = requestId ?: apiRequest.hashCode()
        if ((response.statusCode == 401) && requestAttempts.getOrDefault(apiRequest.hashCode(), 0) < 3) {
            requestAttempts[key] = requestAttempts.getOrDefault(key, 0) + 1
            Log.i("IGDBApiRequest", "Retrying request")
            return@withContext IGDBApiRequest(key, apiRequest)
        }
        null
    }
}

fun Context.findActivity(): FragmentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

fun Activity.restartActivity() {
    recreate()
}