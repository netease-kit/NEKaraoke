/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.Utils
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeGiftModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI
import com.netease.yunxin.kit.karaokekit.ui.R
import com.netease.yunxin.kit.karaokekit.ui.chatroom.ChatRoomMsgCreator
import com.netease.yunxin.kit.karaokekit.ui.helper.SeatHelper
import com.netease.yunxin.kit.karaokekit.ui.listener.MyKaraokeListener
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUtils
import com.netease.yunxin.kit.karaokekit.ui.utils.SeatUtils

class KaraokeRoomViewModel : ViewModel() {

    companion object {
        const val TAG = "KaraokeRoomViewModel"
        const val CURRENT_SEAT_STATE_IDLE = 0
        const val CURRENT_SEAT_STATE_APPLYING = 1
        const val CURRENT_SEAT_STATE_ON_SEAT = 2
    }

    var currentSeatState = MutableLiveData<Int>()

    val errorData = MutableLiveData<NEKaraokeEndReason>()

    val chatRoomMsgData = MutableLiveData<CharSequence>()

    val memberCountData = MutableLiveData<Int>()

    val userAccountData = MutableLiveData<Int>()

    val rewardData = MutableLiveData<NEKaraokeGiftModel>()

    val audioEffectFinishData = MutableLiveData<Int>()

    val audioMixingFinishData = MutableLiveData<Boolean>()

    val kickedOutData = MutableLiveData<Boolean>()

    val onSeatListData = MutableLiveData<List<OnSeatModel>>()

    val applySeatListData = MutableLiveData<List<ApplySeatModel>>()

    var liveInfoNE: NEKaraokeRoomInfo? = null

    val songListData = MutableLiveData<Boolean>()

    val switchSongData = MutableLiveData<NEKaraokeOrderSongModel>()

    val nextSongData = MutableLiveData<NEKaraokeOrderSongModel>()

    private var listener = object : MyKaraokeListener() {

        override fun onReceiveTextMessage(message: NEKaraokeChatTextMessage) {
            val content = message.text
            ALog.d(TAG, "onRecvRoomTextMsg :${message.fromNick}")
            chatRoomMsgData.postValue(ChatRoomMsgCreator.createText(NEKaraokeUI.getInstance().application, KaraokeUtils.isHost(message.fromUserUuid), message.fromNick, content))
        }

        override fun onReceiveGift(rewardMsg: NEKaraokeGiftModel) {
            rewardData.postValue(rewardMsg)
        }

        override fun onMemberAudioMuteChanged(
            member: NEKaraokeMember,
            mute: Boolean,
            operateBy: NEKaraokeMember?
        ) {
            val onSeatList = SeatHelper.getInstance().onSeatItems
            ALog.i(TAG, "onMemberAudioMuteChanged onSeatList = $onSeatList")
            onSeatList?.forEach {
                if (TextUtils.equals(it.account, member.account)) {
                    it.isMute = mute
                }
            }
            onSeatListData.postValue(onSeatList)
        }

        override fun onMemberJoinRoom(members: List<NEKaraokeMember>) {
            for (member in members) {
                ALog.d(TAG, "onMemberJoinRoom :${member.name}")
                if (!KaraokeUtils.isMySelf(member.account)) {
                    chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomEnter(member.name))
                }
            }
            updateRoomMemberCount()
        }

