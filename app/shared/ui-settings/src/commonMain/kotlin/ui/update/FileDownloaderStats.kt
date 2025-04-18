/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.update

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import me.him188.ani.app.data.repository.RepositoryNetworkException
import me.him188.ani.app.tools.update.FileDownloader
import me.him188.ani.app.tools.update.FileDownloaderState
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * 将 [FileDownloader] 状态收集为 Compose [State]
 */
@Stable
data class FileDownloaderStats(
    @param:FloatRange(from = 0.0, to = 1.0)
    val progress: Float,
    val state: FileDownloaderState,
    val isPlaceholder: Boolean = false,
) {
    companion object {
        val Placeholder = FileDownloaderStats(
            progress = 0f,
            state = FileDownloaderState.Idle,
            isPlaceholder = true,
        )
    }
}

class FileDownloaderPresenter(
    fileDownloader: FileDownloader,
    flowScope: CoroutineScope,
) {
    val flow = combine(
        fileDownloader.progress,
        fileDownloader.state,
        ::FileDownloaderStats,
    ).stateIn(
        scope = flowScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = FileDownloaderStats.Placeholder,
    )
}

@TestOnly
object TestFileDownloaderStats {
    @TestOnly
    val Downloading
        get() = FileDownloaderStats(
            progress = 0.5f,
            state = FileDownloaderState.Downloading,
            isPlaceholder = false,
        )

    @TestOnly
    val Succeed
        get() = FileDownloaderStats(
            progress = 1f,
            state = FileDownloaderState.Succeed("", Path("").inSystem, true),
            isPlaceholder = false,
        )

    @TestOnly
    val Failed
        get() = FileDownloaderStats(
            progress = 1f,
            state = FileDownloaderState.Failed(RepositoryNetworkException()),
            isPlaceholder = false,
        )
}
