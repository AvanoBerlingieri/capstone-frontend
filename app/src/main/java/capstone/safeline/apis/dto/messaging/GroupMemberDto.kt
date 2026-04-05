package capstone.safeline.apis.dto.messaging

import com.google.gson.annotations.SerializedName

/** Nested user object when the API returns e.g. `{ "user": { "id": "...", "username": "..." } }`. */
data class GroupMemberUserSnippet(
    @SerializedName(value = "id", alternate = ["userId", "user_id"])
    val id: String? = null,
    @SerializedName(value = "username", alternate = ["userName", "name"])
    val username: String? = null,
    val email: String? = null
)

data class GroupMemberDto(
    @SerializedName(
        value = "userId",
        alternate = ["id", "user_id", "memberId", "member_id"]
    )
    val userId: String? = null,
    @SerializedName(value = "username", alternate = ["userName", "name", "displayName"])
    val username: String? = null,
    val email: String? = null,
    val user: GroupMemberUserSnippet? = null
)

fun GroupMemberDto.resolveMemberId(): String =
    listOf(userId, user?.id).firstOrNull { !it.isNullOrBlank() } ?: ""

fun GroupMemberDto.resolveDisplayUsername(): String =
    listOf(username, user?.username).firstOrNull { !it.isNullOrBlank() } ?: ""

fun GroupMemberDto.resolveEmail(): String =
    listOf(email, user?.email).firstOrNull { !it.isNullOrBlank() } ?: ""
