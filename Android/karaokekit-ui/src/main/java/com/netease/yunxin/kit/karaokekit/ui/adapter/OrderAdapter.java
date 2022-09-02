// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.adapter;

import static com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedMediaChannel.CLOUD_MUSIC;
import static com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedMediaChannel.MI_GU;
import static com.netease.yunxin.kit.karaokekit.api.NEKaraokeErrorCode.ERR_ORDER_SONG_COUNT_EXCEED_LIMIT;
import static com.netease.yunxin.kit.karaokekit.api.NEKaraokeErrorCode.ERR_ROOM_ORDER_SONG_COUNT_EXCEED_LIMIT;
import static com.netease.yunxin.kit.karaokekit.api.NEKaraokeErrorCode.ERR_SONG_ALREADY_ORDERED;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.blankj.utilcode.util.CollectionUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.NEErrorCode;
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.databinding.OrderItemLayoutBinding;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.MediaUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.NetUtils;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import java.util.List;

/** team message read state adapter */
public class OrderAdapter extends CommonMoreAdapter<KaraokeOrderSongModel, OrderItemLayoutBinding> {
  private static final String TAG = "OrderAdapter";
  private OrderSongViewModel orderSongViewModel;

  public OrderAdapter(FragmentActivity activity, OrderSongViewModel orderSongViewModel) {
    this.orderSongViewModel = orderSongViewModel;
    orderSongViewModel.getPerformDownloadSongEvent().removeObservers(activity);
    orderSongViewModel
        .getPerformDownloadSongEvent()
        .observe(
            activity,
            orderSong -> {
              if (orderSong != null) {
                preloadSong(activity, orderSong);
                orderSongViewModel.getPerformDownloadSongEvent().postValue(null);
              }
            });
    orderSongViewModel.getStartOrderSongEvent().removeObservers(activity);
    orderSongViewModel
        .getStartOrderSongEvent()
        .observe(
            activity,
            orderSong -> {
              if (orderSong != null) {
                onOrderSong(activity, orderSong);
                orderSongViewModel.getStartOrderSongEvent().postValue(null);
              }
            });
  }

