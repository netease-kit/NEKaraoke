// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.viewmodel;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.utils.Utils;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeBatchGiftModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatInfo;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.karaokekit.ui.helper.SeatHelper;
import com.netease.yunxin.kit.karaokekit.ui.listener.MyKaraokeListener;
import com.netease.yunxin.kit.karaokekit.ui.listener.NEKaraokeCallbackWrapper;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeSeatUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUIUtils;
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KaraokeRoomViewModel extends ViewModel {

  public static final String TAG = "KaraokeRoomViewModel";
  public static final int CURRENT_SEAT_STATE_IDLE = 0;
  public static final int CURRENT_SEAT_STATE_APPLYING = 1;
  public static final int CURRENT_SEAT_STATE_ON_SEAT = 2;

  MutableLiveData<Integer> currentSeatState = new MutableLiveData<>();

  MutableLiveData<NEKaraokeEndReason> errorData = new MutableLiveData<>();
  MutableLiveData<NERoomConnectType> roomConnectState = new MutableLiveData<>();

  MutableLiveData<CharSequence> chatRoomMsgData = new MutableLiveData<>();
  MutableLiveData<Integer> memberCountData = new MutableLiveData<>();

  MutableLiveData<NEKaraokeBatchGiftModel> rewardData = new MutableLiveData<>();

  MutableLiveData<List<OnSeatModel>> onSeatListData = new MutableLiveData<>();

  MutableLiveData<List<ApplySeatModel>> applySeatListData = new MutableLiveData<>();

  MutableLiveData<Boolean> songListData = new MutableLiveData<>();

  NEKaraokeRoomInfo liveInfoNE = null;

  private final MyKaraokeListener listener =
      new MyKaraokeListener() {

        @Override
        public void onReceiveTextMessage(@NonNull NEKaraokeChatTextMessage message) {
          String content = message.getText();
          ALog.i(TAG, "onReceiveTextMessage :${message.fromNick}");
          chatRoomMsgData.postValue(
              ChatRoomMsgCreator.createText(
                  NEKaraokeUI.getInstance().getApplication(),
                  KaraokeUIUtils.isHost(message.getFromUserUuid()),
                  message.getFromNick(),
                  content));
        }

        @Override
        public void onReceiveBatchGift(@NonNull NEKaraokeBatchGiftModel giftModel) {
          ALog.i(TAG, "onReceiveGift");
          rewardData.postValue(giftModel);
        }

        @Override
        public void onMemberAudioMuteChanged(
            @NonNull NEKaraokeMember member, boolean mute, @Nullable NEKaraokeMember operateBy) {
          ALog.i(TAG, "onMemberAudioMuteChanged onSeatList = $onSeatList");
          List<OnSeatModel> onSeatList = SeatHelper.getInstance().getOnSeatItems();
          if (onSeatList != null) {
            for (OnSeatModel seatModel : onSeatList) {
              if (TextUtils.equals(seatModel.getAccount(), member.getAccount())) {
                seatModel.setMute(mute);
              }
            }
            onSeatListData.postValue(onSeatList);
          }
        }

        @Override
        public void onMemberJoinRoom(@NonNull List<NEKaraokeMember> members) {
          for (NEKaraokeMember member : members) {
            ALog.d(TAG, "onMemberJoinRoom :${member.name}");
            if (!KaraokeUIUtils.isLocalAccount(member.getAccount())) {
              chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomEnter(member.getName()));
            }
          }
          updateRoomMemberCount();
        }

        @Override
        public void onMemberLeaveRoom(@NonNull List<NEKaraokeMember> members) {
          for (NEKaraokeMember member : members) {
            ALog.d(TAG, "onMemberLeaveRoom :$member.name");
            chatRoomMsgData.postValue(ChatRoomMsgCreator.createRoomExit(member.getName()));
          }
          updateRoomMemberCount();
          getSeatRequestList();
        }

        @Override
        public void onMemberJoinChatroom(@NonNull List<NEKaraokeMember> members) {
          for (NEKaraokeMember member : members) {
            ALog.d(TAG, "onMemberJoinChatroom :$member.name");
          }
        }

        @Override
        public void onMemberLeaveChatroom(@NonNull List<NEKaraokeMember> members) {
          for (NEKaraokeMember member : members) {
            ALog.d(TAG, "onMemberJoinChatroom :$member.name");
          }
        }

        @Override
        public void onSeatRequestSubmitted(int seatIndex, @NonNull String account) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_APPLYING);
          }
          if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    KaraokeSeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_on_seat_request)));
          }
          getSeatRequestList();
        }

        @Override
        public void onSeatRequestApproved(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_ON_SEAT);
          }
          if (!KaraokeUIUtils.isHost(account)) {
            if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
              chatRoomMsgData.postValue(
                  ChatRoomMsgCreator.createSeatMessage(
                      KaraokeSeatUtils.getMemberNick(account),
                      Utils.getApp().getString(R.string.karaoke_on_seat)));
            }
          }
          getSeatRequestList();
        }

        @Override
        public void onSeatRequestCancelled(int seatIndex, @NonNull String account) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
          }
          if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    KaraokeSeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_cancel_on_seat_request)));
          }
          getSeatRequestList();
        }

        @Override
        public void onSeatRequestRejected(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
          }
          getSeatRequestList();
          if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    KaraokeSeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_on_seat_reject)));
          }
        }

        @Override
        public void onSeatLeave(int seatIndex, @NonNull String account) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
          }
          if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    KaraokeSeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_down_seat)));
          }
        }

        @Override
        public void onSeatListChanged(@NonNull List<NEKaraokeSeatItem> seatItems) {
          ALog.i(TAG, "onSeatListChanged seatItems = $seatItems");
          onSeatListData.postValue(KaraokeSeatUtils.transNESeatItem2OnSeatModel(seatItems));
        }

        @Override
        public void onSeatKicked(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          if (TextUtils.equals(account, KaraokeSeatUtils.getCurrentUuid())) {
            currentSeatState.postValue(CURRENT_SEAT_STATE_IDLE);
          }
          if (!TextUtils.isEmpty(KaraokeSeatUtils.getMemberNick(account))) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSeatMessage(
                    KaraokeSeatUtils.getMemberNick(account),
                    Utils.getApp().getString(R.string.karaoke_kickout_seat)));
          }
        }

        @Override
        public void onReceiveChorusMessage(
            @NonNull NEKaraokeChorusActionType actionType, @NonNull NEKaraokeSongModel songModel) {
          switch (actionType) {
            case INVITE: // / 邀请消息
              break;
            case AGREE_INVITE: // 同意邀请消息
              break;
            case READY: // / 准备完成可以开始了
              break;
            case START_SONG:
              if (songModel.getOperator() != null
                  && songModel.getOperator().getUserName() != null) {
                chatRoomMsgData.postValue(
                    ChatRoomMsgCreator.createSongMessage(
                        songModel.getOperator().getUserName(),
                        Utils.getApp().getString(R.string.song_start, songModel.getSongName())));
              }
              break;
            case CANCEL_INVITE: // 取消邀请
              break;
            case PAUSE_SONG: // / 歌曲暂停
              if (songModel.getOperator() != null
                  && songModel.getOperator().getUserName() != null) {
                chatRoomMsgData.postValue(
                    ChatRoomMsgCreator.createSongMessage(
                        songModel.getOperator().getUserName(),
                        Utils.getApp().getString(R.string.song_pause, songModel.getSongName())));
              }
              break;
            case RESUME_SONG: // / 恢复播放
              if (songModel.getOperator() != null
                  && songModel.getOperator().getUserName() != null) {
                chatRoomMsgData.postValue(
                    ChatRoomMsgCreator.createSongMessage(
                        songModel.getOperator().getUserName(),
                        Utils.getApp().getString(R.string.song_restart, songModel.getSongName())));
              }
              break;
            case ABANDON:
              break;
            case END_SONG: // / 结束
              break;
          }
        }

        @Override
        public void onRoomEnded(@NonNull NEKaraokeEndReason reason) {
          errorData.postValue(reason);
        }

        @Override
        public void onRoomConnectStateChanged(@NonNull NERoomConnectType state) {
          roomConnectState.postValue(state);
        }

        @Override
        public void onRtcChannelError(int code) {
          if (code == 30015) {
            errorData.postValue(NEKaraokeEndReason.valueOf("END_OF_RTC"));
          }
        }

        @Override
        public void onOrderedSongListChanged() {
          ALog.i(TAG, "onOrderedSongListChanged");
          songListData.postValue(true);
        }

        @Override
        public void onSongOrdered(NEKaraokeOrderSongModel song) {
          ALog.i(TAG, "onSongOrdered song = $song");
          if (song.getOperatorUser() != null && song.getOperatorUser().getUserName() != null) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.getOperatorUser().getUserName(),
                    Utils.getApp()
                        .getString(
                            R.string.song_ordered,
                            song.getOrderSongResultDto().getOrderSong().getSongName())));
          }
        }

        @Override
        public void onSongDeleted(NEKaraokeOrderSongModel song) {
          ALog.i(TAG, "onSongDeleted song = $song");
          if (song.getOperatorUser() != null && song.getOperatorUser().getUserName() != null) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.getOperatorUser().getUserName(),
                    Utils.getApp()
                        .getString(
                            R.string.song_deleted,
                            song.getOrderSongResultDto().getOrderSong().getSongName())));
          }
        }

        @Override
        public void onSongTopped(NEKaraokeOrderSongModel song) {
          ALog.i(TAG, "onSongTopped song = $song");
          if (song.getOperatorUser() != null && song.getOperatorUser().getUserName() != null) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.getOperatorUser().getUserName(),
                    Utils.getApp()
                        .getString(
                            R.string.song_top,
                            song.getOrderSongResultDto().getOrderSong().getSongName())));
          }
        }

        @Override
        public void onNextSong(NEKaraokeOrderSongModel song) {
          ALog.i(TAG, "onSongNext song = $song");
          if (song.getOperatorUser() != null && song.getOperatorUser().getUserName() != null) {
            chatRoomMsgData.postValue(
                ChatRoomMsgCreator.createSongMessage(
                    song.getOperatorUser().getUserName(),
                    Utils.getApp().getString(R.string.song_switched)));
          }
        }
      };

  public void getSeatRequestList() {
    NEKaraokeKit.getInstance()
        .getSeatRequestList(
            new NEKaraokeCallback<List<NEKaraokeSeatRequestItem>>() {

              @Override
              public void onFailure(int code, @Nullable String msg) {}

              @Override
              public void onSuccess(
                  @Nullable List<NEKaraokeSeatRequestItem> neKaraokeSeatRequestItems) {
                if (neKaraokeSeatRequestItems != null) {
                  List<ApplySeatModel> applySeatList = new ArrayList<>();
                  for (NEKaraokeSeatRequestItem requestItem : neKaraokeSeatRequestItems) {
                    applySeatList.add(
                        new ApplySeatModel(
                            requestItem.getUser(),
                            requestItem.getUserName(),
                            requestItem.getIcon(),
                            ApplySeatModel.SEAT_STATUS_PRE_ON_SEAT));
                  }
                  SeatHelper.getInstance().setApplySeatList(applySeatList);
                  applySeatListData.postValue(applySeatList);
                }
              }
            });
  }

  void refreshLiveInfo(NEKaraokeRoomInfo liveInfoNE) {
    this.liveInfoNE = liveInfoNE;
  }

  public boolean isAnchor(String fromAccount) {
    return liveInfoNE.getAnchor().getAccount().equals(fromAccount);
  }

  public void initDataOnJoinRoom() {
    NEKaraokeKit.getInstance().addKaraokeListener(listener);
    updateRoomMemberCount();
  }

  void updateRoomMemberCount() {
    memberCountData.postValue(NEKaraokeKit.getInstance().getAllMemberList().size());
  }

  public void updateSeat() {
    NEKaraokeKit.getInstance()
        .getSeatInfo(
            new NEKaraokeCallbackWrapper<NEKaraokeSeatInfo>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeSeatInfo seatInfo) {
                if (seatInfo != null) {

                  List<OnSeatModel> onSeatModelList =
                      KaraokeSeatUtils.transNESeatItem2OnSeatModel(seatInfo.getSeatItems());
                  onSeatListData.postValue(onSeatModelList);

                  List<OnSeatModel> onSeatModels = new ArrayList<>();
                  if (onSeatModelList != null) {
                    for (OnSeatModel onSeatModel : onSeatModelList) {
                      if (onSeatModel.getStatus() == ApplySeatModel.SEAT_STATUS_ON_SEAT) {
                        onSeatModels.add(onSeatModel);
                      }
                    }
                  }
                  Collections.sort(onSeatModels);
                  SeatHelper.getInstance().setOnSeatItems(onSeatModels);
                  currentSeatState.postValue(
                      KaraokeSeatUtils.isCurrentOnSeat()
                          ? CURRENT_SEAT_STATE_ON_SEAT
                          : (KaraokeSeatUtils.isCurrentApplyingSeat()
                              ? CURRENT_SEAT_STATE_APPLYING
                              : CURRENT_SEAT_STATE_IDLE));
                }
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.e(TAG, "getSeatInfo failed code = " + code + " msg = " + msg);
              }
            });
  }

  @Override
  public void onCleared() {
    super.onCleared();
    NEKaraokeKit.getInstance().removeKaraokeListener(listener);
    SeatHelper.getInstance().destroy();
  }

  public MutableLiveData<Integer> getCurrentSeatState() {
    return currentSeatState;
  }

  public MutableLiveData<NEKaraokeEndReason> getErrorData() {
    return errorData;
  }

  public MutableLiveData<NERoomConnectType> getRoomConnectState() {
    return roomConnectState;
  }

  public MutableLiveData<CharSequence> getChatRoomMsgData() {
    return chatRoomMsgData;
  }

  public MutableLiveData<Integer> getMemberCountData() {
    return memberCountData;
  }

  public MutableLiveData<NEKaraokeBatchGiftModel> getRewardData() {
    return rewardData;
  }

  public MutableLiveData<List<OnSeatModel>> getOnSeatListData() {
    return onSeatListData;
  }

  public MutableLiveData<List<ApplySeatModel>> getApplySeatListData() {
    return applySeatListData;
  }

  public MutableLiveData<Boolean> getSongListData() {
    return songListData;
  }

  public NEKaraokeRoomInfo getLiveInfoNE() {
    return liveInfoNE;
  }

  public MyKaraokeListener getListener() {
    return listener;
  }
}
