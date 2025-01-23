// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
import NERtcSDK

/// 唱歌模式扩展
extension NEKaraokeAudioPlayService {
  @objc
  func timerSendSEI() {
    let position = roomContext?.rtcController
      .getEffectCurrentPosition(effectId: currentEffectId)
    if callback != nil, position != nil {
      callback!.onSongPlayPosition(position!)
    }
    // 发送SEI
    if let data = SEI(position ?? 0, orderId: currentOrderId).toData() {
      roomContext?.rtcController.sendSEIMsg(data)
    }
  }

  @discardableResult
  func playOrginalAndAccompany(_ roomContext: NERoomContext,
                               _ orginalPath: String,
                               _ accompanyPath: String,
                               _ volume: Int,
                               timestamp: Int64 = 0,
                               type: NERoomAudioStreamType = .main,
                               sendEnable: Bool = true) -> Int {
    roomContext.rtcController.stopAllEffects()
    if !accompanyPath.isEmpty,
       orginalPath.isEmpty {
      // 只有伴奏
      let aOption = NERoomCreateAudioEffectOption()
      aOption.startTimeStamp = timestamp
      aOption.path = accompanyPath
      aOption.playbackVolume = volume
      aOption.sendVolume = volume
      aOption.sendEnabled = sendEnable
      aOption.sendWithAudioType = type
      let aCode = roomContext.rtcController.playEffect(effectId: NEKaraokeKit.AccompanyEffectId, option: aOption)
      isOriginal = false
      return (aCode != 0) ? -1 : 0
    } else if accompanyPath.isEmpty,
              !orginalPath.isEmpty {
      // 只有原唱
      let aOption = NERoomCreateAudioEffectOption()
      aOption.startTimeStamp = timestamp
      aOption.path = orginalPath
      aOption.playbackVolume = volume
      aOption.sendVolume = volume
      aOption.sendEnabled = sendEnable
      aOption.sendWithAudioType = type
      let aCode = roomContext.rtcController.playEffect(effectId: NEKaraokeKit.OriginalEffectId, option: aOption)
      isOriginal = true
      return (aCode != 0) ? -1 : 0
    } else if !accompanyPath.isEmpty,
              !orginalPath.isEmpty {
      // 原唱伴奏都有，优先使用伴奏
      let aOption = NERoomCreateAudioEffectOption()
      aOption.startTimeStamp = timestamp
      aOption.path = accompanyPath
      aOption.playbackVolume = volume
      aOption.sendVolume = volume
      aOption.sendEnabled = sendEnable
      aOption.sendWithAudioType = type
      let aCode = roomContext.rtcController.playEffect(effectId: NEKaraokeKit.AccompanyEffectId, option: aOption)
      let oOption = NERoomCreateAudioEffectOption()
      oOption.startTimeStamp = timestamp
      oOption.path = orginalPath
      oOption.playbackVolume = aCode == 0 ? 0 : volume // 如果伴奏播放失败则播放原唱
      oOption.sendVolume = aCode == 0 ? 0 : volume
      oOption.sendEnabled = sendEnable
      oOption.sendWithAudioType = type
      let oCode = roomContext.rtcController.playEffect(effectId: NEKaraokeKit.OriginalEffectId, option: oOption)
      if aCode == 0 {
        isOriginal = false
      } else {
        isOriginal = true
      }
      // 两个失败才算失败
      return (aCode != 0 && oCode != 0) ? -1 : 0
    } else {
      // 原唱伴奏都没有
      return -1
    }
  }

