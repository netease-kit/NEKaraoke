// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeControlView.h"
#import <BlocksKit/BlocksKit+UIKit.h>
#import <Masonry/Masonry.h>
#import <libextobjc/extobjc.h>
#import "UIImage+Karaoke.h"

@interface NEKaraokeControlButton : UIView

@property(nonatomic, copy) NSString *title;
@property(nonatomic, copy) NSString *selectedTitle;
@property(nonatomic, strong) UIImage *icon;
@property(nonatomic, strong) UIImage *selectedIcon;
@property(nonatomic, assign) bool enable;
@property(nonatomic, assign) bool selected;

@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, strong) UIImageView *iconImage;

@end

@implementation NEKaraokeControlButton

- (instancetype)initWithFrame:(CGRect)frame {
  // 先写死一个尺寸
  if ([super initWithFrame:CGRectMake(0, 0, 40, 60)]) {
    [self setupView];
  }
  return self;
}

- (void)addAction:(void (^)(NEKaraokeControlButton *))block {
  [self bk_whenTapped:^{
    if (self.enable) {
      block(self);
    }
  }];
}

- (void)setupView {
  [self addSubview:self.iconImage];
  [self.iconImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self);
    make.centerX.equalTo(self);
    make.width.height.mas_equalTo(28);
  }];

  [self addSubview:self.titleLabel];
  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self);
    make.top.equalTo(self.iconImage.mas_bottom).offset(2);
    make.left.right.equalTo(self);
  }];
}

- (void)setTitle:(NSString *)title {
  _title = title;
  if (!self.selected) {
    _titleLabel.text = title;
  }
  [self setEnable:self.enable];
}

- (void)setSelectedTitle:(NSString *)selectedTitle {
  _selectedTitle = selectedTitle;
  if (self.selected) {
    _titleLabel.text = selectedTitle;
  }
  [self setEnable:self.enable];
}

- (void)setIcon:(UIImage *)icon {
  _icon = icon;
  if (!self.selected) {
    _iconImage.image = icon;
  }
  [self setEnable:self.enable];
}

- (void)setSelectedIcon:(UIImage *)selectedIcon {
  _selectedIcon = selectedIcon;
  if (self.selected) {
    _iconImage.image = selectedIcon;
  }
  [self setEnable:self.enable];
}

- (void)setSelected:(bool)selected {
  _selected = selected;
  if (selected && _selectedIcon) {
    _iconImage.image = _selectedIcon;
  } else {
    _iconImage.image = _icon;
  }
  if (selected && _selectedTitle) {
    _titleLabel.text = _selectedTitle;
  } else {
    _titleLabel.text = _title;
  }
  [self setEnable:self.enable];
}

- (void)setEnable:(bool)enable {
  _enable = enable;
  if (enable) {
    self.titleLabel.textColor = [UIColor whiteColor];
    if (self.selected && self.selectedIcon) {
      self.iconImage.image = self.selectedIcon;
    } else {
      self.iconImage.image = self.icon;
    }
  } else {
    self.titleLabel.textColor = [UIColor colorWithWhite:1 alpha:0.4];
    if (self.iconImage.image) {
      self.iconImage.image =
          [self.iconImage.image karaoke_imageWithTintColor:[UIColor colorWithWhite:1 alpha:0.4]];
    }
  }
}

- (UILabel *)titleLabel {
  if (!_titleLabel) {
    _titleLabel = [[UILabel alloc] init];
    _titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
    _titleLabel.textColor = [UIColor whiteColor];
    _titleLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _titleLabel;
}

- (UIImageView *)iconImage {
  if (!_iconImage) {
    _iconImage = [UIImageView new];
  }
  return _iconImage;
}

@end

@interface NEKaraokeControlView ()

@property(nonatomic, strong) NEKaraokeControlButton *voiceButton;
@property(nonatomic, strong) NEKaraokeControlButton *pauseButton;
@property(nonatomic, strong) NEKaraokeControlButton *switchButton;
@property(nonatomic, strong) NEKaraokeControlButton *orginalButton;

@end

@implementation NEKaraokeControlView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
    [self reset];
  }
  return self;
}

- (void)setupView {
  // 两边各 36，按钮宽度 40
  CGFloat margin = (CGRectGetWidth(self.frame) - 36 * 2 - 40 * 4) / 3;

  [self addSubview:self.voiceButton];
  [self.voiceButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(36);
    make.top.bottom.equalTo(self);
    make.width.mas_equalTo(40);
  }];

  [self addSubview:self.pauseButton];
  [self.pauseButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.voiceButton.mas_right).offset(margin);
    make.top.bottom.equalTo(self);
    make.width.mas_equalTo(40);
  }];

  [self addSubview:self.switchButton];
  [self.switchButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.pauseButton.mas_right).offset(margin);
    make.top.bottom.equalTo(self);
    make.width.mas_equalTo(40);
  }];

  [self addSubview:self.orginalButton];
  [self.orginalButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self).offset(-36);
    make.top.bottom.equalTo(self);
    make.width.mas_equalTo(40);
  }];
}

