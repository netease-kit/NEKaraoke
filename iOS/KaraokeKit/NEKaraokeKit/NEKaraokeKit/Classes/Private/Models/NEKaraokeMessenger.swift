// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

enum NEKaraokeMessenger {
  enum Code {
    // 消息ID
    static var commandId: Int = 10000
    // 发起/邀请 合唱
    static var inviteChorus = 1
    // 加入合唱
    static var joinChorus = 2
  }

  enum Key {
    // 消息类型
    static var messageType: String = "messageType"
    // 设备信息，用于决定合唱策略
    static var deviceInfo: String = "deviceInfo"
    // 用户Uid
    static var userUid: String = "userUid"
    // 歌曲ID
    static var songId: String = "songId"
  }
}
