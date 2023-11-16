// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.audioeffect.ui;

import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.DEFAULT_EFFECT_VALUE;
import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.DEFAULT_RECORD_SIGNAL_VOLUME;
import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.DEFAULT_VALUE;
import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.INVALID_EFFECT_STRENGTH;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.AudioEffectErrorCode;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;

public class ToneViewModel extends AndroidViewModel implements ToneContract.ViewModel {

  private static final String TAG = "ToneViewModel";
  private final MutableLiveData<ToneContract.ToneUIState> toneUIState =
      new MutableLiveData<>(new ToneContract.ToneUIState());
  private ToneContract.ReverberationType lastType = null;
  private final NEAudioEffectManager audioEffectManager = NEAudioEffectManager.INSTANCE;
  public boolean isFirstShow = true;
  private int currentEffectId;

  public ToneViewModel(@NonNull Application application) {
    super(application);
  }

  @Override
  public LiveData<ToneContract.ToneUIState> getToneUIState() {
    return toneUIState;
  }

  @Override
  public void reset() {
    ALog.d(TAG, "reset");
    //数据复位
    setEffectPitch(0);
    audioEffectManager.setReverbPreset(ToneContract.ReverberationType.values()[0].preset);
    //UI复位
    ToneContract.ToneUIState toneUIState = new ToneContract.ToneUIState();
    toneUIState.reverberationType = ToneContract.ReverberationType.OFF;
    toneUIState.earBackEnabled = NEAudioEffectManager.INSTANCE.isEarBackEnable();
    toneUIState.earBackVolume = DEFAULT_VALUE;
    toneUIState.effectVolume = DEFAULT_EFFECT_VALUE;
    toneUIState.effectPitch = 0;
    toneUIState.recordingSignalVolume = DEFAULT_RECORD_SIGNAL_VOLUME;
    toneUIState.reverberationStrength = INVALID_EFFECT_STRENGTH;
    this.toneUIState.setValue(toneUIState);
  }

  @Override
  public void setEarBackEnable(boolean enable) {
    if (!audioEffectManager.isHeadsetOn(getApplication())) {
      ALog.e(TAG, "Headset is not exist");
      return;
    }
    ALog.d(TAG, "setEarBackEnable,enable:" + enable);
    if (audioEffectManager.enableEarBack(enable) == AudioEffectErrorCode.OK) {
      final ToneContract.ToneUIState state = toneUIState.getValue();
      if (state != null) {
        toneUIState.setValue(
            new ToneContract.ToneUIState(
                enable,
                state.earBackVolume,
                state.effectVolume,
                state.recordingSignalVolume,
                state.effectPitch,
                state.reverberationType,
                state.reverberationStrength));
      }
    }
  }

  @Override
  public void setEarBackVolume(int volume) {
    ALog.d(TAG, "setEarBackVolume,volume:" + volume);
    audioEffectManager.setEarBackVolume(volume);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    if (state != null) {
      toneUIState.setValue(
          new ToneContract.ToneUIState(
              state.earBackEnabled,
              volume,
              state.effectVolume,
              state.recordingSignalVolume,
              state.effectPitch,
              state.reverberationType,
              state.reverberationStrength));
    }
  }

  @Override
  public void setEffectVolume(int volume) {
    ALog.d(TAG, "setEffectVolume,volume:" + volume);
    audioEffectManager.setAudioMixingVolume(currentEffectId, volume);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            volume,
            state.recordingSignalVolume,
            state.effectPitch,
            state.reverberationType,
            state.reverberationStrength));
  }

  @Override
  public void setEffectPitch(int pitch) {
    ALog.d(TAG, "setEffectPitch pitch=" + pitch);
    audioEffectManager.setEffectPitch(currentEffectId, pitch);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            state.recordingSignalVolume,
            pitch,
            state.reverberationType,
            state.reverberationStrength));
  }

  @Override
  public int getEffectPitch() {
    ALog.d(TAG, "getEffectPitch");
    return audioEffectManager.getEffectPitch(currentEffectId);
  }

  @Override
  public void setAudioMixingPitch(int pitch) {
    ALog.d(TAG, "setAudioMixingPitch pitch=" + pitch);
    audioEffectManager.setAudioMixingPitch(pitch);
  }

  @Override
  public int getAudioMixingPitch() {
    ALog.d(TAG, "getAudioMixingPitch");
    return audioEffectManager.getAudioMixingPitch();
  }

  @Override
  public void setRecordingSignalVolume(int volume) {
    ALog.d(TAG, "setRecordingSignalVolume,volume:" + volume);
    audioEffectManager.adjustRecordingSignalVolume(volume);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            volume,
            state.effectPitch,
            state.reverberationType,
            state.reverberationStrength));
  }

  @Override
  public void setReverberationType(ToneContract.ReverberationType type) {
    if (type.isReverberation) {
      audioEffectManager.setReverbPreset(type.preset);
    } else {
      audioEffectManager.setEqualizePreset(type.preset);
    }
    ALog.d(TAG, "setReverberationType,preset:" + type.preset);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    if (lastType != state.reverberationType) {
      state.reverberationStrength = DEFAULT_VALUE;
      if (type.preset == NERtcVoiceBeautifierType.VOICE_BEAUTIFIER_OFF) {
        state.reverberationStrength = INVALID_EFFECT_STRENGTH;
      }
    }
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            state.recordingSignalVolume,
            state.effectPitch,
            type,
            state.reverberationStrength));
    lastType = state.reverberationType;
  }

  @Override
  public void setReverberationStrength(int strength) {
    ALog.d(TAG, "setReverberationStrength strength:" + strength);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            state.recordingSignalVolume,
            state.effectPitch,
            state.reverberationType,
            strength));
    audioEffectManager.setEqualizeIntensity(strength);
  }

  public void setCurrentEffectId(int currentEffectId) {
    this.currentEffectId = currentEffectId;
  }
}
