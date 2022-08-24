// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeUIManager.h"
#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import "NEKaraokeCreateViewController.h"
#import "NEKaraokeListViewController.h"
#import "NEKaraokeSongLog.h"
#import "NEKaraokeUILog.h"

@interface NEKaraokeUIManager () <NEKaraokeAuthListener>
@property(nonatomic, strong) NEKaraokeKitConfig *config;
@end

@implementation NEKaraokeUIManager

+ (NEKaraokeUIManager *)sharedInstance {
  static NEKaraokeUIManager *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[NEKaraokeUIManager alloc] init];
    [[NEKaraokeKit shared] addAuthListener:instance];
  });
  return instance;
}
- (instancetype)init {
  self = [super init];
  if (self) {
    [NEKaraokeUILog setup];
  }
  return self;
}
- (void)initializeWithConfig:(NEKaraokeKitConfig *)config
                    callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  self.config = config;
  [NEKaraokeKit.shared initializeWithConfig:config callback:callback];
  [NEKaraokeSongLog setup];
}

- (void)loginWithAccount:(NSString *)account
                   token:(NSString *)token
                nickname:(NSString *)nickname
                callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  self.nickname = nickname;
  __weak typeof(self) weakSelf = self;
  [NEKaraokeKit.shared
         login:account
         token:token
      callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
        if (code == 0) {
          [NEKaraokeKit.shared getSongTokenWithCallback:^(NSInteger code, NSString *_Nullable msg,
                                                          NSString *_Nullable tokenRealTime) {
            __strong typeof(self) strongSelf = weakSelf;
            if (code != 0) {
              NSLog(@"初始化失败");
            } else {
              [[NECopyrightedMedia getInstance] initialize:strongSelf.config.appKey
                                                     token:tokenRealTime
                                                  userUuid:account
                                                    extras:strongSelf.config.extras];
            }
          }];
        }
        callback(code, msg, obj);
      }];
}

- (void)logoutWithCallback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  [NEKaraokeKit.shared logoutWithCallback:callback];
}

- (bool)isLoggedIn {
  return [[NEKaraokeKit shared] isLoggedIn];
}

- (void)onKaraokeAuthEvent:(enum NEKaraokeAuthEvent)event {
  if ([self.delegate respondsToSelector:@selector(onKaraokeClientEvent:)]) {
    [self.delegate onKaraokeClientEvent:event];
  }
}

- (UINavigationController *)createViewController {
  UINavigationController *c = [[UINavigationController alloc]
      initWithRootViewController:[[NEKaraokeCreateViewController alloc] init]];
  c.modalPresentationStyle = UIModalPresentationFullScreen;
  return c;
}

- (UINavigationController *)roomListViewController {
  UINavigationController *c = [[UINavigationController alloc]
      initWithRootViewController:[[NEKaraokeListViewController alloc] init]];
  if (@available(iOS 13.0, *)) {
    c.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
  }
  c.modalPresentationStyle = UIModalPresentationFullScreen;
  return c;
}

@end
