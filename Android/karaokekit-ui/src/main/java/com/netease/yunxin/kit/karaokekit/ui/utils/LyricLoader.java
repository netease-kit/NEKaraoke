// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import static com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType.NELyricTypeKas;
import static com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType.NELyricTypeQrc;
import static com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType.NELyricTypeYrc;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.ui.model.LyricBusinessModel;

public class LyricLoader {
  private static final String TAG = "LyricLoader";

  /** 加载歌词 */
  public static void loadLyric(
      NEKaraokeSongModel model, NEKaraokeCallback<LyricBusinessModel> callback) {
    if (model == null || TextUtils.isEmpty(model.getSongId())) {
      if (callback != null) {
        callback.onFailure(-1, "song model is null");
      }
      return;
    }
    String songId = model.getSongId();
    if (songId == null) {
      return;
    }
    if (model.getChannel() == null) {
      return;
    }
    int channel = model.getChannel();
    NECopyrightedMedia.getInstance()
        .preloadSongLyric(
            songId,
            channel,
            new LyricCallback() {

              @Override
              public void success(
                  @Nullable String content, @Nullable String type, @Nullable int channel) {
                if (callback != null) {
                  LyricBusinessModel lyricBusinessModel = new LyricBusinessModel();
                  String midiContent = NECopyrightedMedia.getInstance().getPitch(songId, channel);
                  if (NELyricTypeYrc.getType().equals(type)) {
                    lyricBusinessModel.lyricType = NELyricTypeYrc;
                  } else if (NELyricTypeQrc.getType().equals(type)) {
                    lyricBusinessModel.lyricType = NELyricTypeQrc;
                  } else if (NELyricTypeKas.getType().equals(type)) {
                    lyricBusinessModel.lyricType = NELyricTypeKas;
                  } else {
                    lyricBusinessModel.lyricType = NELyricType.NELyricTypeLrc;
                  }
                  if (TextUtils.isEmpty(content)) {
                    lyricBusinessModel.lyricContent = "";
                  } else {
                    lyricBusinessModel.lyricContent = content;
                  }
                  if (TextUtils.isEmpty(midiContent)) {
                    lyricBusinessModel.midiContent = "";
                  } else {
                    lyricBusinessModel.midiContent = midiContent;
                  }
                  long beginTime = System.currentTimeMillis();
                  NELyric lyric =
                      NELyric.initWithContent(
                          lyricBusinessModel.lyricContent, lyricBusinessModel.lyricType);
                  long cost = System.currentTimeMillis() - beginTime;
                  ALog.d(TAG, "cost:" + cost);
                  lyricBusinessModel.lyric = lyric;
                  lyricBusinessModel.preludeTime = LyricUtil.getPreludeTimeMillis(lyric);
                  ALog.i(
                      TAG,
                      "loadLyric success,lyricContent="
                          + content
                          + "\r\ntype = "
                          + type
                          + "\r\nmidiContent="
                          + midiContent);
                  callback.onSuccess(lyricBusinessModel);
                }
              }

              @Override
              public void error(int code, @Nullable String msg) {
                ALog.i(TAG, "loadLyric error code = " + code + " " + msg);
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }
}
