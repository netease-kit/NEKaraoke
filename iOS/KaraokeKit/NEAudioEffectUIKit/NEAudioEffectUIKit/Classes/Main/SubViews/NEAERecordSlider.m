// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAERecordSlider.h"
#import <BlocksKit/BlocksKit+UIKit.h>
#import <BlocksKit/BlocksKit.h>
#import <Masonry/Masonry.h>
#import "NEAERecordSliderBubbleView.h"
#import "UIColor+AudioEffect.h"
#import "UIImage+AudioEffect.h"
#import "UIView+AudioEffect.h"

@interface NEAERecordSingleHighlightedView : UIView
@property(nonatomic, assign) BOOL highlighted;

@end

@implementation NEAERecordSingleHighlightedView
- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.userInteractionEnabled = NO;
  }
  return self;
}
- (void)setHighlighted:(BOOL)highlighted {
  _highlighted = highlighted;
  [self setNeedsDisplay];
}
- (void)drawRect:(CGRect)rect {
  [super drawRect:rect];
  if (self.highlighted) {
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetLineWidth(context, 2.0);
    CGContextSetStrokeColorWithColor(context, [UIColor ne_colorWithHex:0xe4e4e4 alpha:1].CGColor);

    CGFloat margin = 2;
    CGContextMoveToPoint(context, CGRectGetMidX(rect) - margin, CGRectGetMidY(rect) - 3);
    CGContextAddLineToPoint(context, CGRectGetMidX(rect) - margin, CGRectGetMidY(rect) + 3);

    CGContextMoveToPoint(context, CGRectGetMidX(rect) + margin, CGRectGetMidY(rect) - 3);
    CGContextAddLineToPoint(context, CGRectGetMidX(rect) + margin, CGRectGetMidY(rect) + 3);
    CGContextStrokePath(context);
  }
}
@end

static const CGSize kNormalSize = {5, 16};
static const CGSize kHighlightedSize = {32, 18};

@interface NEAERecordSingleSlider : UISlider
@property(nonatomic, strong) CAShapeLayer *progressLayer;
@property(nonatomic, strong) CALayer *maskLayer;
@property(nonatomic, assign) BOOL showMask;
@property(nonatomic, assign) CGFloat currentSlideValue;

/*shake when reached recommendValue*/
@property(nonatomic, assign) CGFloat recommendValue;
@property(nonatomic, assign) BOOL showRecommendValue;
@property(nonatomic, assign) BOOL shakeWhenReachRecommendValue;
@property(nonatomic, strong) UIView *recommendImageV;
@property(nonatomic, assign) BOOL needRefreshRecommendValue;
@property(nonatomic, assign) CGRect recommandValueFrame;
@property(nonatomic, strong) NEAERecordSingleHighlightedView *hightedThumbImageV;
@property(nonatomic, strong) UIImpactFeedbackGenerator *feedBack;

@end

@implementation NEAERecordSingleSlider
- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadUI];
  }
  return self;
}

#pragma mark - Touch Cycle

- (BOOL)beginTrackingWithTouch:(UITouch *)touch withEvent:(UIEvent *)event {
  BOOL begin = [super beginTrackingWithTouch:touch withEvent:event];
  if (begin) {
    [UIView animateWithDuration:0.15
        delay:0
        options:UIViewAnimationOptionBeginFromCurrentState | UIViewAnimationOptionCurveEaseIn |
                UIViewAnimationOptionAllowUserInteraction
        animations:^{
          self.hightedThumbImageV.bounds =
              CGRectMake(0, 0, kHighlightedSize.width, kHighlightedSize.height);
        }
        completion:^(BOOL finished) {
          self.hightedThumbImageV.highlighted = YES;
        }];
  }
  return begin;
}

