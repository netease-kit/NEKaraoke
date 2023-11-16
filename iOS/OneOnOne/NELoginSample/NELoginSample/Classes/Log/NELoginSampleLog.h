// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/// 日志
@interface NELoginSampleLog : NSObject
/// 初始化
+ (void)setUp:(NSString *)appkey;
/// API类型 log
+ (void)apiLog:(NSString *)className desc:(NSString *)desc;
/// info类型 log
+ (void)infoLog:(NSString *)className desc:(NSString *)desc;
/// warn类型 log
+ (void)warnLog:(NSString *)className desc:(NSString *)desc;
/// success类型 log
+ (void)successLog:(NSString *)className desc:(NSString *)desc;
/// error类型 log
+ (void)errorLog:(NSString *)className desc:(NSString *)desc;
/// 自定义信息日志
+ (void)messageLog:(NSString *)className desc:(NSString *)desc;
/// 网络日志
+ (void)networkLog:(NSString *)className desc:(NSString *)desc;
@end

NS_ASSUME_NONNULL_END
