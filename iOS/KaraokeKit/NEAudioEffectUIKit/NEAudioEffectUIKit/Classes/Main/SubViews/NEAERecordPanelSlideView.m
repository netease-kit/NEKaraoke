// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAERecordPanelSlideView.h"
#import <Masonry/Masonry.h>

@interface NEAERecordPanelSlideView ()

@property(nonatomic, assign) CGFloat min;
@property(nonatomic, assign) CGFloat max;
@property(nonatomic, assign) BOOL layoutFinished;
@end

@implementation NEAERecordPanelSlideView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.min = 0;
    self.max = 1;
    self.sliderRight = 24;
    [self loadUI];
  }
  return self;
}

- (instancetype)initWithMinValue:(CGFloat)min maxValue:(CGFloat)max {
  if (self = [super initWithFrame:CGRectZero]) {
    self.min = min;
    self.max = max;
    self.sliderRight = 24;
    [self loadUI];
  }
  return self;
}

- (void)loadUI {
  [self addSubview:self.titleL];
  [self addSubview:self.slider];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  if (!self.layoutFinished) {
    if (self.titleL.text.length > 0) {
      [self.titleL mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self).offset(14);
        make.centerY.mas_equalTo(self);
      }];
    } else {
      [self.titleL mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self).offset(0);
        make.centerY.mas_equalTo(self);
      }];
    }

    [self.slider mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.mas_equalTo(self.titleL.mas_right).offset(16);
      make.centerY.mas_equalTo(self);
      make.right.mas_equalTo(self).offset(-self.sliderRight);
      make.top.bottom.mas_equalTo(self);
    }];
    self.layoutFinished = YES;
  }
}

- (NEAERecordSlider *)slider {
  if (!_slider) {
    _slider = [[NEAERecordSlider alloc] initWithMinValue:self.min maxValue:self.max];
    _slider.backgroundColor = [UIColor blackColor];
  }
  return _slider;
}

- (UILabel *)titleL {
  if (!_titleL) {
    _titleL = [UILabel new];
    _titleL.font = [UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    _titleL.textColor = [UIColor whiteColor];
  }
  return _titleL;
}

@end
