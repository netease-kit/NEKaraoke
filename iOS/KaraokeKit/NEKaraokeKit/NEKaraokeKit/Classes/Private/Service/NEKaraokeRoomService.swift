// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

class NEKaraokeRoomService {
  /// 获取房间列表
  /// - Parameters:
  ///   - type: 房间类型，默认为3：KTV
  ///   - liveState: 房间状态
  ///   - pageNum: 每页数量
  ///   - pageSize: 页号
  ///   - callback: 回调
  func getKaraokeRoomList(_ type: Int = 3,
                          liveState: Int,
                          pageNum: Int,
                          pageSize: Int,
                          success: ((NEKaraokeRoomList?) -> Void)? = nil,
                          failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "pageNum": pageNum,
      "pageSize": pageSize,
      "live": liveState,
      "liveType": type,
    ]
    NEAPI.Room.roomList.request(params,
                                returnType: _NEKaraokeListResponse.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      let roomList = NEKaraokeRoomList(data)
      success?(roomList)
    } failed: { error in
      failure?(error)
    }
  }

  func getKaraokeRoomInfo(_ liveRecordId: Int,
                          success: ((_NECreateLiveResponse?) -> Void)? = nil,
                          failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "liveRecordId": liveRecordId,
    ]
    NEAPI.Room.info.request(params, returnType: _NECreateLiveResponse.self,
                            success: { data in
                              guard let data = data else {
                                success?(nil)
                                return
                              }
                              success?(data)
                            }, failed: failure)
  }

  /// 创建房间
  /// - Parameters:
  ///   - params: 创建房间参数
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func startKaraoke(_ params: NECreateKaraokeParams,
                    success: ((_NECreateLiveResponse?) -> Void)? = nil,
                    failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "liveType": 3,
      "liveTopic": params.title,
      "cover": "163.com",
      "configId": params.confidId,
      "singMode": params.singMode.rawValue,
      "seatCount": params.seatCount,
      "seatApplyMode": params.seatMode,
    ]
    NEAPI.Room.create.request(param,
                              returnType: _NECreateLiveResponse.self) { resp in
      success?(resp)
    } failed: { error in
      failure?(error)
    }
  }

  /// 结束房间
  /// - Parameters:
  ///   - liveRecordId: 直播记录编号
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func endRoom(_ liveRecordId: Int,
               success: (() -> Void)? = nil,
               failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "liveRecordId": liveRecordId,
    ]
    NEAPI.Room.destroy.request(param,
                               success: { _ in
                                 success?()
                               }, failed: failure)
  }

  /// 批量礼物打赏
  /// - Parameters:
  ///   - liveRecordId: 直播编号
  ///   - giftId: 礼物id
  ///   - giftCount: 礼物个数
  ///   - userUuids: 打赏给主播或者麦上观众
  ///   - success: 成功回调
  ///   - failure: 失败回调
  func batchReward(_ liveRecordId: Int,
                   giftId: Int,
                   giftCount: Int,
                   userUuids: [String],
                   success: (() -> Void)? = nil,
                   failure: ((NSError) -> Void)? = nil) {
    let param = [
      "liveRecordId": liveRecordId,
      "giftId": giftId,
      "giftCount": giftCount,
      "targets": userUuids,
    ] as [String: Any]
    NEAPI.Room.batchReward.request(param, success: { _ in
      success?()
    }, failed: failure)
  }

  /// 获取实时Tokne
  /// - Parameters:
  ///   - success: 成功回调
  ///   - failure: 失败回调

  func getSongToken(_ success: ((NEKaraokeDynamicToken?) -> Void)? = nil,
                    failure: ((NSError) -> Void)? = nil) {
    NEAPI.PickSong.getMusicToken().request(returnType: NEKaraokeDynamicToken.self,
                                           success: { resp in
                                             success?(resp)
                                           }, failed: failure)
  }
}
