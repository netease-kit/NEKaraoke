// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAERecordSliderBubbleView.h"
#import <Masonry/Masonry.h>
#import "UIImage+AudioEffect.h"

@implementation NEAERecordSliderBubbleView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  self.userInteractionEnabled = NO;
  self.backgroundImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 57.6, 57)];
  self.titleLabel = [[UILabel alloc] init];
  [self addSubview:self.backgroundImageView];
  [self addSubview:self.titleLabel];

  self.backgroundImageView.image = [UIImage ne_imageNamed:@"rcd_common_icn_slider_bubble.pdf"];
  self.titleLabel.textColor = [UIColor whiteColor];

  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.equalTo(self.mas_centerX);
    make.centerY.equalTo(self.mas_centerY).offset(-3);
  }];
}

- (void)setDirection:(NEAERecordSliderBubbleViewDirection)direction {
  if (_direction != direction) {
    _direction = direction;
    self.backgroundImageView.transform = CGAffineTransformMakeRotation(-M_PI / 2.0 * direction);
    self.backgroundImageView.center =
        CGPointMake(CGRectGetWidth(self.frame) / 2.0, CGRectGetHeight(self.frame) / 2.0);
    CGFloat xOffset = -3;
    CGFloat yOffset = -3;
    if (direction == NEAERecordSliderBubbleViewDirectionLeft ||
        direction == NEAERecordSliderBubbleViewDirectionRight) {
      yOffset = 0;
      xOffset = -3 * (direction == NEAERecordSliderBubbleViewDirectionLeft ? -1 : 1);
    }

    if (direction == NEAERecordSliderBubbleViewDirectionBottom ||
        direction == NEAERecordSliderBubbleViewDirectionUp) {
      xOffset = 0;
      yOffset = -3 * (direction == NEAERecordSliderBubbleViewDirectionUp ? -1 : 1);
    }
    [self.titleLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.centerX.equalTo(self.mas_centerX).offset(xOffset);
      make.centerY.equalTo(self.mas_centerY).offset(yOffset);
    }];
  }
}

@end