  // 独唱
  func startSolo(_ roomContext: NERoomContext,
                 orginalPath: String,
                 accompanyPath: String,
                 volume: Int) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "startSolo.")
    NERtcEngine.shared().setChannelProfile(.karaoke)

    let code = playOrginalAndAccompany(roomContext, orginalPath, accompanyPath, volume)
    if code == 0 {
      NEKaraokeLog.successLog(kitTag, desc: "Successfully play music. start play timer")
      if playTimer == nil {
        playTimer = Timer(timeInterval: 0.02, target: self, selector: #selector(timerSendSEI), userInfo: nil, repeats: true)
        if playTimer != nil {
          RunLoop.current.add(playTimer!, forMode: .common)
          playTimer?.fire()
        }
      }
    } else {
      roomContext.rtcController.stopAllEffects()
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to play music. Code: \(code).")
    }
    return code
  }

  /// 串行合唱
  func startSerialChorus(_ roomContext: NERoomContext,
                         orginalPath: String,
                         accompanyPath: String,
                         anchorId: String,
                         choristerId: String,
                         volume: Int) -> Int {
    NEKaraokeLog.infoLog(kitTag, desc: "startSerialChorus.")
    // 保存 主唱、副唱Id
    self.anchorId = anchorId
    chorusId = choristerId
    if singer == .anchor { // 主唱
      // 开启AEC模式
      NERtcEngine.shared().setChannelProfile(.karaoke)

      let code = playOrginalAndAccompany(roomContext, orginalPath, accompanyPath, volume)
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully anchor serial chorus. start play timer")
        roomContext.rtcController.unsubscribeRemoteAudio(userUuid: choristerId)
        roomContext.rtcController.setAudioSubscribeOnlyBy([choristerId])
        // 开启定时器
        if playTimer == nil {
          playTimer = Timer(timeInterval: 0.1, target: self, selector: #selector(timerSendSEI), userInfo: nil, repeats: true)
          if playTimer != nil {
            RunLoop.current.add(playTimer!, forMode: .common)
            playTimer?.fire()
          }
        }
      } else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to anchor serial chorus. Code: \(code)."
        )
      }
      return code
    } else { // 副唱
      return 0
    }
  }

  /// 实时合唱
  func realTimeChorus(_ roomContext: NERoomContext,
                      orginalPath: String,
                      accompanyPath: String,
                      volume: Int,
                      timestamp: Int64 = 0,
                      anchorId: String,
                      chorusId: String) -> Int {
    self.anchorId = anchorId
    self.chorusId = chorusId
    self.orginalPath = orginalPath
    self.accompanyPath = accompanyPath
    realTimeVolume = volume
    NEKaraokeLog.infoLog(kitTag, desc: "Start realTime chorus.")
    if singer == .anchor { // 主唱
      /// 开启低延时
      roomContext.rtcController.setParameters(["engine.audio.ktv.chrous": true])
      NERtcEngine.shared().setChannelProfile(.karaoke)

      roomContext.rtcController.enableLocalSubstreamAudio()

      let localTime = Int64(NSDate().timeIntervalSince1970 * 1000) + timestamp
      let code = playOrginalAndAccompany(
        roomContext,
        orginalPath,
        accompanyPath,
        volume,
        timestamp: localTime,
        type: .sub
      )
      if code == 0 {
        let ntpTimestamp = roomContext.rtcController.getNtpTimeOffset()
        let time = localTime - ntpTimestamp
        NEKaraokeKit.shared().sendCustomMessage(chorusId,
                                                commandId: 10001,
                                                data: ["ntp_timestamp": time, "type": 0]
                                                  .prettyJSON) { code, msg, _ in
          if code == 0 {
            NEKaraokeLog.successLog(
              kitTag,
              desc: "Successfully send custom messge to chorister."
            )
          } else {
            NEKaraokeLog.errorLog(
              kitTag,
              desc: "Failed to send custom messge to chorister. Code: \(code). Msg: \(msg ?? "")"
            )
          }
        }
        NEKaraokeLog.infoLog(kitTag, desc: "Start realTime chorus and start play timer")
        // 开启定时器
        if playTimer == nil {
          playTimer = Timer(timeInterval: 0.1, target: self, selector: #selector(timerSendSEI), userInfo: nil, repeats: true)
          if playTimer != nil {
            RunLoop.current.add(playTimer!, forMode: .common)
            playTimer?.fire()
          }
        }
      } else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to anchor realTime chorus. Code: \(code)."
        )
      }
      return code
    } else { // 副唱
      NEKaraokeLog.infoLog(kitTag, desc: "实时副唱. anchorId: \(anchorId)")
      /// 开启低延时
      self.roomContext?.rtcController.setParameters(["engine.audio.ktv.chrous": true])
      NERtcEngine.shared().setChannelProfile(.karaoke)
        
      // 不订阅主唱音频辅流
      self.roomContext?.rtcController.unsubscribeRemoteAudioSubStream(anchorId)

      return 0
    }
  }
}
