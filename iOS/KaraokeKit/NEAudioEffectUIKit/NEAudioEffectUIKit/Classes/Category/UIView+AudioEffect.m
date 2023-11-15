// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "UIView+AudioEffect.h"

@implementation UIView (AudioEffect)

#pragma makr - Basic
// coordinator getters
- (CGFloat)left {
  return self.x;
}
- (void)setLeft:(CGFloat)left {
  self.x = left;
}

- (CGFloat)right {
  //    return self.frame.size.width + self.frame.origin.x;
  return CGRectGetMaxX(self.frame);
}
- (void)setRight:(CGFloat)right {
  self.x = right - self.frame.size.width;
}

- (CGFloat)top {
  return self.y;
}
- (void)setTop:(CGFloat)top {
  self.y = top;
}

- (CGFloat)bottom {
  //    return self.frame.size.height + self.frame.origin.y;
  return CGRectGetMaxY(self.frame);
}
- (void)setBottom:(CGFloat)bottom {
  self.y = bottom - self.frame.size.height;
}

- (CGFloat)x {
  //    return self.frame.origin.x;
  return CGRectGetMinX(self.frame);
}
- (void)setX:(CGFloat)x {
  self.frame = CGRectMake(x, self.frame.origin.y, self.frame.size.width, self.frame.size.height);
}

- (CGFloat)y {
  //    return self.frame.origin.y;
  return CGRectGetMinY(self.frame);
}
- (void)setY:(CGFloat)y {
  self.frame = CGRectMake(self.frame.origin.x, y, self.frame.size.width, self.frame.size.height);
}

- (CGFloat)centerX {
  return self.center.x;
}
- (void)setCenterX:(CGFloat)centerX {
  CGPoint center = CGPointMake(self.centerX, self.centerY);
  center.x = centerX;
  self.center = center;
}

- (CGFloat)centerY {
  return self.center.y;
}
- (void)setCenterY:(CGFloat)centerY {
  CGPoint center = CGPointMake(self.centerX, self.centerY);
  center.y = centerY;
  self.center = center;
}
- (CGPoint)origin {
  return self.frame.origin;
}

- (void)setOrigin:(CGPoint)origin {
  CGRect frame = self.frame;
  frame.origin = origin;
  self.frame = frame;
}

- (CGFloat)height {
  //    return self.frame.size.height;
  return CGRectGetHeight(self.frame);
}
- (void)setHeight:(CGFloat)height {
  CGRect newFrame = CGRectMake(self.x, self.y, self.width, height);
  self.frame = newFrame;
}

- (CGFloat)width {
  //    return self.frame.size.width;
  return CGRectGetWidth(self.frame);
}
- (void)setWidth:(CGFloat)width {
  CGRect newFrame = CGRectMake(self.x, self.y, width, self.height);
  self.frame = newFrame;
}

- (CGSize)size {
  return self.frame.size;
}
- (void)setSize:(CGSize)size {
  self.frame = CGRectMake(self.x, self.y, size.width, size.height);
}

#pragma mark - Alingment
// center
- (void)centerEqualToView:(UIView *)view {
  [self centerXEqualToView:view];
  [self centerYEqualToView:view];
}

- (void)centerEqualToView:(UIView *)view offset:(CGPoint)offset {
  [self centerEqualToView:view];
  self.centerX += offset.x;
  self.centerY += offset.y;
}

- (void)centerXEqualToView:(UIView *)view {
  UIView *superView = view.superview ? view.superview : view;
  CGPoint viewCenterPoint = [superView convertPoint:view.center toView:self.topSuperView];
  CGPoint centerPoint = [self.topSuperView convertPoint:viewCenterPoint toView:self.superview];
  self.centerX = centerPoint.x;
}

- (void)centerXEqualToView:(UIView *)view offset:(CGFloat)offset {
  [self centerXEqualToView:view];
  self.centerX += offset;
}

- (void)centerYEqualToView:(UIView *)view {
  UIView *superView = view.superview ? view.superview : view;
  CGPoint viewCenterPoint = [superView convertPoint:view.center toView:self.topSuperView];
  CGPoint centerPoint = [self.topSuperView convertPoint:viewCenterPoint toView:self.superview];
  self.centerY = centerPoint.y;
}

- (void)centerYEqualToView:(UIView *)view offset:(CGFloat)offset {
  [self centerYEqualToView:view];
  self.centerY += offset;
}

- (void)topEqualToView:(UIView *)view {
  [self topEqualToView:view offset:0];
}
- (void)topEqualToView:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.y = newOrigin.y + offset;
}

