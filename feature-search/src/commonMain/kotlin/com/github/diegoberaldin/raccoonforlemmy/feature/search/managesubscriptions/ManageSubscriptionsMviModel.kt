package com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

interface ManageSubscriptionsMviModel :
    MviModel<ManageSubscriptionsMviModel.Intent, ManageSubscriptionsMviModel.UiState, ManageSubscriptionsMviModel.Effect> {
    sealed interface Intent {
        data object HapticIndication : Intent

        data class Unsubscribe(val index: Int) : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val communities: List<CommunityModel> = emptyList(),
    )

    sealed interface Effect
}