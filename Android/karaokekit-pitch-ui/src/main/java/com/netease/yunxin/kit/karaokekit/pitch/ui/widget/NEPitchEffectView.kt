/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.view.animation.LinearInterpolator
import com.netease.yunxin.kit.karaokekit.pitch.ui.R
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.DimensionUtils
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ILifeCycleComponent
import java.util.Random

internal class NEPitchEffectView : View, ILifeCycleComponent {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    private var mBitmapPool = intArrayOf(R.drawable.pitch_ui_bubble_1, R.drawable.pitch_ui_bubble_2)
    private val mAnimItems = ArrayList<MidiEffectItem>()
    private val mExpireAnimItems = ArrayList<MidiEffectItem>()
    private val mItemPool = ArrayList<Pair<Int, MidiEffectItem>>() // 泡泡复用

    private lateinit var mValueAnimator: ValueAnimator
    private var mRandom: Random = Random()

    @Volatile
    private var mPause: Boolean = false

    @Volatile
    private var mStop: Boolean = false

    @Volatile
    private var mPosition: Int = 0
    private var mIsRunning = false
    private var mAnimValue = 0f
    private var mIndex = 0

    init {
        initAnim()
    }

    private fun initAnim() {
        mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        mValueAnimator.duration = 1000
        mValueAnimator.interpolator = LinearInterpolator()
        mValueAnimator.addUpdateListener {
            mAnimValue = it.animatedValue as Float
            if (mAnimItems.size > 0) {
                invalidate()
            }
            if (!mPause && !mStop && mPosition > 0) {
                val rand = mRandom.nextInt(100)
                if (rand > 20) {
                    return@addUpdateListener
                }
                showNewItem()
            }
        }
        mValueAnimator.repeatCount = ValueAnimator.INFINITE
    }

    fun start() {
        mStop = false
        if (!mValueAnimator.isRunning) {
            realStartAnim()
        }
    }

    fun stop() {
        mStop = true
        if (mValueAnimator.isRunning) {
            realStopAnim()
        }
    }

    fun setPosition(pos: Int) {
        mPosition = pos
    }

    fun destroy() {
        if (!mIsRunning) {
            return
        }
        mValueAnimator.removeAllUpdateListeners()
        realStopAnim()
        mAnimItems.clear()
        mItemPool.clear()
        mPause = true
        mIsRunning = false
    }

    fun resume() {
        mPause = false
        if (!mValueAnimator.isRunning) {
            realStartAnim()
        }
    }

    fun pause() {
        mPause = true
    }

    fun isPaused(): Boolean {
        return mPause
    }

    fun configBitmapArray(bitmapArray: IntArray) {
        mBitmapPool = bitmapArray
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (item in mAnimItems) {
            if (item.updateAnim()) {
                mExpireAnimItems.add(item)
                continue
            }
            item.draw(canvas)
        }
        filterExpiredItems()
    }

    private fun showNewItem() {
        val anim = getNewItem()
        mAnimItems.add(anim)
        invalidate()
    }

    private fun getNewItem(): MidiEffectItem {
        val index = getRandomIndex()
        val size = mItemPool.size
        if (size > 0) {
            for (i in 0 until size) {
                val item = mItemPool[i]
                if (item.first == index) {
                    val item = mItemPool.removeAt(i).second
                    item.initParams()
                    item.setPosition(mPosition)
                    return item
                }
            }
        }
        val item = MidiEffectItem(context, width, index)
        item.setPosition(mPosition)
        return item
    }

    private fun realStartAnim() {
        if (!mValueAnimator.isRunning) {
            mIsRunning = true
            mValueAnimator.start()
        }
    }

    private fun realStopAnim() {
        if (mValueAnimator.isRunning) {
            mValueAnimator.cancel()
        }
    }

    private fun getRandomIndex(): Int {
        return mRandom.nextInt(mBitmapPool.size)
    }

