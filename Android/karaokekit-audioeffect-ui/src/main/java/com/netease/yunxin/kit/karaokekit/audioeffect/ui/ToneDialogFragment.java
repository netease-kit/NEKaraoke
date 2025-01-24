// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.audioeffect.ui;

import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.DEFAULT_RECORD_SIGNAL_VOLUME_MAX;
import static com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneContract.ToneUIState.DeFAULT_OTHER_SIGNAL_VOLUME_MAX;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;
import com.netease.yunxin.kit.karaokekit.audioeffect.ui.databinding.AudioEffectFragmentToneDialogBinding;
import com.netease.yunxin.kit.karaokekit.audioeffect.ui.databinding.AudioEffectItemToneReverberationTypeBinding;

public class ToneDialogFragment extends BottomSheetDialogFragment {
  private static final int PITCH_MAX = 12;
  private static final int PITCH_MIN = -12;
  private static final String TAG = "ToneDialogFragment";
  private AudioEffectFragmentToneDialogBinding binding;

  private ToneViewModel viewModel;

  private Context context;

  private int currentEffectId;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.context = context;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheet);
    if (getArguments() != null) {
      currentEffectId = getArguments().getInt(NEAudioEffectUIConstants.INTENT_CURRENT_EFFECT_ID);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = AudioEffectFragmentToneDialogBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.toneDialogFragmentActionReset.setOnClickListener(
        v -> {
          viewModel.reset();
        });
    binding.toneDialogFragmentEarBackSwitch.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!NEAudioEffectManager.INSTANCE.isHeadsetOn(context)) {
              compoundButton.setChecked(false);
              return;
            }
            viewModel.setEarBackEnable(isChecked);
          }
        });
    binding.toneDialogFragmentEarBackVolumeSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setEarBackVolume(progress));
    binding.toneDialogFragmentEffectVolumeSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setEffectVolume(progress));
    binding.toneDialogFragmentRecordingVolumeSeekbar.setMax(DEFAULT_RECORD_SIGNAL_VOLUME_MAX);
    binding.toneDialogFragmentRecordingVolumeSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setRecordingSignalVolume(progress));
    binding.toneDialogFragmentOtherRecordingVolumeSeekbar.setMax(DeFAULT_OTHER_SIGNAL_VOLUME_MAX);
    binding.toneDialogFragmentOtherRecordingVolumeSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.adjustPlaybackSignalVolume(progress));
    binding.toneDialogFragmentReverberationStrengthSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setReverberationStrength(progress));

    binding.toneDialogFragmentReverberationTypeRecyclerview.addItemDecoration(
        new SpacesItemDecoration(
            getResources().getDimensionPixelSize(R.dimen.audioeffect_dimen_8),
            SpacesItemDecoration.HORIZONTAL));

    binding.toneDialogFragmentEffectPitchUp.setOnClickListener(
        v -> {
          viewModel.setEffectPitch(viewModel.getEffectPitch() + 1);
          binding.toneDialogFragmentEffectCurrentPitch.setText(
              String.valueOf(viewModel.getEffectPitch()));
          refreshPitchButton();
        });

    binding.toneDialogFragmentEffectPitchDown.setOnClickListener(
        v -> {
          viewModel.setEffectPitch(viewModel.getEffectPitch() - 1);
          binding.toneDialogFragmentEffectCurrentPitch.setText(
              String.valueOf(viewModel.getEffectPitch()));
          refreshPitchButton();
        });
  }

  private void refreshPitchButton() {
    binding.toneDialogFragmentEffectPitchUp.setEnabled(viewModel.getEffectPitch() < PITCH_MAX);
    binding.toneDialogFragmentEffectPitchDown.setEnabled(viewModel.getEffectPitch() > PITCH_MIN);
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel = new ViewModelProvider(requireActivity()).get(ToneViewModel.class);
    viewModel.setCurrentEffectId(currentEffectId);
    ReverberationTypeAdapter adapter = new ReverberationTypeAdapter(viewModel);
    binding.toneDialogFragmentReverberationTypeRecyclerview.setAdapter(adapter);
    viewModel
        .getToneUIState()
        .observe(
            this,
            toneUIState -> {
              ALog.d(TAG, "toneUIState:" + toneUIState.toString());
              binding.toneDialogFragmentEarBackSwitch.setChecked(toneUIState.earBackEnabled);
              binding.toneDialogFragmentEarBackVolumeSeekbar.setProgress(toneUIState.earBackVolume);
              binding.toneDialogFragmentEffectVolumeSeekbar.setProgress(toneUIState.effectVolume);
              binding.toneDialogFragmentRecordingVolumeSeekbar.setProgress(
                  toneUIState.recordingSignalVolume);
              binding.toneDialogFragmentOtherRecordingVolumeSeekbar.setProgress(
                  toneUIState.otherSignalVolume);
              if (toneUIState.reverberationStrength > 0) {
                binding.toneDialogFragmentReverberationStrengthSeekbar.setProgress(
                    toneUIState.reverberationStrength);
              }
              binding.toneDialogFragmentEffectCurrentPitch.setText(
                  String.valueOf(toneUIState.effectPitch));
              binding.toneDialogFragmentReverberationStrengthSeekbar.setVisibility(
                  toneUIState.reverberationType == ToneContract.ReverberationType.OFF
                      ? View.INVISIBLE
                      : View.VISIBLE);
              binding.tvNoEffectStrength.setVisibility(
                  toneUIState.reverberationType == ToneContract.ReverberationType.OFF
                      ? View.VISIBLE
                      : View.INVISIBLE);
              binding.toneDialogFragmentReverberationStrength.setVisibility(
                  toneUIState.reverberationType == ToneContract.ReverberationType.OFF
                      ? View.INVISIBLE
                      : View.VISIBLE);
              adapter.notifyDataSetChanged();
            });

    if (NEAudioEffectManager.INSTANCE.isHeadsetOn(context)) {
      viewModel.setEarBackEnable(NEAudioEffectManager.INSTANCE.isEarBackEnable());
    }
    if (viewModel.isFirstShow) {
      viewModel.isFirstShow = false;
    }

    viewModel.setEffectPitch(viewModel.getEffectPitch());
  }
}

