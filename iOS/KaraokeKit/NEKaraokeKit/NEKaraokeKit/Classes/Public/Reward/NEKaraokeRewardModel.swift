// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 礼物模型
@objcMembers
public class NEKaraokeBatchGiftModel: NSObject {
  /// 发送者账号
  public var sendAccout: String = ""
  /// 礼物编号
  public var giftId: Int = 0
  /// 礼物个数
  public var giftCount: Int = 0

  public var rewarderUserUuid: String = ""
//  // 打赏者昵称
  public var rewarderUserName: String = ""
//  public var rewardeeUserUuid: String = ""
//  // 被打赏者昵称
//  public var rewardeeUserName: String = ""

  /// 麦上主播或者观众打赏信息
  public var seatUserReward: [NEKaraokeBatchSeatUserReward]
  ///
  public var rewardeeUsers: [NEKaraokeBatchSeatUserRewardee]

  init(_ rewardMsg: _NEKaraokeBatchRewardMessage) {
    sendAccout = rewardMsg.senderUserUuid ?? ""
    giftId = rewardMsg.giftId ?? 0
    giftCount = rewardMsg.giftCount ?? 0
    rewarderUserName = rewardMsg.userName ?? ""
    rewarderUserUuid = rewardMsg.userUuid ?? ""
    seatUserReward = rewardMsg.seatUserReward.map { NEKaraokeBatchSeatUserReward($0) }
    rewardeeUsers = rewardMsg.targets.map { NEKaraokeBatchSeatUserRewardee($0) }
  }
}

@objcMembers
public class NEKaraokeBatchSeatUserReward: NSObject {
  public var seatIndex: Int = 0
  public var userUuid: String?
  public var userName: String?
  public var rewardTotal: Int = 0
  public var icon: String?

  init(_ batchSeatUserReward: _NEKaraokeBatchSeatUserReward?) {
    if let batchSeatUserReward = batchSeatUserReward {
      seatIndex = batchSeatUserReward.seatIndex
      userUuid = batchSeatUserReward.userUuid
      userName = batchSeatUserReward.userName
      rewardTotal = batchSeatUserReward.rewardTotal
      icon = batchSeatUserReward.icon
    }
  }
}

@objcMembers
public class NEKaraokeBatchSeatUserRewardee: NSObject {
  public var userUuid: String?
  public var userName: String?
  public var icon: String?

  init(_ batchSeatUserReward: _NEKaraokeBatchSeatUserRewardee?) {
    if let batchSeatUserRewardee = batchSeatUserReward {
      userUuid = batchSeatUserRewardee.userUuid
      userName = batchSeatUserRewardee.userName
      icon = batchSeatUserRewardee.icon
    }
  }
}
