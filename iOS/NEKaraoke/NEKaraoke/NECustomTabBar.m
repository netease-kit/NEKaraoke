// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NECustomTabBar.h"

@implementation NECustomTabBar

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    self.tintColor = [UIColor blackColor];
    self.backgroundColor = [UIColor whiteColor];
  }
  return self;
}

- (void)layoutSubviews {
  [super layoutSubviews];

  CGFloat w = self.bounds.size.width;
  CGFloat h = self.bounds.size.height;
  // 3个控件
  CGFloat width = self.bounds.size.width / 3.0;
  CGFloat centerY = h / 2;
  int i = 0;
  for (UIView *btn in self.subviews) {
    if ([btn isKindOfClass:NSClassFromString(@"UITabBarButton")]) {
      if (i == 1) {
        i = 2;
      }
      centerY = btn.center.y;
      btn.frame = CGRectMake(i * width, btn.frame.origin.y, width, btn.frame.size.height);
      i++;
    }
  }
  self.customButton.center = CGPointMake(w * 0.5, centerY);
}

- (UIButton *)customButton {
  if (!_customButton) {
    _customButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_customButton setImage:[UIImage imageNamed:@"tabbar_btn"] forState:UIControlStateNormal];
    _customButton.frame = CGRectMake(0, 0, 32, 32);
    [self addSubview:_customButton];
  }
  return _customButton;
}

- (void)drawRect:(CGRect)rect {
  [super drawRect:rect];

  // 分割线
  CAShapeLayer *lineLayer = [[CAShapeLayer alloc] init];
  lineLayer.strokeColor = [UIColor colorWithRed:0.961 green:0.961 blue:0.961 alpha:1].CGColor;
  lineLayer.lineWidth = 1.0;
  CGMutablePathRef path = CGPathCreateMutable();
  CGPathMoveToPoint(path, NULL, 0, 0);
  CGPathAddLineToPoint(path, NULL, rect.size.width, 0);
  lineLayer.path = path;
  [self.layer addSublayer:lineLayer];
}

@end
