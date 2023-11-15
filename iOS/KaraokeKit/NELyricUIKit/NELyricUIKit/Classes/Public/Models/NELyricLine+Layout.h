// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import <NELyricUIKit/NELyricLineLayout.h>

NS_ASSUME_NONNULL_BEGIN

@interface NELyricPresentLine : NSObject

@property(nonatomic, strong) NSArray<NELyricWord *> *words;
@property(nonatomic, assign) CGRect lineFrame;

@end

@interface NELyricWord (Layout)

@property(nonatomic, assign) CGRect cacheFrame;
@property(nonatomic, weak) NELyricPresentLine *lineModel;

@end

@interface NELyricLine (Layout)

@property(nonatomic, strong) NELyricLineLayout *layoutInfo;

- (void)asynLayoutSizeWithMaxSize:(CGSize)maxSize sizeBlock:(void (^)(CGSize size))sizeBlock;

// 逐字歌词专用

@property(nonatomic, strong) NSArray<NELyricPresentLine *> *presentLines;

- (NELyricPresentLine *)presentLineForWord:(NELyricWord *)word;

/// 计算每一个word单位frame 且自动切行
/// @param width 最大宽度
/// @param complete 完成后的size
- (void)preLayoutWithMaxWidth:(CGFloat)width complete:(void (^)(CGSize size))complete;

@end

NS_ASSUME_NONNULL_END
