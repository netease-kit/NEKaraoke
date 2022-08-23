// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN
@class NEKaraokeSeatView;
@protocol NEKaraokeSeatViewDelegate <NSObject>
@optional
- (void)didSelected:(NEKaraokeSeatView *)seatView seatItem:(NEKaraokeSeatItem *)seatItem;
@end

@interface NEKaraokeSeatView : UIView
@property(nonatomic, weak) id<NEKaraokeSeatViewDelegate> delegate;
- (void)configWithSeatItems:(NSArray<NEKaraokeSeatItem *> *)items hostUuid:(NSString *)uuid;
- (void)configSongModel:(NEKaraokeSongModel *)songModel;
@end

NS_ASSUME_NONNULL_END
