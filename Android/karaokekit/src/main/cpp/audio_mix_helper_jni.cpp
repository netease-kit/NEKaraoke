// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#include "audio_mix_helper.h"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_netease_yunxin_kit_karaokekit_impl_utils_AudioMixHelper_mixAudioFrameData(
    JNIEnv* env, jobject thiz, jbyteArray dest, jbyteArray src,
    jint samples_per_channel, jint number_of_channels) {
  if (dest == nullptr) {
    __android_log_print(ANDROID_LOG_ERROR, "ORC",
                        "mixAudioFrameData dest == null");
    return nullptr;
  }

  if (src == nullptr) {
    __android_log_print(ANDROID_LOG_ERROR, "ORC",
                        "mixAudioFrameData src == null");
    return nullptr;
  }

  int dest_length = env->GetArrayLength(dest);

  jbyte* dest_buf = env->GetByteArrayElements(dest, JNI_FALSE);
  jbyte* src_buf = env->GetByteArrayElements(src, JNI_FALSE);

  MixToFloatFrame((int16_t*)dest_buf, (int16_t*)src_buf, (int16_t*)dest_buf,
                  samples_per_channel, number_of_channels);

  jbyteArray array = env->NewByteArray(dest_length);
  env->SetByteArrayRegion(array, 0, dest_length, dest_buf);

  env->ReleaseByteArrayElements(dest, dest_buf, 0);
  env->ReleaseByteArrayElements(src, src_buf, 0);

  return array;
}
