// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeInputToolBar.h"
#import <Masonry/Masonry.h>
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"
@interface NEKaraokeInputToolBar ()

@property(nonatomic, strong, readwrite) UITextField *textField;
@property(nonatomic, strong) UILabel *inputLab;

@property(nonatomic, strong) UIButton *giftBtn;
@property(nonatomic, strong) UIButton *micBtn;
@property(nonatomic, strong) UIButton *seatBtn;
@property(nonatomic, strong) UIButton *chooseBtn;

@property(nonatomic, strong) UILabel *unreadLabel;
@property(nonatomic, strong) UILabel *pickUnreadLabel;
@end

@implementation NEKaraokeInputToolBar

- (instancetype)init {
  if (self = [super init]) {
    [self setupView:true];
  }
  return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
  if (self = [super initWithFrame:frame]) {
    [self setupView:true];
  }
  return self;
}

- (instancetype)initWithFrame:(CGRect)frame showGift:(BOOL)showGift {
  if ([super initWithFrame:frame]) {
    [self setupView:showGift];
  }
  return self;
}

- (void)setupView:(BOOL)showGift {
  if (showGift) {
    [self addSubview:self.giftBtn];
    [self.giftBtn mas_makeConstraints:^(MASConstraintMaker *make) {
      make.right.equalTo(self).offset(-10);
      make.width.height.mas_equalTo(36);
      make.centerY.equalTo(self);
    }];
  }

  [self addSubview:self.chooseBtn];
  [self.chooseBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(showGift ? self.giftBtn.mas_left : self).offset(-10);
    make.width.height.mas_equalTo(36);
    make.centerY.equalTo(self);
  }];

  [self addSubview:self.pickUnreadLabel];
  [self.pickUnreadLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(12, 12));
    make.right.equalTo(self.chooseBtn.mas_right).offset(4);
    make.top.equalTo(self.chooseBtn.mas_top).offset(-4);
  }];

  [self addSubview:self.seatBtn];
  [self.seatBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.chooseBtn.mas_left).offset(-10);
    make.width.height.mas_equalTo(36);
    make.centerY.equalTo(self);
  }];
  [self addSubview:self.unreadLabel];
  [self.unreadLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(12, 12));
    make.right.equalTo(self.seatBtn.mas_right).offset(4);
    make.top.equalTo(self.seatBtn.mas_top).offset(-4);
  }];

  [self addSubview:self.micBtn];
  [self.micBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.seatBtn.mas_left).offset(-10);
    make.width.height.mas_equalTo(36);
    make.centerY.equalTo(self);
  }];

  [self addSubview:self.textField];
  [self.textField mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.mas_equalTo(36);
    make.right.equalTo(self.micBtn.mas_left).offset(-14);
    make.left.equalTo(self).offset(12);
    make.centerY.equalTo(self);
  }];

  [self addSubview:self.inputLab];
  [self.inputLab mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.mas_equalTo(36);
    make.right.equalTo(self.textField);
    make.left.equalTo(self.textField);
    make.centerY.equalTo(self.textField);
  }];
}

- (void)setMicBtnSelected:(BOOL)selected {
  self.micBtn.selected = selected;
}

- (void)clickButton:(UIButton *)button {
  if (!(self.delegate && [self.delegate respondsToSelector:@selector(clickInputToolBarAction:)])) {
    return;
  }
  NEKaraokeInputToolBarAction action = NEKaraokeInputToolBarActionInput;
  if (button == self.giftBtn) {
    action = NEKaraokeInputToolBarActionGift;
  } else if (button == self.micBtn) {
    action =
        self.micBtn.selected ? NEKaraokeInputToolBarActionUnmute : NEKaraokeInputToolBarActionMute;
  } else if (button == self.chooseBtn) {
    action = NEKaraokeInputToolBarActionChooseSong;
  } else if (button == self.seatBtn) {
    action = NEKaraokeInputToolBarActionSeat;
  }
  [self.delegate clickInputToolBarAction:action];
}

- (void)clickInputLabel {
  [self.textField becomeFirstResponder];
  if ([self.delegate respondsToSelector:@selector(clickInputToolBarAction:)]) {
    [self.delegate clickInputToolBarAction:NEKaraokeInputToolBarActionInput];
  }
}
- (void)configSeatWithType:(NEKaraokeInputToolBarSeatType)type {
  NSString *imageName = @"seat_on";
  switch (type) {
    case NEKaraokeInputToolBarSeatTypeOn:
      imageName = @"seat_on";
      break;
    case NEKaraokeInputToolBarSeatTypeDown:
      imageName = @"seat_off";
      break;
    default:
      imageName = @"seat_wait";
      break;
  }
  [self.seatBtn setImage:[UIImage karaoke_imageNamed:imageName] forState:UIControlStateNormal];
}
- (void)resignFirstResponder {
  [self.textField resignFirstResponder];
}
- (void)configSeatUnreadNumber:(NSInteger)number {
  self.unreadLabel.hidden = !(number > 0);
  self.unreadLabel.text = [NSString stringWithFormat:@"%ld", number];
}
- (void)configPickSongUnreadNumber:(NSInteger)number {
  self.pickUnreadLabel.hidden = !(number > 0);
  self.pickUnreadLabel.text = [NSString stringWithFormat:@"%ld", number];
}
- (void)isShowMicBtn:(BOOL)flag {
  self.micBtn.hidden = !flag;
  if (flag) {
    [self.textField mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.height.mas_equalTo(36);
      make.right.equalTo(self.micBtn.mas_left).offset(-14);
      make.left.equalTo(self).offset(12);
      make.centerY.equalTo(self);
    }];
  } else {
    [self.textField mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.height.mas_equalTo(36);
      make.right.equalTo(self.seatBtn.mas_left).offset(-14);
      make.left.equalTo(self).offset(12);
      make.centerY.equalTo(self);
    }];
  }
}
#pragma mark - lazyMethod
/// private button
- (UIButton *)alphaCircleButton {
  UIButton *btn = [[UIButton alloc] init];
  [btn addTarget:self action:@selector(clickButton:) forControlEvents:UIControlEventTouchUpInside];
  return btn;
}

