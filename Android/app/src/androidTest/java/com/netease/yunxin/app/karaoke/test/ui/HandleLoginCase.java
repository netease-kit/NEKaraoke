// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.ui;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.integrationtest.base.AbsHandleIntegratedEvent;
import com.netease.yunxin.kit.integrationtest.model.TestItem;
import com.netease.yunxin.kit.integrationtest.uitest.ClassRegister;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
@ClassRegister
public class HandleLoginCase extends AbsHandleIntegratedEvent<TestItem> {
  private static final String TAG = "HandleCallbackCase";

  @Override
  public boolean handle(TestItem testItem) throws Exception {
    super.handle(testItem);
    ALog.d(TAG, "methodName:" + methodName);
    //TODO UI自动化适配
    return false;
  }
}
