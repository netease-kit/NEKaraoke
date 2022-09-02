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
    compileSdk = 31
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.airbnb.android:lottie:5.0.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("com.netease.yunxin.kit:alog:1.0.2")


    implementation("com.netease.yunxin.kit.karaoke:karaokekit:1.2.0")
    implementation("com.netease.yunxin.kit.common:common-ui:1.1.6")
    implementation("com.netease.yunxin.kit.common:common-network:1.1.6")
    implementation("com.netease.yunxin.kit.common:common-image:1.1.6")
    implementation("com.netease.yunxin.kit.karaoke:karaokekit-pitch-ui:1.2.0")
    implementation("com.netease.yunxin.kit.karaoke:karaokekit-lyric-ui:1.2.0")
    api("com.netease.yunxin.kit.karaoke:karaokekit-audioeffect:1.2.0")
    api("com.netease.yunxin.kit.karaoke:karaokekit-audioeffect-ui:1.2.0")

    implementation("com.gyf.immersionbar:immersionbar:3.0.0")
    implementation("com.netease.yunxin.kit.auth:auth-yunxin-login:1.0.1")
    implementation("com.blankj:utilcodex:1.30.6")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.scwang.smart:refresh-layout-kernel:2.0.1")

}

