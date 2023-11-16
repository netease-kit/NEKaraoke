/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 礼物
 * @property senderAccount 发送者账号
 * @property sendNick 发送者昵称
 * @property giftId 礼物id
 * @constructor
 */
class NEKaraokeBatchGiftModel(
    private val senderAccount: String, // 	打赏者账号
    val sendNick: String, // 	打赏者昵称
    val giftId: Int // 	礼物编号
)
