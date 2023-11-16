// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN

@protocol NEKaraokeSeatListVCDelegate <NSObject>
@optional
- (void)cancelRequestSeat:(NEKaraokeSeatRequestItem *)item;
- (void)rejectRequestSeat:(NEKaraokeSeatRequestItem *)item;
- (void)allowRequestSeat:(NEKaraokeSeatRequestItem *)item;
@end

@interface NEKaraokeSeatListVC : UIViewController
@property(nonatomic, weak) id<NEKaraokeSeatListVCDelegate> delegate;
@property(nonatomic, assign) BOOL isHost;
@end

NS_ASSUME_NONNULL_END
