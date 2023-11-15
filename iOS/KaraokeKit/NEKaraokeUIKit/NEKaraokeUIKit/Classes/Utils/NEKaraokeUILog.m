// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeUILog.h"
#import <NECoreKit/XKitLog.h>

static XKitLog *_log = nil;

@implementation NEKaraokeUILog
+ (void)setUp:(NSString *)appkey {
  XKitLogOptions *options = [[XKitLogOptions alloc] init];
  options.level = XKitLogLevelInfo;
  options.moduleName = @"KaraokeUI";
  options.sensitives = @[ appkey ];
  _log = [XKitLog setUp:options];
}
+ (void)infoLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className desc:[NSString stringWithFormat:@"⚠️ %@", desc]];
}
+ (void)successLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className desc:[NSString stringWithFormat:@"✅ %@", desc]];
}
/// error类型 log
+ (void)errorLog:(NSString *)className desc:(NSString *)desc {
  [_log errorLog:className desc:[NSString stringWithFormat:@"❌ %@", desc]];
}
+ (void)messageLog:(NSString *)className desc:(NSString *)desc {
  [_log infoLog:className desc:[NSString stringWithFormat:@"✉️ %@", desc]];
}
@end
