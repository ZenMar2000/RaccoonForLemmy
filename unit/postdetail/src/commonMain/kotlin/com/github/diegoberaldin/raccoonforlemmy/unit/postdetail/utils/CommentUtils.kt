package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

internal fun List<CommentModel>.populateLoadMoreComments() = mapIndexed { idx, comment ->
    val hasMoreComments = (comment.comments ?: 0) > 0
    val isNextCommentNotChild =
        idx < lastIndex && this[idx + 1].depth <= comment.depth
    comment.copy(loadMoreButtonVisible = hasMoreComments && isNextCommentNotChild)
}

/*
 * CREDITS:
 * This is loosely inspired by the "missing node" algorithm found in
 * https://github.com/dessalines/jerboa/blob/21e2222a4fb2098000bef7254dd5c566a1f6a395/app/src/main/java/com/jerboa/Utils.kt
 */

internal fun List<CommentModel>.sortToNestedOrder(ancestorId: Int? = null): List<CommentModel> {
    // populate a memo for quick access
    val memo = mutableMapOf<Int, CommentNode>()
    for (comment in this) {
        val node = CommentNode.Actual(comment = comment)
        memo[comment.id] = node
    }

    // reconstructs the missing nodes and relationships between nodes
    for (comment in this) {
        memo[comment.id]?.also { node ->
            connectNodesAndGeneratePlaceholders(
                memo = memo,
                currentPath = comment.path,
                currentNode = node,
                rootCommentId = ancestorId
            )
        }
    }


    // joins the forest under a single root
    val root = joinForestUnderSingleRoot(
        ancestorId = ancestorId,
        memo = memo
    )

    // linearize the tree and convert to comment list
    return mutableListOf<CommentNode>().apply {
        linearize(
            node = root,
            list = this,
        )
    }.map { node ->
        when (node) {
            is CommentNode.Actual -> node.comment
            is CommentNode.Placeholder -> CommentModel(
                id = node.id,
                text = "",
                removed = true,
            )
        }
    }
}

data class PlaceholderComment(
    val id: Int,
    val path: String,
)

private sealed interface CommentNode {
    val children: MutableList<CommentNode>
    var parent: CommentNode?
    val id: Int

    data class Actual(
        val comment: CommentModel,
        override val children: MutableList<CommentNode> = mutableListOf(),
        override var parent: CommentNode? = null,
    ) : CommentNode {
        override val id = comment.id
    }

    data class Placeholder(
        val missingComment: PlaceholderComment,
        override val children: MutableList<CommentNode> = mutableListOf(),
        override var parent: CommentNode? = null,
    ) : CommentNode {
        override val id = missingComment.id
    }
}

private fun connectNodesAndGeneratePlaceholders(
    memo: MutableMap<Int, CommentNode>,
    currentPath: String,
    currentNode: CommentNode,
    rootCommentId: Int?,
) {
    if (currentNode is CommentNode.Actual) {
        // replaces the placeholder with an actual node
        val memoizedNode = memo[currentNode.id]
        if (memoizedNode is CommentNode.Placeholder) {
            memo[currentNode.id] = currentNode.copy(
                parent = memoizedNode.parent,
                children = memoizedNode.children
            )
        }
    }

    val splitPath = currentPath.split(".")
    val parentId = if (splitPath.size > 1) {
        splitPath[splitPath.size - 2].toInt()
    } else {
        null
    }

    if (parentId != null && currentNode.id != rootCommentId) {
        val parent = memo[parentId]
        if (parent != null) {
            // creates the connection between the nodes
            currentNode.parent = parent
            parent.children += currentNode
        } else {
            // if the parent doesn't exist, adds a placeholder node
            val parentPath = currentPath.substringBeforeLast(".")
            val placeholder = CommentNode.Placeholder(
                missingComment = PlaceholderComment(id = parentId, path = parentPath),
            )
            placeholder.children += currentNode
            currentNode.parent = placeholder
            memo[parentId] = placeholder

            // invokes recursion one level up until root of the path
            connectNodesAndGeneratePlaceholders(
                memo = memo,
                currentPath = parentPath,
                currentNode = placeholder,
                rootCommentId = rootCommentId
            )
        }
    }
}

private fun joinForestUnderSingleRoot(
    ancestorId: Int?,
    memo: Map<Int, CommentNode>,
): CommentNode {
    return CommentNode.Placeholder(
        missingComment = PlaceholderComment(
            id = 0,
            path = "",
        )
    ).apply {
        children += memo.values.filter { node ->
            node.parent?.id == ancestorId
        }
    }
}

private fun linearize(node: CommentNode, list: MutableList<CommentNode>) {
    if (node.id != 0) {
        list.add(node)
    }
    for (c in node.children) {
        linearize(c, list)
    }
}