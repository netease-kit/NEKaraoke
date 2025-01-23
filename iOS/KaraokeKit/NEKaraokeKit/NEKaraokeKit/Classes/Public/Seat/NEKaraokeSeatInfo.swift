// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 麦位信息
@objcMembers
public class NEKaraokeSeatInfo: NSObject {
  /// 唯一ID
  var uuid: String = ""
  /// 麦位创建者
  public var creator: String = ""
  /// 麦位管理员列表
  public var managers: [String] = []
  /// 麦位列表信息
  public var seatItems: [NEKaraokeSeatItem] = []
  init(_ info: NESeatInfo) {
    uuid = info.uuid
    creator = info.creator
    managers = info.managers
    var items = [NEKaraokeSeatItem]()
    info.seatItems.forEach { items.append(NEKaraokeSeatItem($0)) }
    seatItems = items
  }
}
