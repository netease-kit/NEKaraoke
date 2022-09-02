// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view.seat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieDrawable;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.adapter.BaseAdapter;
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.VoiceRoomSeat;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.SeatUtils;
import java.util.ArrayList;

public class SeatAdapter extends BaseAdapter<VoiceRoomSeat> {

  private NEKaraokeSongModel songModel;

  public SeatAdapter(ArrayList<VoiceRoomSeat> seats, Context context) {
    super(seats, context);
  }

  @Override
  protected RecyclerView.ViewHolder onCreateBaseViewHolder(ViewGroup parent, int viewType) {
    return new SeatViewHolder(layoutInflater.inflate(R.layout.item_seat_ktv, parent, false));
  }

  @Override
  protected void onBindBaseViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    VoiceRoomSeat seat = getItem(position);
    if (seat == null) {
      return;
    }
    SeatViewHolder viewHolder = (SeatViewHolder) holder;
    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.rightMargin = ScreenUtil.dip2px(2);
    viewHolder.itemView.setLayoutParams(layoutParams);
    int status = seat.getStatus();
    OnSeatModel user = seat.getUser();
    viewHolder.waveView.stop();
    switch (status) {
      case VoiceRoomSeat.Status.INIT:
        viewHolder.ivStatusHint.setVisibility(View.GONE);
        viewHolder.iv_user_status.setVisibility(View.VISIBLE);
        viewHolder.iv_user_status.setImageResource(R.drawable.sofa);
        viewHolder.circle.setVisibility(View.INVISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
      case VoiceRoomSeat.Status.APPLY:
        viewHolder.iv_user_status.setVisibility(View.VISIBLE);
        viewHolder.iv_user_status.setImageResource(0);
        viewHolder.applying.setVisibility(View.VISIBLE);
        viewHolder.applying.setRepeatCount(LottieDrawable.INFINITE);
        viewHolder.applying.playAnimation();
        if (seat.getReason() != VoiceRoomSeat.Reason.APPLY_MUTED) {
          viewHolder.ivStatusHint.setVisibility(View.GONE);
        } else {
          viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
          viewHolder.ivStatusHint.setImageResource(R.drawable.audio_be_muted_status);
        }
        viewHolder.tvNick.setText(user != null ? user.getAccount() : "");
        viewHolder.circle.setVisibility(View.INVISIBLE);
        break;
      case VoiceRoomSeat.Status.ON:
        viewHolder.iv_user_status.setVisibility(View.GONE);
        viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
        viewHolder.ivStatusHint.setImageResource(R.drawable.icon_seat_open_micro);
        viewHolder.circle.setVisibility(View.VISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
      case VoiceRoomSeat.Status.CLOSED:
        viewHolder.iv_user_status.setVisibility(View.VISIBLE);
        viewHolder.ivStatusHint.setVisibility(View.GONE);
        viewHolder.iv_user_status.setImageResource(R.drawable.close);
        viewHolder.circle.setVisibility(View.INVISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
      case VoiceRoomSeat.Status.FORBID:
        viewHolder.iv_user_status.setVisibility(View.VISIBLE);
        viewHolder.ivStatusHint.setVisibility(View.GONE);
        viewHolder.iv_user_status.setImageResource(R.drawable.seat_close);
        viewHolder.circle.setVisibility(View.INVISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
      case VoiceRoomSeat.Status.AUDIO_MUTED:
      case VoiceRoomSeat.Status.AUDIO_CLOSED_AND_MUTED:
        viewHolder.iv_user_status.setVisibility(View.GONE);
        viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
        viewHolder.ivStatusHint.setImageResource(R.drawable.audio_be_muted_status);
        viewHolder.circle.setVisibility(View.INVISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
      case VoiceRoomSeat.Status.AUDIO_CLOSED:
        viewHolder.iv_user_status.setVisibility(View.GONE);
        viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
        viewHolder.ivStatusHint.setImageResource(R.drawable.icon_seat_close_micro);
        viewHolder.circle.setVisibility(View.INVISIBLE);
        viewHolder.applying.setVisibility(View.GONE);
        break;
    }

    viewHolder.tvNick.setText(String.valueOf(position + 1));

    if (user != null) {
      if (user.isMute()) {
        viewHolder.ivStatusHint.setImageResource(R.drawable.audio_be_muted_status);
      } else {
        viewHolder.ivStatusHint.setImageResource(R.drawable.icon_seat_open_micro);
        viewHolder.waveView.start();
      }

      if (SeatUtils.isSinging(songModel, user.getAccount())) {
        viewHolder.tvNick.setText(context.getText(R.string.karaoke_singing));
      } else if (KaraokeUtils.isHost(user.getAccount())) {
        viewHolder.tvNick.setText(context.getText(R.string.karaoke_host));
      }
    }

    if (user != null && status == VoiceRoomSeat.Status.APPLY) { //请求麦位
      viewHolder.ivAvatar.loadAvatar(user.getAvatar());
      viewHolder.ivAvatar.setVisibility(View.VISIBLE);
      viewHolder.avatarBg.setVisibility(View.VISIBLE);
    } else if (user != null && seat.isOn()) { //麦上有人
      viewHolder.ivAvatar.loadAvatar(user.getAvatar());
      viewHolder.ivAvatar.setVisibility(View.VISIBLE);
      viewHolder.avatarBg.setVisibility(View.VISIBLE);
    } else {
      viewHolder.circle.setVisibility(View.INVISIBLE);
      viewHolder.ivAvatar.setVisibility(View.INVISIBLE);
      viewHolder.avatarBg.setVisibility(View.INVISIBLE);
    }

    viewHolder.ivUserSinging.setVisibility(View.GONE);
  }

  @SuppressLint("NotifyDataSetChanged")
  public void updateSongModel(NEKaraokeSongModel songModel) {
    this.songModel = songModel;
    notifyDataSetChanged();
  }
}
