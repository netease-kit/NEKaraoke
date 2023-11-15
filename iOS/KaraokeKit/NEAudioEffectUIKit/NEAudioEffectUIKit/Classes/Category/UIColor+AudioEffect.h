// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIColor (AudioEffect)

+ (UIColor *)ne_colorWithHex:(NSInteger)rgbValue alpha:(float)alpha;

+ (UIColor *)ne_colorWithHex:(NSInteger)rgbValue;

@end

NS_ASSUME_NONNULL_END
