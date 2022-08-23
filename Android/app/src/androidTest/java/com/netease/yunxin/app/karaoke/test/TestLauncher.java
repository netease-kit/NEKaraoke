// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import com.netease.yunxin.app.karaoke.activity.SplashActivity;
import com.netease.yunxin.app.karaoke.test.api.HandleCallbackCase;
import com.netease.yunxin.app.karaoke.test.ui.HandleLoginCase;
import com.netease.yunxin.kit.integrationtest.IntegrationTest;
import com.netease.yunxin.kit.integrationtest.apm.APMHandle;
import com.netease.yunxin.kit.integrationtest.base.AbsHandleIntegratedEvent;
import com.netease.yunxin.kit.integrationtest.model.TestItem;
import com.netease.yunxin.perfkit.Perfkit;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestLauncher {

  @Rule
  public ActivityTestRule<SplashActivity> mActivityRule =
      new ActivityTestRule<>(SplashActivity.class);

  @Rule
  public GrantPermissionRule mGrantPermissionRule =
      GrantPermissionRule.grant(
          "android.permission.INTERNET",
          "android.permission.ACCESS_NETWORK_STATE",
          "android.permission.ACCESS_WIFI_STATE",
          "android.permission.CHANGE_WIFI_STATE",
          "android.permission.WAKE_LOCK",
          "android.permission.READ_EXTERNAL_STORAGE",
          "android.permission.WRITE_EXTERNAL_STORAGE",
          "android.permission.CAMERA",
          "android.permission.RECORD_AUDIO",
          "android.permission.READ_PHONE_STATE",
          "android.permission.MODIFY_AUDIO_SETTINGS",
          "android.permission.FOREGROUND_SERVICE",
          "android.permission.BLUETOOTH",
          "android.permission.BROADCAST_STICKY");

  private static List<AbsHandleIntegratedEvent<TestItem>> getHandleIntegratedEventList() {
    List<AbsHandleIntegratedEvent<TestItem>> list = new ArrayList<>();
    list.add(new HandleCallbackCase());
    list.add(new HandleLoginCase());
    return list;
  }

  @Before
  public void init() {
    IntegrationTest.getInstance().setContext(ApplicationProvider.getApplicationContext());
    IntegrationTest.getInstance().setRequestInfo("Karaoke", "1.0.0", "1.3.0");
    //        IntegrationTest.getInstance().registerClass(new ClassFactory(ApplicationProvider.getApplicationContext()));
    IntegrationTest.getInstance().registerHandle(TestLauncher::getHandleIntegratedEventList);
    IntegrationTest.getInstance().setInterceptor(new CaseResultInterceptor());
    IntegrationTest.getInstance()
        .setAPMHandle(
            new APMHandle() {
              @Override
              public void onStart(String missionId) {
                Perfkit.getInstance().startEventReporter(missionId);
              }

              @Override
              public void onEnd(String missionId) {
                Perfkit.getInstance().stopEventReporter(missionId);
              }
            });

    Perfkit.getInstance()
        .initEventReporter(
            ApplicationProvider.getApplicationContext(), "", "Karaoke", "1.0.0", "online");
  }

  @Test
  public void test() {
    IntegrationTest.getInstance().start();
  }
}
