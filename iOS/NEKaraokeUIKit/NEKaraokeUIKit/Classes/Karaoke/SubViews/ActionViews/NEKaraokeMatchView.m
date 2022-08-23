// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeMatchView.h"
#import <BlocksKit/BlocksKit+UIKit.h>
#import <Masonry/Masonry.h>
#import "UIImage+Karaoke.h"

@interface NEKaraokeMatchView ()

@property(nonatomic, strong) UILabel *matchLabel;
@property(nonatomic, strong) UILabel *songNameLabel;
@property(nonatomic, strong) UIImageView *userImageView;
@property(nonatomic, strong) UIImageView *addImageView;
@property(nonatomic, strong) UIButton *soloButton;
@property(nonatomic, strong) UIButton *joinButton;

@end

@implementation NEKaraokeMatchView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.matchLabel];
  [self.matchLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.left.equalTo(self);
    make.top.equalTo(self).offset(24);
    make.width.mas_equalTo(14);
  }];

  [self addSubview:self.songNameLabel];
  [self.songNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.matchLabel.mas_bottom).offset(6);
    make.left.right.equalTo(self);
    make.height.mas_equalTo(16);
  }];

  [self addSubview:self.addImageView];
  [self.addImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self).offset(22);
    make.height.width.mas_equalTo(54);
  }];

  [self addSubview:self.userImageView];
  [self.userImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.songNameLabel.mas_bottom).offset(12);
    make.centerX.equalTo(self).offset(-22);
    make.height.width.mas_equalTo(54);
  }];

  [self addSubview:self.soloButton];
  [self.soloButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.userImageView.mas_bottom).offset(11);
    make.centerX.equalTo(self);
    make.height.mas_equalTo(24);
    make.width.mas_equalTo(52);
  }];

  [self addSubview:self.joinButton];
  [self.joinButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.soloButton);
    make.centerX.equalTo(self);
    make.height.equalTo(self.soloButton);
    make.width.mas_equalTo(105);
  }];
}

- (void)updateTime:(NSInteger)time {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSString *content = [NSString stringWithFormat:@"合唱匹配中 %zds", time];
    NSMutableAttributedString *attributedString =
        [[NSMutableAttributedString alloc] initWithString:content];
    [attributedString addAttribute:NSForegroundColorAttributeName
                             value:[UIColor colorWithWhite:1 alpha:0.5]
                             range:NSMakeRange(0, 5)];
    [attributedString addAttribute:NSForegroundColorAttributeName
                             value:[UIColor whiteColor]
                             range:NSMakeRange(5, content.length - 5)];
    self.matchLabel.attributedText = attributedString;
  });
}

- (void)setSoloBtnHidden:(BOOL)hidden {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.soloButton.hidden = hidden;
  });
}
- (void)setJoinBtnHidden:(BOOL)hidden {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.joinButton.hidden = hidden;
  });
}

- (void)setUserIcon:(UIImage *)userIcon {
  _userIcon = userIcon;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.userImageView.image = userIcon;
  });
}

- (void)setSongName:(NSString *)songName {
  _songName = songName;
  dispatch_async(dispatch_get_main_queue(), ^{
    self.songNameLabel.text = [NSString stringWithFormat:@"《%@》", songName];
  });
}

- (UILabel *)matchLabel {
  if (!_matchLabel) {
    _matchLabel = [[UILabel alloc] init];
    _matchLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _matchLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _matchLabel;
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

- (UIImageView *)userImageView {
  if (!_userImageView) {
    _userImageView = [UIImageView new];
    _userImageView.layer.cornerRadius = 27;
    _userImageView.clipsToBounds = true;
  }
  return _userImageView;
}

- (UIImageView *)addImageView {
  if (!_addImageView) {
    _addImageView = [[UIImageView alloc] initWithImage:[UIImage karaoke_imageNamed:@"match_icon"]];
    _addImageView.userInteractionEnabled = true;
    [_addImageView bk_whenTapped:^{
      [self joinChorus];
    }];
  }
  return _addImageView;
}

- (UIButton *)soloButton {
  if (!_soloButton) {
    _soloButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _soloButton.backgroundColor = [UIColor whiteColor];
    [_soloButton setTitle:@"独唱" forState:UIControlStateNormal];
    [_soloButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_soloButton addTarget:self
                    action:@selector(solo)
          forControlEvents:UIControlEventTouchUpInside];
    _soloButton.layer.cornerRadius = 12;
    _soloButton.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  }
  return _soloButton;
}

- (UIButton *)joinButton {
  if (!_joinButton) {
    _joinButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _joinButton.backgroundColor = [UIColor whiteColor];
    [_joinButton setTitle:@"加入合唱" forState:UIControlStateNormal];
    [_joinButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_joinButton addTarget:self
                    action:@selector(joinChorus)
          forControlEvents:UIControlEventTouchUpInside];
    _joinButton.layer.cornerRadius = 12;
    _joinButton.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  }
  return _joinButton;
}

- (void)solo {
  if (self.toSolo) {
    self.toSolo();
  }
}

- (void)joinChorus {
  if (self.join && self.joinEnable) {
    self.join();
  }
}

@end
