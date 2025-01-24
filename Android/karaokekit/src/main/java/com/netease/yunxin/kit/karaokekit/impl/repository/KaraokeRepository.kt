/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.impl.repository

import android.content.Context
import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.common.network.ServiceCreator
import com.netease.yunxin.kit.karaokekit.BuildConfig
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.impl.model.request.CancelChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.InviteChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.JoinChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.StartSingRequest
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeRoomList
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.NEKaraokeDynamicToken
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.impl.repository.ServerConfig
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KaraokeRepository {

    companion object {
        lateinit var serverConfig: ServerConfig
    }
    private val serviceCreator: ServiceCreator = ServiceCreator()

    private lateinit var karaokeApi: KaraokeApi

    fun initialize(context: Context, url: String) {
        serviceCreator.init(
            context,
            url,
            if (BuildConfig.DEBUG) ServiceCreator.LOG_LEVEL_BODY else ServiceCreator.LOG_LEVEL_BASIC,
            NERoomKit.getInstance().deviceId
        )
        val localLanguage = Locale.getDefault().language
        serviceCreator.addHeader(ServiceCreator.ACCEPT_LANGUAGE_KEY, localLanguage)
        karaokeApi = serviceCreator.create(KaraokeApi::class.java)
    }

    fun addHeader(key: String, value: String) {
        serviceCreator.addHeader(key, value)
    }

    suspend fun getKaraokeRoomList(
        liveType: Int,
        live: Int,
        pageNum: Int,
        pageSize: Int
    ): Response<KaraokeRoomList> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveType" to liveType,
            "live" to live,
            "pageNum" to pageNum,
            "pageSize" to pageSize
        )
        karaokeApi.getKaraokeRoomList(params)
    }

    suspend fun startKaraoke(
        liveTopic: String?,
        cover: String?,
        liveType: Int,
        configId: Int,
        seatCount: Int,
        seatMode: Int,
        singMode: Int
    ): Response<KaraokeRoomInfo> =
        withContext(Dispatchers.IO) {
            val params = mapOf<String, Any?>(
                "liveTopic" to liveTopic,
                "cover" to cover,
                "liveType" to liveType,
                "configId" to configId,
                "seatCount" to seatCount,
                "seatApplyMode" to seatMode,
                "singMode" to singMode
            )
            karaokeApi.startKaraoke(params)
        }

    suspend fun stopKaraoke(liveRecordId: Long): Response<Unit> = withContext(Dispatchers.IO) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        karaokeApi.stopKaraoke(params)
    }

    suspend fun requestKaraokeInfo(liveRecordId: Long): Response<KaraokeRoomInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        karaokeApi.requestKaraokeInfo(params)
    }

    suspend fun reward(liveRecordId: Long, giftId: Int, giftCount: Int, userUuids: List<String>): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId,
            "giftId" to giftId,
            "giftCount" to giftCount,
            "targets" to userUuids
        )
        karaokeApi.reward(params)
    }

    suspend fun inviteControl(roomUuid: String, request: InviteChorusRequest): Response<KaraokeSongInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "deviceParam" to request.deviceParam,
            "orderId" to request.orderId
        )
        karaokeApi.inviteChorus(params)
    }

    suspend fun joinControl(roomUuid: String, request: JoinChorusRequest): Response<KaraokeSongInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "deviceParam" to request.deviceParam,
            "chorusId" to request.chorusId
        )
        karaokeApi.joinChorus(params)
    }

    suspend fun cancelControl(roomUuid: String, request: CancelChorusRequest): Response<KaraokeSongInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "chorusId" to request.chorusId
        )
        karaokeApi.cancelChorus(params)
    }

    suspend fun chorusReady(roomUuid: String, chorusId: String): Response<KaraokeSongInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "chorusId" to chorusId
        )
        karaokeApi.ready(params)
    }

    suspend fun startSing(roomUuid: String, request: StartSingRequest): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "chorusId" to request.chorusId,
            "ext" to request.ext,
            "orderId" to request.orderId
        )
        karaokeApi.startSing(params)
    }

    suspend fun playControl(roomUuid: String, action: Int): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "action" to action
        )
        karaokeApi.playControl(params)
    }

    suspend fun currentRoomSingInfo(roomUuid: String): Response<KaraokeSongInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid
        )
        karaokeApi.currentRoomSingInfo(params)
    }

    suspend fun getRoomInfo(liveRecordId: Long): Response<KaraokeRoomInfo> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "liveRecordId" to liveRecordId
        )
        karaokeApi.getRoomInfo(params)
    }

    suspend fun abandon(roomUuid: String, orderId: Long): Response<Unit> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "roomUuid" to roomUuid,
            "orderId" to orderId
        )
        karaokeApi.abandon(params)
    }

    suspend fun getSongToken(): Response<NEKaraokeDynamicToken> = withContext(Dispatchers.IO) {
        karaokeApi.getMusicToken()
    }
}
