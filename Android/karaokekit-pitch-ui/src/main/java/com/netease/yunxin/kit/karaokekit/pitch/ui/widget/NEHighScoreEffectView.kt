/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.airbnb.lottie.LottieDrawable
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.LottieLoader
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.PitchUILog

const val thresholdNiceDefault = 70
const val thresholdCoolDefault = 80
const val thresholdPerfectDefault = 90

var thresholdNice = thresholdNiceDefault
var thresholdCool = thresholdCoolDefault
var thresholdPerfect = thresholdPerfectDefault

internal class HighScoreEffectView : androidx.appcompat.widget.AppCompatImageView {
    companion object {
        private const val TAG = "HighScoreEffectView"
    }
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    fun customThresHold(
        nice: Int = thresholdNiceDefault,
        cool: Int = thresholdCoolDefault,
        perfect: Int = thresholdPerfectDefault
    ) {
        thresholdNice = nice
        thresholdCool = cool
        thresholdPerfect = perfect
    }

    fun show(score: Int) {
        if (score < thresholdNice) {
            visibility = View.GONE
            return
        }
        val res: String = when (score) {
            in thresholdPerfect..100 -> "pitch_perfect"
            in thresholdCool until thresholdPerfect -> "pitch_cool"
            in thresholdNice until thresholdCool -> "pitch_nice"
            else -> ""
        }
        PitchUILog.d(TAG, "show,score:$score")
        LottieLoader.load(
            res,
            object : LottieLoader.Callback {
                override fun onSuccess(drawable: LottieDrawable?) {
                    drawable?.let {
                        setImageDrawable(drawable)
                        visibility = View.VISIBLE
                        drawable.start()
                        drawable.addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator) {
                            }

                            override fun onAnimationEnd(p0: Animator) {
                                visibility = View.GONE
                            }

                            override fun onAnimationCancel(p0: Animator) {
                            }

                            override fun onAnimationRepeat(p0: Animator) {
                            }
                        })
                    }
                }

                override fun onFailed() {
                    PitchUILog.d(TAG, "onFailed")
                    visibility = View.GONE
                }
            }
        )
    }
}
