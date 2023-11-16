// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PlayerAudioMixer : NSObject

+ (void)mixAudioFrameData:(int16_t *)mix_data
                    data2:(int16_t *)src_data2
        samplesPerChannel:(int)samples_per_channel
                 channels:(int)number_of_channels;
+ (void)setAudioFrameDataVolume:(int16_t *)to_data dataLen:(int)len volume:(float)volume;
@end

NS_ASSUME_NONNULL_END
