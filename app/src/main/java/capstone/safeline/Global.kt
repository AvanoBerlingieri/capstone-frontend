package capstone.safeline
import android.content.Context
import android.util.Log
import capstone.safeline.models.ChatUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Global {
    fun loadUsersChats(context: Context): List<ChatUser> {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.usermsgs)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            val listType = object : TypeToken<List<ChatUser>>() {}.type
            Gson().fromJson<List<ChatUser>>(jsonString, listType)

        } catch (e: Exception) {
            Log.e("Global", "Failed to load user chats", e)
            emptyList()
        }
    }
}
