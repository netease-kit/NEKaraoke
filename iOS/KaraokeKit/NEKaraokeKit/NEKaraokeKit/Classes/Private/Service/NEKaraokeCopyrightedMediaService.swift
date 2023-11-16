// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECopyrightedMedia

let copyrightedTag = "NEKaraokeCopyrightedMediaService"
let maxCopyrightedRetry = 10

class NEKaraokeCopyrightedMediaService: NSObject {
  /// 当前重试幂
  private var retryLater = 2
  /// 请求线程队列
  private var getTokenRetryQueue = DispatchQueue(label: "getTokenRetryQueue")
  /// 定时器线程
  private let timerQueue: DispatchQueue = .init(label: "karaoke.copyrightedMedia.timer.queue", qos: .default, attributes: .concurrent)

  /// 过期定时器
  private var expiredTimer: Timer?
  /// 过期时间
  private var expiredSeconds: Int64 = 180

  // 幂等网络请求子线程处理
  func getSongDynamicTokenUntilSuccess(success: ((NEKaraokeDynamicToken?) -> Void)? = nil) {
    NEKaraokeKit.shared().getSongToken { code, msg, dynamicToken in
      if code != 0 {
        NEKaraokeLog.errorLog(copyrightedTag, desc: "获取动态token失败")
        if self.retryLater > maxCopyrightedRetry {
          guard let success = success else {
            return
          }
          success(nil)
        } else {
          self.getTokenRetryQueue
            .asyncAfter(deadline: .now() + .seconds(self.retryLater)) { [weak self] in
              self?.getSongDynamicTokenUntilSuccess(success: success)
              guard let retryLater = self?.retryLater else {
                return
              }
              self?.retryLater = 2 * retryLater
            }
        }

      } else {
        guard let success = success else {
          return
        }
        NEKaraokeLog.successLog(copyrightedTag, desc: "获取动态token成功")
        self.retryLater = 2
        success(dynamicToken)
      }
    }
  }

  // 定时器相关处理
  // 计算过期时间
  func calculateExpiredTime(timeExpired: Int64) {
    // 单位是秒
    // 直接释放定时器
    releaseExpiredTimer()
    if timeExpired > 0 {
      if timeExpired > expiredSeconds {
        // 大于用户设定过期提醒时间
        timerQueue.async { [weak self] in
          guard let self = self else {
            return
          }
          self.expiredTimer = Timer.scheduledTimer(
            timeInterval: TimeInterval(timeExpired - self.expiredSeconds),
            target: self,
            selector: #selector(self.timeEvent),
            userInfo: nil,
            repeats: false
          )
          if let curTimer = self.expiredTimer {
            RunLoop.current.run()
            RunLoop.current.add(curTimer, forMode: .common)
          }
        }
      } else {
        // 直接提示
        // 设置属性即将过期
        timeEvent()
      }
    } else {
      // 未设定过期时间，直接释放
      timeEvent()
    }
  }

  func releaseExpiredTimer() {
    guard let _ = expiredTimer else {
      return
    }
    expiredTimer?.invalidate()
    expiredTimer = nil
  }

  @objc func timeEvent() {
    NEKaraokeLog.infoLog(copyrightedTag, desc: "动态token即将过期，重新Token")
    getSongDynamicTokenUntilSuccess { dynamicToken in
      guard let dynamicToken = dynamicToken else {
        return
      }
      guard let accessToken = dynamicToken.accessToken,
            let expiresIn = dynamicToken.expiresIn
      else {
        return
      }
      NECopyrightedMedia.getInstance().renewToken(accessToken)
      self.calculateExpiredTime(timeExpired: expiresIn)
    }
  }
}
