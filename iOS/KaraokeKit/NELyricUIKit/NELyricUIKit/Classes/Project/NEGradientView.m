// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEGradientView.h"

@interface NSArray (Map)

- (NSArray *)map:(id (^)(id obj))block;

@end

@implementation NSArray (Map)

- (NSArray *)map:(id (^)(id obj))block {
  NSMutableArray *result = [NSMutableArray arrayWithCapacity:self.count];

  [self enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
    id value = block(obj) ?: [NSNull null];
    [result addObject:value];
  }];

  return result;
}

@end

@interface NEGradientView () {
  CGGradientRef _gradient;
}

@end

@implementation NEGradientView

- (instancetype)init {
  if ([super init]) {
    self.automaticallyDims = true;
    self.drawsThinBorders = true;
  }
  return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    self.automaticallyDims = true;
    self.drawsThinBorders = true;
  }
  return self;
}

- (void)setColors:(NSArray<UIColor *> *)colors {
  _colors = colors;
  [self updateGradient];
}

- (void)setDimmedColors:(NSArray<UIColor *> *)dimmedColors {
  _dimmedColors = dimmedColors;
  [self updateGradient];
}

- (void)setLocations:(NSArray<NSNumber *> *)locations {
  _locations = locations;
  [self updateGradient];
}

- (void)setMode:(NEGradientViewMode)mode {
  _mode = mode;
  [self setNeedsDisplay];
}

- (void)setDirection:(NEGradientViewDirection)direction {
  _direction = direction;
  [self setNeedsDisplay];
}

- (void)setDrawsThinBorders:(bool)drawsThinBorders {
  _drawsThinBorders = drawsThinBorders;
  [self setNeedsDisplay];
}

- (void)setTopBorderColor:(UIColor *)topBorderColor {
  _topBorderColor = topBorderColor;
  [self setNeedsDisplay];
}

- (void)setRightBorderColor:(UIColor *)rightBorderColor {
  _rightBorderColor = rightBorderColor;
  [self setNeedsDisplay];
}

- (void)setBottomBorderColor:(UIColor *)bottomBorderColor {
  _bottomBorderColor = bottomBorderColor;
  [self setNeedsDisplay];
}

- (void)setLeftBorderColor:(UIColor *)leftBorderColor {
  _leftBorderColor = leftBorderColor;
  [self setNeedsDisplay];
}

- (void)drawRect:(CGRect)rect {
  CGContextRef context = UIGraphicsGetCurrentContext();
  CGContextClearRect(context, rect);
  CGSize size = self.bounds.size;
  if (_gradient) {
    CGGradientDrawingOptions options = kCGGradientDrawsAfterEndLocation;
    if (_mode == NEGradientViewModeLinear) {
      CGPoint startPoint = CGPointZero;
      CGPoint endPoint = _direction == NEGradientViewDirectionVertical ? CGPointMake(0, size.height)
                                                                       : CGPointMake(size.width, 0);
      CGContextDrawLinearGradient(context, _gradient, startPoint, endPoint, options);
    } else {
      CGPoint center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
      CGContextDrawRadialGradient(context, _gradient, center, 0, center,
                                  MIN(size.width, size.height) / 2, options);
    }
  }
  UIScreen *screen = [UIScreen mainScreen];
  CGFloat borderWidth = _drawsThinBorders ? 1.0 / screen.scale : 1.0;
  if (_topBorderColor) {
    CGContextSetFillColorWithColor(context, _topBorderColor.CGColor);
    CGContextFillRect(context, CGRectMake(0, 0, size.width, borderWidth));
  }
  CGFloat sideY = _topBorderColor != nil ? borderWidth : 0;
  CGFloat sideHeight = size.height - sideY - (_bottomBorderColor != nil ? borderWidth : 0);
  if (_rightBorderColor) {
    CGContextSetFillColorWithColor(context, _rightBorderColor.CGColor);
    CGContextFillRect(context,
                      CGRectMake(size.width - borderWidth, sideY, borderWidth, sideHeight));
  }
  if (_bottomBorderColor) {
    CGContextSetFillColorWithColor(context, _bottomBorderColor.CGColor);
    CGContextFillRect(context, CGRectMake(0, size.height - borderWidth, size.width, borderWidth));
  }
  if (_leftBorderColor) {
    CGContextSetFillColorWithColor(context, _leftBorderColor.CGColor);
    CGContextFillRect(context, CGRectMake(0, sideY, borderWidth, sideHeight));
  }
}

- (void)tintColorDidChange {
  [super tintColorDidChange];
  if (_automaticallyDims) {
    [self updateGradient];
  }
}

- (void)didMoveToWindow {
  [super didMoveToWindow];
  self.contentMode = UIViewContentModeRedraw;
}

- (void)updateGradient {
  _gradient = nil;
  [self setNeedsDisplay];

  NSArray<UIColor *> *colors = [self gradientColors];
  if (colors.count) {
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGColorSpaceModel colorSpaceModel = CGColorSpaceGetModel(colorSpace);
    NSArray *gradientColors = [colors map:^id(UIColor *color) {
      CGColorRef cgColor = color.CGColor;
      CGColorSpaceRef cgColorSpace = CGColorGetColorSpace(cgColor) ?: colorSpace;
      if (CGColorSpaceGetModel(cgColorSpace) == colorSpaceModel) {
        return (__bridge id)cgColor;
      }

      CGFloat red = 0, blue = 0, green = 0, alpha = 0;
      [color getRed:&red green:&green blue:&blue alpha:&alpha];
      return (__bridge id)([UIColor colorWithRed:red green:green blue:blue alpha:alpha].CGColor);
    }];
    CGFloat locations[_locations.count];
    for (int i = 0; i < gradientColors.count; i++) {
      locations[i] = [_locations[i] floatValue];
    }
    _gradient = CGGradientCreateWithColors(colorSpace, (CFArrayRef)gradientColors, locations);
  }
}

- (NSArray<UIColor *> *)gradientColors {
  if (self.tintAdjustmentMode == UIViewTintAdjustmentModeDimmed) {
    if (_dimmedColors) {
      return _dimmedColors;
    }
    if (_automaticallyDims && _colors.count) {
      return [_colors map:^id(UIColor *obj) {
        CGFloat hue = 0, brightness = 0, alpha = 0;
        [obj getHue:&hue saturation:nil brightness:&brightness alpha:&alpha];
        return [UIColor colorWithHue:hue saturation:0 brightness:brightness alpha:alpha];
      }];
    }
  }
  return _colors;
}

@end
