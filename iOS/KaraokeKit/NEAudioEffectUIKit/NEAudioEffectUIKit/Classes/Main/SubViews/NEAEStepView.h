// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NEAEStepView : UIView

@property(nonatomic, assign) NSInteger step;

@property(nonatomic, copy, nullable) void (^valueChangedBlock)(NSInteger step);

+ (instancetype)stepViewWithTitle:(NSString *)title
                              max:(NSInteger)max
                              min:(NSInteger)min
                             step:(NSInteger)step;

@end

NS_ASSUME_NONNULL_END
