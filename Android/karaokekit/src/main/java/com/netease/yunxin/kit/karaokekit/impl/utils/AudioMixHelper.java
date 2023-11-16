// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.impl.utils;

public class AudioMixHelper {

  static {
    System.loadLibrary("ne_karaoke_utils");
  }

  public native byte[] mixAudioFrameData(
      byte[] dest, byte[] src, int samples_per_channel, int number_of_channels);

  //    public native void setAudioFrameDataVolume(ByteBuffer data, int len, float volume);

}
