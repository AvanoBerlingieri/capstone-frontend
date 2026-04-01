package capstone.safeline.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import capstone.safeline.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "secure_store")

class DataStoreManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {
    companion object {
        private val ENCRYPTED_TOKEN = stringPreferencesKey("encrypted_token")
        private val IV_KEY = stringPreferencesKey("iv_key")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")

        private val USER_ID = stringPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        val encrypted = prefs[ENCRYPTED_TOKEN]
        val iv = prefs[IV_KEY]

        if (encrypted != null && iv != null) {
            cryptoManager.decrypt(encrypted, iv)
        } else null
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { it[USERNAME] ?: "User" }

    val emailFlow: Flow<String> = context.dataStore.data.map { it[EMAIL] ?: "No Email" }

    val userIdFlow: Flow<String> = context.dataStore.data.map { it[USER_ID] ?: "" }

    suspend fun saveUserInfo(username: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME] = username
            prefs[EMAIL] = email
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    suspend fun saveToken(token: String) {
        val result = cryptoManager.encrypt(token)
        if (result != null) {
            val (encrypted, iv) = result
            context.dataStore.edit { prefs ->
                prefs[ENCRYPTED_TOKEN] = encrypted
                prefs[IV_KEY] = iv
            }
        }
    }


    // clear just the token
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(ENCRYPTED_TOKEN)
            prefs.remove(IV_KEY)
        }
    }

    // clears everything from datastore
    suspend fun clearAll() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            context.dataStore.edit { it.clear() }
        }
    }
}