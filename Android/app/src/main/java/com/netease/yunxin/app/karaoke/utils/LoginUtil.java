// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.utils;

import android.content.Context;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.karaoke.config.AppConfig;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKitConfig;
import java.util.HashMap;
import java.util.Map;
import kotlin.Unit;

public class LoginUtil {
  private static final String TAG = "LoginUtil";

  public static void loginVoiceRoom(
      Context context, NemoAccount nemoAccount, LoginVoiceRoomCallback callback) {
    ALog.i(TAG, "initVoiceRoomKit");
    Map<String, String> extras = new HashMap<>();
    extras.put("serverUrl", AppConfig.getNERoomServerUrl());
    extras.put("baseUrl", AppConfig.getBaseUrl());
    NEKaraokeKit.getInstance()
        .initialize(
            context,
            new NEKaraokeKitConfig(AppConfig.getAppKey(), extras),
            new NEKaraokeCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                ALog.d(TAG, "NEVoiceRoomKit init success");
                loginKaraokeInner(context, nemoAccount, callback);
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                if (callback != null) {
                  callback.onError(
                      code, "NEVoiceRoomKit initialize failed,code:" + code + "，msg:" + msg);
                }
              }
            });
  }

  private static void loginKaraokeInner(
      Context context, NemoAccount nemoAccount, LoginVoiceRoomCallback callback) {
    NEKaraokeKit.getInstance()
        .login(
            nemoAccount.userUuid,
            nemoAccount.userToken,
            new NEKaraokeCallback<Unit>() {

              @Override
              public void onSuccess(@Nullable Unit unit) {
                ALog.d(TAG, "NEVoiceRoomKit login success");
                UserInfoManager.setUserInfo(
                    nemoAccount.userUuid,
                    nemoAccount.userToken,
                    nemoAccount.imToken,
                    nemoAccount.userName,
                    nemoAccount.icon,
                    nemoAccount.mobile);
                UserInfoManager.saveUserInfoToSp(nemoAccount);
                if (callback != null) {
                  callback.onSuccess();
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "NEVoiceRoomKit login failed code = " + code + ", msg = " + msg);
                UserInfoManager.clearUserInfo();
                if (callback != null) {
                  callback.onError(
                      code, "NEVoiceRoomKit login failed code = " + code + ", msg = " + msg);
                }
              }
            });
  }

  public interface LoginVoiceRoomCallback {
    void onSuccess();

    void onError(int errorCode, String errorMsg);
  }
}
