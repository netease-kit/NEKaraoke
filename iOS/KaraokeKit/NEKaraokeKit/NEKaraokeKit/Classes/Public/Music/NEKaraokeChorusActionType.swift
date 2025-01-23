// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 收到打赏消息
let BATCH_REWARD_TYPE = 1005

/// 合唱状态
@objc
public enum NEKaraokeChorusActionType: Int {
  /// 取消合唱邀请
  case cancelInvite = 1029
  /// 邀请合唱
  case invite = 1030
  /// 同意合唱邀请
  case agreeInvite = 1031
  /// 已准备好合唱
  case ready = 1032
  /// 开始唱歌
  case startSong = 1033
  /// 暂停唱歌
  case pauseSong = 1034
  /// 恢复唱歌
  case resumeSong = 1035
  /// 结束唱歌
  case endSong = 1036
  /// 放弃演唱
  case abandon = 1037
  /// 播放下一首歌
  case next = 1038
}

/// 点歌状态
@objc
public enum NEKaraokePickSongActionType: Int {
  /// 点歌
  case pick = 1008
  /// 取消点歌
  case cancelPick = 1009
  /// 切歌
  case switchSong = 1010
  /// 置顶
  case top = 1011
  /// 列表变化
  case listChange = 1012
}
