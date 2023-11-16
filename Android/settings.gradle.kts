
/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        jcenter()
    }
}

include(":app")
include(":karaokekit")
include(":karaokekit-audioeffect")
include(":karaokekit-audioeffect-ui")
include(":karaokekit-lyric-ui")
include(":karaokekit-pitch-ui")
include(":karaokekit-ui")
include(":entertainment-common")
