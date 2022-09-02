// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import static com.netease.yunxin.kit.karaokekit.pitch.ui.widget.NESingScoreViewKt.COUNT_DOWN_INTERVAL;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.PitchUILog;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.utils.LyricUtil;

public class NESkipPreludeView extends androidx.appcompat.widget.AppCompatTextView {
  private static final String TAG = "NESkipPreludeView";
  private CountDownTimer countDownTimer = null;
  private NELyric lyric = null;
  private final Context context;
  private int seekTime = 0;
  private OnSkipPreludeClickListener listener = null;

  /** 最小前奏时间，小于10s不展示跳过前奏 */
  private static final int MIN_PRELUDE_TIME = 10000;

  private static final int PRELUDE_BUFFER_TIME = 5000;

  public NESkipPreludeView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init();
  }

  private void init() {
    int dp10 = dip2px(10f);
    int dp5 = dip2px(5f);
    setPadding(dp10, dp5, dp10, dp5);
    setTextColor(Color.WHITE);
    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
  }

  void setupLyric(NELyric lyric) {
    this.lyric = lyric;
    int preludeTimeMillis = LyricUtil.getPreludeTimeMillis(lyric);
    if (preludeTimeMillis > 0) {
      if (preludeTimeMillis - MIN_PRELUDE_TIME > 0) {
        seekTime = preludeTimeMillis - PRELUDE_BUFFER_TIME;
        setVisibility(View.VISIBLE);
        setBackgroundResource(R.drawable.karaoke_skip_prelude_bg);
        setPreludeTime(preludeTimeMillis);
      } else {
        setVisibility(View.INVISIBLE);
        setBackgroundResource(0);
      }
    } else {
      setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 设置前奏时间
   *
   * @param preludeTimeMillis 前奏时间 ms
   */
  private void setPreludeTime(int preludeTimeMillis) {
    long preludeSeconds = handleTime(preludeTimeMillis);
    PitchUILog.d(
        TAG, "setPreludeTime,preludeTimeMillis:$preludeTimeMillis,preludeSeconds:$preludeSeconds");
    setText(
        new StringBuilder()
            .append(context.getText(R.string.karaoke_skip_prelude))
            .append("(")
            .append(preludeSeconds)
            .append("s")
            .append(")"));
    startCountdown(preludeSeconds);
  }

  private long handleTime(int millis) {
    return Math.round((millis / 1000f));
  }

  private int dip2px(float dipValue) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale + 0.5f);
  }

  private void startCountdown(long preludeSeconds) {
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
    countDownTimer =
        new CountDownTimer(preludeSeconds * 1000L, COUNT_DOWN_INTERVAL) {

          @Override
          public void onTick(long millisUntilFinished) {
            PitchUILog.d(TAG, "onTick:$millisUntilFinished");
            setText(
                new StringBuilder()
                    .append(context.getText(R.string.karaoke_skip_prelude))
                    .append("(")
                    .append((millisUntilFinished / COUNT_DOWN_INTERVAL + 1))
                    .append("s")
                    .append(")"));
          }

          @Override
          public void onFinish() {
            if (listener != null) {
              listener.onPreludeEnd();
            }
            PitchUILog.d(TAG, "onFinish，listener:$listener");
          }
        };
    countDownTimer.start();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (countDownTimer != null) {
      countDownTimer.cancel();
      countDownTimer = null;
    }
  }

  public void setSkipPreludeClickListener(OnSkipPreludeClickListener listener) {
    this.listener = listener;
    setOnClickListener(v -> listener.onSkipPrelude(seekTime));
  }

  public interface OnSkipPreludeClickListener {
    void onSkipPrelude(int seekTime);

    void onPreludeEnd();
  }
}
