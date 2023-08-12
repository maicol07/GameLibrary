import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(context: Context) {

    private val masterKeyAlias =
        (MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build())
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "gamelibrary_secure_preferences",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}