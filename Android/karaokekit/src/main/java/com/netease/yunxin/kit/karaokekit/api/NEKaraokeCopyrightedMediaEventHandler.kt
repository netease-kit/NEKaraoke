/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api

/**
 * 注册事件通知回调
 */
interface NEKaraokeCopyrightedMediaEventHandler {
    /**
     * 用户Token过期
     */
    fun onTokenExpired()
}
