// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 用户音量信息
@objcMembers
public class NEKaraokeMemberVolumeInfo: NSObject {
  /// 成员ID
  public var userUuid: String = ""
  /// 音量大小 区间  **[0~100]**
  public var volume: Int = 0

  init(_ volumeInfo: NEMemberVolumeInfo) {
    userUuid = volumeInfo.userUuid
    volume = volumeInfo.volume
  }
}
