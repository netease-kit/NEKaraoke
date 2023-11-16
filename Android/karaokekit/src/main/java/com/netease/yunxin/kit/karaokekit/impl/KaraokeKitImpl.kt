/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl

import android.content.Context
import android.media.AudioManager
import android.text.TextUtils
import com.netease.yunxin.kit.common.network.ContextRegistry
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotDimension
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedHotType
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedHotSong
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedSong
import com.netease.yunxin.kit.copyrightedmedia.impl.NECopyrightedEventHandler
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeOptions
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeParams
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeOptions
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeParams
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAuthEvent
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAuthListener
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaEventHandler
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaListener
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeErrorCode
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKitConfig
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeListener
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeLiveState
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongParams
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeReverbParam
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomList
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatInfo
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel
import com.netease.yunxin.kit.karaokekit.impl.model.KaraokeRoomInfo
import com.netease.yunxin.kit.karaokekit.impl.model.StartKaraokeParam
import com.netease.yunxin.kit.karaokekit.impl.model.request.CancelChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.DeviceParam
import com.netease.yunxin.kit.karaokekit.impl.model.request.InviteChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.JoinChorusRequest
import com.netease.yunxin.kit.karaokekit.impl.model.request.StartSingRequest
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeRoomList
import com.netease.yunxin.kit.karaokekit.impl.model.response.KaraokeSongInfo
import com.netease.yunxin.kit.karaokekit.impl.model.response.NEKaraokeDynamicToken
import com.netease.yunxin.kit.karaokekit.impl.repository.KaraokeRepository
import com.netease.yunxin.kit.karaokekit.impl.service.CopyrightedMediaService
import com.netease.yunxin.kit.karaokekit.impl.service.KaraokeRoomService
import com.netease.yunxin.kit.karaokekit.impl.service.KaraokeService
import com.netease.yunxin.kit.karaokekit.impl.service.KaraokeServiceImpl
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeLog
import com.netease.yunxin.kit.karaokekit.impl.utils.KaraokeUtils
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil
import com.netease.yunxin.kit.karaokekit.impl.utils.TimerTaskUtil
import com.netease.yunxin.kit.roomkit.api.NECallback2
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomKitOptions
import com.netease.yunxin.kit.roomkit.api.service.NEAuthEvent
import com.netease.yunxin.kit.roomkit.api.service.NEAuthListener
import com.netease.yunxin.kit.roomkit.api.service.NEAuthService
import com.netease.yunxin.kit.roomkit.api.service.NESeatInfo
import com.netease.yunxin.kit.roomkit.api.service.NESeatRequestItem
import com.netease.yunxin.kit.roomkit.impl.repository.ServerConfig
import com.netease.yunxin.kit.roomkit.impl.utils.CoroutineRunner
import java.util.*
import kotlin.math.pow

internal class KaraokeKitImpl : NEKaraokeKit, CoroutineRunner() {

    private val karaokeMode = 3 // 房间类型（1：互动直播 2：语聊房 3：Karaoke）
    private val karaokeService: KaraokeService by lazy { KaraokeServiceImpl }
    private val myRoomService = KaraokeRoomService()
    private val copyrightedMediaService = CopyrightedMediaService()
    private var karaokeInfo: KaraokeRoomInfo? = null
    private val authListeners: ArrayList<NEKaraokeAuthListener> by lazy { ArrayList() }
    private lateinit var context: Context
    private var liveRecordId: Long = 0
    private var hasLogin: Boolean = false
    private val refreshTokenTaskId = 430
    private val tokenWillExpiredTaskId = 431
    private val refreshTokenRetryTimes = 3
    private val aheadTimeRefreshToken = 180

    companion object {
        private const val TAG = "NEKaraokeKit"
        private const val SERVER_URL_KEY = "serverUrl"
        private const val HTTP_PREFIX = "http"
        private const val BASE_URL_KEY = "baseUrl"
        private const val TEST_URL_VALUE = "test"
    }

    override val localMember: NEKaraokeMember?
        get() = myRoomService.getLocalMember()

    override val allMemberList: List<NEKaraokeMember>
        get() {
            return myRoomService.getLocalMember()?.let {
                val list = mutableListOf(it)
                list.addAll(myRoomService.getRemoteMembers())
                list
            } ?: emptyList()
        }

