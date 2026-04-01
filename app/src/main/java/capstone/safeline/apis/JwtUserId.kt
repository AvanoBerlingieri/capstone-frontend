package capstone.safeline.apis

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Reads the current user id from the JWT payload.
 */
fun extractUserIdFromJwt(token: String): String? {
    return try {
        val json = decodeJwtPayloadJson(token) ?: return null
        val map: Map<String, Any> =
            Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type) ?: return null
        (map["sub"] as? String)?.trim()?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
}

private fun decodeJwtPayloadJson(token: String): String? {
    val parts = token.split('.')
    if (parts.size < 2) return null
    val segment = parts[1]
    val padding = "=".repeat((4 - (segment.length % 4)) % 4)
    val bytes = Base64.decode(segment + padding, Base64.URL_SAFE)
    return bytes.toString(Charsets.UTF_8)
}
