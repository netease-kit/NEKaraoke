// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 音频帧信息
@objcMembers
public class NEKaraokeAudioFrame: NSObject {
  /// 音频格式
  public var format: NEKaraokeAudioFormat

  /// 音频裸数据
  public var data: UnsafeMutableRawPointer

  /// 同步音频主辅流的时间戳，一般只有在同时开启外部音频主流及辅流输入时用到
  public var syncTimestamp: Int64 = 0
  init(_ frame: NERoomRtcAudioFrame) {
    let format = NEKaraokeAudioFormat(frame.format)
    self.format = format
    data = frame.data
    syncTimestamp = frame.syncTimestamp
  }
}

/// 音频类型
@objc
public enum NEKaraokeAudioType: Int {
  case pcm16 = 0
}

/// 音频格式
@objcMembers
public class NEKaraokeAudioFormat: NSObject {
  /// 音频类型
  public var type: NEKaraokeAudioType = .pcm16
  /// 音频声道数。 1：单声道  2：双声道
  public var channels: UInt32 = 1
  /// 音频采样率
  public var sampleRate: UInt32 = 0
  /// 每个采样点的字节数
  public var bytesPerSample: UInt32 = 0
  /// 每个声道的采样点数
  public var samplesPerChannel: UInt32 = 0
  init(_ format: NERoomRtcAudioFormat) {
    type = NEKaraokeAudioType(rawValue: format.type.rawValue) ?? .pcm16
    channels = format.channels
    sampleRate = format.sampleRate
    bytesPerSample = format.bytesPerSample
    samplesPerChannel = format.samplesPerChannel
  }
}
