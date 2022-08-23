// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeChooseView.h"
#import <Masonry/Masonry.h>
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"

@interface NEKaraokeMusicWaveView : UIView

@end

@implementation NEKaraokeMusicWaveView

- (void)drawRect:(CGRect)rect {
  [super drawRect:rect];
  if (self.layer.sublayers.count > 0) {
    return;
  }
  // 单条宽 1，间距 5， 一个单位算作 6
  int count = CGRectGetWidth(rect) / 6;
  for (int i = 0; i < count; i++) {
    float rate = rand() % 100 / 100.0;
    float random = CGRectGetHeight(rect) * rate;

    CGPoint start = CGPointMake(i * 6, (CGRectGetHeight(rect) - random) / 2);
    CGPoint end = CGPointMake(i * 6, start.y + random);

    float alpha = abs(i - count / 2) / (float)(count / 2);
    CAShapeLayer *lineLayer = [[CAShapeLayer alloc] init];
    lineLayer.strokeColor =
        [UIColor colorWithRed:0.851 green:0.851 blue:0.851 alpha:1 - alpha].CGColor;
    lineLayer.lineWidth = 1.0;
    CGMutablePathRef path = CGPathCreateMutable();
    CGPathMoveToPoint(path, NULL, start.x, start.y);
    CGPathAddLineToPoint(path, NULL, start.x, end.y);
    lineLayer.path = path;
    [self.layer addSublayer:lineLayer];
  }
}

@end

@interface NEKaraokeChooseView ()

@property(nonatomic, strong) UIView *chooseButton;

@property(nonatomic, strong) CAGradientLayer *buttonBackground;

@end

@implementation NEKaraokeChooseView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  self.backgroundColor = [UIColor clearColor];
  [self addSubview:self.chooseButton];
  [self.chooseButton.layer addSublayer:self.buttonBackground];

  [self.chooseButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(44);
    make.width.mas_equalTo(96);
    make.height.mas_equalTo(32);
    make.centerX.equalTo(self);
  }];

  UIImageView *icon =
      [[UIImageView alloc] initWithImage:[UIImage karaoke_imageNamed:@"choose_mic"]];
  icon.contentMode = UIViewContentModeScaleAspectFit;
  [icon sizeToFit];
  [self.chooseButton addSubview:icon];
  [icon mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.chooseButton).offset(10);
    make.centerY.equalTo(self.chooseButton);
  }];

  UILabel *label = [[UILabel alloc] init];
  label.text = @"我要点歌";
  label.textColor = [UIColor whiteColor];
  label.font = [UIFont fontWithName:@"PingFangSC-Medium" size:14];
  label.userInteractionEnabled = true;
  [label addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self
                                                                      action:@selector(choose)]];
  [self.chooseButton addSubview:label];
  [label mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(icon.mas_right).offset(10);
    make.right.equalTo(self.chooseButton).offset(-10);
    make.centerY.equalTo(self.chooseButton);
  }];

  UILabel *tips = [[UILabel alloc] init];
  tips.text = @"唱歌时请佩戴耳机避免回音";
  tips.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  tips.textColor = [UIColor colorWithWhite:1 alpha:0.5];
  tips.textAlignment = NSTextAlignmentCenter;
  [self addSubview:tips];
  [tips mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self);
    make.top.equalTo(self.chooseButton.mas_bottom).offset(12);
    make.height.mas_equalTo(22);
  }];

  NEKaraokeMusicWaveView *wave = [[NEKaraokeMusicWaveView alloc] init];
  [self addSubview:wave];
  [wave mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(60);
    make.right.equalTo(self).offset(-60);
    make.top.equalTo(tips.mas_bottom).offset(12);
    make.bottom.equalTo(self).offset(-40);
  }];
}

- (CAGradientLayer *)buttonBackground {
  if (!_buttonBackground) {
    _buttonBackground = [CAGradientLayer layer];
    NSArray *colors =
        [NSArray arrayWithObjects:(id)[[UIColor karaoke_colorWithHex:0xFF60AF] CGColor],
                                  (id)[[UIColor karaoke_colorWithHex:0xF96C6E] CGColor],
                                  (id)[[UIColor blackColor] CGColor], nil];
    [_buttonBackground setColors:colors];
    _buttonBackground.locations = @[ @0, @1 ];
    _buttonBackground.startPoint = CGPointMake(0.25, 0.5);
    _buttonBackground.endPoint = CGPointMake(0.75, 0.5);
    [_buttonBackground setFrame:CGRectMake(0, 0, CGRectGetWidth(self.chooseButton.frame),
                                           CGRectGetHeight(self.chooseButton.frame))];
    _buttonBackground.cornerRadius = 16;
  }
  return _buttonBackground;
}

- (UIView *)chooseButton {
  if (!_chooseButton) {
    _chooseButton = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 96, 32)];
    _chooseButton.layer.cornerRadius = 16;
  }
  return _chooseButton;
}

- (void)choose {
  if (self.chooseSong) {
    self.chooseSong();
  }
}

@end
