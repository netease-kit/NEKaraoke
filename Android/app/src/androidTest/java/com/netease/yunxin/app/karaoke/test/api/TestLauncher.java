// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.api;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.netease.yunxin.kit.integrationtest.Hawk;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestLauncher {
  //    @Rule
  //    public GrantPermissionRule mGrantPermissionRule =
  //            GrantPermissionRule.grant("android.permission.ACCESS_NETWORK_STATE","android.permission.INTERNET","android.permission.ACCESS_NETWORK_STATE","android.permission.ACCESS_WIFI_STATE","android.permission.CHANGE_WIFI_STATE","android.permission.WAKE_LOCK","android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA","android.permission.RECORD_AUDIO","android.permission.READ_PHONE_STATE","android.permission.MODIFY_AUDIO_SETTINGS","android.permission.FLASHLIGHT","android.permission.VIBRATE","android.permission.BLUETOOTH","android.permission.FOREGROUND_SERVICE","com.netease.yunxin.app.karaoke.permission.RECEIVE_MSG","android.permission.BROADCAST_STICKY");
  @Before
  public void init() {
    Hawk.getInstance().setContext(ApplicationProvider.getApplicationContext());
    Hawk.getInstance().setRequestInfo("Karaoke", "1.0.0", "");
    Hawk.getInstance()
        .registerClass(
            new ClassFactory(
                ApplicationProvider.getApplicationContext())); // 注册类、callback、方法参数类型等的解析器
    //        IntegrationTest.getInstance().registerParams(new CallKitParamsRegister());
    //        IntegrationTest.getInstance().registerHandle(new CallbackManualParser());
    Hawk.getInstance().setInterceptor(new CaseResultInterceptor());
  }

  @Test
  public void test() {
    Hawk.getInstance().start();
  }
}
