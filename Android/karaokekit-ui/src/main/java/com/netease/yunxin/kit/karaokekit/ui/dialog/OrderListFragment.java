// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.adapter.OrderAdapter;
import com.netease.yunxin.kit.karaokekit.ui.adapter.OrderLoadMoreDecorator;
import com.netease.yunxin.kit.karaokekit.ui.databinding.KaraokeOrderListLayoutBinding;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import java.util.List;

/** chat message read state page */
public class OrderListFragment extends BaseFragment {
  private static final String TAG = "OrderListFragment";
  private KaraokeOrderListLayoutBinding binding;

  private OrderAdapter adapter;
  private OrderSongViewModel orderSongViewModel;
  private int pageNum;
  private boolean isSearching = false;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    pageNum = 0;
    orderSongViewModel = new ViewModelProvider(requireActivity()).get(OrderSongViewModel.class);
    binding = KaraokeOrderListLayoutBinding.inflate(inflater, container, false);
    initView();
    initData();
    return binding.getRoot();
  }

  private void initView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    binding.etSearch.setOnFocusChangeListener(onFocusChangeListener);
    binding.etSearch.setOnEditorActionListener(
        (textView, i, keyEvent) -> {
          if (null != keyEvent
              && keyEvent.getAction() == KeyEvent.ACTION_DOWN
              && (KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()
                  || KeyEvent.KEYCODE_DPAD_CENTER == keyEvent.getKeyCode())) {
            onSearchSong();
          }
          return false;
        });

    binding.ivClear.setOnClickListener(
        view -> {
          isSearching = false;
          pageNum = 0;
          adapter.clear();
          initData();
          binding.etSearch.setText("");
        });
    binding.recyclerView.setLayoutManager(layoutManager);
    adapter = new OrderAdapter(getActivity(), orderSongViewModel);
    binding.recyclerView.setAdapter(adapter);

    OrderLoadMoreDecorator<KaraokeOrderSongModel> orderLoadMoreDecorator =
        new OrderLoadMoreDecorator<>(binding.recyclerView, layoutManager, adapter);
    orderLoadMoreDecorator.setLoadMoreListener(
        data -> {
          if (isSearching) {
            searchMoreSong();
          } else {
            loadMoreSong();
          }
        });
  }

  private void onSearchSong() {
    pageNum = 0;
    adapter.clear();

    if ((String.valueOf(binding.etSearch.getText())).length() > 0) {
      binding.ivClear.setVisibility(View.VISIBLE);
      isSearching = true;
      searchMoreSong();
    } else {
      binding.ivClear.setVisibility(View.GONE);
      loadMoreSong();
    }
  }

  private void initData() {
    loadMoreSong();
  }

  private void loadMoreSong() {
    orderSongViewModel.refreshSongList(
        pageNum,
        20,
        new NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>>() {
          @Override
          public void success(@Nullable List<KaraokeOrderSongModel> info) {
            adapter.append(info);
            pageNum++;
          }

          @Override
          public void error(int code, @Nullable String msg) {
            ALog.e("orderSong fail:" + code + " " + msg);
            ToastX.showShortToast(getString(R.string.get_song_list_failed));
          }
        });
  }

  private void searchMoreSong() {
    orderSongViewModel.searchSong(
        String.valueOf(binding.etSearch.getText()),
        pageNum,
        20,
        new NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>>() {
          @Override
          public void success(@Nullable List<KaraokeOrderSongModel> info) {
            if (info == null) {
              return;
            } else if (info.isEmpty()) {
              ToastX.showShortToast(R.string.did_not_find_right_result);
            }
            adapter.append(info);
            pageNum++;
          }

          @Override
          public void error(int code, @Nullable String msg) {
            ALog.e("searchSong fail:" + code + " " + msg);
          }
        });
  }

  private final View.OnFocusChangeListener onFocusChangeListener =
      new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
          if (b && TextUtils.isEmpty(String.valueOf(binding.etSearch.getText()))) {
            pageNum = 0;
            isSearching = true;
            adapter.clear();
          }
        }
      };
}
