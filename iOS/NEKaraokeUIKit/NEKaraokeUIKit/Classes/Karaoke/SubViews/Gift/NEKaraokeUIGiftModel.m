// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeUIGiftModel.h"

@implementation NEKaraokeUIGiftModel

- (instancetype)initWithGiftId:(int32_t)giftId
                          icon:(NSString *)icon
                       display:(NSString *)display
                         price:(int32_t)price {
  self = [super init];
  if (self) {
    _giftId = giftId;
    _icon = icon;
    _display = display;
    _price = price;
  }
  return self;
}

+ (NSArray<NEKaraokeUIGiftModel *> *)defaultGifts {
  NEKaraokeUIGiftModel *gift1 = [[NEKaraokeUIGiftModel alloc] initWithGiftId:1
                                                                        icon:@"gift03_ico"
                                                                     display:@"荧光棒"
                                                                       price:9];
  NEKaraokeUIGiftModel *gift2 = [[NEKaraokeUIGiftModel alloc] initWithGiftId:2
                                                                        icon:@"gift04_ico"
                                                                     display:@"安排"
                                                                       price:99];
  NEKaraokeUIGiftModel *gift3 = [[NEKaraokeUIGiftModel alloc] initWithGiftId:3
                                                                        icon:@"gift02_ico"
                                                                     display:@"跑车"
                                                                       price:199];
  NEKaraokeUIGiftModel *gift4 = [[NEKaraokeUIGiftModel alloc] initWithGiftId:4
                                                                        icon:@"gift01_ico"
                                                                     display:@"火箭"
                                                                       price:999];
  return @[ gift1, gift2, gift3, gift4 ];
}

+ (nullable NEKaraokeUIGiftModel *)getRewardWithGiftId:(NSInteger)giftId {
  NEKaraokeUIGiftModel *gift = nil;
  for (NEKaraokeUIGiftModel *tmp in [NEKaraokeUIGiftModel defaultGifts]) {
    if (tmp.giftId == giftId) {
      gift = tmp;
      break;
    }
  }
  return gift;
}

@end
