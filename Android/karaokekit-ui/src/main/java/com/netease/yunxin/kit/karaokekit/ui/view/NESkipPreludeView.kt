/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.view

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.view.isVisible
import com.netease.yunxin.kit.karaokekit.lyric.model.NELyric
import com.netease.yunxin.kit.karaokekit.pitch.ui.R
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.LyricUtil
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.PitchUILog
import com.netease.yunxin.kit.karaokekit.pitch.ui.widget.COUNT_DOWN_INTERVAL
import kotlin.math.roundToLong

/**
 * 跳过前奏
 */
class NESkipPreludeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private var countDownTimer: CountDownTimer? = null
    private var lyric: NELyric? = null

    private var seekTime = 0
    private var listener: OnSkipPreludeClickListener? = null

    companion object {
        private const val TAG = "NESkipPreludeView"

        /**
         * 最小前奏时间，小于10s不展示跳过前奏
         */
        const val MIN_PRELUDE_TIME = 10000
        const val PRELUDE_BUFFER_TIME = 5000
    }

    init {
        val dp10 = dip2px(10f)
        val dp5 = dip2px(5f)
        setPadding(dp10, dp5, dp10, dp5)
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
    }

    fun setupLyric(lyric: NELyric) {
        this.lyric = lyric
        val preludeTimeMillis = LyricUtil.getPreludeTimeMillis(lyric)
        if (preludeTimeMillis > 0) {
            if (preludeTimeMillis - MIN_PRELUDE_TIME > 0) {
                seekTime = preludeTimeMillis - PRELUDE_BUFFER_TIME
                isVisible = true
                setBackgroundResource(R.drawable.karaoke_skip_prelude_bg)
                setPreludeTime(preludeTimeMillis)
            } else {
                isVisible = false
                setBackgroundResource(0)
            }
        } else {
            isVisible = false
        }
    }

    /**
     * 设置前奏时间
     * @param preludeTimeMillis 前奏时间 ms
     */
    private fun setPreludeTime(preludeTimeMillis: Int) {
        val preludeSeconds = handleTime(preludeTimeMillis)
        PitchUILog.d(TAG, "setPreludeTime,preludeTimeMillis:$preludeTimeMillis,preludeSeconds:$preludeSeconds")
        text = StringBuilder().append(context.getText(R.string.karaoke_skip_prelude))
            .append("(")
            .append(preludeSeconds)
            .append("s")
            .append(")")
        startCountdown(preludeSeconds)
    }

    private fun handleTime(millis: Int): Long {
        return (millis / 1000f).roundToLong()
    }

    private fun dip2px(dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    private fun startCountdown(preludeSeconds: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(preludeSeconds * 1000L, COUNT_DOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                PitchUILog.d(TAG, "onTick:$millisUntilFinished")
                text = StringBuilder().append(context.getText(R.string.karaoke_skip_prelude))
                    .append("(")
                    .append((millisUntilFinished / COUNT_DOWN_INTERVAL + 1))
                    .append("s")
                    .append(")")
            }

            override fun onFinish() {
                listener?.onPreludeEnd()
                PitchUILog.d(TAG, "onFinish，listener:$listener")
            }
        }
        countDownTimer?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun setSkipPreludeClickListener(listener: OnSkipPreludeClickListener) {
        this.listener = listener
        setOnClickListener {
            listener.onSkipPrelude(seekTime)
        }
    }

    interface OnSkipPreludeClickListener {
        fun onSkipPrelude(seekTime: Int)
        fun onPreludeEnd()
    }
}
