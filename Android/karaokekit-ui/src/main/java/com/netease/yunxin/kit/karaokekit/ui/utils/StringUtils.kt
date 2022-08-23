/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.utils

import com.blankj.utilcode.util.Utils
import com.netease.yunxin.kit.karaokekit.ui.R
import java.text.DecimalFormat

/**
 * Created by luc on 2020/12/1.
 */
object StringUtils {

    /**
     * 格式化展示观众数，超过 1w 展示 xxw
     *
     * @param audienceCount 观众实际数
     * @return 观众数字符串
     */
    fun getAudienceCount(audienceCount: Int): String? {
        if (audienceCount < 10000) {
            return audienceCount.coerceAtLeast(0).toString()
        }
        if (audienceCount < 1000000) {
            val decimalFormat = DecimalFormat("#.##")
            return decimalFormat.format((audienceCount / 10000f).toDouble()) + Utils.getApp()
                .getString(
                    R.string.karaoke_ten_thousand
                )
        }
        val decimalFormat = DecimalFormat("#.##")
        return decimalFormat.format((audienceCount / 1000000f).toDouble()) + Utils.getApp()
            .getString(
                R.string.karaoke_million
            )
    }
}
