/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api

/** 下载回调  */
interface NEKaraokeCopyrightedMediaListener {
    /**
     * 开始下载
     *
     * @param songId 歌曲编号
     */
    fun onPreloadStart(songId: String?, channel: Int)

    /**
     * 下载进度更新
     *
     * @param songId 歌曲编号
     * @param progress 进度
     */
    fun onPreloadProgress(songId: String?, channel: Int, progress: Float)

    /**
     * 下载完成
     *
     * @param songId 歌曲编号
     * @param errorCode 错误码
     * @param msg 错误信息
     */
    fun onPreloadComplete(songId: String?, channel: Int, errorCode: Int, msg: String?)
}
