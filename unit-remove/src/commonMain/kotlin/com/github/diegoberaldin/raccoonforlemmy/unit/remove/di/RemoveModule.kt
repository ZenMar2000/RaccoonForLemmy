package com.github.diegoberaldin.raccoonforlemmy.unit.remove.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveViewModel
import org.koin.dsl.module

val removeModule = module {
    factory<RemoveMviModel> { params ->
        RemoveViewModel(
            postId = params[0],
            commentId = params[1],
            mvi = DefaultMviModel(RemoveMviModel.UiState()),
            identityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            notificationCenter = get(),
        )
    }
}