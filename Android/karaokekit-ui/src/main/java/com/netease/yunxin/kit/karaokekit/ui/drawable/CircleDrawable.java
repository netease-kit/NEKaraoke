// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.drawable;

import android.graphics.drawable.GradientDrawable;

/** Created on 2020/10/16 Author: wangxiaojie Description: */
public class CircleDrawable extends GradientDrawable {

  public CircleDrawable() {
    super.setShape(OVAL);
  }

  public CircleDrawable(Orientation orientation, int[] colors) {
    super(orientation, colors);
    super.setShape(OVAL);
  }

  @Override
  public void setShape(int shape) {}

  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    int width = right - left;
    int height = bottom - top;
    int realWidth = getIntrinsicWidth();
    int realHeight = getIntrinsicHeight();
    float offsetX = 0;
    float offsetY = 0;

    if (width > realWidth) {
      offsetX = width - realWidth;
      width = realWidth;
    }

    if (height > realHeight) {
      offsetY = height - realHeight;
      height = realHeight;
    }

    if (width > height) {
      offsetX += width - height;
    } else if (width < height) {
      offsetY += height - width;
    }

    if (offsetX != 0) {
      int temp = (int) (offsetX / 2f);
      left += temp;
      right -= temp;
    }

    if (offsetY != 0) {
      int temp = (int) (offsetY / 2f);
      top += temp;
      bottom -= temp;
    }
    super.setBounds(left, top, right, bottom);
  }

  @Override
  public void setHotspotBounds(int left, int top, int right, int bottom) {
    int width = right - left;
    int height = bottom - top;
    int realWidth = getIntrinsicWidth();
    int realHeight = getIntrinsicHeight();
    float offsetX = 0;
    float offsetY = 0;

    if (width > realWidth) {
      offsetX = width - realWidth;
      width = realWidth;
    }

    if (height > realHeight) {
      offsetY = height - realHeight;
      height = realHeight;
    }

    if (width > height) {
      offsetX += width - height;
    } else if (width < height) {
      offsetY += height - width;
    }

    if (offsetX != 0) {
      int temp = (int) (offsetX / 2f);
      left += temp;
      right -= temp;
    }

    if (offsetY != 0) {
      int temp = (int) (offsetY / 2f);
      top += temp;
      bottom -= temp;
    }
    super.setHotspotBounds(left, top, right, bottom);
  }
}
