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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.copyrightedmedia.api.NECopyrightedMedia;
import com.netease.yunxin.kit.copyrightedmedia.api.NESongPreloadCallback;
import com.netease.yunxin.kit.copyrightedmedia.impl.NECopyrightedEventHandler;
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeOptions;
import com.netease.yunxin.kit.karaokekit.api.NEJoinKaraokeParams;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeRole;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeGiftModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
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
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeRoomModel;
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig;
import com.netease.yunxin.kit.karaokekit.ui.utils.ClickUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.InputUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.NetUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.SeatUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.ViewUtils;
import com.netease.yunxin.kit.karaokekit.ui.view.SingingControlView;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.KaraokeRoomViewModel;
import com.netease.yunxin.kit.karaokekit.ui.viewmodel.OrderSongViewModel;
import java.util.List;
import kotlin.Unit;

public class KaraokeRoomActivity extends BaseActivity implements NECopyrightedEventHandler {

  protected static final String TAG = "KaraokeRoomActivity";

  protected static final String ARRANGE_MICRO_DIALOG_TAG = "arrangeMicroDialog";

  protected static final String AUDIENCE_ARRANGE_MICRO_DIALOG_TAG = "audienceArrangeMicroDialog";

  protected ActivityKaraokeRoomBinding binding;

  protected NEKaraokeRoomInfo roomInfo;

  protected KaraokeRoomModel roomModel;

  protected KaraokeRoomViewModel karaokeRoomViewModel;

  protected ArrangeMicroDialog arrangeMicroDialog = null;

  protected AudienceArrangeMicroDialog audienceArrangeMicroDialog = null;

  protected final FragmentManager fm = getSupportFragmentManager();

  private List<ApplySeatModel> applySeatItems;

  protected int currentSeatState = KaraokeRoomViewModel.CURRENT_SEAT_STATE_IDLE;

  private OrderSongViewModel orderSongViewModel;

  private GiftDialog giftDialog;

  private final GiftRender giftRender = new GiftRender();

  private boolean isLastDisconnected;

