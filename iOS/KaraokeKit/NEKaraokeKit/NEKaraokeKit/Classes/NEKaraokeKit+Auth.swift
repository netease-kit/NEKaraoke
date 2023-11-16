// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 登录模块扩展
public extension NEKaraokeKit {
  /// 是否登录
  var isLoggedIn: Bool {
    NERoomKit.shared().authService.isLoggedIn
  }

  /// 添加登录监听
  /// - Parameter listener: 监听器
  func addAuthListener(_ listener: NEKaraokeAuthListener) {
    NEKaraokeLog.apiLog(kitTag, desc: "Add auth listener.")
    authListeners.addPointer(Unmanaged.passUnretained(listener).toOpaque())
  }

  /// 移除登录监听
  /// - Parameter listener: 监听器
  func removeAuthListener(_ listener: NEKaraokeAuthListener) {
    NEKaraokeLog.apiLog(kitTag, desc: "Remove auth listener.")
    var listenerIndexArray = [Int]()
    for index in 0 ..< authListeners.allObjects.count {
      let pointerListener = authListeners.pointer(at: index)
      if let temPointerListener = pointerListener {
        // 优先判断赋值，不可直接强制解包 可能过程中出现nil
        let tempListener = Unmanaged<NEKaraokeAuthListener>
          .fromOpaque(temPointerListener).takeUnretainedValue()
        if tempListener.isEqual(listener) {
          listenerIndexArray.append(index)
        }
      }
    }
    for listenerIndex in listenerIndexArray {
      if listenerIndex < authListeners.allObjects.count {
        authListeners.removePointer(at: listenerIndex)
      }
    }
  }

  /// 登录
  /// - Parameters:
  ///   - account: 账号
  ///   - token: 令牌
  ///   - callback: 回调
  func login(_ account: String,
             token: String,
             callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Login. Account: \(account). Token: \(token)")

    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to login. Uninitialized.")
      callback?(NEKaraokeErrorCode.failed, "Failed to login. Uninitialized.", nil)
      return
    }

    NERoomKit.shared().authService.login(account: account,
                                         token: token) { code, str, _ in
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully login.")
        // 登陆成功后，headers添加属性
        NE.addHeader([
          "user": account,
          "token": token,
          "appkey": self.config?.appKey ?? "",
        ])
        NEKaraokeKit.shared().copyrightedMediaService?
          .getSongDynamicTokenUntilSuccess(success: { dynamicToken in
            guard let dynamicToken = dynamicToken,
                  let accessToken = dynamicToken.accessToken,
                  let expiresIn = dynamicToken.expiresIn
            else {
              return
            }
            guard let appkey = NEKaraokeKit.shared().config?.appKey else {
              return
            }
            NEKaraokeKit.shared().initializeCopyrightedMedia(
              appkey,
              token: accessToken,
              userUuid: account,
              extras: NEKaraokeKit.shared().config?.extras,
              success: {
                NEKaraokeKit.shared().copyrightedMediaService?
                  .calculateExpiredTime(timeExpired: expiresIn)
              },
              failure: { error in
              }
            )
          })

      } else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to login. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }

  /// 退出登录
  /// - Parameter callback: 回调
  func logout(callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.apiLog(kitTag, desc: "Logout.")
    guard NEKaraokeKit.shared().isInitialized else {
      NEKaraokeLog.errorLog(kitTag, desc: "Failed to logout. Uninitialized.")
      callback?(NEKaraokeErrorCode.failed, "Failed to logout. Uninitialized.", nil)
      return
    }
    NERoomKit.shared().authService.logout { code, str, _ in
      if code == 0 {
        NEKaraokeLog.successLog(kitTag, desc: "Successfully logout.")
      } else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to logout. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }
}

extension NEKaraokeKit: NEAuthListener {
  public func onAuthEvent(evt: NEAuthEvent) {
    DispatchQueue.main.async {
      for pointerListener in self.authListeners.allObjects {
        guard pointerListener is NEKaraokeAuthListener, let listener = pointerListener as? NEKaraokeAuthListener else { continue }
        if listener.responds(to: #selector(NEKaraokeAuthListener.onKaraokeAuthEvent(_:))) {
          listener
            .onKaraokeAuthEvent?(NEKaraokeAuthEvent(rawValue: evt.rawValue) ?? .loggedOut)
        }
      }
    }
  }
}
