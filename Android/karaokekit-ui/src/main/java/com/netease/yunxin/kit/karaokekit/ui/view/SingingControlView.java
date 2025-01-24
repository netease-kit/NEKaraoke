// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import static com.netease.yunxin.kit.copyrightedmedia.api.SongResType.TYPE_ACCOMP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.DateFormatUtils;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.NEPitchSongScore;
import com.netease.yunxin.kit.copyrightedmedia.api.SongResType;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchAudioData;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchRecordSingInfo;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.entertainment.common.utils.NetUtils;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaListener;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode;
import com.netease.yunxin.kit.karaokekit.api.model.*;
import com.netease.yunxin.kit.karaokekit.audioeffect.api.NEAudioEffectManager;
import com.netease.yunxin.kit.karaokekit.audioeffect.ui.NEAudioEffectUIConstants;
import com.netease.yunxin.kit.karaokekit.audioeffect.ui.ToneDialogFragment;
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil;
import com.netease.yunxin.kit.karaokekit.lyric.ui.widget.NELyricView;
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEKTVPlayResultModel;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.activity.KaraokeRoomActivity;
import com.netease.yunxin.kit.karaokekit.ui.dialog.CommonDialog;
import com.netease.yunxin.kit.karaokekit.ui.listener.MyKaraokeListener;
import com.netease.yunxin.kit.karaokekit.ui.model.LyricBusinessModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeSeatUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUIUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.LyricLoader;
import java.util.Objects;
import kotlin.Unit;

/** 演唱区控制view */
public class SingingControlView extends LinearLayout implements ISingViewController {

  private static final String TAG = "SingingControlView";

  private RoomModel roomModel;

  private NESoloSingView flSolo; // 独唱模块，带打分、歌词、跳过前奏

  private NELyricView lrcView;

  private TextView tvOrderSong;

  private FrameLayout flySing; // / 包含歌词，歌曲名称的布局，为了设置最底部的背景

  private HeadImageView ivUserAvatar;

  private HeadImageView ivChorusAvatar;

  private TextView soloTextView; // / 独唱按钮

  private TextView tvUserNick;

  private TextView tvTime;

  private TextView tvMusicName;

  private LinearLayout llyReady; // / 开始播放，匹配中UI。。。匹配上之后UI控制。显示要fly_singing设置为可见

  private RelativeLayout rlySing; // / 包含歌词，歌曲名称的布局，要显示歌词必要要先设置flySing 可见

  private LinearLayout llyNoOrderSong; // / 无歌状态的UI

  private TextView ivSoundSetting;

  private TextView ivPause;

  private TextView ivNext; // / 切歌

  private TextView ivSwitchOrigin; // 原声/伴唱切换

  private LinearLayout llyControl;

  private LottieAnimationView lavLoading;

  private CountDownTimer timerForMatch; // 准备倒计时

  private CountDownTimer timerForPlay; // 开始唱 倒计时

  private boolean paused = false; // 已暂停

  private boolean currentOrigin = false;

  private NEKaraokeSongModel currentSongModel; // / 当前歌曲信息

  private static final long MATCH_TIME = 10000; // / 匹配倒计时

  private static final long PLAY_TIME = 5000; // / 播放倒计时

  private int currentTime = 3; // / 倒计时剩余时间，"轮到你"对话框展示使用

  private CommonDialog turnToYouDialog; // / "轮到你"对话框

  private CommonDialog soloTipDialog; // 匹配失败后，提示是否进入独唱

  private CountDownTimer timerForSoloTip; // 进入独唱提示，倒计时

  private final long preludeBufferTime = 5 * 1000;

  private int preludeTime;

  private boolean alreadyPlayLoadAnimate = false;

  private static final int KTV_STATE_INIT = 0; // 初始状态

  private static final int KTV_STATE_WAITING_FOR_CHORUS = 1; // 发送邀请后的状态

  private static final int KTV_STATE_WAITING_FOR_RESOURCES = 2; // 显示伴奏加载中状态

  private static final int KTV_STATE_WAITING_FOR_SOLE = 3; // 等待独唱确认状态

  private static final int KTV_STATE_SINGING = 4; // 演唱中状态

  private static final int KTV_STATE_GRADE = 5; // 打分状态

  private OnSongModelChangeListener songModelChangeListener;

  private LinearLayout llSongTime;

  private TextView tvSongTime;

  private MyKaraokeListener listener;

  public static final long REAL_DELAY_TIME = 3000;
  private long currentPosition;

  private long tempTime = 0;
  private final long DELAY_TIME = 15000;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable accompanyDelayTask =
      () ->
          ToastUtils.INSTANCE.showShortToast(
              getContext().getApplicationContext(),
              getResources().getString(R.string.download_fail));

  public SingingControlView(Context context) {
    super(context);
    initView();
  }

