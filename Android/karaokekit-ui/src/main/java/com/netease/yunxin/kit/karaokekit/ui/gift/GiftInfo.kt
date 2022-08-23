/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.newlive.gift

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

/**
 * Created by luc on 2020/11/19.
 */
class GiftInfo(
    /**
     * 礼物id
     */
    val giftId: Int,
    /**
     * 礼物名称
     */
    val name: String?,
    /**
     * 价值云币数量
     */
    val coinCount: Long,
    /**
     * 静态图资源
     */
    @field:DrawableRes val staticIconResId: Int,
    /**
     * 动态图资源
     */
    @field:RawRes val dynamicIconResId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val info = other as GiftInfo?
        return giftId == info?.giftId
    }

    override fun hashCode(): Int {
        return giftId
    }
}
