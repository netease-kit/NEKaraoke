// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, NEKaraokeControlEventType) {
  // 调音
  NEKaraokeControlEventTypeVoice,
  // 暂停
  NEKaraokeControlEventTypePause,
  // 恢复播放
  NEKaraokeControlEventTypeResume,
  // 切歌
  NEKaraokeControlEventTypeSwitch,
  // 切到原唱
  NEKaraokeControlEventTypeOriginal,
  // 切到伴奏
  NEKaraokeControlEventTypeAccompany
};

@protocol NEKaraokeControlViewDelegate <NSObject>

- (void)onControlEvent:(NEKaraokeControlEventType)type;

@end

@interface NEKaraokeControlView : UIView

@property(nonatomic, weak) id<NEKaraokeControlViewDelegate> delegate;

@property(nonatomic, assign, readonly) BOOL isVoiceEnabled;
@property(nonatomic, assign, readonly) BOOL isPauseEnabled;
@property(nonatomic, assign, readonly) BOOL isPauseSelected;
@property(nonatomic, assign, readonly) BOOL isSwitchEnabled;
@property(nonatomic, assign, readonly) BOOL isOrginalEnabled;
@property(nonatomic, assign, readonly) BOOL isOrginalSelected;

/// 启用禁用调音按钮
/// @param enable 是否启用
- (void)enableVoice:(BOOL)enable;

/// 启用禁用暂停按钮
/// @param enable 是否启用
- (void)enablePause:(BOOL)enable;

/// 选中暂停按钮
/// @param selected 是否选中
- (void)selectPause:(BOOL)selected;

/// 启用禁用切歌按钮
/// @param enable 是否启用
- (void)enableSwitch:(BOOL)enable;

/// 启用禁用原唱按钮
/// @param enable 是否启用
- (void)enableOrginal:(BOOL)enable;

/// 选中原唱伴奏切换按钮
/// @param selected 是否选中
- (void)selectOrginal:(BOOL)selected;
/// 状态重置
- (void)reset;

@end

NS_ASSUME_NONNULL_END
