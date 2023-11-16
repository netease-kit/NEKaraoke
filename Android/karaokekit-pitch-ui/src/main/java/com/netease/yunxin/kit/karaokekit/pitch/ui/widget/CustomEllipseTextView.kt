/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget

import android.content.Context
import android.util.AttributeSet

internal class CustomEllipseTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (lineCount == 0) {
            return
        }
        if (layout == null) {
            return
        }
        val ellipsisCount = layout.getEllipsisCount(lineCount - 1)
        if (ellipsisCount == 0) {
            return
        }
        var content = text.toString()
        val lastChar = content.substring(content.length - 1, content.length)
        val measuredWidth = measuredWidth
        val lineCount = lineCount
        val maxMW = measuredWidth * lineCount
        while (paint.measureText("$content...$lastChar") > maxMW) {
            content = content.substring(0, content.length - 1)
            if (content.isEmpty()) {
                break
            }
        }
        text = "$content...$lastChar"
    }
}
