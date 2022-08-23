// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.copyrightedmedia.api.LyricCallback;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.lyric.model.NELyric;
import com.netease.yunxin.kit.karaokekit.ui.model.LyricBusinessModel;

public class LyricLoader {
  private static final String TAG = "LyricLoader";
  private static final String YRC_TYPE = "yrc";
  private static final String QRC_TYPE = "qrc";

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
    NECopyrightedMedia.getInstance()
        .preloadSongLyric(
            songId,
            new LyricCallback() {

              @Override
              public void success(@Nullable String content, @Nullable String type) {
                if (callback != null) {
                  LyricBusinessModel lyricBusinessModel = new LyricBusinessModel();
                  String midiContent = NECopyrightedMedia.getInstance().getMidi(songId);
                  if (YRC_TYPE.equals(type)) {
                    lyricBusinessModel.lyricType = NELyric.NELyricType.NELyricTypeYrc;
                  } else if (QRC_TYPE.equals(type)) {
                    lyricBusinessModel.lyricType = NELyric.NELyricType.NELyricTypeQrc;
                  } else {
                    lyricBusinessModel.lyricType = NELyric.NELyricType.NELyricTypeLrc;
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
