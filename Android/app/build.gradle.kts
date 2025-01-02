
/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.netease.yunxin.app.karaoke"
        minSdk = 21
        targetSdk = 33
        versionCode = 5
        versionName = "1.4.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21") 
    implementation("com.google.android.material:material:1.5.0") 
    implementation("com.netease.yunxin.kit:alog:1.1.0")
    implementation(project(":karaokekit-ui"))
}

configurations.all {
    resolutionStrategy {
        force("com.netease.yunxin:nertc-base:5.6.40")
    }
}
