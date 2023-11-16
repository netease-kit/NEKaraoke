// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.SongScene;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.InputUtils;
import com.netease.yunxin.kit.entertainment.common.utils.NetUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ViewUtils;
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeOptions;
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeParams;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaEventHandler;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCopyrightedMediaListener;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeRole;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeBatchGiftModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.karaokekit.ui.databinding.ActivityKaraokeRoomBinding;
import com.netease.yunxin.kit.karaokekit.ui.dialog.ArrangeMicroDialog;
import com.netease.yunxin.kit.karaokekit.ui.dialog.AudienceArrangeMicroDialog;
import com.netease.yunxin.kit.karaokekit.ui.dialog.CommonDialog;
import com.netease.yunxin.kit.karaokekit.ui.dialog.GiftDialog;
import com.netease.yunxin.kit.karaokekit.ui.dialog.OrderSongDialog;
import com.netease.yunxin.kit.karaokekit.ui.gift.GiftCache;
import com.netease.yunxin.kit.karaokekit.ui.gift.GiftRender;
import com.netease.yunxin.kit.karaokekit.ui.gift.ui.GifAnimationView;
import com.netease.yunxin.kit.karaokekit.ui.listener.NEKaraokeCallbackWrapper;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeSeatUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUIUtils;
import com.netease.yunxin.kit.karaokekit.ui.view.SingingControlView;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.KaraokeRoomViewModel;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;

