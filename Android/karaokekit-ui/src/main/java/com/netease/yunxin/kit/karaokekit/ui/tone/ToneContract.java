// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.tone;

import android.graphics.drawable.Drawable;
import androidx.lifecycle.LiveData;
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ApplicationWrapper;
import com.netease.yunxin.kit.karaokekit.ui.R;

public interface ToneContract {
  enum ReverberationType {
    OFF(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_OFF,
        ApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.effect_default),
        true),
    KTV(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_KTV,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_ktv),
        true),
    CHURCH(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_CHURCH,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_church),
        true),
    MELLOW(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_MELLOW,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_mellow),
        false),
    REMOTE(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_REMOTE,
        ApplicationWrapper.getNewApplication()
            .getResources()
            .getDrawable(R.drawable.effect_distant),
        true),
    LIVE(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_LIVE,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_live),
        true),
    CLEAR(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_CLEAR,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_clear),
        false),
    MUFFLED(
        NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_MUFFLED,
        ApplicationWrapper.getNewApplication().getResources().getDrawable(R.drawable.effect_low),
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
    public static final int DEFAULT_RECORD_SIGNAL_VOLUME = 100;
    public static final int INVALID_EFFECT_STRENGTH = -1;

    ToneUIState() {
      this.reverberationType = ReverberationType.OFF;
      this.earBackEnabled = NEAudioEffectManager.INSTANCE.isEarBackEnable();
      this.earBackVolume = DEFAULT_VALUE;
      this.effectVolume = DEFAULT_VALUE;
      this.recordingSignalVolume = DEFAULT_RECORD_SIGNAL_VOLUME;
      this.reverberationStrength = INVALID_EFFECT_STRENGTH;
    }

    public ToneUIState(
        boolean earBackEnabled,
        int earBackVolume,
        int effectVolume,
        int recordingSignalVolume,
        ReverberationType reverberationType,
        int reverberationStrength) {
      this.earBackEnabled = earBackEnabled;
      this.earBackVolume = earBackVolume;
      this.effectVolume = effectVolume;
      this.recordingSignalVolume = recordingSignalVolume;
      this.reverberationType = reverberationType;
      this.reverberationStrength = reverberationStrength;
    }

    public boolean earBackEnabled;

    public int earBackVolume;

    public int effectVolume;

    public int recordingSignalVolume;

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

    /**
     * 设置人声音量
     *
     * @param volume 音量
     */
    void setRecordingSignalVolume(int volume);

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
