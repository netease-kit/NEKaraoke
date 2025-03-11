// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.audioeffect.ui;

import android.graphics.drawable.Drawable;
import androidx.lifecycle.LiveData;
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;

public interface ToneContract {
  enum ReverberationType {
    OFF(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_OFF,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_default),
        true),
    KTV(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_KTV,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_ktv),
        true),
    CHURCH(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_CHURCH,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_church),
        true),
    MELLOW(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_MELLOW,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_mellow),
        false),
    REMOTE(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_REMOTE,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_distant),
        true),
    LIVE(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_LIVE,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_live),
        true),
    CLEAR(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_CLEAR,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_clear),
        false),
    MUFFLED(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_MUFFLED,
        AudioEffectApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.audio_effect_low),
        false);

    public int preset;
    public Drawable drawable;
    public boolean isReverberation;

    ReverberationType(int preset, Drawable drawable, boolean isReverberation) {
      this.preset = preset;
      this.drawable = drawable;
      this.isReverberation = isReverberation;
    }
  }

  class ToneUIState {
    public static final int DEFAULT_VALUE = 50;
    public static final int DEFAULT_EFFECT_VALUE = 15;
    public static final int INVALID_EFFECT_STRENGTH = -1;
    public static final int DEFAULT_RECORD_SIGNAL_VOLUME_MAX = 100;
    public static final int DeFAULT_OTHER_SIGNAL_VOLUME_MAX = 100;
    public static final int DEFAULT_RECORD_SIGNAL_VOLUME = 80;
    public static final int DEFAULT_IN_KTV_OTHER_SIGNAL_VOLUME = 10;
    public static final int DEFAULT_OTHER_SIGNAL_VOLUME = 100;

    ToneUIState() {
      this.reverberationType = ReverberationType.OFF;
      this.earBackEnabled = NEAudioEffectManager.INSTANCE.isEarBackEnable();
      this.earBackVolume = NEAudioEffectManager.INSTANCE.getEarBackVolume();
      this.effectVolume = NEAudioEffectManager.INSTANCE.getAudioMixingVolume();
      this.recordingSignalVolume = NEAudioEffectManager.INSTANCE.getRecordingSignalVolume();
      this.otherSignalVolume = NEAudioEffectManager.INSTANCE.getOtherSignalVolume();
      this.reverberationStrength = INVALID_EFFECT_STRENGTH;
    }

    public ToneUIState(
        boolean earBackEnabled,
        int earBackVolume,
        int effectVolume,
        int recordingSignalVolume,
        int otherSignalVolume,
        int effectPitch,
        ReverberationType reverberationType,
        int reverberationStrength) {
      this.earBackEnabled = earBackEnabled;
      this.earBackVolume = earBackVolume;
      this.effectVolume = effectVolume;
      this.recordingSignalVolume = recordingSignalVolume;
      this.otherSignalVolume = otherSignalVolume;
      this.effectPitch = effectPitch;
      this.reverberationType = reverberationType;
      this.reverberationStrength = reverberationStrength;
    }

    public boolean earBackEnabled;

    public int earBackVolume;

    public int effectVolume;

    public int effectPitch;

    public int recordingSignalVolume;
    public int otherSignalVolume;

    public ReverberationType reverberationType;

    public int reverberationStrength = DEFAULT_VALUE;

    @Override
    public String toString() {
      return "ToneUIState{"
          + "earBackEnabled="
          + earBackEnabled
          + ", earBackVolume="
          + earBackVolume
          + ", effectVolume="
          + effectVolume
          + ", recordingSignalVolume="
          + recordingSignalVolume
          + ", reverberationType="
          + reverberationType
          + ", reverberationStrength="
          + reverberationStrength
          + '}';
    }
  }

  interface ViewModel {

    LiveData<ToneUIState> getToneUIState();

    /** 重置 */
    void reset();

    /** 开关耳返 */
    void setEarBackEnable(boolean enable);

    /**
     * 设置耳返音量
     *
     * @param volume 音量
     */
    void setEarBackVolume(int volume);

    /**
     * 设置伴奏音量
     *
     * @param volume 音量
     */
    void setEffectVolume(int volume);

    void setEffectPitch(int pitch);

    int getEffectPitch();

    void setAudioMixingPitch(int pitch);

    int getAudioMixingPitch();

    /**
     * 设置人声音量
     *
     * @param volume 音量
     */
    void setRecordingSignalVolume(int volume);

    /**
     * 设置其他人音量
     *
     * @param volume
     */
    void adjustPlaybackSignalVolume(int volume);

    /**
     * 设置混响/均匀类型
     *
     * @param type 类型
     */
    void setReverberationType(ReverberationType type);

    /**
     * 设置混响/均匀强度
     *
     * @param strength 强度
     */
    void setReverberationStrength(int strength);
  }
}
