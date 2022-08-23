/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig

/**
 * Created by luc on 2020/11/13.
 */
open class BaseFragment : Fragment() {
    protected fun paddingStatusBarHeight(view: View?) {
        StatusBarConfig.paddingStatusBarHeight(activity, view)
    }
}
