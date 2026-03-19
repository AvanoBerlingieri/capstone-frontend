package capstone.safeline.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
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
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        val encrypted = prefs[ENCRYPTED_TOKEN]
        val iv = prefs[IV_KEY]

        if (encrypted != null && iv != null) {
            cryptoManager.decrypt(encrypted, iv)
        } else null
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

    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }
}