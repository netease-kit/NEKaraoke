// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEAudioEffectKit/NEAudioEffectManager.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NEAudioEffectViewController : UIViewController

@property(nonatomic, assign) NSInteger currentEffectId;

- (instancetype)initWithManager:(NEAudioEffectManager *)manager;

@end

NS_ASSUME_NONNULL_END
