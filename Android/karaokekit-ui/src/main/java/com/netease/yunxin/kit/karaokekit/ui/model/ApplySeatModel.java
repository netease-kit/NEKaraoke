// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

public class ApplySeatModel {
  public static final int SEAT_STATUS_CLOSE = -1;
  public static final int SEAT_STATUS_NO_PERSON = 0;
  public static final int SEAT_STATUS_PRE_ON_SEAT = 1;
  public static final int SEAT_STATUS_ON_SEAT = 2;

  private String uuid;
  private String nick;
  private String avatar;
  private int state;

  public ApplySeatModel(String uuid, String nick, String avatar, int state) {
    this.uuid = uuid;
    this.nick = nick;
    this.avatar = avatar;
    this.state = state;
  }

  public String getUuid() {
    return uuid;
  }

  public String getAvatar() {
    return avatar;
  }

  public String getNick() {
    return nick;
  }

  public int getState() {
    return state;
  }
}
