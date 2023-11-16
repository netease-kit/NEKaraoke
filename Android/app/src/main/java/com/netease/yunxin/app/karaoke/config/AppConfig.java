// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.config;

import android.annotation.SuppressLint;
import android.content.Context;

public class AppConfig {
  private static final String APP_KEY = "your AppKey"; // 请填写应用对应的AppKey，可在云信控制台的”AppKey管理“页面获取
  public static final String APP_SECRET =
      "your AppSecret"; // 请填写应用对应的AppSecret，可在云信控制台的”AppKey管理“页面获取
  /** 默认的BASE_URL地址仅用于跑通体验Demo，请勿用于正式产品上线。在产品上线前，请换为您自己实际的服务端地址 */
  public static final String BASE_URL = "https://yiyong.netease.im/"; //云信派对服务端国内的体验地址

  private static final int ONLINE_CONFIG_ID = 400;

  @SuppressLint("StaticFieldLeak")
  private static Context sContext;

  public static void init(Context context) {
    if (sContext == null) {
      sContext = context.getApplicationContext();
    }
  }

  public static String getAppKey() {
    return APP_KEY;
  }

  public static int getKaraokeConfigId() {
    return ONLINE_CONFIG_ID;
  }

  public static String getBaseUrl() {
    return BASE_URL;
  }

  public static String getNERoomServerUrl() {
    return "online";
  }
}
