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
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import it.unibo.gamelibrary.BuildConfig
import ru.pixnews.igdbclient.IgdbClient
import java.net.UnknownHostException

val snackbarHostState = SnackbarHostState()

var notificationId by mutableIntStateOf(0)
var channel_id by mutableStateOf("")

suspend fun <T> safeRequest(apiRequest: suspend () -> T): T? {
    return try {
        apiRequest()
    } catch (e: Exception) {
        if (e is UnknownHostException || e.cause is UnknownHostException) {
            Log.e("IGDBApiRequest", "No internet connection")
        } else {
            Log.e("IGDBApiRequest", e.toString())
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

val Http = HttpClient(Android) {
    developmentMode = BuildConfig.DEBUG
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.d("Http", message)
            }
        }
        level = LogLevel.INFO
    }
    install(HttpCache)
}

lateinit var IGDBClient: IgdbClient

fun Activity.restartActivity() {
    recreate()
}