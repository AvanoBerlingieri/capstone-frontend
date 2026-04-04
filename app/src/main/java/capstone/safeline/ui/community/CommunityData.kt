package capstone.safeline.ui.community

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object CommunityData {

    val groupChats = mutableStateListOf(
        GroupChat(
            id = "1",
            name = mutableStateOf("T177"),
            users = mutableStateListOf("Alex", "John", "Maria")
        )
    )

    data class GroupChat(
        val id: String,
        val name: MutableState<String>,
        val users: SnapshotStateList<String>
    )


    val servers = mutableStateListOf<String>()
    val channelsMap = mutableStateMapOf<String, MutableList<String>>()

    val usersMap = mutableStateMapOf(
        "TestServer" to mutableStateListOf("Alex", "John", "Maria")
    )

    val rolesMap = mutableStateMapOf<String, MutableMap<String, SnapshotStateList<Role>>>()

    data class Role(
        var name: String,
        var permissions: MutableMap<String, String>
    )
}