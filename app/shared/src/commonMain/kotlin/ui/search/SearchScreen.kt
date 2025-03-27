/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.search

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.ui.exploration.search.SearchPage
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.widgets.BackNavigationIconButton
import me.him188.ani.app.ui.main.SearchViewModel
import me.him188.ani.app.ui.subject.details.SubjectDetailsScreen

@Composable
fun SearchScreen(
    vm: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEpisodeDetails: (subjectId: Int, episodeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forPageContent()
) {
    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator()
    val coroutineScope = rememberCoroutineScope()
    SearchPage(
        vm.searchPageState,
        detailContent = {
            val subjectDetailsState by vm.subjectDetailsStateLoader.state
                .collectAsStateWithLifecycle(null)
            val authState by vm.authState.collectAsStateWithLifecycle(AuthState.NotAuthed)

            SubjectDetailsScreen(
                subjectDetailsState,
                authState,
                onPlay = { episodeId ->
                    val current = subjectDetailsState
                    if (current != null) {
                        onNavigateToEpisodeDetails(current.subjectId, episodeId)
                    }
                },
                onLoadErrorRetry = { vm.reloadCurrentSubjectDetails() },
                onClickTag = { vm.searchPageState.updateQuery { copy(tags = listOf(it.name)) } },
                navigationIcon = {
                    // 只有在单面板模式下才显示返回按钮
                    if (listDetailLayoutParameters.isSinglePane) {
                        BackNavigationIconButton(
                            onNavigateBack = {
                                coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                                    listDetailNavigator.navigateBack()
                                }
                            },
                        )
                    }
                },
                windowInsets = paneContentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Right),
            )
        },
        modifier.fillMaxSize(),
        contentWindowInsets = windowInsets,
        onSelect = { index, item ->
            vm.searchPageState.selectedItemIndex = index
            vm.viewSubjectDetails(item)
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                listDetailNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        },
        navigator = listDetailNavigator,
        navigationIcon = {
            BackNavigationIconButton(onNavigateBack)
        },
    )
    SideEffect {
        vm.startInitialSearch()
    }
}