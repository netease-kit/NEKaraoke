/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.media.AudioFormat
import android.os.Handler
import android.os.Looper
import com.netease.lava.nertc.sdk.NERtcConstants
import com.netease.lava.nertc.sdk.NERtcEx
import com.netease.lava.nertc.sdk.audio.NERtcAudioFrameRequestFormat
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFormat
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeConstant
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeErrorCode
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongModelResult
import com.netease.yunxin.kit.karaokekit.impl.utils.AudioMixHelper
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeLog
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NERoomContext
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomListenerAdapter
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomRtcAudioFrameObserver
import com.netease.yunxin.kit.roomkit.api.NERoomRtcController
import com.netease.yunxin.kit.roomkit.api.model.NERoomCreateAudioEffectOption
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioFrame
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioFrameOpMode
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioFrameRequestFormat
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioStreamType
import com.netease.yunxin.kit.roomkit.api.model.NERoomSessionTypeEnum
import com.netease.yunxin.kit.roomkit.api.model.NEVideoStreamType
import com.netease.yunxin.kit.roomkit.api.service.NEMessageChannelListener
import com.netease.yunxin.kit.roomkit.api.service.NERoomCustomMessage
import com.netease.yunxin.kit.roomkit.api.service.NERoomRecentSession
import com.netease.yunxin.kit.roomkit.api.service.NERoomSessionMessage
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import org.json.JSONObject

/**
 *
 */
