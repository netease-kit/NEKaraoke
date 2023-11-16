// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.karaokekit.ui.adapter.OrderedAdapter;
import com.netease.yunxin.kit.karaokekit.ui.databinding.KaraokeOrderedListLayoutBinding;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;

/** chat message read state page */
public class OrderedListFragment extends BaseFragment {
  private KaraokeOrderedListLayoutBinding binding;
  private OrderedAdapter adapter;
  private OrderSongViewModel orderSongViewModel;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    orderSongViewModel = new ViewModelProvider(requireActivity()).get(OrderSongViewModel.class);
    binding = KaraokeOrderedListLayoutBinding.inflate(inflater, container, false);
    initView();
    initData();
    return binding.getRoot();
  }

  private void initView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    binding.recyclerView.setLayoutManager(layoutManager);
    adapter = new OrderedAdapter(orderSongViewModel);
    binding.recyclerView.setAdapter(adapter);
    orderSongViewModel
        .getOrderSongListChangeEvent()
        .observe(
            getViewLifecycleOwner(),
            orderSongs -> {
              if (orderSongs != null && !orderSongs.isEmpty()) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.llyEmpty.setVisibility(View.GONE);
                adapter.refresh(orderSongs);
              } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.llyEmpty.setVisibility(View.VISIBLE);
              }
            });
  }

  private void initData() {
    orderSongViewModel.refreshOrderSongs();
  }
}
