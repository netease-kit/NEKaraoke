// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.activity.KaraokeRoomActivity;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeRoomModel;

public class NavUtils {

  private static final String TAG = "NavUtil";

  public static void toBrowsePage(Context context, String title, String url) {}

  public static void toKaraokeRoomPage(Context context, KaraokeRoomModel roomModel) {
    if (context == null) {
      ALog.d(TAG, "toKaraokeRoomPage but context == null");
      return;
    }
    Intent intent = new Intent(context, KaraokeRoomActivity.class);
    intent.putExtra(NEKaraokeUIConstants.INTENT_ROOM_MODEL, roomModel);
    context.startActivity(intent);
  }
}
