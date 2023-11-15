// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeNoLyricView.h"
#import <Masonry/Masonry.h>
#import <SDWebImage/SDWebImage.h>
#import "NEKaraokeLocalized.h"

@interface NEKaraokeNoLyricView ()

@property(nonatomic, strong) UILabel *songNameLabel;
@property(nonatomic, strong) UIImageView *iconImageView;

@end

@implementation NEKaraokeNoLyricView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.songNameLabel];
  [self.songNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(44);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(16);
  }];

  [self addSubview:self.iconImageView];
  [self.iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self);
    make.height.width.mas_equalTo(54);
  }];

  UILabel *label = [UILabel new];
  label.text = NELocalizedString(@"暂无歌词");
  label.textColor = UIColor.whiteColor;
  label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
  label.textAlignment = NSTextAlignmentCenter;
  [self addSubview:label];
  [label mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self);
    make.top.equalTo(self.iconImageView.mas_bottom).offset(11);
    make.height.mas_equalTo(14);
  }];
}

- (void)setSongName:(NSString *)songName {
  _songName = songName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.songNameLabel.text = [NSString stringWithFormat:@"《%@》", songName];
  });
}

- (void)setUserIcon:(NSString *)userIcon {
  _userIcon = userIcon;
  [self.iconImageView sd_setImageWithURL:[NSURL URLWithString:userIcon]];
}

- (UILabel *)songNameLabel {
  if (!_songNameLabel) {
    _songNameLabel = [UILabel new];
    _songNameLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    _songNameLabel.textColor = [UIColor whiteColor];
    _songNameLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _songNameLabel;
}

- (UIImageView *)iconImageView {
  if (!_iconImageView) {
    _iconImageView = [UIImageView new];
    _iconImageView.layer.cornerRadius = 27;
    _iconImageView.clipsToBounds = true;
  }
  return _iconImageView;
}

@end
