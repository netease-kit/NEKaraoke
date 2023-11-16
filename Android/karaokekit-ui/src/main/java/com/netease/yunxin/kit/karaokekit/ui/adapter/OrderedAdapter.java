// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.adapter;

import static com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongStatus.STATUS_SINGING;
import static com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongStatus.STATUS_WAIT;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.DateFormatUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongResult;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.databinding.KaraokeOrderedItemLayoutBinding;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUIUtils;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import java.util.Locale;
import java.util.Objects;

/** team message read state adapter */
public class OrderedAdapter
    extends CommonMoreAdapter<NEKaraokeOrderSongResult, KaraokeOrderedItemLayoutBinding> {
  private final OrderSongViewModel orderSongViewModel;

  public OrderedAdapter(OrderSongViewModel orderSongViewModel) {
    this.orderSongViewModel = orderSongViewModel;
  }

  @NonNull
  @Override
  public BaseMoreViewHolder<NEKaraokeOrderSongResult, KaraokeOrderedItemLayoutBinding>
      getViewHolder(@NonNull ViewGroup parent, int viewType) {
    KaraokeOrderedItemLayoutBinding binding =
        KaraokeOrderedItemLayoutBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new OrderedItemViewHolder(binding);
  }

  public class OrderedItemViewHolder
      extends BaseMoreViewHolder<NEKaraokeOrderSongResult, KaraokeOrderedItemLayoutBinding> {

    public OrderedItemViewHolder(@NonNull KaraokeOrderedItemLayoutBinding binding) {
      super(binding);
    }

    @Override
    public void bind(NEKaraokeOrderSongResult item) {
      getBinding().songCover.setCornerRadius(SizeUtils.dp2px(5));
      if (getBindingAdapterPosition() == 0) {
        getBinding().musicIcon.setVisibility(View.VISIBLE);
        getBinding().songOrder.setVisibility(View.GONE);
      } else {
        getBinding().musicIcon.setVisibility(View.GONE);
        getBinding().songOrder.setVisibility(View.VISIBLE);
      }
      if (item.getOrderSong().getStatus() == STATUS_SINGING || getBindingAdapterPosition() == 0) {
        getBinding().songSinging.setVisibility(View.VISIBLE);
        getBinding().orderCancel.setVisibility(View.GONE);
      } else if (item.getOrderSong().getStatus() == STATUS_WAIT) {
        getBinding().songSinging.setVisibility(View.GONE);
        if (KaraokeUIUtils.isLocalHost()
            || Objects.equals(
                KaraokeUIUtils.getLocalAccount(), item.getOrderSong().getUserUuid())) {
          getBinding().orderCancel.setVisibility(View.VISIBLE);
          getBinding()
              .orderCancel
              .setOnClickListener(v -> deleteSong(v, item.getOrderSong().getOrderId()));
        } else {
          getBinding().orderCancel.setVisibility(View.GONE);
        }
      } else {
        getBinding().songSinging.setVisibility(View.GONE);
        getBinding().orderCancel.setVisibility(View.GONE);
      }
      if (getBindingAdapterPosition() > 1 && KaraokeUIUtils.isLocalHost()) {
        getBinding().songTop.setVisibility(View.VISIBLE);
        getBinding().songTop.setOnClickListener(v -> topSong(v, item.getOrderSong().getOrderId()));
      } else {
        getBinding().songTop.setVisibility(View.GONE);
      }

      getBinding()
          .songOrder
          .setText(String.format(Locale.CHINA, "%02d", getBindingAdapterPosition()));
      if (TextUtils.isEmpty(item.getOrderSong().getSongCover())) {
        getBinding().songCover.setData(R.drawable.icon_song_cover, "");
      } else {
        getBinding().songCover.setData(item.getOrderSong().getSongCover(), "");
      }

      getBinding().songName.setText(item.getOrderSong().getSongName());
      if (TextUtils.isEmpty(item.getOrderSongUser().getIcon())) {
        getBinding().userAvatar.setData(R.drawable.default_avatar, "");
      } else {
        getBinding().userAvatar.setData(item.getOrderSongUser().getIcon(), "");
      }

      getBinding().songName.setText(item.getOrderSong().getSongName());
      getBinding().userName.setText(item.getOrderSongUser().getUserName());
      getBinding().songSize.setText(DateFormatUtils.long2StrHS(item.getOrderSong().getSongTime()));
    }
  }

  public void deleteSong(View view, long orderId) {
    orderSongViewModel.deleteSong(
        orderId,
        new NECopyrightedMedia.Callback<Boolean>() {
          @Override
          public void success(@Nullable Boolean info) {}

          @Override
          public void error(int code, @Nullable String msg) {
            if (code == NEKaraokeUIConstants.ERROR_NETWORK) {
              ToastUtils.INSTANCE.showShortToast(
                  view.getContext(), view.getContext().getString(R.string.karaoke_network_error));
            }
          }
        });
  }

  public void topSong(View view, long orderId) {
    orderSongViewModel.topSong(
        orderId,
        new NECopyrightedMedia.Callback<Boolean>() {
          @Override
          public void success(@Nullable Boolean info) {}

          @Override
          public void error(int code, @Nullable String msg) {
            if (code == NEKaraokeUIConstants.ERROR_NETWORK) {
              ToastUtils.INSTANCE.showShortToast(
                  view.getContext(), view.getContext().getString(R.string.karaoke_network_error));
            }
          }
        });
  }
}
