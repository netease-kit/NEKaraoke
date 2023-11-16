// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.pitch.ui.model;

public class NEKTVPlayResultModel {
  /** 音乐名称 */
  public String songName;
  /** 用户昵称 */
  public String nickName;
  /** 头像链接 */
  public String headerUrl;

  public NEKTVPlayResultModel(String songName, String nickName, String headerUrl) {
    this.songName = songName;
    this.nickName = nickName;
    this.headerUrl = headerUrl;
  }

  @Override
  public String toString() {
    return "NEKTVPlayResultModel{"
        + "songName='"
        + songName
        + '\''
        + ", nickName='"
        + nickName
        + '\''
        + ", headerUrl='"
        + headerUrl
        + '\''
        + '}';
  }
}
