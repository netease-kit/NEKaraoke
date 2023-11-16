// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import <UIKit/UIKit.h>
#import <YYText/YYText.h>

NS_ASSUME_NONNULL_BEGIN

@class NELyricCell;

@protocol NELyricCellDelegate <NSObject>

- (NSInteger)timeForYrcView:(NELyricCell *)yrcView;

@end

/// 界面更新模式
typedef NS_ENUM(NSInteger, NELyricUpdateType) {
  /// 逐行更新
  NELyricUpdateTypeLine,
  /// 逐字更新
  NELyricUpdateTypeWord,
};

@interface NELyricCell : UIView

@property(nonatomic, copy) void (^presentViewUpdateHandler)(__kindof UIView *view, NSInteger line);

@property(nonatomic, copy) void (^backgroundLabelUpdateHandler)(YYLabel *label);
@property(nonatomic, assign) BOOL notNeedShadow;

@property(nonatomic, strong, readonly) NELyricLine *yrcLineModel;
@property(nonatomic, weak) id<NELyricCellDelegate> delegate;

/// 传入逐行model，model需要提前计算布局
/// @param lineModel 传入逐行model
- (void)reloadWithYrcLineModel:(NELyricLine *)lineModel type:(NELyricUpdateType)type;

- (void)startAnimation;
- (void)pauseAnimation;
- (void)resetAnimation;

@end

NS_ASSUME_NONNULL_END