        override fun onMemberLeaveRoom(members: List<NEKaraokeMember>) {
            for (member in members) {
                ALog.d(TAG, "onUserLeft :$member.name")
                chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomExit(member.name))
            }
            updateRoomMemberCount()
            getSeatRequestList()
        }

        override fun onSeatRequestSubmitted(seatIndex: Int, account: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_APPLYING)
            }

            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    SeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_on_seat_request)
                )
            )
            getSeatRequestList()
        }

        override fun onSeatRequestApproved(seatIndex: Int, account: String, operateBy: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_ON_SEAT)
            }
            if (!KaraokeUtils.isHost(account)) {
                chatRoomMsgData.postValue(
                    ChatRoomMsgCreator.createSeatMessage(
                        SeatUtils.getMemberNick(account),
                        Utils.getApp().getString(R.string.karaoke_on_seat)
                    )
                )
            }
            getSeatRequestList()
        }

        override fun onSeatRequestCancelled(seatIndex: Int, account: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE)
            }
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    SeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_cancel_on_seat_request)
                )
            )
            getSeatRequestList()
        }

        override fun onSeatRequestRejected(seatIndex: Int, account: String, operateBy: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE)
            }
            getSeatRequestList()
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    SeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_on_seat_reject)
                )
            )
        }

        override fun onSeatLeave(seatIndex: Int, account: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE)
            }
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    SeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_down_seat)
                )
            )
        }

        override fun onSeatListChanged(seatItems: List<NEKaraokeSeatItem>) {
            ALog.i(TAG, "onSeatListChanged seatItems = $seatItems")
            onSeatListData.postValue(SeatUtils.transNESeatItem2OnSeatModel(seatItems))
        }

        override fun onSeatKicked(seatIndex: Int, account: String, operateBy: String) {
            if (TextUtils.equals(account, SeatUtils.getCurrentUuid())) {
                currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE)
            }
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    SeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_kickout_seat)
                )
            )
        }

        override fun onReceiveChorusMessage(
            actionType: NEKaraokeChorusActionType,
            songModel: NEKaraokeSongModel
        ) {
            when {
                actionType === NEKaraokeChorusActionType.INVITE -> { // / 邀请消息
                }
                actionType === NEKaraokeChorusActionType.AGREE_INVITE -> { // 同意邀请消息
                }
                actionType === NEKaraokeChorusActionType.READY -> { // / 准备完成可以开始了
                }
                actionType === NEKaraokeChorusActionType.START_SONG -> {
                    chatRoomMsgData.postValue(
                        ChatRoomMsgCreator.createSongMessage(
                            songModel.operator?.userName ?: "",
                            Utils.getApp().getString(R.string.song_start, songModel.songName)
                        )
                    )
                }
                actionType === NEKaraokeChorusActionType.CANCEL_INVITE -> { // 取消邀请
                }
                actionType === NEKaraokeChorusActionType.PAUSE_SONG -> { // / 歌曲暂停
                    chatRoomMsgData.postValue(
                        ChatRoomMsgCreator.createSongMessage(
                            songModel.operator?.userName ?: "",
                            Utils.getApp().getString(R.string.song_pause, songModel.songName)
                        )
                    )
                }
                actionType === NEKaraokeChorusActionType.RESUME_SONG -> { // / 恢复播放
                    chatRoomMsgData.postValue(
                        ChatRoomMsgCreator.createSongMessage(
                            songModel.operator?.userName ?: "",
                            Utils.getApp().getString(R.string.song_restart, songModel.songName)
                        )
                    )
                }
                actionType === NEKaraokeChorusActionType.ABANDON -> {
                }
                actionType === NEKaraokeChorusActionType.END_SONG -> { // / 结束
                }
            }
        }

        override fun onRoomEnded(reason: NEKaraokeEndReason) {
            errorData.postValue(reason)
        }

        override fun onRtcChannelError(code: Int) {
            if (code == 30015) {
                errorData.postValue(NEKaraokeEndReason.fromValue("END_OF_RTC"))
            }
        }

        override fun onSongListChanged() {
            songListData.postValue(true)
        }

        override fun onSongOrdered(song: NEKaraokeOrderSongModel) {
            ALog.i(TAG, "onSongOrder song = $song")
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.operator?.userName ?: "",
                    Utils.getApp().getString(R.string.song_ordered, song.songName)
                )
            )
        }

        override fun onSongDeleted(song: NEKaraokeOrderSongModel) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.operator?.userName ?: "",
                    Utils.getApp().getString(R.string.song_deleted, song.songName)
                )
            )
        }

        override fun onSongTopped(song: NEKaraokeOrderSongModel) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.operator?.userName ?: "",
                    Utils.getApp().getString(R.string.song_top, song.songName)
                )
            )
        }

        override fun onNextSong(song: NEKaraokeOrderSongModel, isManual: Boolean) {
            ALog.i(TAG, "onSongNext song = $song")
            if (isManual) {
                chatRoomMsgData.postValue(
                    ChatRoomMsgCreator.createSongMessage(
                        song.operator?.userName ?: "",
                        Utils.getApp().getString(R.string.song_switched, song.songName)
                    )
                )
                switchSongData.postValue(song)
            } else {
                nextSongData.postValue(song)
            }
        }
    }

    fun getSeatRequestList() {
        NEKaraokeKit.getInstance().getSeatRequestList(object : NEKaraokeCallback<List<NEKaraokeSeatRequestItem>> {
            override fun onSuccess(t: List<NEKaraokeSeatRequestItem>?) {
                val applySeatList = mutableListOf<ApplySeatModel>()
                t?.forEach {
                    applySeatList.add(ApplySeatModel(it.user, it.userName, it.icon, ApplySeatModel.SEAT_STATUS_PRE_ON_SEAT))
                }
                SeatHelper.getInstance().applySeatList = applySeatList
                applySeatListData.postValue(applySeatList)
            }

            override fun onFailure(code: Int, msg: String?) {
            }
        })
    }

    fun refreshLiveInfo(liveInfoNE: NEKaraokeRoomInfo) {
        this.liveInfoNE = liveInfoNE
    }

    private fun isAnchor(fromAccount: String): Boolean {
        return liveInfoNE?.anchor?.account == fromAccount
    }

    fun initDataOnJoinRoom() {
        NEKaraokeKit.getInstance().addKaraokeListener(listener)
        updateRoomMemberCount()
    }

    fun updateRoomMemberCount() {
        memberCountData.postUpdateValue {
            NEKaraokeKit.getInstance().allMemberList.size
        }
    }

    override fun onCleared() {
        super.onCleared()
        NEKaraokeKit.getInstance().removeKaraokeListener(listener)
    }
}

internal inline fun <T> MutableLiveData<T>.updateValue(computation: () -> T) {
    value = computation()
}

internal inline fun <T> MutableLiveData<T>.postUpdateValue(computation: () -> T) {
    postValue(computation())
}
