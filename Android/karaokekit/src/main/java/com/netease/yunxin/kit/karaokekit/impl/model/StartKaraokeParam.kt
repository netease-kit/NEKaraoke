/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.model

data class StartKaraokeParam(
    val roomTopic: String,
    val userName: String,
    val cover: String? = null,
    val liveType: Int, // /karaoke 3
    val configId: Int = 0,
    val seatCount: Int = 7,
    val seatMode: Int = 1,
    val singMode: Int
)
