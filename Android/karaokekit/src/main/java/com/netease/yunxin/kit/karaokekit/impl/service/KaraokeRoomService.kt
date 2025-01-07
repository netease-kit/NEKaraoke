/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.service

import android.text.TextUtils
import com.google.gson.JsonObject
import com.netease.lava.nertc.sdk.NERtc
import com.netease.lava.nertc.sdk.NERtcConstants
import com.netease.lava.nertc.sdk.NERtcEx
import com.netease.yunxin.kit.karaokekit.api.MUTE_VOICE_KEY
import com.netease.yunxin.kit.karaokekit.api.MUTE_VOICE_VALUE_OFF
import com.netease.yunxin.kit.karaokekit.api.MUTE_VOICE_VALUE_ON
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioOutputDevice
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeListener
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeReverbParam
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeGiftModel
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongModelResult
import com.netease.yunxin.kit.karaokekit.impl.utils.GsonUtils
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeLog
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeUtils
import com.netease.yunxin.kit.roomkit.api.NECallback
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomChatTextMessage
import com.netease.yunxin.kit.roomkit.api.NERoomContext
import com.netease.yunxin.kit.roomkit.api.NERoomEndReason
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomListener
import com.netease.yunxin.kit.roomkit.api.NERoomListenerAdapter
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.NERoomRtcStatsListener
import com.netease.yunxin.kit.roomkit.api.model.NEAudioOutputDevice
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType
import com.netease.yunxin.kit.roomkit.api.model.NERoomReverbParam
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioProfile
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioRecvStats
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioScenario
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcAudioSendStats
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcChannelProfile
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcClientRole
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcNetworkQualityInfo
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcStats
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcVideoRecvStats
import com.netease.yunxin.kit.roomkit.api.model.NERoomRtcVideoSendStats
import com.netease.yunxin.kit.roomkit.api.service.NEJoinRoomOptions
import com.netease.yunxin.kit.roomkit.api.service.NEJoinRoomParams
import com.netease.yunxin.kit.roomkit.api.service.NERoomService
import com.netease.yunxin.kit.roomkit.api.service.NESeatEventListener
import com.netease.yunxin.kit.roomkit.api.service.NESeatInfo
import com.netease.yunxin.kit.roomkit.api.service.NESeatItem
import com.netease.yunxin.kit.roomkit.api.service.NESeatItemStatus
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem
import com.netease.yunxin.kit.roomkit.impl.model.RoomCustomMessages
import java.util.Locale

internal class KaraokeRoomService : NERoomRtcStatsListener, NEPlayStateChangeCallback {
    private var TAG = "KaraokeRoomService"
    private var currentRoomContext: NERoomContext? = null
    private val listeners = ArrayList<NEKaraokeListener>()
    private var roomListener: NERoomListener? = null
    private var seatListener: NESeatEventListener? = null
    private var rtcStatsListener: NERoomRtcStatsListener? = null
    private var audioPlayService: NEAudioPlayService? = null
    private var reportRtt: Long = 0; // / 实时合唱时上报给服务端的rtt

    companion object {
        const val TYPE_ORDER_SONG = 1008 // 点歌
        const val TYPE_CANCEL_SONG = 1009 // 取消点歌
        const val TYPE_MANUAL_SWITCH_SONG = 1010 // 切歌
        const val TYPE_TOP_SONG = 1011 // 置顶
        const val TYPE_ORDERED_SONG_LIST_CHANGE = 1012 // 点歌列表变化
        const val TYPE_GIFT = 1005 // 礼物
    }

    fun getRtt(): Long {
        return reportRtt
    }

    fun getRoomUuid(): String? {
        return currentRoomContext?.roomUuid
    }

    /**
     * 获取采集播放延时间
     */
    fun getDelayTime(): Long {
        try {
            val timeStr =
                currentRoomContext?.rtcController?.getParameter("engine.audio.adm.delay", "")
            KaraokeLog.i(TAG, "getDelayTime======>$timeStr")
            return timeStr?.toLong() ?: 0
        } catch (e: Exception) {
            KaraokeLog.e(TAG, "getDelayTime error", e)
        }
        return 0
    }

    fun getAudioPlayService(): NEAudioPlayService? {
        return audioPlayService
    }

