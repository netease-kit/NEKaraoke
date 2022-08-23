// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode;
import java.io.Serializable;

public class KaraokeRoomModel implements Serializable {

  private long liveRecordId;
  private String roomUuid;
  private String role;
  private String nick;
  private NEKaraokeSongMode mode;
  private String roomName;

  public long getLiveRecordId() {
    return liveRecordId;
  }

  public void setLiveRecordId(long liveRecordId) {
    this.liveRecordId = liveRecordId;
  }

  public String getRoomUuid() {
    return roomUuid;
  }

  public void setRoomUuid(String roomUuid) {
    this.roomUuid = roomUuid;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public NEKaraokeSongMode getMode() {
    return mode;
  }

  public void setMode(NEKaraokeSongMode mode) {
    this.mode = mode;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }
}
