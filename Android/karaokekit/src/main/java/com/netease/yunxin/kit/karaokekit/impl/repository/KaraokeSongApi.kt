/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.repository

import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface KaraokeSongApi {

    /**
     * 点歌
     */
    @POST("/nemo/entertainmentLive/live/song/orderSong")
    suspend fun orderSong(
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<NEKaraokeOrderSongResult>

    /**
     * 切歌
     */
    @POST("/nemo/entertainmentLive/live/song/switchSong")
    suspend fun switchSong(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>

    /**
     * 点歌列表查询
     */
    @GET("/nemo/entertainmentLive/live/song/getOrderSongs")
    suspend fun getOrderSongs(
        @QueryMap params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<NEKaraokeOrderSongResult>>

    /**
     * 已点歌曲删除
     */
    @POST("/nemo/entertainmentLive/live/song/cancelOrderSong")
    suspend fun cancelOrderSong(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>

    /**
     * 歌曲置顶
     */
    @POST("/nemo/entertainmentLive/live/song/songSetTop")
    suspend fun songSetTop(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>
}