- (void)endTrackingWithTouch:(UITouch *)touch withEvent:(UIEvent *)event {
  //(32, 18)
  //(5, 16)
  [UIView animateWithDuration:0.25
                        delay:0
                      options:UIViewAnimationOptionBeginFromCurrentState |
                              UIViewAnimationOptionCurveEaseOut
                   animations:^{
                     self.hightedThumbImageV.bounds =
                         CGRectMake(0, 0, kNormalSize.width, kNormalSize.height);
                     self.hightedThumbImageV.highlighted = NO;
                   }
                   completion:NULL];
  [super endTrackingWithTouch:touch withEvent:event];
}

- (void)cancelTrackingWithEvent:(UIEvent *)event {
  [UIView animateWithDuration:0.25
                        delay:0
                      options:UIViewAnimationOptionBeginFromCurrentState |
                              UIViewAnimationOptionCurveEaseOut
                   animations:^{
                     self.hightedThumbImageV.bounds =
                         CGRectMake(0, 0, kNormalSize.width, kNormalSize.height);
                     self.hightedThumbImageV.highlighted = NO;
                   }
                   completion:NULL];
  [super cancelTrackingWithEvent:event];
}

- (void)loadUI {
  // 真正的 Thumb 用透明图片隐藏了，用 _thumbView 模拟
  UIImage *thumbImage = [UIImage ne_imageWithColor:UIColor.clearColor
                                              size:CGSizeMake(8, 16)];  // 真正的
  [self setThumbImage:thumbImage forState:UIControlStateNormal];
  [self setThumbImage:thumbImage forState:UIControlStateHighlighted];

  [self addSubview:self.hightedThumbImageV];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  [self bringSubviewToFront:self.hightedThumbImageV];
}
- (void)setRecommendValue:(CGFloat)recommendValue {
  if (_recommendValue != recommendValue) {
    self.needRefreshRecommendValue = YES;
    _recommendValue = recommendValue;
  }
}

- (CGRect)trackRectForBounds:(CGRect)bounds {
  bounds = [super trackRectForBounds:bounds];
  CGFloat trackH = 4.0;
  return CGRectMake(bounds.origin.x, bounds.origin.y + bounds.size.height / 2 - trackH / 2,
                    bounds.size.width, trackH);
}

- (CGRect)thumbRectForBounds:(CGRect)bounds trackRect:(CGRect)rect value:(float)value {
  CGRect target = [super thumbRectForBounds:bounds trackRect:rect value:value];
  self.hightedThumbImageV.center = CGPointMake(CGRectGetMidX(target), CGRectGetMidY(target));
  if (self.showMask) {  // show Mask
    if (self.progressLayer.superlayer == nil) {
      self.progressLayer.cornerRadius = 0.5 * CGRectGetHeight(rect);
      self.progressLayer.masksToBounds = YES;
      [self.layer addSublayer:self.progressLayer];
    }
    self.progressLayer.frame = rect;
    if (self.maskLayer.superlayer == nil) {
      [self.layer addSublayer:self.maskLayer];
    }

    CGFloat midX = CGRectGetMidX(rect);
    CGFloat originX = CGRectGetMidX(target);
    CGFloat maskLayerX = MIN(midX, originX);
    CGFloat maskLayerW = fabs(midX - originX);
    self.maskLayer.frame =
        CGRectMake(maskLayerX, CGRectGetMinY(rect), maskLayerW, CGRectGetHeight(rect));
  } else {
    [self.maskLayer removeFromSuperlayer];
    [self.progressLayer removeFromSuperlayer];
  }

  // recommandValueFrame
  if (self.needRefreshRecommendValue) {
    CGFloat scale = 0;
    if ((self.maximumValue - self.minimumValue) > 0) {
      scale = (self.recommendValue - self.minimumValue) / (self.maximumValue - self.minimumValue);
    }
    CGFloat x = CGRectGetMinX(rect) + scale * CGRectGetWidth(rect) - 0.5 * target.size.width;

    CGFloat originX = MAX(0, MIN(x, CGRectGetMaxX(rect) - target.size.width));
    CGFloat originY = CGRectGetMinY(target);
    _recommandValueFrame = CGRectMake(originX, originY, kNormalSize.width, kNormalSize.height);
    self.needRefreshRecommendValue = NO;
  }

  if (self.showRecommendValue) {
    if (self.recommendImageV.superview == nil) {
      [self addSubview:self.recommendImageV];
    }
    self.recommendImageV.frame = _recommandValueFrame;
  } else {
    [self.recommendImageV removeFromSuperview];
  }

  if ([self shouldShake:value]) {
    [self shake];
  }
  // 这样可以增大点击区域
  return CGRectInset(target, -10, -10);
}
- (BOOL)shouldShake:(CGFloat)value {
  if (self.shakeWhenReachRecommendValue &&
      self.isTracking) {  // shake mobile when slider reach recommend value
    if (self.currentSlideValue < self.recommendValue && value >= self.recommendValue) {
      // left -> right
      self.currentSlideValue = value;
      return YES;
    } else if (self.currentSlideValue > self.recommendValue && value <= self.recommendValue) {
      // right -> left
      self.currentSlideValue = value;
      return YES;
    }
  }
  self.currentSlideValue = value;
  return NO;
}

