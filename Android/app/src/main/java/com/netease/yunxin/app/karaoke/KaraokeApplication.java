// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke;

import android.app.Application;
import com.netease.yunxin.app.karaoke.config.AppConfig;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;

public class KaraokeApplication extends Application {

  private static final String TAG = "VoiceRoomApplication";

  @Override
  public void onCreate() {
    super.onCreate();
    ALog.init(this, ALog.LEVEL_ALL);
    AppConfig.init(this);
    AppStatusManager.init(this);
    initAuth();
    initKaraokeRoomUI();
  }

  private void initAuth() {
    ALog.i(TAG, "initAuth");
  }

  private void initKaraokeRoomUI() {
    NEKaraokeUI.getInstance().init(this);
  }
}