class ReverberationTypeViewHolder extends RecyclerView.ViewHolder {

  public final AudioEffectItemToneReverberationTypeBinding binding;

  public ReverberationTypeViewHolder(AudioEffectItemToneReverberationTypeBinding binding) {
    super(binding.getRoot());
    this.binding = binding;
  }
}

class ReverberationTypeAdapter extends RecyclerView.Adapter<ReverberationTypeViewHolder> {

  private final ToneContract.ViewModel viewModel;

  public ReverberationTypeAdapter(ToneContract.ViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @NonNull
  @Override
  public ReverberationTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    AudioEffectItemToneReverberationTypeBinding binding =
        AudioEffectItemToneReverberationTypeBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new ReverberationTypeViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ReverberationTypeViewHolder holder, int position) {
    ToneContract.ReverberationType type = ToneContract.ReverberationType.values()[position];
    holder.binding.ivBg.setImageDrawable(type.drawable);
    holder.binding.toneReverberationTypeSelected.setVisibility(
        type == viewModel.getToneUIState().getValue().reverberationType ? View.VISIBLE : View.GONE);
    holder.itemView.setOnClickListener(
        v -> {
          viewModel.setReverberationType(type);
        });
  }

  @Override
  public int getItemCount() {
    return ToneContract.ReverberationType.values().length;
  }
}

class SpacesItemDecoration extends RecyclerView.ItemDecoration {
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;

  private final int space;
  private final int orientation;

  public SpacesItemDecoration(int space, int orientation) {
    this.space = space;
    this.orientation = orientation;
  }

  @Override
  public void getItemOffsets(
      Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    if (orientation == VERTICAL) {
      outRect.set(0, 0, 0, space);
    } else {
      outRect.set(0, 0, space, 0);
    }
  }
}

interface OnSeekBarChangeListenerAdapter extends SeekBar.OnSeekBarChangeListener {

  @Override
  default void onStartTrackingTouch(SeekBar seekBar) {}

  @Override
  default void onStopTrackingTouch(SeekBar seekBar) {}
}
