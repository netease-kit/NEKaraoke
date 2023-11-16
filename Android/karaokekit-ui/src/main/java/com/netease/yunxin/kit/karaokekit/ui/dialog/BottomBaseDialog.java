// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.ui.R;

public abstract class BottomBaseDialog extends Dialog {
  private static final String TAG = "BottomBaseDialog";
  protected View rootView = LayoutInflater.from(getContext()).inflate(contentLayoutId(), null);

  public BottomBaseDialog(@NonNull Activity activity) {
    super(activity, R.style.BottomDialogTheme);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      window.getDecorView().setPadding(0, 0, 0, 0);
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.BOTTOM;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    setContentView(rootView);
    setCanceledOnTouchOutside(true);
  }

  @LayoutRes
  protected int contentLayoutId() {
    return R.layout.karaoke_view_dialog_utils_base;
  }

  /** 页面渲染 */
  protected void renderRootView(View rootView) {
    if (rootView == null) {
      return;
    }
    renderTopView(rootView.findViewById(R.id.fl_dialog_top));
    renderBottomView(rootView.findViewById(R.id.fl_dialog_bottom));
  }

  /**
   * 渲染dialog顶部UI
   *
   * @param parent UI 容器
   */
  protected abstract void renderTopView(FrameLayout parent);

  /**
   * 渲染dialog底部UI
   *
   * @param parent UI 容器
   */
  protected abstract void renderBottomView(FrameLayout parent);

  @Override
  public void show() {
    if (isShowing()) {
      return;
    }
    renderRootView(rootView);
    try {
      super.show();
    } catch (WindowManager.BadTokenException e) {
      ALog.e(TAG, "error message is :" + e.getMessage());
    }
  }
}
