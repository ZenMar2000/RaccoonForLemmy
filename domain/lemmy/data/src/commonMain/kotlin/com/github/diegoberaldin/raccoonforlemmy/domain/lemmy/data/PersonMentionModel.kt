package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class PersonMentionModel(
    val id: Int = 0,
    val post: PostModel,
    val creator: UserModel,
    val comment: CommentModel,
    val community: CommunityModel,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    val myVote: Int,
    val saved: Boolean,
    val isOwnPost: Boolean = false,
    val publishDate: String? = null,
    val read: Boolean = false,
) : JavaSerializable