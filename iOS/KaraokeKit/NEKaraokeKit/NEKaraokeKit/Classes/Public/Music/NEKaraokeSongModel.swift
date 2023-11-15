// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers
/// 动作操作人
public class NEKaraokeOperator: NSObject, Codable {
  /// 操作人ID
  private var userUuid: String?
  public var account: String {
    userUuid ?? ""
  }

  /// 操作人昵称
  public var userName: String?
  /// 操作人头像
  public var icon: String?
}

@objcMembers
/// 邀请合唱模型
public class NEKaraokeSongModel: NSObject, Codable {
  /// 额外参数
  public var ext: [String: Any]?
  public var actionOperator: NEKaraokeOperator?
  public var chorusInfo: NEKaraokeSongInfoModel?

  enum CodingKeys: String, CodingKey {
    case ext,
         actionOperator = "operator",
         chorusInfo,
         singInfo
  }

  override public init() {
    super.init()
  }

  public required init(from decoder: Decoder) throws {
    let container = try decoder.container(keyedBy: CodingKeys.self)
    ext = try container.decodeIfPresent([String: Any].self, forKey: .ext)
    actionOperator = try container.decodeIfPresent(NEKaraokeOperator.self, forKey: .actionOperator)
    chorusInfo = try container.decodeIfPresent(NEKaraokeSongInfoModel.self, forKey: .chorusInfo) ?? container.decodeIfPresent(NEKaraokeSongInfoModel.self, forKey: .singInfo)
  }

  public func encode(to encoder: Encoder) throws {}
}

@objcMembers
public class NEKaraokeSongInfoModel: NSObject, Codable {
  public var chorusId: String?
  public var appKey: String?
  public var roomUuid: String?
  public var roomName: String?
  public var orderId: Int64 = 0
  public var liveRecordId: Int = 0
  public var liveTopic: String?
  public var singMode: Int = 0
  // 主唱uuid
  public var userUuid: String?
  // 主唱昵称
  public var userName: String?
  // 主唱头像
  public var icon: String?
  // 副唱uuid
  public var assistantUuid: String?
  // 副唱昵称
  public var assistantName: String?
  // 副唱头像
  public var assistantIcon: String?
  // 歌曲名称
  public var songName: String?
  // 歌曲封面
  public var songCover: String?
  // 歌手名称
  public var singer: String?
  // 歌手封面
  public var singerCover: String?
  // 歌曲编号
  public var songId: String?
  // 合唱的类型，1:串行合唱（默认） 2:实时合唱，同意合唱后会含有该字段
  public var chorusType: Int = 0
  /// 额外参数
  public var ext: [String: Any]?

  public var chorusStatus: Int = 0

  public var songTime: Int = 0

  public var channel: Int = 0

  public var songStatus: Int = 0

  enum CodingKeys: String, CodingKey {
    case chorusId, appKey, roomUuid, roomName, orderId, liveRecordId, liveTopic, singMode, userUuid, userName, icon, assistantUuid, assistantName, assistantIcon, songName, songCover, singer, singerCover, songId, chorusType, ext, chorusStatus, songTime, channel, songStatus
  }

  public required init(from decoder: Decoder) throws {
    let container = try decoder.container(keyedBy: CodingKeys.self)
    chorusId = try container.decodeIfPresent(String.self, forKey: .chorusId)
    appKey = try container.decodeIfPresent(String.self, forKey: .appKey)
    roomUuid = try container.decodeIfPresent(String.self, forKey: .roomUuid)
    orderId = try container.decodeIfPresent(Int64.self, forKey: .orderId) ?? 0
    liveRecordId = try container.decodeIfPresent(Int.self, forKey: .liveRecordId) ?? 0
    liveTopic = try container.decodeIfPresent(String.self, forKey: .liveTopic)
    singMode = try container.decodeIfPresent(Int.self, forKey: .singMode) ?? 0
    userUuid = try container.decodeIfPresent(String.self, forKey: .userUuid)
    userName = try container.decodeIfPresent(String.self, forKey: .userName)
    icon = try container.decodeIfPresent(String.self, forKey: .icon)
    assistantUuid = try container.decodeIfPresent(String.self, forKey: .assistantUuid)
    assistantName = try container.decodeIfPresent(String.self, forKey: .assistantName)
    assistantIcon = try container.decodeIfPresent(String.self, forKey: .assistantIcon)
    songName = try container.decodeIfPresent(String.self, forKey: .songName)
    songCover = try container.decodeIfPresent(String.self, forKey: .songCover)
    singer = try container.decodeIfPresent(String.self, forKey: .singer)
    singerCover = try container.decodeIfPresent(String.self, forKey: .singerCover)
    songId = try container.decodeIfPresent(String.self, forKey: .songId)
    chorusType = try container.decodeIfPresent(Int.self, forKey: .chorusType) ?? 0
    ext = try container.decodeIfPresent([String: Any].self, forKey: .ext)
    chorusStatus = try container.decodeIfPresent(Int.self, forKey: .chorusStatus) ?? 0
    songTime = try container.decodeIfPresent(Int.self, forKey: .songTime) ?? 0
    channel = try container.decodeIfPresent(Int.self, forKey: .channel) ?? 0
    songStatus = try container.decodeIfPresent(Int.self, forKey: .songStatus) ?? 0
  }

  public func encode(to encoder: Encoder) throws {}
}
