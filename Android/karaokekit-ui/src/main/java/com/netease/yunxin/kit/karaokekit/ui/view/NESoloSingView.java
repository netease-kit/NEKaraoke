// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEOpusLevel;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchAudioData;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchRecordSingInfo;
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEKTVPlayResultModel;
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEPitchLayoutBuilder;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ImageLoader;
import com.netease.yunxin.kit.karaokekit.ui.databinding.KaraokeSoloSingViewBinding;
import com.netease.yunxin.kit.karaokekit.ui.utils.LyricUtil;
import com.netease.yunxin.kit.karaokekit.ui.utils.SoloSingTimer;

public class NESoloSingView extends FrameLayout {
  private static final String TAG = "NESoloSingView";
  private Context context;
  private final float LINE_HEIGHT = 10f;
  private final int BOTTOM_COLOR = Color.WHITE;
  private final int DRAW_COLOR = Color.parseColor("#ff3764");
  private final int LINEAR_GRADIENT_START_COLOR = Color.parseColor("#334BF0FB");
  private final int LINEAR_GRADIENT_END_COLOR = Color.BLUE;
  private final long PERIOD = 30L;
  private NELyric lyric = null;
  private OnSkipPreludeClickListener skipPreludeClick = null;
  private KaraokeSoloSingViewBinding soloSingViewBinding;
  private NEPitchLayoutBuilder pitchLayoutBuilder = new NEPitchLayoutBuilder();

  private SoloSingTimer updateUITimer = null;
  private long timerPosition = 0;
  private boolean paused = false;
  private boolean hasAdjustTimeStamp = false;
  private Runnable runnable =
      new Runnable() {

        @Override
        public void run() {
          if (paused) {
            return;
          }
          timerPosition += PERIOD;
          if (getVisibility() == VISIBLE) {
            soloSingViewBinding.gradeView.update(timerPosition);
          }
        }
      };

  public NESoloSingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    soloSingViewBinding =
        KaraokeSoloSingViewBinding.inflate(LayoutInflater.from(context), this, true);
    init();
  }

  private void init() {
    configPitchLayoutBuilder();
    soloSingViewBinding.skipPreludeView.setSkipPreludeClickListener(
        new NESkipPreludeView.OnSkipPreludeClickListener() {

          @Override
          public void onSkipPrelude(int seekTime) {
            if (skipPreludeClick != null) {
              skipPreludeClick.onSkipPrelude(seekTime);
            }
            soloSingViewBinding.skipPreludeView.setVisibility(INVISIBLE);
            ALog.d(TAG, "onSkipPrelude,seekTime:$seekTime");
          }

          @Override
          public void onPreludeEnd() {
            soloSingViewBinding.skipPreludeView.setVisibility(INVISIBLE);
            ALog.d(TAG, "onPreludeEnd");
          }
        });
  }

  private void configPitchLayoutBuilder() {
    pitchLayoutBuilder.lineHeight = LINE_HEIGHT;
    pitchLayoutBuilder.bottomColor = BOTTOM_COLOR;
    pitchLayoutBuilder.drawColor = DRAW_COLOR;
    pitchLayoutBuilder.linearGradientStartColor = LINEAR_GRADIENT_START_COLOR;
    pitchLayoutBuilder.linearGradientEndColor = LINEAR_GRADIENT_END_COLOR;
  }

  void showLyricAndScore(String lyricContent, String midiContent, NELyric.NELyricType lyricType) {
    String separator = ",";
    if (lyricType == NELyric.NELyricType.NELyricTypeQrc) {
      separator = " ";
    }
    lyric = NELyric.initWithContent(lyricContent, lyricType);
    soloSingViewBinding.skipPreludeView.setupLyric(lyric);
    soloSingViewBinding.gradeView.loadRecordDataWithPitchContent(
        midiContent,
        separator,
        0,
        LyricUtil.getEndTimeMillis(lyric),
        lyricContent,
        lyricType,
        pitchLayoutBuilder);
  }

  void update(long timestamp) {
    if (!hasAdjustTimeStamp) {
      timerPosition = timestamp;
      hasAdjustTimeStamp = true;
    }
  }

  void seek(long startTime) {
    ALog.d(TAG, "seek,startTime:$startTime");
    timerPosition = startTime;
    soloSingViewBinding.gradeView.seekTime(startTime);
  }

  void setOnSkipPreludeClickListener(OnSkipPreludeClickListener skipPreludeClick) {
    this.skipPreludeClick = skipPreludeClick;
  }

  void showFinalScore(
      NEKTVPlayResultModel userData,
      NEPitchRecordSingInfo finalScore,
      ImageLoader.WebpAnimationPlayEndCallback callback) {
    ALog.d(TAG, "showFinalScore");
    float level = 0;
    if (finalScore.finalMark != null) {
      level = finalScore.finalMark.totalValue;
    }

    if (finalScore.availableLyricCount == 0) {
      level = 0;
    } else {
      level /= finalScore.availableLyricCount;
    }
    NEOpusLevel opusLevel;
    if (level > 90) {
      opusLevel = NEOpusLevel.NEOpusLevelSSS;
    } else if (level > 80) {
      opusLevel = NEOpusLevel.NEOpusLevelSS;
    } else {
      opusLevel = NEOpusLevel.NEOpusLevelS;
    }

    soloSingViewBinding.gradeView.showScoreViewWithUserData(userData, opusLevel, callback);
    pause();
  }

  void pause() {
    ALog.d(TAG, "pause");
    paused = true;
    soloSingViewBinding.gradeView.pitchPause();
  }

  void hideScoreView() {
    ALog.d(TAG, "hideScoreView");
    soloSingViewBinding.gradeView.hideScoreView();
  }

  void pushAudioData(NEPitchAudioData audioData) {
    //        ALog.d(TAG, "pushAudioData,audioData:$audioData")
    soloSingViewBinding.gradeView.pushAudioData(audioData);
  }

  void start() {
    ALog.d(TAG, "start");
    paused = false;
    hasAdjustTimeStamp = false;
    if (updateUITimer == null) {
      timerPosition = 0;
      updateUITimer = new SoloSingTimer(runnable);
    }
    updateUITimer.scheduleAtFixedRate(0, PERIOD);
    soloSingViewBinding.gradeView.pitchStart();
  }

  void resetMidi() {
    ALog.d(TAG, "resetMidi");
    soloSingViewBinding.gradeView.resetMidi();
  }

  boolean needShowFinalScore() {
    return soloSingViewBinding.gradeView.needShowFinalScore();
  }

  void destroyTimer() {
    timerPosition = 0;
    paused = false;
    hasAdjustTimeStamp = false;
    if (updateUITimer != null) {
      updateUITimer.destroy();
      updateUITimer = null;
    }
  }

  public interface OnSkipPreludeClickListener {
    void onSkipPrelude(int seekTime);
  }
}
