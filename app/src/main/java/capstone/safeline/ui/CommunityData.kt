package capstone.safeline.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

object CommunityData {
    val servers = mutableStateListOf<String>()
    val channelsMap = mutableStateMapOf<String, MutableList<String>>()
}