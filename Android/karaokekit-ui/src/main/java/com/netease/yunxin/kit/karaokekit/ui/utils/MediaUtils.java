// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.media.MediaMetadataRetriever;
import java.io.IOException;

public class MediaUtils {

  public static long getDuration(String path) {
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    long duration = 0;
    try {
      mmr.setDataSource(path);
      String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      duration = Long.parseLong(time);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      try {
        mmr.release();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return duration;
  }
}