  @NonNull
  @Override
  public BaseMoreViewHolder<KaraokeOrderSongModel, OrderItemLayoutBinding> getViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    OrderItemLayoutBinding binding =
        OrderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new OrderedItemViewHolder(binding);
  }

  public class OrderedItemViewHolder
      extends BaseMoreViewHolder<KaraokeOrderSongModel, OrderItemLayoutBinding> {

    public OrderedItemViewHolder(@NonNull OrderItemLayoutBinding binding) {
      super(binding);
    }

    @Override
    public void bind(KaraokeOrderSongModel item) {
      getBinding().songCover.setCornerRadius(SizeUtils.dp2px(5));
      if (TextUtils.isEmpty(item.getSongCover())) {
        getBinding().songCover.setData(R.drawable.icon_song_cover, "");
      } else {
        getBinding().songCover.setData(item.getSongCover(), "");
      }
      getBinding().songName.setText(item.getSongName());
      if (CollectionUtils.isNotEmpty(item.getSingers())) {
        getBinding().userName.setText(item.getSingers().get(0).getSingerName());
      }
      int channelIconRes = getChannelIconRes(item.getChannel());
      getBinding().channelIcon.setImageResource(channelIconRes);
      if (item.getStatus() == KaraokeOrderSongModel.STATE_DOWNLOADING) {
        getBinding().orderSong.setVisibility(View.GONE);
        getBinding().progressNum.setVisibility(View.VISIBLE);
        getBinding().progressBar.setVisibility(View.VISIBLE);
        getBinding().progressBar.setProgress(item.getDownloadProgress());
      } else {
        getBinding().orderSong.setVisibility(View.VISIBLE);
        getBinding().progressNum.setVisibility(View.GONE);
        getBinding().progressBar.setVisibility(View.GONE);
        getBinding()
            .orderSong
            .setOnClickListener(
                view -> {
                  if (!NetUtils.checkNetwork(view.getContext())) {
                    return;
                  }

                  ALog.i(TAG, "orderSong:" + item);
                  orderSongViewModel.getPerformOrderSongEvent().postValue(item);
                });
      }
    }

    @Override
    public void bind(KaraokeOrderSongModel item, @NonNull List<?> payloads) {
      if (item.getStatus() == KaraokeOrderSongModel.STATE_DOWNLOADING) {
        getBinding().orderSong.setVisibility(View.GONE);
        getBinding().progressNum.setVisibility(View.VISIBLE);
        getBinding().progressBar.setVisibility(View.VISIBLE);
        getBinding().progressBar.setProgress(item.getDownloadProgress());
      } else {
        getBinding().orderSong.setVisibility(View.VISIBLE);
        getBinding().progressNum.setVisibility(View.GONE);
        getBinding().progressBar.setVisibility(View.GONE);
        getBinding()
            .orderSong
            .setOnClickListener(
                view -> {
                  ALog.i(TAG, "orderSong:" + item);
                  orderSongViewModel.getPerformOrderSongEvent().postValue(item);
                });
      }
    }
  }

  private static int getChannelIconRes(int channel) {
    int channelIconRes = R.drawable.icon_cloud_music;
    switch (channel) {
      case CLOUD_MUSIC:
        channelIconRes = R.drawable.icon_cloud_music;
        break;
      case MI_GU:
        channelIconRes = R.drawable.icon_migu;
        break;
      default:
        break;
    }
    return channelIconRes;
  }

  public void preloadSong(Context context, KaraokeOrderSongModel copyrightSong) {
    orderSongViewModel.preloadSong(
        copyrightSong.getSongId(),
        new NESongPreloadCallback() {

          @Override
          public void onPreloadStart(String songId) {
            copyrightSong.setStatus(KaraokeOrderSongModel.STATE_DOWNLOADING);
            refreshDataAndNotify(copyrightSong, true);
          }

          @Override
          public void onPreloadProgress(String songId, float progress) {
            copyrightSong.setDownloadProgress((int) (progress * 100));
            refreshDataAndNotify(copyrightSong, true);
          }

          @Override
          public void onPreloadComplete(String songId, int errorCode, String msg) {
            copyrightSong.setStatus(KaraokeOrderSongModel.STATE_DOWNLOADED);
            copyrightSong.setDownloadProgress(100);
            String filePath =
                NECopyrightedMedia.getInstance().getSongURI(songId, SongResType.TYPE_ACCOMP);
            if (TextUtils.isEmpty(filePath)) {
              filePath =
                  NECopyrightedMedia.getInstance().getSongURI(songId, SongResType.TYPE_ORIGIN);
            }
            if (filePath != null) {
              copyrightSong.setSongTime(MediaUtils.getDuration(filePath));
            }
            if (errorCode == NEErrorCode.OK) {
              orderSong(copyrightSong);
            } else {
              ToastUtils.INSTANCE.showShortToast(
                  context, context.getString(R.string.preloading_failure, errorCode, msg));
            }
            refreshDataAndNotify(copyrightSong, true);
          }
        });
  }

  public void orderSong(KaraokeOrderSongModel copyrightSong) {
    orderSongViewModel.getStartOrderSongEvent().postValue(copyrightSong);
  }

  public void onOrderSong(Context context, KaraokeOrderSongModel copyrightSong) {
    orderSongViewModel.orderSong(
        copyrightSong,
        new NECopyrightedMedia.Callback<Boolean>() {
          @Override
          public void success(@Nullable Boolean info) {
            ToastUtils.INSTANCE.showShortToast(
                context, context.getString(R.string.order_song_success));
          }

          @Override
          public void error(int code, @Nullable String msg) {
            if (code == ERR_SONG_ALREADY_ORDERED) {
              ToastUtils.INSTANCE.showShortToast(
                  context, context.getString(R.string.song_already_ordered));
            } else if (code == ERR_ORDER_SONG_COUNT_EXCEED_LIMIT) {
              ToastUtils.INSTANCE.showShortToast(
                  context, context.getString(R.string.err_order_song_count_exceed_limit));
            } else if (code == ERR_ROOM_ORDER_SONG_COUNT_EXCEED_LIMIT) {
              ToastUtils.INSTANCE.showShortToast(
                  context, context.getString(R.string.err_room_order_song_count_exceed_limit));
            } else {
              ToastUtils.INSTANCE.showShortToast(
                  context, context.getString(R.string.order_song_failure, code, msg));
            }
          }
        });
  }
}
