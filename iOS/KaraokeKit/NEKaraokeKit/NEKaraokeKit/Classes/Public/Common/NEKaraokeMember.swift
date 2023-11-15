// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

@objcMembers
/// Karaoke 成员模型
public class NEKaraokeMember: NSObject {
  /// 用户ID
  public var account: String = ""
  /// 用户名
  public var name: String = ""
  /// 用户角色
  public var role: String = ""
  /// 音频是否打开
  public var isAudioOn: Bool = false

  init(_ member: NERoomMember) {
    account = member.uuid
    name = member.name
    role = member.role.name
    isAudioOn = member.properties["recordDevice"] == "on"
  }
}
