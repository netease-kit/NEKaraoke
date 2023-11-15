// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@import NESocialUIKit;

NS_ASSUME_NONNULL_BEGIN

@protocol NEKaraokeSendGiftViewtDelegate <NSObject>

- (void)didSendGift:(NESocialGiftModel *)gift;

@end

@interface NEKaraokeSendGiftViewController : UIViewController

+ (void)showWithTarget:(id<NEKaraokeSendGiftViewtDelegate>)target
        viewController:(UIViewController *)viewController;

@end

NS_ASSUME_NONNULL_END
