// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.ui;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.assertViewExist;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.findViewInteraction;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.viewClick;

import android.content.Context;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import com.netease.yunxin.kit.integrationtest.uitest.ClassRegister;
import com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils;
import com.netease.yunxin.kit.karaokekit.ui.view.ExTextView;
import org.hamcrest.Description;

@ClassRegister // 该注解表示被注册的类
public class CommonUITest {
  /**
   * 查看控件是否存在
   *
   * @param text UI控件文案。需要确保界面上只有一个唯一的text文案的控件。否则不一定能查找到对应控件。
   */
  public static void checkComponent(String text) {
    assertViewExist(text);
  }

  /**
   * 点击控件
   *
   * @param text UI控件文案。需要确保界面上只有一个唯一的text文案的控件。否则不一定能查找到对应控件。
   */
  public static void clickComponent(String text) {
    assertViewExist(text);
    viewClick(text);
  }

  /** 返回上级目录 */
  public static void pressBack() {
    BaseTestUtils.pressBack();
  }

  public static ViewInteraction checkLocalTextViewTopDrawable(
      @IdRes int id, @DrawableRes int drawableId) {
    return findViewInteraction(id).check(matches(isAimTextViewTopDrawable(drawableId)));
  }

  public static BoundedMatcher<View, ExTextView> isAimTextViewTopDrawable(@DrawableRes int id) {
    return new BoundedMatcher<View, ExTextView>(ExTextView.class) {
      @Override
      protected boolean matchesSafely(ExTextView item) {
        if (item == null) {
          return false;
        }
        Context context = item.getContext();
        if (context == null) {
          return false;
        }
        return item.getCompoundDrawables()[1].getCurrent().getConstantState()
            == context.getResources().getDrawable(id).getConstantState();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("is same drawable");
      }
    };
  }
}
