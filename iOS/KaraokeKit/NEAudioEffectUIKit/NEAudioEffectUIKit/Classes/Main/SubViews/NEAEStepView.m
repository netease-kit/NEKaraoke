// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAEStepView.h"
#import <BlocksKit/BlocksKit+UIKit.h>
#import <Masonry/Masonry.h>
#import "UIColor+AudioEffect.h"
#import "UIImage+AudioEffect.h"

@interface NEAEStepView ()

@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, strong) UIButton *titleL;
@property(nonatomic, strong) UIButton *titleR;
@property(nonatomic, strong) UILabel *stepLabel;

@property(nonatomic, assign) NSInteger max;
@property(nonatomic, assign) NSInteger min;
@property(nonatomic, copy) NSString *title;

@property(nonatomic, assign) BOOL layoutFinished;

@end

@implementation NEAEStepView

+ (instancetype)stepViewWithTitle:(NSString *)title
                              max:(NSInteger)max
                              min:(NSInteger)min
                             step:(NSInteger)step {
  NEAEStepView *slider = [[NEAEStepView alloc] init];
  slider.max = max;
  slider.min = min;
  if (step > max) {
    step = max;
  } else if (step < min) {
    step = min;
  }
  slider.step = step;
  slider.title = title;
  return slider;
}

- (void)loadUI {
  [self addSubview:self.titleLabel];

  UIView *back = [[UIView alloc] init];
  back.backgroundColor = [UIColor ne_colorWithHex:0x475857];
  [self addSubview:back];

  [back addSubview:self.titleL];
  [back addSubview:self.stepLabel];
  [back addSubview:self.titleR];

  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(self).offset(14);
    make.centerY.mas_equalTo(self);
  }];

  [back mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(self.titleLabel.mas_right).offset(16);
    make.centerY.mas_equalTo(self);
    make.size.mas_equalTo(CGSizeMake(107, 30));
  }];

  [self.titleL mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(back.mas_left).offset(6);
    make.centerY.mas_equalTo(back);
    make.size.mas_equalTo(CGSizeMake(20, 20));
  }];

  [self.titleR mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.mas_equalTo(back.mas_right).offset(-6);
    make.centerY.mas_equalTo(back);
    make.size.mas_equalTo(CGSizeMake(20, 20));
  }];

  [self.stepLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(self.titleL.mas_right).offset(1);
    make.right.mas_equalTo(self.titleR.mas_left).offset(-1);
    make.top.bottom.mas_equalTo(back);
  }];

  back.layer.cornerRadius = 15;
  back.clipsToBounds = YES;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  if (!_layoutFinished) {
    [self loadUI];
    _layoutFinished = YES;
  }
}

#pragma mark - action

- (void)tapLeft:(UIButton *)sender {
  self.step = _step - 1;
}

- (void)tapRight:(UIButton *)sender {
  self.step = _step + 1;
}

- (void)setStep:(NSInteger)step {
  if (_step == step) {
    return;
  }
  if (step > _max) {
    step = _max;
  } else if (step < _min) {
    step = _min;
  }
  _step = step;
  self.stepLabel.text = [NSString stringWithFormat:@"%zd", _step];
  if (self.valueChangedBlock) {
    self.valueChangedBlock(_step);
  }

  if (_step == _max) {
    self.titleL.enabled = true;
    self.titleR.enabled = false;
  } else if (_step == _min) {
    self.titleL.enabled = false;
    self.titleR.enabled = true;
  } else {
    self.titleL.enabled = true;
    self.titleR.enabled = true;
  }
}

- (UIButton *)titleL {
  if (!_titleL) {
    _titleL = [UIButton new];
    [_titleL setImage:[UIImage ne_imageNamed:@"rcd_sing_icn_adjust_minus"]
             forState:UIControlStateNormal];
    [_titleL addTarget:self
                  action:@selector(tapLeft:)
        forControlEvents:UIControlEventTouchUpInside];
  }
  return _titleL;
}

- (UIButton *)titleR {
  if (!_titleR) {
    _titleR = [UIButton new];
    [_titleR setImage:[UIImage ne_imageNamed:@"rcd_sing_icn_adjust_plus"]
             forState:UIControlStateNormal];
    [_titleR addTarget:self
                  action:@selector(tapRight:)
        forControlEvents:UIControlEventTouchUpInside];
  }
  return _titleR;
}

- (UILabel *)titleLabel {
  if (!_titleLabel) {
    _titleLabel = [UILabel new];
    _titleLabel.font = [UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    _titleLabel.textColor = [UIColor whiteColor];
    _titleLabel.text = _title;
  }
  return _titleLabel;
}

- (UILabel *)stepLabel {
  if (!_stepLabel) {
    _stepLabel = [UILabel new];
    _stepLabel.font = [UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    _stepLabel.textColor = [UIColor whiteColor];
    _stepLabel.textAlignment = NSTextAlignmentCenter;
    _stepLabel.text = [NSString stringWithFormat:@"%zd", _step];
  }
  return _stepLabel;
}

@end
