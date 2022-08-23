// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

// 合唱匹配界面
@interface NEKaraokeMatchView : UIView

@property(nonatomic, strong) UIImage *userIcon;
@property(nonatomic, copy) NSString *songName;
@property(nonatomic, assign) BOOL joinEnable;

@property(nonatomic, copy) void (^toSolo)(void);
@property(nonatomic, copy) void (^join)(void);

- (void)updateTime:(NSInteger)time;
- (void)setSoloBtnHidden:(BOOL)hidden;
- (void)setJoinBtnHidden:(BOOL)hidden;

@end

NS_ASSUME_NONNULL_END
