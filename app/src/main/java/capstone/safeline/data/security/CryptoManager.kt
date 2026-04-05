package capstone.safeline.data.security

import android.security.keystore.*
import android.util.Base64
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {
    companion object {
        @Volatile
        private var INSTANCE: CryptoManager? = null

        fun getInstance(): CryptoManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CryptoManager().also { INSTANCE = it }
            }
        }
    }

    private val keyAlias = "jwt_key"
    private val transformation = "AES/GCM/NoPadding"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        keyStore.getKey(keyAlias, null)?.let { return it as SecretKey }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        generator.init(spec)
        return generator.generateKey()
    }

    fun encrypt(data: String): Pair<String, String>? {
        return try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray())

            val encrypted = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)

            Pair(encrypted, ivString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(encryptedData: String, iv: String): String? {
        return try {
            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
            String(cipher.doFinal(decoded))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}