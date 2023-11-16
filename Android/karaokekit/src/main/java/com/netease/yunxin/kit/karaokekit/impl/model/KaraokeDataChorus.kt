/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.impl.model

/**
 * 副歌资源
 */
class KaraokeDataChorus {
    var startTime: Long = // 副歌开始时间
        0
    var stopTime: Long = // 副歌结束时间
        0

    override fun toString(): String {
        return "KaraokeDataChorus{" +
            "startTime=" + startTime +
            ", stopTime=" + stopTime +
            '}'
    }
}
