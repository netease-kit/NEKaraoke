// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@import NEKaraokeKit;

NS_ASSUME_NONNULL_BEGIN

/// 登录事件枚举
typedef NS_ENUM(NSInteger, NEKaraokeClientEvent) {
  /// 被踢出登录
  NEKaraokeClientEventKicOut,
  /// 服务器禁止登录
  NEKaraokeClientEventForbidden,
  /// 账号或密码错误
  NEKaraokeClientEventAccountTokenError,
  /// 登录成功
  NEKaraokeClientEventLoggedIn,
  /// 未登录
  NEKaraokeClientEventLoggedOut,
  /// 授权错误
  NEKaraokeClientEventIncorrectToken,
  /// Token过期
  NEKaraokeClientEventTokenExpored,
};

@protocol NEKaraokeUIDelegate <NSObject>

- (void)onKaraokeClientEvent:(NEKaraokeClientEvent)event;
- (void)onKaraokeJoinRoom;
- (void)onKaraokeLeaveRoom;
- (BOOL)inOtherRoom;
- (void)leaveOtherRoomWithCompletion:(void (^__nullable)(void))completion;

@end

@interface NEKaraokeUIManager : NSObject

@property(nonatomic, copy) NSString *nickname;

@property(nonatomic, assign, readonly) bool isLoggedIn;

@property(nonatomic, weak) id<NEKaraokeUIDelegate> delegate;

@property(nonatomic, assign) NSInteger configId;

+ (NEKaraokeUIManager *)sharedInstance;

- (void)initializeWithConfig:(NEKaraokeKitConfig *)config
                    configId:(NSInteger)configId
                    callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

- (void)loginWithAccount:(NSString *)account
                   token:(NSString *)token
                nickname:(NSString *)nickname
                callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

- (void)logoutWithCallback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

/// 房间创建界面
- (UINavigationController *)createViewController;

/// 房间列表页
- (UINavigationController *)roomListViewController;

@end

NS_ASSUME_NONNULL_END
