// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
import NERtcSDK

enum NEKaraokeSinger {
  // 主唱
  case anchor
  // 合唱者
  case chorister
  // 观众
  case audience
}

/// K 歌模式
@objc
public enum NEKaraokeSongMode: Int {
  /// 独唱
  case solo
  /// 实时合唱
  case realTimeChorus
  /// 串行合唱
  case seaialChorus
  /// 智能模式 或根据主唱副唱当下环境智能选择串行合唱还是实时合唱
  case intelligencce
}

/// 播放状态的相关回调
protocol NEKaraokePlayStateChangeCallback {
  /// 播放进度
  func onSongPlayPosition(_ postion: UInt64)

  /// 音频采集数据
  func onRecordingAudioFrame(frame: NERoomRtcAudioFrame)
}

let SEI_POS: String = "pos"
let SEI_ORDER: String = "orderId"

class SEI {
  var dictionary = [String: Any]()
  init(_ timestamp: UInt64, orderId: Int64?) {
    dictionary[SEI_POS] = timestamp
    if let orderId = orderId {
      dictionary[SEI_ORDER] = orderId
    }
  }

  func toData() -> Data? {
    NEKaraokeDecoder.getJsonData(with: dictionary)
  }
}

/// 唱歌 相关操作接口
/// 独唱、合唱
class NEKaraokeAudioPlayService: NSObject {
  var playTimer: Timer?

  var currentOrderId: Int64?

  var currentEffectId: UInt32 {
    isOriginal ? NEKaraokeKit.OriginalEffectId : NEKaraokeKit.AccompanyEffectId
  }

  var roomContext: NERoomContext?
  var callback: NEKaraokePlayStateChangeCallback?
  // 歌唱者类型
  var singer: NEKaraokeSinger = .audience
  // 默认独唱
  var mode: NEKaraokeSongMode = .solo
  var roomUuid: String = ""
  // 主唱Id
  var anchorId: String = ""
  // 副唱Id
  var chorusId: String?
  // 本地原唱地址
  var orginalPath: String?
  // 本地伴唱地址
  var accompanyPath: String?
  // 实时合唱传入的音量
  var realTimeVolume: Int?
  /// 初始化
  /// - Parameter roomUuid: 房间id
  init(roomUuid: String) {
    super.init()
    self.roomUuid = roomUuid
    // 默认配置
    defaultConfig()
  }

  func defaultConfig() {
    roomContext = NERoomKit.shared().roomService.getRoomContext(roomUuid: roomUuid)
    // 设置NTP精准对齐
    roomContext?.rtcController.setStreamAlignmentProperty(true)
    roomContext?.addRoomListener(listener: self)
    NERoomKit.shared().messageChannelService.addMessageChannelListener(listener: self)
    roomContext?.addRtcStatsListener(listener: self)
    // 监听音频
    roomContext?.rtcController.setAudioFrame(withObserver: self)
    // 会前 设置场景
    NERtcEngine.shared().setChannelProfile(.highQualityChatroom)
      
    // 设置录制和播放声音混音后的数据格式
    let format = NERoomRtcAudioFrameRequestFormat()
    format.channels = 2
    format.sampleRate = 48000
    format.mode = .readonly
    roomContext?.rtcController.setMixedAudioFrameParameters(format)
    // 设置采集的音频格式
    let recordFormat = NERoomRtcAudioFrameRequestFormat()
    recordFormat.channels = 2
    recordFormat.sampleRate = 48000
    recordFormat.mode = .readwrite
    roomContext?.rtcController.setRecordingAudioFrameParameters(recordFormat)
  }

  /// 销毁
  func destroy() {
    finishedSong()
    NERoomKit.shared().messageChannelService.removeMessageChannelListener(listener: self)
    roomContext?.rtcController.setAudioFrame(withObserver: nil)
    roomContext?.removeRoomListener(listener: self)
    roomContext = nil
  }

