// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "UIColor+AudioEffect.h"

@implementation UIColor (AudioEffect)

+ (UIColor *)ne_colorWithHex:(NSInteger)rgbValue alpha:(float)alpha {
  return [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0
                         green:((float)((rgbValue & 0x00FF00) >> 8)) / 255.0
                          blue:((float)(rgbValue & 0x0000FF)) / 255.0
                         alpha:alpha];
}

+ (UIColor *)ne_colorWithHex:(NSInteger)rgbValue {
  return [self ne_colorWithHex:rgbValue alpha:1];
}

@end
