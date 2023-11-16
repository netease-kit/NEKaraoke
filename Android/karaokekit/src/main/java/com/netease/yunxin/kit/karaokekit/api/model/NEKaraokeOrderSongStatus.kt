/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 点歌状态状态 -2 已唱 -1 删除 0:等待唱 1 唱歌中
 */
object NEKaraokeOrderSongStatus {

    /**
     * 已唱
     */
    const val STATUS_DONE = -2

    /**
     * 删除
     */
    const val STATUS_CANCELED = -1

    /**
     * 等待唱
     */
    const val STATUS_WAIT = 0

    /**
     * 唱歌中
     */
    const val STATUS_SINGING = 1
}
