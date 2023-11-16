// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAudioEffectUIManager.h"
#import "NEActionSheetNavigationController.h"
#import "NEAudioEffectViewController.h"

@implementation NEAudioEffectUIManager

+ (NEAudioEffectViewController *)showAudioEffectViewController:(UIViewController *)parentController
                                                       manager:(NEAudioEffectManager *)manager
                                                      effectId:(NSInteger)effectId {
  NEAudioEffectViewController *vc = [[NEAudioEffectViewController alloc] initWithManager:manager];
  vc.currentEffectId = effectId;
  NEActionSheetNavigationController *nav =
      [[NEActionSheetNavigationController alloc] initWithRootViewController:vc];
  nav.dismissOnTouchOutside = YES;
  [parentController presentViewController:nav animated:YES completion:nil];
  return vc;
}

@end