- (void)shake {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.feedBack impactOccurred];
  });
}

- (UIImpactFeedbackGenerator *)feedBack {
  if (!_feedBack) {
    _feedBack = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleLight];
  }
  return _feedBack;
}

- (NEAERecordSingleHighlightedView *)hightedThumbImageV {
  if (!_hightedThumbImageV) {
    _hightedThumbImageV = [[NEAERecordSingleHighlightedView alloc] init];
    _hightedThumbImageV.layer.cornerRadius = 2.5;
    _hightedThumbImageV.backgroundColor = [UIColor whiteColor];
    _hightedThumbImageV.clipsToBounds = YES;
    _hightedThumbImageV.bounds = CGRectMake(0, 0, 5, 16);
  }
  return _hightedThumbImageV;
}
- (CAShapeLayer *)progressLayer {
  if (!_progressLayer) {
    _progressLayer = [CAShapeLayer layer];
    _progressLayer.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3].CGColor;
  }
  return _progressLayer;
}

- (CALayer *)maskLayer {
  if (!_maskLayer) {
    _maskLayer = [CALayer layer];
    _maskLayer.backgroundColor = [UIColor whiteColor].CGColor;
  }
  return _maskLayer;
}

- (UIView *)recommendImageV {
  if (!_recommendImageV) {
    _recommendImageV = [[UIView alloc] init];
    _recommendImageV.layer.cornerRadius = 2.5;
    _recommendImageV.clipsToBounds = YES;
    _recommendImageV.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
    _recommendImageV.userInteractionEnabled = NO;
  }
  return _recommendImageV;
}
@end

@interface NEAERecordSlider ()
@property(nonatomic, strong) NEAERecordSingleSlider *slider;
@property(nonatomic, assign) CGFloat maxValue;
@property(nonatomic, assign) CGFloat minValue;
@property(nonatomic, strong) UILabel *titleL;
@property(nonatomic, assign) BOOL layoutFinished;

@property(nonatomic, strong) NSArray<NSNumber *> *stepNumbers;

@property(nonatomic, assign) CGFloat lastNoContinueValue;

@property(nonatomic, strong) NEAERecordSliderBubbleView *bubbleView;

@property(nonatomic, strong) id tapShowTarget;
@end
@implementation NEAERecordSlider
- (instancetype)initWithMinValue:(CGFloat)min maxValue:(CGFloat)max {
  if (self = [super initWithFrame:CGRectZero]) {
    [self initial];
    self.maxValue = max;
    self.minValue = min;
  }
  return self;
}

- (void)initial {
  _showValue = NO;
  _defaultValue = 0.0;
  _continuous = YES;
  _stepValue = 1.0;
  _bubbleFont = [UIFont systemFontOfSize:20 weight:UIFontWeightBold];
}

