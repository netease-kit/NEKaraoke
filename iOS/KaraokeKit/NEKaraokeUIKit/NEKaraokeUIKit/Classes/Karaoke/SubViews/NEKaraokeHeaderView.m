// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeHeaderView.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeLocalized.h"
#import "UIImage+Karaoke.h"

@interface NEKaraokeHeaderView ()

@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, strong) UILabel *countLabel;
@property(nonatomic, strong) UIButton *closeButton;

@end

@implementation NEKaraokeHeaderView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.titleLabel];
  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(14);
    make.top.equalTo(self.mas_top);
    make.bottom.equalTo(self.mas_bottom);
  }];

  [self addSubview:self.closeButton];
  [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self).offset(-14);
    make.centerY.equalTo(self);
    make.width.height.mas_equalTo(24);
  }];

  [self addSubview:self.countLabel];
  [self.countLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.closeButton.mas_left).offset(-10);
    make.height.mas_equalTo(20);
    make.centerY.equalTo(self);
    make.width.mas_equalTo(70);
  }];
}

- (void)setTitle:(NSString *)title {
  _title = title;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.titleLabel.text = title;
  });
}

- (void)setCount:(NSInteger)count {
  if (count < 0) {
    count = 0;
  }
  dispatch_async(dispatch_get_main_queue(), ^{
    self.countLabel.text = [NSString stringWithFormat:NELocalizedString(@"在线 %zd 人"), count];
  });
}

- (UILabel *)countLabel {
  if (!_countLabel) {
    _countLabel = [[UILabel alloc] init];
    _countLabel.textColor = [UIColor whiteColor];
    _countLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10.0];
    _countLabel.layer.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5].CGColor;
    _countLabel.layer.cornerRadius = 10;
    _countLabel.textAlignment = NSTextAlignmentCenter;
    _countLabel.text = NELocalizedString(@"在线 0 人");
  }
  return _countLabel;
}

- (UILabel *)titleLabel {
  if (!_titleLabel) {
    _titleLabel = [[UILabel alloc] init];
    _titleLabel.textColor = [UIColor whiteColor];
    _titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16.0];
  }
  return _titleLabel;
}

- (UIButton *)closeButton {
  if (!_closeButton) {
    _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_closeButton setImage:[UIImage karaoke_imageNamed:@"close_ico"] forState:UIControlStateNormal];
    [_closeButton addTarget:self
                     action:@selector(close)
           forControlEvents:UIControlEventTouchUpInside];
  }
  return _closeButton;
}

- (void)close {
  if ([self.delegate respondsToSelector:@selector(onClose)]) {
    [self.delegate onClose];
  }
}

@end
