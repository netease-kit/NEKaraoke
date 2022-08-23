/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 30
        applicationId = "com.netease.yunxin.app.karaoke"
        versionCode = 2
        versionName = "1.1.0"
        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }
}


dependencies {
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")

    implementation("com.netease.yunxin.kit:alog:1.0.2")
    implementation("com.netease.yunxin.kit.auth:auth-yunxin-login:1.0.1")

    implementation(project(":karaokekit-ui"))


    implementation("com.netease.yunxin.kit.karaoke:karaokekit:1.1.0")
    implementation("com.netease.yunxin.kit.common:common-image:1.1.5")

    implementation("com.gyf.immersionbar:immersionbar:3.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.scwang.smart:refresh-layout-kernel:2.0.1")
    implementation("com.blankj:utilcodex:1.30.6")
    implementation("com.tencent.bugly:crashreport:3.3.9")

    implementation("androidx.test:rules:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
