// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.integrationtest.base.AbsHandleIntegratedEvent;
import com.netease.yunxin.kit.integrationtest.model.TestItem;

//占位过滤操作
public class HandleCallbackCase extends AbsHandleIntegratedEvent<TestItem> {
  private static final String TAG = "HandleCallbackCase";

  @Override
  public boolean handle(TestItem testItem) throws Exception {
    super.handle(testItem);
    ALog.d(TAG, "methodName:" + methodName);
    // Todo 处理特殊情况的手动解析
    return false;
  }
}
