// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

class NEKaraokeMusicService {
  var roomUuid: String
  var liveRecordId: UInt64

  init(_ liveRecordId: UInt64, roomUuid: String) {
    self.liveRecordId = liveRecordId
    self.roomUuid = roomUuid
  }

  /// 邀请接口
  /// - Parameters:
  ///   - orderId: 点歌编号，action=1 时必传
  ///   - action: 操作类型，1：邀请， 2：同意， 3：拒绝，4：取消
  ///   - chorusId: 合唱id，当action=2 3 4 时 必传
  ///   - anchorUserUuid: 目标用户编号(被邀请者)
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func invitation(_ orderId: Int64? = nil,
                  action: NEKaraokeInviteAction = .invite,
                  chorusId: String? = nil,
                  anchorUserUuid: String? = nil,
                  deviceParam: _NEKaraokeDeviceParam? = nil,
                  success: ((NEKaraokeSongInfoModel?) -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    var params: [String: Any] = [:]
    if let orderId = orderId { params["orderId"] = orderId }
    if let chorusId = chorusId { params["chorusId"] = chorusId }
    if let anchorUserUuid = anchorUserUuid { params["anchorUserUuid"] = anchorUserUuid }
    if let deviceParam = deviceParam,
       let dic = NEKaraokeEncoder.encoder(toDictionry: deviceParam) {
      params["deviceParam"] = dic
    }
    params["roomUuid"] = roomUuid
    switch action {
    case .invite:
      NEAPI.Music.invite().request(params,
                                   returnType: NEKaraokeSongInfoModel.self,
                                   success: success,
                                   failed: failure)
    case .agree:
      NEAPI.Music.join().request(params,
                                 returnType: NEKaraokeSongInfoModel.self,
                                 success: success,
                                 failed: failure)
    case .cancel:
      NEAPI.Music.cancelInvite().request(params,
                                         returnType: NEKaraokeSongInfoModel.self,
                                         success: success,
                                         failed: failure)
    case .reject: break
    default: break
    }
  }

  /// 合唱准备完成接口
  /// - Parameters:
  ///   - chorusId: 合唱编号
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func ready(_ chorusId: String,
             success: ((NEKaraokeSongInfoModel?) -> Void)? = nil,
             failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "chorusId": chorusId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Music.ready().request(params,
                                returnType: NEKaraokeSongInfoModel.self,
                                success: success,
                                failed: failure)
  }

  /// 开始演唱
  /// - Parameters:
  ///   - orderId: 点歌编号
  ///   - chorusId: 合唱编号，存在时即可不传其他信息
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func start(orderId: Int64,
             chorusId: String?,
             ext: [String: Any]? = nil,
             success: (() -> Void)? = nil,
             failure: ((NSError) -> Void)? = nil) {
    var params: [String: Any] = [
      "orderId": orderId,
      "roomUuid": roomUuid,
    ]
    if let chorusId = chorusId { params["chorusId"] = chorusId }
    if let ext = ext { params["ext"] = ext }

    NEAPI.Music.start().request(params, success: { _ in
      success?()
    }, failed: failure)
  }

  /// 歌曲操作 （暂停/继续播放/结束）
  /// - Parameters:
  ///   - action: 歌曲操作，0: 暂停  1：继续播放   2：结束
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func operation(_ action: _NEKaraokeMusicOperationType,
                 success: (() -> Void)? = nil,
                 failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "action": action.rawValue,
      "roomUuid": roomUuid,
    ]
    NEAPI.Music.action().request(params, success: { _ in
      success?()
    }, failed: failure)
  }

  func abandon(_ orderId: Int64,
               success: (() -> Void)? = nil,
               failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "orderId": orderId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Music.abandon().request(params,
                                  success: { _ in
                                    success?()
                                  }, failed: failure)
  }

  /// 获取房间当前演唱信息
  /// - Parameters:
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func currentInfo(_ success: ((NEKaraokeSongInfoModel?) -> Void)? = nil,
                   failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "roomUuid": roomUuid,
    ]
    NEAPI.Music.info().request(params, returnType: NEKaraokeSongInfoModel.self,
                               success: success,
                               failed: failure)
  }

  func switchSong(_ orderId: Int64,
                  success: (() -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "currentOrderId": orderId,
      "liveRecordId": liveRecordId,
    ]
    NEAPI.PickSong.switchSong().request(params,
                                        success: { _ in
                                          success?()
                                        }, failed: failure)
  }

  /// 点歌
  /// - Parameters:
  /// - success: 成功回调
  /// - failure: 失败回调
  func orderSong(songInfo: NEKaraokeOrderSongParams,
                 _ success: ((NEKaraokeOrderSongResult?) -> Void)? = nil,
                 failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "songId": songInfo.songId,
      "channel": songInfo.channel ?? 0,
      "liveRecordId": liveRecordId,
      "songName": songInfo.songName ?? "",
      "songCover": songInfo.songCover ?? "",
      "songTime": songInfo.songTime ?? 0,
      "singer": songInfo.singer ?? "",
    ]
    NEAPI.PickSong.orderSong().request(params,
                                       returnType: NEKaraokeOrderSongResult.self,
                                       success: success,
                                       failed: failure)
  }

  /// 获取已点列表
  /// - Parameters:
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func getOrderedSongs(_ success: (([NEKaraokeOrderSongResult]?) -> Void)? = nil,
                       failure: ((NSError) -> Void)? = nil) {
    NEAPI.PickSong.getOrderedSongs(String(liveRecordId)).request(success: { data in
      guard let data = data,
            let arr = data["data"] as? [[String: Any]],
            let models = NEKaraokeDecoder.decode(NEKaraokeOrderSongResult.self, array: arr)
      else {
        failure?(makeError(NEKaraokeErrorCode.failed))
        return
      }
      success?(models)

    }, failed: failure)
  }

  /// 取消已点歌曲
  /// - Parameters:
  ///   - orderId: 歌曲ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func deleteSong(_ orderId: Int64,
                  _ success: (() -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "orderId": orderId,
      "liveRecordId": liveRecordId,
    ]
    NEAPI.PickSong.deleteSong().request(params, success: { _ in
      success?()
    }, failed: failure)
  }

  /// 指定歌曲
  /// - Parameters:
  ///   - orderId: 歌曲ID
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func topSong(_ orderId: Int64,
               _ success: (() -> Void)? = nil,
               failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "orderId": orderId,
      "liveRecordId": liveRecordId,
    ]
    NEAPI.PickSong.topSong().request(params, success: { _ in
      success?()
    }, failed: failure)
  }
}
