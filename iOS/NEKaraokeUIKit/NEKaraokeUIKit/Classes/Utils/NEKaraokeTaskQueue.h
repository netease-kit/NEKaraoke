// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, NEKaraokeTaskType) {
  // 主唱等待合唱加入
  NEKaraokeTaskChorusMatch,
  // 独唱等待
  NEKaraokeTaskSoloWait,
  // 等待副唱加载资源
  NEKaraokeTaskLoadSource,
};

@interface NEKaraokeTask : NSObject

@property(nonatomic, assign) NEKaraokeTaskType type;
@property(nonatomic, assign) NSTimeInterval addTime;
@property(nonatomic, assign) long wholeLeftTime;
@property(nonatomic, assign) long currentLeftTime;

+ (NEKaraokeTask *)defaultChorusMatchTask;
+ (NEKaraokeTask *)defaultSoloWaitTask;
+ (NEKaraokeTask *)defaultLoadSourceTask;

@end

@interface NEKaraokeTaskQueue : NSObject

@property(nonatomic, copy) void (^taskCompleteBlock)(NEKaraokeTask *task);
@property(nonatomic, copy) void (^taskProgressBlock)(NEKaraokeTask *task);
@property(nonatomic, copy) void (^taskCanceledBlock)(NEKaraokeTask *task);

- (void)start;

- (void)stop;

- (void)addTask:(NEKaraokeTask *)task;

- (void)removeTask;

@end

NS_ASSUME_NONNULL_END
