// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIColor (Karaoke)

+ (UIColor *)karaoke_colorWithHex:(NSInteger)rgbValue alpha:(float)alpha;

+ (UIColor *)karaoke_colorWithHex:(NSInteger)rgbValue
                            alpha:(float)alpha
                          darkHex:(NSInteger)darkRgbValue
                            alpha:(float)darkAlpha;

+ (UIColor *)karaoke_colorWithHex:(NSInteger)rgbValue;

+ (UIColor *)karaoke_colorWithHex:(NSInteger)rgbValue darkHex:(NSInteger)darkRgbValue;

@end

NS_ASSUME_NONNULL_END