  public SingingControlView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public SingingControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  public void init(RoomModel roomModel) {
    this.roomModel = roomModel;
    NEKaraokeKit.getInstance()
        .requestPlayingSongInfo(
            new NEKaraokeCallback<NEKaraokeSongModel>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeSongModel karaokeSongModel) {
                ALog.i(TAG, "currentRoomSingInfo success,neKaraokeSongModel:" + karaokeSongModel);
                currentSongModel = karaokeSongModel;
                if (songModelChangeListener != null) {
                  songModelChangeListener.onSongModelChange(currentSongModel);
                }
                if (karaokeSongModel != null
                    && !TextUtils.isEmpty(karaokeSongModel.getSongId())
                    && (karaokeSongModel.getSongStatus() != null)
                    && (karaokeSongModel.getSongStatus() == NEKaraokeSongStatus.PAUSE
                        || karaokeSongModel.getSongStatus() == NEKaraokeSongStatus.PLAY)) {
                  switchKTVState(KTV_STATE_SINGING);
                  initLyricView();
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "currentRoomSingInfo failed code = " + code + ", msg = " + msg);
              }
            });
  }

  private void initView() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_sing_control_layout, this, true);
    flSolo = findViewById(R.id.fl_solo);
    lrcView = findViewById(R.id.lyricView);
    lrcView.reset(" ");
    tvOrderSong = findViewById(R.id.tv_order);
    flySing = findViewById(R.id.fly_singing);
    llyNoOrderSong = findViewById(R.id.lly_no_ordered_song);
    ivUserAvatar = findViewById(R.id.iv_user_avatar);
    ivChorusAvatar = findViewById(R.id.chorus_user_avatar);
    soloTextView = findViewById(R.id.solo);
    tvUserNick = findViewById(R.id.tv_user_nick);
    tvMusicName = findViewById(R.id.tv_music_name);
    tvTime = findViewById(R.id.tv_time);
    llyReady = findViewById(R.id.lly_ready);
    rlySing = findViewById(R.id.rly_music_singing);
    llyControl = findViewById(R.id.lly_control);
    ivSoundSetting = findViewById(R.id.iv_set_sound);
    ivPause = findViewById(R.id.iv_pause);
    ivNext = findViewById(R.id.iv_next_music);
    ivSwitchOrigin = findViewById(R.id.iv_switch_origin);
    ImageView ivBackground = findViewById(R.id.iv_bg);
    lavLoading = findViewById(R.id.loading);
    llSongTime = findViewById(R.id.ll_song_time);
    tvSongTime = findViewById(R.id.tv_song_time);
    ImageLoader.with(getContext())
        .load(R.drawable.diange_bg)
        .roundedCorner(ScreenUtil.dip2px(10))
        .into(ivBackground);
    ivPause.setOnClickListener(v -> playOrPause());
    ivNext.setOnClickListener(v -> clickNextSong());
    ivSwitchOrigin.setOnClickListener(v -> clickSwitchToOriginalVolume());
    ivSoundSetting.setOnClickListener(v -> clickEffectView());
    flSolo.setOnSkipPreludeClickListener(
        seekTime -> {
          ALog.d(TAG, "onSkipPrelude:" + seekTime);
          NEKaraokeKit.getInstance().setPlayingPosition(seekTime);
          flSolo.seek(seekTime);
          flSolo.resetMidi();
        });
    registerKaraokeListener();
  }

  private void startLoading() {
    String LOADING_LOTTIE_RED = "loading/red.json";
    lavLoading.setAnimation(LOADING_LOTTIE_RED);
    lavLoading.playAnimation();
  }

  private void onStartSong(NEKaraokeSongModel model) { //  同意邀请处理
    if (model == null) {
      ALog.e(TAG, "startSong but model == null");
      return;
    }

    if (model.getChannel() == null) {
      ALog.e(TAG, "startSong but model.getChannel() == null");
      return;
    }

    String typeOriginPath =
        NEKaraokeKit.getInstance()
            .getSongURI(
                Objects.requireNonNull(model.getSongId()),
                model.getChannel(),
                SongResType.TYPE_ORIGIN);
    String typeAccPath =
        NEKaraokeKit.getInstance().getSongURI(model.getSongId(), model.getChannel(), TYPE_ACCOMP);
    ALog.i(TAG, "typeOriginPath--->" + typeOriginPath);
    ALog.i(TAG, "typeAccPath--->" + typeAccPath);
    ALog.i(TAG, "songMode--->" + model);
    String originPath = "";
    String accompanyPath = "";
    boolean enableSwitchSongTypeNotSerial = true;
    if (!TextUtils.isEmpty(typeOriginPath)) {
      originPath = typeOriginPath;
    }
    if (!TextUtils.isEmpty(typeAccPath)) {
      accompanyPath = typeAccPath;
    }
    if (!TextUtils.isEmpty(typeOriginPath) && TextUtils.isEmpty(typeAccPath)) {
      originPath = typeOriginPath;
      accompanyPath = typeOriginPath;
      enableSwitchSongTypeNotSerial = false;
      handler.post(() -> changeSwitchOriginUI(true, false));
    }
    if (TextUtils.isEmpty(typeOriginPath) && !TextUtils.isEmpty(typeAccPath)) {
      originPath = typeAccPath;
      accompanyPath = typeAccPath;
      enableSwitchSongTypeNotSerial = false;
    }
    String anchorUuid = currentSongModel.getUserUuid();
    String chorusUid = currentSongModel.getAssistantUuid();
    long startTimeStamp = 0;
    alreadyPlayLoadAnimate = false;
    handleControlView(enableSwitchSongTypeNotSerial);
    switchKTVState(KTV_STATE_SINGING);
    if (currentSongModel.getAssistantUuid() == null) { // / 没有副唱的时候，就是独唱
      if (isLocalMain()) {
        String finalOriginPath = originPath;
        String finalAccompanyPath = accompanyPath;
        LyricLoader.loadLyric(
            model,
            new NEKaraokeCallback<LyricBusinessModel>() {
              @Override
              public void onSuccess(@Nullable LyricBusinessModel lyricBusinessModel) {
                if (lyricBusinessModel == null) {
                  return;
                }
                isLyricAlready = !TextUtils.isEmpty(lyricBusinessModel.lyricContent);
                refreshSingView();
                preludeTime = lyricBusinessModel.preludeTime;
                llyControl.setVisibility(VISIBLE);
                playSong(
                    finalOriginPath,
                    finalAccompanyPath,
                    anchorUuid,
                    chorusUid,
                    startTimeStamp,
                    true,
                    NEKaraokeSongMode.SOLO);
                // 显示歌词，可能会导致上一首歌的音频数据还在回调，先放在play后面
                showLyricAndScore(lyricBusinessModel);
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                showNoLyric();
                ALog.e(TAG, "loadLyric failed code = " + code + ", msg = " + msg);
              }
            });

      } else {
        initLyricView();
      }
    } else { // 合唱
      NEKaraokeSongMode mode = getNeSongMode();
      switchKTVState(KTV_STATE_SINGING);
      if (isLocalMain()) { // 主唱
        initLyricView();
        playSong(originPath, accompanyPath, anchorUuid, chorusUid, REAL_DELAY_TIME, true, mode);
      } else if (isLocalAssistant()) { // 副唱
        initLyricView();
        playSong(originPath, accompanyPath, anchorUuid, chorusUid, REAL_DELAY_TIME, false, mode);
      } else { // 听众
        initLyricView();
      }
    }
  }

  private void playSong(
      String finalOriginPath,
      String finalAccompanyPath,
      String anchorUuid,
      String chorusUid,
      long startTimeStamp,
      boolean isAnchor,
      NEKaraokeSongMode mode) {

    NEAudioEffectManager.INSTANCE.adjustRecordingSignalVolumeWithRemember(
        NEAudioEffectManager.INSTANCE.getRecordingSignalVolume());
    NEAudioEffectManager.INSTANCE.adjustPlaybackSignalVolumeWithRemember(
        NEAudioEffectManager.INSTANCE.getOtherSignalVolume());

    NEKaraokeKit.getInstance()
        .playSong(
            finalOriginPath,
            finalAccompanyPath,
            NEAudioEffectManager.INSTANCE.getAudioMixingVolume(),
            anchorUuid,
            chorusUid,
            startTimeStamp,
            isAnchor,
            mode,
            null);
  }

  private void handleControlView(boolean enableSwitchSongTypeNotSerial) {
    llyControl.setVisibility(VISIBLE);
    ivSoundSetting.setClickable(true);
    ivPause.setClickable(true);
    ivNext.setClickable(true);
    ivSwitchOrigin.setClickable(true);
    ivSwitchOrigin.setClickable(enableSwitchSongTypeNotSerial);
    if (KaraokeUIUtils.isLocalHost()) { // 房主
      if (isLocalMain()) { // 房主是主唱，所有操作都可以
      } else if (isLocalAssistant()) {
        if (getNeSongMode() == NEKaraokeSongMode.SERIAL_CHORUS) { // 房主是副唱，串行合唱 不能切原生/伴奏，不能切歌
          ivSwitchOrigin.setClickable(false);
        }
      } else { /// 听众
        ivSoundSetting.setClickable(false);
        ivSwitchOrigin.setClickable(false);
      }
    } else { /// 不是房主
      if (isLocalMain()) { // 房主是主唱，所有操作都可以
      } else if (isLocalAssistant()) { // 副唱
        if (getNeSongMode() == NEKaraokeSongMode.SERIAL_CHORUS) {
          ivSwitchOrigin.setClickable(false);
        }
      } else { // 听众
        llyControl.setVisibility(GONE);
        ivPause.setClickable(false);
        ivNext.setClickable(false);
        ivSwitchOrigin.setClickable(false);
        ivSoundSetting.setClickable(false);
      }
    }
  }

  @NonNull
  private NEKaraokeSongMode getNeSongMode() {
    NEKaraokeSongMode mode = NEKaraokeSongMode.SERIAL_CHORUS;
    if (currentSongModel == null
        || currentSongModel.getAssistantUuid() == null
        || currentSongModel.getSingMode() == null) {
      return NEKaraokeSongMode.SOLO;
    }
    if (currentSongModel.getSingMode() == 0) {
      if (currentSongModel.getChorusType() == null) {
        mode = NEKaraokeSongMode.SOLO;
      } else if (currentSongModel.getChorusType() == NEKaraokeUIConstants.REAL_TIME_CHORUS) {
        mode = NEKaraokeSongMode.REAL_TIME_CHORUS;
      }
    } else if (currentSongModel.getSingMode() == 2) {
      mode = NEKaraokeSongMode.REAL_TIME_CHORUS;
    } else if (currentSongModel.getSingMode() == 3) {
      mode = NEKaraokeSongMode.SOLO;
    }
    return mode;
  }

  /** 注册 karaoke监听 */
  private void registerKaraokeListener() {
    listener =
        new MyKaraokeListener() {
          @Override
          public void onReceiveChorusMessage(
              @NonNull NEKaraokeChorusActionType actionType, @NonNull NEKaraokeSongModel model) {
            ALog.d(TAG, "actionType========>" + actionType.name() + " model = " + model);
            if (actionType == NEKaraokeChorusActionType.INVITE) { // / 邀请消息
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              matchingMember();
            } else if (actionType == NEKaraokeChorusActionType.AGREE_INVITE) { // 同意邀请消息
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              cancelCountDownTimerForMatch();
              showLoadingSong();
              handler.postDelayed(accompanyDelayTask, DELAY_TIME);

            } else if (actionType == NEKaraokeChorusActionType.READY) { // / 准备完成可以开始了
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              handler.removeCallbacks(accompanyDelayTask);
              if (isLocalMain()) {
                NEKaraokeKit.getInstance()
                    .requestPlaySong(model.getOrderId(), model.getChorusId(), null, null);
              }
            } else if (actionType == NEKaraokeChorusActionType.START_SONG) {
              handleStartSong(model);
            } else if (actionType
                == NEKaraokeChorusActionType.CANCEL_INVITE) { // 超时取消邀请，展示waiting for sole提醒
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              switchKTVState(
                  KTV_STATE_WAITING_FOR_SOLE,
                  new SongModel(
                      currentSongModel.getSongName(),
                      currentSongModel.getIcon(),
                      currentSongModel.getUserName(),
                      currentSongModel.getAssistantIcon(),
                      currentSongModel.getAssistantName()));
              cancelCountDownTimerForMatch();
              if (isLocalMain()) { /// 主唱展示 是否进入独唱提示框 倒计时
                showSoloTipDialog(currentSongModel.getOrderId());
              } else {
                startCountDownTimerForAudienceWaitSolo();
              }
            } else if (actionType == NEKaraokeChorusActionType.PAUSE_SONG) { // / 歌曲暂停
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              playButtonChangeUI(true);
            } else if (actionType == NEKaraokeChorusActionType.RESUME_SONG) { // / 恢复播放
              currentSongModel = model;
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(currentSongModel);
              }
              playButtonChangeUI(false);
            } else if (actionType == NEKaraokeChorusActionType.ABANDON) {
              ALog.i(TAG, "onReceiveChorusMessage  NEKaraokeChorusActionType.ABANDON");
              flSolo.destroyTimer();
              currentSongModel = null;
              cancelCountDownTimerForMatch();
              cancelCountDownTimerForPlay();
              if (songModelChangeListener != null) {
                songModelChangeListener.onSongModelChange(null);
              }
              switchKTVState(KTV_STATE_INIT);
            } else if (actionType == NEKaraokeChorusActionType.END_SONG) { // / 结束
              ALog.i(TAG, "onReceiveChorusMessage  NEKaraokeChorusActionType.END_SONG");
              handleEndSong(model);
              if (needShowScore()) {
                flSolo.pause();
              }
              flSolo.destroyTimer();
            } else if (actionType == NEKaraokeChorusActionType.NEXT) { // / 下一首歌曲
              ALog.i(TAG, "onReceiveChorusMessage  NEKaraokeChorusActionType.NEXT");
              handleNextSong(model);
            }
          }

          @Override
          public void onRecordingAudioFrame(@NonNull NEKaraokeAudioFrame frame) {
            if (needShowScore()) {
              // 独唱且是主唱才需要打分
              NEPitchAudioData audioData =
                  new NEPitchAudioData(
                      frame.data,
                      frame.format.samplesPerChannel,
                      (int) (currentPosition - 10),
                      true,
                      frame.format.sampleRate,
                      frame.format.channels);
              flSolo.pushAudioData(audioData);
            }
          }

          @Override
          public void onSongPlayingCompleted() {
            ALog.d(TAG, "PlayState,onComplete");
            currentPosition = 0;
            if (isLocalMain()) {
              if (getNeSongMode() == NEKaraokeSongMode.SOLO
                  && currentSongModel != null
                  && flSolo.needShowFinalScore()) {
                showGradeAndFinishSong(currentSongModel);
              } else {
                NEKaraokeKit.getInstance().requestStopPlayingSong(null);
              }
            }
          }

          @Override
          public void onSongPlayingPosition(long position) {
            currentPosition = position;
            if (!alreadyPlayLoadAnimate && position > (preludeTime - preludeBufferTime)) {
              alreadyPlayLoadAnimate = true;
              ALog.d(TAG, "onSongPlayPosition startLoading,position:" + position);
              if (isLocalAssistant() || isLocalMain()) { // 主唱和副唱展示loading动画
                startLoading();
              }
            }
            if (position - tempTime > 0) {
              long songTime = 0;
              if (currentSongModel != null && currentSongModel.getSongTime() != null) {
                songTime = currentSongModel.getSongTime();
              }
              tvSongTime.setText(
                  getContext()
                      .getString(
                          R.string.song_current_time,
                          DateFormatUtils.long2StrHS(position),
                          DateFormatUtils.long2StrHS(songTime)));
              updateLyric(position);
              tempTime = position;
            }
          }
        };
    NEKaraokeKit.getInstance().addKaraokeListener(listener);
  }

  private void handleStartSong(NEKaraokeSongModel model) {
    tempTime = 0;
    currentSongModel = model;
    if (songModelChangeListener != null) {
      songModelChangeListener.onSongModelChange(currentSongModel);
    }
    cancelCountDownTimerForMatch();
    cancelCountDownTimerForPlay();
    onStartSong(model);
    if (needShowScore()) {
      flSolo.start();
    }

    if (songModelChangeListener != null) {
      songModelChangeListener.onStartSong(model);
    }
  }

  private void handleNextSong(NEKaraokeSongModel model) {
    if (songModelChangeListener != null) {
      songModelChangeListener.onNextSong(model);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    NEKaraokeKit.getInstance().removeKaraokeListener(listener);
    if (flSolo != null) {
      flSolo.destroyTimer();
    }
    NEAudioEffectManager.INSTANCE.resetAll();
  }

  private void handleEndSong(NEKaraokeSongModel model) {
    ALog.d(TAG, "handleEndSong  model:" + model);
    tempTime = 0;
    ALog.d(TAG, "handleEndSong  tempTime:" + tempTime);
    if (songModelChangeListener != null) {
      songModelChangeListener.onSongModelChange(null);
    }
    switchKTVState(KTV_STATE_INIT);
    NEAudioEffectManager.INSTANCE.adjustRecordingSignalVolume(
        NEAudioEffectManager.DEFAULT_RECORDING_SIGNAL_VOLUME);
    NEAudioEffectManager.INSTANCE.adjustPlaybackSignalVolume(
        NEAudioEffectManager.DEFAULT_OTHER_SIGNAL_VOLUME);
    currentSongModel = null;
  }

  /** 切换播放/暂停 按钮UI状态 */
  @SuppressLint("UseCompatLoadingForDrawables")
  private void playButtonChangeUI(boolean isPaused) {
    paused = isPaused;
    if (isPaused) {
      if (needShowScore()) {
        flSolo.pause();
      }
      ivPause.setText(getResources().getString(R.string.karaoke_resume));
      Drawable drawable = getResources().getDrawable(R.drawable.resume_icon);
      int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
      drawable.setBounds(1, 1, size, size);
      ivPause.setCompoundDrawables(null, drawable, null, null);
    } else {
      if (needShowScore()) {
        flSolo.start();
      }
      ivPause.setText(getResources().getString(R.string.karaoke_pause));
      Drawable drawable = getResources().getDrawable(R.drawable.icon_music_state_switch);
      int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
      drawable.setBounds(1, 1, size, size);
      ivPause.setCompoundDrawables(null, drawable, null, null);
    }
  }

  /**
   * 歌曲状态变化
   *
   * @param isPause 是否暂停
   */
  private void changeMusicState(boolean isPause) {
    if (isPause) {
      NEKaraokeKit.getInstance()
          .requestPausePlayingSong(
              new NEKaraokeCallback<Unit>() {

                @Override
                public void onSuccess(@Nullable Unit unit) {
                  ALog.d(TAG, "changeMusicState pauseSong success,isPause true");
                  playButtonChangeUI(true);
                }

                @Override
                public void onFailure(int code, @Nullable String msg) {
                  ALog.e(
                      TAG,
                      "changeMusicState pauseSong onFailure ,isPause true code:"
                          + code
                          + ",msg:"
                          + msg);
                }
              });
    } else {
      NEKaraokeKit.getInstance()
          .requestResumePlayingSong(
              new NEKaraokeCallback<Unit>() {

                @Override
                public void onSuccess(@Nullable Unit unit) {
                  ALog.d(TAG, "changeMusicState resumeSong success,isPause false");
                  playButtonChangeUI(false);
                }

                @Override
                public void onFailure(int code, @Nullable String msg) {
                  ALog.e(
                      TAG,
                      "changeMusicState resumeSong onFailure ,isPause false code:"
                          + code
                          + ",msg:"
                          + msg);
                }
              });
    }
  }

  public void setOrder(OnClickListener listener) {
    tvOrderSong.setOnClickListener(listener);
  }

  private void showLoadingSong() {
    switchKTVState(
        KTV_STATE_WAITING_FOR_RESOURCES,
        new SongModel(
            currentSongModel.getSongName(),
            currentSongModel.getIcon(),
            currentSongModel.getUserName(),
            currentSongModel.getAssistantIcon(),
            currentSongModel.getAssistantName()));
  }

  @Override
  public void showLyricAndScore(LyricBusinessModel lyricBusinessModel) {
    if (lyricBusinessModel == null) {
      return;
    }
    flSolo.showLyricAndScore(
        lyricBusinessModel.lyricContent,
        lyricBusinessModel.midiContent,
        lyricBusinessModel.lyricType);
  }

  @Override
  public void matchingMember() {
    ALog.d(TAG, "matchingMember");
    switchKTVState(
        KTV_STATE_WAITING_FOR_CHORUS,
        new SongModel(
            currentSongModel.getSongName(),
            currentSongModel.getIcon(),
            currentSongModel.getUserName()));
    cancelCountDownTimerForMatch();
    startCountDownTimerForMatch(); // 开启匹配倒计时
    if (isLocalMain()) { /// 是主唱展示 匹配中 和 倒计时
      ivUserAvatar.loadAvatar(currentSongModel.getIcon());
      ivUserAvatar.setVisibility(VISIBLE);
      soloTextView.setText(getResources().getString(R.string.solo));
      soloTextView.setOnClickListener(
          view -> {
            if (!NetUtils.checkNetwork(getContext())) {
              return;
            }
            cancelCountDownTimerForMatch();
            NEKaraokeKit.getInstance()
                .requestPlaySong(
                    currentSongModel.getOrderId(),
                    null,
                    null,
                    new NEKaraokeCallback<Unit>() {

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ALog.e(
                            TAG,
                            "matchingMember  startSong  onFailure:code:" + code + ",msg:" + msg);
                      }

                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        ALog.i(TAG, "matchingMember startSong  success");
                      }
                    });
          });
    } else { /// 不是主唱展示 同意加入
      ivUserAvatar.loadAvatar(currentSongModel.getIcon());
      ivUserAvatar.setVisibility(VISIBLE);
      soloTextView.setText(getResources().getString(R.string.join_chrous));
      soloTextView.setOnClickListener(
          view -> {
            if (currentSongModel != null && currentSongModel.getChorusId() != null) {
              if (!NetUtils.checkNetwork(getContext())) {
                return;
              }
              if (KaraokeSeatUtils.isCurrentOnSeat()) {
                agreeAddChorus(currentSongModel.getChorusId());
              } else {
                ((KaraokeRoomActivity) getContext())
                    .showApplySeatDialog(
                        getResources().getString(R.string.order_song_apply_seat),
                        getResources().getString(R.string.apply_seat));
              }
            } else {
              ALog.d(
                  TAG,
                  "joinChorus error currentSongModel is null or currentSongModel.getChorusId() == null");
            }
          });
    }
  }

  /** 听众等待 主唱是否进入独唱倒计时 */
  private void startCountDownTimerForAudienceWaitSolo() {
    ALog.i(TAG, "startCountDownTimerForAudienceWaitSolo");
    //                int timeSec = (int) (millisUntilFinished / 1000);
    /// 匹配时间到，没有人加入合唱，那么执行独唱逻辑
    //                        startCountDownTimerForTurnToYou();
    /// 听众等在主唱开始进入独唱模式
    CountDownTimer timerForAudienceWaitSolo =
        new CountDownTimer(PLAY_TIME, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {
            //                int timeSec = (int) (millisUntilFinished / 1000);
            tvTime.post(
                () ->
                    tvTime.setText(
                        getResources()
                            .getString(
                                R.string.wait_for_solo_with_time,
                                String.valueOf(millisUntilFinished / 1000))));
          }

          @Override
          public void onFinish() {
            if (currentSongModel == null
                || currentSongModel.getAssistantUuid() == null) { // / 匹配时间到，没有人加入合唱，那么执行独唱逻辑
              if (isLocalMain()) {
                showTurnToYouDialog();
                //                        startCountDownTimerForTurnToYou();
              } else {
                startCountDownTimerForPlay();
              }
            }
          }
        };
    timerForAudienceWaitSolo.start();
  }

  /** 合唱匹配 倒计时 */
  private void startCountDownTimerForMatch() {
    ALog.i(TAG, "startCountDownTimerForMatch");
    timerForMatch =
        new CountDownTimer(MATCH_TIME, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {
            tvTime.post(
                () ->
                    tvTime.setText(
                        getResources()
                            .getString(
                                R.string.matching_with_time, millisUntilFinished / 1000 + "s")));
          }

          @Override
          public void onFinish() {
            if (currentSongModel != null
                && currentSongModel.getChorusId() != null
                && isLocalMain()) {
              NEKaraokeKit.getInstance()
                  .cancelInviteChorus(
                      currentSongModel.getChorusId(),
                      new NEKaraokeCallback<NEKaraokeSongModel>() {

                        @Override
                        public void onSuccess(@Nullable NEKaraokeSongModel karaokeSongModel) {
                          ALog.d(TAG, "startCountDownTimerForMatch  cancelInviteChorus success");
                        }

                        @Override
                        public void onFailure(int code, @Nullable String msg) {
                          ALog.e(
                              TAG,
                              "startCountDownTimerForMatch  cancelInviteChorus fail code:"
                                  + code
                                  + ",msg:"
                                  + msg);
                        }
                      });
            }
          }
        };
    timerForMatch.start();
  }

  /** 有合唱这进来，取消匹配倒计时 */
  private void cancelCountDownTimerForMatch() {
    ALog.i(TAG, "cancelCountDownTimerForMatch");
    if (timerForMatch != null) {
      timerForMatch.cancel();
    }
  }

  /** 开始播放 倒计时 */
  private void startCountDownTimerForPlay() {
    ALog.i(TAG, "startCountDownTimerForPlay");
    timerForPlay =
        new CountDownTimer(PLAY_TIME, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {
            currentTime = (int) (millisUntilFinished / 1000);
            tvTime.post(
                () -> {
                  tvTime.setText(
                      getResources()
                          .getString(
                              R.string.chorus_loading_with_time, String.valueOf(currentTime)));
                  if (turnToYouDialog != null) {
                    turnToYouDialog.updateNegativeContent(
                        getResources().getString(R.string.give_up) + "(" + currentTime + "s)");
                  }
                });
          }

          @Override
          public void onFinish() {
            if (currentSongModel != null && isLocalMain()) {
              NEKaraokeKit.getInstance()
                  .abandonSong(
                      currentSongModel.getOrderId(),
                      new NEKaraokeCallback<Unit>() {

                        @Override
                        public void onSuccess(@Nullable Unit unit) {
                          ALog.d(TAG, "startCountDownTimerForPlay success");
                        }

                        @Override
                        public void onFailure(int code, @Nullable String msg) {
                          ALog.e(
                              TAG, "startCountDownTimerForPlay fail code:" + code + ",msg:" + msg);
                        }
                      });
            }
          }
        };
    timerForPlay.start();
  }

  /** 取消播放倒计时 */
  private void cancelCountDownTimerForPlay() {
    ALog.i(TAG, "cancelCountDownTimerForPlay");
    if (timerForPlay != null) {
      timerForPlay.cancel();
    }
  }

  /** 开始"独唱" 倒计时 */
  private void startCountDownTimerForSoloTip(long orderId) {
    ALog.i(TAG, "startCountDownTimerForSoloTip");
    timerForSoloTip =
        new CountDownTimer(PLAY_TIME, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {
            if (soloTipDialog != null) {
              soloTipDialog.updateNegativeContent(
                  getResources().getString(R.string.give_up)
                      + "("
                      + Math.round(millisUntilFinished / 1000f)
                      + "s)");
            }
          }

          @Override
          public void onFinish() {
            if (soloTipDialog != null) {
              soloTipDialog.updateNegativeContent(
                  getResources().getString(R.string.give_up) + "(0s)");
              postDelayed(() -> soloTipDialog.dismiss(), 200);
            }
            NEKaraokeKit.getInstance()
                .abandonSong(
                    orderId,
                    new NEKaraokeCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        ALog.d(TAG, "startCountDownTimerForSoloTip abandon success");
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ALog.e(
                            TAG,
                            "startCountDownTimerForSoloTip abandon  fail code:"
                                + code
                                + ",msg:"
                                + msg);
                      }
                    });
          }
        };
    timerForSoloTip.start();
  }

  /** 取消"是否进入合唱"倒计时 */
  private void cancelCountDownTimerForSoloTip() {
    ALog.i(TAG, "cancelCountDownTimerForSoloTip");
    if (timerForSoloTip != null) {
      timerForSoloTip.cancel();
    }
  }

  /** 匹配失效后，主唱侧提示 */
  private void showSoloTipDialog(long orderId) {
    soloTipDialog = new CommonDialog(getContext());
    soloTipDialog
        .setTitle(getResources().getString(R.string.in_solo))
        .setCanceledOutside(false)
        .setTitle(getResources().getString(R.string.turn_to_you))
        .setContent(getResources().getString(R.string.karaoke_tip))
        .setPositiveBtnName(getResources().getString(R.string.karaoke_sure))
        .setPositiveOnClickListener(
            view -> { // 开始独唱
              cancelCountDownTimerForSoloTip();
              NEKaraokeKit.getInstance()
                  .requestPlaySong(
                      orderId,
                      null,
                      null,
                      new NEKaraokeCallback<Unit>() {
                        @Override
                        public void onSuccess(@Nullable Unit unit) {
                          ALog.d(TAG, "showSoloTipDialog  startSong success");
                        }

                        @Override
                        public void onFailure(int code, @Nullable String msg) {
                          ALog.e(
                              TAG,
                              "showSoloTipDialog  startSong onFailure: code:"
                                  + code
                                  + ",msg:"
                                  + msg);
                        }
                      });
            })
        .setNegativeBtnName(getResources().getString(R.string.give_up) + "(" + currentTime + "s)")
        .setNegativeOnClickListener(
            view -> {
              cancelCountDownTimerForSoloTip();
              NEKaraokeKit.getInstance()
                  .abandonSong(
                      orderId,
                      new NEKaraokeCallback<Unit>() {
                        @Override
                        public void onSuccess(@Nullable Unit unit) {
                          ALog.d(TAG, "showSoloTipDialog  stopSong success");
                        }

                        @Override
                        public void onFailure(int code, @Nullable String msg) {
                          ALog.e(
                              TAG,
                              "showSoloTipDialog  stopSong onFailure: code:"
                                  + code
                                  + ",msg:"
                                  + msg);
                        }
                      });
            })
        .show();
    startCountDownTimerForSoloTip(orderId);
  }

  @Override
  public void showTurnToYouDialog() {
    turnToYouDialog = new CommonDialog(getContext());
    turnToYouDialog
        .setTitle(getResources().getString(R.string.turn_to_you))
        .setCanceledOutside(false)
        .setContent(getResources().getString(R.string.karaoke_tip))
        .setPositiveBtnName(getResources().getString(R.string.karaoke_sure))
        .setPositiveOnClickListener(
            view -> {
              if (currentSongModel != null) {
                NEKaraokeKit.getInstance()
                    .requestPlaySong(
                        currentSongModel.getOrderId(),
                        currentSongModel.getChorusId(),
                        null,
                        new NEKaraokeCallback<Unit>() {

                          @Override
                          public void onSuccess(@Nullable Unit unit) {
                            ALog.d(TAG, "showTurnToYouDialog  startSong success");
                          }

                          @Override
                          public void onFailure(int code, @Nullable String msg) {
                            ALog.e(
                                TAG,
                                "showTurnToYouDialog  startSong onFailure code:"
                                    + code
                                    + ",msg:"
                                    + msg);
                          }
                        });
              }
              //                    showPlayJustNow();
            })
        .setNegativeBtnName(getResources().getString(R.string.give_up) + "(" + currentTime + "s)")
        .setNegativeOnClickListener(view -> NEKaraokeKit.getInstance().requestStopPlayingSong(null))
        .show();
  }

  boolean isLyricAlready = false;

  @Override
  public void initLyricView() {
    isLyricAlready = false;
    LyricLoader.loadLyric(
        currentSongModel,
        new NEKaraokeCallback<LyricBusinessModel>() {
          @Override
          public void onSuccess(@Nullable LyricBusinessModel lyricBusinessModel) {
            ALog.i(TAG, "initLyricView loadLyric success");
            if (lyricBusinessModel == null) {
              return;
            }
            isLyricAlready = !TextUtils.isEmpty(lyricBusinessModel.lyricContent);
            refreshSingView();
            preludeTime = lyricBusinessModel.preludeTime;
            lrcView.loadWithLyricModel(lyricBusinessModel.lyric);
            tvSongTime.setText("");
            updateLyric(0);
          }

          @Override
          public void onFailure(int code, @Nullable String msg) {
            ALog.i(TAG, "initLyricView loadLyric onFailure code:" + code);
            isLyricAlready = false;
            refreshSingView();
          }
        });
  }

  private void showLyric() {
    lrcView.setVisibility(VISIBLE);
    llyReady.setVisibility(GONE);
    tvMusicName.setVisibility(GONE);
    ivUserAvatar.setVisibility(GONE);
    tvUserNick.setVisibility(GONE);
    soloTextView.setVisibility(VISIBLE);
  }

  private void showNoLyric() {
    lrcView.setVisibility(GONE);
    llyReady.setVisibility(VISIBLE);
    tvMusicName.setVisibility(VISIBLE);
    tvMusicName.setText(getContext().getString(R.string.song_name, currentSongModel.getSongName()));

    ivUserAvatar.setVisibility(VISIBLE);
    if (currentSongModel != null) {
      ivUserAvatar.loadAvatar(currentSongModel.getIcon());
    }
    tvUserNick.setVisibility(VISIBLE);
    tvUserNick.setText(getContext().getString(R.string.karaoke_no_lyric));
    tvTime.setText("");
    soloTextView.setVisibility(GONE);
  }

  private void switchKTVState(int state) {
    switchKTVState(state, null);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  private void switchKTVState(int state, SongModel song) {
    ALog.i(TAG, "switchKTVState state = " + state + ",mode:" + getNeSongMode());
    switch (state) {
      case KTV_STATE_WAITING_FOR_CHORUS:
        llyNoOrderSong.setVisibility(GONE);
        flySing.setVisibility(VISIBLE);
        llyReady.setVisibility(VISIBLE);
        ivChorusAvatar.setVisibility(VISIBLE);
        ivChorusAvatar.setImageResource(R.drawable.icon_default_head);
        soloTextView.setVisibility(VISIBLE);
        flSolo.setVisibility(GONE);
        tvUserNick.setVisibility(GONE);
        llSongTime.setVisibility(GONE);
        tvSongTime.setVisibility(GONE);
        tvMusicName.setVisibility(VISIBLE);
        if (song != null) {
          tvMusicName.setText(getContext().getString(R.string.song_name, song.getSongName()));
        }
        break;
      case KTV_STATE_WAITING_FOR_RESOURCES:
        llyNoOrderSong.setVisibility(GONE);
        flySing.setVisibility(VISIBLE);
        llyReady.setVisibility(VISIBLE);
        flSolo.setVisibility(GONE);
        ivChorusAvatar.setVisibility(VISIBLE);
        tvTime.setText(getResources().getString(R.string.chorus_loading));
        tvMusicName.setVisibility(VISIBLE);
        if (song != null) {
          ivUserAvatar.loadAvatar(song.getIcon());
          ivChorusAvatar.loadAvatar(song.getAssistantIcon());
          tvMusicName.setText(getContext().getString(R.string.song_name, song.getSongName()));
          tvUserNick.setVisibility(VISIBLE);
          soloTextView.setVisibility(GONE);
          tvUserNick.setText(
              getResources()
                  .getString(
                      R.string.prepare_chorus, song.getUserName(), song.getAssistantUserName()));
        }
        break;
      case KTV_STATE_WAITING_FOR_SOLE:
        llyNoOrderSong.setVisibility(GONE);
        flySing.setVisibility(VISIBLE);
        llyReady.setVisibility(VISIBLE);
        flSolo.setVisibility(GONE);
        ivChorusAvatar.setVisibility(GONE);
        tvTime.setText(getResources().getString(R.string.wait_for_solo));
        soloTextView.setVisibility(GONE);
        tvMusicName.setVisibility(VISIBLE);
        ivUserAvatar.setVisibility(VISIBLE);
        tvUserNick.setVisibility(VISIBLE);
        if (song != null) {
          tvMusicName.setText(getContext().getString(R.string.song_name, song.getSongName()));
          ivUserAvatar.loadAvatar(song.getIcon());
          tvUserNick.setText(getResources().getString(R.string.prepare_single, song.getUserName()));
        }

        break;
      case KTV_STATE_SINGING:
        refreshSingView();
        break;
      case KTV_STATE_GRADE:
        lrcView.setVisibility(GONE);
        flSolo.setVisibility(VISIBLE);
        llSongTime.setVisibility(GONE);
        break;
      default:
        llyReady.setVisibility(GONE);
        rlySing.setVisibility(GONE);
        flySing.setVisibility(GONE);
        lrcView.setVisibility(GONE);
        flSolo.setVisibility(GONE);
        llyControl.setVisibility(GONE);
        llyNoOrderSong.setVisibility(VISIBLE);
        currentOrigin = false;
        ivSwitchOrigin.setText(getResources().getString(R.string.karaoke_origin_close));
        Drawable drawable = getResources().getDrawable(R.drawable.icon_switch_origin_colse);
        int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
        drawable.setBounds(1, 1, size, size);
        ivSwitchOrigin.setCompoundDrawables(null, drawable, null, null);
        llSongTime.setVisibility(GONE);
        tvSongTime.setText("");
        playButtonChangeUI(false);
        ALog.d(TAG, "======> changeSwitchOriginUI: default");
        break;
    }
  }

  private void refreshSingView() {
    llyReady.setVisibility(GONE);
    llyNoOrderSong.setVisibility(GONE);
    rlySing.setVisibility(VISIBLE);
    flySing.setVisibility(VISIBLE);
    flSolo.setVisibility(GONE);
    ivUserAvatar.setVisibility(GONE);
    tvUserNick.setVisibility(GONE);
    ivChorusAvatar.setVisibility(GONE);
    lrcView.setVisibility(VISIBLE);
    llSongTime.setVisibility(VISIBLE);
    tvSongTime.setVisibility(VISIBLE);
    tvMusicName.setVisibility(GONE);
    // 只有主唱并且是solo模式，才展示打分
    if (needShowScore()) {
      flSolo.setVisibility(VISIBLE);
      lrcView.setVisibility(GONE);
      if (!isLyricAlready) {
        showNoLyric();
      }
    } else {
      flSolo.setVisibility(GONE);
      if (!isLyricAlready) {
        showNoLyric();
      } else {
        showLyric();
      }
    }
    tvTime.setText("");
  }

  @Override
  public void updateLyric(long timestamp) {
    lrcView.update(timestamp);
    flSolo.update(timestamp);
  }

  @Override
  public void clickEffectView() {
    FragmentActivity activity = (FragmentActivity) getContext();
    Bundle bundle = new Bundle();
    bundle.putInt(
        NEAudioEffectUIConstants.INTENT_CURRENT_EFFECT_ID,
        NEKaraokeKit.getInstance().currentSongIdForAudioEffect());
    BottomSheetDialogFragment fragment = new ToneDialogFragment();
    fragment.setArguments(bundle);
    fragment.show(activity.getSupportFragmentManager(), TAG);
  }

  @Override
  @SuppressLint("UseCompatLoadingForDrawables")
  public void clickSwitchToOriginalVolume() {
    ALog.d(TAG, "clickSwitchToOriginalVolume");
    if (!NetUtils.checkNetwork(getContext())) {
      return;
    }
    currentOrigin = !currentOrigin;
    NEKaraokeKit.getInstance().switchAccompaniment(!currentOrigin);
    changeSwitchOriginUI(currentOrigin, true);
  }

  private void changeSwitchOriginUI(boolean switchOrigin, boolean enableSwitchSongTypeNotSerial) {
    ALog.d(TAG, "======> changeSwitchOriginUI:" + switchOrigin);
    if (switchOrigin) {
      ivSwitchOrigin.setText(getResources().getString(R.string.karaoke_origin_open));
      Drawable drawable =
          ResourcesCompat.getDrawable(
              getResources(),
              enableSwitchSongTypeNotSerial
                  ? R.drawable.icon_switch_origin_open
                  : R.drawable.icon_switch_origin_open_gray,
              null);
      int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
      if (drawable != null) {
        drawable.setBounds(1, 1, size, size);
        ivSwitchOrigin.setCompoundDrawables(null, drawable, null, null);
      }
    } else {
      ivSwitchOrigin.setText(getResources().getString(R.string.karaoke_origin_close));
      Drawable drawable =
          ResourcesCompat.getDrawable(getResources(), R.drawable.icon_switch_origin_colse, null);
      int size = getResources().getDimensionPixelSize(R.dimen.dimen_28_dp);
      if (drawable != null) {
        drawable.setBounds(1, 1, size, size);
        ivSwitchOrigin.setCompoundDrawables(null, drawable, null, null);
      }
    }
  }

  @Override
  public void clickNextSong() {
    ALog.d(TAG, "clickNextSong");
    if (!NetUtils.checkNetwork(getContext())) {
      ALog.e(TAG, "clickNextSong but no network");
      return;
    }

    if (currentSongModel == null) {
      ALog.e(TAG, "clickNextSong but currentSongModel == null");
      return;
    }

    if (isLocalHost() || isLocalMain() || isLocalAssistant()) {
      NEKaraokeKit.getInstance().nextSong(currentSongModel.getOrderId(), null);
    } else {
      ALog.e(TAG, "clickNextSong but current not main or assistant");
    }
  }

  public void switchSongByNetwork() {
    ALog.d(TAG, "onNetworkConnected");
    if (!NetUtils.checkNetwork(getContext())) {
      ALog.e(TAG, "onNetworkConnected but no network");
      return;
    }

    if (currentSongModel == null) {
      ALog.e(TAG, "onNetworkConnected but currentSongModel == null");
      return;
    }

    if (isLocalMain() || isLocalAssistant()) {
      NEKaraokeKit.getInstance().nextSong(currentSongModel.getOrderId(), null);
    } else {
      ALog.e(TAG, "onNetworkConnected but current not main or assistant");
    }
  }

  private void showGradeAndFinishSong(NEKaraokeSongModel currentSongModel) {
    // 独唱展示最终打分
    switchKTVState(KTV_STATE_GRADE);
    NEPitchSongScore instance = NEPitchSongScore.getInstance();
    instance.getFinalScore(
        new NECopyrightedMedia.Callback<NEPitchRecordSingInfo>() {
          @Override
          public void success(@Nullable NEPitchRecordSingInfo info) {
            if (roomModel != null && info != null) {
              NEKTVPlayResultModel userData =
                  new NEKTVPlayResultModel(
                      currentSongModel.getSongName(), roomModel.getNick(), roomModel.getAvatar());
              flSolo.showFinalScore(
                  userData,
                  info,
                  () ->
                      postDelayed(
                          () -> {
                            flSolo.hideScoreView();
                            NEKaraokeKit.getInstance().requestStopPlayingSong(null);
                          },
                          3000));
            }
          }

          @Override
          public void error(int code, @Nullable String msg) {
            ALog.e(TAG, "showGradeAndFinishSong onError errorCode:" + code);
          }
        });
  }

  @Override
  public void playOrPause() {
    ALog.d(TAG, "playOrPause，paused：" + paused);
    if (!NetUtils.checkNetwork(getContext())) {
      return;
    }
    changeMusicState(!paused);
  }

  public void startSolo(NEKaraokeSongModel song) {
    ALog.d(TAG, "startSolo，song:" + song);
    switchKTVState(
        KTV_STATE_WAITING_FOR_SOLE,
        new SongModel(song.getSongName(), song.getIcon(), song.getUserName()));
    showSoloTipDialog(song.getOrderId());
  }

  @Override
  public void inviteChorus(long orderId) {
    ALog.i(TAG, "inviteChorus orderId = " + orderId);
    NEKaraokeKit.getInstance()
        .inviteChorus(
            orderId,
            new NEKaraokeCallback<NEKaraokeSongModel>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeSongModel karaokeSongModel) {
                ALog.d(TAG, "inviteChorus:onSuccess,neKaraokeSongModel:" + karaokeSongModel);
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.d(TAG, "inviteChorus fail code:" + code + ",msg:" + msg);
              }
            });
  }

  private void loadRes(String songId, Integer channel, NEKaraokeCallback<Void> callback) {
    boolean isExit = NEKaraokeKit.getInstance().isSongPreloaded(songId, channel);
    ALog.d(TAG, "loadRes,songId:" + songId + ",isExit:" + isExit);
    if (!isExit) {
      ALog.d(TAG, "loadRes 2------>" + songId);
      NEKaraokeKit.getInstance()
          .preloadSong(
              songId,
              channel,
              new NEKaraokeCopyrightedMediaListener() {

                @Override
                public void onPreloadStart(String songId, int channel) {
                  ALog.i(TAG, "onPreloadStart songId = " + songId);
                }

                @Override
                public void onPreloadProgress(String songId, int channel, float progress) {
                  ALog.i(TAG, "onPreloadProgress songId = " + songId + " progress = " + progress);
                }

                @Override
                public void onPreloadComplete(
                    String songId, int channel, int errorCode, String msg) {
                  ALog.i(
                      TAG,
                      "onPreloadComplete songId = "
                          + songId
                          + ", errorCode = "
                          + errorCode
                          + ", msg = "
                          + msg);
                  if (errorCode == 0) {
                    callback.onSuccess(null);
                  } else {
                    callback.onFailure(errorCode, msg);
                  }
                }
              });
    } else {
      callback.onSuccess(null);
    }
  }

  @Override
  public void agreeAddChorus(String chorusId) {
    ALog.d(TAG, "agreeAddChorus，chorusId：" + chorusId);
    NEKaraokeKit.getInstance()
        .joinChorus(
            chorusId,
            new NEKaraokeCallback<NEKaraokeSongModel>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeSongModel karaokeSongModel) {
                ALog.d(TAG, "joinChorus success");
                if (karaokeSongModel == null) {
                  ALog.d(TAG, "joinChorus success but neKaraokeSongModel == null");
                  return;
                }
                ALog.d(TAG, "agreeAddChorus，neKaraokeSongModel：" + karaokeSongModel);
                loadRes(
                    karaokeSongModel.getSongId(),
                    karaokeSongModel.getChannel(),
                    new NEKaraokeCallback<Void>() {

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ALog.e(TAG, "agreeAddChorus loadRes，code:" + code + ",msg:" + msg);
                      }

                      @Override
                      public void onSuccess(@Nullable Void unused) {
                        if (currentSongModel.getChorusId() != null) {
                          NEKaraokeKit.getInstance()
                              .chorusReady(
                                  currentSongModel.getChorusId(),
                                  new NEKaraokeCallback<NEKaraokeSongModel>() {

                                    @Override
                                    public void onSuccess(
                                        @Nullable NEKaraokeSongModel karaokeSongModel) {
                                      ALog.d(
                                          TAG,
                                          "agreeAddChorus loadRes onSuccess ，neKaraokeSongModel:"
                                              + karaokeSongModel);
                                    }

                                    @Override
                                    public void onFailure(int code, @Nullable String msg) {
                                      ALog.e(
                                          TAG,
                                          "agreeAddChorus loadRes onFailure ，code:"
                                              + code
                                              + ",msg:"
                                              + msg);
                                    }
                                  });
                        } else {
                          ALog.e(TAG, "loadRes success but chorusId == null");
                        }
                      }
                    });
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.d(TAG, "joinChorus onFailure code = " + code + ", msg = " + msg);
                if (msg != null) {
                  ToastUtils.INSTANCE.showShortToast(getContext(), msg);
                }
              }
            });
  }

  private boolean isLocalHost() {
    return KaraokeUIUtils.isLocalHost();
  }

  @Override
  public boolean isLocalMain() { // 主唱
    return KaraokeUIUtils.isLocalMain(currentSongModel);
  }

  @Override
  public boolean isLocalAssistant() { // 副唱
    return KaraokeUIUtils.isLocalAssistant(currentSongModel);
  }

  public void setSongModelChangeListener(OnSongModelChangeListener songModelChangeListener) {
    this.songModelChangeListener = songModelChangeListener;
  }

  // 只有主唱并且是solo模式，才展示打分
  private boolean needShowScore() {
    return getNeSongMode() == NEKaraokeSongMode.SOLO && isLocalMain();
  }

  public interface OnSongModelChangeListener {

    void onSongModelChange(NEKaraokeSongModel songModel);

    void onNextSong(NEKaraokeSongModel songModel);

    void onStartSong(NEKaraokeSongModel songModel);
  }

  static class SongModel {

    String songName;

    String icon;

    String userName;

    String assistantIcon;

    String assistantUserName;

    public SongModel(String songName, String icon, String userName) {
      this(songName, icon, userName, null, null);
    }

    public SongModel(
        String songName,
        String icon,
        String userName,
        String assistantIcon,
        String assistantUserName) {
      this.songName = songName;
      this.icon = icon;
      this.userName = userName;
      this.assistantIcon = assistantIcon;
      this.assistantUserName = assistantUserName;
    }

    public String getSongName() {
      return songName;
    }

    public String getIcon() {
      return icon;
    }

    public String getUserName() {
      return userName;
    }

    public String getAssistantIcon() {
      return assistantIcon;
    }

    public String getAssistantUserName() {
      return assistantUserName;
    }
  }
}
