/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api

import android.annotation.SuppressLint
import android.content.Context
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotDimension
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotType
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia.Callback
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedHotSong
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedSong
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongParams
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeReverbParam
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomList
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel
import com.netease.yunxin.kit.karaokekit.impl.KaraokeKitImpl
import com.netease.yunxin.kit.karaokekit.impl.model.response.NEKaraokeDynamicToken

/**
 * NEKaraokeKit 核心类
 * @property localMember 本端成员
 * @property allMemberList 所有成员（包括本端）
 * @property isInitialized 是否初始化
 * @property isLoggedIn 是否登录
 */
interface NEKaraokeKit {
    companion object {
        /**
         * NEKaraokeKit实例
         */
        @SuppressLint("StaticFieldLeak")
        @JvmField
        val instance: NEKaraokeKit = KaraokeKitImpl()

        /**
         * 获取NEKaraokeKit实例
         * @return NEKaraokeKit实例
         */
        @JvmStatic
        fun getInstance(): NEKaraokeKit = instance
    }

    /**
     * 本端成员信息 [NEKaraokeMember]
     * 加入房间后获取
     */
    val localMember: NEKaraokeMember?

    /**
     * 所有成员（包括本端）
     * 加入房间后获取
     */
    val allMemberList: List<NEKaraokeMember>

    /**
     * NEKaraokeKit 初始化
     *
     * @param context 上下文
     * @param config 初始化配置 [NEKaraokeKitConfig]
     * @param callback 回调
     */
    fun initialize(
        context: Context,
        config: NEKaraokeKitConfig,
        callback: NEKaraokeCallback<Unit>? = null
    )

    /**
     * 初始化状态
     *
     * true 已初始化  false 未初始化
     */
    val isInitialized: Boolean

    /**
     * 是否已经登录
     */
    val isLoggedIn: Boolean

    /**
     * 添加登录状态监听
     * @param listener [NEKaraokeAuthListener] 状态参考[NEKaraokeAuthEvent]
     *
     */
    fun addAuthListener(listener: NEKaraokeAuthListener)

    /**
     * 移除登录状态监听
     * @param listener [NEKaraokeAuthListener] 状态参考[NEKaraokeAuthEvent]
     */
    fun removeAuthListener(listener: NEKaraokeAuthListener)

    /**
     * 注册房间监听
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param listener 回调
     *
     */
    fun addKaraokeListener(listener: NEKaraokeListener)

    /**
     * 移除房间监听
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param listener 回调
     *
     */
    fun removeKaraokeListener(listener: NEKaraokeListener)

