// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.ui;

public class TestAccessHelper {

  // 右上角退出房间按钮
  public static int getLeaveRoomId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_leave_room;
  }

  // 底部上麦、排麦按钮
  public static int getArrangeMicroId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_arrange_micro;
  }

  // 底部上麦、排麦按钮 下麦
  public static int getArrangeDownMicroDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.down_mircro;
  }

  // 底部上麦、排麦按钮 上麦
  public static int getArrangeOnMicroDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.on_micro;
  }

  // 底部上麦、排麦按钮 主持人 排麦
  public static int getArrangeMicroDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.arrange_micro;
  }

  // 底部麦克风按钮
  public static int getLocalAudioSwitchId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_local_audio_switch;
  }

  // 底部消息发送
  public static int getTVRoomMsgInputId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.tv_room_msg_input;
  }

  // 消息输入框
  public static int getEVRoomMsgInputId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.et_room_msg_input;
  }

  // 底部点歌按钮
  public static int getIvMusicId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_music;
  }

  // 弹窗点歌台列表
  public static int getOrderSongRecyclerViewId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.recyclerView;
  }

  // 弹窗点歌台点歌按钮
  public static int getOrderSongId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.order_song;
  }

  // 播放/暂停歌曲
  public static int getIvPauseId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_pause;
  }

  // 暂停播放的icon
  public static int getIvResumeDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.resume_icon;
  }

  // 正在播放的icon
  public static int getIvMusicDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.icon_music_state_switch;
  }

  // 切换原唱/伴唱
  public static int getIvSwitchOriginId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_switch_origin;
  }

  // 原唱状态的icon
  public static int getIvOriginDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.icon_switch_origin_open;
  }

  // 伴唱状态的icon
  public static int getIvAccompDrawable() {
    return com.netease.yunxin.kit.karaokekit.ui.R.drawable.icon_switch_origin_colse;
  }

  // 切歌的icon
  public static int getIvNextSongId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.iv_next_music;
  }

  // 加入合唱按钮
  public static int getIvJoinChorusId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.solo;
  }

  // 账号输入框
  public static int getEtLoginEditPhoneId() {
    return com.netease.yunxin.kit.login.R.id.phone_login_edit_phone_et;
  }

  // 验证码输入框
  public static int getEtLoginEditSMSCodeId() {
    return com.netease.yunxin.kit.login.R.id.phone_login_edit_sms_code_et;
  }

  // 登录按钮
  public static int getBtnLoginRegisterLoginId() {
    return com.netease.yunxin.kit.login.R.id.phone_login_btn_register_login;
  }

  // 无歌曲状态 UI
  public static int getNoOrderedSongId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.lly_no_ordered_song;
  }

  // 演唱区 Id
  public static int getSingControlViewId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.sing_control_view;
  }

  // 演唱区 独唱按钮 Id
  public static int getBtnSoloId() {
    return com.netease.yunxin.kit.karaokekit.ui.R.id.solo;
  }
}
