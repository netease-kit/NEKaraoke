/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.model

class KaraokeGiftModel(
    val senderUserUuid: String, // 	打赏者用户编号
    val userName: String, // 	打赏者昵称
    val giftId: Int, // 	礼物编号
    val giftCount: Int // 	礼物数量
)
