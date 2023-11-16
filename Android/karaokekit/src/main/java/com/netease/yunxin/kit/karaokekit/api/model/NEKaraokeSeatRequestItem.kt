/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 成员麦位申请信息。
 * @property index 麦位位置。如果为**-1**，表示未指定位置。
 * @property user 申请人。
 */
data class NEKaraokeSeatRequestItem(
    val index: Int,
    val user: String,
    val userName: String?,
    val icon: String?
)
