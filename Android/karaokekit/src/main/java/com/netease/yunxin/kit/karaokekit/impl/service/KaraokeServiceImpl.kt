/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.common.network.Request
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
import com.netease.yunxin.kit.karaokekit.impl.repository.KaraokeRepository
import com.netease.yunxin.kit.karaokekit.impl.repository.KaraokeSongRepository
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeLog
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NEErrorMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object KaraokeServiceImpl : KaraokeService {

    const val TAG = "KaraokeServiceImpl"

    private var karaokeRepository = KaraokeRepository()

    private var songRepository = KaraokeSongRepository()

    private var karaokeScope: CoroutineScope? = null

    init {
        karaokeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun initialize(context: Context, url: String) {
        karaokeRepository.initialize(context, url)
        songRepository.initialize(context, url)
    }

    override fun addHeader(key: String, value: String) {
        karaokeRepository.addHeader(key, value)
        songRepository.addHeader(key, value)
    }

    override fun getKaraokeRoomList(
        type: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int,
        callback: NetRequestCallback<KaraokeRoomList>
    ) {
        karaokeScope?.launch {
            Request.request(
                {
                    karaokeRepository.getKaraokeRoomList(type, live, pageNum, pageSize)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun startKaraoke(
        param: StartKaraokeParam,
        callback: NetRequestCallback<KaraokeRoomInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                {
                    karaokeRepository.startKaraoke(
                        param.roomTopic,
                        param.cover,
                        param.liveType,
                        param.configId,
                        param.seatCount,
                        param.seatMode,
                        param.singMode
                    )
                },
                success = {
                    it?.let {
                        callback.success(it)
                    }
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getRoomInfo(liveRecordId: Long, callback: NetRequestCallback<KaraokeRoomInfo>) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.getRoomInfo(liveRecordId) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun abandon(
        roomUuid: String,
        orderId: Long,
        callback: NetRequestCallback<Unit>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.abandon(roomUuid, orderId) },
                success = {
                    callback.success()
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun stopKaraoke(liveRecodeId: Long, callback: NetRequestCallback<Unit>) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.stopKaraoke(liveRecodeId) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun requestKaraokeInfo(
        liveRecordId: Long,
        callback: NetRequestCallback<KaraokeRoomInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                {
                    karaokeRepository.requestKaraokeInfo(
                        liveRecordId
                    )
                },
                success = {
                    it?.let {
                        callback.success(it)
                    }
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun sendBatchGift(
        liveRecodeId: Long,
        giftId: Int,
        giftCount: Int,
        userUuids: List<String>,
        callback: NetRequestCallback<Unit>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.reward(liveRecodeId, giftId, giftCount, userUuids) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun inviteChorus(
        roomUuid: String,
        request: InviteChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.inviteControl(roomUuid, request) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun joinChorus(
        roomUuid: String,
        request: JoinChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.joinControl(roomUuid, request) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun cancelChorus(
        roomUuid: String,
        request: CancelChorusRequest,
        callback: NetRequestCallback<KaraokeSongInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.cancelControl(roomUuid, request) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun chorusReady(
        roomUuid: String,
        chorusId: String,
        callback: NetRequestCallback<KaraokeSongInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.chorusReady(roomUuid, chorusId) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun startSing(
        roomUuid: String,
        request: StartSingRequest,
        callback: NetRequestCallback<Unit>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.startSing(roomUuid, request) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun playControl(
        roomUuid: String,
        action: Int,
        callback: NetRequestCallback<Unit>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.playControl(roomUuid, action) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun currentRoomSingInfo(
        roomUuid: String,
        callback: NetRequestCallback<KaraokeSongInfo>
    ) {
        karaokeScope?.launch {
            Request.request(
                { karaokeRepository.currentRoomSingInfo(roomUuid) },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getOrderedSongs(
        liveRecordId: Long,
        callback: NetRequestCallback<List<NEKaraokeOrderSongResult>>
    ) {
        karaokeScope?.launch {
            Request.request(
                {
                    songRepository.getOrderSongs(liveRecordId)
                },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun orderSong(
        liveRecordId: Long,
        songInfo: NEKaraokeOrderSongParams,
        callback: NetRequestCallback<NEKaraokeOrderSongResult>
    ) {
        karaokeScope?.launch {
            songInfo.apply {
                Request.request(
                    {
                        songRepository.orderSong(
                            liveRecordId,
                            songId,
                            songName,
                            songCover,
                            songTime,
                            channel
                        )
                    },
                    success = {
                        callback.success(it)
                    },
                    error = { code: Int, msg: String ->
                        callback.error(code, msg)
                    }
                )
            }
        }
    }

    override fun deleteSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>) {
        karaokeScope?.launch {
            Request.request(
                {
                    songRepository.cancelOrderSong(
                        liveRecordId,
                        orderId
                    )
                },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun topSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>) {
        karaokeScope?.launch {
            Request.request(
                {
                    songRepository.songSetTop(
                        liveRecordId,
                        orderId
                    )
                },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun nextSong(liveRecordId: Long, orderId: Long, callback: NetRequestCallback<Boolean>) {
        karaokeScope?.launch {
            Request.request(
                {
                    songRepository.switchSong(
                        liveRecordId,
                        orderId
                    )
                },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getSongToken(callback: NetRequestCallback<NEKaraokeDynamicToken>) {
        karaokeScope?.launch {
            Request.request(
                {
                    karaokeRepository.getSongToken()
                },
                success = {
                    callback.success(it)
                },
                error = { code: Int, msg: String ->
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun reportHttpErrorEvent(error: HttpErrorReporter.ErrorEvent) {
        if (error.code != NEErrorCode.SUCCESS) {
            KaraokeLog.e(TAG, "report http error: $error")
        }
        httpErrorEvents.value = error
    }

    override val httpErrorEvents =
        MutableStateFlow(HttpErrorReporter.ErrorEvent(NEErrorCode.SUCCESS, NEErrorMsg.SUCCESS, "0"))

    fun destroy() {
        karaokeScope?.cancel()
        karaokeScope = null
    }
}
