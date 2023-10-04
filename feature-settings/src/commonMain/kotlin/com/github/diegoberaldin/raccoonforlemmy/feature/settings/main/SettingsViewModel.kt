package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.CrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.resources.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val mvi: DefaultMviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>,
    private val themeRepository: ThemeRepository,
    private val colorSchemeProvider: ColorSchemeProvider,
    private val languageRepository: LanguageRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val crashReportConfiguration: CrashReportConfiguration,
) : ScreenModel,
    MviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect> by mvi {

    init {
        notificationCenter.addObserver(
            { handleLogout() },
            this::class.simpleName.orEmpty(),
            NotificationCenterContractKeys.Logout
        )
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.Main) {
            themeRepository.state.onEach { currentTheme ->
                mvi.updateState { it.copy(currentTheme = currentTheme) }
            }.launchIn(this)
            themeRepository.contentFontScale.onEach { value ->
                mvi.updateState { it.copy(currentFontScale = value.toFontScale()) }
            }.launchIn(this)
            themeRepository.navItemTitles.onEach { value ->
                mvi.updateState { it.copy(navBarTitlesVisible = value) }
            }.launchIn(this)
            themeRepository.dynamicColors.onEach { value ->
                mvi.updateState { it.copy(dynamicColors = value) }
            }.launchIn(this)
            themeRepository.customSeedColor.onEach { value ->
                mvi.updateState { it.copy(customSeedColor = value) }
            }.launchIn(this)
            themeRepository.postLayout.onEach { value ->
                mvi.updateState { it.copy(postLayout = value) }
            }.launchIn(this)
            languageRepository.currentLanguage.onEach { lang ->
                mvi.updateState { it.copy(lang = lang) }
            }.launchIn(this)
            identityRepository.authToken.onEach { auth ->
                mvi.updateState { it.copy(isLogged = !auth.isNullOrEmpty()) }
            }.launchIn(this)
        }

        val settings = settingsRepository.currentSettings.value
        mvi.updateState {
            it.copy(
                defaultListingType = settings.defaultListingType.toListingType(),
                defaultPostSortType = settings.defaultPostSortType.toSortType(),
                defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                includeNsfw = settings.includeNsfw,
                blurNsfw = settings.blurNsfw,
                supportsDynamicColors = colorSchemeProvider.supportsDynamicColors,
                openUrlsInExternalBrowser = settings.openUrlsInExternalBrowser,
                enableSwipeActions = settings.enableSwipeActions,
                crashReportEnabled = crashReportConfiguration.isEnabled(),
                appVersion = AppInfo.versionCode,
            )
        }
    }

    override fun reduce(intent: SettingsMviModel.Intent) {
        when (intent) {
            is SettingsMviModel.Intent.ChangeTheme -> {
                changeTheme(intent.value)
            }

            is SettingsMviModel.Intent.ChangeContentFontSize -> {
                changeContentFontScale(intent.value)
            }

            is SettingsMviModel.Intent.ChangeLanguage -> {
                changeLanguage(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultCommentSortType -> {
                changeDefaultCommentSortType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultListingType -> {
                changeDefaultListingType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultPostSortType -> {
                changeDefaultPostSortType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeBlurNsfw -> {
                changeBlurNsfw(intent.value)
            }

            is SettingsMviModel.Intent.ChangeIncludeNsfw -> {
                changeIncludeNsfw(intent.value)
            }

            is SettingsMviModel.Intent.ChangeNavBarTitlesVisible -> {
                changeNavBarTitlesVisible(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDynamicColors -> {
                changeDynamicColors(intent.value)
            }

            is SettingsMviModel.Intent.ChangeOpenUrlsInExternalBrowser -> {
                changeOpenUrlsInExternalBrowser(intent.value)
            }

            is SettingsMviModel.Intent.ChangeEnableSwipeActions -> {
                changeEnableSwipeActions(intent.value)
            }

            is SettingsMviModel.Intent.ChangeCustomSeedColor -> changeCustomSeedColor(
                intent.value
            )

            is SettingsMviModel.Intent.ChangePostLayout -> changePostLayout(intent.value)
            is SettingsMviModel.Intent.ChangeCrashReportEnabled -> {
                changeCrashReportEnabled(intent.value)
            }
        }
    }

    private fun changeTheme(value: ThemeState) {
        themeRepository.changeTheme(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                theme = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontScale(value: Float) {
        themeRepository.changeContentFontScale(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontScale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeLanguage(value: String) {
        languageRepository.changeLanguage(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                locale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultListingType(value: ListingType) {
        mvi.updateState { it.copy(defaultListingType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultListingType = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultPostSortType(value: SortType) {
        mvi.updateState { it.copy(defaultPostSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultPostSortType = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        mvi.updateState { it.copy(defaultCommentSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultCommentSortType = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        themeRepository.changeNavItemTitles(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                navigationTitlesVisible = value
            )
            saveSettings(settings)
        }
    }

    private fun changeIncludeNsfw(value: Boolean) {
        mvi.updateState { it.copy(includeNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                includeNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeBlurNsfw(value: Boolean) {
        mvi.updateState { it.copy(blurNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                blurNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDynamicColors(value: Boolean) {
        themeRepository.changeDynamicColors(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                dynamicColors = value
            )
            saveSettings(settings)
        }
    }

    private fun changeCustomSeedColor(value: Color?) {
        themeRepository.changeCustomSeedColor(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                customSeedColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeOpenUrlsInExternalBrowser(value: Boolean) {
        mvi.updateState { it.copy(openUrlsInExternalBrowser = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                openUrlsInExternalBrowser = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEnableSwipeActions(value: Boolean) {
        mvi.updateState { it.copy(enableSwipeActions = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                enableSwipeActions = value
            )
            saveSettings(settings)
        }
    }

    private fun changePostLayout(value: PostLayout) {
        themeRepository.changePostLayout(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                postLayout = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeCrashReportEnabled(value: Boolean) {
        crashReportConfiguration.setEnabled(value)
        mvi.updateState { it.copy(crashReportEnabled = value) }
    }

    private suspend fun saveSettings(settings: SettingsModel) {
        val accountId = accountRepository.getActive()?.id
        settingsRepository.updateSettings(settings, accountId)
        settingsRepository.changeCurrentSettings(settings)
    }

    private fun handleLogout() {
        mvi.scope?.launch {
            val settings = settingsRepository.getSettings(null)
            mvi.updateState {
                it.copy(
                    defaultListingType = settings.defaultListingType.toListingType(),
                    defaultPostSortType = settings.defaultPostSortType.toSortType(),
                    defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                )
            }
        }
    }
}