- (void)dealloc {
  [self.bubbleView removeFromSuperview];
}
- (void)setupUI {
  self.backgroundColor = [UIColor clearColor];
  [self addSubview:self.slider];
  [self.slider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.equalTo(self);
    make.bottom.equalTo(self).offset(-3);
  }];

  [self addSubview:self.titleL];
  [self.titleL mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.equalTo(self);
    make.top.equalTo(self).offset(-3);
  }];

  [self setSlideValue];

  CGFloat value = [self currentValue];
  self.titleL.text = [NSString stringWithFormat:@"%.0f", value];
  self.titleL.hidden = !self.showValue;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  if (!self.layoutFinished) {
    [self setupUI];
    self.layoutFinished = YES;
  }
}

#pragma mark - private

- (void)setSlideValue {
  if (self.continuous) {  // 连续滑动
    self.slider.maximumValue = self.maxValue;
    self.slider.minimumValue = self.minValue;
  } else {  // 非连续
    self.slider.maximumValue = self.stepNumbers.count - 1;
    self.slider.minimumValue = 0;
  }
  [self updateDefault];
  [self updateRecommend];
}

- (void)updateDefault {
  if (self.continuous) {  // 连续滑动
    self.slider.value = self.defaultValue;
  } else {  // 非连续
    NSInteger index = (NSInteger)((self.defaultValue - self.minValue) / self.stepValue);
    self.slider.value = index;
  }
}

- (void)updateRecommend {
  if (self.continuous) {  // 连续滑动
    self.slider.recommendValue = self.recommendValue;
  } else {  // 非连续
    NSInteger index = (NSInteger)((self.recommendValue - self.minValue) / self.stepValue);
    self.slider.recommendValue = index;
  }
  self.slider.showRecommendValue = self.showRecommendValue;
  self.slider.shakeWhenReachRecommendValue = self.shakeWhenReachRecommendValue;
}

- (void)setDefaultValue:(CGFloat)defaultValue {
  _defaultValue = defaultValue;
  if (self.layoutFinished) {
    [self updateDefault];
  }
}

- (void)continusNotifyValueChanged {
  if (self.valueChangedBlock) {
    self.valueChangedBlock(self.slider.value);
  }
  self.titleL.text = [NSString stringWithFormat:@"%.0f", self.slider.value];
}

- (void)nonContinusNotifyValueChanged {
  // closest
  CGFloat value = [self currentValue];
  self.titleL.text = [NSString stringWithFormat:@"%.0f", value];
  if (self.valueChangedBlock) {
    self.valueChangedBlock(value);
  }
}

- (CGFloat)currentValue {
  if (self.continuous) {
    return self.slider.value;
  } else {
    NSUInteger index = (NSUInteger)(self.slider.value);
    CGFloat part = self.slider.value - index;
    NSNumber *number = self.stepNumbers[index];
    return [number floatValue] + part * self.stepValue;
  }
}

#pragma mark - public
- (void)slideToValue:(CGFloat)value animated:(BOOL)animated {
  if (self.continuous) {
    [self p_slideToValue:value animated:animated];
  } else {
    NSNumber *target = [self.stepNumbers bk_match:^BOOL(NSNumber *obj) {
      return [obj isEqualToNumber:@(value)];
    }];
    if (target != nil) {
      [self p_slideToValue:[self.stepNumbers indexOfObject:target] animated:animated];
    }
  }
}

