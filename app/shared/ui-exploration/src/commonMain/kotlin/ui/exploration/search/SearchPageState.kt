/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.him188.ani.app.data.models.subject.CanonicalTagKind
import me.him188.ani.app.domain.search.SearchSort
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.search.PagingSearchState
import me.him188.ani.app.ui.search.SearchState
import me.him188.ani.app.ui.search.TestSearchState
import me.him188.ani.app.ui.search.launchAsItemsIn
import me.him188.ani.utils.platform.annotations.TestOnly

@Stable
class SearchPageState(
    searchHistoryPager: Flow<PagingData<String>>,
    suggestionsPager: Flow<PagingData<String>>,
    val queryFlow: StateFlow<SubjectSearchQuery>,
    val setQuery: (SubjectSearchQuery) -> Unit,
    val onRequestPlay: suspend (SubjectPreviewItemInfo) -> EpisodeTarget?,
    val searchState: SearchState<SubjectPreviewItemInfo>,
    private val onRemoveHistory: suspend (String) -> Unit,
    backgroundScope: CoroutineScope,
    private val onStartSearch: (String) -> Unit = {},
    private val tagKinds: List<CanonicalTagKind> = SearchFilterState.DEFAULT_TAG_KINDS,
) {
    // to navigate to episode page
    data class EpisodeTarget(
        val subjectId: Int,
        val episodeId: Int,
    )

    val suggestionSearchBarState = SuggestionSearchBarState(
        historyPager = searchHistoryPager,
        suggestionsPager = suggestionsPager,
        searchState = searchState,
        queryFlow = queryFlow.map { it.keywords },
        setQueryValue = { setQuery(queryFlow.value.copy(keywords = it)) },
        onRemoveHistory = { onRemoveHistory(it) },
        onStartSearch = { query ->
            onStartSearch(query)
        },
        backgroundScope = backgroundScope,
    )
    val gridState = LazyGridState()

    val searchFilterStateFlow = queryFlow.map { it.tags.orEmpty() }.map { selectedTags ->
        val groups = selectedTags.groupBy { tag ->
            tagKinds.find { kind -> tag in kind.values }
        }
        val chips = buildList(capacity = tagKinds.size + 1) {
            for (kind in tagKinds) {
                add(
                    SearchFilterChipState(
                        kind = kind,
                        values = kind.values,
                        selected = groups[kind].orEmpty(),
                    ),
                )
            }

            // 加入自定义
            groups[null]?.let { customValues ->
                add(
                    SearchFilterChipState(
                        kind = null,
                        values = customValues,
                        selected = customValues,
                    ),
                )
            }
        }
        SearchFilterState(
            chips = chips,
        )
    }.stateIn(
        backgroundScope, started = SharingStarted.WhileSubscribed(),
        SearchFilterState(
            chips = tagKinds.map { kind ->
                SearchFilterChipState(
                    kind = kind,
                    values = kind.values,
                    selected = emptyList(),
                )
            },
        ),
    )

    val items = searchState.launchAsItemsIn(backgroundScope)

    var selectedItemIndex: Int by mutableIntStateOf(-1)

    val playTasker = MonoTasker(backgroundScope)
    var playingItem: SubjectPreviewItemInfo? by mutableStateOf(null)
        private set

    suspend fun requestPlay(info: SubjectPreviewItemInfo): EpisodeTarget? {
        playingItem = info
        return playTasker.async {
            onRequestPlay(info)
        }.await()
    }

    fun toggleTagSelection(
        tag: SearchFilterChipState,
        value: String,
        unselectOthersOfSameKind: Boolean,
    ) {
        updateQuery {
            val query = this
            val existingTags = query.tags.orEmpty()
            copy(
                tags = if (value in existingTags) {
                    existingTags - value
                } else {
                    if (unselectOthersOfSameKind) {
                        existingTags.filterNot { it in tag.values } // 取消选中同类的其他选项
                            .plus(value) // 选中当前选项
                    } else {
                        existingTags + value
                    }
                },
            )
        }
    }

    inline fun updateQuery(block: SubjectSearchQuery.() -> SubjectSearchQuery) {
        setQuery(queryFlow.value.block())
        searchState.startSearch()
    }

    fun updateSort(
        sort: SearchSort,
    ) {
        updateQuery {
            copy(sort = sort)
        }
    }
}

@TestOnly
fun createTestSearchPageState(
    backgroundScope: CoroutineScope,
    searchState: SearchState<SubjectPreviewItemInfo> = TestSearchState(
        MutableStateFlow(MutableStateFlow(PagingData.from(TestSubjectPreviewItemInfos))),
    )
): SearchPageState {
    val queryFlow = MutableStateFlow(SubjectSearchQuery(""))
    return SearchPageState(
        searchHistoryPager = MutableStateFlow(PagingData.from(listOf("test history"))),
        suggestionsPager = MutableStateFlow(PagingData.from(listOf("suggestion1"))),
        queryFlow = queryFlow,
        setQuery = { new ->
            queryFlow.update { new }
        },
        onRequestPlay = {
            delay(3000)
            SearchPageState.EpisodeTarget(1, 2)
        },
        searchState = searchState,
        onRemoveHistory = {

        },
        backgroundScope = backgroundScope,
    )
}

@TestOnly
fun createTestInteractiveSubjectSearchState(scope: CoroutineScope): SearchState<SubjectPreviewItemInfo> {
    return PagingSearchState(
        {
            MutableStateFlow(PagingData.from(TestSubjectPreviewItemInfos))
        },
        scope,
    )
}

@TestOnly
fun createTestFinishedSubjectSearchState(): SearchState<SubjectPreviewItemInfo> {
    return TestSearchState(
        MutableStateFlow(MutableStateFlow(PagingData.from(TestSubjectPreviewItemInfos))),
    )
}
