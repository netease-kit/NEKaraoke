/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.impl.utils
import android.content.Context
import com.netease.yunxin.kit.alog.ALog

internal class KaraokeLog {

    companion object {
        private const val prefix = "[KaraokeKit]"

        @JvmStatic
        fun init(context: Context, level: Int) {
            ALog.init(context, level)
        }

        @JvmStatic
        fun i(tag: String, log: String) {
            ALog.i("$prefix $tag", log)
        }

        @JvmStatic
        fun w(tag: String, log: String) {
            ALog.w("$prefix $tag", log)
        }

        @JvmStatic
        fun d(tag: String, log: String) {
            ALog.d("$prefix $tag", log)
        }

        @JvmStatic
        fun e(tag: String, log: String) {
            ALog.e("$prefix $tag", log)
        }

        @JvmStatic
        fun e(tag: String, log: String, throwable: Throwable) {
            ALog.e("$prefix $tag", log, throwable)
        }

        @JvmStatic
        fun logApi(log: String) {
            ALog.iApi(prefix, log)
        }

        @JvmStatic
        fun flush(isFlush: Boolean) {
            ALog.flush(isFlush)
        }
    }
}
