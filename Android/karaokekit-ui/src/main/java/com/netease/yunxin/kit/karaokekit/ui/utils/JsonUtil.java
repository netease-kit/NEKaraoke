// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import com.netease.yunxin.kit.alog.ALog;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

  private static final String TAG = "JsonUtil";

  public static JSONObject parse(String json) {
    try {
      return new JSONObject(json);
    } catch (JSONException e) {
      ALog.e(TAG, "parse exception =" + e.getMessage());
      return null;
    }
  }

  public static String getUnescapeJson(String escapeJson) {
    return null;
  }
}
