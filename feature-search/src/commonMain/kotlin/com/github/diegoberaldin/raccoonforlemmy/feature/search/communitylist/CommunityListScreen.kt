package com.github.diegoberaldin.raccoonforlemmy.feature.search.communitylist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getSearchScreenModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class CommunityListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSearchScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val bottomNavigator = LocalBottomSheetNavigator.current

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                Row(
                    modifier = Modifier.height(50.dp).padding(
                        horizontal = Spacing.s,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.onClick {
                            bottomNavigator.show(
                                ListingTypeBottomSheet(
                                    isLogged = uiState.isLogged,
                                    onHide = {
                                        bottomNavigator.hide()
                                    },
                                    onSelected = {
                                        model.reduce(CommunityListMviModel.Intent.SetListingType(it))
                                    },
                                ),
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    ) {
                        Image(
                            imageVector = uiState.listingType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                        ) {
                            Text(
                                text = stringResource(MR.strings.instance_detail_communities),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = uiState.listingType.toReadableName(),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                TextField(
                    modifier = Modifier.padding(
                        horizontal = Spacing.m,
                        vertical = Spacing.s,
                    ).fillMaxWidth(),
                    label = {
                        Text(text = stringResource(MR.strings.explore_search_placeholder))
                    },
                    singleLine = true,
                    value = uiState.searchText,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                    onValueChange = { value ->
                        model.reduce(CommunityListMviModel.Intent.SetSearch(value))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick {
                                if (uiState.searchText.isNotEmpty()) {
                                    model.reduce(CommunityListMviModel.Intent.SetSearch(""))
                                }
                            },
                            imageVector = if (uiState.searchText.isEmpty()) Icons.Default.Search else Icons.Default.Clear,
                            contentDescription = null,
                        )
                    },
                )
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        model.reduce(CommunityListMviModel.Intent.SearchFired)
                    }) {
                        Text(
                            text = stringResource(MR.strings.button_search),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(CommunityListMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.padding(Spacing.xxs).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        items(uiState.communities) { community ->
                            CommunityItem(
                                modifier = Modifier.fillMaxWidth().onClick {
                                    navigator.push(
                                        CommunityDetailScreen(
                                            community = community,
                                            onBack = {
                                                navigator.pop()
                                            },
                                        ),
                                    )
                                },
                                community = community,
                            )
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(CommunityListMviModel.Intent.LoadNextPage)
                            }
                            if (uiState.loading && !uiState.refreshing) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(25.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = uiState.refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
