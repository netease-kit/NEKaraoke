// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import "NMCTPCircularBuffer.h"
NS_ASSUME_NONNULL_BEGIN

@interface NEKaraokeConversion : NSObject
@property(nonatomic, assign) NMCTPCircularBuffer *recvBuffer;
+ (instancetype)shared;
- (int32_t)bufferFillCount;
- (BOOL)circularBufferProduceBytesWithSrc:(const void *)src len:(int32_t)len;
- (void *)circularBufferTailWithAvailableBytes:(int32_t *)availableBytes;
- (void)circularBufferConsumeWithAmount:(int32_t)amount;
@end

NS_ASSUME_NONNULL_END