internal class AudioPlayServiceImpl :
    NEAudioPlayService,
    NERoomRtcAudioFrameObserver,
    NERoomListenerAdapter(),
    NEMessageChannelListener {
    companion object {
        const val DEFAULT_SEND_VOLUME = 50
        const val DEFAULT_PLAYBACK_VOLUME = 50
    }
    private var tag = "NEAudioPlayServiceImpl"
    private var rtcController: NERoomRtcController? = null
    private var sendVolume = DEFAULT_SEND_VOLUME // 伴音默认的发送音量
    private var playbackVolume = DEFAULT_PLAYBACK_VOLUME // 伴音默认本地播放的音量
    private var isOriginal = false // 默认是伴唱
    private val playCallbacks: ArrayList<NEPlayStateChangeCallback> = ArrayList()
    private var songDuration: Long = 0 // 歌曲时长
    private var isAnchor: Boolean = false // 默认不是主唱
    private var songMode: NEKaraokeSongMode? = null // 默认是null，是观众
    private val cacheFrames: LinkedList<ByteArray> by lazy { // 串行合唱使用 缓存音频数据
        LinkedList()
    }
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    private val mixer: AudioMixHelper by lazy { // 串行合唱使用，混流操作
        AudioMixHelper()
    }
    private var anchorUuid: String? = null // 主唱uid
    private var chorusUid: String? = null // 副唱uid
    private var roomContext: NERoomContext? = null
    private val COMMAND_ID = 10001 // / 发送ntp消息
    private var orderId: Long = 0
    private lateinit var originPath: String
    private lateinit var accompanyPath: String
    private var realDelayTime: Long = 0
    private val PLAY_PAUSE_DELAY_TIME: Long = 1000
    private val TYPE_PAUSE = 1
    private val TYPE_RESUME = 2
    private val PROGRESS_INTERVAL: Long = 100 // 进度刷新频次

    override fun init(roomUuid: String) {
        roomContext = NERoomKit.getInstance().roomService.getRoomContext(roomUuid)
        rtcController = roomContext?.rtcController
        rtcController?.setStreamAlignmentProperty(true) // 设置对齐本地系统与服务端的时间为 可用
        setAudioFrameRequestFormat()
        roomContext?.addRoomListener(this)
        setCustomMessageListener()
    }

    override fun startLocalSong(
        originPath: String,
        accompanyPath: String,
        volume: Int,
        anchorUuid: String,
        chorusUid: String?,
        startTimeStamp: Long,
        anchor: Boolean,
        mode: NEKaraokeSongMode
    ) {
        KaraokeLog.i(tag, "originPath : $originPath,  accompanyPath: $accompanyPath，songMode：$mode")
        reset()
        rtcController?.setChannelProfile(NERtcConstants.RTCChannelProfile.Karaoke)
        this.originPath = originPath
        this.realDelayTime = startTimeStamp
        this.accompanyPath = accompanyPath
        this.songMode = mode
        this.anchorUuid = anchorUuid
        this.chorusUid = chorusUid
        this.isAnchor = anchor
        this.sendVolume = volume
        this.playbackVolume = volume
        if (mode == NEKaraokeSongMode.SOLO) { // 独唱
            if (isAnchor()) {
                rtcController?.setAudioFrameObserver(this)
                solo(originPath, accompanyPath, 0)
            }
        } else if (mode == NEKaraokeSongMode.SERIAL_CHORUS) { // 串行合唱
            if (isAnchor()) {
                serialChorusForAnchor(originPath, accompanyPath, 0, chorusUid!!)
            } else if (isChorus()) { // 副唱添加监听
                serialChorusForChorus()
            }
        } else if (mode == NEKaraokeSongMode.REAL_TIME_CHORUS) { // 实时合唱
            // / 如果是主唱，就不要订阅副唱的sei（性能优化，rtc的同学说取消订阅可能会省0～200ms的延时）
            if (isAnchor()) {
                chorusUid?.let {
                    rtcController?.unsubscribeRemoteVideoStream(chorusUid, NEVideoStreamType.LOW)
                }
            }
            // / 如果是副唱，就不要订阅主唱的sei（性能优化，rtc的同学说取消订阅可能会省0～200ms的延时）
            if (isChorus()) {
                rtcController?.unsubscribeRemoteVideoStream(anchorUuid, NEVideoStreamType.LOW)
            }

            rtcController?.setParameters("engine.audio.ktv.chrous", true)
            if (isAnchor()) { // 主唱 发送NTp，并开始倒计时，以及开始播放前的准备
                val currentTime = System.currentTimeMillis()
                val startTime = currentTime + startTimeStamp
                KaraokeLog.i("ntp", "主唱ntp:${currentTime + startTimeStamp}")
                realTimeChorusForAnchor(originPath, accompanyPath, startTime)
                sendNTP(chorusUid = chorusUid, currentTime = currentTime)
            } else if (isChorus()) { // 副唱
                realTimeChorusForChorus()
            }
        }
    }

    /**
     * 实时合唱 给副唱发送NTP
     */
    private fun sendNTP(type: Int = 0, chorusUid: String?, currentTime: Long) {
        val json = JSONObject()
        KaraokeLog.i(tag, "getNtpTimeOffset:${rtcController?.getNtpTimeOffset() ?: 0}")
        val serverTime = currentTime - (rtcController?.getNtpTimeOffset() ?: 0)
        val delayTime = if (type == 0) realDelayTime else PLAY_PAUSE_DELAY_TIME
        val anchorStartTime = serverTime + delayTime
        json.put("ntp_timestamp", anchorStartTime)
        json.put("type", type)
        sendCustomMessage(chorusUid!!, COMMAND_ID, json.toString())
    }

    /**
     * 切换或者停止当前歌曲 ，做一些重置操作
     */
    private fun reset() {
        songDuration = 0
        isOriginal = false
        if (songMode == NEKaraokeSongMode.SERIAL_CHORUS && isAnchor()) { // 如果是串行合唱，要重置主唱：1、将自己声音可以发送给所有人 2、恢复订阅副唱音频
            rtcController?.setAudioSubscribeOnlyBy(emptyList())
            chorusUid?.apply {
                rtcController?.subscribeRemoteAudioStream(this)
            }
        }
        rtcController?.setAudioFrameObserver(null) // 设置 音频数据监听为null，取消注册
        if (isChorus()) {
            anchorUuid?.apply { // / 恢复订阅 副唱的音频辅流
                rtcController?.subscribeRemoteAudioSubStream(anchorUuid!!)
            }
        }
        rtcController?.setParameters("engine.audio.ktv.chrous", false)
        rtcController?.setChannelProfile(NERtcConstants.RTCChannelProfile.HIGHQUALITY_CHATROOM)
        rtcController?.disableLocalSubStreamAudio() // / 关闭主唱音频辅流
        rtcController?.stopEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN)
        rtcController?.stopEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY)
        isAnchor = false
        chorusUid = null
        anchorUuid = null
    }

    private fun setCustomMessageListener() {
        NERoomKit.getInstance().messageChannelService.addMessageChannelListener(this)
    }

    override fun pauseSong(songModel: KaraokeSongModelResult) {
        if (songMode == NEKaraokeSongMode.REAL_TIME_CHORUS) {
            if (isAnchor()) {
                val currentTime = System.currentTimeMillis()
                sendNTP(TYPE_PAUSE, chorusUid, currentTime)
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        pauseLocalSong()
                    },
                    PLAY_PAUSE_DELAY_TIME
                )
            }
        } else {
            pauseLocalSong()
        }
    }

    override fun pauseLocalSong() {
        rtcController?.pauseEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN)
        rtcController?.pauseEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY)
    }

    override fun resumeLocalSong() {
        rtcController?.resumeEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN)
        rtcController?.resumeEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY)
    }

    override fun startSong(orderId: Long) {
        this.orderId = orderId
    }

    override fun resumeSong(songModel: KaraokeSongModelResult) {
        if (songMode == NEKaraokeSongMode.REAL_TIME_CHORUS) {
            if (isAnchor()) {
                val currentTime = System.currentTimeMillis()
                sendNTP(TYPE_RESUME, chorusUid, currentTime)
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        resumeLocalSong()
                    },
                    PLAY_PAUSE_DELAY_TIME
                )
            }
        } else {
            resumeLocalSong()
        }
    }

    override fun nextSong(songModel: KaraokeSongModelResult) {
    }

    override fun stopSong(songModel: KaraokeSongModelResult) {
        if (isAnchor() || (isChorus() && (songMode == NEKaraokeSongMode.REAL_TIME_CHORUS || songMode == NEKaraokeSongMode.SERIAL_CHORUS))) {
            // / 只有实时合唱的情况下，主副唱都可以操作播控，所以此时主副唱要做操作同步
            reset()
        }
        if (isAnchor()) {
            songModel.singInfo?.assistantUuid?.let {
                rtcController?.subscribeRemoteVideoStream(it, NEVideoStreamType.LOW)
            }
        }

        if (isChorus()) {
            songModel.singInfo?.userUuid?.let {
                rtcController?.subscribeRemoteVideoStream(it, NEVideoStreamType.LOW)
            }
        }
    }

    override fun switchToOriginalVolume(isOriginal: Boolean): Int {
        KaraokeLog.d(tag, "isOriginal : $isOriginal")
        this.isOriginal = isOriginal

        val currentAccompanyVolume = rtcController?.getEffectSendVolume(
            NEKaraokeConstant.EFFECT_ID_ACCOMPANY
        ) ?: DEFAULT_SEND_VOLUME
        val currentOriginVolume = rtcController?.getEffectSendVolume(
            NEKaraokeConstant.EFFECT_ID_ORIGIN
        ) ?: DEFAULT_SEND_VOLUME
        var currentVolume = DEFAULT_SEND_VOLUME
        if (!isOriginal) {
            if (currentOriginVolume > 0) {
                currentVolume = currentOriginVolume
            }
            // / 伴奏设置
            rtcController?.setEffectPlaybackVolume(
                NEKaraokeConstant.EFFECT_ID_ACCOMPANY,
                currentVolume
            )
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, currentVolume)

            // 原唱设置
            rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
            songDuration = rtcController?.getEffectDurationWithId(
                NEKaraokeConstant.EFFECT_ID_ACCOMPANY
            ) ?: 0
        } else {
            if (currentAccompanyVolume > 0) {
                currentVolume = currentAccompanyVolume
            }
            // / 伴奏设置
            rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 0)
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 0)

            // 原唱设置
            rtcController?.setEffectPlaybackVolume(
                NEKaraokeConstant.EFFECT_ID_ORIGIN,
                currentVolume
            )
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, currentVolume)
            songDuration = rtcController?.getEffectDurationWithId(
                NEKaraokeConstant.EFFECT_ID_ORIGIN
            ) ?: 0
        }
        return NEKaraokeErrorCode.SUCCESS
    }

    override fun setPlaybackVolume(volume: Int) {
        KaraokeLog.d(tag, "setPlaybackVolume : $isOriginal,volume:$volume")
        if (isOriginal) {
            rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, volume)
        } else {
            rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, volume)
        }
    }

    override fun setSendVolume(volume: Int) {
        KaraokeLog.d(tag, "setSendVolume : $isOriginal,volume:$volume")
        if (isOriginal) {
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, volume)
        } else {
            rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, volume)
        }
    }

    override fun seek(position: Long): Int {
        rtcController?.setEffectPosition(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, position)
        rtcController?.setEffectPosition(NEKaraokeConstant.EFFECT_ID_ORIGIN, position)
        return NEKaraokeErrorCode.SUCCESS
    }

    override fun isOriginal(): Boolean {
        return isOriginal
    }

    override fun currentEffectId(): Int {
        if (isOriginal) {
            return NEKaraokeConstant.EFFECT_ID_ORIGIN
        }
        return NEKaraokeConstant.EFFECT_ID_ACCOMPANY
    }

    override fun addPlayStateChangeCallback(callback: NEPlayStateChangeCallback) {
        playCallbacks.add(callback)
    }

    override fun removePlayStateChangeCallback(callback: NEPlayStateChangeCallback) {
        playCallbacks.remove(callback)
    }

    override fun destroy() {
        reset()
        playCallbacks.clear()
        roomContext?.removeRoomListener(this)
        NERoomKit.getInstance().messageChannelService.removeMessageChannelListener(this)
    }

    override fun onRtcRecvSEIMsg(uuid: String, seiMsg: String) {
        val sei = SEI.createFromJson(seiMsg)
        val timestamp = sei.pos // 经验值减去传输花费的时间
        KaraokeLog.d(tag, "onRtcRecvSEIMsg orderId = $orderId sei = $sei")
        if (orderId == sei.orderId || orderId == 0L || sei.orderId == 0L) { // 排除主唱和副唱有值，但是不等的情况
            if (songMode == NEKaraokeSongMode.SERIAL_CHORUS) { // 如果是串行合唱，副唱收到主唱发送的播放进度，副唱将它从副唱侧在发送出去。听众收到主唱发送的SEI不处理，只处理副唱发来的进度
                if (uuid == anchorUuid) { // / 主唱发送的SEI消息
                    if (isChorus()) { // / 当前用户是合唱者,将合唱者的播放进度通知出去
                        playCallbacks.forEach {
                            it.onSongPlayPosition(timestamp)
                        }
                        sendSEIMsg(timestamp + 50)
                    }
                } else {
                    if (!isAnchor() && !isChorus()) { // 是串行 不是主唱发来的，就是副唱发来的，观众更新进度
                        playCallbacks.forEach {
                            it.onSongPlayPosition(timestamp)
                        }
                    }
                }
            } else {
                if (!isChorus() && !isAnchor()) {
                    playCallbacks.forEach { // // 不是串行 观众根据收到的进度去更新。如果是合唱者 则进度是根据自身播放进度回调去获取的
                        it.onSongPlayPosition(timestamp)
                    }
                }
            }
        }
    }

    override fun onAudioEffectTimestampUpdate(effectId: Long, timeStampMS: Long) {
//        KaraokeLog.i(TAG,"onAudioEffectTimestampUpdate:$timeStampMS")
        if (isAnchor()) { // 如果是主唱，要发送SEI消息，同步播放进度。
            sendSEIMsg(timeStampMS)
        }

        playCallbacks.forEach {
            Handler(Looper.getMainLooper()).post {
                it.onSongPlayPosition(timeStampMS)
            }
        }
    }

    override fun onChatroomMessageAttachmentProgress(
        messageUuid: String,
        transferred: Long,
        total: Long
    ) {
    }

    override fun onAudioMixingStateChanged(reason: Int) {
        KaraokeLog.i(tag, "onAudioMixingStateChanged: reason=$reason")
    }

    // 当前用户是否是主唱
    private fun isAnchor(): Boolean {
        return isAnchor
    }

    // 当前用户是否是副唱
    private fun isChorus(): Boolean {
        val localMemberUuid = NEKaraokeKit.getInstance().localMember!!.account
        return localMemberUuid == chorusUid
    }

    /**
     * 独唱--主唱(点歌者)以主流的方式发送伴音，默认以伴奏
     * @param originPath 原唱文件path
     * @param accompanyPath 伴音文件path
     * @param startTimeStamp 开始播放的时间
     */
    private fun solo(originPath: String, accompanyPath: String, startTimeStamp: Long) {
        val originOption = NERoomCreateAudioEffectOption( // 原唱 音效配置
            path = originPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = 0,
            playbackEnabled = true,
            playbackVolume = 0,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain
        )
        val accompanyOption = NERoomCreateAudioEffectOption(
            // 伴音 音效配置
            path = accompanyPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = sendVolume,
            playbackEnabled = true,
            playbackVolume = playbackVolume,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain
        )
        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 100)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, sendVolume)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, accompanyOption)

        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN, originOption)
        songDuration = rtcController?.getEffectDurationWithId(NEKaraokeConstant.EFFECT_ID_ACCOMPANY) ?: 0
    }

    /**
     * 计算开始播放时间---逻辑：
     * 主唱会将自己的根据自己的ntpOffset时间计算出服务器时间，
     * 那么此处收到服务时间后+自己的ntpOffset时间+业务设置的多长
     * 时间后开始，就是实际要开始的时间
     *
     * @param receiveServerTime 接收到主唱计算完的服务器时间
     */
    private fun getLocalStartTime(receiveServerTime: Long): Long {
        KaraokeLog.i(
            tag,
            "副唱 getLocalStartTime   getNtpTimeOffset:${rtcController?.getNtpTimeOffset() ?: 0}"
        )
        val ntpTimeOffset = rtcController?.getNtpTimeOffset() ?: 0
        return receiveServerTime + ntpTimeOffset
    }

    /**
     *  发送SEI信息 主要用户歌词同步使用
     *  在接收到该信息之后需要同步给歌词组件
     *  @param pos  当前伴音播放进度
     */
    private fun sendSEIMsg(pos: Long) {
        if (pos < 500) {
            KaraokeLog.i(tag, "sendSEIMsg pos = $pos")
        }
        rtcController?.sendSEIMsg(SEI(orderId, pos).toJson())
    }

    /**
     * 处理串行合唱时主唱要做的处理
     *参考文档https://docs.popo.netease.com/lingxi/3e9e1e699bf547eab53cf53637439807?popo_locale=zh-CN#edit
     * 1、主唱播放伴奏，并以主流方式发送伴奏
     * 2、主唱的干声和伴奏只有副唱才能听到，主唱不订阅副唱的干声(流)
     *
     * @param originPath 原唱path
     * @param accompanyPath 伴奏path
     * @param startTimeStamp 开始播放时间
     * @param chorusUid 合唱者的uid
     */
    private fun serialChorusForAnchor(
        originPath: String,
        accompanyPath: String,
        startTimeStamp: Long,
        chorusUid: String
    ) {
        val uidList = ArrayList<String>()
        uidList.add(chorusUid)
        rtcController?.setAudioSubscribeOnlyBy(uidList)
        rtcController?.unsubscribeRemoteAudioStream(chorusUid)

        val originOption = NERoomCreateAudioEffectOption( // 原唱 音效配置
            path = originPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = 0,
            playbackEnabled = true,
            playbackVolume = 0,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain
        )
        val accompanyOption = NERoomCreateAudioEffectOption(
            // 伴音 音效配置
            path = accompanyPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = sendVolume,
            playbackEnabled = true,
            playbackVolume = playbackVolume,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeMain
        )
        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 100)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, sendVolume)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, accompanyOption)

        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN, originOption)
        songDuration = rtcController?.getEffectDurationWithId(NEKaraokeConstant.EFFECT_ID_ACCOMPANY) ?: 0
    }

    /**
     * 设置采集的音频格式
     * 设置录制和播放声音混音之后的数据格式
     *
     */
    private fun setAudioFrameRequestFormat() {
        val frameRequestFormat = NERoomRtcAudioFrameRequestFormat(
            channels = 2,
            sampleRate = 48000,
            opMode = NERoomRtcAudioFrameOpMode.audioFrameOpModeReadWrite
        )
        rtcController?.setRecordingAudioFrameParameters(frameRequestFormat)

        val mixRequestFormat = NERoomRtcAudioFrameRequestFormat(
            channels = 2,
            sampleRate = 48000,
            opMode = NERoomRtcAudioFrameOpMode.audioFrameOpModeReadOnly
        )
        rtcController?.setMixedAudioFrameParameters(mixRequestFormat)
        val beforeMixRequestFormat = NERtcAudioFrameRequestFormat()
        beforeMixRequestFormat.channels = 2
        beforeMixRequestFormat.sampleRate = 48000
        beforeMixRequestFormat.opMode = NERoomRtcAudioFrameOpMode.audioFrameOpModeReadOnly
        NERtcEx.getInstance().setPlaybackBeforeMixingAudioFrameParameters(beforeMixRequestFormat)
    }

    /**
     * 串行合唱 副唱处理
     *1、注册监听音频数据，mix主唱音频
     */
    private fun serialChorusForChorus() {
        rtcController?.setAudioFrameObserver(this) // 设置监听用户 音频数据做混流使用
    }

    /**
     * 实时合唱 副唱处理
     * 1、副唱不订阅主唱 伴奏,本地播放伴奏
     *
     */
    private fun realTimeChorusForChorus() {
        anchorUuid?.apply {
            rtcController?.unsubscribeRemoteAudioSubStream(anchorUuid!!)
            KaraokeLog.d(tag, "realTimeChorusForChorus =======unsubscribeRemoteAudioSubStream=====")
        }
    }

    /**
     * 副唱 开始播放伴奏，但是不发送伴奏
     */
    private fun realTimeChorusPlaySong(
        originPath: String,
        accompanyPath: String,
        startTimeStamp: Long
    ) {
        val originOption = NERoomCreateAudioEffectOption(
            // 原唱 音效配置
            path = originPath,
            loopCount = 1,
            sendEnabled = false,
            sendVolume = 0,
            playbackEnabled = true,
            playbackVolume = 0,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeSub
        )
        val accompanyOption = NERoomCreateAudioEffectOption(
            // 伴音 音效配置
            path = accompanyPath,
            loopCount = 1,
            sendEnabled = false,
            sendVolume = 0,
            playbackEnabled = true,
            playbackVolume = playbackVolume,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeSub
        )

        KaraokeLog.i(
            "ntp",
            "realTimeChorusPlaySong startTimestamp:$startTimeStamp, system time:${System.currentTimeMillis()}, ntp:${rtcController?.getNtpTimeOffset()}"
        )
        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 100)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, sendVolume)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, accompanyOption)

        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN, originOption)
        songDuration = rtcController?.getEffectDurationWithId(NEKaraokeConstant.EFFECT_ID_ACCOMPANY) ?: 0
    }

    /**
     * 实时合唱 主唱处理
     * 1、主唱播放本地伴音
     * 2、主唱以辅流方式 发送伴音
     * 3、打开低延时模式
     */
    private fun realTimeChorusForAnchor(
        originPath: String,
        accompanyPath: String,
        startTimeStamp: Long
    ) {
        val originOption = NERoomCreateAudioEffectOption(
            // 原唱 音效配置
            path = originPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = 0,
            playbackEnabled = true,
            playbackVolume = 0,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeSub
        )
        val accompanyOption = NERoomCreateAudioEffectOption(
            // 伴音 音效配置
            path = accompanyPath,
            loopCount = 1,
            sendEnabled = true,
            sendVolume = sendVolume,
            playbackEnabled = true,
            playbackVolume = playbackVolume,
            startTimestamp = startTimeStamp,
            progressInterval = PROGRESS_INTERVAL,
            sendWithAudioType = NERoomRtcAudioStreamType.NERtcAudioStreamTypeSub
        )
        KaraokeLog.i(
            "ntp",
            "realTimeChorusForAnchor startTimestamp:$startTimeStamp, system time:${System.currentTimeMillis()}, ntp:${rtcController?.getNtpTimeOffset()}"
        )
        rtcController?.enableLocalSubStreamAudio() // / 开启主唱音频辅流

        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, 100)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, sendVolume)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ACCOMPANY, accompanyOption)

        rtcController?.setEffectPlaybackVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.setEffectSendVolume(NEKaraokeConstant.EFFECT_ID_ORIGIN, 0)
        rtcController?.playEffect(NEKaraokeConstant.EFFECT_ID_ORIGIN, originOption)
        songDuration = rtcController?.getEffectDurationWithId(NEKaraokeConstant.EFFECT_ID_ACCOMPANY) ?: 0
    }

    override fun onRecordFrame(audioFrame: NERoomRtcAudioFrame) {
        if (songMode == NEKaraokeSongMode.SERIAL_CHORUS && isChorus()) { // 如果是串行合唱并且当前用户是合唱者
            try {
                lock.write {
                    //            if (songMode == NESongMode.SERIAL_CHORUS && chorusUid == localMemberUuid) { // 如果是串行合唱并且当前用户是合唱者
                    if (cacheFrames.size > 0) {
                        val buf = cacheFrames.poll() ?: return
                        val format = audioFrame.format
                        val length =
                            format.bytesPerSample * format.samplesPerChannel * format.channels
                        val destBuffer = ByteArray(length)
                        val recordByteBuffer = audioFrame.data
                        recordByteBuffer.position(0)
                        recordByteBuffer.get(destBuffer, 0, length)
                        val buffer = mixer.mixAudioFrameData(
                            destBuffer,
                            buf,
                            format.samplesPerChannel,
                            format.channels
                        )
                        if (buffer == null) {
                            KaraokeLog.i(tag, "mixAudioFrameData buffer is null")
                            return
                        }
                        recordByteBuffer.position(0)
                        recordByteBuffer.put(buffer)
                    }
//            }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (songMode == NEKaraokeSongMode.SOLO && isAnchor()) {
            playCallbacks.forEach {
                val frame = NEKaraokeAudioFrame(
                    format = NEKaraokeAudioFormat(
                        audioFormat = AudioFormat.ENCODING_PCM_16BIT,
                        channels = audioFrame.format.channels,
                        sampleRate = audioFrame.format.sampleRate,
                        bytesPerSample = audioFrame.format.bytesPerSample,
                        samplesPerChannel = audioFrame.format.samplesPerChannel
                    ),
                    data = audioFrame.data
                )
                it.onRecordAudioFrame(frame)
            }
        }
    }

    override fun onRecordSubStreamAudioFrame(audioFrame: NERoomRtcAudioFrame) {
    }

    override fun onPlaybackFrame(audioFrame: NERoomRtcAudioFrame) {
    }

    override fun onPlaybackAudioFrameBeforeMixingWithUserID(
        userUuid: String,
        audioFrame: NERoomRtcAudioFrame
    ) {
        if (songMode == NEKaraokeSongMode.SERIAL_CHORUS && isChorus() && userUuid == anchorUuid) { // 是串行合唱，当前用户是合唱者，并且过来的音频数据是主唱的
            try {
                val format = audioFrame.format
                val length = format.bytesPerSample * format.samplesPerChannel * format.channels
                val buf = ByteArray(length)
                audioFrame.data.get(buf)
                lock.write {
                    if (cacheFrames.size >= 10) {
                        cacheFrames.poll()
                    }
                    cacheFrames.offer(buf)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPlaybackSubStreamAudioFrameBeforeMixingWithUserID(
        userUuid: String,
        audioFrame: NERoomRtcAudioFrame
    ) {
    }

    override fun onMixedAudioFrame(audioFrame: NERoomRtcAudioFrame) {
    }

    override fun onMemberJoinRtcChannel(members: List<NERoomMember>) {
        members.forEach {
            rtcController?.subscribeRemoteVideoStream(
                it.uuid,
                NEVideoStreamType.LOW
            )
        }
    }

    override fun onMemberLeaveRtcChannel(members: List<NERoomMember>) {
        members.forEach {
            rtcController?.unsubscribeRemoteVideoStream(
                it.uuid,
                NEVideoStreamType.LOW
            )
        }
    }

    override fun onAudioEffectFinished(effectId: Int) {
        if (NEKaraokeConstant.EFFECT_ID_ACCOMPANY == effectId) {
            playCallbacks.forEach {
                it.onSongPlayCompleted()
                KaraokeLog.d(
                    tag,
                    "onAudioEffectFinished effectId = $effectId"
                )
            }
        }
    }

    private fun sendCustomMessage(userUuid: String, commandId: Int, data: String) {
        roomContext?.roomUuid?.let {
            NERoomKit.getInstance().messageChannelService.sendCustomMessage(
                roomUuid = it,
                userUuid,
                commandId = commandId,
                data = data,
                object : NECallback2<Unit>() {
                    override fun onSuccess(data: Unit?) {
                        KaraokeLog.d(tag, "sendCustomMessage success")
                    }

                    override fun onError(code: Int, message: String?) {
                        KaraokeLog.d(tag, "sendCustomMessage onError :$code")
                    }
                }
            )
        }
    }

    override fun onCustomMessageReceived(message: NERoomCustomMessage) {
        try {
            val json = JSONObject(message.data)
            val commandId = message.commandId
            if (commandId == COMMAND_ID) { // / 实时合唱 主唱发来自定义消息，开始播放时间
                val receiveServerTime = json.optLong("ntp_timestamp")
                when (json.opt("type")) {
                    TYPE_PAUSE -> {
                        val offsetTime =
                            System.currentTimeMillis() - getLocalStartTime(receiveServerTime)
                        val executeTime: Long = if (offsetTime < 0) {
                            getLocalStartTime(receiveServerTime) - System.currentTimeMillis()
                        } else {
                            System.currentTimeMillis() - getLocalStartTime(receiveServerTime)
                        }
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                pauseLocalSong()
                            },
                            executeTime
                        )
                    }
                    TYPE_RESUME -> {
                        val offsetTime =
                            System.currentTimeMillis() - getLocalStartTime(receiveServerTime)
                        val executeTime: Long = if (offsetTime < 0) {
                            getLocalStartTime(receiveServerTime) - System.currentTimeMillis()
                        } else {
                            System.currentTimeMillis() - getLocalStartTime(receiveServerTime)
                        }
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                resumeLocalSong()
                            },
                            executeTime
                        )
                    }
                    else -> {
                        KaraokeLog.i("ntp", "副唱ntp:${getLocalStartTime(receiveServerTime)}")
                        realTimeChorusPlaySong(
                            originPath,
                            accompanyPath,
                            getLocalStartTime(receiveServerTime)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            KaraokeLog.e(tag, "onReceiveCustomMessage message.data is not json")
            e.printStackTrace()
        }
    }

    override fun onSessionMessageAllDeleted(sessionId: String, sessionType: NERoomSessionTypeEnum) {
        TODO("Not yet implemented")
    }

    override fun onSessionMessageDeleted(message: NERoomSessionMessage?) {
        TODO("Not yet implemented")
    }

    override fun onSessionMessageReceived(message: NERoomSessionMessage?) {
        TODO("Not yet implemented")
    }

    override fun onSessionMessageRecentChanged(messages: List<NERoomRecentSession>?) {
        TODO("Not yet implemented")
    }
}
