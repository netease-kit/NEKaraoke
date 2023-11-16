/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.impl.model.response
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo

/**
 * 直播主页面列表返回值
 */
class KaraokeRoomList {

    var pageNum: Int = 0 // 当前页码

    var hasNextPage = false // boolean	是否有下一页

    var list: MutableList<KaraokeRoomInfo>? = null // 直播房间列表
}

data class KaraokeSongModelResult(
    val singInfo: KaraokeSongInfo?,
    val chorusInfo: KaraokeChorusInfo?,
    val operator: Operator?
)

data class KaraokeSongInfo(
    var chorusId: String?, // / 合唱id
    val roomUuid: String, // 房间id
    val orderId: Long, // 点歌编号
    val liveRecordId: Long, // 直播记录id
    val liveTopic: String?, // 直播主题
    val singMode: Int?, // / 演唱模式，0：智能合唱， 1: 串行合唱，2：NTP实时合唱，3：独唱，默认智能合唱，KTV场景使用
    val userUuid: String?, // / 用户id
    val userName: String?, // / 主唱昵称
    val icon: String?, // 主唱头像
    val assistantUuid: String?, // 副唱id
    val assistantName: String?, // 副唱昵称
    val assistantIcon: String?, // 副唱头像
    val songId: String?, // 歌曲编号
    val songName: String?, // 歌曲名称
    val songCover: String?, // 歌曲封面
    val songTime: Long?, // 歌曲时长
    val singer: String?, // 歌手名称
    val singerCover: String?, // 歌手封面
    val songStatus: Int?, // / 状态：0: 暂停  1:播放  2:结束
    val chorusType: Int?, // / 合唱类型 1:串行合唱（默认） 2:实时合唱，同意合唱后会含有该字段
    val operator: Operator?, // / 操作人
    val ext: Map<String, Any>?, // / 扩展字段 存储歌曲时长
    val channel: Int? // 歌曲渠道
)

data class KaraokeChorusInfo(
    var chorusId: String?, // / 合唱id
    val roomUuid: String, // 房间id
    val orderId: Long, // 点歌编号
    val liveRecordId: Long, // 直播记录id
    val liveTopic: String?, // 直播主题
    val singMode: Int?, // / 演唱模式，0：智能合唱， 1: 串行合唱，2：NTP实时合唱，3：独唱，默认智能合唱，KTV场景使用
    val userUuid: String?, // / 用户id
    val userName: String?, // / 主唱昵称
    val icon: String?, // 主唱头像
    val assistantUuid: String?, // 副唱id
    val assistantName: String?, // 副唱昵称
    val assistantIcon: String?, // 副唱头像
    val songId: String?, // 歌曲编号
    val songName: String?, // 歌曲名称
    val songCover: String?, // 歌曲封面
    val songTime: Long?, // 歌曲时长
    val singer: String?, // 歌手名称
    val singerCover: String?, // 歌手封面
    val songStatus: Int?, // / 状态：0: 暂停  1:播放  2:结束
    val chorusType: Int?, // / 合唱类型 1:串行合唱（默认） 2:实时合唱，同意合唱后会含有该字段
    val operator: Operator?, // / 操作人
    val ext: Map<String, Any>?, // / 扩展字段 存储歌曲时长
    val channel: Int? // 歌曲渠道
)

data class Operator(
    val userUuid: String?,
    val userName: String?,
    val icon: String?
)

data class CustomAttachment(
    val type: Int,
    val data: CommonAttachment
)

data class CommonAttachment(
    val appKey: String,
    val roomUuid: String,
    val type: String,
    val cmd: Int,
    val data: KaraokeSongInfo
)

data class NEKaraokeDynamicToken(
    val accessToken: String,
    val expiresIn: Long
)
