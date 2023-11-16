// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECopyrightedMedia
@objc
public protocol NEKaraokeCopyrightedMediaListener: NSObjectProtocol {
  /// 开始下载回调
  /// @param songId 歌曲Id
  @objc optional func karaoke_onPreloadStart(_ songId: String, channel: SongChannel)

  /// 下载进度回调
  /// @param songId 歌曲ID
  /// @param progress 进度值
  @objc optional func karaoke_onPreloadProgress(_ songId: String, channel: SongChannel,
                                                progress: Float)

  /// 下载完成
  /// @param songId 歌曲歌曲ID
  /// @param error 成功为nil
  @objc optional func karaoke_onPreloadComplete(_ songId: String, channel: SongChannel,
                                                error: Error?)
}

@objc
public protocol NEKaraokeCopyrightedMediaEventHandler: NSObjectProtocol {
  /// Tokne过期
  @objc optional func onTokenExpired()
}
