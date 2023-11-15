// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <NEAudioEffectKit/NEAudioEffectManager.h>
#import <UIKit/UIKit.h>
#import "NEAudioEffectViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface NEAudioEffectUIManager : NSObject

+ (NEAudioEffectViewController *)showAudioEffectViewController:(UIViewController *)parentController
                                                       manager:(NEAudioEffectManager *)manager
                                                      effectId:(NSInteger)effectId;

@end

NS_ASSUME_NONNULL_END
