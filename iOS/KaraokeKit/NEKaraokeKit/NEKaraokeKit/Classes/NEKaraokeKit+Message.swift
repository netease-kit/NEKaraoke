// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 消息扩展
public extension NEKaraokeKit {
  /// 发送文字聊天消息
  /// - Parameters:
  ///   - content: 发送的聊天内容
  ///   - callback: 回调
  func sendTextMessage(_ content: String,
                       callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Send text message. Content: \(content).")
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to send text message. Uninitialized.")
      callback?(NEKaraokeErrorCode.failed, "Failed to send text message. Uninitialized.", nil)
      return
    }
    guard let roomContext = roomContext else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to send text message. RoomContext is nil.")
      callback?(NEKaraokeErrorCode.failed, nil, nil)
      return
    }
    roomContext.chatController.sendBroadcastTextMessage(message: content) { code, msg, _ in
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully send text message.")
      } else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to send text message. Code: \(code). Msg: \(msg ?? "")"
        )
      }
      callback?(code, msg, nil)
    }
  }

  /// 打赏麦上的主播或者观众
  /// - Parameters:
  ///   - giftId: 礼物编号
  ///   - giftCount: 礼物数量
  ///   - userUuids: 要打赏的目标用户
  ///   - callback: 结果回调
  func sendBatchGift(_ giftId: Int,
                     giftCount: Int,
                     userUuids: [String],
                     callback: NEKaraokeCallback<AnyObject>? = nil) {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to send batch gift. Uninitialized.")
      callback?(NEKaraokeErrorCode.failed, "Failed to send batch gift. Uninitialized.", nil)
      return
    }
    guard let liveRecordId = liveInfo?.live?.liveRecordId
    else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to send batch gift. liveRecordId not exist.")
      callback?(
        NEKaraokeErrorCode.failed,
        "Failed to send batch gift. liveRecordId not exist.",
        nil
      )
      return
    }
    roomService.batchReward(liveRecordId, giftId: giftId, giftCount: giftCount, userUuids: userUuids) {
      callback?(NEKaraokeErrorCode.success, "Successfully send batch gift.", nil)
    } failure: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// 给房间内用户发送自定义消息，如房间内信令
  /// - Parameters:
  ///   - userUuid: 目标成员Id
  ///   - commandId: 消息类型 区间[10000 - 19999]
  ///   - data: 自定义消息内容
  ///   - callback: 回调
  internal func sendCustomMessage(_ userUuid: String,
                                  commandId: Int,
                                  data: String,
                                  callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(
      kitTag,
      desc: "Send custom message. UserUuid: \(userUuid). CommandId: \(commandId). Data: \(data)"
    )
    Judge.preCondition({
      NERoomKit.shared().messageChannelService
        .sendCustomMessage(roomUuid: self.roomContext!.roomUuid,
                           userUuid: userUuid,
                           commandId: commandId,
                           data: data, crossAppAuthorization: nil) { code, msg, _ in
          if code == 0 {
            NEKaraokeLog.successLog(kitTag, desc: "Successfully send custom message.")
          } else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to send custom message. Code: \(code). Msg: \(msg ?? "")"
            )
          }
          callback?(code, msg, nil)
        }
    }, failure: callback)
  }
}

