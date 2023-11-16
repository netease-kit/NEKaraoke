/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.karaokekit.pitch.ui.util

import android.util.Log
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object LottieLoader {
    private const val TAG = "LottieLoader"
    private const val LOTTIE_NAME = "data.json"
    fun load(file: String, callback: Callback) {
        GlobalScope.launch(Dispatchers.IO) {
            var `in`: InputStream? = null
            val assetManager = ApplicationWrapper.getNewApplication().assets
            try {
                `in` = assetManager.open(file + File.separator + LOTTIE_NAME)
            } catch (e: IOException) {
                Log.e(TAG, "e:$e")
            }
            try {
                val result = LottieCompositionFactory.fromJsonInputStreamSync(`in`, file)
                val composition = result.value
                if (composition != null) {
                    val drawable = LottieDrawable()
                    drawable.setImagesAssetsFolder(file + File.separator + "images")
                    drawable.composition = composition
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onSuccess(drawable)
                    }
                }
            } catch (e: Exception) {
                GlobalScope.launch(Dispatchers.Main) {
                    callback.onFailed()
                }
            }
        }
    }

    interface Callback {
        fun onSuccess(drawable: LottieDrawable?)
        fun onFailed()
    }
}
