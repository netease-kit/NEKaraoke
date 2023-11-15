// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeViewController.h"
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, NEKaraokeAlertTitleType) {
  NEKaraokeAlertTitleTypeBlue,  // 蓝色
  NEKaraokeAlertTitleTypeGray
};

@interface NEKaraokeViewController (UI)
/// 背景视图
- (void)setupBgView;
/// 布局子视图
- (void)setupSubviews;
/// 监听键盘处理
- (void)observeKeyboard;
/// 是否展示控制视图
- (void)showControlView:(BOOL)flag;

/// alert 弹框 确认为蓝色
- (void)showAlert:(NSString *)title confirm:(NSString *)confirm block:(void (^)(void))block;
/// alert 弹框 取消为蓝色
- (void)showAlert:(NSString *)title cancel:(NSString *)confirm block:(void (^)(void))block;

/// 发送聊天室通知消息
- (void)sendChatroomNotifyMessage:(NSString *)content;

/// 控制视图展示
- (void)configControlView:(NEKaraokeSongMode)songMode;

/// 展示接受合唱 歌词模块
- (void)showAcceptChorusLyricModule;
/// 展示取消邀请合唱 歌词模块
- (void)showCancelInviteLyrricModule;
/// 展示无歌词页面
- (void)showNoLyricView:(NEKaraokeSongInfoModel *)songModel;
/// 获取点歌列表
- (void)fetchPickSongList;
@end

NS_ASSUME_NONNULL_END
