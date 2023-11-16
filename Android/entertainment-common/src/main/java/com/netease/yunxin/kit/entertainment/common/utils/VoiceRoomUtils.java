// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.entertainment.common.utils;

import com.netease.yunxin.kit.corekit.service.XKitServiceManager;

public class VoiceRoomUtils {
  private static final String VOICE_ROOM_SERVICE_NAME = "VoiceRoomKit";

  public static boolean isShowFloatView() {
    Object result =
        XKitServiceManager.Companion.getInstance()
            .callService(VOICE_ROOM_SERVICE_NAME, "isShowFloatView", null);
    return result instanceof Boolean && (boolean) result;
  }

  public static void stopFloatPlay() {
    XKitServiceManager.Companion.getInstance()
        .callService(VOICE_ROOM_SERVICE_NAME, "stopFloatPlay", null);
  }
}
