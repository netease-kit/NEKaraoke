// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeViewController.h"
NS_ASSUME_NONNULL_BEGIN

@interface NEKaraokeViewController (Seat)
/// 获取麦位信息
- (void)getSeatInfo;
/// 是否在麦上
- (BOOL)isOnSeat;
/// 麦位排序
- (void)sortSeatItems:(NSArray<NEKaraokeSeatItem *> *)items;
/// 获取麦位申请列表
- (void)fetchSeatRequestList;
@end

NS_ASSUME_NONNULL_END
