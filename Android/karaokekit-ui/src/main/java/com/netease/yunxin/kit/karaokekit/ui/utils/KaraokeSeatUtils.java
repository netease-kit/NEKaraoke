// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.text.TextUtils;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.ui.helper.SeatHelper;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel;
import java.util.ArrayList;
import java.util.List;

public class KaraokeSeatUtils {

  public static boolean isCurrentOnSeat() {
    return isOnSeat(NEKaraokeKit.getInstance().getLocalMember().getAccount());
  }

  public static boolean isOnSeat(String userUuid) {
    List<OnSeatModel> onSeatItems = SeatHelper.getInstance().getOnSeatItems();
    boolean isOnSeat = false;
    if (onSeatItems != null) {
      for (OnSeatModel seatItem : onSeatItems) {
        if (TextUtils.equals(seatItem.getAccount(), userUuid)) {
          if (seatItem.getStatus() == ApplySeatModel.SEAT_STATUS_ON_SEAT) {
            isOnSeat = true;
            break;
          }
        }
      }
    }
    return isOnSeat;
  }

  public static boolean isCurrentApplyingSeat() {
    return isApplyingSeat(getCurrentUuid());
  }

  public static boolean isApplyingSeat(String userUuid) {
    List<ApplySeatModel> applySeatModels = getApplyingOnSeatList();
    for (ApplySeatModel seatModel : applySeatModels) {
      if (TextUtils.equals(seatModel.getUuid(), userUuid)) {
        return true;
      }
    }
    return false;
  }

  public static List<ApplySeatModel> getApplyingOnSeatList() {
    List<ApplySeatModel> filteredApplySeatItems = new ArrayList<>();
    List<ApplySeatModel> applySeatItems = SeatHelper.getInstance().getApplySeatList();
    if (applySeatItems != null) {
      for (ApplySeatModel seatItem : applySeatItems) {
        if (seatItem.getState() == ApplySeatModel.SEAT_STATUS_PRE_ON_SEAT) {
          filteredApplySeatItems.add(seatItem);
        }
      }
    }
    return filteredApplySeatItems;
  }

  public static String getCurrentUuid() {
    return NEKaraokeKit.getInstance().getLocalMember().getAccount();
  }

  public static String getMemberNick(String uuid) {
    NEKaraokeMember member = KaraokeUIUtils.getMember(uuid);
    if (member != null) {
      return member.getName();
    }
    return "";
  }

  public static List<OnSeatModel> transNESeatItem2OnSeatModel(
      List<NEKaraokeSeatItem> neSeatItemList) {
    List<OnSeatModel> onSeatList = new ArrayList<>();
    for (NEKaraokeSeatItem item : neSeatItemList) {
      OnSeatModel seatModel =
          new OnSeatModel(
              item.getIndex(),
              item.getStatus(),
              item.getUser(),
              item.getUserName(),
              item.getIcon(),
              KaraokeUIUtils.isMute(item.getUser()),
              false,
              item.getUpdated());
      onSeatList.add(seatModel);
    }
    return onSeatList;
  }

  public static boolean isSinging(NEKaraokeSongModel songModel, String userUuid) {
    if (songModel == null) {
      return false;
    }
    if (TextUtils.equals(songModel.getUserUuid(), userUuid)) {
      return true;
    }
    if (TextUtils.equals(songModel.getAssistantUuid(), userUuid)) {
      return true;
    }
    return false;
  }
}
