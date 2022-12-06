// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeTaskQueue.h"
#import "NEKaraokeCustomTimer.h"

@implementation NEKaraokeTask

+ (NEKaraokeTask *)defaultChorusMatchTask {
  NEKaraokeTask *task = [[NEKaraokeTask alloc] init];
  task.addTime = [[NSDate date] timeIntervalSince1970];
  task.wholeLeftTime = 10 * 1000;
  task.currentLeftTime = 10 * 100;
  task.type = NEKaraokeTaskChorusMatch;
  return task;
}

+ (NEKaraokeTask *)defaultSoloWaitTask {
  NEKaraokeTask *task = [[NEKaraokeTask alloc] init];
  task.addTime = [[NSDate date] timeIntervalSince1970];
  task.wholeLeftTime = 5 * 1000;
  task.currentLeftTime = 5 * 100;
  task.type = NEKaraokeTaskSoloWait;
  return task;
}

+ (NEKaraokeTask *)defaultLoadSourceTask {
  NEKaraokeTask *task = [[NEKaraokeTask alloc] init];
  task.addTime = [[NSDate date] timeIntervalSince1970];
  task.wholeLeftTime = 15 * 1000;
  task.currentLeftTime = 15 * 100;
  task.type = NEKaraokeTaskLoadSource;
  return task;
}

@end

@interface NEKaraokeTaskQueue ()

// 目前场景同时只会有一个定时任务
@property(nonatomic, strong) NEKaraokeTask *task;
@property(nonatomic, strong) dispatch_queue_t timeQueue;
@property(nonatomic, strong) NEKaraokeCustomTimer *timer;

@end

@implementation NEKaraokeTaskQueue

- (instancetype)init {
  if ([super init]) {
    self.timeQueue = dispatch_queue_create("com.karaoke.timer", DISPATCH_QUEUE_SERIAL);
  }
  return self;
}

- (void)addTask:(NEKaraokeTask *)task {
  if (self.task) {
    if (self.taskCanceledBlock) {
      self.taskCanceledBlock(self.task);
    }
    self.task = nil;
  }
  self.task = task;
  self.task.addTime = [[NSDate date] timeIntervalSince1970];
}

- (void)removeTask {
  if (self.task) {
    if (self.taskCanceledBlock) {
      self.taskCanceledBlock(self.task);
    }
    self.task = nil;
  }
}

- (void)start {
  __weak typeof(self) weakSelf = self;
  dispatch_async(self.timeQueue, ^() {
    __strong typeof(self) strongSelf = weakSelf;
    if (strongSelf && !strongSelf.timer) {
      strongSelf.timer = [[NEKaraokeCustomTimer alloc] initWithTimeInterval:0.3
                                                                     target:strongSelf
                                                                   selector:@selector(timerDown)
                                                                    repeats:YES];
      [[NSRunLoop currentRunLoop] addTimer:strongSelf.timer.timer forMode:NSRunLoopCommonModes];
      [[NSRunLoop currentRunLoop] run];
    }
  });
}

- (void)stop {
  [self.timer invalidate];
  self.timer = nil;
}

- (void)timerDown {
  if (self.task) {
    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    self.task.currentLeftTime = self.task.wholeLeftTime - (currentTime - self.task.addTime) * 1000;
    if (self.task.currentLeftTime <= 0) {
      if (self.taskCompleteBlock) {
        self.task.currentLeftTime = 0;
        self.taskCompleteBlock(self.task);
      }
      self.task = nil;
    } else {
      if (self.taskProgressBlock) {
        self.taskProgressBlock(self.task);
      }
    }
  }
}

@end
