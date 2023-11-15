// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

// 合唱准备界面
@interface NEKaraokeChorusWaitView : UIView

@property(nonatomic, copy) NSString *songName;
@property(nonatomic, copy) NSString *mainUserName;
@property(nonatomic, copy) NSString *mainUserIcon;
@property(nonatomic, copy) NSString *attachUserName;
@property(nonatomic, copy) NSString *attachUserIcon;

- (void)accompanyLoading:(BOOL)isLoading;

@end

NS_ASSUME_NONNULL_END
