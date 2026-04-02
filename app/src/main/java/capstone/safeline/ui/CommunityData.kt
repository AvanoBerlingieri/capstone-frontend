package capstone.safeline.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object CommunityData {
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