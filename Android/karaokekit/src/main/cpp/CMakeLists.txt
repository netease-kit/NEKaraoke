cmake_minimum_required(VERSION 3.18.1)


add_library(
        ne_karaoke_utils
        SHARED
        audio_mix_helper.cpp
        audio_mix_helper_jni.cpp
)

find_library(
        log-lib
        log )


target_link_libraries(
        ne_karaoke_utils
        android
        ${log-lib}
)