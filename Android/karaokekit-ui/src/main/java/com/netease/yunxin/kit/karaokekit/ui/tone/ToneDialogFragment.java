// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.tone;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.databinding.FragmentToneDialogBinding;
import com.netease.yunxin.kit.karaokekit.ui.databinding.ItemToneReverberationTypeBinding;

public class ToneDialogFragment extends BottomSheetDialogFragment {

  private static final String TAG = "ToneDialogFragment";
  private FragmentToneDialogBinding binding;

  private ToneViewModel viewModel;

  private Context context;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.context = context;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.AppBottomSheet);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentToneDialogBinding.inflate(inflater, container, false);
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
    binding.toneDialogFragmentRecordingVolumeSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setRecordingSignalVolume(progress));
    binding.toneDialogFragmentReverberationStrengthSeekbar.setOnSeekBarChangeListener(
        (OnSeekBarChangeListenerAdapter)
            (seekBar, progress, fromUser) -> viewModel.setReverberationStrength(progress));

    binding.toneDialogFragmentReverberationTypeRecyclerview.addItemDecoration(
        new SpacesItemDecoration(
            getResources().getDimensionPixelSize(R.dimen.dimen_8),
            SpacesItemDecoration.HORIZONTAL));
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel = new ViewModelProvider(requireActivity()).get(ToneViewModel.class);
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
              if (toneUIState.reverberationStrength > 0) {
                binding.tvNoEffectStrength.setVisibility(View.INVISIBLE);
                binding.toneDialogFragmentReverberationStrength.setVisibility(View.VISIBLE);
                binding.toneDialogFragmentReverberationStrengthSeekbar.setVisibility(View.VISIBLE);
                binding.toneDialogFragmentReverberationStrengthSeekbar.setProgress(
                    toneUIState.reverberationStrength);
              } else {
                binding.tvNoEffectStrength.setVisibility(View.VISIBLE);
                binding.toneDialogFragmentReverberationStrength.setVisibility(View.INVISIBLE);
                binding.toneDialogFragmentReverberationStrengthSeekbar.setVisibility(
                    View.INVISIBLE);
              }
              adapter.notifyDataSetChanged();
            });

    if (NEAudioEffectManager.INSTANCE.isHeadsetOn(context)) {
      if (viewModel.isFirstShow) {
        // 第一次进来，插着耳机状态下默认打开耳返功能
        viewModel.setEarBackEnable(true);
      } else {
        viewModel.setEarBackEnable(NEAudioEffectManager.INSTANCE.isEarBackEnable());
      }
    }
    if (viewModel.isFirstShow) {
      viewModel.isFirstShow = false;
    }
  }
}

class ReverberationTypeViewHolder extends RecyclerView.ViewHolder {

  public final ItemToneReverberationTypeBinding binding;

  public ReverberationTypeViewHolder(ItemToneReverberationTypeBinding binding) {
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
    ItemToneReverberationTypeBinding binding =
        ItemToneReverberationTypeBinding.inflate(
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