    fun getLocalMember(): NEKaraokeMember? {
        return currentRoomContext?.let { mapMember(it.localMember) }
    }

    fun getRemoteMembers(): List<NEKaraokeMember> {
        return currentRoomContext?.remoteMembers?.map {
            mapMember(it)
        } ?: emptyList()
    }

    private fun setAudioProfile() {
//        currentRoomContext?.rtcController?.setAudioProfile(
//            NERoomRtcAudioProfile.HIGH_QUALITY_STEREO,
//            NERoomRtcAudioScenario.MUSIC
//        )
//        currentRoomContext?.rtcController?.setChannelProfile(
//            NERoomRtcChannelProfile.liveBroadcasting
//        )
        NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.HIGHQUALITY_CHATROOM)

    }

    fun joinRoom(roomUuid: String, role: String, userName: String, callback: NECallback2<Unit>) {
        NERoomKit.getInstance().getService(NERoomService::class.java).joinRoom(
            NEJoinRoomParams(roomUuid = roomUuid, userName = userName, role = role),
            NEJoinRoomOptions(),
            object : NECallback2<NERoomContext>() {
                override fun onSuccess(data: NERoomContext?) {
                    currentRoomContext = data!!
                    setAudioProfile()
                    audioPlayService = NEAudioPlayService.getInstance()
                    audioPlayService?.init(currentRoomContext!!.roomUuid)
                    audioPlayService?.addPlayStateChangeCallback(this@KaraokeRoomService)
                    addRoomListener()
                    addNERoomRtcStatsListener()
                    addSeatListener()
                    KaraokeLog.d(TAG, "joinRoom roomUuid = $roomUuid success")
                    currentRoomContext?.rtcController?.setClientRole(NERoomRtcClientRole.AUDIENCE)

                    joinRtcChannel(object : NECallback2<Unit>() {
                        override fun onSuccess(data: Unit?) {
                            KaraokeLog.d(TAG, "joinRtcChannel roomUuid = $roomUuid success")
                            joinChatroomChannel(object : NECallback2<Unit>() {
                                override fun onSuccess(data: Unit?) {
                                    KaraokeLog.d(
                                        TAG,
                                        "joinChatroomChannel roomUuid = $roomUuid success"
                                    )
                                    callback.onSuccess(data)
                                }

                                override fun onError(code: Int, message: String?) {
                                    KaraokeLog.e(
                                        TAG,
                                        "joinChatroomChannel roomUuid = $roomUuid error code = $code message = $message"
                                    )

                                    leaveRtcChannel(object : NECallback2<Unit>() {
                                        override fun onSuccess(data: Unit?) {
                                            KaraokeLog.d(
                                                TAG,
                                                "leaveRtcChannel roomUuid = $roomUuid success"
                                            )
                                        }

                                        override fun onError(code: Int, message: String?) {
                                            KaraokeLog.e(
                                                TAG,
                                                "leaveRtcChannel failed roomUuid = $roomUuid error code = $code message = $message"
                                            )
                                        }
                                    })
                                    callback.onError(code, message)
                                }
                            })
                        }

                        override fun onError(code: Int, message: String?) {
                            KaraokeLog.e(
                                TAG,
                                "joinRtcChannel failed roomUuid = $roomUuid error code = $code message = $message"
                            )
                            callback.onError(code, message)
                        }
                    })
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(
                        TAG,
                        "joinRoom roomUuid = $roomUuid error code = $code message = $message"
                    )
                    callback.onResult(code, message, null)
                }
            }
        )
    }

    fun joinRtcChannel(callback: NECallback2<Unit>) {
        currentRoomContext?.rtcController?.joinRtcChannel(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.d(TAG, "joinRtcChannel success")
                callback.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "joinRtcChannel error code = $code message = $message")
                callback.onError(code, message)
            }
        })
    }

    fun leaveRtcChannel(callback: NECallback2<Unit>) {
        currentRoomContext?.rtcController?.leaveRtcChannel(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.d(TAG, "leaveRtcChannel success")
                callback.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "leaveRtcChannel error code = $code message = $message")
                callback.onError(code, message)
            }
        })
    }

    fun joinChatroomChannel(callback: NECallback2<Unit>) {
        currentRoomContext?.chatController?.joinChatroom(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.d(TAG, "joinChatroomChannel success")
                callback.onResult(NEErrorCode.SUCCESS, "", null)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "joinChatroomChannel error code = $code message = $message")
                callback.onError(code, message)
            }
        })
    }

    /**
     * 移除监听 --- 离开房间，结束房间
     */
    private fun removeListener() {
        roomListener?.apply { currentRoomContext?.removeRoomListener(roomListener!!) }
        rtcStatsListener?.apply { currentRoomContext?.removeRtcStatsListener(rtcStatsListener!!) }
        seatListener?.apply {
            currentRoomContext?.seatController?.removeSeatListener(seatListener!!)
        }
    }

    fun leaveRoom(callback: NECallback2<Unit>) {
        currentRoomContext?.leaveRoom(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                callback.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                callback.onError(code, message)
            }
        })
        audioPlayService?.removePlayStateChangeCallback(this@KaraokeRoomService)
        audioPlayService?.destroy()
        removeListener()
    }

    fun endRoom(callback: NECallback<Unit>) {
        audioPlayService?.removePlayStateChangeCallback(this@KaraokeRoomService)
        audioPlayService?.destroy()
        removeListener()
        currentRoomContext?.endRoom(
            true,
            object : NECallback2<Unit>() {
            }
        )
    }

    fun sendTextMessage(content: String, callback: NECallback2<NERoomChatMessage>) {
        currentRoomContext?.chatController?.sendBroadcastTextMessage(content, callback)
            ?: callback.onError(
                NEErrorCode.FAILURE,
                "roomContext is not exist ！"
            )
    }

    fun kickMemberOut(userUuid: String, callback: NECallback2<Unit>) {
        currentRoomContext?.kickMemberOut(userUuid, false, callback)
            ?: callback.onError(
                NEErrorCode.FAILURE,
                "roomContext is not exist ！"
            )
    }

    fun muteMyAudio(callback: NECallback2<Unit>) {
        val uuid = currentRoomContext?.localMember?.uuid
        uuid?.apply {
            currentRoomContext?.updateMemberProperty(
                this,
                MUTE_VOICE_KEY,
                MUTE_VOICE_VALUE_OFF,
                callback
            )
        } ?: callback.onError(NEErrorCode.FAILURE, "roomContext is not exist ！")
    }

    fun unmuteMyAudio(callback: NECallback2<Unit>) {
        val uuid = currentRoomContext?.localMember?.uuid
        uuid?.apply {
            currentRoomContext?.updateMemberProperty(
                this,
                MUTE_VOICE_KEY,
                MUTE_VOICE_VALUE_ON,
                callback
            )
        } ?: callback.onError(NEErrorCode.FAILURE, "roomContext is not exist ！")
    }

    fun enableEarBack(volume: Int): Int {
        return currentRoomContext?.rtcController?.enableEarBack(volume) ?: NEErrorCode.FAILURE
    }

    fun setLocalVoiceReverbParam(param: NEKaraokeReverbParam): Int {
        val result =
            currentRoomContext?.rtcController?.setLocalVoiceReverbParam(
                NERoomReverbParam().apply {
                    wetGain = param.wetGain
                    dryGain = param.dryGain
                    roomSize = param.roomSize
                    damping = param.damping
                    decayTime = param.decayTime
                    preDelay = param.preDelay
                }
            ) ?: NEErrorCode.FAILURE
        return result
    }

    fun setLocalVoiceEqualization(bandFrequency: Int, bandGain: Int): Int {
        return currentRoomContext?.rtcController?.setLocalVoiceEqualization(bandFrequency, bandGain)
            ?: NEErrorCode.FAILURE
    }

    fun setLocalVoicePitch(pitch: Double): Int {
        return currentRoomContext?.rtcController?.setLocalVoicePitch(pitch) ?: NEErrorCode.FAILURE
    }

    fun adjustRecordingSignalVolume(volume: Int): Int {
        return currentRoomContext?.rtcController?.adjustRecordingSignalVolume(volume)
            ?: NEErrorCode.FAILURE
    }

    fun adjustPlayMusicVolume(effectId: Int, volume: Int): Int {
        return currentRoomContext?.rtcController?.setEffectPlaybackVolume(effectId, volume)
            ?: NEErrorCode.FAILURE
    }

    fun disableEarBack(): Int {
        return currentRoomContext?.rtcController?.disableEarBack() ?: NEErrorCode.FAILURE
    }

    fun getSeatInfo(callback: NECallback<NESeatInfo>) {
        currentRoomContext?.seatController?.getSeatInfo(callback)
    }

    fun getSeatRequestList(callback: NECallback<List<NESeatRequestItem>>) {
        currentRoomContext?.seatController?.getSeatRequestList(callback)
    }

    fun applyOnSeat(callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.submitSeatRequest(callback)
    }

    fun cancelApplyOnSeat(callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.cancelSeatRequest(callback)
    }

    fun leaveSeat(callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.leaveSeat(callback)
    }

    fun approveSeatRequest(user: String, callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.approveSeatRequest(user, callback)
    }

    fun rejectSeatRequest(user: String, callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.rejectSeatRequest(user, callback)
    }

    fun kickSeat(user: String, callback: NECallback<Unit>) {
        currentRoomContext?.seatController?.kickSeat(user, callback)
    }

    fun removeListener(listener: NEKaraokeListener) {
        listeners.remove(listener)
    }

    fun addListener(listener: NEKaraokeListener) {
        listeners.add(listener)
    }

    private fun mapMember(member: NERoomMember): NEKaraokeMember {
        return NEKaraokeMember(
            member.uuid,
            member.name,
            member.role.name,
            KaraokeUtils.isAudioOn(member)
        )
    }

    private fun addRoomListener() {
        roomListener = object : NERoomListenerAdapter() {

            override fun onRtcChannelError(code: Int) {
                KaraokeLog.e(TAG, "onRtcChannelError code = $code")
                listeners.forEach {
                    it.onRtcChannelError(code)
                }
            }

            override fun onMemberPropertiesChanged(
                member: NERoomMember,
                properties: Map<String, String>
            ) {
                if (properties.containsKey(MUTE_VOICE_KEY)) {
                    val uuid = getLocalMember()?.account
                    val voiceValue = properties[MUTE_VOICE_KEY]
                    if (voiceValue == MUTE_VOICE_VALUE_ON || voiceValue == MUTE_VOICE_VALUE_OFF) {
                        val mute = voiceValue == MUTE_VOICE_VALUE_OFF
                        if (member.uuid == uuid) {
                            currentRoomContext?.rtcController?.setRecordDeviceMute(mute)
                        }
                        val ktvMember = mapMember(member)
                        listeners.forEach {
                            it.onMemberAudioMuteChanged(ktvMember, mute, ktvMember)
                        }
                    }
                }
            }

            override fun onMemberPropertiesDeleted(
                member: NERoomMember,
                properties: Map<String, String>
            ) {
            }

            override fun onMemberJoinRoom(members: List<NERoomMember>) {
                val memberList = members.map {
                    mapMember(it)
                }
                listeners.forEach {
                    it.onMemberJoinRoom(memberList)
                }
            }

            override fun onMemberLeaveRoom(members: List<NERoomMember>) {
                val memberList = members.map {
                    mapMember(it)
                }
                listeners.forEach {
                    it.onMemberLeaveRoom(memberList)
                }
            }

            override fun onMemberJoinChatroom(members: List<NERoomMember>) {
                val memberList = members.map {
                    mapMember(it)
                }
                listeners.forEach {
                    it.onMemberJoinChatroom(memberList)
                }
            }

            override fun onMemberLeaveChatroom(members: List<NERoomMember>) {
                val memberList = members.map {
                    mapMember(it)
                }
                listeners.forEach {
                    it.onMemberLeaveChatroom(memberList)
                }
            }

            override fun onRoomEnded(reason: NERoomEndReason) {
                val karaokeReason =
                    NEKaraokeEndReason.fromValue(reason.name.uppercase(Locale.getDefault()))
                listeners.forEach {
                    it.onRoomEnded(karaokeReason)
                }
            }

            override fun onRoomConnectStateChanged(state: NERoomConnectType) {
                listeners.forEach {
                    it.onRoomConnectStateChanged(state)
                }
            }

            override fun onAudioEffectFinished(effectId: Int) {
                listeners.forEach {
                    it.onSongPlayingCompleted()
                }
            }

            override fun onRtcAudioOutputDeviceChanged(device: NEAudioOutputDevice) {
                val outputDevice =
                    NEKaraokeAudioOutputDevice.fromValue(device.name.uppercase(Locale.getDefault()))
                listeners.forEach {
                    it.onAudioOutputDeviceChanged(outputDevice)
                }
            }

            override fun onMemberAudioMuteChanged(
                member: NERoomMember,
                mute: Boolean,
                operateBy: NERoomMember?
            ) {
                val operateMember = operateBy?.let { mapMember(it) }
                listeners.forEach {
                    it.onMemberAudioMuteChanged(mapMember(member), mute, operateMember)
                }
            }

            override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
                messages.forEach {
                    if (it is NERoomChatTextMessage) {
                        val textMessage = NEKaraokeChatTextMessage(
                            it.fromUserUuid,
                            it.fromNick,
                            it.toUserUuidList,
                            it.time,
                            it.text
                        )
                        listeners.forEach { listener ->
                            listener.onReceiveTextMessage(textMessage)
                        }
                    } else if (it is RoomCustomMessages) {
                        KaraokeLog.i(
                            TAG,
                            "onReceiveChatroomMessages  RoomCustomMessages  attachStr  :${it.attachStr}"
                        )
                        try {
                            when (getType(it.attachStr)) {
                                in NEKaraokeChorusActionType.CANCEL_INVITE.actionType..NEKaraokeChorusActionType.NEXT.actionType -> {
                                    handleKTVProtocol(
                                        getType(it.attachStr),
                                        getData(it.attachStr)
                                    )
                                }

                                in TYPE_ORDER_SONG..TYPE_ORDERED_SONG_LIST_CHANGE -> {
                                    handleOrderSongProtocol(
                                        getType(it.attachStr),
                                        getData(it.attachStr)
                                    )
                                }

                                TYPE_GIFT -> {
                                    handleGiftProtocol(getType(it.attachStr), getData(it.attachStr))
                                }
                            }
                        } catch (e: Exception) {
                            KaraokeLog.e(TAG, "handle protocol error ${e.message}")
                        }
                    }
                }
            }

            override fun onChatroomMessageAttachmentProgress(messageUuid: String, transferred: Long, total: Long) {
            }

            override fun onAudioMixingStateChanged(reason: Int) {
            }
        }
        currentRoomContext?.addRoomListener(roomListener!!)
    }

    fun handleKTVProtocol(type: Int, data: String?) {
        KaraokeLog.i(
            TAG,
            "handleKTVProtocol customAttachment type: $type data: $data"
        )
        val songModelResult =
            fromJson(
                data,
                KaraokeSongModelResult::class.java
            )

        if (type == NEKaraokeChorusActionType.START_SONG.actionType) {
            songModelResult?.let {
                audioPlayService?.startSong(it.singInfo?.orderId ?: 0)
            }
        } else if (type == NEKaraokeChorusActionType.PAUSE_SONG.actionType) {
            songModelResult?.let {
                audioPlayService?.pauseSong(it)
            }
        } else if (type == NEKaraokeChorusActionType.RESUME_SONG.actionType) {
            songModelResult?.let {
                audioPlayService?.resumeSong(it)
            }
        } else if (type == NEKaraokeChorusActionType.ABANDON.actionType) {
            songModelResult?.let {
                audioPlayService?.stopSong(it)
            }
        } else if (type == NEKaraokeChorusActionType.NEXT.actionType) {
            songModelResult?.let {
                audioPlayService?.nextSong(it)
            }
        } else if (type == NEKaraokeChorusActionType.END_SONG.actionType) {
            songModelResult?.let {
                audioPlayService?.stopSong(it)
            }
        }

        listeners.forEach { listener ->
            songModelResult?.let {
                listener.onReceiveChorusMessage(
                    NEKaraokeChorusActionType.fromValue(
                        type
                    ),
                    if (it.singInfo != null) {
                        KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(it.singInfo, it.operator)
                    } else {
                        KaraokeUtils.karaokeChorusInfo2NEKaraokeSongModel(
                            it.chorusInfo!!,
                            it.operator
                        )
                    }
                )
            }
        }
    }

    fun handleOrderSongProtocol(type: Int, data: String?) {
        KaraokeLog.i(
            TAG,
            "handleOrderSongProtocol customAttachment type:$type data:$data"
        )
        val orderSongData = fromJson(
            data,
            NEKaraokeOrderSongModel::class.java
        )

        when (type) {
            TYPE_ORDER_SONG -> {
                orderSongData?.run {
                    listeners.forEach { listener ->
                        listener.onSongOrdered(this)
                    }
                }
            }

            TYPE_CANCEL_SONG -> {
                orderSongData?.run {
                    listeners.forEach { listener ->
                        listener.onSongDeleted(this)
                    }
                }
            }

            TYPE_MANUAL_SWITCH_SONG -> {
                orderSongData?.run {
                    listeners.forEach { listener ->
                        listener.onNextSong(this)
                    }
                }
            }

            TYPE_TOP_SONG -> {
                orderSongData?.run {
                    listeners.forEach { listener ->
                        listener.onSongTopped(this)
                    }
                }
            }

            TYPE_ORDERED_SONG_LIST_CHANGE -> {
                listeners.forEach { listener ->
                    listener.onOrderedSongListChanged()
                }
            }
        }
    }

    fun handleGiftProtocol(type: Int, data: String?) {
        KaraokeLog.i(
            TAG,
            "handleGiftProtocol customAttachment:$type data:$data"
        )
        val result2 = fromJson(
            data,
            KaraokeGiftModel::class.java
        )
        result2?.let {
            listeners.forEach { listener ->
                listener.onReceiveBatchGift(
                    KaraokeUtils.karaokeGiftModel2NEKaraokeGiftModel(it)
                )
            }
        }
    }

    private fun <T> fromJson(data: String?, zClass: Class<T>): T? {
        return try {
            GsonUtils.fromJson(data, zClass)
        } catch (e: Throwable) {
            KaraokeLog.e(TAG, "fromJson fail $data, zClass=$zClass, error=${e.message}")
            null
        }
    }

    private fun getType(json: String): Int {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["type"]?.asInt ?: 0
    }

    private fun getData(json: String): String? {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["data"]?.toString()
    }

    private fun addNERoomRtcStatsListener() {
        currentRoomContext?.addRtcStatsListener(this)
    }

    private fun addSeatListener() {
        seatListener = object : NESeatEventListener() {
            override fun onSeatInvitationReceived(seatIndex: Int, user: String, operateBy: String) {
                KaraokeLog.d(
                    TAG,
                    "onSeatInvitationReceived seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
            }

            override fun onSeatInvitationAccepted(seatIndex: Int, user: String, isAutoAgree: Boolean) {
                KaraokeLog.d(
                    TAG,
                    "onSeatInvitationAccepted seatIndex = $seatIndex user = $user isAutoAgree = $isAutoAgree"
                )
            }

            override fun onSeatRequestApproved(seatIndex: Int, user: String, operateBy: String, isAutoAgree: Boolean) {
                KaraokeLog.d(
                    TAG,
                    "onSeatRequestApproved seatIndex = $seatIndex user = $user operateBy = $operateBy isAutoAgree = $isAutoAgree"
                )
                listeners.forEach {
                    it.onSeatRequestApproved(seatIndex, user, operateBy)
                }
            }

            override fun onSeatRequestCancelled(seatIndex: Int, user: String) {
                KaraokeLog.d(TAG, "onSeatRequestCancelled seatIndex = $seatIndex user = $user")
                listeners.forEach {
                    it.onSeatRequestCancelled(seatIndex, user)
                }
            }

            override fun onSeatInvitationCancelled(
                seatIndex: Int,
                user: String,
                operateBy: String
            ) {
                KaraokeLog.d(
                    TAG,
                    "onSeatInvitationCancelled seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
            }

            override fun onSeatInvitationRejected(seatIndex: Int, user: String) {
                KaraokeLog.d(TAG, "onSeatInvitationRejected seatIndex = $seatIndex user = $user")
            }

            override fun onSeatKicked(seatIndex: Int, user: String, operateBy: String) {
                KaraokeLog.d(
                    TAG,
                    "onSeatKicked seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatKicked(seatIndex, user, operateBy)
                }
            }

            override fun onSeatLeave(seatIndex: Int, user: String) {
                KaraokeLog.d(TAG, "onSeatInvitationRejected seatIndex = $seatIndex user = $user")
                listeners.forEach {
                    it.onSeatLeave(seatIndex, user)
                }
            }

            override fun onSeatListChanged(seatItems: List<NESeatItem>) {
                var isCurrentOnSeat = isCurrentOnSeat(seatItems)
                if (isCurrentOnSeat) {
                    currentRoomContext?.rtcController?.unmuteMyAudio()
                }
                currentRoomContext?.rtcController?.setClientRole(
                    if (isCurrentOnSeat) NERoomRtcClientRole.BROADCASTER else NERoomRtcClientRole.AUDIENCE
                )

                KaraokeLog.d(TAG, "onSeatListChanged seatItems = $seatItems")
                listeners.forEach {
                    it.onSeatListChanged(
                        seatItems.map { it2 -> KaraokeUtils.karaokeSeatItem2NEKaraokeSeatItem(it2) }
                    )
                }
            }

            override fun onSeatManagerAdded(managers: List<String>) {
                KaraokeLog.d(TAG, "onSeatManagerAdded managers = $managers")
            }

            override fun onSeatManagerRemoved(managers: List<String>) {
                KaraokeLog.d(TAG, "onSeatManagerRemoved managers = $managers")
            }

            override fun onSeatRequestRejected(seatIndex: Int, user: String, operateBy: String) {
                KaraokeLog.d(
                    TAG,
                    "onSeatRequestRejected seatIndex = $seatIndex user = $user operateBy = $operateBy"
                )
                listeners.forEach {
                    it.onSeatRequestRejected(seatIndex, user, operateBy)
                }
            }

            override fun onSeatRequestSubmitted(seatIndex: Int, user: String) {
                KaraokeLog.d(TAG, "onSeatRequestSubmitted seatIndex = $seatIndex user = $user")
                listeners.forEach {
                    it.onSeatRequestSubmitted(seatIndex, user)
                }
            }
        }

        currentRoomContext?.seatController?.addSeatListener(seatListener!!)
    }

    private fun isCurrentOnSeat(seatItems: List<NESeatItem>): Boolean {
        var currentOnSeat = false
        seatItems.forEach {
            if (it.status == NESeatItemStatus.TAKEN &&
                TextUtils.equals(currentRoomContext?.localMember?.uuid, it.user)
            ) {
                currentOnSeat = true
            }
        }
        return currentOnSeat
    }

    override fun onRtcStats(stats: NERoomRtcStats) {
        val downRtt = stats.downRtt
        val upRtt = stats.upRtt
        if (downRtt > 0 && upRtt > 0) {
            reportRtt = convertRtt((downRtt + upRtt) / 2)
        } else if (downRtt == 0L && upRtt > 0) {
            reportRtt = convertRtt(upRtt)
        } else if (downRtt > 0 && upRtt == 0L) {
            reportRtt = convertRtt(downRtt)
        }
    }

    private fun convertRtt(originRtt: Long): Long {
        return (originRtt / 2) + 15
    }

    override fun onLocalAudioStats(stats: NERoomRtcAudioSendStats) {
    }

    override fun onRemoteAudioStats(statsArray: Array<NERoomRtcAudioRecvStats>) {
    }

    override fun onLocalVideoStats(stats: NERoomRtcVideoSendStats) {
    }

    override fun onRemoteVideoStats(statsArray: Array<NERoomRtcVideoRecvStats>) {
    }

    override fun onNetworkQuality(statsArray: Array<NERoomRtcNetworkQualityInfo>) {
    }

    override fun onSongPlayPosition(position: Long) {
        listeners.forEach {
            it.onSongPlayingPosition(position)
        }
    }

    override fun onRecordAudioFrame(frame: NEKaraokeAudioFrame) {
        listeners.forEach {
            it.onRecordingAudioFrame(frame)
        }
    }

    override fun onSongPlayCompleted() {
        listeners.forEach {
            it.onSongPlayingCompleted()
        }
    }
}