- (void)p_slideToValue:(CGFloat)value animated:(BOOL)animated {
  CGFloat valid = MAX(MIN(self.slider.maximumValue, value), self.slider.minimumValue);
  [self.slider setValue:valid animated:animated];
  self.titleL.text = [NSString stringWithFormat:@"%.0f", value];
}
- (BOOL)isTracking {
  return self.slider.highlighted;
}
- (void)setShowValue:(BOOL)showValue {
  _showValue = showValue;
  self.titleL.hidden = !showValue;
}
- (void)setBackgroundColor:(UIColor *)backgroundColor {
  [super setBackgroundColor:backgroundColor];
  [self setNeedsDisplay];
}
- (void)drawRect:(CGRect)rect {
  [super drawRect:rect];
  self.slider.backgroundColor = self.backgroundColor;
  UIColor *defaultMinimumTrackTintColor = nil;
  UIColor *defaultMaximumTrackTintColor = nil;
  if (self.continuous) {
    defaultMinimumTrackTintColor = [UIColor whiteColor];
    defaultMaximumTrackTintColor = [UIColor colorWithWhite:0 alpha:0.3];
  } else {
    defaultMinimumTrackTintColor = [UIColor clearColor];
    defaultMaximumTrackTintColor = [UIColor clearColor];
    self.slider.showMask = YES;
  }
  self.slider.minimumTrackTintColor = _minimumTrackTintColor ?: defaultMinimumTrackTintColor;
  self.slider.maximumTrackTintColor = _maximumTrackTintColor ?: defaultMaximumTrackTintColor;
}
- (void)setThumbTintColor:(UIColor *)thumbTintColor {
  _thumbTintColor = thumbTintColor;
  self.slider.hightedThumbImageV.backgroundColor = thumbTintColor;
}
- (void)setRecommendTintColor:(UIColor *)recommendTintColor {
  _recommendTintColor = recommendTintColor;
  self.slider.recommendImageV.backgroundColor = recommendTintColor;
}

- (void)increase {
  [self increaseWithStep:self.stepValue];
}

- (void)increaseWithStep:(CGFloat)step {
  if (!self.continuous) {
    [self p_slideToValue:self.slider.value + (step / self.stepValue) animated:YES];
    [self nonContinusNotifyValueChanged];
    [self tapShowBubbleView];
  }
}

- (void)tapShowBubbleView {
  if (self.tapShowBubble) {
    if ([self p_isVisible]) {  // 如果已经视图已经被隐藏，就不显示了
      if (self.tapShowTarget) {
        [NSObject bk_cancelBlock:self.tapShowTarget];
      }
      [self showBubbleViewIfNeeded:YES];
      __weak typeof(self) weakSelf = self;
      self.tapShowTarget = [NSObject
          bk_performBlock:^(void) {
            [weakSelf showBubbleViewIfNeeded:NO];
          }
               afterDelay:1];
    } else {
      [self showBubbleViewIfNeeded:NO];
    }
  }
}

- (void)decrease {
  [self decreaseWithStep:self.stepValue];
}

- (void)decreaseWithStep:(CGFloat)step {
  if (!self.continuous) {
    [self p_slideToValue:self.slider.value - (step / self.stepValue) animated:YES];
    [self nonContinusNotifyValueChanged];

    [self tapShowBubbleView];
  }
}
- (void)updateBubbleTitle {
  CGFloat value = [self currentValue];
  NSString *text = nil;
  if (self.minValue == 0 && self.maxValue == 1) {             // 0->1 归一化成0-100
    text = [NSString stringWithFormat:@"%.0f", value * 100];  //

  } else {
    if (self.maxValue <= 1) {
      text = [NSString stringWithFormat:@"%.1f", value];
    } else {
      text = [NSString stringWithFormat:@"%.0f", value];
    }
  }
  self.bubbleView.titleLabel.text = text;
}

- (UIView *)getShownView {
  return self;
}

