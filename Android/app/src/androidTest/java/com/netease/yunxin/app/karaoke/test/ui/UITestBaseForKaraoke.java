// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.test.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.netease.yunxin.app.karaoke.test.ui.CommonUITest.checkLocalTextViewTopDrawable;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.*;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.getArrangeMicroId;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.getEVRoomMsgInputId;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.getIvPauseId;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.getLocalAudioSwitchId;
import static com.netease.yunxin.app.karaoke.test.ui.TestAccessHelper.getOrderSongRecyclerViewId;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.assertViewExist;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.checkLocalDrawable;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.checkText;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.clickViewOnRecyclerViewItem;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.isViewDisplayed;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.viewAction;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.viewClick;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.waitForTime;
import static com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils.waitForView;
import static org.hamcrest.Matchers.anyOf;

import androidx.test.espresso.action.ViewActions;
import com.netease.yunxin.kit.integrationtest.uitest.ClassRegister;
import com.netease.yunxin.kit.integrationtest.uitest.UITestRegisterHelper;
import com.netease.yunxin.kit.integrationtest.utils.BaseTestUtils;

@ClassRegister // 该注解表示被注册的类
public class UITestBaseForKaraoke {

  // 点击图标、输入房间名、点击创建
  public static void createRoom(String roomName, int type) {
    viewClick(com.netease.yunxin.app.karaoke.R.id.iv_new_live);
    viewAction(com.netease.yunxin.app.karaoke.R.id.et_room_name, ViewActions.replaceText(roomName));
    if (type == 1) {
      viewClick(com.netease.yunxin.app.karaoke.R.id.rb_serial_chorus);
    } else if (type == 2) {
      viewClick(com.netease.yunxin.app.karaoke.R.id.rb_realtime_chorus);
    }
    viewClick(com.netease.yunxin.app.karaoke.R.id.tv_create_room);
    waitForTime(5000);
  }

  // 进入指定房间
  public static void joinRoom(String roomName) {
    // 执行刷新
    UITestRegisterHelper.getDevice().swipe(500, 600, 500, 1800, 10);
    waitForTime(1000);
    viewClick(roomName);
  }

  // 点歌
  public static void orderSong(int position, boolean isSolo) {
    viewClick(getIvMusicId());
    waitForTime(1000);
    clickViewOnRecyclerViewItem(getOrderSongRecyclerViewId(), position, getOrderSongId());
    BaseTestUtils.pressBack();
    if (isSolo) {
      waitForView(anyOf(withText("确定"), withId(getBtnSoloId())), 20000, 200);
      if (isViewDisplayed(onView(withText("确定")))) viewClick("确定");
      else viewClick("独唱");
    }
  }

  // 申请上麦
  public static void requestSeat() {
    checkLocalDrawable(getArrangeMicroId(), getArrangeOnMicroDrawable());
    viewClick(getArrangeMicroId());
    viewClick("确定");
  }

  // 允许上麦
  public static void approveRequestSeat() {
    checkLocalDrawable(getArrangeMicroId(), getArrangeMicroDrawable());
    viewClick(getArrangeMicroId());
    viewClick("允许上麦");
    //	onView(allOf(withText("允许上麦"), hasSibling(withText(account)))).perform(click());
  }

  // 拒绝上麦
  public static void rejectRequestSeat() {
    checkLocalDrawable(getArrangeMicroId(), getArrangeMicroDrawable());
    viewClick(getArrangeMicroId());
    viewClick("拒绝");
    //    onView(allOf(withText("拒绝"), hasSibling(withText(account)))).perform(click());
  }

  // 下麦
  public static void leaveSeat() {
    checkLocalDrawable(getArrangeMicroId(), getArrangeDownMicroDrawable());
    viewClick(getArrangeMicroId());
    viewClick("确定");
  }

  // 取消上麦
  public static void cancelRequestSeat() {
    checkLocalDrawable(getArrangeMicroId(), getArrangeMicroDrawable());
    viewClick(getArrangeMicroId());
    viewClick("取消上麦");
  }

  // 离开房间
  public static void leaveRoom() {
    viewClick(getLeaveRoomId());
    if (isViewDisplayed(onView(withText("确定")))) viewClick("确定");
  }

  // 结束房间
  public static void endRoom() {
    viewClick(getLeaveRoomId());
    viewClick("确定");
  }

  // 发送聊天室消息
  public static void sendTextMessage(String content) {
    viewClick(getTVRoomMsgInputId());
    viewAction(getEVRoomMsgInputId(), ViewActions.replaceText(content));
    onView(withId(getEVRoomMsgInputId())).perform(pressImeActionButton());
  }

  // 关闭自己麦克风
  public static void muteMyAudio() {
    viewClick(getLocalAudioSwitchId());
  }

  // 打开自己的麦克风
  public static void unmuteMyAudio() {
    viewClick(getLocalAudioSwitchId());
  }

  // 加入合唱
  public static void joinChorus() {
    waitForView(getIvJoinChorusId(), 20000, 200);
    viewClick(getIvJoinChorusId());
  }

  // 暂停歌曲
  public static void requestPausePlayingSong() {
    waitForTime(3000);
    viewClick(getIvPauseId());
    assertResumePlayingSongState();
  }

  // 恢复播放
  public static void requestResumePlayingSong() {
    waitForTime(3000);
    viewClick(getIvPauseId());
    assertMusicPlayingSongState();
  }

  // 暂停播放断言
  public static void assertResumePlayingSongState() {
    checkLocalTextViewTopDrawable(getIvPauseId(), getIvResumeDrawable());
    checkText(getIvPauseId(), "播放");
  }

  // 正在播放断言
  public static void assertMusicPlayingSongState() {
    checkLocalTextViewTopDrawable(getIvPauseId(), getIvMusicDrawable());
    checkText(getIvPauseId(), "暂停");
  }

  // 切换原唱/伴唱
  public static void switchAccompaniment() {
    viewClick(getIvSwitchOriginId());
  }

  // 原唱状态断言
  public static void assertOriginState() {
    checkLocalTextViewTopDrawable(getIvSwitchOriginId(), getIvOriginDrawable());
  }

  // 伴唱状态断言
  public static void assertAccompState() {
    checkLocalTextViewTopDrawable(getIvSwitchOriginId(), getIvAccompDrawable());
  }

  // 切歌
  public static void nextSong() {
    viewClick(getIvNextSongId());
  }

  // 列表页状态断言
  public static void assertRoomListPageState() {
    assertViewExist(com.netease.yunxin.app.karaoke.R.id.iv_new_live);
  }

  // 登录
  public static void login(String phone, String smsCode) {
    // 如果在首页，先执行logout操作
    if (isViewDisplayed(onView(withId(com.netease.yunxin.app.karaoke.R.id.iv_new_live)))) logout();
    viewAction(getEtLoginEditPhoneId(), ViewActions.replaceText(phone));
    viewAction(getEtLoginEditSMSCodeId(), ViewActions.replaceText(smsCode));
    viewClick(getBtnLoginRegisterLoginId());
  }

  // 登出
  public static void logout() {
    viewClick("我的");
    viewClick("退出登录");
    viewClick("是");
  }

  // 断言无歌曲状态
  public static void assertNoOrderedSongState() {
    waitForTime(2000);
    isViewDisplayed(onView(withId(getNoOrderedSongId())));
  }

  // 断言有歌曲状态
  public static void assertHasOrderedSongState() {
    waitForTime(2000);
    isViewDisplayed(onView(withId(getSingControlViewId())));
  }
}
