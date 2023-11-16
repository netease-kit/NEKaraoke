// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, NEAERecordSliderBubbleViewDirection) {
  // 箭头方向
  NEAERecordSliderBubbleViewDirectionBottom = 0,
  NEAERecordSliderBubbleViewDirectionRight = 1,
  NEAERecordSliderBubbleViewDirectionUp = 2,
  NEAERecordSliderBubbleViewDirectionLeft = 3,

};

@interface NEAERecordSliderBubbleView : UIView

@property(nonatomic, strong) UIImageView *backgroundImageView;
@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, assign) NEAERecordSliderBubbleViewDirection direction;

@end

NS_ASSUME_NONNULL_END
