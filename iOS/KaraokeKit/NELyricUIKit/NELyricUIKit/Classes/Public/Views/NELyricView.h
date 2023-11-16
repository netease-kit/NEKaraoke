// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import <NELyricUIKit/NELyricCell.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
  NELyricViewTypeVideo,
  // 合唱
  NELyricViewTypeChorus,
  // 独唱
  NELyricViewTypeLyric,
  NELyricViewTypeLyricPreview,
  NELyricViewTypeLyricOther,
} NELyricViewType;

FOUNDATION_EXTERN CGFloat NELyricViewCellPadding(NELyricViewType type);
FOUNDATION_EXTERN CGFloat NELyricViewCellBottom(NELyricViewType type);
FOUNDATION_EXTERN CGFloat NELyricViewCellTop(NELyricViewType type);
FOUNDATION_EXTERN CGFloat NELyricViewCellScale(NELyricViewType type);

@interface NELyricView : UIView

- (instancetype)initWithFrame:(CGRect)frame type:(NELyricViewType)type;

@property(nonatomic, assign, readonly) NSInteger lastSelect;
@property(nonatomic, copy) NSInteger (^timeForCurrent)(void);
@property(nonatomic, copy) NSInteger (^waitTimeForCurrent)(void);

// 仅当 NELyricViewTypeLyricPreview 有效
@property(nonatomic, assign) CGFloat insetHeight;
@property(nonatomic, strong, readonly) NSArray<NELyricLine *> *lyricList;
@property(nonatomic, copy) void (^timeAtInterlude)(NSInteger leftTime);

@property(nonatomic, assign) NELyricUpdateType updateType;
@property(nonatomic, assign) bool scrollEnabled;

- (void)seekToMove:(NSInteger)idx
    seekActionBlock:(void (^)(NSInteger targetTime))seekActionBlock
    doUIActionBlock:(void (^)(void))doUIActionBlock;

- (void)loadWithLyricModel:(NELyric *)model;

- (void)loadWithLyricModel:(NELyric *)model
                 startTime:(NSInteger)startTime
                   endTime:(NSInteger)endTime;

- (void)update;

- (CGFloat)lyricTopHeight;

@end

NS_ASSUME_NONNULL_END
