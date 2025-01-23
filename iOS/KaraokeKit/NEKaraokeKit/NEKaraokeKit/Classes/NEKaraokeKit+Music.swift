// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import AVFAudio
import Foundation

/// 歌曲相关接口
public extension NEKaraokeKit {
  /// 开始唱歌
  /// - Parameters:
  ///   - orderId: 点歌台id
  ///   - chorusId: 合唱id
  ///   - ext: 额外参数
  ///   - callback: 回调
  func requestPlaySong(orderId: Int64,
                       chorusId: String?,
                       ext: [String: Any]? = nil,
                       callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Start song.")
    Judge.preCondition({
      self.musicService!.start(orderId: orderId,
                               chorusId: chorusId,
                               ext: ext) {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully start song.")
        callback?(NEKaraokeErrorCode.success, nil, nil)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to start song. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 是否插入耳机
  internal func isHeadSetPlugging() -> Bool {
    let route = AVAudioSession.sharedInstance().currentRoute
    var isHead = false
    for desc in route.outputs {
      switch desc.portType {
      case .headphones, .bluetoothA2DP, .usbAudio:
        isHead = true
      default: break
      }
    }
    return isHead
  }

  /// 邀请合唱
  /// - Parameters:
  ///   - orderId: 点歌编号
  ///   - callback: 回调
  func inviteChorus(orderId: Int64,
                    callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Invite chorus. OrderId: \(orderId)")

    Judge.preCondition({
      let deviceParam = _NEKaraokeDeviceParam(
        rtt: self.audioPlayService!.rtt,
        wiredHeadset: self.isHeadSetPlugging() ? 1 : 0
      )
      self.musicService!.invitation(orderId,
                                    deviceParam: deviceParam,
                                    success: { data in
                                      guard let data = data else {
                                        NEKaraokeLog.errorLog(
                                          kitTag,
                                          desc: "Failed to invite chorus. Data structure error."
                                        )
                                        callback?(
                                          NEKaraokeErrorCode.failed,
                                          "Data structure error",
                                          nil
                                        )
                                        return
                                      }
                                      NEKaraokeLog.successLog(
                                        kitTag,
                                        desc: "Successfully invite chorus."
                                      )
                                      callback?(NEKaraokeErrorCode.success, nil, data)
                                    }, failure: { error in
                                      NEKaraokeLog.errorLog(
                                        kitTag,
                                        desc: "Failed to invite chorus. Code: \(error.code). Msg: \(error.localizedDescription)"
                                      )
                                      callback?(error.code, error.localizedDescription, nil)
                                    })
    }, failure: callback)
  }

  /// 取消合唱邀请
  /// - Parameters:
  ///   - chorusId: 合唱Id
  ///   - callback: 回调
  func cancelInviteChorus(_ chorusId: String,
                          callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Cancel invite chorus. ChorusId: \(chorusId)")
    Judge.preCondition({
      self.musicService!.invitation(action: .cancel,
                                    chorusId: chorusId,
                                    success: { data in
                                      guard let data = data else {
                                        NEKaraokeLog.errorLog(
                                          kitTag,
                                          desc: "Failed to cancel invite chorus. Data structure error."
                                        )
                                        callback?(
                                          NEKaraokeErrorCode.failed,
                                          "Data structure error",
                                          nil
                                        )
                                        return
                                      }
                                      NEKaraokeLog.successLog(
                                        kitTag,
                                        desc: "Successfully cancel invite chorus."
                                      )
                                      callback?(NEKaraokeErrorCode.success, nil, data)
                                    }, failure: { error in
                                      NEKaraokeLog.errorLog(
                                        kitTag,
                                        desc: "Failed to cancel invite chorus. Code: \(error.code). Msg: \(error.localizedDescription)"
                                      )
                                      callback?(error.code, error.localizedDescription, nil)
                                    })
    }, failure: callback)
  }

  /// 拒绝合唱
  /// - Parameters:
  ///   - chorusId: 合唱Id
  ///   - callback: 回调
  func rejectInviteChorus(_ chorusId: String,
                          callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Reject invite chorus. ChorusId: \(chorusId)")

    Judge.preCondition({
      self.musicService!.invitation(action: .reject,
                                    chorusId: chorusId,
                                    success: { data in
                                      guard let data = data else {
                                        NEKaraokeLog.errorLog(
                                          kitTag,
                                          desc: "Failed to reject invite chorus. Data structure error."
                                        )
                                        callback?(
                                          NEKaraokeErrorCode.failed,
                                          "Data structure error",
                                          nil
                                        )
                                        return
                                      }
                                      NEKaraokeLog.successLog(
                                        kitTag,
                                        desc: "Successfully reject invite chorus."
                                      )
                                      callback?(NEKaraokeErrorCode.success, nil, data)
                                    }, failure: { error in
                                      NEKaraokeLog.errorLog(
                                        kitTag,
                                        desc: "Failed to reject invite chorus. Code: \(error.code). Msg: \(error.localizedDescription)"
                                      )
                                      callback?(error.code, error.localizedDescription, nil)
                                    })
    }, failure: callback)
  }

  /// 加入合唱
  /// - Parameters:
  ///   - chorusId: 合唱Id
  ///   - callback: 回调
  func joinChorus(chorusId: String,
                  callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Join chorus. ChorusId: \(chorusId)")

    Judge.preCondition({
      let deviceParam = _NEKaraokeDeviceParam(
        rtt: self.audioPlayService!.rtt,
        wiredHeadset: self.isHeadSetPlugging() ? 1 : 0
      )
      self.musicService!.invitation(action: .agree,
                                    chorusId: chorusId,
                                    deviceParam: deviceParam,
                                    success: { data in
                                      guard let data = data else {
                                        NEKaraokeLog.errorLog(
                                          kitTag,
                                          desc: "Failed to join chorus. Data structure error."
                                        )
                                        callback?(
                                          NEKaraokeErrorCode.failed,
                                          "Data structure error",
                                          nil
                                        )
                                        return
                                      }
                                      NEKaraokeLog.successLog(
                                        kitTag,
                                        desc: "Successfully join chorus."
                                      )
                                      callback?(NEKaraokeErrorCode.success, nil, data)
                                    }, failure: { error in
                                      NEKaraokeLog.errorLog(
                                        kitTag,
                                        desc: "Failed to join chorus. Code: \(error.code). Msg: \(error.localizedDescription)"
                                      )
                                      callback?(error.code, error.localizedDescription, nil)
                                    })
    }, failure: callback)
  }

  /// 已准备完成
  /// - Parameters:
  ///   - chorusId: 合唱id
  ///   - callback: 回调
  func chorusReady(chorusId: String,
                   callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Prepared. ChorusId: \(chorusId).")

    Judge.preCondition({
      self.musicService!.ready(chorusId,
                               success: { data in
                                 guard let data = data else {
                                   NEKaraokeLog.errorLog(
                                     kitTag,
                                     desc: "Failed to prepared. Data structure error."
                                   )
                                   callback?(
                                     NEKaraokeErrorCode.failed,
                                     "Data structure error",
                                     nil
                                   )
                                   return
                                 }
                                 NEKaraokeLog.successLog(
                                   kitTag,
                                   desc: "Successfully prepared."
                                 )
                                 callback?(NEKaraokeErrorCode.success, nil, data)
                               }, failure: { error in
                                 NEKaraokeLog.errorLog(
                                   kitTag,
                                   desc: "Failed to prepared. Code: \(error.code). Msg: \(error.localizedDescription)"
                                 )
                                 callback?(error.code, error.localizedDescription, nil)
                               })
    }, failure: callback)
  }

  /// 暂停歌曲
  /// - Parameter callback: 回调
  func requestPausePlayingSong(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Pause song.")

    Judge.preCondition({
      self.musicService!.operation(.pause,
                                   success: {
                                     NEKaraokeLog.successLog(
                                       kitTag,
                                       desc: "Successfully pause song."
                                     )
                                     callback?(NEKaraokeErrorCode.success, nil, nil)
                                   }, failure: { error in
                                     NEKaraokeLog.errorLog(
                                       kitTag,
                                       desc: "Failed to pause song. Code: \(error.code). Msg: \(error.localizedDescription)"
                                     )
                                     callback?(error.code, error.localizedDescription, nil)
                                   })
    }, failure: callback)
  }

  /// 恢复演唱
  /// - Parameter callback: 回调
  func requestResumePlayingSong(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Resume song.")

    Judge.preCondition({
      self.musicService!.operation(.resume,
                                   success: {
                                     NEKaraokeLog.successLog(
                                       kitTag,
                                       desc: "Successfully resume song."
                                     )
                                     callback?(NEKaraokeErrorCode.success, nil, nil)
                                   }, failure: { error in
                                     NEKaraokeLog.errorLog(
                                       kitTag,
                                       desc: "Failed to resume song. Code: \(error.code). Msg: \(error.localizedDescription)"
                                     )
                                     callback?(error.code, error.localizedDescription, nil)
                                   })
    }, failure: callback)
  }

  /// 停止演唱
  /// - Parameter callback: 回调
  func requestStopPlayingSong(_ callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Stop song.")
    Judge.preCondition({
      // 避免SEI的时序问题，所以调用接口就暂停playTimer
      self.audioPlayService?.playTimer?.fireDate = Date.distantFuture
      self.musicService!.operation(.stop,
                                   success: {
                                     NEKaraokeLog.successLog(
                                       kitTag,
                                       desc: "Successfully stop song."
                                     )
                                     self.audioPlayService?.stopSong()
                                     self.audioPlayService?.finishedSong()
                                     callback?(NEKaraokeErrorCode.success, nil, nil)
                                   }, failure: { error in
                                     NEKaraokeLog.errorLog(
                                       kitTag,
                                       desc: "Failed to stop song. Code: \(error.code). Msg: \(error.localizedDescription)"
                                     )
                                     // 停止失败的情况下恢复SEI发送
                                     self.audioPlayService?.playTimer?.fireDate = Date()
                                     callback?(error.code, error.localizedDescription, nil)
                                   })
    }, failure: callback)
  }

  /// 放弃演唱
  /// - Parameters:
  ///   - orderId: 点歌编号
  ///   - callback: 回调
  func abandonSong(orderId: Int64,
                   callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Abandon song.")
    Judge.preCondition({
      self.musicService!.abandon(orderId) {
        callback?(NEKaraokeErrorCode.success, nil, nil)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to abandon song. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 切歌
  /// - Parameters:
  ///   - orderId: 点歌编号
  ///   - callback: 回调
  func nextSong(orderId: Int64,
                callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Next song.")
    Judge.preCondition({
      // 避免SEI的时序问题，所以调用接口就暂停playTimer
      self.audioPlayService?.playTimer?.fireDate = Date.distantFuture
      self.musicService!.switchSong(orderId) {
        self.audioPlayService?.stopSong()
        self.audioPlayService?.finishedSong()
        callback?(NEKaraokeErrorCode.success, nil, nil)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to switch song. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        // 停止失败的情况下恢复SEI发送
        self.audioPlayService?.playTimer?.fireDate = Date()
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 获取当前房间内歌曲信息
  /// - Parameter callback: 回调
  func requestPlayingSongInfo(_ callback: NEKaraokeCallback<NEKaraokeSongInfoModel>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get current song info.")
    Judge.preCondition({
      self.musicService!.currentInfo { data in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully get current song info.")
        callback?(NEKaraokeErrorCode.success, nil, data)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to get current song info. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 切换伴奏和原声
  /// - Parameter enableAccompniment: 是否是伴奏
  /// - Returns: 0代表成功，否则失败
  @discardableResult
  func switchAccompaniment(_ enableAccompniment: Bool) -> Int {
    NEKaraokeLog.apiLog(kitTag, desc: "Switch accompaniment. Enable: \(enableAccompniment).")
    Judge.condition {
      self.audioPlayService?.switchToOriginalVolume(isOriginal: !enableAccompniment)
    }
    return 0
  }

  /// 是否是原唱
  var isOriginalSongPlaying: Bool {
    if let song = _audioPlayService {
      return song.isOriginal
    } else {
      return false
    }
  }

  /// 调音台使用的音效Id
  var currentSongIdForAudioEffect: Int {
    if let song = _audioPlayService {
      return Int(song.currentEffectId)
    }
    return -1
  }

  /// 点歌台接口
  /// 点歌
  func orderSong(_ songinfo: NEKaraokeOrderSongParams,
                 callback: NEKaraokeCallback<NEKaraokeOrderSongResult>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Order Song")
    Judge.preCondition({
      self.musicService!.orderSong(songInfo: songinfo) { data in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully orderSong")
        callback?(NEKaraokeErrorCode.success, nil, data)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to orderSong. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 获取已点列表
  func getOrderedSongs(callback: NEKaraokeCallback<[NEKaraokeOrderSongResult]>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Get Ordered Songs")
    Judge.preCondition({
      self.musicService!.getOrderedSongs { data in
        NEKaraokeLog.successLog(kitTag, desc: "Successfully getOrderedSongs")
        callback?(NEKaraokeErrorCode.success, nil, data)
      } failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to getOrderedSongs. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  /// 删除歌曲
  func deleteSong(orderId: Int64, callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Delete Song")
    Judge.preCondition({
      self.deletingSongs.append(orderId)
      self.musicService?.deleteSong(orderId, {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully deleteSong")
        self.deletingSongs.removeAll(where: { $0 == orderId })
        callback?(NEKaraokeErrorCode.success, nil, nil)
      }, failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to deleteSong. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        self.deletingSongs.removeAll(where: { $0 == orderId })
        callback?(error.code, error.localizedDescription, nil)

      })
    }, failure: callback)
  }

  /// 置顶歌曲
  func topSong(orderId: Int64, callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Top Song")
    Judge.preCondition({
      self.musicService?.topSong(orderId, {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully topSong")
        callback?(NEKaraokeErrorCode.success, nil, nil)
      }, failure: { error in
        NEKaraokeLog.errorLog(
          kitTag,
          desc: "Failed to topSong. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)

      })
    }, failure: callback)
  }
}
