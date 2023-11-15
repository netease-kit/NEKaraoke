// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

// 独唱准备界面
@interface NEKaraokeWaitView : UIView

@property(nonatomic, copy) NSString *songName;
@property(nonatomic, copy) NSString *userName;
@property(nonatomic, copy) NSString *userIcon;

- (void)updateTime:(NSInteger)time;

@end

NS_ASSUME_NONNULL_END
