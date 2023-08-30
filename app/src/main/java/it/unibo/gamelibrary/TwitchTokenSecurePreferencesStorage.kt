package it.unibo.gamelibrary

import SecurePreferences
import android.content.Context
import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import it.unibo.gamelibrary.utils.Http
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload.Companion.NO_TOKEN
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenPayload.Companion.getTwitchAccessToken
import ru.pixnews.igdbclient.auth.twitch.TwitchTokenStorage

class TwitchTokenSecurePreferencesStorage(
    context: Context
): TwitchTokenStorage {
    private var tokenValid = false
    private val prefs = SecurePreferences(context)

    private val lock = Mutex()
    override suspend fun getToken(): TwitchTokenPayload {
        return lock.withLock {
            val token = prefs.getString("twitch_token", "")
            if (token == "") {
                NO_TOKEN
            }

            if (!tokenValid) {
                validateToken(token)
                if (!tokenValid) {
                    NO_TOKEN
                }
            }

            TwitchTokenPayload(token)
        }
    }

    override suspend fun updateToken(
        oldToken: TwitchTokenPayload,
        newToken: TwitchTokenPayload
    ): Boolean {
        return lock.withLock {
            if (oldToken.getTwitchAccessToken() == prefs.getString("twitch_token", "")) {
                val newTokenString = newToken.getTwitchAccessToken()
                if (newTokenString != null) {
                    prefs.putString("twitch_token", newTokenString)
                }
                true
            } else {
                false
            }
        }
    }

    private suspend fun validateToken(token: String) {
        Log.d("TwitchTokenStorage", "Validating token $tokenValid")
        val result = Http.get("https://id.twitch.tv/oauth2/validate") {
            header("Authorization", "OAuth $token")
        }
        tokenValid = result.status == HttpStatusCode.OK
    }
}