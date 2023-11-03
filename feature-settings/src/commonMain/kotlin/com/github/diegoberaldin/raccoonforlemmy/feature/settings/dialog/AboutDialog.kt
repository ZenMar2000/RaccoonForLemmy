package com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.di.getAboutDialogViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.CHANGELOG_URL
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.REPORT_EMAIL_ADDRESS
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.REPORT_URL
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.WEBSITE_URL
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AboutDialog : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val viewModel = rememberScreenModel { getAboutDialogViewModel() }
        viewModel.bindToLifecycle(key)

        val uriHandler = LocalUriHandler.current
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val uiState by viewModel.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }

        LaunchedEffect(viewModel) {
            viewModel.effects.onEach { effect ->
                when (effect) {
                    is AboutDialogMviModel.Effect.OpenCommunity -> {
                        navigator?.push(
                            CommunityDetailScreen(
                                community = effect.community,
                                otherInstance = effect.instance,
                            )
                        )
                    }
                }
            }.launchIn(this)
        }

        AlertDialog(
            onDismissRequest = {
                notificationCenter.getObserver(NotificationCenterContractKeys.CloseDialog)
                    ?.invoke(Unit)
            },
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(vertical = Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(MR.strings.settings_about),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(Spacing.s))
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = Spacing.s, horizontal = Spacing.m)
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    item {
                        AboutItem(
                            text = stringResource(MR.strings.settings_about_app_version),
                            value = uiState.version,
                        )
                    }
                    item {
                        AboutItem(
                            text = stringResource(MR.strings.settings_about_changelog),
                            vector = Icons.Default.OpenInBrowser,
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                handleUrl(
                                    url = CHANGELOG_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    navigator = navigator,
                                )
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                handleUrl(
                                    url = REPORT_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    navigator = navigator,
                                )
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.settings_about_report_github),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Button(
                            onClick = {
                                uriHandler.openUri("mailto:$REPORT_EMAIL_ADDRESS")
                            },
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(MR.strings.settings_about_report_email),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    item {
                        AboutItem(
                            painter = painterResource(MR.images.ic_github),
                            text = stringResource(MR.strings.settings_about_view_github),
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                handleUrl(
                                    url = WEBSITE_URL,
                                    openExternal = settings.openUrlsInExternalBrowser,
                                    uriHandler = uriHandler,
                                    navigator = navigator,
                                )
                            },
                        )
                    }
                    item {
                        AboutItem(
                            painter = painterResource(MR.images.ic_lemmy),
                            text = stringResource(MR.strings.settings_about_view_lemmy),
                            textDecoration = TextDecoration.Underline,
                            onClick = {
                                viewModel.reduce(AboutDialogMviModel.Intent.OpenOwnCommunity)
                            },
                        )
                    }
                }
                Button(
                    onClick = {
                        notificationCenter.getObserver(NotificationCenterContractKeys.CloseDialog)
                            ?.invoke(Unit)
                    },
                ) {
                    Text(text = stringResource(MR.strings.button_close))
                }
            }
        }
    }

    @Composable
    fun AboutItem(
        painter: Painter? = null,
        vector: ImageVector? = null,
        text: String,
        textDecoration: TextDecoration = TextDecoration.None,
        value: String = "",
        onClick: (() -> Unit)? = null,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.xs,
                vertical = Spacing.s,
            ).onClick(
                rememberCallback {
                    onClick?.invoke()
                },
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageModifier = Modifier.size(22.dp)
            if (painter != null) {
                Image(
                    modifier = imageModifier,
                    painter = painter,
                    contentDescription = null,
                )
            } else if (vector != null) {
                Image(
                    modifier = imageModifier,
                    imageVector = vector,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = textDecoration,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}