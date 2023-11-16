// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.dialog.CommonDialog;
import java.util.ArrayList;
import java.util.List;

public class KaraokeUIUtils {

  public static String getLocalAccount() {
    if (NEKaraokeKit.getInstance().getLocalMember() == null) {
      return "";
    }
    return NEKaraokeKit.getInstance().getLocalMember().getAccount();
  }

  public static String getLocalName() {
    if (NEKaraokeKit.getInstance().getLocalMember() == null) {
      return "";
    }
    return NEKaraokeKit.getInstance().getLocalMember().getName();
  }

  public static boolean isLocalHost() {
    return NEKaraokeKit.getInstance().getLocalMember() != null
        && TextUtils.equals(
            NEKaraokeKit.getInstance().getLocalMember().getRole(), NEKaraokeUIConstants.ROLE_HOST);
  }

  public static boolean isLocalAccount(String uuid) {
    return NEKaraokeKit.getInstance().getLocalMember() != null
        && TextUtils.equals(NEKaraokeKit.getInstance().getLocalMember().getAccount(), uuid);
  }

  public static boolean isHost(String uuid) {
    NEKaraokeMember member = getMember(uuid);
    if (member == null) {
      return false;
    }
    return TextUtils.equals(member.getRole(), NEKaraokeUIConstants.ROLE_HOST);
  }

  public static boolean isLocalMain(NEKaraokeSongModel songModel) {
    if (songModel == null) {
      return false;
    }
    String uuid = KaraokeUIUtils.getLocalAccount();
    return uuid.equals(songModel.getUserUuid());
  }

  public static boolean isLocalAssistant(NEKaraokeSongModel songModel) {
    if (songModel == null || songModel.getAssistantUuid() == null) {
      return false;
    }
    String uuid = KaraokeUIUtils.getLocalAccount();
    return uuid.equals(songModel.getAssistantUuid());
  }

  public static NEKaraokeMember getMember(String uuid) {
    List<NEKaraokeMember> allMemberList = NEKaraokeKit.getInstance().getAllMemberList();
    for (int i = 0; i < allMemberList.size(); i++) {
      NEKaraokeMember member = allMemberList.get(i);
      if (TextUtils.equals(member.getAccount(), uuid)) {
        return member;
      }
    }
    return null;
  }

  public static boolean isHostRole(String role) {
    if (role == null) {
      return false;
    }
    return TextUtils.equals(role, NEKaraokeUIConstants.ROLE_HOST);
  }

  public static boolean isMute(String uuid) {
    NEKaraokeMember member = getMember(uuid);
    if (member != null) {
      return !member.isAudioOn();
    }
    return true;
  }

  public static boolean isLocalMute() {
    NEKaraokeMember member = NEKaraokeKit.getInstance().getLocalMember();
    if (member != null) {
      return !member.isAudioOn();
    }
    return false;
  }

  public static boolean isLocalSong(NEKaraokeSongModel song) {
    if (song == null) {
      return false;
    }
    return TextUtils.equals(song.getUserUuid(), getLocalAccount());
  }

  public static void showCommonDialog(
      Context context, String title, View.OnClickListener listener) {
    showCommonDialog(context, title, null, null, listener);
  }

  public static void showCommonDialog(
      Context context, String title, String content, View.OnClickListener listener) {
    showCommonDialog(context, title, content, null, listener);
  }

  public static void showCommonDialog(
      Context context,
      String title,
      String content,
      String positiveStr,
      View.OnClickListener listener) {
    CommonDialog dialog = new CommonDialog(context);
    if (!TextUtils.isEmpty(title)) {
      dialog.setTitle(title);
    }
    if (!TextUtils.isEmpty(content)) {
      dialog.setContent(content);
    }
    if (!TextUtils.isEmpty(positiveStr)) {
      dialog.setPositiveBtnName(positiveStr);
    }
    dialog.setPositiveOnClickListener(listener);
    dialog.show();
  }

  public static List<RoomModel> neKaraokeRoomInfos2RoomInfos(
      List<NEKaraokeRoomInfo> voiceRoomInfos) {
    List<RoomModel> result = new ArrayList<>();
    for (NEKaraokeRoomInfo roomInfo : voiceRoomInfos) {
      result.add(neKaraokeRoomInfo2RoomInfo(roomInfo));
    }
    return result;
  }

  public static RoomModel neKaraokeRoomInfo2RoomInfo(NEKaraokeRoomInfo karaokeRoomInfo) {
    if (karaokeRoomInfo == null) {
      return null;
    }
    RoomModel roomModel = new RoomModel();
    roomModel.setRoomUuid(karaokeRoomInfo.getLiveModel().getRoomUuid());
    if (karaokeRoomInfo.getLiveModel().getAudienceCount() != null) {
      roomModel.setAudienceCount(karaokeRoomInfo.getLiveModel().getAudienceCount());
    }
    if (karaokeRoomInfo.getLiveModel().getOnSeatCount() != null) {
      roomModel.setOnSeatCount(karaokeRoomInfo.getLiveModel().getOnSeatCount());
    }
    roomModel.setCover(karaokeRoomInfo.getLiveModel().getCover());
    roomModel.setLiveRecordId(karaokeRoomInfo.getLiveModel().getLiveRecordId());
    roomModel.setRoomName(karaokeRoomInfo.getLiveModel().getLiveTopic());
    roomModel.setAnchorAvatar(karaokeRoomInfo.getAnchor().getAvatar());
    roomModel.setAnchorNick(karaokeRoomInfo.getAnchor().getNick());
    roomModel.setAnchorUserUuid(karaokeRoomInfo.getAnchor().getAccount());
    return roomModel;
  }
}
