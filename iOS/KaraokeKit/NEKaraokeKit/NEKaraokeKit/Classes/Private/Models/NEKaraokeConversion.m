// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeConversion.h"

#define kRecvBufferMaxSize 2 * 2 * 480 * 10
static NEKaraokeConversion *_conversion = nil;
@implementation NEKaraokeConversion
+ (instancetype)shared {
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    _conversion = [NEKaraokeConversion new];
  });
  return _conversion;
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _recvBuffer = (NMCTPCircularBuffer *)malloc(sizeof(NMCTPCircularBuffer));  // 需要释放
    NMCTPCircularBufferInit(_recvBuffer, kRecvBufferMaxSize);
  }
  return self;
}
- (int32_t)bufferFillCount {
  return _recvBuffer->fillCount;
}
- (BOOL)circularBufferProduceBytesWithSrc:(const void *)src len:(int32_t)len {
  return NMCTPCircularBufferProduceBytes(_recvBuffer, src, len);
}
- (void *)circularBufferTailWithAvailableBytes:(int32_t *)availableBytes {
  return NMCTPCircularBufferTail(_recvBuffer, availableBytes);
}
- (void)circularBufferConsumeWithAmount:(int32_t)amount {
  NMCTPCircularBufferConsume(_recvBuffer, amount);
}
@end
