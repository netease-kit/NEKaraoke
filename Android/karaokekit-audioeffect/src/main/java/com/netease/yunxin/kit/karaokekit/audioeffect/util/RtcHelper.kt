/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.audioeffect.util

import com.netease.lava.nertc.sdk.NERtcEx
import com.netease.lava.nertc.sdk.audio.NERtcReverbParam
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType
import com.netease.lava.nertc.sdk.audio.NERtcVoiceChangerType
import com.netease.lava.nertc.sdk.audio.NERtcVoiceEqualizationBand
import com.netease.yunxin.kit.alog.ALog

internal object RtcHelper {
    var TAG = "RtcHelper"

    /**
     * 设置预设的美声效果
     * 调用该方法可以为本地发流用户设置 SDK 预设的人声美声效果
     * 该方法在加入房间前后都能调用，通话结束后重置为默认关闭状态
     * @param preset 预设的美声效果模式。默认关闭美声效果
     * @see NERtcVoiceBeautifierType
     * @return Int Int 0 方法调用成功，其他失败
     */
    fun setVoiceBeautifierPreset(preset: Int): Int {
        return NERtcEx.getInstance().setVoiceBeautifierPreset(preset)
    }

    /**
     * 设置本地语音混响
     * 该方法在加入房间前后都能调用，通话结束后重置为默认的关闭状态
     * @param param 混响参数
     * @see NERtcReverbParam
     * @return Int 0 方法调用成功，其他失败
     */
    fun setLocalVoiceReverbParam(param: NERtcReverbParam): Int {
        return NERtcEx.getInstance().setLocalVoiceReverbParam(param)
    }

    /**
     * 设置本地语音音效均衡，即自定义设置本地人声均衡波段的中心频率
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @see NERtcVoiceEqualizationBand
     * @param bandGain 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
     * @return Int 0 方法调用成功，其他失败
     */
    fun setLocalVoiceEqualization(bandFrequency: Int, bandGain: Int): Int {
        return NERtcEx.getInstance().setLocalVoiceEqualization(bandFrequency, bandGain)
    }

    /**
     * 设置 SDK 预设的人声的变声音效
     * 设置变声音效可以将人声原音调整为多种特殊效果，改变声音特性
     * @param preset 预设的变声音效。默认关闭变声音效
     * @see NERtcVoiceChangerType
     * @return Int 0 方法调用成功，其他失败
     */
    fun setAudioEffectPreset(preset: Int): Int {
        return NERtcEx.getInstance().setAudioEffectPreset(preset)
    }

    fun enableEarback(enable: Boolean): Int {
        return NERtcEx.getInstance().enableEarback(enable, 100)
    }

    /**
     * 调节采集信号音量。
     * @param volume 采集信号音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）。
     * @return Int
     */
    fun adjustRecordingSignalVolume(volume: Int): Int {
        return NERtcEx.getInstance().adjustRecordingSignalVolume(volume)
    }

    fun setEffectSendVolume(effectId: Int, volume: Int): Int {
        return NERtcEx.getInstance().setEffectSendVolume(effectId, volume)
    }

    fun setEffectPlaybackVolume(effectId: Int, volume: Int): Int {
        return NERtcEx.getInstance().setEffectPlaybackVolume(effectId, volume)
    }

    fun setEarBackVolume(volume: Int): Int {
        return NERtcEx.getInstance().setEarbackVolume(volume)
    }

    fun setEffectPitch(effectId: Int, pitch: Int): Int {
        val result = NERtcEx.getInstance().setEffectPitch(effectId, pitch)
        ALog.i(TAG, "setEffectPitch: effectId=$effectId, pitch=$pitch, result=$result")
        return result
    }

    fun getEffectPitch(effectId: Int): Int {
        val result = NERtcEx.getInstance().getEffectPitch(effectId)
        ALog.i(TAG, "getEffectPitch: effectId=$effectId, result=$result")
        return result
    }

    fun setAudioMixingPitch(pitch: Int): Int {
        val result = NERtcEx.getInstance().setAudioMixingPitch(pitch)
        ALog.i(TAG, "setAudioMixingPitch: pitch=$pitch, result=$result")
        return result
    }

    fun getAudioMixingPitch(): Int {
        val result = NERtcEx.getInstance().audioMixingPitch
        ALog.i(TAG, "getAudioMixingPitch: result=$result")
        return result
    }
}