- (UIView *)getBubbleShownView {
  id target = self;
  while (target) {
    target = ((UIResponder *)target).nextResponder;
    if ([target isKindOfClass:[UIViewController class]]) {
      break;
    }
  }
  UIViewController *vc = target;
  UIView *parentView = vc.view;
  if (parentView == nil) {
    parentView = self.window;
  }
  return parentView;
}
- (BOOL)p_isVisible {
  UIView *superView = self.superview;
  BOOL hidden = NO;
  while (superView != nil && hidden != YES) {
    superView = superView.superview;
    hidden = superView.hidden;
  }
  return !hidden;
}
- (void)hideBubbleView {
  [self showBubbleViewIfNeeded:NO];
}
- (void)showBubbleViewIfNeeded:(BOOL)show {
  UIView *parentView = [self getBubbleShownView];
  if (!parentView) {
    return;
  }
  if (show) {
    if (self.bubbleView.alpha == 0) {
      [UIView animateWithDuration:0.2
                       animations:^{
                         self.bubbleView.alpha = 1;
                       }];
    }

    CGPoint pos = CGPointMake([self xPositionFromSliderValue:self.slider],
                              CGRectGetHeight(self.slider.bounds) * 0.5);

    if (self.frame.size.width < self.frame.size.height) {
      // 竖向的
      // 优先放在左边
      pos = CGPointMake(pos.x, pos.y - 20 - CGRectGetWidth(self.bubbleView.bounds) * 0.5);
      CGPoint pointInView = [self.slider convertPoint:pos toView:parentView];
      self.bubbleView.center = pointInView;

      if (CGRectGetMinX(self.bubbleView.frame) < 0) {
        CGPoint pos = CGPointMake([self xPositionFromSliderValue:self.slider],
                                  CGRectGetHeight(self.slider.bounds) * 0.5);
        pos = CGPointMake(pos.x, pos.y + 20 + CGRectGetWidth(self.bubbleView.bounds) * 0.5);
        CGPoint pointInView = [self.slider convertPoint:pos toView:parentView];
        self.bubbleView.center = pointInView;
        self.bubbleView.direction = NEAERecordSliderBubbleViewDirectionLeft;
      } else {
        self.bubbleView.direction = NEAERecordSliderBubbleViewDirectionRight;
      }

      if (CGRectGetMinY(self.bubbleView.frame) < 0) {
        self.bubbleView.top = 0;
      } else if (CGRectGetMaxY(self.bubbleView.frame) > CGRectGetHeight(parentView.frame)) {
        self.bubbleView.bottom = CGRectGetHeight(parentView.frame);
      }
    } else {
      // 横向
      self.bubbleView.direction = NEAERecordSliderBubbleViewDirectionBottom;
      // move
      pos = CGPointMake(pos.x, pos.y - 20 - CGRectGetHeight(self.bubbleView.bounds) * 0.5);
      CGPoint pointInView = [self.slider convertPoint:pos toView:parentView];
      self.bubbleView.center = pointInView;

      // 判断下临界值不要超出了
      if (CGRectGetMinX(self.bubbleView.frame) < 0) {
        self.bubbleView.left = 0;
      } else if (CGRectGetMaxX(self.bubbleView.frame) > CGRectGetWidth(parentView.frame)) {
        self.bubbleView.right = CGRectGetWidth(parentView.frame);
      }
    }
    [self updateBubbleTitle];
    [parentView addSubview:self.bubbleView];

  } else {
    if (self.bubbleView.alpha == 1) {
      [UIView animateWithDuration:0.2
          animations:^{
            self.bubbleView.alpha = 0;
          }
          completion:^(BOOL finished) {
            [self.bubbleView removeFromSuperview];
          }];
    }
  }
}

- (CGFloat)xPositionFromSliderValue:(NEAERecordSingleSlider *)aSlider {
  CGFloat sliderRange = aSlider.frame.size.width - kNormalSize.width;
  CGFloat sliderOrigin = aSlider.frame.origin.x + (kNormalSize.width / 2.0);

  CGFloat sliderValueToPixels =
      (((aSlider.value - aSlider.minimumValue) / (aSlider.maximumValue - aSlider.minimumValue)) *
       sliderRange) +
      sliderOrigin;

  return sliderValueToPixels;
}
- (BOOL)shouldResetToRecommendValue {
  // 如果放手的时候靠近推荐值附近+-0.2%，自动到推荐值
  if (self.showRecommendValue) {
    CGFloat margin = 0.02 * (self.slider.maximumValue - self.slider.minimumValue);
    if (self.slider.value >= self.recommendValue - margin &&
        self.slider.value <= self.recommendValue + margin) {
      return YES;
    }
  }
  return NO;
}
#pragma mark - getter & setter

