// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioFrame;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeAudioOutputDevice;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeChorusActionType;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeEndReason;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeListener;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeChatTextMessage;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeGiftModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeMember;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeOrderSongModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatItem;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import java.util.List;

public class MyKaraokeListener implements NEKaraokeListener {

  @Override
  public void onMemberJoinRoom(@NonNull List<NEKaraokeMember> members) {}

  @Override
  public void onMemberLeaveRoom(@NonNull List<NEKaraokeMember> members) {}

  @Override
  public void onMemberJoinChatroom(@NonNull List<NEKaraokeMember> members) {}

  @Override
  public void onMemberLeaveChatroom(@NonNull List<NEKaraokeMember> members) {}

  @Override
  public void onRoomEnded(@NonNull NEKaraokeEndReason reason) {}

  @Override
  public void onRtcChannelError(int code) {}

  @Override
  public void onAudioOutputDeviceChanged(@NonNull NEKaraokeAudioOutputDevice device) {}

  @Override
  public void onMemberAudioMuteChanged(
      @NonNull NEKaraokeMember member, boolean mute, @Nullable NEKaraokeMember operateBy) {}

  @Override
  public void onReceiveTextMessage(@NonNull NEKaraokeChatTextMessage message) {}

  @Override
  public void onReceiveChorusMessage(
      @NonNull NEKaraokeChorusActionType actionType, @NonNull NEKaraokeSongModel model) {}

  @Override
  public void onReceiveGift(@NonNull NEKaraokeGiftModel rewardMsg) {}

  @Override
  public void onSeatRequestSubmitted(int seatIndex, @NonNull String account) {}

  @Override
  public void onSeatRequestCancelled(int seatIndex, @NonNull String account) {}

  @Override
  public void onSeatRequestApproved(
      int seatIndex, @NonNull String account, @NonNull String operateBy) {}

  @Override
  public void onSeatRequestRejected(
      int seatIndex, @NonNull String account, @NonNull String operateBy) {}

  @Override
  public void onSeatLeave(int seatIndex, @NonNull String account) {}

  @Override
  public void onSeatKicked(int seatIndex, @NonNull String account, @NonNull String operateBy) {}

  @Override
  public void onSeatListChanged(@NonNull List<NEKaraokeSeatItem> seatItems) {}

  @Override
  public void onSongListChanged() {}

  @Override
  public void onSongOrdered(NEKaraokeOrderSongModel song) {}

  @Override
  public void onSongDeleted(NEKaraokeOrderSongModel song) {}

  @Override
  public void onSongTopped(NEKaraokeOrderSongModel song) {}

  @Override
  public void onNextSong(NEKaraokeOrderSongModel song) {}

  @Override
  public void onSongPlayingPosition(long position) {}

  @Override
  public void onRecordingAudioFrame(@NonNull NEKaraokeAudioFrame frame) {}

  @Override
  public void onSongPlayingCompleted() {}
}
