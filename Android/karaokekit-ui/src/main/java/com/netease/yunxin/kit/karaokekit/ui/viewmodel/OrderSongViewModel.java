// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.NEErrorCode;
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NECopyrightedSong;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaListener;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeOrderSongModel;
import java.util.ArrayList;
import java.util.List;

public class OrderSongViewModel extends ViewModel {
  private NEKaraokeKit karaokeKit = NEKaraokeKit.getInstance();
  private final MutableLiveData<List<NEKaraokeOrderSongModel>> orderSongListChangeEvent =
      new MutableLiveData<>();
  private final MutableLiveData<KaraokeOrderSongModel> performOrderSongEvent =
      new MutableLiveData<>();
  private final MutableLiveData<KaraokeOrderSongModel> performDownloadSongEvent =
      new MutableLiveData<>();
  private final MutableLiveData<KaraokeOrderSongModel> startOrderSongEvent =
      new MutableLiveData<>();

  public void refreshSongList(
      int pageNum,
      int pageSize,
      NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>> callback) {
    karaokeKit.getSongList(
        null,
        null,
        pageNum,
        pageSize,
        new NECopyrightedMedia.Callback<List<NECopyrightedSong>>() {

          @Override
          public void error(int code, @Nullable String msg) {
            ALog.e("getSongList fail code = " + code + ", msg = " + msg);
            callback.error(code, msg);
          }

          @Override
          public void success(@Nullable List<NECopyrightedSong> info) {
            ALog.i("getSongList success:$info");
            List<KaraokeOrderSongModel> songList = new ArrayList<>();
            if (info != null) {
              for (NECopyrightedSong copyrightedSong : info) {
                if (copyrightedSong.getHasAccompany() != 0) {
                  songList.add(new KaraokeOrderSongModel(copyrightedSong));
                }
              }
            }
            callback.success(songList);
          }
        });
  }

  public void searchSong(
      String keyword,
      int pageNum,
      int pageSize,
      NECopyrightedMedia.Callback<List<KaraokeOrderSongModel>> callback) {
    karaokeKit.searchSong(
        keyword,
        null,
        pageNum,
        pageSize,
        new NECopyrightedMedia.Callback<List<NECopyrightedSong>>() {

          @Override
          public void error(int code, @Nullable String msg) {
            ALog.e("searchSong fail code = " + code + ", msg = " + msg);
            callback.error(code, msg);
          }

          @Override
          public void success(@Nullable List<NECopyrightedSong> info) {
            ALog.i("searchSong success:$info");
            List<KaraokeOrderSongModel> songList = new ArrayList<>();
            if (info != null) {
              for (NECopyrightedSong copyrightedSong : info) {
                if (copyrightedSong.getHasAccompany() != 0) {
                  songList.add(new KaraokeOrderSongModel(copyrightedSong));
                }
              }
            }
            callback.success(songList);
          }
        });
  }

  public void refreshOrderSongs() {
    karaokeKit.getOrderedSongs(
        new NEKaraokeCallback<List<NEKaraokeOrderSongModel>>() {

          @Override
          public void onFailure(int code, @Nullable String msg) {
            ALog.e("getOrderSongs fail code = " + code + ", msg = " + msg);
          }

          @Override
          public void onSuccess(@Nullable List<NEKaraokeOrderSongModel> neKaraokeOrderSongModels) {
            ALog.i("getOrderSongs success");
            if (neKaraokeOrderSongModels != null) {
              orderSongListChangeEvent.postValue(neKaraokeOrderSongModels);
            }
          }
        });
  }

