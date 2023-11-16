// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEAudioEffectKit/NEAudioEffectManager.h>
#import <NEAudioEffectUIKit/NEActionSheetNavigationController.h>
#import <NEAudioEffectUIKit/NEAudioEffectUIManager.h>
#import <NEAudioEffectUIKit/NEAudioEffectViewController.h>
#import <UIKit/UIKit.h>
#import "NEKaraokeAnimationView.h"
#import "NEKaraokeControlView.h"
#import "NEKaraokeHeaderView.h"
#import "NEKaraokeInputToolBar.h"
#import "NEKaraokeKeyboardToolbarView.h"
#import "NEKaraokeLyricActionView.h"
#import "NEKaraokeSeatListVC.h"
#import "NEKaraokeSeatView.h"
#import "NEKaraokeSendGiftViewController.h"
#import "NEKaraokeTaskQueue.h"
#import "NEKaraokeUIManager.h"
@import NESocialUIKit;

@import NEKaraokeKit;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, NEKaraokeViewRole) {
  // 主场
  NEKaraokeViewRoleHost,
  // 听众
  NEKaraokeViewRoleAudience,
  // 副唱，不支持在初始化时设置
  NEKaraokeViewRoleAuxiliary,
};
/// 麦位UI展示状态
typedef NS_ENUM(NSInteger, NEKaraokeSeatRequestType) {
  // 展示上麦
  NEKaraokeSeatRequestTypeOn,
  // 展示下麦
  NEKaraokeSeatRequestTypeDown,
  // 展示排麦
  NEKaraokeSeatRequestTypeApplying
};

@interface NEKaraokeViewController : UIViewController
@property(nonatomic, strong) NEKaraokeHeaderView *headerView;            // 头部
@property(nonatomic, strong) NEKaraokeLyricActionView *lyricActionView;  // 歌词及打分
@property(nonatomic, strong) NEKaraokeControlView *controlView;  // 调音、暂停、切歌、原唱
@property(nonatomic, strong) NEKaraokeSeatView *seatView;        // 麦位
@property(nonatomic, strong) NEKaraokeInputToolBar *bottomView;      // 底部工具栏
@property(nonatomic, strong) NESocialChatroomView *chatView;         // 聊天室消息列表
@property(nonatomic, strong) NEKaraokeKeyboardToolbarView *toolBar;  // 键盘工具条
@property(nonatomic, strong) NEKaraokeAnimationView *giftAnimation;  // 礼物动画

@property(nonatomic, assign) NEKaraokeViewRole role;  // 当前的角色
@property(nonatomic, strong) NEKaraokeRoomInfo *detail;

@property(nonatomic, assign) NSInteger time;

@property(nonatomic, strong) NEAudioEffectViewController *audioViewController;
@property(nonatomic, strong) NEAudioEffectManager *audioManager;

@property(nonatomic, strong) NSMutableArray<NEKaraokeSeatItem *> *seatItems;

/// 听众申请状态
@property(nonatomic, assign) NEKaraokeSeatRequestType seatRequestType;
/// 本地存储点歌台模型
@property(nonatomic, strong) NEKaraokeOrderSongResult *localOrderSong;
/// 合唱Id
@property(nonatomic, copy) NSString *chorusId;
/// 本地维持
@property(nonatomic, strong, nullable) NEKaraokeSongModel *songModel;

@property(nonatomic, strong) NEKaraokeTaskQueue *taskQueue;

@property(nonatomic, strong) UIAlertController *soloAlert;

- (instancetype)initWithRole:(NEKaraokeViewRole)role detail:(NEKaraokeRoomInfo *)detail;

@end

NS_ASSUME_NONNULL_END
