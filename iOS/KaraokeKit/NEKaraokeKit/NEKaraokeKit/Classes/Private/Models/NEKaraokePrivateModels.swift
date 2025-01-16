// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
class _NECreateLiveResponse: NSObject, Codable {
  var anchor: _NECreateLiveAnchor?
  var live: _NECreateLiveLive?
}

// MARK: 开播

/// 主播信息
@objcMembers
class _NECreateLiveAnchor: NSObject, Codable {
  /// 用户编号
  var userUuid: String?
  /// 房间用户编号
  var rtcUid: Int?
  /// 昵称
  var userName: String?
  /// 头像地址
  var icon: String?
}

/// 直播信息
@objcMembers
class _NECreateLiveLive: NSObject, Codable {
  /// 应用编号
  public var appId: String?
  /// 房间号
  public var roomUuid: String?
  /// 创建人账号
  public var userUuid: String?
  /// 直播记录编号
  public var liveRecordId: Int?
  /// 直播类型
  public var liveType: Int?
  /// 直播记录是否有效 1: 有效 -1 无效
  public var status: Int?
  /// 直播主题
  public var liveTopic: String?
  /// 背景图地址
  public var cover: String?
  /// 打赏总额
  public var rewardTotal: Int?
  /// 观众人数
  public var audienceCount: Int?
  /// 直播状态，0.未开始，1.直播中，2.PK中 3. 惩罚中  4.连麦中  5.等待PK中  6.直播结束
  public var live: Int?
  /// 唱歌模式
  public var singMode: Int?
  /// 麦位人数
  public var onSeatCount: Int?
}

// MARK: 直播列表

/// 直播信息
@objcMembers
class _NEKaraokeListResponse: NSObject, Codable {
  var pageNum: Int?
  var pageSize: Int?
  var size: Int?
  var startRow: Int?
  var endRow: Int?
  var pages: Int?
  var prePage: Int?
  var nextPage: Int?
  var isFirstPage: Bool?
  var isLastPage: Bool?
  var hasPreviousPage: Bool = false
  var hasNextPage: Bool = false
  var navigatePages: Int?
  var navigatepageNums: [Int]?
  var navigateFirstPage: Int?
  var navigateLastPage: Int?
  var total: Int?
  var list: [_NEKaraokeInfoResponse]?
}

// MARK: 直播详情

/// 直播信息
@objcMembers
class _NEKaraokeInfoResponse: NSObject, Codable {
  var anchor: _NECreateLiveAnchor?
  var live: _NECreateLiveLive?
}

/// 设备参数 判断走串行还是实时合唱的参数
class _NEKaraokeDeviceParam: Codable {
  /// 播放延时，单位：ms
  var playDelay: UInt64 = 30
  /// rtt值，单位：ms
  var rtt: UInt64 = 0
  /// 有线耳机，1: 有线耳机，0: 非有线耳机
  var wiredHeadset: Int = 0
  init(_ playDelay: UInt64 = 30,
       rtt: UInt64,
       wiredHeadset: Int) {
    self.playDelay = playDelay
    self.rtt = rtt
    self.wiredHeadset = wiredHeadset
  }
}

enum _NEKaraokeMusicOperationType: Int {
  // 暂停
  case pause = 0
  // 恢复
  case resume
  // 停止
  case stop
}

// MARK: 打赏

@objcMembers
class _NEKaraokeBatchRewardMessage: NSObject, Codable {
  /// 消息发送者用户编号
  var senderUserUuid: String?
  /// 发送消息时间
  var sendTime: Int?
  /// 打赏者昵称
  var userName: String?
  /// 打赏者id
  var userUuid: String?
  /// 礼物编号
  var giftId: Int?
  /// 礼物个数
  var giftCount: Int?
  /// 麦上所有人的被打赏信息
  var seatUserReward: [_NEKaraokeBatchSeatUserReward]
  /// 被打赏者信息列表
  var targets: [_NEKaraokeBatchSeatUserRewardee]
}

class _NEKaraokeBatchSeatUserReward: NSObject, Codable {
  var seatIndex: Int
  var userUuid: String?
  var userName: String?
  var rewardTotal: Int
  var icon: String?
}

class _NEKaraokeBatchSeatUserRewardee: NSObject, Codable {
  var userUuid: String?
  var userName: String?
  var icon: String?
}