  protected final NetworkUtils.OnNetworkStatusChangedListener netWorkStatusChangeListener =
      new NetworkUtils.OnNetworkStatusChangedListener() {

        @Override
        public void onDisconnected() {
          ALog.i(TAG, "network disconnected");
          isLastDisconnected = true;
          onNetworkDisconnected();
        }

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
          ALog.i(TAG, "network onConnected");
          if (isLastDisconnected) {
            onNetworkConnected(networkType.name());
            isLastDisconnected = false;
          }
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityKaraokeRoomBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    int barHeight = ImmersionBar.getStatusBarHeight(this);
    binding.clyAnchorInfo.setPadding(
        binding.clyAnchorInfo.getPaddingLeft(),
        binding.clyAnchorInfo.getPaddingTop() + barHeight,
        binding.clyAnchorInfo.getPaddingRight(),
        binding.clyAnchorInfo.getPaddingBottom());
    initIntent();
    karaokeRoomViewModel =
        new ViewModelProvider(
                this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory())
            .get(KaraokeRoomViewModel.class);
    requestPermissionsIfNeeded();
    orderSongViewModel =
        new ViewModelProvider(
                this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory())
            .get(OrderSongViewModel.class);
    orderSongViewModel
        .getPerformOrderSongEvent()
        .observe(
            this,
            orderSong -> {
              if (SeatUtils.isCurrentOnSeat()) {
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
              if (orderSongs.size() > 0) {
                binding.tvMusicNum.setVisibility(View.VISIBLE);
                binding.tvMusicNum.setText(String.valueOf(orderSongs.size()));
              } else {
                binding.tvMusicNum.setVisibility(View.GONE);
              }
            });
    initView();
    NECopyrightedMedia.getInstance().setEventHandler(this);
  }

  private void initView() {
    binding.tvKaraokeRoomName.setText(roomModel.getRoomName());
    binding.tvChatRoomMemberCount.setText(getString(R.string.karaoke_online_member_count, 0));
    if (KaraokeUtils.isHostRole(roomModel.getRole())) {
      binding.ivGift.setVisibility(View.GONE);
    } else {
      binding.ivGift.setVisibility(View.VISIBLE);
      binding.ivArrangeMicro.setImageResource(R.drawable.on_micro);
      binding.ivArrangeMicro.setBackgroundResource(R.drawable.red_cycle_bg);
    }
  }

  private void initIntent() {
    roomModel =
        (KaraokeRoomModel) getIntent().getSerializableExtra(NEKaraokeUIConstants.INTENT_ROOM_MODEL);
  }

  /** 权限检查 */
  private void requestPermissionsIfNeeded() {
    PermissionUtils.permission(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE)
        .callback(
            new PermissionUtils.FullCallback() {

              @Override
              public void onGranted(@NonNull List<String> granted) {
                if (granted.size() == 3) {
                  joinRoom(
                      roomModel.getRoomUuid(),
                      roomModel.getNick(),
                      roomModel.getLiveRecordId(),
                      roomModel.getRole());
                }
              }

              @Override
              public void onDenied(
                  @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                ToastUtils.showShort(R.string.karaoke_authorization_failed);
                finish();
              }
            })
        .request();
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
                KaraokeRoomActivity.this.roomInfo = roomInfo;
                initViewAfterJoinRoom();
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.e(TAG, "joinRoom failed code = " + code + " msg = " + msg);
                ToastUtils.showShort(R.string.karaoke_room_already_closed);
                KaraokeRoomActivity.this.finish();
              }
            });
  }

  protected void initViewAfterJoinRoom() {
    binding.singControlView.init();
    initListener();
    initDataObserve();
    initGiftAnimation();
    karaokeRoomViewModel.initDataOnJoinRoom();
    orderSongViewModel.refreshOrderSongs();
    binding.ivArrangeMicro.setImageResource(
        KaraokeUtils.isCurrentHost() ? R.drawable.arrange_micro : R.drawable.on_micro);
    binding.ivArrangeMicro.setBackgroundResource(
        KaraokeUtils.isCurrentHost() ? R.drawable.dark_cycle_bg : R.drawable.red_cycle_bg);
    if (KaraokeUtils.isCurrentHost()) {
      applyOnSeat(null);
    } else {
      updateSeat();
    }
    refreshAudioSwitchButton();
  }

  public void updateSeat() {
    NEKaraokeKit.getInstance()
        .getSeatInfo(
            new NEKaraokeCallbackWrapper<NEKaraokeSeatInfo>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeSeatInfo seatInfo) {
                if (seatInfo != null) {
                  binding.seatView.updateSeats(
                      SeatUtils.transNESeatItem2OnSeatModel(seatInfo.getSeatItems()));
                }
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.e(TAG, "getSeatInfo failed code = " + code + " msg = " + msg);
              }
            });
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
    if (SeatUtils.isCurrentOnSeat()) {
      binding.ivLocalAudioSwitch.setVisibility(View.VISIBLE);
    } else {
      binding.ivLocalAudioSwitch.setVisibility(View.GONE);
      binding.ivLocalAudioSwitch.setSelected(false);
    }
  }

  @SuppressLint("MissingPermission")
  protected void initListener() {
    binding.ivLeaveRoom.setOnClickListener(v -> onClickLeaveBtn());
    NetUtils.registerStateListener(netWorkStatusChangeListener);
    binding.tvRoomMsgInput.setOnClickListener(
        view -> InputUtils.showSoftInput(binding.etRoomMsgInput));
    binding.etRoomMsgInput.setOnEditorActionListener(
        (textView, i, keyEvent) -> {
          if (textView == binding.etRoomMsgInput) {
            if (!NetUtils.checkNetwork(KaraokeRoomActivity.this)) {
              return true;
            }
            String input = binding.etRoomMsgInput.getText().toString();
            InputUtils.hideSoftInput(binding.etRoomMsgInput);
            sendTextMsg(input);
            return true;
          }
          return false;
        });
    binding.ivLocalAudioSwitch.setOnClickListener(
        v -> {
          if (!NetUtils.isConnected()) {
            ToastUtils.showShort(R.string.karaoke_network_error);
            return;
          }
          if (binding.ivLocalAudioSwitch.isSelected()) {
            NEKaraokeKit.getInstance()
                .unmuteMyAudio(
                    new NEKaraokeCallback<Unit>() {
                      @Override
                      public void onSuccess(@Nullable Unit unit) {
                        binding.ivLocalAudioSwitch.setSelected(false);
                        ToastUtils.showShort(R.string.karaoke_micro_phone_is_open);
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
                        ToastUtils.showShort(R.string.karaoke_micro_phone_is_close);
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
          if (KaraokeUtils.isCurrentHost()) {
            if (model != null
                && model.getUser() != null
                && !KaraokeUtils.isMySelf(model.getAccount())) {
              showKickOutSeatDialog(model.getUser().getAccount());
            }
          } else {
            if (model != null) {
              if (model.getUser() != null) {
                if (SeatUtils.isOnSeat(model.getUser().getAccount())
                    && KaraokeUtils.isMySelf(model.getAccount())) {
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
          if (KaraokeUtils.isCurrentHost()) {
            showArrangeMicroDialog();
          } else {
            if (SeatUtils.isCurrentOnSeat()) {
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
          giftDialog.show(
              giftId ->
                  NEKaraokeKit.getInstance()
                      .sendGift(
                          giftId,
                          new NEKaraokeCallbackWrapper<Unit>() {

                            @Override
                            public void onSuccess(@Nullable Unit unit) {}

                            @Override
                            public void onError(int code, @Nullable String msg) {
                              ToastUtils.showShort(R.string.karaoke_reward_failed);
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
            if (binding.singControlView.isAnchor() || binding.singControlView.isAssistant()) {
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

    if (SeatUtils.isCurrentOnSeat()) {
      return;
    }
    KaraokeUtils.showCommonDialog(
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
                  public void onError(int code, @Nullable String msg) { // 1303
                    ToastUtils.showShort(msg);
                  }
                }));
  }

  protected void showCloseRoomDialog() {
    KaraokeUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        getString(R.string.karaoke_host_confirm_close_room_title),
        getString(R.string.karaoke_host_confirm_close_room_content),
        v -> leaveRoom());
  }

  protected void showOnSeatAudienceCloseRoomDialog() {
    KaraokeUtils.showCommonDialog(
        KaraokeRoomActivity.this,
        getString(R.string.karaoke_on_seat_confirm_close_room_title),
        v -> leaveRoom());
  }

  protected void showDownSeatDialog() {
    KaraokeUtils.showCommonDialog(
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
                      ToastUtils.showShort(msg);
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
        .getErrorData()
        .observe(
            this,
            endReason -> {
              if (endReason == NEKaraokeEndReason.CLOSE_BY_MEMBER) {
                if (!KaraokeUtils.isCurrentHost()) {
                  ToastUtils.showShort(R.string.karaoke_host_close_room);
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
                  String.valueOf(SeatUtils.getApplyingOnSeatList().size()));
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
              currentSeatState = seatState;
              switch (currentSeatState) {
                case KaraokeRoomViewModel.CURRENT_SEAT_STATE_APPLYING:
                  if (!KaraokeUtils.isCurrentHost()) {
                    binding.ivArrangeMicro.setImageResource(R.drawable.arrange_micro);
                    binding.ivArrangeMicro.setBackgroundResource(R.drawable.dark_cycle_bg);
                    binding.tvArrangeMicroNum.setVisibility(View.VISIBLE);
                  }
                  break;
                case KaraokeRoomViewModel.CURRENT_SEAT_STATE_ON_SEAT:
                  if (!KaraokeUtils.isCurrentHost()) {
                    binding.ivArrangeMicro.setImageResource(R.drawable.down_mircro);
                    binding.ivArrangeMicro.setBackgroundResource(R.drawable.dark_cycle_bg);
                    binding.tvArrangeMicroNum.setVisibility(View.GONE);
                  }
                  NEKaraokeKit.getInstance().unmuteMyAudio(null);
                  break;
                default:
                  if (!KaraokeUtils.isCurrentHost()) {
                    binding.ivArrangeMicro.setImageResource(R.drawable.on_micro);
                    binding.ivArrangeMicro.setBackgroundResource(R.drawable.red_cycle_bg);
                    binding.tvArrangeMicroNum.setVisibility(View.GONE);
                  }
              }
              refreshAudioSwitchButton();
            });
    karaokeRoomViewModel
        .getApplySeatListData()
        .observe(
            this,
            applySeatModels -> {
              applySeatItems = applySeatModels;
              if (KaraokeUtils.isCurrentHost()) {
                if (applySeatModels.size() > 0) {
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

  protected void onUserReward(NEKaraokeGiftModel reward) {
    if (roomInfo == null) {
      return;
    }
    binding.crvMsgList.appendItem(
        ChatRoomMsgCreator.createGiftReward(
            KaraokeRoomActivity.this,
            reward.getSendNick(),
            1,
            GiftCache.getGift(reward.getGiftId()).getStaticIconResId()));
    if (!KaraokeUtils.isCurrentHost()) {
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
                          KaraokeUtils.isCurrentHost(),
                          KaraokeUtils.getCurrentName(),
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
    ToastUtils.showLong(R.string.karaoke_network_error);
    ALog.d(TAG, "onDisconnected():" + System.currentTimeMillis());
    if (giftDialog != null && giftDialog.isShowing()) {
      giftDialog.dismiss();
    }
  }

  protected void onNetworkConnected(String networkType) {
    ALog.i(TAG, "onNetworkConnected type = " + networkType);
    binding.singControlView.clickNextSong();
    updateSeat();
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    int x = (int) ev.getRawX();
    int y = (int) ev.getRawY();
    // 键盘区域外点击收起键盘
    if (!ViewUtils.isInView(binding.etRoomMsgInput, x, y)) {
      InputUtils.hideSoftInput(binding.etRoomMsgInput);
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
    if (KaraokeUtils.isCurrentHost()) {
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
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    NetUtils.unregisterStateListener(netWorkStatusChangeListener);
  }

  private void nextSong(NEKaraokeSongModel song) {
    if (isMySong(song)) {
      if (NEKaraokeKit.getInstance().getAllMemberList().size() <= 1) {
        binding.singControlView.startSolo(song);
      } else {
        binding.singControlView.inviteChorus(song.getOrderId());
      }
    }
    if (song != null) {
      loadRes(song.getSongId());
    }
  }

  private void unmuteMyAudio() {
    boolean isAudioOn = NEKaraokeKit.getInstance().getLocalMember().isAudioOn();
    if (!isAudioOn) {
      NEKaraokeKit.getInstance()
          .unmuteMyAudio(
              new NEKaraokeCallback<Unit>() {
                @Override
                public void onSuccess(@Nullable Unit unit) {
                  binding.ivLocalAudioSwitch.setSelected(false);
                  ToastUtils.showShort(R.string.karaoke_micro_phone_is_open);
                }

                @Override
                public void onFailure(int code, @Nullable String msg) {
                  ALog.e(TAG, "nextSong: unmuteMyAudio failure code:" + code + " msg = " + msg);
                }
              });
    }
  }

  private boolean isMySong(NEKaraokeSongModel song) {
    if (song == null) {
      return false;
    }
    return TextUtils.equals(
        song.getUserUuid(), NEKaraokeKit.getInstance().getLocalMember().getAccount());
  }

  private void loadRes(String songId) {
    boolean isExit = NECopyrightedMedia.getInstance().isSongPreloaded(songId);
    if (!isExit) {
      NECopyrightedMedia.getInstance()
          .preloadSong(
              songId,
              new NESongPreloadCallback() {

                @Override
                public void onPreloadStart(String songId) {}

                @Override
                public void onPreloadProgress(String songId, float progress) {}

                @Override
                public void onPreloadComplete(String songId, int errorCode, String msg) {}
              });
    }
  }

  private void onClickLeaveBtn() {
    if (KaraokeUtils.isCurrentHost()) {
      showCloseRoomDialog();
    } else if (SeatUtils.isCurrentOnSeat()) {
      showOnSeatAudienceCloseRoomDialog();
    } else {
      leaveRoom();
    }
  }

  @Override
  public void onBackPressed() {
    if (KaraokeUtils.isCurrentHost()) {
      showCloseRoomDialog();
    } else if (SeatUtils.isCurrentOnSeat()) {
      showOnSeatAudienceCloseRoomDialog();
    } else {
      leaveRoom();
      super.onBackPressed();
    }
  }

  @Override
  public void onTokenExpired() {
    ALog.d(TAG, "onTokenExpired");
    ToastUtils.showShort(R.string.copyright_token_has_expired);
    NEKaraokeKit.getInstance().getSongDynamicTokenUntilSuccess(null);
  }
}
