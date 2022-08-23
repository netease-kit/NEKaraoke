// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "AppDelegate.h"
#import <NEKaraokeUIKit/NEKaraokeUIManager.h>
#import <YXLogin/YXLogin.h>
#import <libextobjc/extobjc.h>
#import "ViewController.h"

static const NSString *kAppKey = @"";

@interface AppDelegate () <NEKaraokeDelegate>
@property(nonatomic, strong) ViewController *mainViewController;
@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  self.mainViewController = [[ViewController alloc] init];
  self.window.rootViewController = self.mainViewController;
  [self.window makeKeyAndVisible];

  [application setIdleTimerDisabled:YES];
  [self setupKaraokeKit];
  return YES;
}

- (void)setupKaraokeKit {
  NEKaraokeKitConfig *config = [[NEKaraokeKitConfig alloc] init];
  config.appKey = kAppKey;

  [NEKaraokeUIManager.sharedInstance
      initializeWithConfig:config
                  callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable objc) {
                    if (code != 0) {
                      return;
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                      [self setupLoginSDK];
                    });
                  }];
  [NEKaraokeUIManager sharedInstance].delegate = self;
}
- (void)setupLoginSDK {
  YXConfig *config = [YXConfig new];
  config.appKey = kAppKey;
  config.supportInternationalize = YES;
  config.isOnline = YES;
  config.parentScope = [NSNumber numberWithInt:5];
  config.scope = [NSNumber numberWithInt:5];
  config.type = YXLoginPhone;
  AuthorManager *LoginManager = [AuthorManager shareInstance];
  [LoginManager initAuthorWithConfig:config];
  // 自动登录
  @weakify(self) if ([LoginManager canAutologin]) {
    [LoginManager
        autoLoginWithCompletion:^(YXUserInfo *_Nullable userinfo, NSError *_Nullable error) {
          if (error) return;
          NSLog(@"统一登录sdk登录成功");
          @strongify(self)[self.mainViewController updateTabIcon];
          [NEKaraokeUIManager.sharedInstance
              loginWithAccount:userinfo.accountId
                         token:userinfo.accessToken
                      nickname:userinfo.nickname
                      callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable objc) {
                        if (code != 0) {
                          [LoginManager logoutWithCompletion:^(YXUserInfo *_Nullable userinfo,
                                                               NSError *_Nullable error){

                          }];
                        }
                      }];
        }];
  }
  else {
    [LoginManager
        startEntranceWithCompletion:^(YXUserInfo *_Nullable userinfo, NSError *_Nullable error) {
          if (error) return;
          NSLog(@"统一登录sdk登录成功");
          @strongify(self)[self.mainViewController updateTabIcon];
          [NEKaraokeUIManager.sharedInstance
              loginWithAccount:userinfo.accountId
                         token:userinfo.accessToken
                      nickname:userinfo.nickname
                      callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable objc) {
                        if (code != 0) {
                          [LoginManager logoutWithCompletion:^(YXUserInfo *_Nullable userinfo,
                                                               NSError *_Nullable error){

                          }];
                        }
                      }];
        }];
  }
}

- (void)onKaraokeClientEvent:(NEKaraokeClientEvent)event {
  if (event == NEKaraokeClientEventKicOut) {
    [[AuthorManager shareInstance]
        logoutWithCompletion:^(YXUserInfo *_Nullable userinfo, NSError *_Nullable error) {
          [[AuthorManager shareInstance]
              startLoginWithCompletion:^(YXUserInfo *_Nullable userinfo, NSError *_Nullable error) {
                [NEKaraokeUIManager.sharedInstance
                    loginWithAccount:userinfo.accountId
                               token:userinfo.accessToken
                            nickname:userinfo.nickname
                            callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable objc) {
                              dispatch_async(dispatch_get_main_queue(), ^{
                                [NSNotificationCenter.defaultCenter
                                    postNotification:[NSNotification notificationWithName:@"Login"
                                                                                   object:nil]];
                              });
                            }];
              }];
        }];
  }
}
- (UIInterfaceOrientationMask)application:(UIApplication *)application
    supportedInterfaceOrientationsForWindow:(nullable UIWindow *)window {
  return UIInterfaceOrientationMaskPortrait;
}

@end
