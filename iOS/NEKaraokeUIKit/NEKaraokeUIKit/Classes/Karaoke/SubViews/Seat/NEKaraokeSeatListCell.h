// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN

@interface NEKaraokeSeatListCell : UITableViewCell
@property(nonatomic, copy) void (^cancelBlock)(void);
@property(nonatomic, copy) void (^rejectBlock)(void);
@property(nonatomic, copy) void (^allowBlock)(void);
- (void)configCellWithItem:(NEKaraokeSeatRequestItem *)item
                     index:(NSInteger)index
                  isAnchor:(BOOL)isAnchor
                    isSelf:(BOOL)isSelf;
@end

NS_ASSUME_NONNULL_END
