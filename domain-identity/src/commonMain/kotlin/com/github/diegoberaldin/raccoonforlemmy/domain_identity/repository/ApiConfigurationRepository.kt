package com.github.diegoberaldin.raccoonforlemmy.domain_identity.repository

import kotlinx.coroutines.flow.Flow

interface ApiConfigurationRepository {

    val instance: Flow<String>

    fun getInstance(): String

    fun changeInstance(value: String)
}

