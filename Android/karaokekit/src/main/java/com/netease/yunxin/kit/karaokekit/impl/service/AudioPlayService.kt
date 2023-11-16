/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.annotation.SuppressLint
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongModelResult
import org.json.JSONObject

/**
 * 唱歌 相关操作接口
 * 独唱、合唱
 */
interface NEAudioPlayService {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmField
        val instance: NEAudioPlayService = AudioPlayServiceImpl()

        @JvmStatic
        fun getInstance(): NEAudioPlayService = instance
    }

    /**
     * 初始化
     * @param roomUuid 房间id
     */
    fun init(roomUuid: String)

    fun startLocalSong(
        originPath: String,
        accompanyPath: String,
        volume: Int,
        anchorUuid: String,
        chorusUid: String?,
        startTimeStamp: Long,
        anchor: Boolean,
        mode: NEKaraokeSongMode
    )

    fun pauseLocalSong()

    fun resumeLocalSong()

    /**
     * 开始歌曲
     */
    fun startSong(orderId: Long)

    /**
     * 暂停歌曲
     */
    fun pauseSong(songModel: KaraokeSongModelResult)

    /**
     * 恢复播放
     */
    fun resumeSong(songModel: KaraokeSongModelResult)

    /**
     * 停止播放
     */
    fun stopSong(songModel: KaraokeSongModelResult)

    fun nextSong(songModel: KaraokeSongModelResult)

    /**
     * 切换原声/伴奏
     * 默认 false 伴奏
     * @param isOriginal true : 原声, false : 伴奏
     */
    fun switchToOriginalVolume(isOriginal: Boolean): Int

    /**
     * 设置本地伴音播放音量
     *@param volume 0～200取值，默认50
     */
    fun setPlaybackVolume(volume: Int)

    /**
     * 设置伴音发送音量
     *@param volume 0～200取值，默认50
     */
    fun setSendVolume(volume: Int)

    /**
     * 设置 seek
     */
    fun seek(position: Long): Int

    /**
     * 添加播放状态变更 回调
     */
    fun addPlayStateChangeCallback(callback: NEPlayStateChangeCallback)

    /**
     * 移除播放状态变更 回调
     */
    fun removePlayStateChangeCallback(callback: NEPlayStateChangeCallback)

    /**
     * 当前是否是原唱
     */
    fun isOriginal(): Boolean

    /**
     * 获取当前音效ID
     */
    fun currentEffectId(): Int

    /**
     * 资源回收处理
     */
    fun destroy()
}

/**
 * 播放状态的相关回调
 */
interface NEPlayStateChangeCallback {

    /**
     * 歌曲播放的位置
     */
    fun onSongPlayPosition(position: Long)

    /**
     *  Rtc audioFrame数据回调
     * @param frame 回调数据
     */
    fun onRecordAudioFrame(frame: NEKaraokeAudioFrame)

    /**
     * 播放完成
     */
    fun onSongPlayCompleted()
}

/**
 * 进度同步发送SEI 使用
 */
internal data class SEI(var orderId: Long, var pos: Long) {

    fun toJson(): String {
        val json = JSONObject()
        json.put("orderId", orderId)
        json.put("pos", pos)
        return json.toString()
    }

    companion object {
        fun createFromJson(seiMsg: String): SEI {
            val json = JSONObject(seiMsg)
            val orderId = json.optLong("orderId")
            val pos = json.optLong("pos")
            return SEI(orderId, pos)
        }
    }
}
