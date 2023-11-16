// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#ifndef __AUDIO_MIX_HELPER__
#define __AUDIO_MIX_HELPER__

#include <android/log.h>
#include <jni.h>

void MixToFloatFrame(int16_t* src_data, int16_t* src2_data, int16_t* dst_data,
                     size_t samples_per_channel, size_t number_of_channels);
void setAudioFrameVolume(int16_t* frame, float volume, int samples);

#endif  //__AUDIO_MIX_HELPER__
