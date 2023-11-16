// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.utils;

import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import java.util.Locale;

public class AppUtils {

  // 当前系统是否是中文简体
  public static boolean isMainLand() {
    return Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage());
  }

  public static String getUserName() {
    return UserInfoManager.getSelfNickname();
  }

  public static String getAvatar() {
    return UserInfoManager.getSelfImAvatar();
  }
}
