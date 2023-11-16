/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api

import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeLog
import java.nio.ByteBuffer

/**
 * 房间结束原因枚举
 */
enum class NEKaraokeEndReason {
    /**
     * 成员主动离开房间
     */
    LEAVE_BY_SELF,

    /**
     * 数据同步错误
     */
    SYNC_DATA_ERROR,

    /**
     * 多端同时加入同一房间被踢
     */
    KICK_BY_SELF,

    /**
     * 被管理员踢出房间
     */
    KICK_OUT,

    /**
     * 房间被关闭
     */
    CLOSE_BY_MEMBER,

    /**
     * 房间到期关闭
     */
    END_OF_LIFE,

    /**
     * 所有成员退出
     */
    ALL_MEMBERS_OUT,

    /**
     * 后台关闭
     */
    CLOSE_BY_BACKEND,

    /**
     * 账号异常
     */
    LOGIN_STATE_ERROR,

    /**
     * rtc 异常，退出
     */
    END_OF_RTC,

    /**
     * 未知异常
     */
    UNKNOWN;

    companion object {
        fun fromValue(value: String?): NEKaraokeEndReason = when (value) {
            LEAVE_BY_SELF.name -> LEAVE_BY_SELF
            SYNC_DATA_ERROR.name -> SYNC_DATA_ERROR
            KICK_BY_SELF.name -> KICK_BY_SELF
            KICK_OUT.name -> KICK_OUT
            CLOSE_BY_MEMBER.name -> CLOSE_BY_MEMBER
            END_OF_LIFE.name -> END_OF_LIFE
            ALL_MEMBERS_OUT.name -> ALL_MEMBERS_OUT
            CLOSE_BY_BACKEND.name -> CLOSE_BY_BACKEND
            LOGIN_STATE_ERROR.name -> LOGIN_STATE_ERROR
            END_OF_RTC.name -> END_OF_RTC
            else -> {
                KaraokeLog.e("NEKaraokeEndReason", "parse failure， Unable to identify: $value")
                UNKNOWN
            }
        }
    }
}

/**
 * 本地音频输出设备
 */
enum class NEKaraokeAudioOutputDevice {
    /**
     * 扬声器
     */
    SPEAKER_PHONE,

    /**
     * 有线耳机
     */
    WIRED_HEADSET,

    /**
     * 听筒
     */
    EARPIECE,

    /**
     * 蓝牙耳机
     */
    BLUETOOTH_HEADSET;

    companion object {
        fun fromValue(value: String?): NEKaraokeAudioOutputDevice = when (value) {
            SPEAKER_PHONE.name -> SPEAKER_PHONE
            WIRED_HEADSET.name -> WIRED_HEADSET
            EARPIECE.name -> EARPIECE
            BLUETOOTH_HEADSET.name -> BLUETOOTH_HEADSET
            else -> {
                KaraokeLog.e(
                    "NEKaraokeAudioOutputDevice",
                    "parse failure， Unable to identify: $value"
                )
                SPEAKER_PHONE
            }
        }
    }
}

/**
 * 登录事件枚举
 */
enum class NEKaraokeAuthEvent {
    /**
     * 被踢出登录
     */
    KICK_OUT,

    /**
     * 授权过期或失败
     */
    UNAUTHORIZED,

    /**
     * 服务端禁止登录
     */
    FORBIDDEN,

    /**
     * 账号或密码错误
     */
    ACCOUNT_TOKEN_ERROR,

    /**
     * 登录成功
     */
    LOGGED_IN,

    /**
     * 未登录
     */
    LOGGED_OUT,

    /**
     * Token过期
     */
    TOKEN_EXPIRED,

    /**
     * 授权错误
     */
    INCORRECT_TOKEN;

    companion object {
        fun fromValue(value: String?): NEKaraokeAuthEvent = when (value) {
            KICK_OUT.name -> KICK_OUT
            UNAUTHORIZED.name -> UNAUTHORIZED
            FORBIDDEN.name -> FORBIDDEN
            ACCOUNT_TOKEN_ERROR.name -> ACCOUNT_TOKEN_ERROR
            LOGGED_IN.name -> LOGGED_IN
            LOGGED_OUT.name -> LOGGED_OUT
            else -> {
                KaraokeLog.e("NEKaraokeAuthEvent", "parse failure， Unable to identify: $value")
                LOGGED_OUT
            }
        }
    }
}

/**
 * 错误码
 */
object NEKaraokeErrorCode {
    /**
     * 通用失败code码
     */
    const val FAILURE = -1

    /**
     * 成功code码
     */
    const val SUCCESS = 0

    /**
     * 每个用户最多点2首歌
     */
    const val ERR_ORDER_SONG_COUNT_EXCEED_LIMIT = 1009

    /**
     * 每个房间最多点10首歌
     */
    const val ERR_ROOM_ORDER_SONG_COUNT_EXCEED_LIMIT = 1010

    /**
     * 歌曲已点
     */
    const val ERR_SONG_ALREADY_ORDERED = 1011
}

/**
 * 音频帧
 * @property format 格式
 * @property data 数据
 * @constructor
 */
