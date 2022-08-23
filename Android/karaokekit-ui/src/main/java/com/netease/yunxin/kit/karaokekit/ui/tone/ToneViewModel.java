// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.tone;

import static com.netease.yunxin.kit.karaokekit.ui.tone.ToneContract.ToneUIState.DEFAULT_VALUE;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.lava.nertc.sdk.audio.NERtcVoiceBeautifierType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.AudioEffectErrorCode;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;

public class ToneViewModel extends AndroidViewModel implements ToneContract.ViewModel {

  private static final String TAG = "ToneViewModel";
  private final MutableLiveData<ToneContract.ToneUIState> toneUIState =
      new MutableLiveData<>(new ToneContract.ToneUIState());
  private ToneContract.ReverberationType lastType = null;
  private NEAudioEffectManager audioEffectManager = NEAudioEffectManager.INSTANCE;
  public boolean isFirstShow = true;

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
    toneUIState.setValue(new ToneContract.ToneUIState());
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
      toneUIState.setValue(
          new ToneContract.ToneUIState(
              enable,
              state.earBackVolume,
              state.effectVolume,
              state.recordingSignalVolume,
              state.reverberationType,
              state.reverberationStrength));
    }
  }

  @Override
  public void setEarBackVolume(int volume) {
    ALog.d(TAG, "setEarBackVolume,volume:" + volume);
    audioEffectManager.setEarBackVolume(volume);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            volume,
            state.effectVolume,
            state.recordingSignalVolume,
            state.reverberationType,
            state.reverberationStrength));
  }

  @Override
  public void setEffectVolume(int volume) {
    ALog.d(TAG, "setEffectVolume,volume:" + volume);
    audioEffectManager.setAudioMixingVolume(
        NEKaraokeKit.getInstance().currentSongIdForAudioEffect(), volume);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            volume,
            state.recordingSignalVolume,
            state.reverberationType,
            state.reverberationStrength));
  }

  @Override
  public void setRecordingSignalVolume(int volume) {
    ALog.d(TAG, "setRecordingSignalVolume,volume:" + volume);
    audioEffectManager.adjustRecordingSignalVolume(volume * 4);
    final ToneContract.ToneUIState state = toneUIState.getValue();
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            volume,
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
        state.reverberationStrength = ToneContract.ToneUIState.INVALID_EFFECT_STRENGTH;
      }
    }
    toneUIState.setValue(
        new ToneContract.ToneUIState(
            state.earBackEnabled,
            state.earBackVolume,
            state.effectVolume,
            state.recordingSignalVolume,
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
            state.reverberationType,
            strength));
    audioEffectManager.setEqualizeIntensity(strength);
  }
}
