// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NEAERecordSlider.h"

NS_ASSUME_NONNULL_BEGIN

@interface NEAERecordPanelSlideView : UIView

@property(nonatomic, strong) UILabel *titleL;

@property(nonatomic, strong) NEAERecordSlider *slider;
@property(nonatomic, assign) CGFloat sliderRight;

- (instancetype)initWithMinValue:(CGFloat)min maxValue:(CGFloat)max;

@end

NS_ASSUME_NONNULL_END