    /**
     * 登录
     *
     * @param account NERoom登录账号
     * @param token NERoom token
     * @param callback 登录结果回调
     * <br>相关回调：登录成功后，会触发[NEKaraokeAuthListener.onKaraokeAuthEvent]回调
     */
    fun login(account: String, token: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 登出
     *
     * @param callback 登出接口回调
     * <br>相关回调：登出成功后，会触发[NEKaraokeAuthListener.onKaraokeAuthEvent]回调
     */
    fun logout(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 获取Karaoke房间列表
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param liveState 直播状态 (直播状态) [NEKaraokeLiveState]
     * @param pageNum 页码
     * @param pageSize 页大小,一页包含多少条
     * @param callback 房间列表回调
     *
     */
    fun getKaraokeRoomList(
        liveState: NEKaraokeLiveState,
        pageNum: Int,
        pageSize: Int,
        callback: NEKaraokeCallback<NEKaraokeRoomList>? = null
    )

    /**
     * 创建 karaoke 房间
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param params 创建房间参数配置[NECreateKaraokeParams]
     * @param options 进入房间时的必要配置[NECreateKaraokeOptions]
     * @param callback 创建房间回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun createRoom(
        params: NECreateKaraokeParams,
        options: NECreateKaraokeOptions,
        callback: NEKaraokeCallback<NEKaraokeRoomInfo>? = null
    )

    /**
     * 加入房间
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param params 加入房间参数配置[NEJoinKaraokeParams]
     * @param options 进入房间时的必要配置[NEJoinKaraokeOptions]
     * @param callback 进入房间回调
     * <br>相关回调：加入房间成功后，会触发[NEKaraokeListener.onMemberJoinRoom]回调
     */
    fun joinRoom(
        params: NEJoinKaraokeParams,
        options: NEJoinKaraokeOptions,
        callback: NEKaraokeCallback<NEKaraokeRoomInfo>? = null
    )

    /**
     * 离开房间
     * <br>使用前提：该方法仅在调用[joinRoom]方法加入房间成功后调用有效
     *@param callback 离开房间回调
     * <br>相关回调：离开房间成功后，会触发[NEKaraokeListener.onMemberLeaveRoom]回调
     */
    fun leaveRoom(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 结束房间 房主权限
     * @param callback 结束房间回调
     * <br>相关回调：离开房间成功后，会触发[NEKaraokeListener.onRoomEnded]回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun endRoom(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 获取麦位信息。
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 回调。
     *
     */
    fun getSeatInfo(callback: NEKaraokeCallback<NEKaraokeSeatInfo>? = null)

    /**
     * 获取麦位申请列表。按照申请时间正序排序，先申请的成员排在列表前面。
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 回调。
     *
     */
    fun getSeatRequestList(callback: NEKaraokeCallback<List<NEKaraokeSeatRequestItem>>? = null)

    /**
     * 申请上麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback  申请上麦回调
     * <br>相关回调：申请上麦后，房主会触发[NEKaraokeListener.onSeatRequestSubmitted]回调
     * <br>注意事项：只有非房主能执行该操作
     */
    fun requestSeat(callback: NEKaraokeCallback<Unit>? = null)

    /***
     * 取消申请上麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 取消上麦回调
     * <br>相关回调：取消申请上麦后，房主会触发[NEKaraokeListener.onSeatRequestCancelled]回调
     * <br>注意事项：只有非房主能执行该操作
     */
    fun cancelRequestSeat(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 同意上麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param account 被同意上麦的用户account
     * @param callback 同意上麦的回调
     * <br>相关回调：房主同意申请上麦后，申请麦位成员会触发[NEKaraokeListener.onSeatRequestApproved]回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun approveRequestSeat(account: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 拒绝上麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param account 被拒绝上麦的用户account
     * @param callback 拒绝上麦的回调
     * <br>相关回调：房主拒绝申请上麦后，申请麦位成员会触发[NEKaraokeListener.onSeatRequestRejected]回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun rejectRequestSeat(account: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 踢麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param account 被踢用户的uid
     * @param callback 被踢回调
     * <br>相关回调：房主踢麦后，被踢麦的成员会触发[NEKaraokeListener.onSeatKicked]回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun kickSeat(account: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 下麦
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 下麦回调
     */
    fun leaveSeat(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 发送聊天室消息
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param content 消息内容
     * @param callback 发送回调
     * <br>相关回调：调用改方法后，房间内其他成员都会触发[NEKaraokeListener.onReceiveTextMessage]回调
     */
    fun sendTextMessage(content: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 踢出房间内成员
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param account 被踢出成员account
     * @param callback 踢出回调
     * <br>注意事项：只有房主能执行该操作
     */
    fun kickMemberOut(account: String, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 关闭自己的麦克风
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param callback 关闭回调
     * <br>相关回调：调用该方法后，本端和其他上麦用户会触发[NEKaraokeListener.onMemberAudioMuteChanged]回调
     */
    fun muteMyAudio(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 打开自己的麦克风
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param callback 打开回调
     * <br>相关回调：调用该方法后，本端和其他上麦用户会触发[NEKaraokeListener.onMemberAudioMuteChanged]回调
     */
    fun unmuteMyAudio(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 开始播放歌曲
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param originPath 原唱本地地址
     * @param accompanyPath 伴唱地址
     * @param volume 播放音量
     * @param anchorAccount 主唱账号
     * @param chorusAccount 副唱账号,非合唱时
     * @param startTimeStamp 延迟播放时间 单位/毫秒
     * @param anchorAccount 是否是主唱 true 主唱，false副唱
     * @param mode k歌模式 [NEKaraokeSongMode]
     */
    fun playSong(
        originPath: String,
        accompanyPath: String,
        volume: Int,
        anchorAccount: String,
        chorusAccount: String?,
        startTimeStamp: Long,
        anchor: Boolean,
        mode: NEKaraokeSongMode,
        callback: NEKaraokeCallback<Unit>? = null
    )

    /**
     * 调节播放歌曲音量
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param volume 音量 范围0-100
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun adjustPlayingSongVolume(volume: Int): Int

    /**
     * 调节人声音量
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param volume 音量 范围0-100
     */
    fun adjustRecordingSignalVolume(volume: Int): Int

    /**
     * 变调
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param pitch  可以在 [0.5, 2.0] 范围内设置。
     * 取值越小，则音调越低。默认值为 1.0，表示不需要修改音调。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun setLocalVoicePitch(pitch: Double): Int

    /**
     * 设置 混响
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun setLocalVoiceReverbParam(param: NEKaraokeReverbParam): Int

    /**
     * 设置本地语音音效均衡，即自定义设置本地人声均衡波段的中心频率。
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @param bandGain 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun setLocalVoiceEqualization(bandFrequency: Int, bandGain: Int): Int

    /**
     * 开启耳返功能。
     * <br>开启耳返功能后，必须连接上耳机或耳麦，才能正常使用耳返功能。
     * @param volume 设置耳返音量，可设置为 0~100，默认为 100。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun enableEarBack(volume: Int): Int

    /**
     * 关闭耳返功能。
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun disableEarBack(): Int

    /**
     * 发起合唱
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，点歌人才能发起
     * @param orderId 点歌编号
     * @param callback 发起合唱结果回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun inviteChorus(orderId: Long, callback: NEKaraokeCallback<NEKaraokeSongModel>? = null)

    /**
     * 取消合唱
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，点歌人才能取消
     * @param [chorusId] 合唱id
     * @param callback 取消合唱结果回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun cancelInviteChorus(
        chorusId: String,
        callback: NEKaraokeCallback<NEKaraokeSongModel>? = null
    )

    /**
     * 加入合唱
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * @param chorusId 合唱id
     * @param callback 加入合唱结果回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun joinChorus(chorusId: String, callback: NEKaraokeCallback<NEKaraokeSongModel>? = null)

    /**
     * 副唱 伴奏等资源完成后调用，通知可以合唱了
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，只有副唱可用
     * @param chorusId 合唱id
     * @param callback 通知可以合唱的回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun chorusReady(chorusId: String, callback: NEKaraokeCallback<NEKaraokeSongModel>? = null)

    /**
     * 放弃演唱
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，只有当前点这首歌的人才能调用
     * @param orderId 点歌编号
     * @param callback 放弃演唱的回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun abandonSong(orderId: Long, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 开始演唱
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，房主、主唱、副唱可用
     * @param orderId 点歌编号,不传时chorusId必传
     * @param chorusId 合唱编号，存在时即可不传其他信息.不传时userUuid、orderId必传
     * @param ext 扩展字段
     * @param callback 回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun requestPlaySong(
        orderId: Long,
        chorusId: String?,
        ext: Map<String, Any>?,
        callback: NEKaraokeCallback<Unit>? = null
    )

    /**
     * 暂停歌曲
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，房主、主唱、副唱可用
     * @param callback 暂停歌曲的回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun requestPausePlayingSong(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 恢复播放
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，房主、主唱、副唱可用
     * @param callback 恢复播放的回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun requestResumePlayingSong(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 停止播放
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效，房主、主唱、副唱可用
     * @param callback 停止播放的回调
     * <br>相关回调：调用该方法后，房间内所有人会收到[NEKaraokeListener.onReceiveChorusMessage]回调
     */
    fun requestStopPlayingSong(callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 获取当前正在播放的信息
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 回调
     */
    fun requestPlayingSongInfo(callback: NEKaraokeCallback<NEKaraokeSongModel>? = null)

    /**
     * 指定播放位置
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功调用有效，独唱模式主唱可用
     * @param position 播放位置
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun setPlayingPosition(position: Long): Int

    /**
     * 切换原唱/伴音
     * <br>使用前提：该方法仅在调用[login]方法登录成功且上麦成功后调用有效
     * <br>串行模式下，只有主唱可用
     * <br>实时合唱模式下，主唱、副唱可用
     * @param enableAccompaniment 是否开启伴奏
     * @return 0：方法调用成功。其他：方法调用失败
     */
    fun switchAccompaniment(enableAccompaniment: Boolean): Int

    /**
     * 当前是否是原唱
     * <br>前提条件：
     * <br>串行模式，只有主唱可用
     * <br>实时合唱模式，主唱、副唱都可用
     * @return true 是原唱，false 非原唱
     */
    fun isOriginalSongPlaying(): Boolean

    /**
     * 调音台使用的音效Id
     * <br>前提条件：
     * <br>串行模式，只有主唱可用
     * <br>实时合唱模式，主唱、副唱都可用
     * @return 当前歌曲Id
     */
    fun currentSongIdForAudioEffect(): Int

    /**
     * 发送礼物
     * @param giftId 礼物id
     * @param giftCount 礼物数量
     * @param userUuids 接收礼物的用户uuid列表
     * @param callback 发送礼物的回调
     * <br>相关回调：发送礼物成功后，房间内所有人会收到[NEKaraokeListener.onReceiveBatchGift]回调
     */
    fun sendBatchGift(giftId: Int, giftCount: Int, userUuids: List<String>, callback: NEKaraokeCallback<Unit>? = null)

    /**
     * 获取已点歌曲列表
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param callback 回调
     */
    fun getOrderedSongs(callback: NEKaraokeCallback<List<NEKaraokeOrderSongResult>>)

    /**
     * 选择歌曲
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param songInfo 歌曲信息
     * @param callback 回调
     * <br>相关回调：点歌成功后，房间内所有人会收到[NEKaraokeListener.onSongOrdered]回调
     */
    fun orderSong(
        songInfo: NEKaraokeOrderSongParams,
        callback: NEKaraokeCallback<NEKaraokeOrderSongResult>
    )

    /**
     * 删除歌曲
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param orderId 歌曲点歌编号
     * @param callback 回调
     * <br>相关回调：房间内所有人会收到[NEKaraokeListener.onSongDeleted]回调
     */
    fun deleteSong(orderId: Long, callback: NEKaraokeCallback<Void>? = null)

    /**
     * 置顶歌曲
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param orderId 歌曲点歌编号
     * @param callback 回调
     * <br>相关回调：房间内所有人会收到[NEKaraokeListener.onSongTopped]回调
     */
    fun topSong(orderId: Long, callback: NEKaraokeCallback<Void>? = null)

    /**
     * 切歌
     * <br>使用前提：该方法仅在调用[login]方法登录成功后调用有效
     * @param orderId 歌曲点歌编号
     * @param callback 回调
     * <br>相关回调：房间内所有人会收到[NEKaraokeListener.onNextSong]回调
     */
    fun nextSong(orderId: Long, callback: NEKaraokeCallback<Void>? = null)

    /**
     * 刷新版权token。网络导致的失败会重试
     *
     * @param callback 执行回调
     */
    fun getSongDynamicTokenUntilSuccess(callback: NEKaraokeCallback<NEKaraokeDynamicToken>?)

    /**
     * 注册事件通知回调
     */
    fun setCopyrightedMediaEventHandler(eventHandler: NEKaraokeCopyrightedMediaEventHandler?)

    /**
     * 预加载 歌曲数据
     *
     * @param songId            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback          下载回调 [NEKaraokeCopyrightedMediaListener]
     */
    fun preloadSong(songId: String, channel: Int, callback: NEKaraokeCopyrightedMediaListener)

    /**
     * 清理本地所有缓存歌曲数据
     */
    fun clearSongCache()

    /**
     * 取消预加载 Song 数据
     *
     * @param songId            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     */
    fun cancelPreloadSong(songId: String, channel: Int)

    /**
     * 检测是否已预加载 Song 数据
     *
     * @param songID            音乐 ID
     * @param channel           版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun isSongPreloaded(songID: String, channel: Int): Boolean

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
    )

    /**
     * 原唱&伴奏：用于播放的本地文件路径
     *
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param songResType      资源类型 1：原唱，2：伴奏
     * @return
     */
    fun getSongURI(songId: String, channel: Int, songResType: SongResType): String?

    /**
     * 本地歌词
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun getLyric(songId: String, channel: Int): String?

    /**
     * 本地MIDI
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @return
     */
    fun getPitch(songId: String, channel: Int): String?

    /**
     * 加载歌词
     *
     * @param songId           音乐 ID
     * @param channel          版权渠道 1 云音乐 2 咪咕，默认不传则包含所有渠道
     * @param callback         加载歌词回调 [LyricCallback]
     */
    fun preloadSongLyric(songId: String, channel: Int, callback: LyricCallback)

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
    )

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
    )
}

/**
 * KaraokeKit配置
 * @property appKey Karaoke 服务的key
 * @property extras 额外参数
 */
data class NEKaraokeKitConfig(val appKey: String, val extras: Map<String, Any?> = mapOf())

/**
 * 创建房间参数
 *
 * @property title 房间名，支持中英文大小写、数字、特殊字符
 * @property nick 昵称
 * @property seatCount 麦位个数，默认8个,取值范围为1~20
 * @property extraData 扩展字段
 */
data class NECreateKaraokeParams(
    val title: String,
    val nick: String,
    val seatCount: Int = 8,
    val singMode: NEKaraokeSongMode = NEKaraokeSongMode.INTELLIGENCE,
    val extraData: String? = null
) {
    override fun toString(): String {
        return "NECreateKaraokeParams(title='$title', nick='$nick', seatCount=$seatCount, singMode=$singMode)"
    }
}

/**
 * 创建房间选项
 */
class NECreateKaraokeOptions

/**
 * 加入房间参数，支持中英文大小写、数字、特殊字符
 *
 * @property roomUuid 房间id
 * @property nick 昵称,最大字符长度64
 * @property role 角色，支持HOST、AUDIENCE
 * @property liveRecordId 直播id
 * @property extraData 扩展字段
 */
data class NEJoinKaraokeParams(
    val roomUuid: String,
    val nick: String,
    val role: NEKaraokeRole,
    val liveRecordId: Long,
    val extraData: String? = null
) {
    override fun toString(): String {
        return "NEJoinKaraokeParams(roomUuid='$roomUuid', nick='$nick', role='$role', liveRecordId=$liveRecordId)"
    }
}

/**
 * 加入房间选项
 */
class NEJoinKaraokeOptions

/**
 * 通用回调
 * @param T 数据
 */
interface NEKaraokeCallback<T> {
    /**
     * 成功回调
     * @param t 数据
     */
    fun onSuccess(t: T?)

    /**
     * 失败回调
     * @param code 错误码
     * @param msg 错误信息
     */
    fun onFailure(code: Int, msg: String?)
}

/**
 * 登录监听器
 */
interface NEKaraokeAuthListener {
    /**
     * 登录事件回调
     * @param evt 登录事件
     */
    fun onKaraokeAuthEvent(evt: NEKaraokeAuthEvent)
}