- (void)setRecommendValue:(CGFloat)recommendValue {
  _recommendValue = recommendValue;
  if (self.layoutFinished) {
    [self updateRecommend];
  }
}

- (void)setShowRecommendValue:(BOOL)showRecommendValue {
  _showRecommendValue = showRecommendValue;
  if (self.layoutFinished) {
    [self updateRecommend];
  }
}

- (NSArray<NSNumber *> *)stepNumbers {
  if (!_stepNumbers) {
    CGFloat value = self.minValue;
    NSMutableArray *numbers = [NSMutableArray array];
    for (int i = 0; value < self.maxValue; i++) {
      value = self.minValue + self.stepValue * i;
      [numbers addObject:@(MIN(value, self.maxValue))];
    }
    _stepNumbers = [numbers copy];
  }
  return _stepNumbers;
}

- (NEAERecordSingleSlider *)slider {
  if (!_slider) {
    _slider = [[NEAERecordSingleSlider alloc] init];
    _slider.backgroundColor = [UIColor whiteColor];
    __weak typeof(self) weakSelf = self;

    dispatch_block_t block = ^void(void) {
      __strong typeof(weakSelf) self = weakSelf;
      if (self.continuous) {
        return;
      }
      NSUInteger index = (NSUInteger)(self.slider.value + 0.5f);
      NSNumber *number = self.stepNumbers[index];
      [self slideToValue:[number integerValue] animated:NO];
      [self nonContinusNotifyValueChanged];
    };
    [_slider
        bk_addEventHandler:^(UISlider *sender) {
          __strong typeof(weakSelf) self = weakSelf;
          if (self.continuous) {
            [self continusNotifyValueChanged];
          } else {
            block();
          }
          if ([self p_isVisible]) {  // 如果已经视图已经被隐藏，就不显示了
            [self showBubbleViewIfNeeded:YES];
          } else {
            [self showBubbleViewIfNeeded:NO];
          }
        }
          forControlEvents:UIControlEventValueChanged];

    [_slider
        bk_addEventHandler:^(UISlider *sender) {
          __strong typeof(weakSelf) self = weakSelf;
          block();
          if (self.slideBeginBlock) {
            CGFloat value = [self currentValue];
            self.slideBeginBlock(value);
          }
        }
          forControlEvents:UIControlEventTouchDown];

    [_slider
        bk_addEventHandler:^(UISlider *sender) {
          __strong typeof(weakSelf) self = weakSelf;
          block();
          CGFloat value = [self currentValue];
          if (self.slideEndedBlock) {
            self.slideEndedBlock(value);
          }
          if (!self.continuous) {
            [self nonContinusNotifyValueChanged];
          }
          // 如果松手的时候靠近推荐值，滑动到推荐值
          if ([self shouldResetToRecommendValue]) {
            [self slideToValue:self.recommendValue animated:YES];
            if (self.continuous) {
              [self continusNotifyValueChanged];
            } else {
              [self nonContinusNotifyValueChanged];
            }
          }
          [self showBubbleViewIfNeeded:NO];
        }
          forControlEvents:UIControlEventTouchUpInside | UIControlEventTouchCancel |
                           UIControlEventTouchUpOutside];
  }
  return _slider;
}

- (UILabel *)titleL {
  if (!_titleL) {
    _titleL = [UILabel new];
    _titleL.font = [UIFont systemFontOfSize:14];
    _titleL.textColor = [UIColor colorWithWhite:1 alpha:0.6];
  }
  return _titleL;
}

- (NEAERecordSliderBubbleView *)bubbleView {
  if (!_bubbleView) {
    _bubbleView = [[NEAERecordSliderBubbleView alloc] initWithFrame:CGRectMake(0, 0, 57.6, 57.6)];
    _bubbleView.titleLabel.font = self.bubbleFont;
  }
  return _bubbleView;
}

@end
