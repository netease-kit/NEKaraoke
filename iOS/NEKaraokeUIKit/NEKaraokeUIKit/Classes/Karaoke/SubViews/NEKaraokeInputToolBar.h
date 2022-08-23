// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

///
/// 直播过程 底部工具条
/// 固定高度 36
///
typedef NS_ENUM(NSUInteger, NEKaraokeInputToolBarAction) {
  NEKaraokeInputToolBarActionInput,
  NEKaraokeInputToolBarActionMute,
  NEKaraokeInputToolBarActionUnmute,
  NEKaraokeInputToolBarActionChooseSong,
  NEKaraokeInputToolBarActionSeat,
  NEKaraokeInputToolBarActionGift,
};

typedef NS_ENUM(NSInteger, NEKaraokeInputToolBarSeatType) {
  NEKaraokeInputToolBarSeatTypeOn,
  NEKaraokeInputToolBarSeatTypeDown,
  NEKaraokeInputToolBarSeatTypeOption
};

@protocol NEKaraokeInputToolBarDelegate <NSObject>

///
/// 触发工具条动作
/// @param action   - 动作事件
///
- (void)clickInputToolBarAction:(NEKaraokeInputToolBarAction)action;

@end

@interface NEKaraokeInputToolBar : UIView

@property(nonatomic, weak) id<NEKaraokeInputToolBarDelegate> delegate;

- (instancetype)initWithFrame:(CGRect)frame showGift:(BOOL)showGift;

- (void)setMicBtnSelected:(BOOL)selected;

/// 取消第一响应
- (void)resignFirstResponder;

- (void)configSeatWithType:(NEKaraokeInputToolBarSeatType)type;
/// 麦位未读数字
- (void)configSeatUnreadNumber:(NSInteger)number;
/// 点歌未读数
- (void)configPickSongUnreadNumber:(NSInteger)number;

/// 是否展示麦克风
- (void)isShowMicBtn:(BOOL)flag;

@end

NS_ASSUME_NONNULL_END
