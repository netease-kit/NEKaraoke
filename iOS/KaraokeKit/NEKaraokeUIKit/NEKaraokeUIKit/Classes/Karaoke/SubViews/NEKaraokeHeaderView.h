// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol NEKaraokeHeaderViewDelegate <NSObject>

- (void)onClose;

@end

@interface NEKaraokeHeaderView : UIView

@property(nonatomic, copy) NSString *title;

@property(nonatomic, assign) NSInteger count;

@property(nonatomic, weak) id<NEKaraokeHeaderViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