  /// 开始k歌
  /// - Parameters:
  ///   - orginalPath: 原唱本地路径
  ///   - accompanyPath: 伴奏本地路径
  ///   - volume: 默认音量
  func startSong(originalPath: String,
                 accompanyPath: String,
                 volume: Int,
                 anchorId: String,
                 choristerId: String?,
                 startTimeStamp: Int64,
                 anchor: Bool,
                 mode: NEKaraokeSongMode,
                 callback: NEKaraokeCallback<AnyObject>? = nil) {
    guard let roomContext = roomContext else {
      NEKaraokeLog.infoLog(kitTag, desc: "Failed to play music. RoomContext is nil.")
      callback?(NEKaraokeErrorCode.failed, nil, nil)
      return
    }
    isOriginal = false
    singer = anchor ? .anchor : .chorister
    self.mode = mode

    // 实时合唱，主唱不订阅副唱SEI，副唱不订阅主唱SEI
    if mode == .realTimeChorus,
       singer != .audience {
      if singer == .anchor, let choristerId = choristerId {
        roomContext.rtcController.unsubscribeRemoteVideoStream(
          userUuid: choristerId,
          streamType: .low
        )
      } else if singer == .chorister {
        roomContext.rtcController.unsubscribeRemoteVideoStream(
          userUuid: anchorId,
          streamType: .low
        )
      }
    }

    switch mode {
    case .solo: // 独唱
      let code = startSolo(
        roomContext,
        orginalPath: originalPath,
        accompanyPath: accompanyPath,
        volume: volume
      )
      callback?(code, nil, nil)
    case .seaialChorus: // 串行
      let code = startSerialChorus(
        roomContext,
        orginalPath: originalPath,
        accompanyPath: accompanyPath,
        anchorId: anchorId,
        choristerId: choristerId ?? "",
        volume: volume
      )
      callback?(code, nil, nil)
    case .realTimeChorus: // 实时
      let code = realTimeChorus(
        roomContext,
        orginalPath: originalPath,
        accompanyPath: accompanyPath,
        volume: volume,
        timestamp: startTimeStamp,
        anchorId: anchorId,
        chorusId: choristerId ?? ""
      )
      callback?(code, nil, nil)
    default: // 智能合唱
      break
    }
  }

  /// 暂停歌曲
  @discardableResult
  func pauseSong() -> Int {
    roomContext?.rtcController.pauseAllEffects() ?? NEKaraokeErrorCode.failed
  }

  /// 恢复播放
  @discardableResult
  func resumeSong() -> Int {
    roomContext?.rtcController.resumeAllEffects() ?? NEKaraokeErrorCode.failed
  }

  /// 停止播放
  @discardableResult
  func stopSong() -> Int {
    roomContext?.rtcController.stopAllEffects() ?? NEKaraokeErrorCode.failed
  }

  /// 状态重置
  func finishedSong() {
    NEKaraokeLog.infoLog(kitTag, desc: "finishedSong")
    playTimer?.invalidate()
    playTimer = nil
    if singer == .anchor {
      // 关闭AEC模式
      NERtcEngine.shared().setChannelProfile(.highQualityChatroom)
      if mode == .seaialChorus { // 主唱 - 串行
        if let chorusId = chorusId {
          roomContext?.rtcController.subscribeRemoteAudio(userUuid: chorusId)
        }
        roomContext?.rtcController.setAudioSubscribeOnlyBy([])
      } else if mode == .realTimeChorus { // 主唱 - 实时
        roomContext?.rtcController.setParameters(["engine.audio.ktv.chrous": false])

        roomContext?.rtcController.disableLocalSubstreamAudio()
        if let chorusId = chorusId {
          roomContext?.rtcController.subscribeRemoteVideoStream(
            userUuid: chorusId,
            streamType: .low
          )
        }
      }
    } else if singer == .chorister {
      if mode == .realTimeChorus { // 副唱 - 实时
        roomContext?.rtcController.setParameters(["engine.audio.ktv.chrous": false])
          NERtcEngine.shared().setChannelProfile(.highQualityChatroom)
        // 不订阅主唱音频辅流
        roomContext?.rtcController.subscribeRemoteAudioSubStream(anchorId)
        roomContext?.rtcController.subscribeRemoteVideoStream(
          userUuid: anchorId,
          streamType: .low
        )
      }
    }
  }

