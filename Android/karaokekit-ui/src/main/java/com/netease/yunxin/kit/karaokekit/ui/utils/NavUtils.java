// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.entertainment.common.activity.AuthenticateActivity;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.ui.activity.KaraokeRoomActivity;

public class NavUtils {

  private static final String TAG = "NavUtil";

  public static void toBrowsePage(Context context, String title, String url) {}

  public static void toKaraokeRoomPage(
      Context context, String username, String avatar, NEKaraokeRoomInfo roomInfo) {
    if (context == null) {
      ALog.d(TAG, "toKaraokeRoomPage but context == null");
      return;
    }
    RoomModel roomModel = new RoomModel();
    roomModel.setLiveRecordId(roomInfo.getLiveModel().getLiveRecordId());
    roomModel.setRoomUuid(roomInfo.getLiveModel().getRoomUuid());
    roomModel.setRole(RoomConstants.ROLE_HOST);
    roomModel.setRoomName(roomInfo.getLiveModel().getLiveTopic());
    roomModel.setNick(username);
    roomModel.setAvatar(avatar);
    roomModel.setAnchorAvatar(roomInfo.getAnchor().getAvatar());
    roomModel.setAnchorNick(roomInfo.getAnchor().getNick());
    roomModel.setAnchorUserUuid(roomInfo.getAnchor().getAccount());
    roomModel.setCover(roomInfo.getLiveModel().getCover());
    Intent intent = new Intent(context, KaraokeRoomActivity.class);
    intent.putExtra(RoomConstants.INTENT_ROOM_MODEL, roomModel);
    context.startActivity(intent);
  }

  public static void toKaraokeRoomPage(
      Context context, String username, String avatar, RoomModel roomInfo) {
    if (context == null) {
      ALog.d(TAG, "toKaraokeRoomPage but context == null");
      return;
    }
    RoomModel roomModel = new RoomModel();
    roomModel.setLiveRecordId(roomInfo.getLiveRecordId());
    roomModel.setRoomUuid(roomInfo.getRoomUuid());
    roomModel.setRole(RoomConstants.ROLE_AUDIENCE);
    roomModel.setRoomName(roomInfo.getRoomName());
    roomModel.setNick(username);
    roomModel.setAvatar(avatar);
    roomModel.setAnchorAvatar(roomInfo.getAnchorAvatar());
    roomModel.setAnchorNick(roomInfo.getAnchorNick());
    roomModel.setAnchorUserUuid(roomInfo.getAnchorUserUuid());
    roomModel.setCover(roomInfo.getCover());
    Intent intent = new Intent(context, KaraokeRoomActivity.class);
    intent.putExtra(RoomConstants.INTENT_ROOM_MODEL, roomModel);
    context.startActivity(intent);
  }

  public static void toAuthenticateActivity(Context context) {
    Intent intent = new Intent(context, AuthenticateActivity.class);
    context.startActivity(intent);
  }
}
