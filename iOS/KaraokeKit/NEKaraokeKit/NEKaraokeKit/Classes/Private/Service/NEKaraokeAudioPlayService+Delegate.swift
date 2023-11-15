// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

extension NEKaraokeAudioPlayService: NERoomRtcAudioFrameObserver {
  func onRecordingAudioFrame(_ audioFrame: NERoomRtcAudioFrame) {
//        guard let roomContex = self.roomContext else { return }
    if callback != nil {
      callback!.onRecordingAudioFrame(frame: audioFrame)
    }
    // 合唱者、串行合唱、是否混音前处理
    guard singer == .chorister,
          mode == .seaialChorus,
          beforeStartMix else { return }
    guard audioChannels == audioFrame.format.channels,
          samplesPerChannel == audioFrame.format.samplesPerChannel else { return }
    var availableBytes: UInt32 = 0
    let size = audioFrame.format.channels * audioFrame.format.bytesPerSample * audioFrame.format
      .samplesPerChannel
    // 取地址
    let place = withUnsafeMutablePointer(to: &availableBytes) { ptr in ptr }
    let buffer = NEKaraokeConversion.shared().circularBufferTail(withAvailableBytes: place)
    if availableBytes >= size {
      PlayerAudioMixer.mixAudioFrameData(
        audioFrame.data.bindMemory(to: Int16.self, capacity: 1),
        data2: buffer.bindMemory(to: Int16.self, capacity: 1),
        samplesPerChannel: Int32(audioFrame.format.samplesPerChannel),
        channels: Int32(audioFrame.format.channels)
      )
      NEKaraokeConversion.shared().circularBufferConsume(withAmount: Int32(size))
    }
  }

  func onPlaybackAudioFrameBeforeMixing(withUserId userId: String,
                                        audioFrame: NERoomRtcAudioFrame) {
    // 副唱、串行合唱
    guard singer == .chorister, mode == .seaialChorus else { return }
    // 判断主唱的UserId
    guard userId == anchorId else { return }

    let size = audioFrame.format.channels * audioFrame.format.bytesPerSample * audioFrame.format
      .samplesPerChannel
    NEKaraokeConversion.shared()
      .circularBufferProduceBytes(withSrc: audioFrame.data, len: Int32(size))
    audioChannels = audioFrame.format.channels
    samplesPerChannel = audioFrame.format.samplesPerChannel

    // 缓存5帧 然后处理
    if !beforeStartMix, NEKaraokeConversion.shared().bufferFillCount() >= 5 * size {
      beforeStartMix = true
    }
  }
}

extension NEKaraokeAudioPlayService: NERoomListener {
  func onRoomEnded(reason _: NERoomEndReason) {
    NEKaraokeKit.shared().leaveRoom()
  }

  /// 接收到SEI消息
  func onRtcReceiveSEIMessage(_ userUuid: String, message: Data) {
    guard let jsonObjc = NEKaraokeDecoder.decode(message) else { return }
    guard let musicPosition = jsonObjc[SEI_POS] as? UInt64 else { return }
    let orderId = jsonObjc[SEI_ORDER] as? Int64
    // 都有值且不对应的情况才忽略
    if orderId == currentOrderId || currentOrderId == nil || orderId == nil {
      // 副唱收到主唱的 SEI 同步自己的歌词，并发送自己的歌词进度
      if singer == .chorister {
        guard let data = SEI(musicPosition, orderId: currentOrderId).toData() else { return }
        if mode == .seaialChorus {
          roomContext?.rtcController.sendSEIMsg(data)
          callback?.onSongPlayPosition(musicPosition)
        }
      } else if singer == .audience {
        callback?.onSongPlayPosition(musicPosition)
      }
    }
  }
}

extension NEKaraokeAudioPlayService: NEMessageChannelListener {
  @objc
  func playTimerBlock() {
    guard let roomContext = roomContext else { return }
    // 进度
    let position = roomContext.rtcController
      .getEffectCurrentPosition(effectId: currentEffectId)
    if callback != nil {
      callback!.onSongPlayPosition(position)
    }
  }

