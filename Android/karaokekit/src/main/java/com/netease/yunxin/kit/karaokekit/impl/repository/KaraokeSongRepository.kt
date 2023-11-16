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
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KaraokeSongRepository {

    private val serviceCreator: ServiceCreator = ServiceCreator()

    private lateinit var songApi: KaraokeSongApi

    fun initialize(context: Context, url: String) {
        serviceCreator.init(
            context,
            url,
            if (BuildConfig.DEBUG) ServiceCreator.LOG_LEVEL_BODY else ServiceCreator.LOG_LEVEL_BASIC,
            NERoomKit.getInstance().deviceId
        )
        val localLanguage = Locale.getDefault().language
        serviceCreator.addHeader(ServiceCreator.ACCEPT_LANGUAGE_KEY, localLanguage)
        songApi = serviceCreator.create(KaraokeSongApi::class.java)
    }

    fun addHeader(key: String, value: String) {
        serviceCreator.addHeader(key, value)
    }

    suspend fun orderSong(
        liveRecordId: Long,
        songId: String,
        songName: String?,
        songCover: String?,
        songTime: Long?,
        channel: Int?
    ): Response<NEKaraokeOrderSongResult> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveRecordId" to liveRecordId,
            "songId" to songId,
            "songName" to songName,
            "songCover" to songCover,
            "songTime" to songTime,
            "channel" to channel
        )
        songApi.orderSong(params)
    }

    suspend fun switchSong(
        liveRecordId: Long,
        orderId: Long
    ): Response<Boolean> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveRecordId" to liveRecordId,
            "currentOrderId" to orderId
        )
        songApi.switchSong(params)
    }

    suspend fun getOrderSongs(
        liveRecordId: Long
    ): Response<List<NEKaraokeOrderSongResult>> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveRecordId" to liveRecordId
        )
        songApi.getOrderSongs(params)
    }

    suspend fun cancelOrderSong(
        liveRecordId: Long,
        orderId: Long
    ): Response<Boolean> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveRecordId" to liveRecordId,
            "orderId" to orderId
        )
        songApi.cancelOrderSong(params)
    }

    suspend fun songSetTop(
        liveRecordId: Long,
        orderId: Long
    ): Response<Boolean> = withContext(Dispatchers.IO) {
        val params = mapOf<String, Any?>(
            "liveRecordId" to liveRecordId,
            "orderId" to orderId
        )
        songApi.songSetTop(params)
    }
}
