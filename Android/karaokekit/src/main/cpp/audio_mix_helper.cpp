// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#include "audio_mix_helper.h"

#include <array>
#include <vector>

using namespace std;

//// Stereo, 48 kHz, 10 ms.
static constexpr int kMaximumAmountOfChannels = 2;
static constexpr int kMaximumChannelSize = 48 * 10;

typedef std::numeric_limits<int16_t> limits_int16;

static inline int16_t FloatS16ToS16(float v) {
  static const float kMaxRound = limits_int16::max() - 0.5f;
  static const float kMinRound = limits_int16::min() + 0.5f;
  if (v > 0)
    return v >= kMaxRound ? limits_int16::max()
                          : static_cast<int16_t>(v + 0.5f);
  return v <= kMinRound ? limits_int16::min() : static_cast<int16_t>(v - 0.5f);
}

void MixToFloatFrame(int16_t* src_data, int16_t* src2_data, int16_t* dst_data,
                     size_t samples_per_channel, size_t number_of_channels) {
  if (samples_per_channel > kMaximumChannelSize ||
      number_of_channels > kMaximumAmountOfChannels) {
    return;
  }
  // Convert to FloatS16 and mix.
  using OneChannelBuffer = std::array<float, kMaximumChannelSize>;
  std::array<OneChannelBuffer, kMaximumAmountOfChannels> mixing_buffer{};
  std::vector<int16_t*> mixList = {src_data, src2_data};

  for (size_t i = 0; i < mixList.size(); i++) {
    int16_t* src_buffer = mixList[i];
    for (size_t j = 0; j < number_of_channels; ++j) {
      for (size_t k = 0; k < samples_per_channel; ++k) {
        mixing_buffer[j][k] += src_buffer[number_of_channels * k + j];
      }
    }
  }

  // Put data in the result frame.
  for (size_t i = 0; i < number_of_channels; ++i) {
    for (size_t j = 0; j < samples_per_channel; ++j) {
      dst_data[number_of_channels * j + i] = FloatS16ToS16(mixing_buffer[i][j]);
    }
  }
}

void setAudioFrameVolume(int16_t* frame, float volume, int samples) {
  if (!frame || volume == 0.0) {
    memset(frame, 0, samples * sizeof(int16_t));
    return;
  }

  volume = volume < 0.0f ? 0.0f : (volume > 1.0f ? 1.0f : volume);
  const size_t count = samples;
  for (size_t i = 0; i < count; i++) {
    frame[i] = static_cast<int16_t>(frame[i] * volume);
  }
}