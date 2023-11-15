// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAESwitchView.h"
#import <Masonry/Masonry.h>
#import "UIColor+AudioEffect.h"

@implementation NEAESwitchView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.backgroundColor = [UIColor clearColor];
    [self loadUI];
  }
  return self;
}

- (void)loadUI {
  [self addSubview:self.titleLabel];
  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self);
    make.height.mas_equalTo(22);
    make.left.equalTo(self).offset(14);
    make.width.mas_equalTo(50);
  }];

  [self addSubview:self.detailLabel];
  [self.detailLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.titleLabel.mas_bottom);
    make.height.mas_equalTo(18);
    make.left.equalTo(self).offset(14);
    make.width.mas_equalTo(150);
  }];

  [self addSubview:self.switchOn];
  [self.switchOn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self);
    make.right.equalTo(self).offset(-14);
  }];
}

- (void)layoutSubviews {
  [super layoutSubviews];
}
- (UILabel *)titleLabel {
  if (!_titleLabel) {
    _titleLabel = [[UILabel alloc] init];
    _titleLabel.font = [UIFont systemFontOfSize:14];
    _titleLabel.textColor = [UIColor whiteColor];
  }
  return _titleLabel;
}

- (UILabel *)detailLabel {
  if (!_detailLabel) {
    _detailLabel = [[UILabel alloc] init];
    _detailLabel.font = [UIFont systemFontOfSize:12];
    _detailLabel.textColor = [UIColor colorWithWhite:1 alpha:0.5];
  }
  return _detailLabel;
}

- (UISwitch *)switchOn {
  if (!_switchOn) {
    _switchOn = [[UISwitch alloc] init];
    _switchOn.onTintColor = [UIColor ne_colorWithHex:0xFE7081];
    _switchOn.thumbTintColor = [UIColor whiteColor];
  }
  return _switchOn;
}

@end
