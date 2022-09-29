// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType;

/** 业务模块需要的歌词模型 */
public class LyricBusinessModel {
  public String lyricContent;
  public String midiContent;
  public NELyricType lyricType;
  public NELyric lyric;
  public int preludeTime;

  public LyricBusinessModel() {}

  @Override
  public String toString() {
    return "LyricModel{"
        + "lyricContent='"
        + lyricContent
        + '\''
        + ", midiContent='"
        + midiContent
        + '\''
        + ", lyricType="
        + lyricType
        + '}';
  }
}
