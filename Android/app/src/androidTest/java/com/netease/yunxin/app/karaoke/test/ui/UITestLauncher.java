// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.ui;

import android.app.Instrumentation;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import com.netease.yunxin.app.karaoke.activity.SplashActivity;
import com.netease.yunxin.kit.integrationtest.Hawk;
import com.netease.yunxin.kit.integrationtest.uitest.UITestParser;
import com.netease.yunxin.kit.integrationtest.uitest.UITestRegisterHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITestLauncher {

  @Rule
  public ActivityScenarioRule<SplashActivity> splashRuler =
      new ActivityScenarioRule<>(SplashActivity.class); // 此行代码仅在UI测试时需要添加，用于启动应用界面首页；非UI测试不需要添加

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(
          "android.permission.CAMERA",
          "android.permission.READ_EXTERNAL_STORAGE",
          "android.permission.RECORD_AUDIO",
          "android.permission.WRITE_EXTERNAL_STORAGE"); // 用于权限声明

  private final Hawk hawk = Hawk.getInstance(); // 初始化自动化测试类

  @Before
  public void init() {
    // 用于强化UI测试调试能力
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    UiDevice uiDevice = UiDevice.getInstance(instrumentation);
    UITestRegisterHelper.setDevice(uiDevice);

    hawk.setContext(ApplicationProvider.getApplicationContext());
    hawk.setRequestInfo("KaraokeUI", "1.0.0", "");
    hawk.registerHandle(new UITestParser()); // 注册UI自动化回调，此行代码仅在UI测试时调用
  }

  @Test
  public void test() {
    UITestRegisterHelper.registerClass(
        UITestBaseForKaraoke.class, CommonUITest.class); // 注册自动化的类与方法
    //    UITestRegisterHelper.setParamsAllInFirst(true);
    hawk.start(); // 启动自动化测试
    UITestRegisterHelper.releaseAll(); // 释放所有资源
  }
}
