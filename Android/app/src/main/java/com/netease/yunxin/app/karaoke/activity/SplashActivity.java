// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.karaoke.Constants;
import com.netease.yunxin.app.karaoke.R;
import com.netease.yunxin.app.karaoke.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.ui.activity.AppStatusConstant;
import com.netease.yunxin.kit.karaokekit.ui.activity.AppStatusManager;
import com.netease.yunxin.kit.karaokekit.ui.activity.BaseActivity;
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;
import com.netease.yunxin.kit.login.model.UserInfo;
import java.util.Objects;
import kotlin.Unit;

public class SplashActivity extends BaseActivity {

  private static final String TAG = "SplashActivity";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_NORMAL);
    super.onCreate(savedInstanceState);
    if (!isTaskRoot()) {
      Intent mainIntent = getIntent();
      String action = mainIntent.getAction();
      if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
        finish();
        return;
      }
    }

    setContentView(R.layout.activity_splash);
    AuthorManager.INSTANCE.registerLoginObserver(
        (LoginObserver<LoginEvent>)
            loginEvent -> {
              ALog.d(
                  TAG,
                  "LoginObserver loginEvent = "
                      + loginEvent.getEventType()
                      + " userInfo = "
                      + (loginEvent.getUserInfo() == null
                          ? ""
                          : loginEvent.getUserInfo().toJson()));
              if (loginEvent.getEventType() == EventType.TYPE_LOGIN) {
                login(loginEvent.getUserInfo());
              } else if (loginEvent.getEventType() == EventType.TYPE_LOGOUT) {
                NEKaraokeKit.getInstance()
                    .logout(
                        new NEKaraokeCallback<Unit>() {

                          @Override
                          public void onSuccess(Unit unit) {
                            ALog.d(TAG, "Karaokekit logout success");
                          }

                          @Override
                          public void onFailure(int code, String msg) {
                            ALog.d(
                                TAG, "Karaokekit logout failed code = " + code + " msg = " + msg);
                          }
                        });
              }
            });
    if (!AuthorManager.INSTANCE.isLogin()) {
      AuthorManager.INSTANCE.autoLogin(
          false,
          new LoginCallback<UserInfo>() {

            @Override
            public void onSuccess(UserInfo userInfo) {
              ALog.d(TAG, "autoLogin success");
              login(userInfo);
            }

            @Override
            public void onError(int code, @NonNull String message) {
              ALog.d(TAG, "autoLogin failed code = " + code + " message = " + message);
              AuthorManager.INSTANCE.launchLogin(
                  SplashActivity.this, Constants.MAIN_PAGE_ACTION, false);
            }
          });
    } else {
      login(AuthorManager.INSTANCE.getUserInfo());
    }
  }

  private void login(UserInfo userInfo) {
    if (userInfo == null) {
      ALog.d(TAG, "login but userInfo == null");
      return;
    }

    if (TextUtils.isEmpty(userInfo.getAccountId())) {
      ALog.d(TAG, "login but userInfo.getAccountId() == null");
      return;
    }

    if (TextUtils.isEmpty(userInfo.getAccessToken())) {
      ALog.d(TAG, "login but userInfo.getAccessToken() == null");
      return;
    }

    NEKaraokeKit.getInstance()
        .login(
            Objects.requireNonNull(userInfo.getAccountId()),
            Objects.requireNonNull(userInfo.getAccessToken()),
            new NEKaraokeCallback<Unit>() {

              @Override
              public void onSuccess(Unit unit) {
                ALog.d(TAG, "Karaokekit login success");
                NavUtils.toMainPage(SplashActivity.this);
                finish();
              }

              @Override
              public void onFailure(int code, String msg) {
                ALog.d(TAG, "Karaokekit login failed code = " + code + ", msg = " + msg);
              }
            });
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.d(TAG, "onNewIntent: intent -> " + intent.getData());
    setIntent(intent);
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder().statusBarDarkFont(true).fullScreen(true).build();
  }
}
