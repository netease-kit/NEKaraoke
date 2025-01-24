/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.audioeffect.api

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import com.netease.lava.nertc.sdk.audio.NERtcReverbParam
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType
import com.netease.lava.nertc.sdk.audio.NERtcVoiceChangerType
import com.netease.lava.nertc.sdk.audio.NERtcVoiceEqualizationBand
import com.netease.yunxin.kit.karaokekit.audioeffect.util.MathUtil
import com.netease.yunxin.kit.karaokekit.audioeffect.util.RtcHelper

/**
 * 音效管理类
 */
object NEAudioEffectManager {

    private const val DEFAULT_EAR_BACK_VOLUME = 50
    private const val DEFAULT_AUDIO_MIXING_VOLUME = 50
    const val DEFAULT_RECORDING_SIGNAL_VOLUME = 100
    private const val DEFAULT_IN_KTV_RECORDING_SIGNAL_VOLUME = 80
    const val DEFAULT_OTHER_SIGNAL_VOLUME = 100
    private const val DEFAULT_IN_KTV_OTHER_SIGNAL_VOLUME = 10

    /**
     * 耳返开启状态
     */
    private var enableEarBack = false

    /**
     * 耳返音量
     */
    private var earBackVolume = DEFAULT_EAR_BACK_VOLUME

    /**
     * 伴奏音量
     */
    private var audioMixingVolume = DEFAULT_AUDIO_MIXING_VOLUME

    /**
     * 人声音量，默认80
     */
    private var recordingSignalVolume = DEFAULT_IN_KTV_RECORDING_SIGNAL_VOLUME

    /**
     * 其他人音量，默认10
     */
    private var otherSignalVolume = DEFAULT_IN_KTV_OTHER_SIGNAL_VOLUME

    /**
     * 开启或耳返。
     * 开启耳返功能后，必须连接上耳机或耳麦，才能正常使用耳返功能
     * @param enable true 开启耳返功能 false 默认）关闭耳返功能
     * @return 0表示方法调用成功，其他失败。
     */
    fun enableEarBack(enable: Boolean): Int {
        enableEarBack = enable
        return RtcHelper.enableEarback(enable)
    }

    /**
     * 设置耳返音量
     * @param volume Int
     * @return Int
     */
    fun setEarBackVolume(volume: Int): Int {
        earBackVolume = volume
        return RtcHelper.setEarBackVolume(volume)
    }

    /**
     * 获取耳返开启状态
     * @return true 耳返开启 false 耳返关闭
     */
    fun isEarBackEnable(): Boolean {
        return enableEarBack
    }

    /**
     * 调节人声音量
     * @param volume Int 采集信号音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）。
     * @return 0表示方法调用成功，其他失败。
     */
    fun adjustRecordingSignalVolumeWithRemember(volume: Int): Int {
        recordingSignalVolume = volume
        return RtcHelper.adjustRecordingSignalVolume(volume)
    }

    /**
     * 调节人声音量
     * @param volume Int 采集信号音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）。
     * @return 0表示方法调用成功，其他失败。
     */
    fun adjustRecordingSignalVolume(volume: Int): Int {
        return RtcHelper.adjustRecordingSignalVolume(volume)
    }

    /**
     * 调节其他人人声音量
     * @param volume Int 采集信号音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）。
     * @return 0表示方法调用成功，其他失败。
     */
    fun adjustPlaybackSignalVolumeWithRemember(volume: Int): Int {
        otherSignalVolume = volume
        return RtcHelper.adjustPlaybackSignalVolume(volume)
    }

    /**
     * 调节其他人人声音量
     * @param volume Int 采集信号音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）。
     * @return 0表示方法调用成功，其他失败。
     */
    fun adjustPlaybackSignalVolume(volume: Int): Int {
        return RtcHelper.adjustPlaybackSignalVolume(volume)
    }

    /**
     * 获取耳返音量
     * @return 耳返音量，取值范围为 [0, 100]。
     */
    fun getEarBackVolume(): Int {
        return earBackVolume
    }

    /**
     * 获取人声音量
     * @return 人声音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）
     */
    fun getRecordingSignalVolume(): Int {
        return recordingSignalVolume
    }

    /**
     * 获取其他人人声音量
     * @return 人声音量，取值范围为 [0, 400]。其中：0：静音。100：（默认）原始音量。400：最大可为原始音量的 4 倍（自带溢出保护）
     */
    fun getOtherSignalVolume(): Int {
        return otherSignalVolume
    }

    /**
     * 调节伴奏音量
     * @param effectId 指定音效的 ID。每个音效均有唯一的 ID
     * @param volume 取值范围为 0~100。默认 100
     */
    fun setAudioMixingVolume(effectId: Int, volume: Int) {
        RtcHelper.setEffectSendVolume(effectId, volume)
        RtcHelper.setEffectPlaybackVolume(effectId, volume)
        audioMixingVolume = volume
    }

    /**
     * 获取伴奏音量
     * @return 伴奏音量，取值范围为 0~100。默认 100
     */
    fun getAudioMixingVolume(): Int {
        return audioMixingVolume
    }

