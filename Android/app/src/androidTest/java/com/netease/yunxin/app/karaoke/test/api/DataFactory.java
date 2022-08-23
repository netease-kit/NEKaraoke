// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import java.util.HashMap;
import java.util.Map;

public class DataFactory {

  private static DataFactory instance = new DataFactory();

  public final Map<String, Object> globalStoreMap = new HashMap<>();

  private DataFactory() {}

  public static DataFactory getInstance() {
    return instance;
  }
}
