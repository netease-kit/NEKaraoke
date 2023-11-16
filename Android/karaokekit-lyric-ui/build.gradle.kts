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
    lint {
        disable += "IconDensities"
    }
}

dependencies {
    api("com.netease.yunxin.kit.copyrightedmedia:copyrightedmedia:1.8.0")
}