// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import com.netease.yunxin.kit.integrationtest.intercept.InterceptResultCallback;
import com.netease.yunxin.kit.integrationtest.intercept.ResultInterceptor;

public class CaseResultInterceptor implements ResultInterceptor {

  @Override
  public boolean onIntercept(Object obj, InterceptResultCallback callback) {
    return false;
  }
}
