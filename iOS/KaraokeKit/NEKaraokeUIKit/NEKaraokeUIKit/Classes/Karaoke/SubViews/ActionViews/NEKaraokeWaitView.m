// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeWaitView.h"
#import <Masonry/Masonry.h>
#import <SDWebImage/SDWebImage.h>
#import "NEKaraokeLocalized.h"

@interface NEKaraokeWaitView ()

@property(nonatomic, strong) UILabel *timeLabel;
@property(nonatomic, strong) UILabel *songNameLabel;
@property(nonatomic, strong) UILabel *userNameLabel;
@property(nonatomic, strong) UIImageView *iconImageView;

@end

@implementation NEKaraokeWaitView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.timeLabel];
  [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(24);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(14);
  }];

  [self addSubview:self.songNameLabel];
  [self.songNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.timeLabel.mas_bottom).offset(6);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(16);
  }];

  [self addSubview:self.iconImageView];
  [self.iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self);
    make.height.width.mas_equalTo(54);
  }];

  [self addSubview:self.userNameLabel];
  [self.userNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.iconImageView.mas_bottom).offset(11);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(14);
  }];
}

- (void)updateTime:(NSInteger)time {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.timeLabel.text = [NSString stringWithFormat:NELocalizedString(@"%zds 后播放"), time];
  });
}

- (void)setSongName:(NSString *)songName {
  _songName = songName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.songNameLabel.text = [NSString stringWithFormat:@"《%@》", songName];
  });
}

- (void)setUserName:(NSString *)userName {
  _userName = userName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.userNameLabel.text =
        [NSString stringWithFormat:@"%@ %@", userName, NELocalizedString(@"请准备")];
  });
}

- (void)setUserIcon:(NSString *)userIcon {
  _userIcon = userIcon;
  [self.iconImageView sd_setImageWithURL:[NSURL URLWithString:userIcon]];
}

- (UILabel *)timeLabel {
  if (!_timeLabel) {
    _timeLabel = [UILabel new];
    _timeLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _timeLabel.textColor = [UIColor whiteColor];
    _timeLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _timeLabel;
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

- (UILabel *)userNameLabel {
  if (!_userNameLabel) {
    _userNameLabel = [UILabel new];
    _userNameLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _userNameLabel.textColor = [UIColor whiteColor];
    _userNameLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _userNameLabel;
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
