// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import com.netease.yunxin.kit.entertainment.common.utils.Utils;
import com.netease.yunxin.kit.karaokekit.ui.R;
import java.text.DecimalFormat;

public class StringUtils {

  /**
   * 格式化展示观众数，超过 1w 展示 xxw
   *
   * @param audienceCount 观众实际数
   * @return 观众数字符串
   */
  public static String getAudienceCount(int audienceCount) {
    if (audienceCount < 0) {
      return "0";
    }

    if (audienceCount < 10000) {
      return String.valueOf(audienceCount);
    }
    if (audienceCount < 1000000) {
      DecimalFormat decimalFormat = new DecimalFormat("#.##");
      return decimalFormat.format(Double.valueOf(audienceCount / 10000f))
          + Utils.getApp().getString(R.string.karaoke_ten_thousand);
    }
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    return decimalFormat.format(Double.valueOf(audienceCount / 1000000f))
        + Utils.getApp().getString(R.string.karaoke_million);
  }
}
