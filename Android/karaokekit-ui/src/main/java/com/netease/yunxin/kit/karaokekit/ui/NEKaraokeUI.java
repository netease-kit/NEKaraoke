// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui;

import android.app.Application;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;

public class NEKaraokeUI {
  private static final String TAG = "NEKaraokeUI";
  private static volatile NEKaraokeUI instance;
  private Application application;
  private NEKaraokeUIStateListener stateListener;

  private NEKaraokeUI() {}

  public static NEKaraokeUI getInstance() {
    if (instance == null) {
      synchronized (NEKaraokeUI.class) {
        if (instance == null) {
          instance = new NEKaraokeUI();
        }
      }
    }
    return instance;
  }

  public Application getApplication() {
    return application;
  }

  public void init(Application application) {
    this.application = application;
  }

  public NEKaraokeUIStateListener getStateListener() {
    return stateListener;
  }

  public void setStateListener(NEKaraokeUIStateListener stateListener) {
    this.stateListener = stateListener;
  }

  public void notifyEnterRoom() {
    if (stateListener != null) {
      stateListener.onEnterRoom();
    }
  }

  public void notifyExitRoom() {
    if (stateListener != null) {
      stateListener.onExitRoom();
    }
  }

  public void exitVoiceRoom(NEKaraokeCallback<Void> callback) {
    if (stateListener != null) {
      stateListener.exitVoiceRoom(callback);
    }
  }

  public interface NEKaraokeUIStateListener {
    void onEnterRoom();

    void onExitRoom();

    void exitVoiceRoom(NEKaraokeCallback<Void> callback);
  }
}
