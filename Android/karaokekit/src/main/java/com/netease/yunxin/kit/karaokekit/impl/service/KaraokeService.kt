/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongParams
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.impl.model.StartKaraokeParam
import com.netease.yunxin.kit.karaokekit.impl.model.request.CancelChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.InviteChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.JoinChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.StartSingRequest
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeRoomList
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.NEKaraokeDynamicToken
import kotlinx.coroutines.flow.Flow

interface HttpErrorReporter {

    /**
     * 网络错误事件
     * @property code 错误码
     * @property msg 信息
     * @property requestId 请求id
     * @constructor
     */
    data class ErrorEvent(
        val code: Int,
        val msg: String?,
        val requestId: String
    )

    fun reportHttpErrorEvent(error: ErrorEvent)

    val httpErrorEvents: Flow<ErrorEvent>
}

/**
 * karaoke 服务端接口对应service
 */
interface KaraokeService : HttpErrorReporter {
    fun initialize(context: Context, url: String)

    fun addHeader(key: String, value: String)

    fun getKaraokeRoomList(
        type: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int,
        callback:
        NetRequestCallback<KaraokeRoomList>
    )

    /**
     * 创建一个karaoke 房间
     *
     */
    fun startKaraoke(param: StartKaraokeParam, callback: NetRequestCallback<KaraokeRoomInfo>)

    /**
     * 获取房间 信息
     */
    fun getRoomInfo(liveRecordId: Long, callback: NetRequestCallback<KaraokeRoomInfo>)

    /**
     * 放弃演唱
     * @param orderId 点歌编号
     */
    fun abandon(roomUuid: String, orderId: Long, callback: NetRequestCallback<Unit>)

    /**
     * 结束 karaoke房间
     */
    fun stopKaraoke(liveRecodeId: Long, callback: NetRequestCallback<Unit>)

    fun requestKaraokeInfo(liveRecordId: Long, callback: NetRequestCallback<KaraokeRoomInfo>)

    /**
     * 发送礼物
     */
    fun sendBatchGift(liveRecodeId: Long, giftId: Int, giftCount: Int, userUuids: List<String>, callback: NetRequestCallback<Unit>)

    /**
     * 邀请合唱接口
     *
     * 不指定具体人员，默认给聊天室（首选）/房间内所有成员发送消息
     * 邀请合唱消息：邀请合唱（cmd=140）发送聊天室消息
     * 同意合唱消息：同意合唱（cmd=141）发送聊天室消息
     * 取消合唱消息：取消合唱（cmd=142）发送聊天室消息
     * 拒绝合唱消息：拒绝合唱（cmd=148）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     */
    fun inviteChorus(
        roomUuid: String,
        request: InviteChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    )

    /**
     * 同意合唱接口
     *
     * 不指定具体人员，默认给聊天室（首选）/房间内所有成员发送消息
     * 邀请合唱消息：邀请合唱（cmd=140）发送聊天室消息
     * 同意合唱消息：同意合唱（cmd=141）发送聊天室消息
     * 取消合唱消息：取消合唱（cmd=142）发送聊天室消息
     * 拒绝合唱消息：拒绝合唱（cmd=148）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     */
    fun joinChorus(
        roomUuid: String,
        request: JoinChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    )

    /**
     * 取消邀请合唱接口
     *
     * 不指定具体人员，默认给聊天室（首选）/房间内所有成员发送消息
     * 邀请合唱消息：邀请合唱（cmd=140）发送聊天室消息
     * 同意合唱消息：同意合唱（cmd=141）发送聊天室消息
     * 取消合唱消息：取消合唱（cmd=142）发送聊天室消息
     * 拒绝合唱消息：拒绝合唱（cmd=148）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     */
    fun cancelChorus(
        roomUuid: String,
        request: CancelChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    )

    /**
     * 合唱准备完成接口（下载好伴奏、歌词、midi等信息）
     * 合唱对端下载好伴奏、歌词、midi等信息时调用，发送聊天室消息告知
     * 合唱准备完成消息：合唱准备完成（cmd=143）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     */
    fun chorusReady(
        roomUuid: String,
        chorusId: String,
        callback: NetRequestCallback<KaraokeSongInfo>
    )

    /**
     * 开始演唱消息：开始演唱（cmd=144）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     */
    fun startSing(roomUuid: String, request: StartSingRequest, callback: NetRequestCallback<Unit>)

    /**
     * 暂停/继续/结束演唱
     *
     * 暂停演唱消息：暂停演唱（cmd=145）发送聊天室消息
     * 继续演唱消息：继续演唱（cmd=146）发送聊天室消息
     * 结束演唱消息：结束演唱（cmd=147）发送聊天室消息
     * 聊天室API：https://doc.yunxin.163.com/docs/TM5MzM5Njk/zMzOTQxNjE?platformId=60353
     * 暂停/继续/结束演唱谁能操作，主唱或副唱或者房主才能够进行
     */
    fun playControl(roomUuid: String, action: Int, callback: NetRequestCallback<Unit>)

    /**
     * 获取房间当前演唱信息
     *
     * @param roomUuid 房间编号
     * @param callback 执行回调
     */
    fun currentRoomSingInfo(roomUuid: String, callback: NetRequestCallback<KaraokeSongInfo>)

    /**
     * 获取已点歌曲列表
     *
     * @param liveRecordId 直播编号
     * @param callback 执行回调
     */
    fun getOrderedSongs(
        liveRecordId: Long,
        callback: NetRequestCallback<List<NEKaraokeOrderSongResult>>
    )

    /**
     * 选择歌曲
     *
     * @param liveRecordId 直播编号
     * @param songInfo 歌曲信息
     * @param callback 执行回调
     */
    fun orderSong(
        liveRecordId: Long,
        songInfo: NEKaraokeOrderSongParams,
        callback: NetRequestCallback<NEKaraokeOrderSongResult>
    )

    /**
     * 删除歌曲
     *
     * @param liveRecordId 直播编号
     * @param orderId 歌曲点歌编号
     * @param callback 执行回调
     */
    fun deleteSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>)

    /**
     * 置顶歌曲
     *
     * @param liveRecordId 直播编号
     * @param orderId 歌曲点歌编号
     * @param callback 执行回调
     */
    fun topSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>)

    /**
     * 切歌
     *
     * @param liveRecordId 直播编号
     * @param orderId 歌曲点歌编号
     * @param callback 执行回调
     */
    fun nextSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>)

    /**
     * 获取版权曲库API鉴权的token
     *
     * @param callback 执行回调
     */
    fun getSongToken(callback: NetRequestCallback<NEKaraokeDynamicToken>)
}
