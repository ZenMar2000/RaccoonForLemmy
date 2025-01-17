package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

interface SelectInstanceMviModel :
    MviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SelectInstance(val value: String) : Intent
        data class DeleteInstance(val value: String) : Intent
        data class ChangeInstanceName(val value: String) : Intent
        data object SubmitChangeInstanceDialog : Intent
    }

    data class State(
        val instances: List<String> = emptyList(),
        val currentInstance: String = "",
        val changeInstanceName: String = "",
        val changeInstanceNameError: StringDesc? = null,
        val changeInstanceLoading: Boolean = false,
    )

    sealed interface Effect {
        data object CloseDialog : Effect
        data class Confirm(val instance: String) : Effect
    }
}