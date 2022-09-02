// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.Timer;
import java.util.TimerTask;

/** 独唱模块Timer */
public class SoloSingTimer {
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Timer timer = new Timer();
  private TimerTask timerTask =
      new TimerTask() {
        @Override
        public void run() {
          handler.post(runnable);
        }
      };

  private final Runnable runnable;
  private boolean isRunning = false;

  public SoloSingTimer(Runnable runnable) {
    this.runnable = runnable;
  }

  /**
   * 执行定时器
   *
   * @param delay
   * @param period
   */
  public void scheduleAtFixedRate(long delay, long period) {
    if (isRunning) return;
    isRunning = true;
    timer.scheduleAtFixedRate(timerTask, delay, period);
  }

  /** 销毁定时器 */
  public void destroy() {
    if (isRunning) {
      timer.cancel();
      timer = null;
      timerTask.cancel();
      timerTask = null;
    }
  }
}