- (void)bottomEqualToView:(UIView *)view {
  [self bottomEqualToView:view offset:0];
}
- (void)bottomEqualToView:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.y = newOrigin.y + view.height - self.height + offset;
}

- (void)leftEqualToView:(UIView *)view {
  [self leftEqualToView:view offset:0];
}
- (void)leftEqualToView:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.x = newOrigin.x + offset;
}

- (void)rightEqualToView:(UIView *)view {
  [self rightEqualToView:view offset:0];
}
- (void)rightEqualToView:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.x = newOrigin.x + view.width - self.width + offset;
}

- (void)above:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.y = newOrigin.y - self.height + offset;
}

- (void)below:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.y = newOrigin.y + view.height + offset;
}

- (void)leftFrom:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.x = newOrigin.x - self.width + offset;
}
- (void)rightFrom:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  self.x = newOrigin.x + view.width + offset;
}

#pragma mark - Size
- (void)heightEqualToView:(UIView *)view {
  self.height = view.height;
}
- (void)widthEqualToView:(UIView *)view {
  self.width = view.width;
}

- (void)setSize:(CGSize)size padding:(CGFloat)padding {
  [self setSize:size inset:UIEdgeInsetsMake(padding, padding, padding, padding)];
}
- (void)setSize:(CGSize)size inset:(UIEdgeInsets)inset {
  CGPoint center = self.center;
  self.frame = CGRectMake(self.x, self.y, size.width, size.height);
  self.center = center;
  self.frame = UIEdgeInsetsInsetRect(self.frame, inset);
}

- (void)sizeEqualToView:(UIView *)view {
  self.frame = CGRectMake(self.x, self.y, view.width, view.height);
}
- (void)sizeEqualToView:(UIView *)view padding:(CGFloat)padding {
  [self setSize:view.size padding:padding];
}
- (void)sizeEqualToView:(UIView *)view inset:(UIEdgeInsets)inset {
  [self setSize:view.size inset:inset];
}

// imbueset
- (void)fillWidth {
  self.width = self.superview.width;
}

- (void)fillHeight {
  self.height = self.superview.height;
}

- (void)fill {
  self.frame = CGRectMake(0, 0, self.superview.width, self.superview.height);
}

- (UIView *)topSuperView {
  UIView *topSuperView = self.superview;
  if (topSuperView == nil) {
    topSuperView = self;
  } else {
    while (topSuperView.superview) {
      topSuperView = topSuperView.superview;
    }
  }
  return topSuperView;
}

- (void)ceilViewSize {
  self.size = CGSizeMake(ceilf(self.width), ceilf(self.height));
}

#pragma mark - Resize
- (void)moveRightToViewLeft:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  CGFloat final = newOrigin.x + offset;
  if (final < self.left) {
    return;
  }
  self.width = final - self.left;
}
- (void)moveRightToViewRight:(UIView *)view offset:(CGFloat)offset {
  [self moveRightToViewLeft:view offset:(offset + view.width)];
}

- (void)moveLeftToViewLeft:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  CGFloat final = newOrigin.x + offset;
  if (final > self.right) {
    return;
  }
  self.width = self.right - final;
  self.left = final;
}

- (void)moveLeftToViewRight:(UIView *)view offset:(CGFloat)offset {
  [self moveLeftToViewLeft:view offset:(offset + view.width)];
}

- (void)moveBottomToViewTop:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  CGFloat final = newOrigin.y + offset;
  if (final < self.top) {
    return;
  }
  self.height = final - self.top;
}

- (void)moveBottomToViewBottom:(UIView *)view offset:(CGFloat)offset {
  [self moveBottomToViewTop:view offset:(offset + view.height)];
}

- (void)moveTopToViewTop:(UIView *)view offset:(CGFloat)offset {
  CGPoint newOrigin = [self transferViewOriginInSelfCoordinate:view];
  CGFloat final = newOrigin.y + offset;
  if (final > self.bottom) {
    return;
  }
  self.height = self.bottom - final;
  self.top = final;
}

- (void)moveTopToViewBottom:(UIView *)view offset:(CGFloat)offset {
  [self moveTopToViewTop:view offset:(offset + view.height)];
}

#pragma mark - Private
- (CGPoint)transferViewOriginInSelfCoordinate:(UIView *)view {
  if (view == nil) {
    return self.origin;
  }
  UIView *superView = view.superview ? view.superview : view;
  CGPoint viewOrigin = [superView convertPoint:view.origin toView:self.topSuperView];
  CGPoint newOrigin = [self.topSuperView convertPoint:viewOrigin toView:self.superview];
  return newOrigin;
}

@end
