/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.api.model

/**
 * 成员
 * @property account 用户id
 * @property name 用户名
 * @property role 用户角色
 * @property isAudioOn 音频是否打开
 * @constructor
 */
data class NEKaraokeMember(
    val account: String,
    val name: String,
    val role: String,
    val isAudioOn: Boolean
)
