// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import com.netease.yunxin.kit.karaokekit.ui.activity.BaseActivity;

public class InputUtils {

  /**
   * 展示软键盘
   *
   * @param inputView 输入框
   */
  public static void showSoftInput(View inputView) {
    if (inputView == null) {
      return;
    }
    InputMethodManager service =
        (InputMethodManager)
            inputView
                .getContext()
                .getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    inputView.setVisibility(View.VISIBLE);
    inputView.requestFocus();
    service.showSoftInput(inputView, 0);
  }

  /**
   * 隐藏软键盘
   *
   * @param inputView 输入框
   */
  public static void hideSoftInput(View inputView) {
    if (inputView == null) {
      return;
    }
    InputMethodManager service =
        (InputMethodManager)
            inputView
                .getContext()
                .getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    service.hideSoftInputFromWindow(inputView.getWindowToken(), 0);
  }

  /**
   * 添加软键盘弹起高度注册监听
   *
   * @param activity 软键盘所在 页面
   */
  public static void registerSoftInputListener(BaseActivity activity, InputParamHelper helper) {
    View rootView = activity.getWindow().getDecorView();
    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {

          @Override
          public void onGlobalLayout() {
            if (helper.getHeight() <= 0) {
              return;
            }
            Rect outRect = new Rect();
            rootView.getWindowVisibleDisplayFrame(outRect);
            int differ = helper.getHeight() - outRect.bottom;
            if (differ == 0) {
              if (helper.getInputView().getVisibility() == View.VISIBLE) {
                helper.getInputView().setVisibility(View.GONE);
                helper.getInputView().setText("");
                ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) helper.getInputView().getLayoutParams();
                layoutParams.bottomMargin = 0;
              }
              return;
            }
            helper.getInputView().setVisibility(View.VISIBLE);
            helper.getInputView().requestFocus();
            ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) helper.getInputView().getLayoutParams();
            layoutParams.bottomMargin = differ;
          }
        };

    rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

    // 反注册
    activity
        .getLifecycle()
        .addObserver(
            (LifecycleEventObserver)
                (source, event) -> {
                  if (event == Lifecycle.Event.ON_DESTROY
                      || event == Lifecycle.Event.ON_PAUSE && activity.isFinishing()) {
                    rootView
                        .getViewTreeObserver()
                        .removeOnGlobalLayoutListener(onGlobalLayoutListener);
                  }
                });
  }

  /** 获取当前屏幕尺寸 */
  public interface InputParamHelper {
    /** 获取屏幕高度 */
    int getHeight();

    /** 获取需要输入的view */
    EditText getInputView();
  }
}
