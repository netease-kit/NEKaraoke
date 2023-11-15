// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECopyrightedMedia
import NERoomKit

let kitTag = "NEKaraokeKit"

@objcMembers
public class NEKaraokeKit: NSObject {
  // MARK: - ------------------------- Public method --------------------------

  /// 伴奏effectId
  public static let AccompanyEffectId: UInt32 = 1001
  /// 原唱 effectId
  public static let OriginalEffectId: UInt32 = 1000

  /// 单例初始化
  /// - Returns: 单例对象
  public static func shared() -> NEKaraokeKit {
    instance
  }

  /// 本端成员信息
  /// 加入房间后获取
  public var localMember: NEKaraokeMember? {
    Judge.syncResult {
      NEKaraokeMember(self.roomContext!.localMember)
    }
  }

  /// 所有成员信息(包含本端)
  /// 加入房间后获取
  public var allMemberList: [NEKaraokeMember] {
    Judge.syncResult {
      var allMembers = [NERoomMember]()
      allMembers.append(self.roomContext!.localMember)
      self.roomContext!.remoteMembers.forEach { allMembers.append($0) }
      return allMembers.map { NEKaraokeMember($0) }
    } ?? []
  }

  /// NEKaraokeKit 初始化
  /// - Parameters:
  ///   - config: 初始化配置
  ///   - callback: 回调
  public func initialize(config: NEKaraokeKitConfig,
                         callback: NEKaraokeCallback<AnyObject>? = nil) {
    NEKaraokeLog.setUp(config.appKey)
    NEKaraokeLog.apiLog(kitTag, desc: "Initialize")
    self.config = config
    if let baseUrl = config.extras["baseUrl"] {
      NE.config.customUrl = baseUrl
    }
    let options = NERoomKitOptions(appKey: config.appKey)
    options.extras = config.extras
    NERoomKit.shared().initialize(options: options) { code, str, _ in
      if code == 0 {
        self.isInitialized = true
        NEKaraokeLog.successLog(kitTag, desc: "Successfully initialize.")
      } else {
        NEKaraokeLog.errorLog(kitTag, desc: "Failed to initialize. Code: \(code)")
      }
      callback?(code, str, nil)
    }
  }

  /// 初始化状态
  public var isInitialized: Bool = false

  /// 添加房间监听
  /// - Parameter listener: 事件监听
  public func addKaraokeListener(_ listener: NEKaraokeListener) {
    NEKaraokeLog.apiLog(kitTag, desc: "Add Karaoke listener.")
    listeners.addPointer(Unmanaged.passUnretained(listener).toOpaque())
  }

  /// 移除房间监听
  /// - Parameter listener: 事件监听
  public func removeKaraokeListener(_ listener: NEKaraokeListener) {
    NEKaraokeLog.apiLog(kitTag, desc: "Remove karaoke listener.")
    var listenerIndexArray = [Int]()
    for index in 0 ..< listeners.allObjects.count {
      let pointerListener = listeners.pointer(at: index)
      if let temPointerListener = pointerListener {
        // 优先判断赋值，不可直接强制解包 可能过程中出现nil
        let tempListener = Unmanaged<NEKaraokeListener>.fromOpaque(temPointerListener)
          .takeUnretainedValue()
        if tempListener.isEqual(listener) {
          listenerIndexArray.append(index)
        }
      }
    }
    for listenerIndex in listenerIndexArray {
      if listenerIndex < listeners.allObjects.count {
        listeners.removePointer(at: listenerIndex)
      }
    }
  }

  // 正在删除的歌曲
  public var deletingSongs: [Int64] = []
  // 歌曲是否在删除中
  public func isSongDeleting(_ orderId: Int64) -> Bool {
    deletingSongs.contains(orderId)
  }

  /// 主播开播详情
  public var liveDetail: NEKaraokeRoomInfo {
    NEKaraokeRoomInfo(create: liveInfo)
  }

  // MARK: - ------------------------- Private method --------------------------

  override init() {
    super.init()
    NERoomKit.shared().authService.addAuthListener(self)
  }

  deinit {
    NERoomKit.shared().authService.removeAuthListener(self)
  }

  private static let instance = NEKaraokeKit()
  // 房间监听器数组
  var listeners = NSPointerArray.weakObjects()
  // 登录监听器数组
  var authListeners = NSPointerArray.weakObjects()
  var config: NEKaraokeKitConfig?
  // 维护房间上下文
  var roomContext: NERoomContext?
  // 房间服务
  private var _roomService = NEKaraokeRoomService()
  var roomService: NEKaraokeRoomService { _roomService }
  // 播放服务
  var _audioPlayService: NEKaraokeAudioPlayService?
  var audioPlayService: NEKaraokeAudioPlayService? { _audioPlayService }
  // 唱歌服务
  var musicService: NEKaraokeMusicService?

  // 版权服务
  var _copyrightedMediaService = NEKaraokeCopyrightedMediaService()
  var copyrightedMediaService: NEKaraokeCopyrightedMediaService? {
    _copyrightedMediaService
  }

  // 版权监听器数组
  var preloadProtocolListeners = NSPointerArray.weakObjects()
  // 版权接口过期监听对象
  var copyrightedEventMediaHandler: AnyObject?

  // 直播信息
  var liveInfo: _NECreateLiveResponse?
}
