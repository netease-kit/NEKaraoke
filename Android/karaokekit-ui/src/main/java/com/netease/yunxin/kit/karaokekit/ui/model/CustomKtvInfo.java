// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

/** 自定义ktv消息 */
public class CustomKtvInfo {
  public String path; //:audio_mixing_path,    // 通常是 url
  public long ntp_time_stamp; //:123456789, // mileseconds SInt64，服务器时钟时间
  public long audio_offset; //:3232, mileseconds, SInt64, 负数表示等待多长时间后播放，正数表示从某个时间点开始播放，

  public CustomKtvInfo() {}

  public CustomKtvInfo(String path, long ntp_time_stamp, long audio_offset) {
    this();
    this.path = path;
    this.ntp_time_stamp = ntp_time_stamp;
    this.audio_offset = audio_offset;
  }
}
