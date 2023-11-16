// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.yunxin.kit.copyrightedmedia.api.NEKaraokeGradeListener;
import com.netease.yunxin.kit.copyrightedmedia.api.NEPitchSongScore;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricModelSingerOption;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEOpusLevel;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchAudioData;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchItemModel;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchRecordSingInfo;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchRecordSingMarkModel;
import com.netease.yunxin.kit.karaokekit.pitch.ui.databinding.PitchSingMixUiBinding;
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEKTVPlayResultModel;
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEPitchLayoutBuilder;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ImageLoader;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.LyricUtil;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.PitchUILog;
import org.jetbrains.annotations.NotNull;

/** 音准打分、歌词、最终得分UI */
public class NEPitchRecordComponentView extends ConstraintLayout {
  private static final String TAG = "NERecordComponentView";
  private NELyric lyric;
  private PitchSingMixUiBinding binding;
  private NEPitchSongScore singMarker;
  private NEKaraokeGradeListener gradeListener =
      new NEKaraokeGradeListener() {
        @Override
        public void onNote(@NotNull NEPitchItemModel itemModel) {
          //          PitchUILog.d(TAG, "onNote:invalid note start time = " + noteInfo.getStart() + ", currentTimeMillis = " + currentTimeMillis);
          binding.pitchView.addNewPitch(
              itemModel.startTime, itemModel.duration, itemModel.pitchNote);
        }

        @Override
        public void onGrade(@NotNull NEPitchRecordSingMarkModel markModel) {
          binding.pitchHighScoreView.show((int) markModel.value);
          binding.pitchHighScoreDoubleHit.show((int) markModel.value);
        }
      };

