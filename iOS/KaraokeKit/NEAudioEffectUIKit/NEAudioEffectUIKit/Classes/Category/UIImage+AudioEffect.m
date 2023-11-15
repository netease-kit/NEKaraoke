// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAudioEffectUIManager.h"
#import "UIImage+AudioEffect.h"

@implementation UIImage (AudioEffect)

+ (UIImage *)ne_imageWithColor:(UIColor *)color size:(CGSize)size {
  CGRect rect = CGRectMake(0, 0, size.width, size.height);
  UIGraphicsBeginImageContext(rect.size);
  CGContextRef context = UIGraphicsGetCurrentContext();
  CGContextSetFillColorWithColor(context, color.CGColor);
  CGContextFillRect(context, rect);
  UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  return image;
}

+ (UIImage *)ne_imageWithColor:(UIColor *)color {
  CGRect rect = CGRectMake(0, 0, 1, 1);
  UIGraphicsBeginImageContextWithOptions(rect.size, NO, [UIScreen mainScreen].scale);
  CGContextRef context = UIGraphicsGetCurrentContext();
  CGContextSetFillColorWithColor(context, color.CGColor);
  CGContextFillRect(context, rect);
  UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  return image;
}

- (UIImage *)ne_imageWithTintColor:(UIColor *)tintColor {
  return [self ne_imageWithTintColor:tintColor blendMode:kCGBlendModeDestinationIn];
}

- (UIImage *)ne_imageWithGradientTintColor:(UIColor *)tintColor {
  return [self ne_imageWithTintColor:tintColor blendMode:kCGBlendModeOverlay];
}

- (UIImage *)ne_imageWithTintColor:(UIColor *)tintColor blendMode:(CGBlendMode)blendMode {
  // We want to keep alpha, set opaque to NO; Use 0.0f for scale to use the
  // scale factor of the device’s main screen.
  UIGraphicsBeginImageContextWithOptions(self.size, NO, 0.0f);
  [tintColor setFill];
  CGRect bounds = CGRectMake(0, 0, self.size.width, self.size.height);
  UIRectFill(bounds);

  // Draw the tinted image in context
  [self drawInRect:bounds blendMode:blendMode alpha:1.0f];

  if (blendMode != kCGBlendModeDestinationIn) {
    [self drawInRect:bounds blendMode:kCGBlendModeDestinationIn alpha:1.0f];
  }

  UIImage *tintedImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();

  return tintedImage;
}

+ (UIImage *)ne_imageNamed:(NSString *)name {
  NSBundle *bundle = [NSBundle bundleForClass:[NEAudioEffectUIManager class]];
  if (!bundle) {
    bundle = [NSBundle mainBundle];
  }
  return [UIImage imageNamed:name inBundle:bundle compatibleWithTraitCollection:nil];
}

@end
