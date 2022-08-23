// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN

@protocol NEKaraokeSeatItemCellDelegate <NSObject>
@optional
- (void)didSelectedCell:(NEKaraokeSeatItem *)seatItem;
@end

@interface NEKaraokeSeatItemCell : UICollectionViewCell
@property(nonatomic, weak) id<NEKaraokeSeatItemCellDelegate> delegate;
- (void)configCellWithInfo:(nullable NEKaraokeSeatItem *)seatItem
                      name:(NSString *)name
                 songModel:(NEKaraokeSongModel *)songModel;
@end

NS_ASSUME_NONNULL_END