- (void)enableVoice:(BOOL)enable {
  self.voiceButton.enable = enable;
}

- (BOOL)isVoiceEnabled {
  return self.voiceButton.enable;
}

- (void)enablePause:(BOOL)enable {
  self.pauseButton.enable = enable;
}

- (BOOL)isPauseEnabled {
  return self.pauseButton.enable;
}

- (void)selectPause:(BOOL)selected {
  self.pauseButton.selected = selected;
}

- (BOOL)isPauseSelected {
  return self.pauseButton.selected;
}

- (void)enableSwitch:(BOOL)enable {
  self.switchButton.enable = enable;
}

- (BOOL)isSwitchEnabled {
  return self.switchButton.enable;
}

- (void)enableOrginal:(BOOL)enable {
  self.orginalButton.enable = enable;
}

- (BOOL)isOrginalEnabled {
  return self.orginalButton.enable;
}

- (void)selectOrginal:(BOOL)selected {
  self.orginalButton.selected = selected;
}

- (BOOL)isOrginalSelected {
  return self.orginalButton.selected;
}

- (void)reset {
  self.voiceButton.enable = YES;
  self.voiceButton.selected = NO;

  self.pauseButton.enable = YES;
  self.pauseButton.selected = NO;

  self.switchButton.enable = YES;
  self.switchButton.selected = NO;

  self.orginalButton.enable = YES;
  self.orginalButton.selected = NO;
}
- (NEKaraokeControlButton *)voiceButton {
  if (!_voiceButton) {
    _voiceButton = [self createButton];
    _voiceButton.title = @"调音";
    _voiceButton.icon = [UIImage karaoke_imageNamed:@"voice_icon"];
    @weakify(self)[_voiceButton addAction:^(NEKaraokeControlButton *button) {
      @strongify(self) if ([self.delegate respondsToSelector:@selector(onControlEvent:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onControlEvent:NEKaraokeControlEventTypeVoice];
        });
      }
    }];
  }
  return _voiceButton;
}

- (NEKaraokeControlButton *)pauseButton {
  if (!_pauseButton) {
    _pauseButton = [self createButton];
    _pauseButton.title = @"暂停";
    _pauseButton.selectedTitle = @"播放";
    _pauseButton.icon = [UIImage karaoke_imageNamed:@"pause_ico"];
    _pauseButton.selectedIcon = [UIImage karaoke_imageNamed:@"resume_ico"];
    @weakify(self)[_pauseButton addAction:^(NEKaraokeControlButton *button) {
      @strongify(self) if ([self.delegate respondsToSelector:@selector(onControlEvent:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onControlEvent:button.selected ? NEKaraokeControlEventTypeResume
                                                        : NEKaraokeControlEventTypePause];
        });
      }
    }];
  }
  return _pauseButton;
}

- (NEKaraokeControlButton *)switchButton {
  if (!_switchButton) {
    _switchButton = [self createButton];
    _switchButton.title = @"切歌";
    _switchButton.icon = [UIImage karaoke_imageNamed:@"switch_ico"];
    @weakify(self)[_switchButton addAction:^(NEKaraokeControlButton *button) {
      @strongify(self) if ([self.delegate respondsToSelector:@selector(onControlEvent:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onControlEvent:NEKaraokeControlEventTypeSwitch];
        });
      }
    }];
  }
  return _switchButton;
}

- (NEKaraokeControlButton *)orginalButton {
  if (!_orginalButton) {
    _orginalButton = [self createButton];
    _orginalButton.title = @"原唱已关";
    _orginalButton.selectedTitle = @"原唱已开";
    _orginalButton.icon = [UIImage karaoke_imageNamed:@"accompany_ico"];
    _orginalButton.selectedIcon = [UIImage karaoke_imageNamed:@"orginal_ico"];
    @weakify(self)[_orginalButton addAction:^(NEKaraokeControlButton *button) {
      bool isSelected = button.selected;
      button.selected = !button.selected;
      @strongify(self) if ([self.delegate respondsToSelector:@selector(onControlEvent:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onControlEvent:isSelected ? NEKaraokeControlEventTypeOriginal
                                                   : NEKaraokeControlEventTypeAccompany];
        });
      }
    }];
  }
  return _orginalButton;
}

- (NEKaraokeControlButton *)createButton {
  NEKaraokeControlButton *button = [[NEKaraokeControlButton alloc] initWithFrame:self.frame];
  button.enable = true;
  return button;
}

@end