/// 输入视图默认富文本文案
- (NSAttributedString *)_inputLabPlaceholder {
  NSMutableAttributedString *attributedString =
      [[NSMutableAttributedString alloc] initWithString:@"   "];

  NSTextAttachment *attchment = [[NSTextAttachment alloc] init];
  attchment.bounds = CGRectMake(0, -2, 16, 16);
  attchment.image = [UIImage karaoke_imageNamed:@"text_ico"];
  NSAttributedString *attachStr = [NSAttributedString attributedStringWithAttachment:attchment];
  [attributedString appendAttributedString:attachStr];

  NSAttributedString *tipStr = [[NSAttributedString alloc]
      initWithString:@" 一起聊聊吧~"
          attributes:@{NSForegroundColorAttributeName : [UIColor colorWithWhite:1 alpha:0.3]}];
  [attributedString appendAttributedString:tipStr];

  return [attributedString copy];
}

#pragma mark - lazy load

- (UITextField *)textField {
  if (!_textField) {
    _textField = [[UITextField alloc] init];
    _textField.textColor = [UIColor clearColor];
  }
  return _textField;
}

- (UILabel *)inputLab {
  if (!_inputLab) {
    _inputLab = [[UILabel alloc] init];
    _inputLab.backgroundColor = [UIColor colorWithWhite:0 alpha:0.6];
    _inputLab.layer.cornerRadius = 18;
    _inputLab.layer.masksToBounds = YES;
    _inputLab.attributedText = [self _inputLabPlaceholder];
    _inputLab.textColor = [UIColor whiteColor];
    _inputLab.font = [UIFont systemFontOfSize:14];
    _inputLab.userInteractionEnabled = YES;
    UITapGestureRecognizer *tap =
        [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickInputLabel)];
    [_inputLab addGestureRecognizer:tap];
  }
  return _inputLab;
}

- (UIButton *)giftBtn {
  if (!_giftBtn) {
    _giftBtn = [self alphaCircleButton];
    [_giftBtn setImage:[UIImage karaoke_imageNamed:@"inupt_gift"] forState:UIControlStateNormal];
  }
  return _giftBtn;
}

- (UIButton *)chooseBtn {
  if (!_chooseBtn) {
    _chooseBtn = [self alphaCircleButton];
    [_chooseBtn setImage:[UIImage karaoke_imageNamed:@"input_choose"]
                forState:UIControlStateNormal];
  }
  return _chooseBtn;
}

- (UIButton *)seatBtn {
  if (!_seatBtn) {
    _seatBtn = [self alphaCircleButton];
    [_seatBtn setImage:[UIImage karaoke_imageNamed:@"seat_on"] forState:UIControlStateNormal];
  }
  return _seatBtn;
}

- (UIButton *)micBtn {
  if (!_micBtn) {
    _micBtn = [self alphaCircleButton];
    [_micBtn setImage:[UIImage karaoke_imageNamed:@"inupt_mic"] forState:UIControlStateNormal];
    [_micBtn setImage:[UIImage karaoke_imageNamed:@"inupt_mic_off"]
             forState:UIControlStateSelected];
  }
  return _micBtn;
}

- (UILabel *)unreadLabel {
  if (!_unreadLabel) {
    _unreadLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _unreadLabel.font = [UIFont systemFontOfSize:10];
    _unreadLabel.backgroundColor = UIColor.whiteColor;
    _unreadLabel.textAlignment = NSTextAlignmentCenter;
    _unreadLabel.textColor = [UIColor karaoke_colorWithHex:0x222222];
    _unreadLabel.layer.cornerRadius = 6;
    _unreadLabel.layer.masksToBounds = YES;
    _unreadLabel.hidden = YES;
  }
  return _unreadLabel;
}
- (UILabel *)pickUnreadLabel {
  if (!_pickUnreadLabel) {
    _pickUnreadLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _pickUnreadLabel.font = [UIFont systemFontOfSize:10];
    _pickUnreadLabel.backgroundColor = UIColor.whiteColor;
    _pickUnreadLabel.textAlignment = NSTextAlignmentCenter;
    _pickUnreadLabel.textColor = [UIColor karaoke_colorWithHex:0x222222];
    _pickUnreadLabel.layer.cornerRadius = 6;
    _pickUnreadLabel.layer.masksToBounds = YES;
    _pickUnreadLabel.hidden = YES;
  }
  return _pickUnreadLabel;
}
@end
