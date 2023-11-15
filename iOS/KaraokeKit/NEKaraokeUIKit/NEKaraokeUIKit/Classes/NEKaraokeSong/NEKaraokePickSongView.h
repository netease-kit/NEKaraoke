// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN
@protocol NEKaraokePickSongViewProtocol <NSObject>

@end

typedef bool (^IsUserOnSeat)(void);
typedef void (^ApplyOnSeat)(void);

@import NEKaraokeKit;
@interface NEKaraokePickSongView : UIView

- (instancetype)initWithFrame:(CGRect)frame detail:(NEKaraokeRoomInfo *)detail;

@property(nonatomic, copy) IsUserOnSeat isUserOnSeat;
@property(nonatomic, copy) ApplyOnSeat applyOnseat;

// 申请连麦相关
- (void)cancelApply;
- (void)applyFaile;
- (void)applySuccess;

@end

NS_ASSUME_NONNULL_END
