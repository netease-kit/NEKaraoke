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
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.databinding.OrderedItemLayoutBinding;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUtils;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import java.util.Locale;
import java.util.Objects;

/** team message read state adapter */
public class OrderedAdapter
    extends CommonMoreAdapter<NEKaraokeOrderSongModel, OrderedItemLayoutBinding> {
  private OrderSongViewModel orderSongViewModel;

  public OrderedAdapter(OrderSongViewModel orderSongViewModel) {
    this.orderSongViewModel = orderSongViewModel;
  }

  @NonNull
  @Override
  public BaseMoreViewHolder<NEKaraokeOrderSongModel, OrderedItemLayoutBinding> getViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    OrderedItemLayoutBinding binding =
        OrderedItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new OrderedItemViewHolder(binding);
  }

  public class OrderedItemViewHolder
      extends BaseMoreViewHolder<NEKaraokeOrderSongModel, OrderedItemLayoutBinding> {

    public OrderedItemViewHolder(@NonNull OrderedItemLayoutBinding binding) {
      super(binding);
    }

    @Override
    public void bind(NEKaraokeOrderSongModel item) {
      getBinding().songCover.setCornerRadius(SizeUtils.dp2px(5));
      if (getBindingAdapterPosition() == 0) {
        getBinding().musicIcon.setVisibility(View.VISIBLE);
        getBinding().songOrder.setVisibility(View.GONE);
      } else {
        getBinding().musicIcon.setVisibility(View.GONE);
        getBinding().songOrder.setVisibility(View.VISIBLE);
      }
      if (item.getStatus() == STATUS_SINGING || getBindingAdapterPosition() == 0) {
        getBinding().songSinging.setVisibility(View.VISIBLE);
        getBinding().orderCancel.setVisibility(View.GONE);
      } else if (item.getStatus() == STATUS_WAIT) {
        getBinding().songSinging.setVisibility(View.GONE);
        if (KaraokeUtils.isCurrentHost()
            || Objects.equals(KaraokeUtils.getCurrentAccount(), item.getUserUuid())) {
          getBinding().orderCancel.setVisibility(View.VISIBLE);
          getBinding().orderCancel.setOnClickListener(v -> deleteSong(v, item.getOrderId()));
        } else {
          getBinding().orderCancel.setVisibility(View.GONE);
        }
      } else {
        getBinding().songSinging.setVisibility(View.GONE);
        getBinding().orderCancel.setVisibility(View.GONE);
      }
      if (getBindingAdapterPosition() > 1 && KaraokeUtils.isCurrentHost()) {
        getBinding().songTop.setVisibility(View.VISIBLE);
        getBinding().songTop.setOnClickListener(v -> topSong(v, item.getOrderId()));
      } else {
        getBinding().songTop.setVisibility(View.GONE);
      }

      getBinding()
          .songOrder
          .setText(String.format(Locale.CHINA, "%02d", getBindingAdapterPosition()));
      if (TextUtils.isEmpty(item.getSongCover())) {
        getBinding().songCover.setData(R.drawable.icon_song_cover, "");
      } else {
        getBinding().songCover.setData(item.getSongCover(), "");
      }

      getBinding().songName.setText(item.getSongName());
      if (TextUtils.isEmpty(item.getIcon())) {
        getBinding().userAvatar.setData(R.drawable.default_avatar, "");
      } else {
        getBinding().userAvatar.setData(item.getIcon(), "");
      }

      getBinding().songName.setText(item.getSongName());
      getBinding().userName.setText(item.getUserName());
      getBinding().songSize.setText(DateFormatUtils.long2StrHS(item.getSongTime()));
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
