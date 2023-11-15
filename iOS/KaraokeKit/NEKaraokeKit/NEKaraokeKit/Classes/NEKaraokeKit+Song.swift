// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

extension NEKaraokeKit: NEKaraokePlayStateChangeCallback {
  func onSongPlayPosition(_ postion: UInt64) {
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener
        else { continue }

        if listener.responds(to: #selector(NEKaraokeListener.onSongPlayingPosition(_:))) {
          listener.onSongPlayingPosition?(postion)
        }
      }
    }
  }

  func onRecordingAudioFrame(frame: NERoomRtcAudioFrame) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEKaraokeListener, let listener = pointerListener as? NEKaraokeListener else { continue }

      if listener.responds(to: #selector(NEKaraokeListener.onRecordingAudioFrame(frame:))) {
        listener.onRecordingAudioFrame?(frame: NEKaraokeAudioFrame(frame))
      }
    }
  }
}
