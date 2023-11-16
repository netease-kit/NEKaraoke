/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.impl.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 直播间直播信息
 */
@Keep
class KaraokeRoomInfo : Serializable {
    @SerializedName("anchor")
    lateinit var anchor: KaraokeAnchor // 主播信息
    @SerializedName("live")
    lateinit var liveModel: KaraokeLiveModel // 房间信息
    override fun toString(): String {
        return "KaraokeRoomInfo(anchor=$anchor, live=$liveModel)"
    }
}
