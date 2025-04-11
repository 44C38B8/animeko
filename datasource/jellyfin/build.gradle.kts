/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

plugins {
    kotlin("multiplatform")
    `android-library`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    `ani-mpp-lib-targets`
}

android {
    namespace = "me.him188.ani.datasources.jellyfin"
}

dependencies {
    commonMainApi(projects.datasource.datasourceApi)
    commonMainImplementation(projects.utils.serialization)
    commonMainImplementation(projects.utils.logging)

    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.ktor.client.logging)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