    private fun filterExpiredItems() {
        if (mExpireAnimItems.size == 0) {
            return
        }
        for (item in mExpireAnimItems) {
            mAnimItems.remove(item)
            mItemPool.add(Pair(item.index, item.reset()))
        }
        mExpireAnimItems.clear()
    }

    inner class MidiEffectItem(
        private val mContext: Context,
        private val mViewWidth: Int,
        val index: Int
    ) {
        private var mRandom: Random = Random()
        private lateinit var mBitmap: Bitmap
        private val mPaint: Paint by lazy {
            Paint()
        }
        private val mMatrix by lazy {
            Matrix()
        }
        private var mBmpWidth: Int = 0
        private var mBmpHeight: Int = 0
        private var mPos = PointF()
        private var mScale: Float = 1f
        private var mAnimOffset = 0f // 动画时间偏移量，因为开始的时候动画时间可能不是刚好从0开始，所以要加一个偏移量，从0开始移动
        private var mInnerIndex = 0

        private val startP: PointF = PointF()
        private val endP: PointF = PointF()

        init {
            init()
        }

        private fun init() {
            mInnerIndex = mIndex++
            @Suppress("DEPRECATION")
            mBitmap = (mContext.resources.getDrawable(mBitmapPool[index]) as BitmapDrawable).bitmap
            mBmpWidth = mBitmap.width
            mBmpHeight = mBitmap.height
            initParams()
        }

        fun initParams() {
            mAnimOffset = 0f
            mPaint.alpha = 255
            mScale = mRandom.nextFloat() / 1.5f
            startP.x = (mViewWidth - DimensionUtils.dpToPx(10f) - mBmpWidth * mScale)
            startP.y = mPosition.toFloat()
            endP.x = (-DimensionUtils.dpToPx(20f)).toFloat()
            endP.y =
                (mPosition - DimensionUtils.dpToPx(35 + mRandom.nextInt(50).toFloat())).toFloat()
            mMatrix.reset()
            mMatrix.setScale(mScale, mScale)
            mMatrix.postTranslate(mPos.x, mPos.y)
        }

        fun setPosition(p: Int) {
            mPosition = p
            mAnimOffset = 1f - mAnimValue
        }

        fun reset(): MidiEffectItem {
            initParams()
            return this
        }

        fun updateAnim(): Boolean {
            // alpha
            val alpha = mPaint.alpha.minus(mRandom.nextInt(10))
            alpha.let { mPaint.alpha = if (it > 0) it else 0 }

            // scale
            mScale += 0.01f
            if (mScale > 1) {
                mScale = 1f
            }
            mMatrix.setScale(mScale, mScale)

            // translate
            val currentValue = mAnimOffset + mAnimValue
            mPos = bezierEvaluate(
                if (currentValue >= 1.0f) (currentValue - 1.0f) else currentValue,
                startP,
                endP
            )
            mMatrix.postTranslate(mPos.x, mPos.y)

            return mPos.y <= endP.y + 1 || mPos.x <= endP.x + 1
        }

        /**
         * 根据贝塞尔曲线计算气泡位置
         * t: 0f ~ 1f
         */
        private fun bezierEvaluate(t: Float, startP: PointF, endP: PointF): PointF {
            val oneMinusT = 1.0f - t
            val point = PointF()
            val controlP = PointF()
            controlP.set((startP.x + endP.x) / 3, startP.y - 30)
            point.x =
                oneMinusT * oneMinusT * startP.x + 2 * t * oneMinusT * controlP.x + t * t * endP.x
            point.y =
                oneMinusT * oneMinusT * startP.y + 2 * t * oneMinusT * controlP.y + t * t * endP.y
            return point
        }

        fun draw(canvas: Canvas) {
            canvas.drawBitmap(mBitmap, mMatrix, mPaint)
        }
    }

    override fun onStart() {
        super.onStart()
        start()
    }

    override fun onResume() {
        super.onResume()
        resume()
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onStop() {
        super.onStop()
        stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
    }
}
