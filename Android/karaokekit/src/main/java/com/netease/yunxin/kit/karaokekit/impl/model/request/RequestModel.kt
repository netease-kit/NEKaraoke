/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.model.request

/**
 * @property orderId 点歌编号，action=1 时必传
 * @property deviceParam 判断走串行还是实时合唱的参数
 */
data class InviteChorusRequest(
    val orderId: Long? = null,
    val deviceParam: DeviceParam? = null
)

/**
 * @property chorusId 合唱编号
 * @property deviceParam 判断走串行还是实时合唱的参数
 */
data class JoinChorusRequest(
    val chorusId: String,
    val deviceParam: DeviceParam? = null
)

/**
 * @property chorusId 合唱编号
 */
data class CancelChorusRequest(
    val chorusId: String
)

/**
 * 设备信息，实时合唱使用
 */
data class DeviceParam(
    val playDelay: Long,
    val rtt: Long,
    val wiredHeadset: Int
)

/**
 * @property orderId 点歌编号,不传时chorusId必传
 * @property chorusId 合唱编号，存在时即可不传其他信息.不传时userUuid、orderId必传
 */
data class StartSingRequest(
    val orderId: Long?,
    val chorusId: String?,
    val ext: Map<String, Any>?
)
