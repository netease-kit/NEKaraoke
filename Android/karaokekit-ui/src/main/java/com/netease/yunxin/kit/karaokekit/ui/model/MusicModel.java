// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.model;

import android.text.TextUtils;
import java.io.Serializable;
import java.util.Objects;

public class MusicModel implements Serializable {

  public String id; //音乐ID

  public String name; //音乐名称

  public String singer; //音乐作者

  public String avatar; //歌曲头像

  public String url; //歌曲地址

  public String accompanimentUrl; //歌曲地址

  public String lyricUrl; //歌词地址

  public String localPath; //歌曲本地地址

  public String accompanimentLocalPath; //伴奏本地地址

  public String lyricLocalPath = "lyric/117465-yrc.txt"; //歌词本地地址
  public String midiLocalPath = "midi/117465-midi.txt"; //midi本地地址

  public String duration; //时长

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MusicModel music = (MusicModel) o;
    return TextUtils.equals(id, music.id)
        && TextUtils.equals(name, music.name)
        && TextUtils.equals(singer, music.singer)
        && TextUtils.equals(avatar, music.avatar)
        && TextUtils.equals(url, music.url)
        && TextUtils.equals(lyricUrl, music.lyricUrl)
        && TextUtils.equals(duration, music.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, singer, avatar, url, lyricUrl, duration);
  }
}
