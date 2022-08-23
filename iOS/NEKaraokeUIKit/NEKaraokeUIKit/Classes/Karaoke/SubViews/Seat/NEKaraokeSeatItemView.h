// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NEKaraokeAnimationButton.h"
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN
@class NEKaraokeSeatItemView;
@protocol NEKaraokeSeatItemViewDelegate <NSObject>
@optional
- (void)didSelectedSeat:(NEKaraokeSeatItemView *)itemView
               seatItem:(nullable NEKaraokeSeatItem *)seatItem;
@end

@interface NEKaraokeSeatItemView : UIView
@property(nonatomic, weak) id<NEKaraokeSeatItemViewDelegate> delegate;
@property(nonatomic, strong) UIImageView *avatarImageView;
@property(nonatomic, strong) UIImageView *microphoneImage;

@property(nonatomic, strong) UILabel *nameLabel;
@property(nonatomic, strong) NEKaraokeAnimationButton *connectBtn;

- (void)configSeatItem:(NEKaraokeSeatItem *)seatItem
                  name:(NSString *)name
             songModel:(NEKaraokeSongModel *)songModel;
@end

NS_ASSUME_NONNULL_END
