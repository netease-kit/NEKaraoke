// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.karaoke.databinding.FragmentUserCenterBinding;
import com.netease.yunxin.app.karaoke.utils.AppUtils;
import com.netease.yunxin.app.karaoke.utils.KaraokeNavUtils;
import com.netease.yunxin.kit.entertainment.common.fragment.BaseFragment;

public class UserCenterFragment extends BaseFragment {
  private static final String TAG = "UserCenterFragment";
  private FragmentUserCenterBinding binding;
  private Dialog loadingDialog;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentUserCenterBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();
    initViews();
    initDataCenter();
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    initUser();
  }

  private void initViews() {
    binding.commonSetting.setOnClickListener(
        v -> KaraokeNavUtils.toCommonSettingPage(requireActivity()));
  }

  private void initUser() {
    binding.ivUserPortrait.loadAvatar(AppUtils.getAvatar());
    binding.tvUserName.setText(AppUtils.getUserName());
  }

  private void initDataCenter() {}

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