  /// 接收自定义消息
  func onReceiveCustomMessage(message: NECustomMessage) {
    guard let dic = message.data.toDictionary() else { return }
    guard let roomContext = roomContext else { return }
    if message.commandId == 10001 {
      NEKaraokeLog.infoLog(kitTag, desc: "Receive anchor npt message.")
      if singer == .chorister {
        guard let ntp_time = dic["ntp_timestamp"] as? Int64,
              let type = dic["type"] as? Int else { return }
        let ntpTimestamp = roomContext.rtcController.getNtpTimeOffset()
        let localTimestamp = ntpTimestamp + ntp_time
        NEKaraokeLog.infoLog(
          kitTag,
          desc: "接收ntp.\nntp_time: \(ntp_time).\nLocalTimestamp: \(localTimestamp)"
        )

        if type == 0 { // 开始播放
          // 开启AEC模式
          roomContext.rtcController.setParameters(["key_audio_external_audio_mix": true])

          playOrginalAndAccompany(
            roomContext,
            orginalPath ?? "",
            accompanyPath ?? "",
            realTimeVolume ?? 15,
            timestamp: localTimestamp,
            type: .main,
            sendEnable: false
          )
          // 开启定时器
          if playTimer == nil {
            NEKaraokeLog.infoLog(kitTag, desc: "recv ntp and start play timer")
            playTimer = Timer(timeInterval: 0.1, target: self, selector: #selector(playTimerBlock), userInfo: nil, repeats: true)
            if playTimer != nil {
              RunLoop.current.add(playTimer!, forMode: .common)
              playTimer?.fire()
            }
          }
        } else if type == 1 { // 暂停
          let time = localTimestamp - Int64(NSDate().timeIntervalSince1970 * 1000)
          DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(time))) {
            self.pauseSong()
          }
        } else if type == 2 { // 恢复播放
          let time = localTimestamp - Int64(NSDate().timeIntervalSince1970 * 1000)
          DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(time))) {
            self.resumeSong()
          }
        }
      }
    }
  }
}

extension NEKaraokeAudioPlayService {
  /// 处理实时合唱 暂停、恢复
  func handleChorusMessage(_ actionType: NEKaraokeChorusActionType,
                           songModel: NEKaraokeSongModel) {
    guard let roomContext = roomContext else { return }
    // 只处理实时
    switch actionType {
    case .pauseSong: // 暂停
      if singer == .anchor { // 主唱
        switch mode {
        case .solo, .seaialChorus:
          pauseSong()
        case .realTimeChorus:
          // 1s 延迟处理
          handlePauseAndResume(roomContext, type: 1)
        default: break
        }
      }
    case .resumeSong: // 恢复
      if singer == .anchor {
        switch mode {
        case .solo, .seaialChorus:
          resumeSong()
        case .realTimeChorus:
          handlePauseAndResume(roomContext, type: 2)
        default: break
        }
      }
    case .endSong, .abandon:
      if !isAudience(songModel) {
        stopSong()
        finishedSong()
      }
      singer = .audience
    case .next:
      if !isAudience(songModel) {
        stopSong()
      }
      singer = songModel.chorusInfo?.userUuid == NEKaraokeKit.shared().localMember?
        .account ? .anchor : .audience
      currentOrderId = songModel.chorusInfo?.orderId
    default: break
    }
  }

  func handlePauseAndResume(_ roomContext: NERoomContext, type: Int) {
    let localTime = Int64(NSDate().timeIntervalSince1970 * 1000) + 1000
    let ntpTimestamp = roomContext.rtcController.getNtpTimeOffset()
    let time = localTime - ntpTimestamp
    DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(1000)) {
      if type == 1 {
        self.pauseSong()
      } else if type == 2 {
        self.resumeSong()
      }
    }
    NEKaraokeKit.shared().sendCustomMessage(chorusId ?? "",
                                            commandId: 10001,
                                            data: ["type": type, "ntp_timestamp": time]
                                              .prettyJSON) { code, msg, _ in
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully send NTP message to chorister.")
      } else {
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to send NTP message to chorister. Code: \(code). Msg: \(msg ?? "")"
        )
      }
    }
  }

  func isAudience(_ songModel: NEKaraokeSongModel) -> Bool {
    let userUuid = NEKaraokeKit.shared().localMember?.account
    guard userUuid == songModel.chorusInfo?.userUuid || userUuid == songModel.chorusInfo?.assistantUuid else {
      return true
    }
    return false
  }
}

extension NEKaraokeAudioPlayService: NERtcStatsListener {
  // 处理rtt
  func onRtcStats(stats: NERoomRtcStats) {
    let downRtt = stats.downRtt
    let upRtt = stats.upRtt
    if downRtt > 0, upRtt > 0 {
      rtt = convertRtt((downRtt + upRtt) / 2)
    } else if downRtt == 0, upRtt > 0 {
      rtt = convertRtt(upRtt)
    } else if downRtt > 0, upRtt == 0 {
      rtt = convertRtt(downRtt)
    }
  }

  // originRtt代表用户手机到rtc边缘节点一个来回的时间。除以2得到单向的时间。合唱双方连接的边缘节点，典型情况会经过1到3个中心节点，取中间值2个，每个节点之间的单向时间估计为10ms，得到总共节点之间耗时30ms，两个人评分，每人加15ms
  func convertRtt(_ originRtt: UInt64) -> UInt64 {
    (originRtt / UInt64(2.0)) + 15
  }
}
