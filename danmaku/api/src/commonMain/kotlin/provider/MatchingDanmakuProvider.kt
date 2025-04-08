/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.danmaku.api.provider

/**
 * 通过请求, 查询出一些结果, 可能需要手动选择的 [DanmakuProvider].
 */
interface MatchingDanmakuProvider : DanmakuProvider {
    suspend fun fetchSubjectList(request: DanmakuFetchRequest): List<DanmakuFetchResult>
}
