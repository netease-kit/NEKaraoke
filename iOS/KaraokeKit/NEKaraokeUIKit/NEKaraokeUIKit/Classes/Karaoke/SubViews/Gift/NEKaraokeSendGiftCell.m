// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSendGiftCell.h"
#import "NEKaraokeLocalized.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"
@import NESocialUIKit;

@interface NEKaraokeSendGiftCell ()

@property(nonatomic, strong) UIView *bgView;
@property(nonatomic, strong) UIImageView *icon;
@property(nonatomic, strong) UILabel *info;

@end

@implementation NEKaraokeSendGiftCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.contentView.backgroundColor = [UIColor clearColor];
    [self.contentView addSubview:self.bgView];
    [self.bgView addSubview:self.icon];
    [self.bgView addSubview:self.info];
  }
  return self;
}

- (void)layoutSubviews {
  self.bgView.frame = CGRectMake(4, 20, 72, 100);
  self.icon.frame = CGRectMake(16, 8, 40, 40);
  self.info.frame =
      CGRectMake(0, self.icon.frame.size.height + self.icon.frame.origin.y + 4, 72, 40);
}

- (void)setSelected:(BOOL)selected {
  if (selected) {
    self.bgView.layer.borderColor = [UIColor whiteColor].CGColor;
    self.bgView.layer.borderWidth = 0.5;
  } else {
    self.bgView.layer.borderWidth = 0;
  }
}

- (void)installWithModel:(NESocialGiftModel *)model {
  self.icon.image = model.icon;
  self.info.attributedText = [self descriptionWithGift:model];
  self.info.textAlignment = NSTextAlignmentCenter;
}

- (NSAttributedString *)descriptionWithGift:(NESocialGiftModel *)gift {
  NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
  style.minimumLineHeight = 20;
  style.maximumLineHeight = 20;

  NSDictionary *displayDic = @{
    NSFontAttributeName : [UIFont systemFontOfSize:13],
    NSForegroundColorAttributeName : [UIColor whiteColor],
    NSParagraphStyleAttributeName : style
  };
  NSDictionary *priceDic = @{
    NSFontAttributeName : [UIFont systemFontOfSize:12],
    NSForegroundColorAttributeName : [UIColor karaoke_colorWithHex:0x666666],
    NSParagraphStyleAttributeName : style
  };
  NSMutableAttributedString *res =
      [[NSMutableAttributedString alloc] initWithString:gift.displayName attributes:displayDic];
  NSAttributedString *price = [[NSAttributedString alloc]
      initWithString:[NSString stringWithFormat:NELocalizedString(@"\n(%zd云币)"), gift.price]
          attributes:priceDic];
  [res appendAttributedString:price];
  return [res copy];
}

+ (NEKaraokeSendGiftCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                        indexPath:(NSIndexPath *)indexPath
                                            datas:(NSArray<NESocialGiftModel *> *)datas {
  NEKaraokeSendGiftCell *cell =
      [collectionView dequeueReusableCellWithReuseIdentifier:[NEKaraokeSendGiftCell description]
                                                forIndexPath:indexPath];
  if ([datas count] > indexPath.row) {
    NESocialGiftModel *gift = datas[indexPath.row];
    [cell installWithModel:gift];
  }
  return cell;
}

/// 计算直播列表页cell size
+ (CGSize)size {
  return CGSizeMake(80, 136);
}

#pragma mark - lazy load

- (UIView *)bgView {
  if (!_bgView) {
    _bgView = [[UIView alloc] init];
    _bgView.layer.cornerRadius = 4;
    _bgView.layer.masksToBounds = YES;
  }
  return _bgView;
}

- (UIImageView *)icon {
  if (!_icon) {
    _icon = [[UIImageView alloc] init];
  }
  return _icon;
}

- (UILabel *)info {
  if (!_info) {
    _info = [[UILabel alloc] init];
    _info.numberOfLines = 2;
  }
  return _info;
}

@end
