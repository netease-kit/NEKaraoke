// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// KaraokeKit 配置项
@objcMembers

public class NEKaraokeKitConfig: NSObject {
  /// appKey 为karaoke服务的Key
  public var appKey: String = ""
  /// 预留字段
  public var extras: [String: String] = .init()
}