    private val pauseSong: Int = 0
    private val resumeSong: Int = 1
    private val finishSong: Int = 2
    private var config: NEKaraokeKitConfig? = null
    private var baseUrl: String = ""
    override fun initialize(
        context: Context,
        config: NEKaraokeKitConfig,
        callback: NEKaraokeCallback<Unit>?
    ) {
        KaraokeLog.logApi("initialize")
        this.context = context
        this.config = config
        ContextRegistry.context = context
        ScreenUtil.init(context)
        var realRoomServerUrl = ""
        val realExtras = HashMap<String, Any?>()
        realExtras.putAll(config.extras)
        if (config.extras[SERVER_URL_KEY] != null) {
            val serverUrl: String = config.extras[SERVER_URL_KEY] as String
            baseUrl = config.extras[BASE_URL_KEY] as String
            KaraokeLog.i(TAG, "serverUrl:$serverUrl")
            KaraokeLog.i(TAG, "baseUrl:$baseUrl")
            if (!TextUtils.isEmpty(serverUrl)) {
                when {
                    TEST_URL_VALUE == serverUrl -> {
                        realRoomServerUrl = serverUrl
                    }
                    serverUrl.startsWith(HTTP_PREFIX) -> {
                        realRoomServerUrl = serverUrl
                    }
                }
            }
        }
        realExtras[SERVER_URL_KEY] = realRoomServerUrl
        val serverConfig =
            ServerConfig.selectServer(config.appKey, realRoomServerUrl)
        KaraokeRepository.serverConfig = serverConfig
        karaokeService.initialize(context, baseUrl)
        karaokeService.addHeader("appkey", config.appKey)
        NERoomKit.getInstance().getService(NEAuthService::class.java).addAuthListener(object :
            NEAuthListener {
            override fun onAuthEvent(evt: NEAuthEvent) {
                KaraokeLog.i(TAG, "onAuthEvent evt = $evt")
                hasLogin = evt == NEAuthEvent.LOGGED_IN
                authListeners.forEach {
                    it.onKaraokeAuthEvent(
                        NEKaraokeAuthEvent.fromValue(evt.name.uppercase(Locale.getDefault()))
                    )
                }
            }
        })
        launch {
            karaokeService.httpErrorEvents.collect { evt ->
                if (evt.code == NEErrorCode.UNAUTHORIZED ||
                    evt.code == NEErrorCode.INCORRECT_TOKEN
                ) {
                    authListeners.forEach {
                        it.onKaraokeAuthEvent(NEKaraokeAuthEvent.UNAUTHORIZED)
                    }
                } else if (evt.code == NEErrorCode.TOKEN_EXPIRED) {
                    authListeners.forEach {
                        it.onKaraokeAuthEvent(NEKaraokeAuthEvent.ACCOUNT_TOKEN_ERROR)
                    }
                }
            }
        }
        if (NERoomKit.getInstance().isInitialized) {
            callback?.onSuccess(Unit)
        } else {
            NERoomKit.getInstance()
                .initialize(
                    context,
                    NERoomKitOptions(appKey = config.appKey, extras = realExtras)
                ) { code, message, _ ->
                    if (code == NEErrorCode.SUCCESS) {
                        callback?.onSuccess(Unit)
                    } else {
                        callback?.onFailure(code, message)
                    }
                }
        }
    }

    override val isInitialized: Boolean
        get() = NERoomKit.getInstance().isInitialized

    override val isLoggedIn: Boolean
        get() = hasLogin

    override fun login(account: String, token: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("login: account = $account")
        if (hasLogin) {
            KaraokeLog.i(TAG, "login but KaraokeKit hasLogin = true")
            karaokeService.addHeader("user", account)
            karaokeService.addHeader("token", token)
            initSongServiceAfterLogin(account)
            callback?.onSuccess(Unit)
        } else {
            if (NERoomKit.getInstance().getService(NEAuthService::class.java).isLoggedIn) {
                KaraokeLog.i(TAG, "login but RoomKit isLoggedIn = true")
                karaokeService.addHeader("user", account)
                karaokeService.addHeader("token", token)
                initSongServiceAfterLogin(account)
                hasLogin = true
                callback?.onSuccess(Unit)
            } else {
                KaraokeLog.i(TAG, "login RoomKit")
                NERoomKit.getInstance().getService(NEAuthService::class.java).login(
                    account,
                    token,
                    object : NECallback2<Unit>() {
                        override fun onSuccess(data: Unit?) {
                            KaraokeLog.i(TAG, "login success")
                            karaokeService.addHeader("user", account)
                            karaokeService.addHeader("token", token)
                            initSongServiceAfterLogin(account)
                            hasLogin = true
                            callback?.onSuccess(data)
                        }

                        override fun onError(code: Int, message: String?) {
                            KaraokeLog.e(TAG, "login error: code=$code, message=$message")
                            callback?.onFailure(code, message)
                            hasLogin = false
                        }
                    }
                )
            }
        }
    }

