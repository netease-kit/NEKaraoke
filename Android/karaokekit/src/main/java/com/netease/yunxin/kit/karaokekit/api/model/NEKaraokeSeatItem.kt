/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSeatItemStatus

/**
 * 单个麦位信息。
 * @property index 麦位位置。
 * @property status 麦位状态，参考[NEKaraokeSeatItemStatus]。
 * @property user 当前状态关联的用户。
 * @property updated 更新时间戳，单位ms。
 */
data class NEKaraokeSeatItem(
    val index: Int,
    val status: Int,
    val user: String?,
    val userName: String?,
    val icon: String?,
    val updated: Long
)
