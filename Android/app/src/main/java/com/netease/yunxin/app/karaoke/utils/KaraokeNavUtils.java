// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.app.karaoke.activity.CommonSettingActivity;
import com.netease.yunxin.app.karaoke.config.AppConfig;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.R;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.karaokekit.ui.activity.KaraokeRoomListActivity;

public class KaraokeNavUtils {

  public static void toCommonSettingPage(Context context) {
    Intent intent = new Intent(context, CommonSettingActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  public static void toKaraokeRoomListPage(Context context) {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.INSTANCE.showShortToast(context, context.getString(R.string.network_error));
      return;
    }
    Intent intent = new Intent(context, KaraokeRoomListActivity.class);
    intent.putExtra(RoomConstants.INTENT_IS_OVERSEA, false);
    intent.putExtra(RoomConstants.INTENT_KEY_CONFIG_ID, AppConfig.getKaraokeConfigId());
    intent.putExtra(RoomConstants.INTENT_USER_NAME, AppUtils.getUserName());
    intent.putExtra(RoomConstants.INTENT_AVATAR, AppUtils.getAvatar());
    context.startActivity(intent);
  }
}