    /**
     * 设置混响音效
     * @param preset 混响预设值
     * @see NERtcVoiceBeautifierType
     * @return 0表示方法调用成功，其他失败。
     */
    fun setReverbPreset(preset: Int): Int {
        return RtcHelper.setVoiceBeautifierPreset(preset)
    }

    fun setEffectPitch(effectId: Int, pitch: Int): Int {
        return RtcHelper.setEffectPitch(effectId, pitch)
    }

    fun getEffectPitch(effectId: Int): Int {
        return RtcHelper.getEffectPitch(effectId)
    }

    fun setAudioMixingPitch(pitch: Int): Int {
        return RtcHelper.setAudioMixingPitch(pitch)
    }

    fun getAudioMixingPitch(): Int {
        return RtcHelper.getAudioMixingPitch()
    }

    /**
     * 设置混响音效强度
     * @param intensity 强度 0-100
     * @return 0表示方法调用成功，其他失败。
     */
    fun setReverbIntensity(intensity: Int): Int {
//        setLocalVoiceReverbParam   wetGain默认0.5           10%强度则把 wetGain设置为0.05
        val reverbParam = NERtcReverbParam()
        // 该参数的单位为分贝（dB），取值范围为 0 ~ 1，默认值为 0.0。
        reverbParam.wetGain = intensity / 100f
        return setReverbParam(reverbParam)
    }

    /**
     * 设置均衡音效
     * @param preset 均衡预设值
     * @see NERtcVoiceBeautifierType
     * @return 0表示方法调用成功，其他失败。
     */
    fun setEqualizePreset(preset: Int): Int {
        return RtcHelper.setVoiceBeautifierPreset(preset)
    }

    /**
     * 设置自定义均衡音效
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @see NERtcVoiceEqualizationBand
     * @param bandGain 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
     *
     */
    fun setCustomEqualization(bandFrequency: Int, bandGain: Int) {
        RtcHelper.setVoiceBeautifierPreset(NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_MUFFLED)
        RtcHelper.setLocalVoiceEqualization(bandFrequency, bandGain)
    }

    /**
     * 设置均衡音效强度
     * @param intensity 强度 0-100
     */
    fun setEqualizeIntensity(intensity: Int) {
        // bandGain 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
        val bandGain =
            MathUtil.mul((MathUtil.div(15.0, 50.0, 1)), ((intensity - 50).toDouble())).toInt()
        Log.d("setEqualizeIntensity", "bandGain:$bandGain")
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_31,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_62,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_125,
            bandGain
        )
        RtcHelper.setLocalVoiceEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_250,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_500,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_1K,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_2K,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_4K,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_8K,
            bandGain
        )
        setLocalAudioEqualization(
            NERtcVoiceEqualizationBand.VoiceEqualizationBand_16K,
            bandGain
        )
    }

    /**
     * 重置均衡音效参数
     */
    fun resetEqualizeParam() {
        RtcHelper.setVoiceBeautifierPreset(NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_OFF)
        setEqualizeIntensity(50)
    }

    /**
     * 设置变声音效
     * @param preset 预设的变声音效。默认关闭变声音效
     * @see NERtcVoiceChangerType
     * @return 0表示方法调用成功，其他失败。
     */
    fun setVoiceChangerPreset(preset: Int): Int {
        return RtcHelper.setAudioEffectPreset(preset)
    }

    /**
     * 设置混响音效参数
     * @param param 混响参数
     * @return 0表示方法调用成功，其他失败。
     */
    private fun setReverbParam(param: NERtcReverbParam): Int {
        return RtcHelper.setLocalVoiceReverbParam(param)
    }

    /**
     * 设置自定义均衡音效
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @see NERtcVoiceEqualizationBand
     * @param bandGain 每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
     * @return 0表示方法调用成功，其他失败。
     */
    private fun setLocalAudioEqualization(bandFrequency: Int, bandGain: Int): Int {
        return RtcHelper.setLocalVoiceEqualization(bandFrequency, bandGain)
    }

    /**
     * 是否插着耳返
     */
    fun isHeadsetOn(context: Context): Boolean {
        val am =
            context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return !isBluetoothEarphone() && isMAndUp() && am.isWiredHeadsetOn
    }

    @SuppressLint("MissingPermission")
    private fun isBluetoothEarphone(): Boolean {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return false
        }
        return BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(
            BluetoothProfile.HEADSET
        ) == BluetoothProfile.STATE_CONNECTED
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
    private fun isMAndUp(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun resetAll() {
        earBackVolume = DEFAULT_EAR_BACK_VOLUME
        audioMixingVolume = DEFAULT_AUDIO_MIXING_VOLUME
        recordingSignalVolume = DEFAULT_IN_KTV_RECORDING_SIGNAL_VOLUME
        otherSignalVolume = DEFAULT_IN_KTV_OTHER_SIGNAL_VOLUME
    }
}

object AudioEffectErrorCode {
    const val OK = 0
    const val Error = -1
}