data class NEKaraokeAudioFrame(
    @JvmField
    var format: NEKaraokeAudioFormat,
    @JvmField
    var data: ByteBuffer
)

/**
 * 音频格式
 * @property audioFormat 音频格式
 * @property channels 音频声道数。 1：单声道  2：双声道
 * @property sampleRate 音频采样率
 * @property bytesPerSample 每个采样点的字节数
 * @property samplesPerChannel 每个声道的采样点数
 * @constructor
 */
data class NEKaraokeAudioFormat(
    /**
     * 音频格式
     */
    @JvmField
    var audioFormat: Int,
    /**
     * 音频声道数。 1：单声道  2：双声道
     */
    @JvmField
    var channels: Int,
    /**
     * 音频采样率
     */
    @JvmField
    var sampleRate: Int,
    /**
     * 每个采样点的字节数
     */
    @JvmField
    var bytesPerSample: Int,
    /**
     * 每个声道的采样点数
     */
    @JvmField
    var samplesPerChannel: Int
)

/**
 * 直播状态
 * @property value 状态
 * @constructor
 */
enum class NEKaraokeLiveState(val value: Int) {
    /**
     * 未开始
     */
    NotStart(0),

    /**
     * 直播中
     */
    Live(1),

    /**
     * 关播
     */
    LiveClose(6)
}

/**
 * 麦位状态
 */
object NEKaraokeSeatItemStatus {

    /**
     * 麦位初始化（无人，可以上麦）
     */
    const val INITIAL = 0

    /**
     * 该麦位正在等待管理员通过申请或等待成员接受邀请后上麦。
     */
    const val WAITING = 1

    /**
     * 当前麦位已被占用
     */
    const val TAKEN = 2

    /**
     * 当前麦位已关闭，不能操作上麦
     */
    const val CLOSED = -1
}

/**
 * 合唱消息
 * @property actionType 动作类型
 * @constructor
 */
enum class NEKaraokeChorusActionType(val actionType: Int) {

    /**
     * 取消合唱邀请
     */
    CANCEL_INVITE(1029),

    /**
     * 邀请合唱
     */
    INVITE(1030),

    /**
     * 同意合唱邀请
     */
    AGREE_INVITE(1031),

    /**
     * 准备好唱歌
     */
    READY(1032),

    /**
     * 开始唱歌
     */
    START_SONG(1033),

    /**
     * 暂停唱歌
     */
    PAUSE_SONG(1034),

    /**
     * 恢复唱歌
     */
    RESUME_SONG(1035),

    /**
     * 结束唱歌
     */
    END_SONG(1036),

    /**
     * 放弃歌曲
     */
    ABANDON(1037),

    /**
     * 下一曲
     */
    NEXT(1038),

    /**
     * 未知
     */
    UNKNOWN(-1);

    companion object {
        fun fromValue(value: Int): NEKaraokeChorusActionType = when (value) {
            CANCEL_INVITE.actionType -> CANCEL_INVITE
            INVITE.actionType -> INVITE
            AGREE_INVITE.actionType -> AGREE_INVITE
            READY.actionType -> READY
            START_SONG.actionType -> START_SONG
            PAUSE_SONG.actionType -> PAUSE_SONG
            RESUME_SONG.actionType -> RESUME_SONG
            END_SONG.actionType -> END_SONG
            ABANDON.actionType -> ABANDON
            NEXT.actionType -> NEXT
            else -> UNKNOWN
        }
    }
}

/**
 * 角色
 * @property value 角色值
 * @constructor
 */
enum class NEKaraokeRole(val value: String) {
    /**
     * 房主
     */
    HOST("host"),

    /**
     * 观众
     */
    AUDIENCE("audience");

    companion object {
        fun fromValue(value: String): NEKaraokeRole = when (value) {
            "host" -> HOST
            "audience" -> AUDIENCE
            else -> AUDIENCE
        }
    }
}

/**
 * K 歌模式
 * @property value 模式
 * @constructor
 */
enum class NEKaraokeSongMode(val value: Int) {
    /**
     * 独唱
     */
    SOLO(3),

    /**
     * 实时合唱
     */
    REAL_TIME_CHORUS(2),

    /**
     * 串行合唱
     */
    SERIAL_CHORUS(1),

    /**
     * 智能模式 或根据主唱副唱当下环境智能选择串行合唱还是实时合唱
     */
    INTELLIGENCE(0);

    companion object {
        fun fromValue(value: Int): NEKaraokeSongMode = when (value) {
            0 -> INTELLIGENCE
            1 -> SERIAL_CHORUS
            2 -> REAL_TIME_CHORUS
            3 -> SOLO
            else -> INTELLIGENCE
        }
    }
}

object NEKaraokeConstant {
    /**
     * 原唱的effectId
     */
    const val EFFECT_ID_ORIGIN = 1000

    /**
     * 伴奏的effectId
     */
    const val EFFECT_ID_ACCOMPANY = 1001
}

const val MUTE_VOICE_KEY = "recordDevice" // / 根据该成员属性 变更mic声音采集
const val MUTE_VOICE_VALUE_ON = "on"
const val MUTE_VOICE_VALUE_OFF = "off"
