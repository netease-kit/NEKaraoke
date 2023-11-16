/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.util

import com.netease.yunxin.kit.alog.ALog

object PitchUILog {
    private const val prefix = "[PitchUILog]"

    @JvmStatic
    fun d(tag: String, msg: String) {
        ALog.d("$prefix $tag", msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        ALog.e("$prefix $tag", msg)
    }

    @JvmStatic
    fun flush(isFlush: Boolean) {
        ALog.flush(isFlush)
    }
}
