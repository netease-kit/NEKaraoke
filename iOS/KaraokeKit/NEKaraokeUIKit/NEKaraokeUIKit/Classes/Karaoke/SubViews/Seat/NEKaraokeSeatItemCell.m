// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatItemCell.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeSeatItemView.h"
@interface NEKaraokeSeatItemCell () <NEKaraokeSeatItemViewDelegate>
@property(nonatomic, strong) NEKaraokeSeatItemView *itemView;
@property(nonatomic, strong) NEKaraokeSeatItem *seatItem;
@end

@implementation NEKaraokeSeatItemCell
- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setupSubviews];
    [self makeConstrains];
  }
  return self;
}
- (void)setupSubviews {
  [self.contentView addSubview:self.itemView];
}
- (void)makeConstrains {
  [self.itemView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.mas_equalTo(UIEdgeInsetsZero);
  }];
}
- (void)configCellWithInfo:(nullable NEKaraokeSeatItem *)seatItem
                      name:(NSString *)name
                 songModel:(NEKaraokeSongInfoModel *)songModel {
  self.seatItem = seatItem;
  [self.itemView configSeatItem:seatItem name:name songModel:songModel];
}
#pragma mark-----------------------------  NEKaraokeSeatItemViewDelegate  -----------------------------
- (void)didSelectedSeat:(NEKaraokeSeatItemView *)itemView seatItem:(NEKaraokeSeatItem *)seatItem {
  if (self.delegate && [self.delegate respondsToSelector:@selector(didSelectedCell:)]) {
    [self.delegate didSelectedCell:seatItem];
  }
}

#pragma mark-----------------------------  Getter  -----------------------------
- (NEKaraokeSeatItemView *)itemView {
  if (!_itemView) {
    _itemView = [[NEKaraokeSeatItemView alloc] initWithFrame:CGRectZero];
    _itemView.delegate = self;
  }
  return _itemView;
}
@end
