// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.lyric.ui.util;

import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricLine;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricWord;
import java.util.List;

public class NELyricViewHelper {

  /** 计算出当前时间戳在这一行文字的百分比 */
  public static float getPercentAtLine(long currentTimeMillis, NELyricLine line) {
    //[20850,3450]我(20850,210)现(21060,390)在(21450,270)这(21720,330)一(22050,270)种(22320,270)心(22590,630)情(23220,1080)
    List<NELyricWord> words = line.words;
    if (words == null || words.isEmpty()) {
      return 0f;
    }
    long offsetTime = currentTimeMillis - line.startTime;
    NELyricWord lastWord;
    long allWordDuration = 0L;
    if (words.size() > 0) {
      lastWord = words.get(words.size() - 1);
      allWordDuration = lastWord.startTime + lastWord.interval - line.startTime;
    }
    float progressAll = 0f;
    if (offsetTime < allWordDuration) {
      for (int i = 0; i < words.size(); i++) {
        NELyricWord currentWord = words.get(i);
        if (offsetTime >= currentWord.offset
            && offsetTime <= currentWord.offset + currentWord.interval) {
          float progressBefore = i / (float) words.size();
          float percent = 1 / (float) words.size();
          float progressCurrentWord =
              (offsetTime - currentWord.offset) / (float) currentWord.interval;
          progressAll = progressBefore + progressCurrentWord * percent;
          break;
        } else if (i < words.size() - 1) {
          NELyricWord nextWordInfo = words.get(i + 1);
          if (offsetTime > currentWord.offset + currentWord.interval
              && offsetTime < nextWordInfo.offset) {
            progressAll = (i + 1) / (float) words.size();
          }
        }
      }
    } else {
      progressAll = 1f;
    }
    return progressAll;
  }

  public static long getScrollDuration(NELyric lyric, int fromPosition, int toPosition) {
    long duration = 0;
    try {
      NELyricLine fromLine = lyric.lineModels.get(fromPosition);
      NELyricLine toLine = lyric.lineModels.get(toPosition);
      duration = toLine.startTime - fromLine.startTime - fromLine.interval;
    } catch (Exception ignored) {
    }
    return duration > 0 ? duration : 0;
  }
}
