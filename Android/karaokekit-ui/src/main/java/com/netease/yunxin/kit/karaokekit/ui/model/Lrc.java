// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

/** 歌词 */
public class Lrc {
  private long time;
  private String text;

  public void setTime(long time) {
    this.time = time;
  }

  public void setText(String text) {
    this.text = text;
  }

  public long getTime() {
    return time;
  }

  public String getText() {
    return text;
  }
}
