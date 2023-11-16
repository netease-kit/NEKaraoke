/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.content.Context
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotDimension
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotType
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia.Callback
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType
import com.netease.yunxin.kit.copyrightedmedia.api.SongScene
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedHotSong
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedSong
import com.netease.yunxin.kit.copyrightedmedia.impl.NECopyrightedEventHandler

internal class CopyrightedMediaService {

    private var copyrightedMedia: NECopyrightedMedia? = null

    /**
     * 初始化 NECopyrightedMedia
     * 进入主界面时候调用
     *
     * @param context 上下文
     * @param appKey
     * @param token   用户鉴权
     * @param account 用户ID，可选
     */
    fun initialize(
        context: Context,
        appKey: String,
        token: String,
        account: String,
        extras: Map<String, Any?>? = mapOf(),
        callback: Callback<Unit>? = null
    ) {
        copyrightedMedia = NECopyrightedMedia.getInstance()
        copyrightedMedia?.initialize(context, appKey, token, account, extras, callback)
    }

    /**
     * 更新token
     * @param token            鉴权token
     */
    fun renewToken(token: String) {
        copyrightedMedia?.renewToken(token)
    }

    /**
     * 注册事件通知回调
     */
    fun setEventHandler(eventHandler: NECopyrightedEventHandler) {
        copyrightedMedia?.setEventHandler(eventHandler)
    }

    /**
     * 预加载 歌曲数据
     *
     * @param songId            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback          下载回调 [NESongPreloadCallback]
     */
    fun preloadSong(songId: String, channel: Int, callback: NESongPreloadCallback) {
        copyrightedMedia?.preloadSong(songId, channel, callback)
    }

    /**
     * 清理本地所有缓存歌曲数据
     */
    fun clearSongCache() {
        copyrightedMedia?.clearSongCache()
    }

    /**
     * 取消预加载 Song 数据
     *
     * @param songId            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     */
    fun cancelPreloadSong(songId: String, channel: Int) {
        copyrightedMedia?.cancelPreloadSong(songId, channel)
    }

    /**
     * 检测是否已预加载 Song 数据
     *
     * @param songID            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun isSongPreloaded(songID: String, channel: Int): Boolean {
        return copyrightedMedia?.isSongPreloaded(songID, channel) ?: false
    }

    /**
     * 搜索歌曲
     *
     * @param keyword            关键字
     * @param pageNum            页码  默认值为0
     * @param pageSize           页大小 默认值为20
     * @param channel            版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback           回调[Callback]
     */
    fun searchSong(
        keyword: String,
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: Callback<List<NECopyrightedSong>>
    ) {
        copyrightedMedia?.searchSong(keyword, channel, pageNum, pageSize, callback)
    }

    /**
     * 原唱&伴奏：用于播放的本地文件路径
     *
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param songResType      资源类型 1：原唱，2：伴奏
     * @return
     */
    fun getSongURI(songId: String, channel: Int, songResType: SongResType): String? {
        return copyrightedMedia?.getSongURI(songId, channel, songResType)
    }

    /**
     * 本地歌词
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun getLyric(songId: String, channel: Int): String? {
        return copyrightedMedia?.getLyric(songId, channel)
    }

    /**
     * 本地MIDI
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun getPitch(songId: String, channel: Int): String? {
        return copyrightedMedia?.getPitch(songId, channel)
    }

    /**
     * 加载歌词
     *
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback         加载歌词回调 [LyricCallback]
     */
    fun preloadSongLyric(songId: String, channel: Int, callback: LyricCallback) {
        copyrightedMedia?.preloadSongLyric(songId, channel, callback)
    }

    /**
     * 歌曲列表
     *
     * @param tags           标签
     * @param pageNum        页码  默认值为0
     * @param pageSize       页大小 默认值为20
     * @param channel        版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback       回调[Callback]
     */
    fun getSongList(
        tags: List<String>? = listOf(),
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: Callback<List<NECopyrightedSong>>
    ) {
        copyrightedMedia?.getSongList(tags, channel, pageNum, pageSize, callback)
    }

    /**
     * 热门榜单
     *
     * @param hotType           点歌榜单类型
     * @param hotDimension      榜单维度
     * @param channel        版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param pageNum        页码  默认值为0
     * @param pageSize       页大小 默认值为20
     * @param callback       回调[Callback]
     */
    fun getHotSongList(
        hotType: NECopyrightedHotType,
        hotDimension: NECopyrightedHotDimension,
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: Callback<List<NECopyrightedHotSong>>
    ) {
        copyrightedMedia?.getHotSongList(
            hotType,
            hotDimension,
            channel,
            pageNum,
            pageSize,
            callback
        )
    }

    fun setSongScene(scene: SongScene) {
        copyrightedMedia?.setSongScene(scene)
    }
}
