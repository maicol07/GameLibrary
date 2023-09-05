package it.unibo.gamelibrary

import it.unibo.gamelibrary.utils.SecurePreferences
import android.content.Context
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

class TwitchTokenSecurePreferencesStorage(context: Context): TwitchTokenStorage {
    private var tokenValidity = mutableMapOf(
        "" to false
    )
    private val prefs = SecurePreferences(context)
    private var token: TwitchTokenPayload = NO_TOKEN

    private val lock = Mutex()

    override suspend fun getToken(): TwitchTokenPayload {
        return lock.withLock {
            val loadedToken = prefs.getString("twitch_token")
            if (token == NO_TOKEN && loadedToken != null) {
                token = TwitchTokenPayload(loadedToken)
            }

            validateToken()
            token
        }
    }

    override suspend fun updateToken(
        oldToken: TwitchTokenPayload,
        newToken: TwitchTokenPayload
    ): Boolean {
        return lock.withLock {
            if (oldToken == token) {
                val newTokenString = newToken.getTwitchAccessToken()

                if (newTokenString == null) {
                    prefs.remove("twitch_token")
                } else {
                    prefs.putString("twitch_token", newTokenString)
                }

                token = newToken
                true
            } else {
                false
            }
        }
    }

    private suspend fun validateToken() {
        val tokenString = token.getTwitchAccessToken()
        val tokenValid = tokenValidity[tokenString]
        if (tokenString != null && tokenValid == null) {
            val result = Http.get("https://id.twitch.tv/oauth2/validate") {
                header("Authorization", "OAuth $tokenString")
            }
            tokenValidity[tokenString] = result.status == HttpStatusCode.OK
        }
    }
}