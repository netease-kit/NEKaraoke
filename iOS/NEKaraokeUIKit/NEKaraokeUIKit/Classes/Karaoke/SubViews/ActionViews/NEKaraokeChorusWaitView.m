// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeChorusWaitView.h"
#import <Masonry/Masonry.h>
#import "UIImage+Karaoke.h"

@interface NEKaraokeChorusWaitView ()

@property(nonatomic, strong) UILabel *waitLabel;
@property(nonatomic, strong) UILabel *songNameLabel;
@property(nonatomic, strong) UIImageView *mainIconImageView;
@property(nonatomic, strong) UIImageView *attachIconImageView;
@property(nonatomic, strong) UILabel *userNameLabel;

@end

@implementation NEKaraokeChorusWaitView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.waitLabel];
  [self.waitLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.left.equalTo(self);
    make.top.equalTo(self).offset(24);
    make.width.mas_equalTo(14);
  }];

  [self addSubview:self.songNameLabel];
  [self.songNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.waitLabel.mas_bottom).offset(6);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(16);
  }];

  [self addSubview:self.attachIconImageView];
  [self.attachIconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self).offset(22);
    make.height.width.mas_equalTo(54);
  }];

  [self addSubview:self.mainIconImageView];
  [self.mainIconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self).offset(-22);
    make.height.width.mas_equalTo(54);
  }];

  [self addSubview:self.userNameLabel];
  [self.userNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.mainIconImageView.mas_bottom).offset(13);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(14);
  }];
}

- (void)setSongName:(NSString *)songName {
  _songName = songName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.songNameLabel.text = [NSString stringWithFormat:@"《%@》", songName];
  });
}

- (void)setMainUserIcon:(UIImage *)mainUserIcon {
  _mainUserIcon = mainUserIcon;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.mainIconImageView.image = mainUserIcon;
  });
}

- (void)setAttachUserIcon:(UIImage *)attachUserIcon {
  _attachUserIcon = attachUserIcon;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.attachIconImageView.image = attachUserIcon;
  });
}

- (void)setMainUserName:(NSString *)mainUserName {
  _mainUserName = mainUserName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.userNameLabel.text =
        [NSString stringWithFormat:@"%@、%@ 请准备！", mainUserName, self.attachUserName];
  });
}

- (void)setAttachUserName:(NSString *)attachUserName {
  _attachUserName = attachUserName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.userNameLabel.text =
        [NSString stringWithFormat:@"%@、%@ 请准备！", self.mainUserName, attachUserName];
  });
}

- (void)accompanyLoading:(BOOL)isLoading {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.waitLabel.hidden = !isLoading;
  });
}

- (UILabel *)waitLabel {
  if (!_waitLabel) {
    _waitLabel = [UILabel new];
    _waitLabel.text = @"伴奏加载中...";
    _waitLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _waitLabel.textAlignment = NSTextAlignmentCenter;
    _waitLabel.textColor = [UIColor colorWithWhite:1 alpha:0.5];
  }
  return _waitLabel;
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
    _userNameLabel.textColor = [UIColor colorWithWhite:1 alpha:0.5];
    _userNameLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _userNameLabel;
}

- (UIImageView *)mainIconImageView {
  if (!_mainIconImageView) {
    _mainIconImageView = [UIImageView new];
    _mainIconImageView.layer.cornerRadius = 27;
    _mainIconImageView.clipsToBounds = true;
  }
  return _mainIconImageView;
}

- (UIImageView *)attachIconImageView {
  if (!_attachIconImageView) {
    _attachIconImageView = [UIImageView new];
    _attachIconImageView.layer.cornerRadius = 27;
    _attachIconImageView.clipsToBounds = true;
  }
  return _attachIconImageView;
}

@end
