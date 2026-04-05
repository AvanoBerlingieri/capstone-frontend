package capstone.safeline.apis.dto.messaging

import com.google.gson.annotations.SerializedName

data class GroupInfoDto(
    @SerializedName(value = "groupId", alternate = ["id", "group_id"])
    val groupId: String,
    @SerializedName(value = "groupName", alternate = ["name", "group_name"])
    val groupName: String
)
