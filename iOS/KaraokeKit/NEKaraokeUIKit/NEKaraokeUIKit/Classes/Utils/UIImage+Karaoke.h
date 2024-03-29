// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIImage (Karaoke)

/// 从当前bundle加载图片
/// @param name 图片名
+ (UIImage *)karaoke_imageNamed:(NSString *)name;

/**
为图片染色

@param tintColor 渲染颜色
@return 染色后的图片
*/
- (UIImage *)karaoke_imageWithTintColor:(UIColor *)tintColor;

@end

NS_ASSUME_NONNULL_END
