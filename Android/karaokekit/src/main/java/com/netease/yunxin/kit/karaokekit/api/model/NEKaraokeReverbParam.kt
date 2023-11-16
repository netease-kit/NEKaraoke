/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 混响参数
 * @property wetGain 湿信号，取值范围为 0 ~ 1。
 * @property dryGain 干信号，取值范围为 0 ~ 1。
 * @property damping 混响阻尼，取值范围为 0 ~ 1。
 * @property roomSize 房间大小，取值范围为 0.1 ~ 2。
 * @property decayTime 持续强度（余响），取值范围为 0.1 ~ 20。
 * @property preDelay 延迟长度，取值范围为 0 ~ 1。
 */
data class NEKaraokeReverbParam(
    var wetGain: Float = 0.0F,
    var dryGain: Float = 1.0F,
    var damping: Float = 1.0F,
    var roomSize: Float = 0.1F,
    var decayTime: Float = 0.1F,
    var preDelay: Float = 0.0F
)
