/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.utils

import android.media.MediaMetadataRetriever

object MediaUtils {
    fun getDuration(path: String): Long {
        val mmr = MediaMetadataRetriever()
        var duration: Long = 0
        try {
            mmr.setDataSource(path)
            val time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = time?.toLong() ?: 0
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            mmr.release()
        }
        return duration
    }
}
