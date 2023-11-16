/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.model

import com.netease.yunxin.kit.roomkit.impl.model.RoomEvent

data class KaraokeCustomMessageEvent(
    val type: Int,
    val data: KaraokeCustomMessageData
)

/**
 * 自定义透传事件
 * @property roomUuid 房间号
 * @property cmd 指令
 * @property data 数据
 * @constructor
 */
data class KaraokeCustomMessageData(
    override val appKey: String?,
    override val roomUuid: String?,
    override val cmd: Int,
    val data: Any
) : RoomEvent

data class KaraokeSongOrderEvent(
    val orderId: Long,
    val userUuid: String,
    val songId: String,
    val songName: String,
    val songCover: String
)

data class KaraokeStartNextSongEvent(
    val songName: String,
    val userName: String,
    val icon: String,
    val songId: String,
    val userUuid: String,
    val orderId: Long,
    val roomUuid: String,
    val appId: String
)
