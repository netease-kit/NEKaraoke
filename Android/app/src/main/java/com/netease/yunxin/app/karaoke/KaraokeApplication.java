// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.karaoke.config.AppConfig;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAuthEvent;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKitConfig;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.AuthorConfig;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.LoginType;
import com.tencent.bugly.crashreport.CrashReport;
import java.util.HashMap;
import java.util.Map;

public class KaraokeApplication extends Application {

  private static final String TAG = "KaraokeApplication";
  private static Application application;

  @Override
  public void onCreate() {
    super.onCreate();
    ALog.init(this, ALog.LEVEL_ALL);
    application = this;
    initAuth();
    initKaraokeUI();
    initKaraokeKit(application, AppConfig.getAppKey());
  }

  private void initKaraokeUI() {
    NEKaraokeUI.getInstance().init(application);
  }

  private void initAuth() {
    ALog.i(TAG, "initAuth");
    AuthorConfig authorConfig =
        new AuthorConfig(
            AppConfig.getAppKey(),
            AppConfig.getParentScope(),
            AppConfig.getScope(),
            false);
    authorConfig.setLoginType(LoginType.LANGUAGE_SWITCH);
    AuthorManager.INSTANCE.initAuthor(getApplicationContext(), authorConfig);
  }

  private void initKaraokeKit(Context context, String appKey) {
    ALog.i(TAG, "initKaraokeKit");
    Map<String, String> extras = new HashMap<>();
    extras.put("serverUrl", AppConfig.getServerUrl());
    NEKaraokeKit.getInstance().initialize(context, new NEKaraokeKitConfig(appKey, extras), null);
    NEKaraokeKit.getInstance()
        .addAuthListener(
            evt -> {
              ALog.i(TAG, "onKaraokeAuthEvent evt = " + evt);
              if (evt != NEKaraokeAuthEvent.LOGGED_IN) {
                AuthorManager.INSTANCE.logout(
                    new LoginCallback<Void>() {
                      @Override
                      public void onSuccess(@Nullable Void unused) {
                        ALog.i(TAG, "logout success");
                      }

                      @Override
                      public void onError(int code, @NonNull String msg) {
                        ALog.i(TAG, "logout failed code = " + code + " msg = " + msg);
                      }
                    });
              }
            });
  }
}
