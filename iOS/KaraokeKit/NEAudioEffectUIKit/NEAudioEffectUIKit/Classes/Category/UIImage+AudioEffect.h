// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIImage (AudioEffect)

+ (UIImage *)ne_imageWithColor:(UIColor *)color size:(CGSize)size;

/**
 绘制纯色图片

 @param color 图片颜色
 @return 纯色图片
 */
+ (UIImage *)ne_imageWithColor:(UIColor *)color;

/**
为图片染色

@param tintColor 渲染颜色
@return 染色后的图片
*/
- (UIImage *)ne_imageWithTintColor:(UIColor *)tintColor;

/// 从当前bundle加载图片
/// @param name 图片名
+ (UIImage *)ne_imageNamed:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
