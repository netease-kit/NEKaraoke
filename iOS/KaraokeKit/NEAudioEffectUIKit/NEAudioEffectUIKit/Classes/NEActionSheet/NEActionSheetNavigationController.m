// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEActionSheetNavigationController.h"
#import "NTESActionSheetTransitioningDelegate.h"
#import "UIImage+AudioEffect.h"

@interface NEActionSheetNavigationController ()

// 圆角遮罩
@property(nonatomic, strong) CAShapeLayer *navigationBarMask;

// 转场动画代理
@property(nonatomic, strong) NTESActionSheetTransitioningDelegate *transitioning;

@end

@implementation NEActionSheetNavigationController

@dynamic dismissOnTouchOutside;

- (instancetype)initWithRootViewController:(UIViewController *)rootViewController {
  self = [super initWithRootViewController:rootViewController];
  if (self) {
    self.navigationBar.tintColor = UIColor.blackColor;
    self.navigationBar.clipsToBounds = YES;
    self.navigationBar.translucent = NO;
    [self.navigationBar
        setTitleTextAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:16]}];
    self.navigationBarMask = [[CAShapeLayer alloc] init];
    self.transitioning = [[NTESActionSheetTransitioningDelegate alloc] init];
    self.modalPresentationStyle = UIModalPresentationCustom;
    self.transitioningDelegate = self.transitioning;
    if (@available(iOS 13, *)) {
      UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];
      [appearance configureWithOpaqueBackground];
      appearance.titleTextAttributes = @{
        NSForegroundColorAttributeName : UIColor.whiteColor,
        NSFontAttributeName : [UIFont systemFontOfSize:16]
      };
      appearance.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:1];
      self.navigationBar.standardAppearance = appearance;
      self.navigationBar.scrollEdgeAppearance = appearance;
    }
  }
  return self;
}

- (void)viewDidLayoutSubviews {
  [super viewDidLayoutSubviews];
  self.navigationBar.frame =
      CGRectMake(0, 0, self.view.frame.size.width, 48);  // 按照设计图高度是48
  self.navigationBarMask.frame = self.navigationBar.bounds;
  UIBezierPath *maskCornor =
      [UIBezierPath bezierPathWithRoundedRect:self.navigationBar.bounds
                            byRoundingCorners:UIRectCornerTopLeft | UIRectCornerTopRight
                                  cornerRadii:CGSizeMake(12, 12)];
  self.navigationBarMask.path = maskCornor.CGPath;
  self.navigationBar.layer.mask = self.navigationBarMask;
}

- (id)forwardingTargetForSelector:(SEL)aSelector {
  if ([self.transitioning respondsToSelector:aSelector]) {
    return self.transitioning;
  }
  return [super forwardingTargetForSelector:aSelector];
}

@end