  public NEPitchRecordComponentView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public NEPitchRecordComponentView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public NEPitchRecordComponentView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    binding = PitchSingMixUiBinding.inflate(LayoutInflater.from(context), this, true);
    binding.pitchHighScoreView.customThresHold(50, 60, 80);
    binding.pitchView.setPitchEffectView(binding.pitchEffectView);
    singMarker = NEPitchSongScore.getInstance();
  }

  /**
   * 刷新数据
   *
   * @param currentTimeMillis 当前歌曲播放的时间戳
   */
  public void update(long currentTimeMillis) {
    binding.lyricView.update(currentTimeMillis);
    binding.pitchView.updatePitchOffset((int) currentTimeMillis);
    singMarker.update(currentTimeMillis);
  }

  /**
   * 数据初始化
   *
   * @param midiContent midi内容
   * @param separator 分隔符
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param lyricContent 歌词内容
   * @param type 歌词类型
   */
  public void loadRecordDataWithPitchContent(
      String midiContent,
      String separator,
      int startTime,
      int endTime,
      String lyricContent,
      NELyricType type,
      NEPitchLayoutBuilder pitchLayoutBuilder) {
    PitchUILog.d(TAG, "loadRecordDataWithPitchContent");
    update(0);
    resetMidi();
    NEPitchRecordSingInfo pitchRecordSingInfo =
        NEPitchRecordSingInfo.createInfoWithPitchContent(
            midiContent, separator, startTime, endTime, lyricContent, type);
    if (pitchRecordSingInfo == null) {
      return;
    }
    lyric = pitchRecordSingInfo.getLyric();
    binding.lyricView.loadWithLyricModel(lyric, startTime, endTime);
    singMarker.removeGradeListener(gradeListener);
    singMarker.initialize(pitchRecordSingInfo);
    singMarker.addGradeListener(gradeListener);
    if (pitchLayoutBuilder != null) {
      NEPitchView.CustomConfigBuilder customConfig =
          new NEPitchView.CustomConfigBuilder()
              .setLineHeight(pitchLayoutBuilder.lineHeight)
              .setPitchBaseColor(pitchLayoutBuilder.bottomColor)
              .setPitchHitColor(pitchLayoutBuilder.drawColor)
              .setMaskShaderStartColor(pitchLayoutBuilder.linearGradientStartColor)
              .setMaskShaderEndColor(pitchLayoutBuilder.linearGradientEndColor)
              .build();
      binding.pitchView.setCustomConfig(customConfig);
    }
    binding.pitchView.reset();
    binding.pitchView.initParam(
        NELyricModelSingerOption.NELyricModelSingerOptionNull,
        true,
        pitchRecordSingInfo.pitchModel);
    showFinalScore(false);
  }

  /**
   * 跳转到指定开始播放时间
   *
   * @param startTime 开始播放时间戳
   */
  public void seekTime(long startTime) {
    PitchUILog.d(TAG, "seekTime，startTime：" + startTime);
    if (lyric == null) {
      return;
    }
    binding.lyricView.loadWithLyricModel(lyric, startTime, LyricUtil.getEndTimeMillis(lyric));
    binding.pitchView.setSeekTime((int) startTime);
    binding.pitchView.updatePitchOffset((int) startTime);
  }

  public void hideScoreView() {
    PitchUILog.d(TAG, "hideScoreView");
    binding.singScoreView.hideScoreAnim();
    binding.singScoreView.setVisibility(GONE);
  }

  /**
   * 最终得分相关
   *
   * @param userData 用户数据
   * @param level 演唱分数等级
   * @param callback webp动图加载完成回调
   */
  public void showScoreViewWithUserData(
      NEKTVPlayResultModel userData,
      NEOpusLevel level,
      ImageLoader.WebpAnimationPlayEndCallback callback) {
    PitchUILog.d(TAG, "showScoreViewWithUserData,level:" + level);
    showFinalScore(true);
    binding.singScoreView.setSingInfo(
        new SingScoreInfo(userData.songName, userData.headerUrl, userData.nickName, false));
    binding.singScoreView.updateLevel(level, callback);
  }

  /** 录唱过程中seek，就是跳过一些句子或者重新从某个句子开始唱，需要刷新一下打分库。 */
  public void resetMidi() {
    PitchUILog.d(TAG, "resetMidi");
    postDelayed(() -> singMarker.resetPitch(), 100);
  }

  /** 销毁打分 */
  public void pitchDestroy() {
    PitchUILog.d(TAG, "pitchDestroy");
    singMarker.destroy();
  }

  /** 暂停打分 */
  public void pitchPause() {
    PitchUILog.d(TAG, "pitchPause");
    singMarker.pause();
    binding.pitchEffectView.pause();
  }

  /** 开始打分 */
  public void pitchStart() {
    PitchUILog.d(TAG, "pitchStart");
    singMarker.start();
    binding.pitchEffectView.resume();
  }

  /**
   * 推送流数据
   *
   * @param audioData 流数据
   */
  public void pushAudioData(@NotNull NEPitchAudioData audioData) {
    //    PitchUILog.d(TAG, "onNote:pushAudioData time = " + audioData.timeStamp);
    singMarker.pushAudioData(audioData);
  }

  /** 是否需要展示最终打分 */
  public boolean needShowFinalScore() {
    return singMarker.isValidSong();
  }

  private void showFinalScore(boolean show) {
    PitchUILog.d(TAG, "showFinalScore,show:" + show);
    if (show) {
      binding.singScoreView.setVisibility(VISIBLE);
      binding.lyricView.setVisibility(GONE);
      binding.pitchView.setVisibility(GONE);
      binding.pitchEffectView.setVisibility(GONE);
      binding.pitchHighScoreView.setVisibility(GONE);
    } else {
      binding.singScoreView.setVisibility(GONE);
      binding.lyricView.setVisibility(VISIBLE);
      binding.pitchEffectView.setVisibility(VISIBLE);
      binding.pitchHighScoreView.setVisibility(GONE);
      if (singMarker.isValidSong()) {
        binding.pitchView.setVisibility(VISIBLE);
      } else {
        binding.pitchView.setVisibility(GONE);
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    PitchUILog.d(TAG, "onDetachedFromWindow");
    pitchDestroy();
  }
}
