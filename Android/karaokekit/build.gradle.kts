/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33
    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path("src/main/cpp/CMakeLists.txt")
        }
    }

    lint {
        disable += "IconDensities"
    }
    ndkVersion = "23.1.7779620"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.netease.yunxin.kit:alog:1.1.0")
    implementation("com.netease.yunxin.kit.common:common:1.3.1")
    implementation("com.netease.yunxin.kit.common:common-network:1.1.8")
    api("com.netease.yunxin.kit.room:roomkit:1.34.0")
    api("com.netease.yunxin.kit.copyrightedmedia:copyrightedmedia:1.8.0")
    api("com.netease.yunxin:nertc-base:5.6.40")
}