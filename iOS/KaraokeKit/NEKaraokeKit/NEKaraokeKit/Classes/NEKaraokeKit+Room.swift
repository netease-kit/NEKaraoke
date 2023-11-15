// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
import UIKit
import NECopyrightedMedia
/// 房间操作扩展
public extension NEKaraokeKit {
  /// 查询房间列表
  /// - Parameters:
  ///   - liveState: 直播状态
  ///   - pageNum: 页码
  ///   - pageSize: 页大小
  ///   - callback: 房间列表回调
  func getKaraokeRoomList(liveState: NEKaraokeLiveState,
                          pageNum: Int,
                          pageSize: Int,
                          callback: NEKaraokeCallback<NEKaraokeRoomList>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Room List.")
    Judge.initCondition({
      self.roomService.getKaraokeRoomList(
        liveState: liveState.rawValue,
        pageNum: pageNum,
        pageSize: pageSize
      ) { list in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully get room list.")
        callback?(NEKaraokeErrorCode.success, nil, list)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to get room list. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 查询房间详情
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - callback: 回调
  internal func getKaraokeRoomInfo(_ liveRecordId: Int,
                                   callback: NEKaraokeCallback<NEKaraokeRoomInfo>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get room detail.")
    Judge.initCondition({
      self.roomService.getKaraokeRoomInfo(liveRecordId) { detail in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully get room detail.")
        callback?(NEKaraokeErrorCode.success, nil, NEKaraokeRoomInfo(create: detail))
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to get room detail. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 创建房间
  /// - Parameters:
  ///   - params: 房间参数
  ///   - options: 房间配置
  ///   - callback: 回调
  func createRoom(_ params: NECreateKaraokeParams,
                  options _: NECreateKaraokeOptions,
                  callback: NEKaraokeCallback<NEKaraokeRoomInfo>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Create room.")
    Judge.initCondition({
      self.roomService.startKaraoke(params) { [weak self] resp in
        guard let self = self else { return }
        guard let resp = resp else {
          NEKaraokeLog.errorLog(kitTag, desc: "Failed to create room. RoomUuid is nil.")
          callback?(
            NEKaraokeErrorCode.failed,
            "Failed to create room. RoomUuid is nil.",
            nil
          )
          return
        }
        // 存储直播信息
        self.liveInfo = resp
        if let roomUuid = resp.live?.roomUuid,
           let liveRecordId = resp.live?.liveRecordId {
          self.musicService = NEKaraokeMusicService(UInt64(liveRecordId), roomUuid: roomUuid)
        }
        NEKaraokeLog.successLog(kitTag, desc: "Successfully create room.")
        callback?(NEKaraokeErrorCode.success, nil, NEKaraokeRoomInfo(create: resp))
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to create room. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 加入房间
  /// - Parameters:
  ///   - params: 加入房间时参数
  ///   - options: 加入房间时配置
  ///   - callback: 回调
  func joinRoom(_ params: NEJoinKaraokeParams,
                options _: NEJoinKaraokeOptions,
                callback: NEKaraokeCallback<NEKaraokeRoomInfo>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Join room.")
    Judge.initCondition({
      func join(_ params: NEJoinKaraokeParams,
                callback: NEKaraokeCallback<NEKaraokeRoomInfo>?) {
        self.musicService = NEKaraokeMusicService(UInt64(params.liveRecordId), roomUuid: params.roomUuid)
        // 版权设置为唱歌场景
        NECopyrightedMedia.getInstance().setSongScene(SongScene(1))
        self.deletingSongs.removeAll()
        self._joinRoom(params.roomUuid,
                       userName: params.nick,
                       role: params.role.toString()) { joinCode, joinMsg, _ in
          if joinCode == 0 {
            self.roomService.getKaraokeRoomInfo(params.liveRecordId) { [weak self] data in
              guard let self = self else { return }
              guard let data = data else {
                NEKaraokeLog.infoLog(
                  kitTag,
                  desc: "Failed to join room. RoomInfo is nil."
                )
                callback?(-1, nil, nil)
                return
              }
              self.liveInfo = data
              callback?(joinCode, nil, NEKaraokeRoomInfo(create: data))
            } failure: { error in
              NEKaraokeLog.errorLog(
                kitTag,
                desc: "Failed to join room. Code: \(error.code). Msg: \(error.localizedDescription)"
              )
              callback?(error.code, error.localizedDescription, nil)
            }
          } else {
            callback?(joinCode, joinMsg, nil)
          }
        }
      }
      // 如果已经在此房间里则先退出
      if let context = NERoomKit.shared().roomService.getRoomContext(roomUuid: params.roomUuid) {
        context.leaveRoom { code, msg, obj in
          join(params, callback: callback)
        }
      } else {
        join(params, callback: callback)
      }
    }, failure: callback)
  }

  /// 离开房间
  /// - Parameter callback: 回调
  func leaveRoom(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Leave room.")
    Judge.preCondition({
      guard let context = self.roomContext else {
        NEKaraokeLog.errorLog(kitTag, desc: "Leave room, roomContext not exist")
        callback?(NEKaraokeErrorCode.success, nil, nil)
        return
      }
      context.leaveRoom { [weak self] code, msg, _ in
        guard let self = self else { return }
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully leave room.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to leave room. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        // 移除 房间监听、消息监听
        self.roomContext?.removeRoomListener(listener: self)
        self.roomContext = nil
        // 销毁
        self.audioPlayService?.destroy()
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 结束房间
  /// - Parameter callback: 回调
  func endRoom(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "End room.")
    Judge.initCondition({
      guard let liveRecordId = self.liveInfo?.live?.liveRecordId else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to end room. LiveRecordId don't exist.")
        callback?(
          NEKaraokeErrorCode.failed,
          "Failed to end room. KaraokeInfo is empty.",
          nil
        )
        return
      }
      self.roomContext?.endRoom(isForce: true)
      self.roomService.endRoom(liveRecordId) {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully end room.")
        callback?(NEKaraokeErrorCode.success, nil, nil)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to end room. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
      self.liveInfo = nil
      // 移除 房间监听、消息监听
      self.roomContext?.removeRoomListener(listener: self)
      self.roomContext = nil
      self.audioPlayService?.destroy()
    }, failure: callback)
  }
}

// 加入karaoke扩展
extension NEKaraokeKit {
  func _joinRoom(_ context: NERoomContext,
                 role: String,
                 callback: NEKaraokeCallback<AnyObject>? = nil) {
    // 初始化播放模块
    context.rtcController.setClientRole(.audience)
    _audioPlayService = NEKaraokeAudioPlayService(roomUuid: context.roomUuid)
    _audioPlayService?.callback = self
    let group = DispatchGroup()

    var rtcCode: Int?
    var rtcMsg: String?
    var chatCode: Int?
    var chatMsg: String?

    // 加入rtc
    group.enter()
    var timestamp = Date().timeIntervalSince1970
    NEKaraokeLog.infoLog(kitTag, desc: "joinRtcChannel Timestamp: \(timestamp)")
    context.rtcController.joinRtcChannel { code, msg, _ in
      timestamp = Date().timeIntervalSince1970
      NEKaraokeLog.infoLog(kitTag, desc: "joinRtcChannel callback Timestamp: \(timestamp)")
      rtcCode = code
      rtcMsg = msg
      group.leave()
    }

    // 加入聊天室
    group.enter()
    timestamp = Date().timeIntervalSince1970
    NEKaraokeLog.infoLog(kitTag, desc: "joinChatroom Timestamp: \(timestamp)")
    context.chatController.joinChatroom { code, msg, _ in
      timestamp = Date().timeIntervalSince1970
      NEKaraokeLog.infoLog(kitTag, desc: "joinChatroom callback Timestamp: \(timestamp)")
      chatCode = code
      chatMsg = msg
      group.leave()
    }

    group.notify(queue: .main) {
      timestamp = Date().timeIntervalSince1970
      NEKaraokeLog.infoLog(kitTag, desc: "joinRoom notify Timestamp: \(timestamp)")
      let isOwner = role == NEKaraokeRole.host.toString()
      if let rtcCode = rtcCode, rtcCode != 0 {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to join rtc. Code: \(rtcCode). Msg: \(rtcMsg ?? "")"
        )
        // 加入rtc 失败，离开房间
        isOwner ? self.endRoom() : context.leaveRoom()
        callback?(rtcCode, rtcMsg, nil)
      } else if let chatCode = chatCode, chatCode != 0 {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to join chatroom. Code: \(chatCode). Msg: \(chatMsg ?? "")"
        )
        // 加入聊天室失败，离开房间
        isOwner ? self.endRoom() : context.leaveRoom()
        callback?(chatCode, chatMsg, nil)
      } else {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully join room.")
        callback?(0, nil, nil)
      }
    }
  }

  func _joinRoom(_ roomUuid: String,
                 userName: String,
                 role: String,
                 roomContext: NERoomContext? = nil,
                 callback: NEKaraokeCallback<AnyObject>? = nil) {
    // 加入
    if let context = roomContext {
      _joinRoom(context, role: role, callback: callback)
      return
    }
    // 进入房间
    let joinParams = NEJoinRoomParams()
    joinParams.roomUuid = roomUuid
    joinParams.userName = userName
    joinParams.role = role
    let joinOptions = NEJoinRoomOptions()
    joinOptions.enableMyAudioDeviceOnJoinRtc = true
    NERoomKit.shared().roomService.joinRoom(params: joinParams,
                                            options: joinOptions) { [weak self] joinCode, joinMsg, context in
      guard let self = self else { return }
      guard let context = context else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to join room. Code: \(joinCode). Msg: \(joinMsg ?? "")"
        )
        callback?(joinCode, joinMsg, nil)
        return
      }
      self.roomContext = context
      context.addRoomListener(listener: self)
      context.seatController.addSeatListener(self)
      // 加入chatroom、rtc
      self._joinRoom(context, role: role, callback: callback)
    }
  }
}

/// 房间内管理扩展
public extension NEKaraokeKit {
  /// 踢人
  /// - Parameters:
  ///   - account: 被踢的用户Id
  ///   - callback: 回调
  func kickMemberOut(_ account: String,
                     callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Kick member out. Account: \(account)")
    Judge.preCondition({
      self.roomContext!.kickMemberOut(userUuid: account) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully kickout member.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to kickout member. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 关闭自己的麦克风
  /// - Parameter callback: 回调
  func muteMyAudio(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Mute my audio.")
    Judge.preCondition({
      guard let local = self.localMember?.account else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to mute my audio. Msg: Can't find LocalMember"
        )
        callback?(NEKaraokeErrorCode.failed, "Can't find LocalMember", nil)
        return
      }
      self.roomContext!.updateMemberProperty(
        userUuid: local,
        key: "recordDevice",
        value: "off"
      ) { code, msg, _ in
        if code == 0 {
          let code = self.roomContext!.rtcController.setRecordDeviceMute(muted: true)
          if code == 0 {
            NEKaraokeLog.successLog(kitTag, desc: "Successfully mute my audio.")
          } else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to mute my audio. Code: \(code). Msg: \(msg ?? "")"
            )
          }
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to mute my audio. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 打开自己的麦克风
  /// - Parameter callback: 回调
  func unmuteMyAudio(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Unmute my audio.")
    Judge.preCondition({
      guard let local = self.localMember?.account else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to unmute my audio. Msg: Can't find LocalMember"
        )
        callback?(NEKaraokeErrorCode.failed, "Can't find LocalMember", nil)
        return
      }
      self.roomContext!.updateMemberProperty(
        userUuid: local,
        key: "recordDevice",
        value: "on"
      ) { code, msg, _ in
        if code == 0 {
          let code = self.roomContext!.rtcController.setRecordDeviceMute(muted: false)
          if code == 0 {
            NEKaraokeLog.successLog(kitTag, desc: "Successfully mute my audio.")
          } else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to mute my audio. Code: \(code). Msg: \(msg ?? "")"
            )
          }
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to unmute my audio. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 开始唱歌
  /// - Parameters:
  ///   - originPath: 原唱地址
  ///   - accompanyPath: 伴音地址
  ///   - volume: 播放音量
  ///   - anchorUuid: 主唱账号
  ///   - chorusUid: 副唱账号
  ///   - startTimeStamp: 延迟播放时间 单位/毫秒
  ///   - anchor: 是否是主唱
  ///   - mode: 演唱模式
  ///   - callback: 回调
  func playSong(originPath: String,
                accompanyPath: String,
                volume: Int,
                anchorAccount: String,
                chorusAccount: String?,
                startTimeStamp: Int64,
                anchor: Bool,
                mode: NEKaraokeSongMode,
                callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(
      kitTag,
      desc: "Play music. OriginPath: \(originPath). AccompanyPath: \(accompanyPath) mode: \(mode.rawValue)"
    )
    Judge.preCondition({
      self.audioPlayService!.startSong(originalPath: originPath,
                                       accompanyPath: accompanyPath,
                                       volume: volume,
                                       anchorId: anchorAccount,
                                       choristerId: chorusAccount,
                                       startTimeStamp: startTimeStamp,
                                       anchor: anchor,
                                       mode: mode,
                                       callback: callback)
    }, failure: callback)
  }

  @discardableResult
  /// 调节播放歌曲的音量
  /// - Parameter volume: 音量  范围为 [0-100]
  /// - Returns: 0代表成功，否则失败
  func adjustPlayingSongVolume(_ volume: UInt32) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Adjust play music volume. Volume: \(volume)")
    guard let roomContext = roomContext else {
      NEKaraokeLog.infoLog(
        kitTag,
        desc: "Failed to adjust play music volume. RoomContext is nil."
      )
      return NEKaraokeErrorCode.failed
    }
    // TODO: 现在没在调用，后面沉到audioPlayService里
    if let effectId = audioPlayService?.currentEffectId {
      return roomContext.rtcController.setEffectPlaybackVolume(
        effectId: effectId,
        volume: UInt32(volume)
      )
    }
    return NEKaraokeErrorCode.failed
  }

  @discardableResult
  /// 调节人声音
  /// - Parameter volume: 音量  范围为 [0-100]
  /// - Returns: 0代表成功，否则失败
  func adjustRecordingSignalVolume(_ volume: UInt32) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Adjust recording signal volume. Volume: \(volume)")
    return Judge.syncCondition {
      self.roomContext!.rtcController.adjustRecordingSignalVolume(volume: volume)
    }
  }

  @discardableResult
  /// 变调
  /// - Parameter pitch: 音调
  /// - Returns: 0代表成功，否则失败
  func setLocalVoicePitch(_ pitch: Double) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Set local voice pitch. Pitch: \(pitch)")
    return Judge.syncCondition {
      self.roomContext!.rtcController.setLocalVoicePitch(pitch: pitch)
    }
  }

  @discardableResult
  /// 调节音效升降key。
  /// 发起音效后可调解。伴音结束后再发起需要重新设置。
  /// 音调pitch取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。取值的绝对值越大，音调升高或降低得越多。
  /// - Parameters:
  ///   - pitch: 按半音音阶调整本地播放音乐的音调，默认值为0，即不调整音调。取值范围为 [-12,12]。
  /// - Returns: 0: 代表成功 否则失败
  func setPlayingSongPitch(_ pitch: Int32) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Set playing song pitch. Pitch: \(pitch)")
    guard let roomContext = roomContext else {
      NEKaraokeLog.infoLog(
        kitTag,
        desc: "Failed to set playing song pitch. RoomContext is nil."
      )
      return NEKaraokeErrorCode.failed
    }
    // TODO: 现在没在调用，后面沉到audioPlayService里
    if let effectId = audioPlayService?.currentEffectId {
      return Int(roomContext.rtcController.setEffectPitch(effectId: effectId, pitch: pitch))
    }
    return NEKaraokeErrorCode.failed
  }

  @discardableResult
  /// 音效混响
  /// - Parameter param: 混响参数
  /// - Returns: 0代表成功，否则失败
  func setLocalVoiceReverbParam(_ param: NEKaraokeReverbParam) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Set local voice reverb param. Param: \(param)")
    return Judge.syncCondition {
      self.roomContext!.rtcController.setLocalVoiceReverbParam(param.converToRoom())
    }
  }

  @discardableResult
  /// 音效均衡
  /// - Parameters:
  ///   - bandFrequency: 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k]
  // Hz
  ///   - bandGain: 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
  /// - Returns: 0代表成功，否则失败
  func setLocalVoiceEqualization(_ bandFrequency: NEKaraokeAudioEqualizationBandFrequency,
                                 bandGain: Int) -> Int {
    NEKaraokeLog.infoLog(
      kitTag,
      desc: "Set local voice equalization. BandFrequency: \(bandFrequency). BandGain: \(bandGain)."
    )
    return Judge.syncCondition {
      self.roomContext!.rtcController.setLocalVoiceEqualization(
        NERoomAudioEqualizationBandFrequency(rawValue: bandFrequency.rawValue) ?? .band31,
        bandGain: bandGain
      )
    }
  }

  @discardableResult
  /// 开启耳返
  /// - Parameter volume: 耳返音量 范围: [0-100]
  /// - Returns: 0代表成功 否则失败
  func enableEarBack(volume: UInt32) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Enable earback. Volume: \(volume).")
    return Judge.syncCondition {
      let code = self.roomContext!.rtcController.enableEarback(volume: volume)
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully enable earback.")
      } else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to enable earback. Code: \(code)")
      }
      return code
    }
  }

  @discardableResult
  /// 关闭耳返
  /// - Returns: 0: 代表成功 否则失败
  func disableEarBack() -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "Disable earback.")
    return Judge.syncCondition {
      let code = self.roomContext!.rtcController.disableEarback()
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully disable earback.")
      } else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to disable earback: Code: \(code)")
      }
      return code
    }
  }

  @discardableResult
  /// 获取当前播放音乐长度
  /// - Returns: 长度
  func getEffectDuration() -> UInt64 {
    audioPlayService?.getEffectDuration() ?? 0
  }

  @discardableResult
  /// 设置音乐播放位置
  /// - Parameter position: 播放位置，单位毫秒
  /// - Returns: 0成功，其他失败
  func setPlayingPosition(position: UInt64) -> Int {
    audioPlayService?.setEffectPosition(position: position) ?? -1
  }

  /// 修改成员属性
  /// - Parameters:
  ///   - userUuid: 用户Id
  ///   - key: 属性名称
  ///   - value: 属性值
  ///   - callback: 回调
  internal func updateMemberProperty(_ userUuid: String,
                                     key: String,
                                     value: String,
                                     callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Update member property. Key: \(key). Value: \(value).")
    Judge.preCondition({
      self.roomContext!.updateMemberProperty(userUuid: userUuid,
                                             key: key,
                                             value: value) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully update member property.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to update update member property. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 删除成员属性
  /// - Parameters:
  ///   - userUuid: 成员ID
  ///   - key: 属性名
  ///   - callback: 回调
  internal func deleteMemberProperty(_ userUuid: String,
                                     key: String,
                                     callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Delete member property. Key: \(key).")
    Judge.preCondition({
      self.roomContext!.deleteMemberProperty(userUuid: userUuid,
                                             key: key) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully delete member property.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to delete member property. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 更新房间属性
  /// - Parameters:
  ///   - key: 属性名
  ///   - value: 属性值
  ///   - callback: 回调
  internal func updateRoomProperty(key: String,
                                   value: String,
                                   callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Updata room property. Key: \(key). Value: \(value).")
    Judge.preCondition({
      self.roomContext!.updateRoomProperty(key: key,
                                           value: value) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully update room property.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to update room property. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 删除房间属性
  /// - Parameters:
  ///   - key: 属性名
  ///   - callback: 回调
  internal func deleteRoomProperty(key: String,
                                   callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.infoLog(kitTag, desc: "Delete room property. Key: \(key).")
    Judge.preCondition({
      self.roomContext!.deleteRoomProperty(key: key) { code, msg, _ in
        if code == 0 {
          NEKaraokeLog.successLog(kitTag, desc: "Successfully delete room property.")
        } else {
          NEKaraokeLog.errorLog(
            kitTag,
            desc: "Failed to delete room property. Code: \(code). Msg: \(msg ?? "")"
          )
        }
        callback?(code, msg, nil)
      }
    }, failure: callback)
  }

  /// 获取实时Token
  /// - Parameter callback: 回调
  func getSongToken(callback: NEKaraokeCallback<NEKaraokeDynamicToken>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get Song Token")
    Judge.initCondition({
      self.roomService.getSongToken { data in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully getSongToken")
        callback?(NEKaraokeErrorCode.success, nil, data)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to getSongToken. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }

    }, failure: callback)
  }
}
