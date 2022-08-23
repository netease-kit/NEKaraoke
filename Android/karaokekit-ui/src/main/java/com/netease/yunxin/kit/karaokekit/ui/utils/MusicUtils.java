// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.utils;

import android.content.Context;
import java.io.File;

/** 音乐相关 */
public class MusicUtils {
  //
  // play music files
  //

  /** 混音以及伴奏 */
  public static final String MUSIC_DIR = "music";

  public static final String MUSIC1 = "music1.mp3";
  public static final String MUSIC2 = "music2.mp3";
  public static final String MUSIC3 = "music3.mp3";
  public static final String EFFECT1 = "effect1.wav";
  public static final String EFFECT2 = "effect2.wav";

  /** ktv播放音乐 */
  public static final String LANRUOCI = "lanruoci.mp3";

  public static final String HUYAN = "huyan.mp3";
  public static final String DANQINGKE = "danqingke.mp3";

  public static final String CHNEGDU = "chengdu.m4a";
  public static final String HOULAI = "houlai.m4a";
  public static final String WOSHIYIZHIYU = "woshiyizhiyu.m4a";

  public static final String WODEMIMI = "117465-origin-aac256.m4a";
  public static final String WODEMIMI_BGM = "117465-accompany-aac256.m4a";

  public static String extractMusicFile(Context context, String name) {
    String root = ensureMusicDirectory(context);
    CommonUtil.copyAssetToFile(context, MUSIC_DIR + File.separator + name, root, name);
    return new File(root, name).getAbsolutePath();
  }

  private static String ensureMusicDirectory(Context context) {
    File dir = context.getExternalFilesDir(MUSIC_DIR);
    if (dir == null) {
      dir = context.getDir(MUSIC_DIR, 0);
    }
    if (dir != null) {
      dir.mkdirs();
      return dir.getAbsolutePath();
    }
    return "";
  }
}
