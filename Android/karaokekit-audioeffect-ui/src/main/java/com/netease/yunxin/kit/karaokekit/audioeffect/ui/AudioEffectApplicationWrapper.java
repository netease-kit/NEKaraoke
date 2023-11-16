// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.audioeffect.ui;

import android.annotation.SuppressLint;
import android.app.Application;

public class AudioEffectApplicationWrapper {
  private static Application currentApplication;

  /**
   * 获取全局的application
   *
   * @return 返回application
   */
  @SuppressLint("PrivateApi")
  public static Application getNewApplication() {
    try {
      if (currentApplication == null) {
        currentApplication =
            (Application)
                Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null, (Object[]) null);
      }
      return currentApplication;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