public class KaraokeRoomActivity extends BaseActivity
    implements NEKaraokeCopyrightedMediaEventHandler {

  protected static final String TAG = "KaraokeRoomActivity";
  private static final String RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
  public static final String TAG_REPORT_PAGE_KARAOKE_ROOM_DETAIL = "page_ktv_chatroom";
  protected static final String ARRANGE_MICRO_DIALOG_TAG = "arrangeMicroDialog";
  protected static final String AUDIENCE_ARRANGE_MICRO_DIALOG_TAG = "audienceArrangeMicroDialog";
  protected ActivityKaraokeRoomBinding binding;
  protected RoomModel roomInfo;
  protected KaraokeRoomViewModel karaokeRoomViewModel;
  protected ArrangeMicroDialog arrangeMicroDialog = null;
  protected AudienceArrangeMicroDialog audienceArrangeMicroDialog = null;
  protected final FragmentManager fm = getSupportFragmentManager();
  private List<ApplySeatModel> applySeatItems;
  protected int currentSeatState = KaraokeRoomViewModel.CURRENT_SEAT_STATE_IDLE;
  private OrderSongViewModel orderSongViewModel;
  private GiftDialog giftDialog;
  private final GiftRender giftRender = new GiftRender();

  private final ActivityResultLauncher<String> requestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          isGranted -> {
            if (isGranted) {
              joinRoom(
                  roomInfo.getRoomUuid(),
                  roomInfo.getNick(),
                  roomInfo.getLiveRecordId(),
                  roomInfo.getRole());
            } else {
              ToastX.showShortToast(R.string.need_permission_audio);
              finish();
            }
          });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityKaraokeRoomBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    paddingStatusBarHeight(binding.getRoot());
    initIntent();
    karaokeRoomViewModel = new ViewModelProvider(this).get(KaraokeRoomViewModel.class);
    orderSongViewModel = new ViewModelProvider(this).get(OrderSongViewModel.class);
    orderSongViewModel
        .getPerformOrderSongEvent()
        .observe(
            this,
            orderSong -> {
              if (KaraokeSeatUtils.isCurrentOnSeat()) {
                orderSong(orderSong);
              } else {
                showApplySeatDialog(
                    getResources().getString(R.string.order_song_apply_seat),
                    getResources().getString(R.string.apply_seat));
              }
            });
    orderSongViewModel
        .getOrderSongListChangeEvent()
        .observe(
            this,
            orderSongs -> {
              if (!orderSongs.isEmpty()) {
                binding.tvMusicNum.setVisibility(View.VISIBLE);
                binding.tvMusicNum.setText(String.valueOf(orderSongs.size()));
              } else {
                binding.tvMusicNum.setVisibility(View.GONE);
              }
            });
    initView();
    NEKaraokeKit.getInstance().setCopyrightedMediaEventHandler(this);
    requestPermissionsIfNeeded();
    NEKaraokeUI.getInstance().notifyEnterRoom();
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected ViewUtils.ModeType getStatusBarTextModeType() {
    return ViewUtils.ModeType.NIGHT;
  }

  private void initView() {
    binding.tvKaraokeRoomName.setText(roomInfo.getRoomName());
    binding.tvChatRoomMemberCount.setText(getString(R.string.karaoke_online_member_count, 0));
    if (KaraokeUIUtils.isHostRole(roomInfo.getRole())) {
      binding.ivGift.setVisibility(View.GONE);
    } else {
      binding.ivGift.setVisibility(View.VISIBLE);
      binding.ivArrangeMicro.setImageResource(R.drawable.on_micro);
      binding.ivArrangeMicro.setBackgroundResource(R.drawable.red_cycle_bg);
    }
  }

  private void initIntent() {
    roomInfo = (RoomModel) getIntent().getSerializableExtra(RoomConstants.INTENT_ROOM_MODEL);
  }

  /** 权限检查 */
  private void requestPermissionsIfNeeded() {

    if (PermissionUtils.hasPermissions(this, RECORD_AUDIO_PERMISSION)) {
      joinRoom(
          roomInfo.getRoomUuid(),
          roomInfo.getNick(),
          roomInfo.getLiveRecordId(),
          roomInfo.getRole());
    } else {
      requestPermissionLauncher.launch(RECORD_AUDIO_PERMISSION);
    }
  }

  protected void joinRoom(String roomUuid, String nick, long liveRecordId, String role) {
    NEJoinKaraokeParams params =
        new NEJoinKaraokeParams(
            roomUuid, nick, NEKaraokeRole.Companion.fromValue(role), liveRecordId, null);
    NEJoinKaraokeOptions options = new NEJoinKaraokeOptions();
    NEKaraokeKit.getInstance()
        .joinRoom(
            params,
            options,
            new NEKaraokeCallbackWrapper<NEKaraokeRoomInfo>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeRoomInfo roomInfo) {
                ALog.i(TAG, "joinRoom success");
                initViewAfterJoinRoom();
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.e(TAG, "joinRoom failed code = " + code + " msg = " + msg);
                ToastX.showShortToast(R.string.karaoke_room_already_closed);
                KaraokeRoomActivity.this.finish();
              }
            });
  }

  protected void initViewAfterJoinRoom() {
    NECopyrightedMedia.getInstance().setSongScene(SongScene.TYPE_KTV);
    binding.singControlView.init(roomInfo);
    initListener();
    initDataObserve();
    initGiftAnimation();
    karaokeRoomViewModel.initDataOnJoinRoom();
    orderSongViewModel.refreshOrderSongs();
    binding.ivArrangeMicro.setImageResource(
        KaraokeUIUtils.isLocalHost() ? R.drawable.arrange_micro : R.drawable.on_micro);
    binding.ivArrangeMicro.setBackgroundResource(
        KaraokeUIUtils.isLocalHost() ? R.drawable.ktv_dark_cycle_bg : R.drawable.red_cycle_bg);
    if (KaraokeUIUtils.isLocalHost()) {
      applyOnSeat(null);
    } else {
      karaokeRoomViewModel.updateSeat();
    }
    refreshAudioSwitchButton();
  }

  protected void initGiftAnimation() {
    GifAnimationView gifAnimationView = new GifAnimationView(KaraokeRoomActivity.this);
    int size = ScreenUtil.getDisplayWidth();
    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    layoutParams.width = size;
    layoutParams.height = size;
    layoutParams.gravity = Gravity.BOTTOM;
    layoutParams.bottomMargin = ScreenUtil.dip2px(166f);
    binding.getRoot().addView(gifAnimationView, layoutParams);
    gifAnimationView.bringToFront();
    giftRender.init(gifAnimationView);
  }

  protected void refreshAudioSwitchButton() {
    if (KaraokeSeatUtils.isCurrentOnSeat()) {
      binding.ivLocalAudioSwitch.setVisibility(View.VISIBLE);
    } else {
      binding.ivLocalAudioSwitch.setVisibility(View.GONE);
      binding.ivLocalAudioSwitch.setSelected(false);
    }

    switch (currentSeatState) {
      case KaraokeRoomViewModel.CURRENT_SEAT_STATE_APPLYING:
        if (!KaraokeUIUtils.isLocalHost()) {
          binding.ivArrangeMicro.setImageResource(R.drawable.arrange_micro);
          binding.ivArrangeMicro.setBackgroundResource(R.drawable.ktv_dark_cycle_bg);
          binding.tvArrangeMicroNum.setVisibility(View.VISIBLE);
        }
        break;
      case KaraokeRoomViewModel.CURRENT_SEAT_STATE_ON_SEAT:
        if (!KaraokeUIUtils.isLocalHost()) {
          binding.ivArrangeMicro.setImageResource(R.drawable.down_mircro);
          binding.ivArrangeMicro.setBackgroundResource(R.drawable.ktv_dark_cycle_bg);
          binding.tvArrangeMicroNum.setVisibility(View.GONE);
        }

        break;
      default:
        if (!KaraokeUIUtils.isLocalHost()) {
          binding.ivArrangeMicro.setImageResource(R.drawable.on_micro);
          binding.ivArrangeMicro.setBackgroundResource(R.drawable.red_cycle_bg);
          binding.tvArrangeMicroNum.setVisibility(View.GONE);
        }
    }
  }

  @SuppressLint("MissingPermission")
  protected void initListener() {
    binding.ivLeaveRoom.setOnClickListener(v -> onClickLeaveBtn());
    binding.tvRoomMsgInput.setOnClickListener(
        view -> {
          InputUtils.showSoftInput(KaraokeRoomActivity.this, binding.etRoomMsgInput);
          binding.etRoomMsgInput.bringToFront();
        });
    binding.etRoomMsgInput.setOnEditorActionListener(
        (textView, i, keyEvent) -> {
          if (textView == binding.etRoomMsgInput) {
            if (!NetUtils.checkNetwork(KaraokeRoomActivity.this)) {
              return true;
            }
            String input = binding.etRoomMsgInput.getText().toString();
            InputUtils.hideSoftInput(KaraokeRoomActivity.this, binding.etRoomMsgInput);
            sendTextMsg(input);
            return true;
          }
          return false;
        });
    binding.ivLocalAudioSwitch.setOnClickListener(
        v -> {
          if (!NetUtils.isConnected()) {
            ToastX.showShortToast(R.string.karaoke_network_error);
            return;
          }
          if (binding.ivLocalAudioSwitch.isSelected()) {
            NEKaraokeKit.getInstance()
                .unmuteMyAudio(
                    new NEKaraokeCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        binding.ivLocalAudioSwitch.setSelected(false);
                        ToastX.showShortToast(R.string.karaoke_micro_phone_is_open);
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {}
                    });
          } else {
            NEKaraokeKit.getInstance()
                .muteMyAudio(
                    new NEKaraokeCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        binding.ivLocalAudioSwitch.setSelected(true);
                        ToastX.showShortToast(R.string.karaoke_micro_phone_is_close);
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {}
                    });
          }
        });
    binding.ivMusic.setOnClickListener(v -> showOrderDialog());
    binding.singControlView.setOrder(v -> showOrderDialog());
    InputUtils.registerSoftInputListener(
        KaraokeRoomActivity.this,
        new InputUtils.InputParamHelper() {

          @NonNull
          @Override
          public EditText getInputView() {
            return binding.etRoomMsgInput;
          }

          @Override
          public int getHeight() {
            return binding.clyAnchorInfo.getHeight();
          }
        });
    binding.seatView.setItemClickListener(
        (model, position) -> {
          if (KaraokeUIUtils.isLocalHost()) {
            if (model != null
                && model.getUser() != null
                && !KaraokeUIUtils.isLocalAccount(model.getAccount())) {
              showKickOutSeatDialog(model.getUser().getAccount());
            }
          } else {
            if (model != null) {
              if (model.getUser() != null) {
                if (KaraokeSeatUtils.isOnSeat(model.getUser().getAccount())
                    && KaraokeUIUtils.isLocalAccount(model.getAccount())) {
                  showDownSeatDialog();
                }
              } else {
                showApplySeatDialog(
                    getString(R.string.karaoke_apply_seate_to_host),
                    getString(R.string.karaoke_sure));
              }
            }
          }
        });
    binding.rlArrangeMicro.setOnClickListener(
        v -> {
          if (KaraokeUIUtils.isLocalHost()) {
            showArrangeMicroDialog();
          } else {
            if (KaraokeSeatUtils.isCurrentOnSeat()) {
              showDownSeatDialog();
            } else {
              if (currentSeatState == KaraokeRoomViewModel.CURRENT_SEAT_STATE_APPLYING) {
                showAudienceArrangeMicroDialog();
              } else {
                showApplySeatDialog(
                    getString(R.string.karaoke_apply_seate_to_host),
                    getString(R.string.karaoke_sure));
              }
            }
          }
        });
    binding.ivGift.setOnClickListener(
        v -> {
          if (!NetUtils.checkNetwork(KaraokeRoomActivity.this)) {
            return;
          }

          if (giftDialog == null) {
            giftDialog = new GiftDialog(KaraokeRoomActivity.this);
          }
          List<String> sendUserUuids = new ArrayList<>();
          sendUserUuids.add(roomInfo.getAnchorUserUuid());
          giftDialog.show(
              giftId ->
                  NEKaraokeKit.getInstance()
                      .sendBatchGift(
                          giftId,
                          1,
                          sendUserUuids,
                          new NEKaraokeCallbackWrapper<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {}

                            @Override
                            public void onError(int code, @Nullable String msg) {
                              ToastX.showShortToast(R.string.karaoke_reward_failed);
                            }
                          }));
        });
    binding.singControlView.setSongModelChangeListener(
        new SingingControlView.OnSongModelChangeListener() {

          @Override
          public void onSongModelChange(NEKaraokeSongModel songModel) {
            ALog.i(TAG, "onSongModelChange songModel = " + songModel);
            binding.seatView.updateSongModel(songModel);
          }

          @Override
          public void onNextSong(NEKaraokeSongModel songModel) {
            ALog.i(TAG, "onSongModelChange songModel = " + songModel);
            nextSong(songModel);
          }

          @Override
          public void onStartSong(NEKaraokeSongModel songModel) {
            if (binding.singControlView.isLocalMain()
                || binding.singControlView.isLocalAssistant()) {
              unmuteMyAudio();
            }
          }
        });
  }

  private void showOrderDialog() {
    if (!ClickUtils.isSlightlyFastClick()) {
      if (!NetUtils.checkNetwork(KaraokeRoomActivity.this)) {
        return;
      }

      ReportUtils.report(
          KaraokeRoomActivity.this, TAG_REPORT_PAGE_KARAOKE_ROOM_DETAIL, "ktv_order_song");

      OrderSongDialog dialog = new OrderSongDialog();
      dialog.show(getSupportFragmentManager(), TAG);
    }
  }

  public void orderSong(KaraokeOrderSongModel copyrightSong) {
    orderSongViewModel.getPerformDownloadSongEvent().postValue(copyrightSong);
  }

  protected void showKickOutSeatDialog(String userUuid) {
    CommonDialog dialog = new CommonDialog(KaraokeRoomActivity.this);
    dialog.setTitle(getString(R.string.karaoke_kickout_seat_confirm));
    dialog.setPositiveOnClickListener(v1 -> NEKaraokeKit.getInstance().kickSeat(userUuid, null));
    dialog.show();
  }

  public void showApplySeatDialog(String title, String positiveStr) {
    if (!NetUtils.checkNetwork(KaraokeRoomActivity.this)) {
      return;
    }

    if (KaraokeSeatUtils.isCurrentOnSeat()) {
      return;
    }
    KaraokeUIUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        title,
        null,
        positiveStr,
        v1 ->
            applyOnSeat(
                new NEKaraokeCallbackWrapper<Unit>() {

                  @Override
                  public void onSuccess(@Nullable Unit unit) {
                    karaokeRoomViewModel.getSeatRequestList();
                  }

                  @Override
                  public void onError(int code, @Nullable String msg) {
                    if (msg != null) {
                      ToastX.showShortToast(msg);
                    }
                  }
                }));
  }

  protected void showCloseRoomDialog() {
    KaraokeUIUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        getString(R.string.karaoke_host_confirm_close_room_title),
        getString(R.string.karaoke_host_confirm_close_room_content),
        v -> leaveRoom());
  }

  protected void showOnSeatAudienceCloseRoomDialog() {
    KaraokeUIUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        getString(R.string.karaoke_on_seat_confirm_close_room_title),
        v -> leaveRoom());
  }

  protected void showDownSeatDialog() {
    KaraokeUIUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        getString(R.string.karaoke_down_seat_confirm),
        v -> {
          NEKaraokeKit.getInstance()
              .leaveSeat(
                  new NEKaraokeCallback<Unit>() {

                    @Override
                    public void onSuccess(@Nullable Unit unit) {}

                    @Override
                    public void onFailure(int code, @Nullable String msg) {
                      if (msg != null) {
                        ToastX.showShortToast(msg);
                      }
                    }
                  });
          NEKaraokeKit.getInstance().requestStopPlayingSong(null);
        });
  }

  protected void showArrangeMicroDialog() {
    if (arrangeMicroDialog != null && arrangeMicroDialog.isVisible()) {
      return;
    }
    if (arrangeMicroDialog == null) {
      arrangeMicroDialog = new ArrangeMicroDialog();
    }
    if (applySeatItems != null) {
      arrangeMicroDialog.setDateList(applySeatItems);
    }
    if (!arrangeMicroDialog.isAdded() && fm.findFragmentByTag(ARRANGE_MICRO_DIALOG_TAG) == null) {
      arrangeMicroDialog.show(fm, ARRANGE_MICRO_DIALOG_TAG);

    } else {
      arrangeMicroDialog.dismiss();
    }
  }

  protected void showAudienceArrangeMicroDialog() {
    if (audienceArrangeMicroDialog != null && audienceArrangeMicroDialog.isVisible()) {
      return;
    }
    if (audienceArrangeMicroDialog == null) {
      audienceArrangeMicroDialog = new AudienceArrangeMicroDialog();
    }
    if (!audienceArrangeMicroDialog.isAdded()
        && fm.findFragmentByTag(AUDIENCE_ARRANGE_MICRO_DIALOG_TAG) == null) {
      audienceArrangeMicroDialog.show(fm, AUDIENCE_ARRANGE_MICRO_DIALOG_TAG);
    } else {
      audienceArrangeMicroDialog.dismiss();
    }
  }

  protected void initDataObserve() {
    karaokeRoomViewModel
        .getMemberCountData()
        .observe(
            this,
            count ->
                binding.tvChatRoomMemberCount.setText(
                    getString(R.string.karaoke_online_member_count, count)));
    karaokeRoomViewModel.getRewardData().observe(this, this::onUserReward);
    karaokeRoomViewModel
        .getRoomConnectState()
        .observe(
            this,
            neRoomConnectType -> {
              //这里处理断网重连的逻辑，而不是使用onNetworkConnected，因为有可能应用层断网重连了，但是IM网络还是不通
              if (neRoomConnectType == NERoomConnectType.Reconnect) {
                ALog.i(TAG, "room connect state connected");
                binding.singControlView.switchSongByNetwork();
                onNetworkConnected();
              } else {
                ALog.i(TAG, "room connect state disConnected");
                onNetworkDisconnected();
              }
            });
    karaokeRoomViewModel
        .getErrorData()
        .observe(
            this,
            endReason -> {
              if (endReason == NEKaraokeEndReason.CLOSE_BY_MEMBER) {
                if (!KaraokeUIUtils.isLocalHost()) {
                  ToastX.showShortToast(R.string.karaoke_host_close_room);
                }
                leaveRoom();
              } else if (endReason == NEKaraokeEndReason.END_OF_RTC) {
                leaveRoom();
              } else {
                KaraokeRoomActivity.this.finish();
              }
            });
    karaokeRoomViewModel
        .getChatRoomMsgData()
        .observe(this, charSequence -> binding.crvMsgList.appendItem(charSequence));
    karaokeRoomViewModel
        .getOnSeatListData()
        .observe(
            this,
            seatList -> {
              binding.seatView.updateSeats(seatList);
              binding.tvArrangeMicroNum.setText(
                  String.valueOf(KaraokeSeatUtils.getApplyingOnSeatList().size()));
              if (arrangeMicroDialog != null && arrangeMicroDialog.isVisible()) {
                arrangeMicroDialog.dismiss();
              }
              if (audienceArrangeMicroDialog != null && audienceArrangeMicroDialog.isVisible()) {
                audienceArrangeMicroDialog.dismiss();
              }
              refreshAudioSwitchButton();
            });
    karaokeRoomViewModel
        .getCurrentSeatState()
        .observe(
            this,
            seatState -> {
              if (currentSeatState != seatState) {
                currentSeatState = seatState;
                refreshAudioSwitchButton();
                if (currentSeatState == KaraokeRoomViewModel.CURRENT_SEAT_STATE_ON_SEAT) {
                  NEKaraokeKit.getInstance().unmuteMyAudio(null);
                }
              }
            });
    karaokeRoomViewModel
        .getApplySeatListData()
        .observe(
            this,
            applySeatModels -> {
              applySeatItems = applySeatModels;
              if (KaraokeUIUtils.isLocalHost()) {
                if (!applySeatModels.isEmpty()) {
                  binding.tvArrangeMicroNum.setVisibility(View.VISIBLE);
                  binding.tvArrangeMicroNum.setText(String.valueOf(applySeatModels.size()));
                } else {
                  binding.tvArrangeMicroNum.setVisibility(View.GONE);
                }
              } else {
                if (currentSeatState == KaraokeRoomViewModel.CURRENT_SEAT_STATE_APPLYING) {
                  binding.tvArrangeMicroNum.setText(String.valueOf(applySeatModels.size()));
                }
              }
            });
    karaokeRoomViewModel
        .getSongListData()
        .observe(this, songList -> orderSongViewModel.refreshOrderSongs());
  }

  protected void onUserReward(NEKaraokeBatchGiftModel reward) {
    if (roomInfo == null) {
      return;
    }
    binding.crvMsgList.appendItem(
        ChatRoomMsgCreator.createGiftReward(
            KaraokeRoomActivity.this,
            reward.getSendNick(),
            1,
            GiftCache.getGift(reward.getGiftId()).getStaticIconResId()));
    if (!KaraokeUIUtils.isLocalHost()) {
      giftRender.addGift(GiftCache.getGift(reward.getGiftId()).getDynamicIconResId());
    }
  }

  private void sendTextMsg(String msg) {
    if (msg == null) {
      return;
    }
    if (roomInfo == null) {
      return;
    }
    if (!TextUtils.isEmpty(msg.trim())) {
      NEKaraokeKit.getInstance()
          .sendTextMessage(
              msg,
              new NEKaraokeCallbackWrapper<Unit>() {

                @Override
                public void onSuccess(Unit unit) {
                  binding.crvMsgList.appendItem(
                      ChatRoomMsgCreator.createText(
                          KaraokeRoomActivity.this,
                          KaraokeUIUtils.isLocalHost(),
                          KaraokeUIUtils.getLocalName(),
                          msg));
                }

                @Override
                public void onError(int code, String msg) {
                  ALog.d(TAG, "sendTextMessage failed code = " + code + " msg = " + msg);
                }
              });
    }
  }

  protected void onNetworkDisconnected() {
    ToastX.showShortToast(R.string.karaoke_network_error);
    if (giftDialog != null && giftDialog.isShowing()) {
      giftDialog.dismiss();
    }
  }

  protected void onNetworkConnected() {
    karaokeRoomViewModel.updateSeat();
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    int x = (int) ev.getRawX();
    int y = (int) ev.getRawY();
    // 键盘区域外点击收起键盘
    if (!ViewUtils.isInView(binding.etRoomMsgInput, x, y)) {
      InputUtils.hideSoftInput(KaraokeRoomActivity.this, binding.etRoomMsgInput);
    }
    return super.dispatchTouchEvent(ev);
  }

  protected void applyOnSeat(NEKaraokeCallback<Unit> callback) {
    NEKaraokeKit.getInstance()
        .requestSeat(
            new NEKaraokeCallbackWrapper<Unit>() {

              @Override
              public void onSuccess(@Nullable Unit unit) {
                if (callback != null) {
                  callback.onSuccess(unit);
                }
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                if (callback != null) {
                  callback.onFailure(code, msg);
                }
              }
            });
  }

  protected void leaveRoom() {
    if (KaraokeUIUtils.isLocalHost()) {
      NEKaraokeKit.getInstance()
          .endRoom(
              new NEKaraokeCallbackWrapper<Unit>() {

                @Override
                public void onSuccess(@Nullable Unit unit) {
                  ALog.i(TAG, "endRoom success");
                  KaraokeRoomActivity.this.finish();
                }

                @Override
                public void onError(int code, @Nullable String msg) {
                  ALog.e(TAG, "endRoom failed code = " + code + " msg = " + msg);
                  KaraokeRoomActivity.this.finish();
                }
              });
    } else {
      NEKaraokeKit.getInstance()
          .leaveRoom(
              new NEKaraokeCallbackWrapper<Unit>() {

                @Override
                public void onSuccess(@Nullable Unit unit) {
                  ALog.i(TAG, "leaveRoom success");
                  KaraokeRoomActivity.this.finish();
                }

                @Override
                public void onError(int code, @Nullable String msg) {
                  ALog.e(TAG, "leaveRoom failed code = " + code + " msg = " + msg);
                  KaraokeRoomActivity.this.finish();
                }
              });
    }
  }

  @Override
  protected void onDestroy() {
    NEKaraokeUI.getInstance().notifyExitRoom();
    super.onDestroy();
  }

  private void nextSong(NEKaraokeSongModel song) {
    if (KaraokeUIUtils.isLocalSong(song)) {
      if (NEKaraokeKit.getInstance().getAllMemberList().size() <= 1) {
        binding.singControlView.startSolo(song);
      } else {
        binding.singControlView.inviteChorus(song.getOrderId());
      }
    }
    if (song != null) {
      if (song.getChannel() != null) {
        loadRes(song.getSongId(), song.getChannel());
      }
    }
  }

  private void unmuteMyAudio() {
    if (KaraokeUIUtils.isLocalMute()) {
      NEKaraokeKit.getInstance()
          .unmuteMyAudio(
              new NEKaraokeCallback<Unit>() {
                @Override
                public void onSuccess(@Nullable Unit unit) {
                  binding.ivLocalAudioSwitch.setSelected(false);
                  ToastX.showShortToast(R.string.karaoke_micro_phone_is_open);
                }

                @Override
                public void onFailure(int code, @Nullable String msg) {
                  ALog.e(TAG, "nextSong: unmuteMyAudio failure code:" + code + " msg = " + msg);
                }
              });
    }
  }

  private void loadRes(String songId, int channel) {
    boolean isExit = NEKaraokeKit.getInstance().isSongPreloaded(songId, channel);
    ALog.d(TAG, "loadRes songId = " + songId + ", isExit = " + isExit);
    if (!isExit) {
      NEKaraokeKit.getInstance()
          .preloadSong(
              songId,
              channel,
              new NEKaraokeCopyrightedMediaListener() {

                @Override
                public void onPreloadStart(String songId, int channel) {
                  ALog.i(TAG, "onPreloadStart songId = " + songId + ", channel = " + channel);
                }

                @Override
                public void onPreloadProgress(String songId, int channel, float progress) {
                  ALog.d(
                      TAG,
                      "onPreloadProgress songId = "
                          + songId
                          + ", channel = "
                          + channel
                          + ", progress = "
                          + progress);
                }

                @Override
                public void onPreloadComplete(
                    String songId, int channel, int errorCode, String msg) {
                  ALog.i(
                      TAG,
                      "onPreloadComplete songId = "
                          + songId
                          + ", channel = "
                          + channel
                          + ", errorCode = "
                          + errorCode
                          + ", msg = "
                          + msg);
                }
              });
    }
  }

  private void onClickLeaveBtn() {
    if (KaraokeUIUtils.isLocalHost()) {
      showCloseRoomDialog();
    } else if (KaraokeSeatUtils.isCurrentOnSeat()) {
      showOnSeatAudienceCloseRoomDialog();
    } else {
      leaveRoom();
    }
  }

  @Override
  public void onBackPressed() {
    if (KaraokeUIUtils.isLocalHost()) {
      showCloseRoomDialog();
    } else if (KaraokeSeatUtils.isCurrentOnSeat()) {
      showOnSeatAudienceCloseRoomDialog();
    } else {
      leaveRoom();
      super.onBackPressed();
    }
  }

  @Override
  public void onTokenExpired() {
    ALog.d(TAG, "onTokenExpired");
    ToastX.showShortToast(R.string.copyright_token_has_expired);
    NEKaraokeKit.getInstance().getSongDynamicTokenUntilSuccess(null);
  }
}