  public void preloadSong(String songId, int channel, NESongPreloadCallback callback) {
    if (karaokeKit.isSongPreloaded(songId, channel)) {
      callback.onPreloadComplete(songId, channel, NEErrorCode.OK, "");
    } else {
      karaokeKit.preloadSong(
          songId,
          channel,
          new NEKaraokeCopyrightedMediaListener() {

            @Override
            public void onPreloadStart(String songId, int channel) {
              ALog.i("onPreloadStart songId = " + songId);
              callback.onPreloadStart(songId, channel);
            }

            @Override
            public void onPreloadProgress(String songId, int channel, float progress) {
              ALog.i("onPreloadProgress songId = " + songId + ", progress = " + progress);
              callback.onPreloadProgress(songId, channel, progress);
            }

            @Override
            public void onPreloadComplete(String songId, int channel, int errorCode, String msg) {
              ALog.i(
                  "onPreloadComplete songId = "
                      + songId
                      + ", errorCode = "
                      + errorCode
                      + ", msg = "
                      + msg);
              callback.onPreloadComplete(songId, channel, errorCode, msg);
            }
          });
    }
  }

  public void orderSong(
      KaraokeOrderSongModel copyrightSong, NECopyrightedMedia.Callback<Boolean> callback) {
    karaokeKit.orderSong(
        buildLocalOrderSong(copyrightSong),
        new NEKaraokeCallback<NEKaraokeOrderSongModel>() {

          @Override
          public void onFailure(int code, @Nullable String msg) {
            ALog.e("orderSong fail: code = " + code + ", msg = " + msg);
            callback.error(code, msg);
          }

          @Override
          public void onSuccess(@Nullable NEKaraokeOrderSongModel neKaraokeOrderSongModel) {
            ALog.i("orderSong success");
            callback.success(true);
          }
        });
  }

  public void deleteSong(long orderId, NECopyrightedMedia.Callback<Boolean> callback) {
    karaokeKit.deleteSong(
        orderId,
        new NEKaraokeCallback<Void>() {

          @Override
          public void onFailure(int code, @Nullable String msg) {
            ALog.e("deleteSong fail: code = " + code + ", msg = " + msg);
            callback.error(code, msg);
          }

          @Override
          public void onSuccess(@Nullable Void unused) {
            ALog.i("deleteSong success");
            callback.success(true);
          }
        });
  }

  public void topSong(long orderId, NECopyrightedMedia.Callback<Boolean> callback) {
    karaokeKit.topSong(
        orderId,
        new NEKaraokeCallback<Void>() {

          @Override
          public void onFailure(int code, @Nullable String msg) {
            ALog.e("topSong fail:$msg");
            callback.error(code, msg);
          }

          @Override
          public void onSuccess(@Nullable Void unused) {
            ALog.i("topSong success");
            callback.success(true);
          }
        });
  }

  public void nextSong() {
    if (orderSongListChangeEvent.getValue() != null
        && orderSongListChangeEvent.getValue().size() > 0) {
      karaokeKit.nextSong(
          orderSongListChangeEvent.getValue().get(0).getOrderId(),
          new NEKaraokeCallback<Void>() {

            @Override
            public void onFailure(int code, @Nullable String msg) {
              ALog.e("nextSong fail code = " + code + ", msg = " + msg);
            }

            @Override
            public void onSuccess(@Nullable Void unused) {
              ALog.i("nextSong success");
            }
          });
    }
  }

  private NEKaraokeOrderSongModel buildLocalOrderSong(KaraokeOrderSongModel copyrightSong) {
    return new NEKaraokeOrderSongModel(
        copyrightSong.getSongId(),
        copyrightSong.getSongName(),
        copyrightSong.getSongCover(),
        copyrightSong.getSongTime(),
        copyrightSong.getChannel());
  }

  public MutableLiveData<List<NEKaraokeOrderSongModel>> getOrderSongListChangeEvent() {
    return orderSongListChangeEvent;
  }

  public MutableLiveData<KaraokeOrderSongModel> getPerformOrderSongEvent() {
    return performOrderSongEvent;
  }

  public MutableLiveData<KaraokeOrderSongModel> getPerformDownloadSongEvent() {
    return performDownloadSongEvent;
  }

  public MutableLiveData<KaraokeOrderSongModel> getStartOrderSongEvent() {
    return startOrderSongEvent;
  }
}
