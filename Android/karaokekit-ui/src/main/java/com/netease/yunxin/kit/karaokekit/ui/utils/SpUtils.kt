/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.utils

import android.content.Context

/**
 * Created by luc on 2020/11/11.
 */
object SpUtils {
    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     */
    fun getScreenWidth(context: Context): Int {
        return context.applicationContext.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     */
    fun getScreenHeight(context: Context): Int {
        return context.applicationContext.resources.displayMetrics.heightPixels
    }

    /**
     * dp 转换成 pixel
     */
    fun dp2pix(context: Context, dp: Float): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (density * dp + 0.5f).toInt()
    }
}