  var isOriginal: Bool = false

  /// 切换原声/伴奏
  /// 默认 false 伴奏
  /// - Parameter isOriginal: true : 原声, false : 伴奏
  func switchToOriginalVolume(isOriginal: Bool) {
    self.isOriginal = isOriginal
    let originalVolume: UInt32 = roomContext?.rtcController
      .getEffectSendVolume(effectId: NEKaraokeKit.OriginalEffectId) ?? 15
    let accompanyVolume: UInt32 = roomContext?.rtcController
      .getEffectSendVolume(effectId: NEKaraokeKit.AccompanyEffectId) ?? 15
    var currentVolume: UInt32 = 15
    if isOriginal {
      if accompanyVolume > 0 {
        currentVolume = accompanyVolume
      }
      roomContext?.rtcController.setEffectSendVolume(
        effectId: NEKaraokeKit.OriginalEffectId,
        volume: currentVolume
      )
      roomContext?.rtcController.setEffectSendVolume(effectId: NEKaraokeKit.AccompanyEffectId, volume: 0)

      roomContext?.rtcController.setEffectPlaybackVolume(
        effectId: NEKaraokeKit.OriginalEffectId,
        volume: currentVolume
      )
      roomContext?.rtcController.setEffectPlaybackVolume(effectId: NEKaraokeKit.AccompanyEffectId, volume: 0)
    } else {
      if originalVolume > 0 {
        currentVolume = originalVolume
      }
      roomContext?.rtcController.setEffectSendVolume(effectId: NEKaraokeKit.OriginalEffectId, volume: 0)
      roomContext?.rtcController.setEffectSendVolume(
        effectId: NEKaraokeKit.AccompanyEffectId,
        volume: currentVolume
      )

      roomContext?.rtcController.setEffectPlaybackVolume(effectId: NEKaraokeKit.OriginalEffectId, volume: 0)
      roomContext?.rtcController.setEffectPlaybackVolume(
        effectId: NEKaraokeKit.AccompanyEffectId,
        volume: currentVolume
      )
    }
  }

  /// 设置本地伴音播放音量
  /// - Parameter volume: 0～100取值，默认100
  func setPlaybackVolume(volume: UInt32) {
    roomContext?.rtcController.setEffectPlaybackVolume(
      effectId: currentEffectId,
      volume: volume
    )
  }

  /// 设置伴音发送音量
  /// - Parameter volume: 0～100取值，默认100
  func setSendVolume(volume: UInt32) {
    roomContext?.rtcController.setEffectSendVolume(effectId: currentEffectId, volume: volume)
  }

  func getEffectDuration() -> UInt64 {
    roomContext?.rtcController.getEffectDuration(effectId: currentEffectId) ?? 0
  }

  func setEffectPosition(position: UInt64) -> Int {
    roomContext?.rtcController.setEffectPosition(effectId: NEKaraokeKit.OriginalEffectId, postion: position)
    return roomContext?.rtcController.setEffectPosition(
      effectId: NEKaraokeKit.AccompanyEffectId,
      postion: position
    ) ?? -1
  }

  private func solo(path _: String, startTimeStamp _: Int) {}

  private func getLocalStartTime(receiveServerTime _: Int, countdownTime _: Int = 0) -> Int {
    0
  }

  var audioChannels: UInt32 = 0
  var samplesPerChannel: UInt32 = 0
  var beforeStartMix: Bool = false
  var rtt: UInt64 = 0
  var roomMembers: [NERoomMember] {
    roomContext?.remoteMembers ?? []
  }
}
