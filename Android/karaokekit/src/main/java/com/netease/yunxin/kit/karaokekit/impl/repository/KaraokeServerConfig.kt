/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.repository

data class KaraokeServerConfig(
    val appKey: String,
    val serverUrl: String
) {

    companion object {

        private const val ONLINE_URL = "https://yiyong-xedu-v2.netease.im/"

        private const val DEVELOP_URL = "https://yiyong-xedu-v2-test.netease.im/"

        fun selectServer(appKey: String, url: String?) = when {
            url?.startsWith("http") == true -> KaraokeServerConfig(appKey, url)
            url == "test" -> KaraokeServerConfig(appKey, DEVELOP_URL)
            else -> KaraokeServerConfig(appKey, ONLINE_URL)
        }
    }
}
