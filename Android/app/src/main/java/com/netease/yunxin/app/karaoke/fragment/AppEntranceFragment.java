// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.app.karaoke.R;
import com.netease.yunxin.app.karaoke.databinding.FragmentAppEntranceBinding;
import com.netease.yunxin.app.karaoke.utils.KaraokeNavUtils;
import com.netease.yunxin.kit.entertainment.common.adapter.FunctionAdapter;
import com.netease.yunxin.kit.entertainment.common.fragment.BaseFragment;
import java.util.ArrayList;
import java.util.List;

public class AppEntranceFragment extends BaseFragment {

  private FragmentAppEntranceBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentAppEntranceBinding.inflate(inflater, container, false);
    View rootView = binding.getRoot();
    paddingStatusBarHeight(rootView);
    initView();
    initListener();
    return rootView;
  }

  private void initView() {
    binding.ivTopLogo.setImageResource(R.drawable.icon_app_top_logo);
    binding.rvFunctionList.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    List<FunctionAdapter.FunctionItem> list = new ArrayList<>();
    list.add(
        new FunctionAdapter.FunctionItem(
            getString(R.string.karaoke),
            getString(R.string.app_karaoke_room_desc),
            R.drawable.home_item_bg_karaoke_room,
            R.raw.home_ktv,
            () -> {
              KaraokeNavUtils.toKaraokeRoomListPage(getContext());
            }));
    binding.rvFunctionList.setAdapter(new FunctionAdapter(getContext(), list));
  }

  private void initListener() {}

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
