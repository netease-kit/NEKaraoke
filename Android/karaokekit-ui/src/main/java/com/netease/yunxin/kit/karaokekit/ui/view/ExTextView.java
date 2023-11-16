// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import com.netease.yunxin.kit.karaokekit.ui.R;

public class ExTextView extends AppCompatTextView {
  private final Drawable enableImg;
  private final Drawable disableImg;

  public ExTextView(Context context) {
    this(context, null);
  }

  public ExTextView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExTextView);
    enableImg = typedArray.getDrawable(R.styleable.ExTextView_enableImg);
    disableImg = typedArray.getDrawable(R.styleable.ExTextView_disableImg);
    typedArray.recycle();
  }

  public ExTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs);
  }

  @Override
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
    if (enableImg == null || disableImg == null) {
      return;
    }
    enableImg.setBounds(1, 1, size, size);
    if (clickable) {
      setTextColor(getResources().getColor(R.color.white));
      setCompoundDrawablesWithIntrinsicBounds(null, enableImg, null, null);
    } else {
      setTextColor(getResources().getColor(R.color.color_80FFFFFF));
      setCompoundDrawablesWithIntrinsicBounds(null, disableImg, null, null);
    }
  }
}
