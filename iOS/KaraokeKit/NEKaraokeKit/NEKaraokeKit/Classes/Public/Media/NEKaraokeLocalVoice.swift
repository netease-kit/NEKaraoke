// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

/// 频谱子带索引 预设值。
@objc
public enum NEKaraokeAudioEqualizationBandFrequency: Int {
  /// 31 Hz.
  case band31 = 0
  /// 62 Hz.
  case band62
  /// 125 Hz.
  case band125
  /// 250 Hz.
  case band250
  /// 500 Hz.
  case band500
  /// 1 kHz.
  case band1K
  /// 2 kHz.
  case band2K
  /// 4 kHz.
  case band4K
  /// 8 kHz.
  case band8K
  /// 16 kHz.
  case band16K
}

/// 混响参数
@objcMembers
public class NEKaraokeReverbParam: NSObject {
  ///  湿信号，取值范围[0,1]，默认值0.0f
  public var wetGain: Float = 0.0

  ///  干信号，取值范围[0,1]，默认值1.0f
  public var dryGain: Float = 0.0

  ///  混响阻尼，取值范围[0,1]，默认值0.0f
  public var damping: Float = 0.0

  ///  房间大小，取值范围[0.1,2]，默认值0.1f
  public var roomSize: Float = 0.0

  ///  持续强度-余响，取值范围[0.1,20]，默认值0.1f
  public var decayTime: Float = 0.0

  ///  延迟长度，取值范围[0,1]，默认值0.0f
  public var preDelay: Float = 0.0

  func converToRoom() -> NERoomReverbParam {
    let param = NERoomReverbParam()
    param.wetGain = wetGain
    param.dryGain = dryGain
    param.damping = damping
    param.roomSize = roomSize
    param.decayTime = decayTime
    param.preDelay = preDelay
    return param
  }
}
