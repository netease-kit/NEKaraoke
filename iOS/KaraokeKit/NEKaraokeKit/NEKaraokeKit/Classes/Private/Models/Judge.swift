// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct Judge {
  /// 前置条件判断
  static func preCondition<T: Any>(_ success: @escaping () -> Void,
                                   failure: NEKaraokeCallback<T>? = nil) {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Uninitialized.")
      failure?(NEKaraokeErrorCode.failed, "Uninitialized.", nil)
      return
    }
    guard let _ = NEKaraokeKit.shared().roomContext else {
      NEKaraokeLog.errorLog(kitTag, desc: "RoomContext is nil.")
      failure?(NEKaraokeErrorCode.failed, "RoomContext is nil.", nil)
      return
    }
    success()
  }

  /// 初始化判断条件
  static func initCondition<T: Any>(_ success: @escaping () -> Void,
                                    failure: NEKaraokeCallback<T>? = nil) {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Uninitialized.")
      failure?(NEKaraokeErrorCode.failed, "Uninitialized.", nil)
      return
    }
    success()
  }

  @discardableResult

  /// 同步返回
  static func syncCondition(_ success: @escaping () -> Int) -> Int {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Uninitialized.")
      return NEKaraokeErrorCode.failed
    }
    guard let _ = NEKaraokeKit.shared().roomContext else {
      NEKaraokeLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return NEKaraokeErrorCode.failed
    }
    return success()
  }

  static func condition(_ success: @escaping () -> Void) {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Uninitialized.")
      return
    }
    guard let _ = NEKaraokeKit.shared().roomContext else {
      NEKaraokeLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return
    }
    success()
  }

  static func syncResult<T: Any>(_ success: @escaping () -> T) -> T? {
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Uninitialized.")
      return nil
    }
    guard let _ = NEKaraokeKit.shared().roomContext else {
      NEKaraokeLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return nil
    }
    return success()
  }
}
