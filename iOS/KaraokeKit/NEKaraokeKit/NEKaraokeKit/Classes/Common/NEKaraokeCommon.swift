// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
/// 房间类型
public enum NEKaraokeLiveRoomType: UInt {
  /// PK直播
  case pkLive = 1
  /// 语聊房
  case multiAudio = 2
  /// KTV
  case ktv = 3
}

@objc
/// 直播状态
public enum NEKaraokeLiveStatus: UInt {
  /// 未直播
  case idle = 0
  /// 直播中
  case living = 1
  /// PK中
  case pking = 2
  /// PK惩罚中
  case punishing = 3
  /// 连麦中
  case connected = 4
  /// PK邀请中
  case inviting = 5
  /// 直播结束
  case end = 6
}

@objc
/// 唱歌模式
public enum NEKaraokeSingMode: Int, Codable {
  /// 智能合唱
  case AIChorus = 0
  /// 串行合唱
  case serialChorus = 1
  /// NTP合唱
  case NTPChorus = 2
  /// 独唱
  case solo = 3
}
