// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.app.karaoke.Constants;
import com.netease.yunxin.app.karaoke.activity.MainActivity;
import com.netease.yunxin.app.karaoke.activity.SplashActivity;
import com.netease.yunxin.app.karaoke.activity.WebViewActivity;
import com.netease.yunxin.app.karaoke.user.AppAboutActivity;
import com.netease.yunxin.kit.alog.ALog;

public class NavUtils {

  private static final String TAG = "NavUtil";

  public static void toSplash(Context context) {
    Intent intent = new Intent(context, SplashActivity.class);
    context.startActivity(intent);
  }

  public static void toMainPage(Context context) {
    Intent intent = new Intent(context, MainActivity.class);
    context.startActivity(intent);
  }

  public static void toBrowsePage(Context context, String title, String url) {
    Intent intent = new Intent(context, WebViewActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Constants.INTENT_KEY_TITLE, title);
    intent.putExtra(Constants.INTENT_KEY_URL, url);

    context.startActivity(intent);
  }

  public static void toAppAboutPage(Context context) {
    if (context == null) {
      ALog.d(TAG, "toAppAboutPage but context == null");
      return;
    }
    context.startActivity(new Intent(context, AppAboutActivity.class));
  }
}
