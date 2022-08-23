// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeListEmptyView.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"

@interface NEKaraokeListEmptyView ()

@property(nonatomic, strong) UIImageView *imgView;
@property(nonatomic, strong) UILabel *tipLabel;

@end

@implementation NEKaraokeListEmptyView

- (instancetype)initWithFrame:(CGRect)frame {
  CGRect rect = CGRectMake(frame.origin.x, frame.origin.y, 100, 138);
  self = [super initWithFrame:rect];
  if (self) {
    [self addSubview:self.imgView];
    [self addSubview:self.tipLabel];

    self.imgView.frame = CGRectMake(0, 0, 100, 100);
    self.tipLabel.frame = CGRectMake(0, 100 + 16, 100, 22);
  }
  return self;
}

#pragma mark - lazy load

- (UIImageView *)imgView {
  if (!_imgView) {
    _imgView = [[UIImageView alloc] init];
    _imgView.image = [UIImage karaoke_imageNamed:@"empty_ico"];
  }
  return _imgView;
}

- (UILabel *)tipLabel {
  if (!_tipLabel) {
    _tipLabel = [[UILabel alloc] init];
    _tipLabel.font = [UIFont systemFontOfSize:14];
    _tipLabel.textColor = [UIColor karaoke_colorWithHex:0x2b2c39];
    _tipLabel.textAlignment = NSTextAlignmentCenter;
    _tipLabel.text = @"暂无直播哦";
  }
  return _tipLabel;
}

@end
