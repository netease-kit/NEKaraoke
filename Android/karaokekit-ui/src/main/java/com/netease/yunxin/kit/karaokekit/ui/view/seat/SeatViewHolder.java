// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view.seat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.netease.yunxin.kit.common.ui.widgets.WaveView;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.view.HeadImageView;

public class SeatViewHolder extends RecyclerView.ViewHolder {
  HeadImageView ivAvatar;
  WaveView waveView;
  ImageView ivStatusHint;
  ImageView iv_user_status;
  TextView tvNick;
  ImageView circle;
  ImageView ivUserSinging;
  View avatarBg;
  LottieAnimationView applying;

  SeatViewHolder(@NonNull View itemView) {
    super(itemView);
    ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
    waveView = itemView.findViewById(R.id.circle_wave);
    ivStatusHint = itemView.findViewById(R.id.iv_user_status_hint);
    tvNick = itemView.findViewById(R.id.tv_user_nick);
    iv_user_status = itemView.findViewById(R.id.iv_user_stats);
    circle = itemView.findViewById(R.id.circle);
    ivUserSinging = itemView.findViewById(R.id.iv_user_singing);
    avatarBg = itemView.findViewById(R.id.avatar_bg);
    applying = itemView.findViewById(R.id.lav_apply);
  }
}
