// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "ViewController.h"
#import <NEKaraokeUIKit/NEKaraokeUIManager.h>
#import <YXLogin/YXLogin.h>
#import "MyViewController.h"
#import "NECustomTabBar.h"

@interface ViewController ()
@property(nonatomic, strong) MyViewController *my;
@end

@implementation ViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.

  NECustomTabBar *tabBar = [[NECustomTabBar alloc] initWithFrame:self.tabBar.frame];
  [self setValue:tabBar forKeyPath:@"tabBar"];
  [tabBar.customButton addTarget:self
                          action:@selector(push:)
                forControlEvents:UIControlEventTouchUpInside];

  UINavigationController *list = [[NEKaraokeUIManager sharedInstance] roomListViewController];
  list.tabBarItem.title = @"在线K歌";
  list.tabBarItem.image =
      [[UIImage imageNamed:@"tab1"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];

  self.my = [[MyViewController alloc] init];
  self.my.tabBarItem.title = @"我的";
  [self updateTabIcon];
  self.viewControllers =
      @[ list, [[UINavigationController alloc] initWithRootViewController:self.my] ];
}

- (void)updateTabIcon {
  dispatch_async(dispatch_get_main_queue(), ^{
    if ([AuthorManager shareInstance].isLogin) {
      YXUserInfo *info = [[AuthorManager shareInstance] getUserInfo];

      UIImage *image =
          [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:info.avatar]]];
      NSData *data = UIImageJPEGRepresentation(image, 0.5);
      UIImage *resultImage = [UIImage imageWithData:data];
      CGSize size = CGSizeMake(20, 20);
      UIGraphicsBeginImageContext(size);
      [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
      resultImage = UIGraphicsGetImageFromCurrentImageContext();
      UIGraphicsEndImageContext();
      // 1.加载原图
      UIImage *newImage = [self circleImageWithImage:resultImage
                                         borderWidth:0
                                         borderColor:[UIColor clearColor]];
      self.my.tabBarItem = nil;
      self.my.tabBarItem = [[UITabBarItem alloc]
          initWithTitle:@"我的"
                  image:[newImage imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal]
                    tag:nil];

    } else {
      self.my.tabBarItem = nil;
      self.my.tabBarItem = [[UITabBarItem alloc]
          initWithTitle:@"我的"
                  image:[[UIImage imageNamed:@"tab2"]
                            imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal]
                    tag:nil];
    }
  });
}
- (void)push:(UIButton *)sender {
  UINavigationController *create = [[NEKaraokeUIManager sharedInstance] createViewController];
  [self presentViewController:create animated:true completion:nil];
}
- (BOOL)shouldAutorotate {
  return NO;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
  return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
  return UIInterfaceOrientationPortrait;
}

- (UIImage *)circleImageWithImage:(UIImage *)oldImage
                      borderWidth:(CGFloat)borderWidth
                      borderColor:(UIColor *)borderColor {
  // 1.加载原图
  //    UIImage *oldImage = [UIImage imageNamed:name];
  // 2.开启上下文
  CGFloat imageW = oldImage.size.width + 2 * borderWidth;
  CGFloat imageH = oldImage.size.height + 2 * borderWidth;
  CGSize imageSize = CGSizeMake(imageW, imageH);
  UIGraphicsBeginImageContextWithOptions(imageSize, NO, 0.0);
  // 3.取得当前的上下文
  CGContextRef ctx = UIGraphicsGetCurrentContext();
  // 4.画边框(大圆)
  [borderColor set];
  CGFloat bigRadius = imageW * 0.5;  // 大圆半径
  CGFloat centerX = bigRadius;       // 圆心
  CGFloat centerY = bigRadius;
  CGContextAddArc(ctx, centerX, centerY, bigRadius, 0, M_PI * 2, 0);
  CGContextFillPath(ctx);  // 画圆
  // 5.小圆
  CGFloat smallRadius = bigRadius - borderWidth;
  CGContextAddArc(ctx, centerX, centerY, smallRadius, 0, M_PI * 2, 0);
  // 裁剪(后面画的东西才会受裁剪的影响)
  CGContextClip(ctx);
  // 6.画图
  [oldImage
      drawInRect:CGRectMake(borderWidth, borderWidth, oldImage.size.width, oldImage.size.height)];
  // 7.取图
  UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
  // 8.结束上下文
  UIGraphicsEndImageContext();
  return newImage;
}
@end
