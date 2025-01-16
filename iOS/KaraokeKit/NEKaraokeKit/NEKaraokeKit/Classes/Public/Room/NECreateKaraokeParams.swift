// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 创建Karaoke房间参数
@objcMembers

public class NECreateKaraokeParams: NSObject {
  /// 房间名称
  public var title: String = ""
  /// 房间内昵称
  public var nick: String = ""
  /// 扩展参数
  public var extraData: String?
  /// 演唱模式，0：智能合唱， 1: 串行合唱，2：NTP实时合唱，3：独唱，不传默认为智能合唱，KTV场景使用
  public var singMode: NEKaraokeSingMode = .AIChorus
  /// 麦位数量。如果设置为大于**0**的值，则会在创建的房间中启用麦位管理
  public var seatCount: Int = 0
  /// 麦位模式，0：自由模式，1：管理员控制模式，不传默认为自由模式
  var seatMode: Int = 1
  /// 模版 ID
  public var confidId: Int = 0
}