    override fun logout(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("logout")
        hasLogin = false
        TimerTaskUtil.release()
        NERoomKit.getInstance().getService(NEAuthService::class.java)
            .logout(object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "logout success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "logout error: code=$code, message=$message")
                    callback?.onFailure(code, message)
                }
            })
    }

    override fun addAuthListener(listener: NEKaraokeAuthListener) {
        KaraokeLog.logApi("addAuthListener: listener=$listener")
        authListeners.add(listener)
    }

    override fun removeAuthListener(listener: NEKaraokeAuthListener) {
        KaraokeLog.logApi("removeAuthListener: listener=$listener")
        authListeners.remove(listener)
    }

    override fun getKaraokeRoomList(
        liveState: NEKaraokeLiveState,
        pageNum: Int,
        pageSize: Int,
        callback: NEKaraokeCallback<NEKaraokeRoomList>?
    ) {
        KaraokeLog.logApi(
            "getKaraokeRoomList: liveState=$liveState, pageNum=$pageNum, pageSize=$pageSize"
        )
        karaokeService.getKaraokeRoomList(
            karaokeMode, // 房间类型（1：互动直播 2：语聊房 3：Karaoke）
            liveState.value,
            pageNum,
            pageSize,
            object :
                NetRequestCallback<KaraokeRoomList> {
                override fun error(code: Int, msg: String?) {
                    KaraokeLog.e(TAG, "getKaraokeRoomList error: code = $code msg = $msg")
                    callback?.onFailure(code, msg)
                }

                override fun success(info: KaraokeRoomList?) {
                    KaraokeLog.d(TAG, "startKaraoke success info = $info")
                    callback?.onSuccess(
                        info?.let {
                            KaraokeUtils.karaokeRoomList2NEKaraokeRoomList(
                                it
                            )
                        }
                    )
                }
            }
        )
    }

    private fun isTest(): Boolean {
        return TextUtils.equals((config?.extras?.get("serverUrl") as String?) ?: "", "test")
    }

    override fun createRoom(
        params: NECreateKaraokeParams,
        options: NECreateKaraokeOptions,
        callback: NEKaraokeCallback<NEKaraokeRoomInfo>?
    ) {
        KaraokeLog.logApi("createRoom: params=$params")
        val createRoomParam = StartKaraokeParam(
            params.title,
            params.nick,
            liveType = karaokeMode,
            configId = if (isTest()) 72 else 400,
            cover = "",
            seatCount = params.seatCount,
            singMode = params.singMode.value
        )
        karaokeService.startKaraoke(
            createRoomParam,
            object : NetRequestCallback<KaraokeRoomInfo> {
                override fun error(code: Int, msg: String?) {
                    KaraokeLog.e(TAG, "createRoom error: code=$code message=$msg")
                    callback?.onFailure(code, msg)
                }

                override fun success(info: KaraokeRoomInfo?) {
                    karaokeInfo = info
                    KaraokeLog.d(TAG, "startKaraoke success info = $info")
                    callback?.onSuccess(
                        info?.let {
                            KaraokeUtils.karaokeRoomInfo2NEKaraokeRoomInfo(
                                it
                            )
                        }
                    )
                }
            }
        )
    }

    override fun joinRoom(
        params: NEJoinKaraokeParams,
        options: NEJoinKaraokeOptions,
        callback: NEKaraokeCallback<NEKaraokeRoomInfo>?
    ) {
        KaraokeLog.logApi("joinRoom: params=$params")
        liveRecordId = params.liveRecordId
        myRoomService.joinRoom(
            params.roomUuid,
            params.role.value,
            userName = params.nick,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "joinRoom success")

                    karaokeService.getRoomInfo(
                        params.liveRecordId,
                        object : NetRequestCallback<KaraokeRoomInfo> {
                            override fun success(info: KaraokeRoomInfo?) {
                                KaraokeLog.d(
                                    TAG,
                                    "joinRoom  getRoomInfo success"
                                )
                                callback?.onSuccess(
                                    info?.let {
                                        KaraokeUtils.karaokeRoomInfo2NEKaraokeRoomInfo(
                                            it
                                        )
                                    }
                                )
                            }

                            override fun error(code: Int, msg: String?) {
                                KaraokeLog.e(
                                    TAG,
                                    "get room info after join room error: code = $code message = $msg"
                                )
                                callback?.onFailure(code, msg)
                            }
                        }
                    )
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.d(TAG, "joinRoom error: code=$code message=$message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    private fun initSongServiceAfterLogin(account: String) {
        TimerTaskUtil.init()
        getSongDynamicTokenUntilSuccess(object :
            NEKaraokeCallback<NEKaraokeDynamicToken> {
            override fun onSuccess(t: NEKaraokeDynamicToken?) {
                // 初始化点歌台服务
                KaraokeRepository.serverConfig.apply {
                    copyrightedMediaService.initialize(
                        context,
                        appKey,
                        t!!.accessToken,
                        account,
                        mapOf("serverUrl" to serverUrl)
                    )
                }
            }

            override fun onFailure(code: Int, msg: String?) {
                KaraokeLog.logApi("getSongDynamicTokenUntilSuccess onFailure")
            }
        })
    }

    override fun endRoom(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("endRoom")
        karaokeInfo?.let {
            karaokeService.stopKaraoke(
                it.liveModel.liveRecordId,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.i(TAG, "stopKaraoke success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "stopKaraoke error: code = $code message = $msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "karaokeInfo info is empty")

        myRoomService.endRoom(object : NECallback2<Unit>() {
            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "endRoom error: code = $code message = $message")
            }

            override fun onSuccess(data: Unit?) {
                KaraokeLog.i(TAG, "endRoom success")
            }
        })
    }

    override fun leaveRoom(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("leaveRoom")
        myRoomService.leaveRoom(object : NECallback2<Unit>() {
            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "leaveRoom: error code = $code message = $message")
                callback?.onFailure(code, message)
            }

            override fun onSuccess(data: Unit?) {
                KaraokeLog.d(TAG, "leaveRoom success")
                callback?.onSuccess(null)
            }
        })
    }

    override fun getSeatInfo(callback: NEKaraokeCallback<NEKaraokeSeatInfo>?) {
        KaraokeLog.logApi("getSeatInfo")
        myRoomService.getSeatInfo(object : NECallback2<NESeatInfo>() {
            override fun onSuccess(data: NESeatInfo?) {
                KaraokeLog.i(TAG, "getSeatInfo success")
                callback?.onSuccess(
                    data?.let {
                        KaraokeUtils.karaokeSeatInfo2NEKaraokeSeatInfo(it)
                    }
                )
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "getSeatInfo error:code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun getSeatRequestList(callback: NEKaraokeCallback<List<NEKaraokeSeatRequestItem>>?) {
        KaraokeLog.logApi("getSeatRequestList")
        myRoomService.getSeatRequestList(object : NECallback2<List<NESeatRequestItem>>() {
            override fun onSuccess(data: List<NESeatRequestItem>?) {
                KaraokeLog.i(TAG, "getSeatRequestList success")
                callback?.onSuccess(
                    data?.map {
                        KaraokeUtils.karaokeSeatRequestItem2NEKaraokeSeatRequestItem(
                            it
                        )
                    }
                )
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "getSeatRequestList error:code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun requestSeat(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("requestSeat")
        myRoomService.applyOnSeat(
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "requestSeat success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "requestSeat error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun cancelRequestSeat(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("cancelRequestSeat")
        myRoomService.cancelApplyOnSeat(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.i(TAG, "cancelRequestSeat success")
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "cancelRequestSeat error: code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun approveRequestSeat(account: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("approveRequestSeat: account=$account")
        myRoomService.approveSeatRequest(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "approveRequestSeat success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "approveRequestSeat error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun rejectRequestSeat(account: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("rejectRequestSeat: account=$account")
        myRoomService.rejectSeatRequest(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "rejectRequestSeat success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "rejectRequestSeat error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun kickSeat(account: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("kickSeat: account=$account")
        myRoomService.kickSeat(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "kickSeat success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "kickSeat error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun leaveSeat(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("leaveSeat")
        myRoomService.leaveSeat(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.i(TAG, "leaveSeat success")
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "leaveSeat error: code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun sendTextMessage(content: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("sendTextMessage")
        myRoomService.sendTextMessage(
            content,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.d(TAG, "sendTextMessage success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "sendTextMessage error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun kickMemberOut(account: String, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("kickMemberOut:account=$account")
        myRoomService.kickMemberOut(
            account,
            object : NECallback2<Unit>() {
                override fun onSuccess(data: Unit?) {
                    KaraokeLog.i(TAG, "kickMemberOut success")
                    callback?.onSuccess(data)
                }

                override fun onError(code: Int, message: String?) {
                    KaraokeLog.e(TAG, "kickMemberOut error: code = $code message = $message")
                    callback?.onFailure(code, message)
                }
            }
        )
    }

    override fun muteMyAudio(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("muteMyAudio")
        myRoomService.muteMyAudio(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.i(TAG, "muteMyAudio success")
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "muteMyAudio error:code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun unmuteMyAudio(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("unmuteMyAudio")
        myRoomService.unmuteMyAudio(object : NECallback2<Unit>() {
            override fun onSuccess(data: Unit?) {
                KaraokeLog.i(TAG, "unmuteMyAudio success")
                callback?.onSuccess(data)
            }

            override fun onError(code: Int, message: String?) {
                KaraokeLog.e(TAG, "unmuteMyAudio error code = $code message = $message")
                callback?.onFailure(code, message)
            }
        })
    }

    override fun playSong(
        originPath: String,
        accompanyPath: String,
        volume: Int,
        anchorAccount: String,
        chorusAccount: String?,
        startTimeStamp: Long,
        anchor: Boolean,
        mode: NEKaraokeSongMode,
        callback: NEKaraokeCallback<Unit>?
    ) {
        KaraokeLog.logApi(
            "playMusic:originPath=$originPath, accompanyPath=$accompanyPath," +
                " volume=$volume, anchorAccount=$anchorAccount, chorusAccount=$chorusAccount, " +
                "startTimeStamp=$startTimeStamp, anchor=$anchor, mode=$mode "
        )
        myRoomService.getAudioPlayService()?.startLocalSong(
            originPath,
            accompanyPath,
            volume,
            anchorAccount,
            chorusAccount,
            startTimeStamp,
            anchor,
            mode
        )
        callback?.onSuccess(Unit)
    }

    override fun adjustPlayingSongVolume(volume: Int): Int {
        KaraokeLog.logApi("adjustPlayMusicVolume: volume=$volume ")
        val effectId = myRoomService.getAudioPlayService()?.currentEffectId() ?: 0
        return myRoomService.adjustPlayMusicVolume(effectId, volume)
    }

    override fun adjustRecordingSignalVolume(volume: Int): Int {
        KaraokeLog.logApi("adjustRecordingSignalVolume: volume=$volume ")
        return myRoomService.adjustRecordingSignalVolume(volume)
    }

    override fun setLocalVoicePitch(pitch: Double): Int {
        KaraokeLog.logApi("setLocalVoicePitch: volume=$pitch ")
        return myRoomService.setLocalVoicePitch(pitch)
    }

    override fun setLocalVoiceReverbParam(param: NEKaraokeReverbParam): Int {
        KaraokeLog.logApi("setLocalVoiceReverbParam: param=$param ")
        return myRoomService.setLocalVoiceReverbParam(param)
    }

    override fun setLocalVoiceEqualization(bandFrequency: Int, bandGain: Int): Int {
        KaraokeLog.logApi(
            "setLocalVoiceEqualization: bandFrequency=$bandFrequency, bandGain=$bandGain "
        )
        return myRoomService.setLocalVoiceEqualization(bandFrequency, bandGain)
    }

    override fun enableEarBack(volume: Int): Int {
        KaraokeLog.logApi("enableEarBack: volume=$volume")
        return myRoomService.enableEarBack(volume)
    }

    override fun disableEarBack(): Int {
        KaraokeLog.logApi("disableEarBack")
        return myRoomService.disableEarBack()
    }

    override fun inviteChorus(orderId: Long, callback: NEKaraokeCallback<NEKaraokeSongModel>?) {
        KaraokeLog.logApi("inviteChorus: orderId=$orderId")
        myRoomService.getRoomUuid()?.apply {
            val rtt = myRoomService.getRtt()
            val delay = myRoomService.getDelayTime()
            val params = DeviceParam(delay, rtt, getWiredHeadset())
            val request = InviteChorusRequest(orderId, deviceParam = params)
            karaokeService.inviteChorus(
                this,
                request,
                object : NetRequestCallback<KaraokeSongInfo> {
                    override fun success(info: KaraokeSongInfo?) {
                        KaraokeLog.d(TAG, "inviteChorus success: info=$info")
                        callback?.onSuccess(
                            info?.let {
                                KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(
                                    it,
                                    it.operator
                                )
                            }
                        )
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "inviteChorus error: code=$code, message=$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(
            NEErrorCode.FAILURE,
            "roomUuid is empty,please ensure join room first"
        )
    }

    override fun cancelInviteChorus(
        chorusId: String,
        callback: NEKaraokeCallback<NEKaraokeSongModel>?
    ) {
        KaraokeLog.logApi("cancelInviteChorus: chorusId=$chorusId")
        myRoomService.getRoomUuid()?.apply {
            karaokeService.cancelChorus(
                this,
                CancelChorusRequest(chorusId = chorusId),
                object : NetRequestCallback<KaraokeSongInfo> {
                    override fun success(info: KaraokeSongInfo?) {
                        KaraokeLog.d(TAG, "cancelInviteChorus success")
                        callback?.onSuccess(
                            info?.let {
                                KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(
                                    it,
                                    it.operator
                                )
                            }
                        )
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "cancelInviteChorus error: code=$code, message=$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(
            NEErrorCode.FAILURE,
            "roomUuid is empty,please ensure join room first"
        )
    }

    override fun joinChorus(chorusId: String, callback: NEKaraokeCallback<NEKaraokeSongModel>?) {
        KaraokeLog.logApi("joinChorus: chorusId=$chorusId")
        myRoomService.getRoomUuid()?.apply {
            val rtt = myRoomService.getRtt()
            val delay = myRoomService.getDelayTime()
            val params = DeviceParam(delay, rtt, getWiredHeadset())
            val request =
                JoinChorusRequest(chorusId = chorusId, deviceParam = params)
            karaokeService.joinChorus(
                this,
                request,
                object : NetRequestCallback<KaraokeSongInfo> {
                    override fun success(info: KaraokeSongInfo?) {
                        KaraokeLog.d(TAG, "joinChorus success")
                        callback?.onSuccess(
                            info?.let {
                                KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(
                                    it,
                                    it.operator
                                )
                            }
                        )
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "joinChorus error: code=$code, message=$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(
            NEErrorCode.FAILURE,
            "roomUuid is empty,please ensure join room first"
        )
    }

    override fun chorusReady(chorusId: String, callback: NEKaraokeCallback<NEKaraokeSongModel>?) {
        KaraokeLog.logApi("chorusReady: chorusId=$chorusId")
        myRoomService.getRoomUuid()?.apply {
            karaokeService.chorusReady(
                this,
                chorusId,
                object : NetRequestCallback<KaraokeSongInfo> {
                    override fun success(info: KaraokeSongInfo?) {
                        KaraokeLog.d(TAG, "chorusReady success")
                        callback?.onSuccess(
                            info?.let {
                                KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(
                                    it,
                                    it.operator
                                )
                            }
                        )
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "chorusReady error: code=$code, message=$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(
            NEErrorCode.FAILURE,
            "roomUuid is empty,please ensure join room first"
        )
    }

    override fun addKaraokeListener(listener: NEKaraokeListener) {
        KaraokeLog.logApi("addKaraokeListener: listener=$listener")
        myRoomService.addListener(listener)
    }

    override fun removeKaraokeListener(listener: NEKaraokeListener) {
        KaraokeLog.logApi("removeKaraokeListener: listener=$listener")
        myRoomService.removeListener(listener)
    }

    override fun requestPlaySong(
        orderId: Long,
        chorusId: String?,
        ext: Map<String, Any>?,
        callback: NEKaraokeCallback<Unit>?
    ) {
        KaraokeLog.logApi("requestPlaySong orderId = $orderId, chorusId = $chorusId,  ext = $ext")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            val request = StartSingRequest(orderId, chorusId, ext)
            karaokeService.startSing(
                roomUuid,
                request,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.d(TAG, "startSong server success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "startSong server error")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        }
    }

    override fun requestPausePlayingSong(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("pauseSong")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            karaokeService.playControl(
                this,
                pauseSong,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.d(TAG, "pauseSong server success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.d(TAG, "pauseSong server error:$code,msg:$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "roomUuid is empty")
    }

    override fun requestPlayingSongInfo(callback: NEKaraokeCallback<NEKaraokeSongModel>?) {
        KaraokeLog.logApi("requestPlayingSongInfo")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            karaokeService.currentRoomSingInfo(
                roomUuid,
                object : NetRequestCallback<KaraokeSongInfo> {
                    override fun success(info: KaraokeSongInfo?) {
                        KaraokeLog.d(TAG, "currentRoomSingInfo success")

                        // 观众可以在这里刷新当前播放歌曲的orderId
                        info?.let {
                            myRoomService.getAudioPlayService()?.startSong(it.orderId)
                        }

                        callback?.onSuccess(
                            info?.let {
                                KaraokeUtils.karaokeSongInfo2NEKaraokeSongModel(
                                    it,
                                    it.operator
                                )
                            }
                        )
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.d(TAG, "currentRoomSingInfo error:$code,msg:$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "roomUuid is empty")
    }

    override fun requestResumePlayingSong(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("requestResumePlayingSong")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            karaokeService.playControl(
                this,
                resumeSong,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.d(TAG, "resumeSong server success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.d(TAG, "resumeSong server error:$code,msg:$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "roomUuid is empty")
    }

    override fun requestStopPlayingSong(callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("stopSong")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            karaokeService.playControl(
                this,
                finishSong,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.d(TAG, "stopSong server success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.d(TAG, "stopSong server error:$code,msg:$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "roomUuid is empty")
    }

    override fun abandonSong(orderId: Long, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.logApi("abandonSong orderId = $orderId")
        val roomUuid = myRoomService.getRoomUuid()
        roomUuid?.apply {
            karaokeService.abandon(
                this,
                orderId,
                object : NetRequestCallback<Unit> {
                    override fun success(info: Unit?) {
                        KaraokeLog.d(TAG, "abandon server success")
                        callback?.onSuccess(info)
                    }

                    override fun error(code: Int, msg: String?) {
                        KaraokeLog.e(TAG, "abandon server error:$code,msg:$msg")
                        callback?.onFailure(code, msg)
                    }
                }
            )
        } ?: callback?.onFailure(NEErrorCode.FAILURE, "roomUuid is empty")
    }

    override fun setPlayingPosition(position: Long): Int {
        KaraokeLog.d(TAG, "setPlayingPosition position = $position")
        return myRoomService.getAudioPlayService()?.seek(position) ?: NEKaraokeErrorCode.FAILURE
    }

    override fun switchAccompaniment(enableAccompaniment: Boolean): Int {
        KaraokeLog.d(TAG, "switchAccompaniment enableAccompaniment = $enableAccompaniment")
        return myRoomService.getAudioPlayService()?.switchToOriginalVolume(!enableAccompaniment)
            ?: NEKaraokeErrorCode.FAILURE
    }

    override fun sendBatchGift(giftId: Int, giftCount: Int, userUuids: List<String>, callback: NEKaraokeCallback<Unit>?) {
        KaraokeLog.d(TAG, "sendGift gift = $giftId")
        karaokeService.sendBatchGift(
            liveRecordId,
            giftId,
            giftCount,
            userUuids,
            object : NetRequestCallback<Unit> {
                override fun success(info: Unit?) {
                    KaraokeLog.i(TAG, "reward success")
                }

                override fun error(code: Int, msg: String?) {
                    KaraokeLog.e(TAG, "reward failed code = $code message = $msg")
                }
            }
        )
    }

    override fun currentSongIdForAudioEffect(): Int {
        KaraokeLog.logApi("currentSongIdForAudioEffect")
        return myRoomService.getAudioPlayService()?.currentEffectId() ?: 0
    }

    override fun isOriginalSongPlaying(): Boolean {
        KaraokeLog.logApi("isOriginalSongPlaying")
        return myRoomService.getAudioPlayService()?.isOriginal() ?: false
    }

    @Suppress("DEPRECATION")
    private fun getWiredHeadset(): Int {
        KaraokeLog.logApi("getWiredHeadset")
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        KaraokeLog.d(TAG, "getWiredHeadset ===:${audioManager.isWiredHeadsetOn}")
        return if (audioManager.isWiredHeadsetOn) {
            1
        } else {
            0
        }
    }

    override fun getOrderedSongs(callback: NEKaraokeCallback<List<NEKaraokeOrderSongResult>>) {
        KaraokeLog.logApi("getOrderedSongs")
        karaokeService.getOrderedSongs(
            liveRecordId,
            object : NetRequestCallback<List<NEKaraokeOrderSongResult>> {
                override fun success(info: List<NEKaraokeOrderSongResult>?) {
                    callback.onSuccess(info)
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    override fun orderSong(
        songInfo: NEKaraokeOrderSongParams,
        callback: NEKaraokeCallback<NEKaraokeOrderSongResult>
    ) {
        KaraokeLog.logApi("orderSong songInfo = $songInfo")
        karaokeService.orderSong(
            liveRecordId,
            songInfo,
            object : NetRequestCallback<NEKaraokeOrderSongResult> {
                override fun success(info: NEKaraokeOrderSongResult?) {
                    callback.onSuccess(info)
                }

                override fun error(code: Int, msg: String?) {
                    callback.onFailure(code, msg)
                }
            }
        )
    }

    override fun deleteSong(orderId: Long, callback: NEKaraokeCallback<Void>?) {
        KaraokeLog.logApi("deleteSong orderId = $orderId")
        karaokeService.deleteSong(
            liveRecordId,
            orderId,
            object : NetRequestCallback<Boolean> {
                override fun success(info: Boolean?) {
                    callback?.onSuccess(null)
                }

                override fun error(code: Int, msg: String?) {
                    callback?.onFailure(code, msg)
                }
            }
        )
    }

    override fun topSong(orderId: Long, callback: NEKaraokeCallback<Void>?) {
        KaraokeLog.logApi("topSong orderId = $orderId")
        karaokeService.topSong(
            liveRecordId,
            orderId,
            object : NetRequestCallback<Boolean> {
                override fun success(info: Boolean?) {
                    callback?.onSuccess(null)
                }

                override fun error(code: Int, msg: String?) {
                    callback?.onFailure(code, msg)
                }
            }
        )
    }

    override fun nextSong(orderId: Long, callback: NEKaraokeCallback<Void>?) {
        KaraokeLog.logApi("nextSong orderId = $orderId")
        myRoomService.getAudioPlayService()?.pauseLocalSong()

        karaokeService.nextSong(
            liveRecordId,
            orderId,
            object : NetRequestCallback<Boolean> {
                override fun success(info: Boolean?) {
                    callback?.onSuccess(null)
                }

                override fun error(code: Int, msg: String?) {
                    myRoomService.getAudioPlayService()?.resumeLocalSong()
                    callback?.onFailure(code, msg)
                }
            }
        )
    }

    private fun getSongToken(callback: NEKaraokeCallback<NEKaraokeDynamicToken>?) {
        karaokeService.getSongToken(object : NetRequestCallback<NEKaraokeDynamicToken> {
            override fun success(info: NEKaraokeDynamicToken?) {
                callback?.onSuccess(info)
            }

            override fun error(code: Int, msg: String?) {
                callback?.onFailure(code, msg)
            }
        })
    }

    /** 刷新版权token。网络导致的失败会重试  */
    override fun getSongDynamicTokenUntilSuccess(
        callback: NEKaraokeCallback<NEKaraokeDynamicToken>?
    ) {
        KaraokeLog.d(KaraokeServiceImpl.TAG, "getSongDynamicTokenUntilSuccess")
        val runnable: Runnable = object : Runnable {
            var count = 0
            override fun run() {
                getSongToken(
                    object : NEKaraokeCallback<NEKaraokeDynamicToken> {
                        override fun onSuccess(token: NEKaraokeDynamicToken?) {
                            if (token != null) {
                                KaraokeLog.d(
                                    KaraokeServiceImpl.TAG,
                                    "getSongDynamicTokenUntilSuccess onSuccess $token "
                                )
                                copyrightedMediaService.renewToken(token.accessToken)

                                // 设置token延时任务
                                val tokenWillExpiredTask = Runnable {
                                    onTokenWillExpired()
                                }
                                var delaySeconds = (token.expiresIn - aheadTimeRefreshToken) * 1000
                                if (delaySeconds < 0) delaySeconds = 0
                                TimerTaskUtil.addTask(
                                    tokenWillExpiredTaskId,
                                    tokenWillExpiredTask,
                                    delaySeconds
                                )
                                callback?.onSuccess(token)
                            }
                        }

                        override fun onFailure(code: Int, msg: String?) {
                            KaraokeLog.d(KaraokeServiceImpl.TAG, "getSongToken failed: $code $msg")
                            count++
                            if (count < refreshTokenRetryTimes) {
                                retryTask()
                            } else {
                                callback?.onFailure(code, msg)
                            }
                        }
                    }
                )
            }

            fun retryTask() {
                TimerTaskUtil.addTask(
                    refreshTokenTaskId,
                    this,
                    (2.0.pow(count.toDouble()) * 1000).toLong()
                )
            }
        }
        TimerTaskUtil.addTask(refreshTokenTaskId, runnable, 0)
    }

    private fun onTokenWillExpired() {
        getSongDynamicTokenUntilSuccess(null)
    }

    override fun setCopyrightedMediaEventHandler(
        eventHandler: NEKaraokeCopyrightedMediaEventHandler?
    ) {
        val copyrightedEventHandler = object : NECopyrightedEventHandler {
            override fun onTokenExpired() {
                eventHandler?.onTokenExpired()
            }
        }
        copyrightedMediaService.setEventHandler(copyrightedEventHandler)
    }

    override fun preloadSong(
        songId: String,
        channel: Int,
        callback: NEKaraokeCopyrightedMediaListener
    ) {
        val songPreloadCallback = object : NESongPreloadCallback {
            override fun onPreloadStart(songId: String?, channel: Int) {
                callback.onPreloadStart(songId, channel)
            }

            override fun onPreloadProgress(songId: String?, channel: Int, progress: Float) {
                callback.onPreloadProgress(songId, channel, progress)
            }

            override fun onPreloadComplete(
                songId: String?,
                channel: Int,
                errorCode: Int,
                msg: String?
            ) {
                callback.onPreloadComplete(songId, channel, errorCode, msg)
            }
        }
        copyrightedMediaService.preloadSong(songId, channel, songPreloadCallback)
    }

    override fun clearSongCache() {
        copyrightedMediaService.clearSongCache()
    }

    override fun cancelPreloadSong(songId: String, channel: Int) {
        copyrightedMediaService.cancelPreloadSong(songId, channel)
    }

    override fun isSongPreloaded(songID: String, channel: Int): Boolean {
        return copyrightedMediaService.isSongPreloaded(songID, channel)
    }

    override fun searchSong(
        keyword: String,
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: NECopyrightedMedia.Callback<List<NECopyrightedSong>>
    ) {
        copyrightedMediaService.searchSong(keyword, channel, pageNum, pageSize, callback)
    }

    override fun getSongURI(songId: String, channel: Int, songResType: SongResType): String? {
        return copyrightedMediaService.getSongURI(songId, channel, songResType)
    }

    override fun getLyric(songId: String, channel: Int): String? {
        return copyrightedMediaService.getLyric(songId, channel)
    }

    override fun getPitch(songId: String, channel: Int): String? {
        return copyrightedMediaService.getPitch(songId, channel)
    }

    override fun preloadSongLyric(songId: String, channel: Int, callback: LyricCallback) {
        copyrightedMediaService.preloadSongLyric(songId, channel, callback)
    }

    override fun getSongList(
        tags: List<String>?,
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: NECopyrightedMedia.Callback<List<NECopyrightedSong>>
    ) {
        copyrightedMediaService.getSongList(tags, channel, pageNum, pageSize, callback)
    }

    override fun getHotSongList(
        hotType: NECopyrightedHotType,
        hotDimension: NECopyrightedHotDimension,
        channel: Int?,
        pageNum: Int?,
        pageSize: Int?,
        callback: NECopyrightedMedia.Callback<List<NECopyrightedHotSong>>
    ) {
        copyrightedMediaService.getHotSongList(
            hotType,
            hotDimension,
            channel,
            pageNum,
            pageSize,
            callback
        )
    }
}
