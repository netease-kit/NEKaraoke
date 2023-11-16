/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 歌曲
 * @property chorusId 合唱id
 * @property roomUuid 房间id
 * @property orderId 点歌编号
 * @property liveRecordId 直播记录id
 * @property liveTopic 直播主题
 * @property singMode 演唱模式，0：智能合唱， 1: 串行合唱，2：NTP实时合唱，3：独唱，默认智能合唱，KTV场景使用
 * @property userUuid 用户id
 * @property userName 主唱昵称
 * @property icon 主唱头像
 * @property assistantUuid 副唱id
 * @property assistantName 副唱昵称
 * @property assistantIcon 副唱头像
 * @property songId 歌曲编号
 * @property songName 歌曲名称
 * @property songCover 歌曲封面
 * @property songTime 歌曲时长
 * @property singer 歌手名称
 * @property singerCover 歌手封面
 * @property songStatus 状态：0: 歌曲暂停  1:歌曲继续播放  2:歌曲结束 3：歌曲准备完成 4：歌曲未开始演唱
 * @property chorusType 合唱类型 1:串行合唱（默认） 2:实时合唱，同意合唱后会含有该字段
 * @property operator 操作者
 * @property ext 扩展字段 存储歌曲时长等
 * @constructor
 */
data class NEKaraokeSongModel(
    var chorusId: String?,
    val roomUuid: String,
    val orderId: Long,
    val liveRecordId: Long,
    val liveTopic: String?,
    val singMode: Int?,
    val userUuid: String?,
    val userName: String?,
    val icon: String?,
    val assistantUuid: String?,
    val assistantName: String?,
    val assistantIcon: String?,
    val songId: String?,
    val songName: String?,
    val songCover: String?,
    val songTime: Long?,
    val singer: String?,
    val singerCover: String?,
    val songStatus: Int?,
    val chorusType: Int?,
    val operator: NEKaraokeOrderSongOperatorUser?,
    val ext: Map<String, Any>?,
    val channel: Int?
)

/**
 * 操作者
 * @property userUuid 用户id
 * @property userName 用户名
 * @property icon 头像
 * @constructor
 */
data class NEKaraokeOrderSongOperatorUser(
    val userUuid: String?,
    val userName: String?,
    val icon: String?
)

/**
 * 歌曲状态
 */
object NEKaraokeSongStatus {
    /**
     * 歌曲暂停
     */
    const val PAUSE = 0

    /**
     * 歌曲继续播放
     */
    const val PLAY = 1

    /**
     * 歌曲结束
     */
    const val END = 2

    /**
     * 歌曲准备完成
     */
    const val READY = 3

    /**
     * 歌曲未开始演唱
     */
    const val NOT_SING = 4
}

data class NEKaraokeOrderSongParams(
    var songId: String = "",
    var songName: String?,
    var songCover: String?,
    // / 版权来源：1：云音乐  2、咪咕
    var channel: Int?,
    var songTime: Long?
)

data class NEKaraokeOrderSongModel(
    var orderSongResultDto: NEKaraokeOrderSongResult,
    var operatorUser: NEKaraokeOrderSongOperatorUser?
)

data class NEKaraokeOrderSongResult(
    var orderSong: NEKaraokeOrderSongSongModel,
    val orderSongUser: NEKaraokeOrderSongOperatorUser?
)

data class NEKaraokeOrderSongSongModel(
    val liveRecordId: Long = 0,
    val orderId: Long = 0,
    val roomArchiveId: String? = null,
    val userUuid: String? = null,
    val roomUuid: String? = null,
    val songId: String? = null,
    val songName: String? = null,
    val songCover: String? = null,
    val singer: String? = null,
    val songTime: Long = 0,
    val channel: Int = 0,
    val status: Int = 0
)
