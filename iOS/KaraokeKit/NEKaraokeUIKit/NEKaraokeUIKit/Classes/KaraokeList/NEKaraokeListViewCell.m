// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeListViewCell.h"
#import <Masonry/Masonry.h>
#import "UIImage+Karaoke.h"

@interface NEKaraokeListViewCell ()

/// 背景图
@property(nonatomic, strong) UIImageView *coverView;
/// 房间名称
@property(nonatomic, strong) UILabel *roomName;
/// 主播名称
@property(nonatomic, strong) UILabel *anchorName;
/// 观众人数
@property(nonatomic, strong) UIImageView *audienceNumIcon;
/// 观众人数
@property(nonatomic, strong) UILabel *audienceNum;
/// 上麦人数
@property(nonatomic, strong) UIImageView *seatNumIcon;
/// 上麦人数
@property(nonatomic, strong) UILabel *seatNum;

@end

@implementation NEKaraokeListViewCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setupViews];
  }
  return self;
}

- (void)setupViews {
  [self.contentView addSubview:self.coverView];
  [self.coverView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self.contentView);
  }];

  [self.contentView addSubview:self.roomName];
  [self.roomName mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.contentView).offset(14);
    make.left.equalTo(self.contentView).offset(14);
    make.height.mas_equalTo(16);
    make.width.equalTo(self.contentView);
  }];

  [self.contentView addSubview:self.anchorName];
  [self.anchorName mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.roomName.mas_bottom).offset(8);
    make.left.equalTo(self.contentView).offset(14);
    make.height.mas_equalTo(14);
    make.width.equalTo(self.contentView);
  }];

  [self.contentView addSubview:self.audienceNumIcon];
  [self.audienceNumIcon mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.contentView).offset(14);
    make.bottom.equalTo(self.contentView).offset(-14);
    make.height.mas_equalTo(14);
    make.width.mas_equalTo(14);
  }];

  [self.contentView addSubview:self.audienceNum];
  [self.audienceNum mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.audienceNumIcon.mas_right).offset(4);
    make.bottom.equalTo(self.contentView).offset(-14);
    make.height.mas_equalTo(14);
    make.width.mas_equalTo(30);
  }];

  [self.contentView addSubview:self.seatNumIcon];
  [self.seatNumIcon mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.audienceNum.mas_right).offset(4);
    make.bottom.equalTo(self.contentView).offset(-14);
    make.height.mas_equalTo(14);
    make.width.mas_equalTo(14);
  }];

  [self.contentView addSubview:self.seatNum];
  [self.seatNum mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.seatNumIcon.mas_right).offset(4);
    make.bottom.equalTo(self.contentView).offset(-14);
    make.height.mas_equalTo(14);
    make.width.mas_equalTo(30);
  }];
}

+ (NEKaraokeListViewCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                        indexPath:(NSIndexPath *)indexPath
                                            datas:(NSArray<NEKaraokeRoomInfo *> *)datas {
  if ([datas count] <= indexPath.row) {
    return [NEKaraokeListViewCell new];
  }

  NEKaraokeListViewCell *cell =
      [collectionView dequeueReusableCellWithReuseIdentifier:[NEKaraokeListViewCell description]
                                                forIndexPath:indexPath];
  NEKaraokeRoomInfo *model = datas[indexPath.row];
  cell.roomName.text = model.liveModel.liveTopic;
  cell.anchorName.text = model.anchor.userName;
  cell.audienceNum.text = [NSString stringWithFormat:@"%zd", model.liveModel.audienceCount + 1];
  cell.seatNum.text = [NSString stringWithFormat:@"%zd", model.liveModel.onSeatCount];
  int random = indexPath.row % 5;
  NSArray *images = @[ @"blue_back", @"gray_back", @"green_back", @"purple_back", @"red_back" ];
  cell.coverView.image = [UIImage karaoke_imageNamed:images[random]];
  return cell;
}

+ (CGSize)size {
  CGFloat length = (CGRectGetWidth([UIScreen mainScreen].bounds) - 8 * 3) / 2.0;
  return CGSizeMake((int)length, 131);
}

#pragma mark - getter

- (UIImageView *)coverView {
  if (!_coverView) {
    _coverView = [[UIImageView alloc] init];
    _coverView.contentMode = UIViewContentModeScaleAspectFill;
    _coverView.clipsToBounds = YES;
    _coverView.layer.cornerRadius = 8;
    _coverView.layer.masksToBounds = YES;
    _coverView.image = [UIImage karaoke_imageNamed:@"gray_back"];
  }
  return _coverView;
}

- (UILabel *)roomName {
  if (!_roomName) {
    _roomName = [[UILabel alloc] init];
    _roomName.textColor = [UIColor whiteColor];
    _roomName.font = [UIFont fontWithName:@"PingFangSC-Medium" size:14];
  }
  return _roomName;
}

- (UILabel *)anchorName {
  if (!_anchorName) {
    _anchorName = [[UILabel alloc] init];
    _anchorName.textColor = [UIColor whiteColor];
    _anchorName.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
  }
  return _anchorName;
}

- (UIImageView *)audienceNumIcon {
  if (!_audienceNumIcon) {
    _audienceNumIcon = [[UIImageView alloc] init];
    _audienceNum.contentMode = UIViewContentModeScaleAspectFit;
    _audienceNumIcon.image = [UIImage karaoke_imageNamed:@"audience_num"];
  }
  return _audienceNumIcon;
}

- (UILabel *)audienceNum {
  if (!_audienceNum) {
    _audienceNum = [[UILabel alloc] init];
    _audienceNum.textColor = [UIColor whiteColor];
    _audienceNum.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _audienceNum.text = @"500";
  }
  return _audienceNum;
}

- (UIImageView *)seatNumIcon {
  if (!_seatNumIcon) {
    _seatNumIcon = [[UIImageView alloc] init];
    _seatNumIcon.contentMode = UIViewContentModeScaleAspectFit;
    _seatNumIcon.image = [UIImage karaoke_imageNamed:@"seat_num"];
  }
  return _seatNumIcon;
}

- (UILabel *)seatNum {
  if (!_seatNum) {
    _seatNum = [[UILabel alloc] init];
    _seatNum.textColor = [UIColor whiteColor];
    _seatNum.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _seatNum.text = @"500";
  }
  return _seatNum;
}

@end
