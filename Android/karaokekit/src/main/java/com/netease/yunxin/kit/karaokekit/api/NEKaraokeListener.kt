/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api

import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeBatchGiftModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType

/**
 * Karaoke 房间事件监听
 */
interface NEKaraokeListener {

    /**
     * 成员进入房间回调
     * @param members 成员列表
     */
    fun onMemberJoinRoom(members: List<NEKaraokeMember>)

    /**
     * 成员离开房间回调
     * @param members 成员列表
     */
    fun onMemberLeaveRoom(members: List<NEKaraokeMember>)

    /**
     * 成员进入聊天室回调
     * @param members 成员列表
     */
    fun onMemberJoinChatroom(members: List<NEKaraokeMember>)

    /**
     * 成员离开聊天室回调
     * @param members 成员列表
     */
    fun onMemberLeaveChatroom(members: List<NEKaraokeMember>)

    /**
     * 房间结束回调
     * @param reason 房间结束的原因
     * @see NEKaraokeEndReason
     */
    fun onRoomEnded(reason: NEKaraokeEndReason)

    /**
     * 房间连接状态已改变回调
     * @param state 当前的网络连接状态
     */
    fun onRoomConnectStateChanged(state: NERoomConnectType)

    /**
     * RTC频道错误回调
     * @param code RTC错误码
     */
    fun onRtcChannelError(code: Int)

    /**
     * 本端音频输出设备变更通知，如切换到扬声器、听筒、耳机等
     * @param device 音频输出类型
     */
    fun onAudioOutputDeviceChanged(device: NEKaraokeAudioOutputDevice)

    /**
     * 成员音频状态回调
     * @param member 成员
     * @param mute 是否静音。true 静音打开，false 静音关闭
     * @param operateBy 操作人
     */
    fun onMemberAudioMuteChanged(
        member: NEKaraokeMember,
        mute: Boolean,
        operateBy: NEKaraokeMember?
    )

    /**
     * 聊天室消息回调
     * @param message 文本消息
     */
    fun onReceiveTextMessage(message: NEKaraokeChatTextMessage)

    /**
     * 收到合唱邀请
     * @param actionType 合唱动作类型
     * @param songModel  歌曲
     * //
     */
    fun onReceiveChorusMessage(actionType: NEKaraokeChorusActionType, songModel: NEKaraokeSongModel)

    /**
     * 收到礼物
     * @param giftModel 礼物消息
     */
    fun onReceiveBatchGift(giftModel: NEKaraokeBatchGiftModel)

    /**
     * 成员[account]提交了位置为[seatIndex]的麦位申请。
     * @param seatIndex 麦位位置，**-1**表示未指定位置。
     * @param account 申请人的用户ID。
     */
    fun onSeatRequestSubmitted(seatIndex: Int, account: String) {}

    /**
     * 成员[account]取消了位置为[seatIndex]的麦位申请。
     * @param seatIndex 麦位位置，**-1**表示未指定位置。
     * @param account 申请人的用户ID。
     */
    fun onSeatRequestCancelled(seatIndex: Int, account: String) {}

    /**
     * 管理员通过了成员[account]的麦位申请，位置为[seatIndex]。
     * @param seatIndex 麦位位置。
     * @param account 申请人的用户ID。
     * @param operateBy 同意该申请的用户ID。
     */
    fun onSeatRequestApproved(seatIndex: Int, account: String, operateBy: String) {}

    /**
     * 管理员拒绝了成员[account]的麦位申请，位置为[seatIndex]。
     * @param seatIndex 麦位位置，**-1**表示未指定位置。
     * @param account 申请人的用户ID。
     * @param operateBy 拒绝该申请的用户ID。
     */
    fun onSeatRequestRejected(seatIndex: Int, account: String, operateBy: String) {}

    /**
     * 成员下麦，位置为[seatIndex]。
     * @param seatIndex 麦位位置。
     * @param account 下麦成员。
     */
    fun onSeatLeave(seatIndex: Int, account: String) {}

    /**
     * 成员[account]被[operateBy]从位置为[seatIndex]的麦位踢掉。
     * @param seatIndex 麦位位置。
     * @param account 成员。
     * @param operateBy 操作人。
     */
    fun onSeatKicked(seatIndex: Int, account: String, operateBy: String) {}

    /**
     * 麦位变更通知。
     * @param seatItems 麦位列表。
     */
    fun onSeatListChanged(seatItems: List<NEKaraokeSeatItem>) {}

    /**
     * 已点列表的更新
     *
     */
    fun onOrderedSongListChanged() {}

    /**
     * 点歌
     *
     * @param song 歌曲
     */
    fun onSongOrdered(song: NEKaraokeOrderSongModel) {}

    /**
     * 已点列表的删除
     *
     * @param song 歌曲
     */
    fun onSongDeleted(song: NEKaraokeOrderSongModel) {}

    /**
     * 已点列表的置顶
     *
     * @param song 歌曲
     */
    fun onSongTopped(song: NEKaraokeOrderSongModel) {}

    /**
     * 下一首歌
     *
     * @param song 歌曲
     */
    fun onNextSong(song: NEKaraokeOrderSongModel) {}

    /**
     * 当前歌曲播放进度
     * @param position 当前播放进度
     */
    fun onSongPlayingPosition(position: Long)

    /**
     *  Rtc audioFrame数据回调
     * @param frame 回调数据
     */
    fun onRecordingAudioFrame(frame: NEKaraokeAudioFrame)

    /**
     * 播放完成
     */
    fun onSongPlayingCompleted()
}
