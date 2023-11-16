/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
import java.util.Properties

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33
    buildFeatures {
        viewBinding = true
    }
    lint {
        disable += "IconDensities"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.netease.yunxin.kit:alog:1.1.0")
    api(project(":karaokekit-audioeffect"))
    implementation("com.netease.yunxin:nertc-base:5.5.203-SNAPSHOT")
}