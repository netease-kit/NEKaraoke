// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
public class NEKaraokeRoomInfo: NSObject {
  /// 主播信息
  public var anchor: NEKaraokeAnchor?
  /// 直播信息
  public var liveModel: NEKaraokeLiveModel?

  init(create: _NECreateLiveResponse?) {
    if let create = create {
      anchor = NEKaraokeAnchor(create.anchor)
      liveModel = NEKaraokeLiveModel(create.live)
    }
  }

  init(liveInfo: _NEKaraokeInfoResponse?) {
    if let info = liveInfo {
      anchor = NEKaraokeAnchor(info.anchor)
      liveModel = NEKaraokeLiveModel(info.live)
    }
  }
}

/// 主播信息
@objcMembers
public class NEKaraokeAnchor: NSObject {
  /// 用户编号
  public var userUuid: String?
  /// 房间用户编号
  public var rtcUid: Int = 0
  /// 昵称
  public var userName: String?
  /// 头像地址
  public var icon: String?
  init(_ anchor: _NECreateLiveAnchor?) {
    if let anchor = anchor {
      userUuid = anchor.userUuid
      userName = anchor.userName
      icon = anchor.icon
    }
  }
}

/// 直播信息
@objcMembers
public class NEKaraokeLiveModel: NSObject {
  /// 应用编号
  public var appId: String = ""
  /// 房间号
  public var roomUuid: String?
  /// 创建人账号
  public var userUuid: String?
  /// 直播记录编号
  public var liveRecordId: Int = 0
  /// 直播类型
  public var liveType: NEKaraokeLiveRoomType = .ktv
  /// 直播记录是否有效 1: 有效 -1 无效
  public var status: Int = 1
  /// 直播主题
  public var liveTopic: String?
  /// 背景图地址
  public var cover: String?
  /// 打赏总额
  public var rewardTotal: Int = 0
  /// 观众人数
  public var audienceCount: Int = 0
  /// 直播状态
  public var live: NEKaraokeLiveStatus = .idle
  /// 唱歌模式
  public var singMode: NEKaraokeSingMode = .AIChorus
  /// 麦位人数
  public var onSeatCount: Int = 0

  init(_ live: _NECreateLiveLive?) {
    if let live = live {
      roomUuid = live.roomUuid
      userUuid = live.userUuid
      liveRecordId = live.liveRecordId ?? 0
      liveType = NEKaraokeLiveRoomType(rawValue: UInt(live.liveType ?? 3)) ?? .ktv
      status = live.status ?? 1
      liveTopic = live.liveTopic
      cover = live.cover
      rewardTotal = live.rewardTotal ?? 0
      audienceCount = live.audienceCount ?? 0
      self.live = NEKaraokeLiveStatus(rawValue: UInt(live.live ?? 0)) ?? .idle
      singMode = NEKaraokeSingMode(rawValue: live.singMode ?? 0) ?? .AIChorus
      onSeatCount = live.onSeatCount ?? 0
    }
  }
}

/// Karaoke 房间列表
@objcMembers
public class NEKaraokeRoomList: NSObject {
  /// 数据列表
  public var list: [NEKaraokeRoomInfo]?
  /// 当前页
  public var pageNum: Int = 0
  /// 是否有下一页
  public var hasNextPage: Bool = false
  init(_ list: _NEKaraokeListResponse?) {
    if let list = list {
      pageNum = list.pageNum ?? 0
      hasNextPage = list.hasNextPage
      if let details = list.list {
        self.list = details.compactMap { detail in
          NEKaraokeRoomInfo(liveInfo: detail)
        }
      }
    }
  }
}
