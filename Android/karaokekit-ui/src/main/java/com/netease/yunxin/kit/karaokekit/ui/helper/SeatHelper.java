// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.helper;

import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel;
import java.util.List;

public class SeatHelper {

  private static volatile SeatHelper mInstance;

  private SeatHelper() {}

  private List<OnSeatModel> onSeatItems;
  private List<ApplySeatModel> applySeatList;

  public static SeatHelper getInstance() {
    if (null == mInstance) {
      synchronized (SeatHelper.class) {
        if (mInstance == null) {
          mInstance = new SeatHelper();
        }
      }
    }
    return mInstance;
  }

  public List<OnSeatModel> getOnSeatItems() {
    return onSeatItems;
  }

  public void setOnSeatItems(List<OnSeatModel> onSeatItems) {
    this.onSeatItems = onSeatItems;
  }

  public List<ApplySeatModel> getApplySeatList() {
    return applySeatList;
  }

  public void setApplySeatList(List<ApplySeatModel> applySeatList) {
    this.applySeatList = applySeatList;
  }

  public void destroy() {
    if (applySeatList != null) {
      applySeatList.clear();
    }

    if (onSeatItems != null) {
      onSeatItems.clear();
    }
  }
}
