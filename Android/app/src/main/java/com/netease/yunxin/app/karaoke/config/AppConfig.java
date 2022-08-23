// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.config;

public class AppConfig {
  private static final String APP_KEY = "your app key";
  private static final int PARENT_SCOPE_ONLINE = 5;
  private static final int SCOPE_ONLINE = 5;

  public static String getAppKey() {
      return APP_KEY;
  }

  public static int getParentScope() {
    return PARENT_SCOPE_ONLINE;
  }

  public static int getScope() {
    return SCOPE_ONLINE;
  }

  public static String getServerUrl() {
    return "";
  }
}