/// 房间内监听
extension NEKaraokeKit: NERoomListener {
  /// 成员属性变更
  public func onMemberPropertiesChanged(member: NERoomMember, properties _: [String: String]) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener,
              let listener = pointerListener as? NEKaraokeListener else { continue }
        let item = NEKaraokeMember(member)
        if listener
          .responds(to: #selector(NEKaraokeListener
              .onMemberAudioMuteChanged(_:mute:operateBy:))) {
          listener.onMemberAudioMuteChanged?(item, mute: !item.isAudioOn, operateBy: nil)
        }
      }
    }
  }

  public func onMemberJoinRtcChannel(members: [NERoomMember]) {
    for member in members {
      if member.uuid == localMember?.account {
        roomContext?.rtcController.unmuteMyAudio()
      } else {
        roomContext?.rtcController.subscribeRemoteVideoStream(
          userUuid: member.uuid,
          streamType: .low
        )
      }
    }
  }

  /// 成员进入房间
  public func onMemberJoinRoom(members: [NERoomMember]) {
    var kMembers = [NEKaraokeMember]()
    for member in members {
      kMembers.append(NEKaraokeMember(member))
    }

    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener,
              let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onMemberJoinRoom(_:))) {
          listener.onMemberJoinRoom?(kMembers)
        }
      }
    }
  }

  /// 成员离开房间
  public func onMemberLeaveRoom(members: [NERoomMember]) {
    var kMembers = [NEKaraokeMember]()
    for member in members {
      kMembers.append(NEKaraokeMember(member))
    }
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener,
              let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onMemberLeaveRoom(_:))) {
          listener.onMemberLeaveRoom?(kMembers)
        }
      }
    }
  }

  public func onMemberJoinChatroom(members: [NERoomMember]) {
    var kMembers = [NEKaraokeMember]()
    for member in members {
      kMembers.append(NEKaraokeMember(member))
    }
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener,
              let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onMemberJoinChatroom(_:))) {
          listener.onMemberJoinChatroom?(kMembers)
        }
      }
    }
  }

  public func onMemberLeaveChatroom(members: [NERoomMember]) {
    var kMembers = [NEKaraokeMember]()
    for member in members {
      kMembers.append(NEKaraokeMember(member))
    }
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener,
              let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onMemberLeaveChatroom(_:))) {
          listener.onMemberLeaveChatroom?(kMembers)
        }
      }
    }
  }

  /// 房间结束
  public func onRoomEnded(reason: NERoomEndReason) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onRoomEnded(_:))) {
          listener.onRoomEnded?(NEKaraokeEndReason(rawValue: reason.rawValue) ?? .unknow)
        }
      }
    }
  }

  /// Rtc频道错误
  public func onRtcChannelError(code: Int) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

        if listener.responds(to: #selector(NEKaraokeListener.onRtcChannelError(_:))) {
          listener.onRtcChannelError?(code)
        }
      }
    }
  }

  /// 本端音频输出设备变更通知，如切换到扬声器、听筒、耳机等
  public func onRtcAudioOutputDeviceChanged(device: NEAudioOutputDevice) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener
          .responds(to: #selector(NEKaraokeListener.onAudioOutputDeviceChanged(_:))) {
          listener
            .onAudioOutputDeviceChanged?(NEKaraokeAudioOutputDevice(rawValue: UInt(device
                .rawValue)) ?? .speakerPhone)
        }
      }
    }
  }

  /// 聊天室消息
  public func onReceiveChatroomMessages(messages: [NERoomChatMessage]) {
    for message in messages {
      switch message.messageType {
      case .text:
        if let msg = message as? NERoomChatTextMessage {
          handleTextMessage(msg)
        }
      case .custom:
        if let msg = message as? NERoomChatCustomMessage {
          handleCustomMessage(msg)
        }
      case .image: break
      case .file: break
      case .notification: break
      default: break
      }
    }
  }

  public func onMemberAudioMuteChanged(member: NERoomMember, mute: Bool,
                                       operateBy: NERoomMember?) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

        if listener
          .responds(to: #selector(NEKaraokeListener
              .onMemberAudioMuteChanged(_:mute:operateBy:))) {
          var item: NEKaraokeMember?
          if let operateBy = operateBy {
            item = NEKaraokeMember(operateBy)
          }
          listener.onMemberAudioMuteChanged?(
            NEKaraokeMember(member),
            mute: mute,
            operateBy: item
          )
        }
      }
    }
  }

  public func onRtcAudioEffectFinished(effectId: UInt32) {
    guard NEKaraokeKit.shared().currentSongIdForAudioEffect == effectId else { return }
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

        if listener.responds(to: #selector(NEKaraokeListener.onSongPlayingCompleted)) {
          listener.onSongPlayingCompleted?()
        }
      }
    }
  }

  public func onRtcAudioEffectTimestampUpdate(effectId: UInt32, timeStampMS: UInt64) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener
          .responds(to: #selector(NEKaraokeListener
              .onRtcAudioEffectTimestampUpdate(effectId:timeStampMS:))) {
          listener.onRtcAudioEffectTimestampUpdate?(
            effectId: effectId,
            timeStampMS: timeStampMS
          )
        }
      }
    }
  }
}

