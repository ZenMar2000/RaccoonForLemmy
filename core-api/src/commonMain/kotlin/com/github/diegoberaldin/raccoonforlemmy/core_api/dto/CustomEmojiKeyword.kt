package com.github.diegoberaldin.raccoonforlemmy.core_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomEmojiKeyword(
    @SerialName("id") val id: Int,
    @SerialName("custom_emoji_id") val customEmojiId: CustomEmojiId,
    @SerialName("keyword") val keyword: String,
)