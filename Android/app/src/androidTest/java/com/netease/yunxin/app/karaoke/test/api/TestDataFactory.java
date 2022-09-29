// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import java.util.HashMap;

public class TestDataFactory {
  private static TestDataFactory sInstance;
  private HashMap<String, Object> storeMap = new HashMap<>();

  private TestDataFactory() {}

  public static TestDataFactory getInstance() {
    if (sInstance == null) {
      sInstance = new TestDataFactory();
    }
    return sInstance;
  }

  public void put(String key, Object value) {
    storeMap.put(key, value);
  }

  public Object get(String key) {
    return storeMap.get(key);
  }
}
