/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.impl.repository

import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeRoomList
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.NEKaraokeDynamicToken
import retrofit2.http.Body
import retrofit2.http.POST

interface KaraokeApi {

    /**
     * 获取卡拉ok房间列表
     */
    @POST("/nemo/entertainmentLive/live/list")
    suspend fun getKaraokeRoomList(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeRoomList>

    /**
     * 创建卡拉ok 房间
     */
    @POST("/nemo/entertainmentLive/live/createLive")
    suspend fun startKaraoke(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeRoomInfo>

    /**
     * 加入成功后上报给服务器
     */
    @POST("nemo/entertainmentLive/live/joinedLiveRoom")
    suspend fun joinedKaraoke(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 直播详情
     */
    @POST("/nemo/entertainmentLive/live/info")
    suspend fun requestKaraokeInfo(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeRoomInfo>

    /**
     * 结束 ktv 房间
     */
    @POST("/nemo/entertainmentLive/live/destroyLive")
    suspend fun stopKaraoke(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 观众打赏
     */
    @POST("/nemo/entertainmentLive/live/batch/reward")
    suspend fun reward(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @POST("/nemo/entertainmentLive/ktv/sing/chorus/invite")
    suspend fun inviteChorus(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeSongInfo>

    @POST("/nemo/entertainmentLive/ktv/sing/chorus/join")
    suspend fun joinChorus(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeSongInfo>

    @POST("/nemo/entertainmentLive/ktv/sing/chorus/cancel")
    suspend fun cancelChorus(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeSongInfo>

    @POST("/nemo/entertainmentLive/ktv/sing/chorus/ready")
    suspend fun ready(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<KaraokeSongInfo>

    @POST("/nemo/entertainmentLive/ktv/sing/start")
    suspend fun startSing(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Unit>

    @POST("/nemo/entertainmentLive/ktv/sing/action")
    suspend fun playControl(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @POST("/nemo/entertainmentLive/ktv/sing/info")
    suspend fun currentRoomSingInfo(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<KaraokeSongInfo>

    @POST("/nemo/entertainmentLive/live/info")
    suspend fun getRoomInfo(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<KaraokeRoomInfo>

    @POST("/nemo/entertainmentLive/ktv/sing/abandon")
    suspend fun abandon(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    /**
     * 获取版权曲库API鉴权的token
     */
    @POST("nemo/entertainmentLive/live/song/getMusicToken")
    suspend fun getMusicToken(): Response<NEKaraokeDynamicToken>
}
