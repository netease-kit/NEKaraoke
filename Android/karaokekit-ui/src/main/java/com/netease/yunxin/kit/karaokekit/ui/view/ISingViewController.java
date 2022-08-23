// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import com.netease.yunxin.kit.karaokekit.ui.model.LyricBusinessModel;

/** 唱歌前后的 各种操作，状态的接口 */
public interface ISingViewController {

  /// 显示歌词和打分。只有独唱时，主唱侧显示该状态
  void showLyricAndScore(LyricBusinessModel lyricBusinessModel);

  /// 合唱匹配成员的时候显示状态
  void matchingMember();

  /// 展示"轮到你了"对话框
  void showTurnToYouDialog();

  /// 初始化歌词组件
  void initLyricView();

  /// 更新歌词
  void updateLyric(long timestamp);

  //// 点击音效
  void clickEffectView();

  //// 点击切换原唱/伴音
  void clickSwitchToOriginalVolume();

  //// 切歌操作
  void clickNextSong();

  /// 播放或暂停
  void playOrPause();

  /// 合唱，发送合唱邀请（主唱） orderId 点歌编号
  void inviteChorus(long orderId);

  /// 同意 加入合唱（上麦的人用户）, chorusId 合唱id
  void agreeAddChorus(String chorusId);

  /// 是否是主唱
  boolean isAnchor();

  /// 是否是副唱
  boolean isAssistant();
}