extension NEKaraokeKit {
  /// 处理文本消息
  func handleTextMessage(_ message: NERoomChatTextMessage) {
    NEKaraokeLog.infoLog(kitTag, desc: "Receive text message.")
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onReceiveTextMessage(_:))) {
          listener.onReceiveTextMessage?(NEKaraokeChatTextMessage(message))
        }
      }
    }
  }

  /// 处理RoomKit自定义消息
  func handleCustomMessage(_ message: NERoomChatCustomMessage) {
    NEKaraokeLog.infoLog(kitTag, desc: "Receive custom message.")
    guard let dic = message.attachStr?.toDictionary() else { return }
    if let cmd = dic["type"] as? Int {
      switch cmd {
      // 合唱消息
      case NEKaraokeChorusActionType.cancelInvite.rawValue ... NEKaraokeChorusActionType.next
        .rawValue:
        handleChorusMessage(cmd, data: dic)
      case NEKaraokePickSongActionType.pick.rawValue ... NEKaraokePickSongActionType
        .listChange.rawValue: // 点歌台
        handlePickSongMessage(cmd, data: dic)
      case BATCH_REWARD_TYPE:
        if let data = dic["data"] as? [String: Any],
           let jsonData = try? JSONSerialization.data(withJSONObject: data, options: []),
           let jsonString = String(data: jsonData, encoding: .utf8),
           let obj = NEKaraokeDecoder.decode(_NEKaraokeBatchRewardMessage.self, jsonString: jsonString) {
          handleBatchGiftMessage(obj)
        }
      default: break
      }
    }
  }

  /// 处理合唱消息
  func handleChorusMessage(_ cmd: Int, data: [String: Any]) {
    NEKaraokeLog.messageLog(kitTag, desc: "Handle chorus message. Cmd: \(cmd). Data: \(data)")
    guard let dic = data["data"] as? [String: Any] else { return }
    let actionType = NEKaraokeChorusActionType(rawValue: cmd) ?? .invite
    guard let songModel = NEKaraokeDecoder.decode(NEKaraokeSongModel.self, param: dic)
    else { return }
    // 内部逻辑处理
    audioPlayService?.handleChorusMessage(actionType, songModel: songModel)
    // 回调
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener
          .responds(to: #selector(NEKaraokeListener.onReceiveChorusMessage(_:songModel:))) {
          listener.onReceiveChorusMessage?(actionType, songModel: songModel)
        }
      }
    }
  }

  /// 处理批量礼物消息
  func handleBatchGiftMessage(_ rewardMsg: _NEKaraokeBatchRewardMessage) {
    guard
      let _ = rewardMsg.userUuid,
      let _ = rewardMsg.userName,
      let _ = rewardMsg.giftId,
      rewardMsg.targets.count > 0
    else {
      return
    }
    let giftModel = NEKaraokeBatchGiftModel(rewardMsg)
    NEKaraokeLog.messageLog(
      kitTag,
      desc: "Handle batch gift message. SendAccount: \(giftModel.sendAccout). SendNick: \(giftModel.rewarderUserName). GiftId: \(giftModel.giftId)."
    )
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
        if listener.responds(to: #selector(NEKaraokeListener.onReceiveBatchGift(giftModel:))) {
          listener.onReceiveBatchGift?(giftModel: giftModel)
        }
      }
    }
  }

  /// 处理点歌台消息
  func handlePickSongMessage(_ cmd: Int, data: [String: Any]) {
    NEKaraokeLog.messageLog(
      kitTag,
      desc: "Handle pick song message. Cmd: \(cmd). Data: \(data)"
    )
    // 列表变化特殊处理
    guard cmd != NEKaraokePickSongActionType.listChange.rawValue else {
      DispatchQueue.main.async {
        for pointerListener in self.listeners.allObjects {
          guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }
          if listener.responds(to: #selector(NEKaraokeListener.onSongListChanged)) {
            listener.onSongListChanged?()
          }
        }
      }
      return
    }
    guard let dic = data["data"] as? [String: Any] else { return }
    var temp = dic
    // 有些有两层
    if let data = dic["data"] as? [String: Any] {
      temp = data
    }
    let actionType = NEKaraokePickSongActionType(rawValue: cmd) ?? .pick
    let orderSongModel = NEKaraokeDecoder.decode(NEKaraokeOrderSongModel.self, param: temp)
    if let attachment = temp["attachment"] as? String {
      orderSongModel?.attachment = attachment
    }

    if let nextOrderSong = temp["nextOrderSong"] as? [String: Any] {
      orderSongModel?.nextOrderSong = NEKaraokeDecoder.decode(
        NEKaraokeOrderSongResult.self,
        param: nextOrderSong
      )
    }
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

        if actionType == NEKaraokePickSongActionType.pick {
          if listener.responds(to: #selector(NEKaraokeListener.onSongOrdered(_:))) {
            listener.onSongOrdered?(orderSongModel)
          }
        } else if actionType == NEKaraokePickSongActionType.cancelPick {
          if listener.responds(to: #selector(NEKaraokeListener.onSongDeleted(_:))) {
            listener.onSongDeleted?(orderSongModel)
          }
        } else if actionType == NEKaraokePickSongActionType.switchSong {
          if listener.responds(to: #selector(NEKaraokeListener.onNextSong(_:))) {
            listener.onNextSong?(orderSongModel)
          }
        } else if actionType == NEKaraokePickSongActionType.top {
          if listener.responds(to: #selector(NEKaraokeListener.onSongTopped(_:))) {
            listener.onSongTopped?(orderSongModel)
          }
        }
      }
    }
  }
}
