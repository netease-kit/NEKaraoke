// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIView (AudioEffect)

#pragma makr - Basic
@property(nonatomic) CGFloat left;     // CGRectGetMinX(self.frame)
@property(nonatomic) CGFloat right;    // CGRectGetMaxX(self.frame)
@property(nonatomic) CGFloat top;      // CGRectGetMinY(self.frame)
@property(nonatomic) CGFloat bottom;   // CGRectGetMaxY(self.frame)
@property(nonatomic) CGFloat x;        // same with x
@property(nonatomic) CGFloat y;        // same with y
@property(nonatomic) CGFloat centerX;  // self.center.x
@property(nonatomic) CGFloat centerY;  // self.center.y
@property(nonatomic) CGPoint origin;   // self.frame.origin

@property(nonatomic) CGFloat height;  // CGRectGetHeight(self.frame)
@property(nonatomic) CGFloat width;   // CGRectGetWidth(self.frame)
@property(nonatomic) CGSize size;     // self.frame.size

#pragma mark - Alingment
/**
 中心对齐

 @param view view description
 */
- (void)centerEqualToView:(UIView *)view;
- (void)centerEqualToView:(UIView *)view offset:(CGPoint)offset;

/**
 水平对齐

 @param view view description
 */
- (void)centerXEqualToView:(UIView *)view;
- (void)centerXEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 垂直对齐

 @param view view description
 */
- (void)centerYEqualToView:(UIView *)view;
- (void)centerYEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 顶部对齐

 @param view view description
 */
- (void)topEqualToView:(UIView *)view;

/**
 顶部对齐

 @param view view description
 @param offset 偏移量
 */
- (void)topEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 底部对齐

 @param view view description
 */
- (void)bottomEqualToView:(UIView *)view;

/**
 底部对齐

 @param view view description
 @param offset 偏移量
 */
- (void)bottomEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 左对齐

 @param view view description
 */
- (void)leftEqualToView:(UIView *)view;

/**
 左对齐

 @param view view description
 @param offset 偏移量
 */
- (void)leftEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 右对齐

 @param view view description
 */
- (void)rightEqualToView:(UIView *)view;

/**
 右对齐

 @param view view description
 @param offset 偏移量
 */
- (void)rightEqualToView:(UIView *)view offset:(CGFloat)offset;

/**
 把 receiver 放到 view 上面

 @param view view description
 @param offset 偏移量
 */
- (void)above:(UIView *)view offset:(CGFloat)offset;

/**
 把 receiver 放到 view 下面

 @param view view description
 @param offset 偏移量
 */
- (void)below:(UIView *)view offset:(CGFloat)offset;

/**
 把 receiver 放到 view 左边

 @param view view description
 @param offset 偏移量
 */
- (void)leftFrom:(UIView *)view offset:(CGFloat)offset;

/**
 把 receiver 放到 view 右边

 @param view view description
 @param offset 偏移量
 */
- (void)rightFrom:(UIView *)view offset:(CGFloat)offset;

#pragma mark - Size
/**
 设置高度和 view 相同

 @param view view description
 */
- (void)heightEqualToView:(UIView *)view;

/**
 设置宽度和 view 相同

 @param view view description
 */
- (void)widthEqualToView:(UIView *)view;

/**
 设置大小，可以在四周空出部分区域

 @param size size description
 @param padding 上下左右空出的边界值
 */
- (void)setSize:(CGSize)size padding:(CGFloat)padding;

/**
 设置大小，可以在四周空出部分区域

 @param size size description
 @param inset 上下左右空出的边界值
 */
- (void)setSize:(CGSize)size inset:(UIEdgeInsets)inset;

/**
 设置大小和 view 相同

 @param view view description
 */
- (void)sizeEqualToView:(UIView *)view;

/**
 设置大小和 view 相同


 @param view view description
 @param padding 上下左右空出的边界值
 */
- (void)sizeEqualToView:(UIView *)view padding:(CGFloat)padding;

/**
 设置大小和 view 相同

 @param view view description
 @param inset 上下左右空出的边界值
 */
- (void)sizeEqualToView:(UIView *)view inset:(UIEdgeInsets)inset;

/**
 设置 width 和 superview 相同。不会修改 origin
 */
- (void)fillWidth;

/**
 设置 height 和 superview 相同，不会修改 origin
 */
- (void)fillHeight;

/**
 设置 size 和 superview 相同，不会修改 origin
 */
- (void)fill;

- (UIView *)topSuperView;

/**
 把 width 和 height 小数进一位
 */
- (void)ceilViewSize;

#pragma mark - Resize
/**
 保持 left 不变的情况下设置 right 到 view.right/left。会导致 width 变化。
 当 newRight 小于 left 时，直接返回

 @param view view description
 @param offset offset description
 */
- (void)moveRightToViewLeft:(UIView *)view offset:(CGFloat)offset;
- (void)moveRightToViewRight:(UIView *)view offset:(CGFloat)offset;

/**
 保持 right 不变的情况下设置 left 到 view.right/left。会导致 width 变化。
 当 newLeft 小于 right 时，直接返回

 @param view view description
 @param offset offset description
 */
- (void)moveLeftToViewLeft:(UIView *)view offset:(CGFloat)offset;
- (void)moveLeftToViewRight:(UIView *)view offset:(CGFloat)offset;

/**
 保持 top 不变的情况下设置 bottom 到 view.top/bottom。会导致 height 变化。
 当 newBottom 小于 top 时，直接返回

 @param view view description
 @param offset offset description
 */
- (void)moveBottomToViewTop:(UIView *)view offset:(CGFloat)offset;
- (void)moveBottomToViewBottom:(UIView *)view offset:(CGFloat)offset;

/**
 保持 bottom 不变的情况下设置 top 到 view.top/bottom。会导致 height 变化。
 当 newTop 小于 bottom 时，直接返回

 @param view view description
 @param offset offset description
 */
- (void)moveTopToViewTop:(UIView *)view offset:(CGFloat)offset;
- (void)moveTopToViewBottom:(UIView *)view offset:(CGFloat)offset;

@end

NS_ASSUME_NONNULL_END
