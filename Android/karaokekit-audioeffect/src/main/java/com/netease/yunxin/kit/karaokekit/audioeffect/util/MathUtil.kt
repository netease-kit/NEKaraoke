/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.audioeffect.util

import java.math.BigDecimal

internal object MathUtil {
    /**
     * 相乘
     *
     * @param doubleValA
     * @param doubleValB
     * @return
     */
    fun mul(doubleValA: Double, doubleValB: Double): Double {
        val a2 = BigDecimal(doubleValA)
        val b2 = BigDecimal(doubleValB)
        return a2.multiply(b2).toDouble()
    }

    /**
     * 相除
     *
     * @param doubleValA
     * @param doubleValB
     * @param scale      除不尽时指定精度
     * @return
     */
    fun div(doubleValA: Double, doubleValB: Double, scale: Int): Double {
        val a2 = BigDecimal(doubleValA)
        val b2 = BigDecimal(doubleValB)
        return a2.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }
}
