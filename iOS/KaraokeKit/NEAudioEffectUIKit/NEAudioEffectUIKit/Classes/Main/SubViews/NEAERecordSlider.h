// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@interface NEAERecordSlider : UIView

/// 顶部中间显示当前的value，default: false
@property(nonatomic, assign) BOOL showValue;

/// 滑块连续滑动,default: true
@property(nonatomic, getter=isContinuous) BOOL continuous;

/// 非连续滑动的步长
@property(nonatomic, assign) CGFloat stepValue;
/// 当前滑块所处的value
@property(nonatomic, readonly) CGFloat currentValue;

/// 滑块初始化的时候默认的value
@property(nonatomic, assign) CGFloat defaultValue;

/// 滑动时候的回调
@property(nonatomic, copy, nullable) void (^valueChangedBlock)(CGFloat value);
/// 开始滑动时候的回调
@property(nonatomic, copy, nullable) void (^slideBeginBlock)(CGFloat value);

/// 手指离开的回调
@property(nonatomic, copy, nullable) void (^slideEndedBlock)(CGFloat value);

/// 推荐的值
@property(nonatomic, assign) CGFloat recommendValue;

/// 显示推荐的value
@property(nonatomic, assign) BOOL showRecommendValue;

/// 滑杆到达推荐值的时候就震动
@property(nonatomic, assign) BOOL shakeWhenReachRecommendValue;
@property(nonatomic, assign) BOOL tapShowBubble;

@property(nonatomic, strong)
    UIFont *bubbleFont;  // default:[UIFont systemFontOfSize:20 weight:UIFontWeightBold];

@property(nullable, nonatomic, strong) UIColor *minimumTrackTintColor;

@property(nullable, nonatomic, strong) UIColor *maximumTrackTintColor;
@property(nullable, nonatomic, strong) UIColor *thumbTintColor;
@property(nullable, nonatomic, strong) UIColor *recommendTintColor;

/// 手指跟踪
@property(readonly) BOOL isTracking;

/// 允许的最大值
@property(readonly) CGFloat maxValue;

/// 允许的最小值
@property(readonly) CGFloat minValue;

/// 滑动到指定value
/// @param value 值
/// @param animated 动画
- (void)slideToValue:(CGFloat)value animated:(BOOL)animated;

/// 初始化
/// @param min 最小值，允许为负值
/// @param max 最大值，允许为负值
- (instancetype)initWithMinValue:(CGFloat)min maxValue:(CGFloat)max;

/// continuous = NO时候生效， 增加一个step
- (void)increase;
- (void)increaseWithStep:(CGFloat)step;

/// continuous = NO时候生效， 减少一个step
- (void)decrease;
- (void)decreaseWithStep:(CGFloat)step;

- (void)hideBubbleView;

@end

NS_ASSUME_NONNULL_END
