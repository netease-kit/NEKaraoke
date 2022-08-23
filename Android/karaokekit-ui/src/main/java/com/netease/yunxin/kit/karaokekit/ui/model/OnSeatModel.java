// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

import java.io.Serializable;

public class OnSeatModel implements Serializable, Comparable<OnSeatModel> {

  private static final String ACCOUNT_KEY = "account";

  private static final String NICK_KEY = "nick";

  private static final String AVATAR_KEY = "avatar";

  private int index = -1;

  private int status = 0;

  private final String account;

  private final String nick;

  private final String avatar;

  private boolean mute;

  private boolean isSinging;

  private long updated;

  public OnSeatModel(
      int index,
      int status,
      String account,
      String nick,
      String avatar,
      boolean mute,
      boolean isSinging,
      long updated) {
    this.index = index;
    this.status = status;
    this.account = account;
    this.nick = nick;
    this.avatar = avatar;
    this.mute = mute;
    this.isSinging = isSinging;
    this.updated = updated;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getAccount() {
    return account;
  }

  public String getNick() {
    return nick;
  }

  public String getAvatar() {
    return avatar;
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  public boolean isSinging() {
    return isSinging;
  }

  public void setSinging(boolean singing) {
    isSinging = singing;
  }

  public long getUpdated() {
    return updated;
  }

  public void setUpdated(long updated) {
    this.updated = updated;
  }

  @Override
  public int compareTo(OnSeatModel o) {
    return Long.compare(this.updated, o.updated);
  }

  @Override
  public String toString() {
    return "OnSeatModel{"
        + "index="
        + index
        + ", status="
        + status
        + ", account='"
        + account
        + '\''
        + ", nick='"
        + nick
        + '\''
        + ", avatar='"
        + avatar
        + '\''
        + ", mute="
        + mute
        + ", isSinging="
        + isSinging
        + ", updated="
        + updated
        + '}';
  }
}
