/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.netease.yunxin.kit.karaokekit.pitch.ui.R
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.DimensionUtils

@Suppress("DEPRECATION")
internal class NEPitchDoubleHitView : View {
    var count = 0
    private var viewWidth = 0
    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }
    private val mAnimator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, 0f, 1f, 1f)
    }
    private val multiIcon by lazy {
        (resources.getDrawable(R.drawable.pitch_ui_sing_score_pic_x) as BitmapDrawable).bitmap
    }
    private val bmpWidth by lazy {
        multiIcon.width
    }
    private var iconResArray = arrayOf(
        R.drawable.pitch_ui_sing_score_pic_0,
        R.drawable.pitch_ui_sing_score_pic_1,
        R.drawable.pitch_ui_sing_score_pic_2,
        R.drawable.pitch_ui_sing_score_pic_3,
        R.drawable.pitch_ui_sing_score_pic_4,
        R.drawable.pitch_ui_sing_score_pic_5,
        R.drawable.pitch_ui_sing_score_pic_6,
        R.drawable.pitch_ui_sing_score_pic_7,
        R.drawable.pitch_ui_sing_score_pic_8,
        R.drawable.pitch_ui_sing_score_pic_9
    )
    private var iconDrawableArray = arrayOfNulls<Bitmap>(10)
    private val offset6 = DimensionUtils.dpToPx(6f)
    private val offset5 = DimensionUtils.dpToPx(5f)
    private val offset4 = DimensionUtils.dpToPx(4f)
    private val offset3 = DimensionUtils.dpToPx(3f)
    private val mBmpRect by lazy {
        Rect()
    }
    private val mBmpRectX by lazy {
        RectF()
    }
    private val mBmpRectUnit by lazy {
        RectF()
    }
    private val mBmpRectDecade by lazy {
        RectF()
    }
    private val scaleWidth: Float by lazy {
        (bmpWidth - offset3).toFloat()
    }

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    init {
        mBmpRect.set(0, 0, bmpWidth, bmpWidth)
        mAnimator.duration = 1900
        mAnimator.addUpdateListener {
            val v: Float = it.animatedValue as Float
            scaleX = v
            scaleY = v
        }
        mAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                visibility = GONE
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationCancel(animation: Animator) {
            }
        })
    }

    fun show(score: Int) {
        if (score in thresholdPerfect..100) {
            count++
            if (count >= 2) {
                doAnim()
            }
        } else {
            reset()
        }
    }

    fun reset() {
        count = 0
    }

    private fun doAnim() {
        val decade = count / 10
        viewWidth = if (decade == 0) bmpWidth * 2 else bmpWidth * 3
        requestLayout()
        mAnimator.cancel()
        mAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(viewWidth, bmpWidth + offset6)
        pivotX = (viewWidth / 2).toFloat()
        pivotY = (bmpWidth / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        mBmpRectX.set(
            offset3.toFloat(),
            offset6.toFloat(),
            offset3.toFloat() + scaleWidth,
            offset6.toFloat() + scaleWidth
        )
        canvas.drawBitmap(multiIcon, mBmpRect, mBmpRectX, mPaint)
        val unit = count % 10
        val decade = count / 10

        // 十位
        if (decade in 1..9) {
            var bmpDecode = iconDrawableArray[decade]
            if (bmpDecode == null) {
                bmpDecode = (resources.getDrawable(iconResArray[decade]) as BitmapDrawable).bitmap
                iconDrawableArray[decade] = bmpDecode
            }
            mBmpRectDecade.set(
                bmpWidth.toFloat(),
                offset3.toFloat(),
                bmpWidth + scaleWidth,
                offset3.toFloat() + scaleWidth
            )
            canvas.drawBitmap(bmpDecode!!, mBmpRect, mBmpRectDecade, mPaint)
        }

        // 个位
        var bmpUnit = iconDrawableArray[unit]
        if (bmpUnit == null) {
            bmpUnit = (resources.getDrawable(iconResArray[unit]) as BitmapDrawable).bitmap
            iconDrawableArray[unit] = bmpUnit
        }
        val left = if (decade == 0) bmpWidth.toFloat() else 2 * bmpWidth.toFloat() - offset6
        val top = if (decade != 0) 0f else offset4.toFloat()
        mBmpRectUnit.set(left, top, left + scaleWidth, top + scaleWidth)
        canvas.drawBitmap(bmpUnit!!, mBmpRect, mBmpRectUnit, mPaint)
    }
}
