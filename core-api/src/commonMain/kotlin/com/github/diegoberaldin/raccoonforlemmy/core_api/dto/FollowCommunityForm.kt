package com.github.diegoberaldin.raccoonforlemmy.core_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowCommunityForm(
    @SerialName("community_id") val communityId: CommunityId,
    @SerialName("follow") val follow: Boolean,
    @SerialName("auth") val auth: String,
)