// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.lyric.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyric;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricLine;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricType;
import com.netease.yunxin.kit.karaokekit.lyric.ui.R;
import com.netease.yunxin.kit.karaokekit.lyric.ui.util.NELyricViewHelper;
import java.util.Iterator;

/** 歌词控件 */
public class NELyricView extends View {

  public enum LyricMode {
    /** 逐行模式 */
    LineByLine,
    /** 逐字模式 */
    WordByWord
  }

  /** 歌词UI默认属性值 */
  public static class LyricViewDefAttrs {

    public static final int DEFAULT_TEXT_COLOR = Color.GRAY;
    public static final int DEFAULT_HIGHLIGHT_COLOR = Color.RED;
    public static final int DEFAULT_HIGHLIGHT_BG_COLOR = Color.WHITE;
    public static final int DEFAULT_HINT_COLOR = Color.WHITE;
    public static final int DEFAULT_SHADER_COLOR = DEFAULT_TEXT_COLOR;
    public static final int DEFAULT_TEXT_SIZE = 12;
    public static final int DEFAULT_LINE_SPACE = 8;
  }

  private static final String TAG = "NELyricView";
  private int mDefaultColor = LyricViewDefAttrs.DEFAULT_TEXT_COLOR;
  private int mHighLightColor = LyricViewDefAttrs.DEFAULT_HIGHLIGHT_COLOR;
  private int mHighLightBgColor = LyricViewDefAttrs.DEFAULT_HIGHLIGHT_BG_COLOR;
  private int mHintColor = LyricViewDefAttrs.DEFAULT_HINT_COLOR;
  private int mShaderColor = LyricViewDefAttrs.DEFAULT_SHADER_COLOR;

  private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

  private int mLineCount;

  private float mHintTextHeight;
  private float mDefaultTextHeight;
  private float mHighLightTextHeight;
  private float mShaderTextHeight;

  private float mDefaultTextSizePx;
  private float mHighLightTextSizePx;
  private float mShaderTextSizePx;
  private float mHintTextSizePx;

  private float mDefaultTextPy = 0;
  private float mHighLightTextPy = 0;
  private float mShaderTextPy = 0;

  private float mLineSpace = 0;
  private int mCurrentPlayLine = -1;

  //设置长歌词从屏幕1/mScale处开始滚动到结束停止，当前默认为长歌词播放到1/3之一就开始滚动。
  private int mScale = 3;

  private volatile NELyric lyric;
  private String mDefaultHint = "";

  private Paint mHintTextPaint;
  private Paint mDefaultTextPaint;
  private Paint mHighLightTextPaint;
  private Paint mShaderTextPaint;
  private Paint mBottomTextPaint;

  private boolean mSliding = false;

  private static final long MAX_SMOOTH_SCROLL_DURATION = 300;

  private LyricMode mode = LyricMode.LineByLine;

  private long mCurrentTimeMillis;

  private ValueAnimator valueAnimator;

  private int paddingTop;

  public NELyricView(Context context) {
    this(context, null);
  }

  public NELyricView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NELyricView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    float defaultTextSizeForPx =
        getRawSize(TypedValue.COMPLEX_UNIT_SP, LyricViewDefAttrs.DEFAULT_TEXT_SIZE);
    float defaultLineSpace =
        getRawSize(TypedValue.COMPLEX_UNIT_SP, LyricViewDefAttrs.DEFAULT_LINE_SPACE);
    TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NELyricView);
    mDefaultTextSizePx =
        typedArray.getDimension(R.styleable.NELyricView_defaultTextSizeSp, defaultTextSizeForPx);
    mHighLightTextSizePx =
        typedArray.getDimension(R.styleable.NELyricView_highLightTextSizeSp, defaultTextSizeForPx);
    mDefaultColor =
        typedArray.getColor(
            R.styleable.NELyricView_defaultTextColor, LyricViewDefAttrs.DEFAULT_TEXT_COLOR);
    mHighLightColor =
        typedArray.getColor(
            R.styleable.NELyricView_highLightTextColor, LyricViewDefAttrs.DEFAULT_HIGHLIGHT_COLOR);
    mHighLightBgColor =
        typedArray.getColor(
            R.styleable.NELyricView_highLightBgTextColor,
            LyricViewDefAttrs.DEFAULT_HIGHLIGHT_BG_COLOR);
    mLineSpace = typedArray.getDimension(R.styleable.NELyricView_lineSpace, defaultLineSpace);
    typedArray.recycle();
    this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    mDefaultHint = getContext().getString(R.string.karaoke_lyric_loading);
    mShaderTextSizePx = defaultTextSizeForPx;
    mHintTextSizePx = defaultTextSizeForPx;
    initPaints();
    initBounds();
  }

  private void initPaints() {
    mHintTextPaint = new Paint();
    mHintTextPaint.setDither(true);
    mHintTextPaint.setAntiAlias(true);
    mHintTextPaint.setTextAlign(Paint.Align.CENTER);
    mHintTextPaint.setColor(mHintColor);
    mHintTextPaint.setTextSize(mHintTextSizePx);

    mDefaultTextPaint = new Paint();
    mDefaultTextPaint.setDither(true);
    mDefaultTextPaint.setAntiAlias(true);
    mDefaultTextPaint.setTextAlign(Paint.Align.CENTER);
    mDefaultTextPaint.setColor(mDefaultColor);
    mDefaultTextPaint.setTextSize(mDefaultTextSizePx);

    mHighLightTextPaint = new Paint();
    mHighLightTextPaint.setDither(true);
    mHighLightTextPaint.setAntiAlias(true);
    mHighLightTextPaint.setTextAlign(Paint.Align.CENTER);
    mHighLightTextPaint.setColor(mHighLightColor);
    mHighLightTextPaint.setTextSize(mHighLightTextSizePx);

    mShaderTextPaint = new Paint();
    mShaderTextPaint.setDither(true);
    mShaderTextPaint.setAntiAlias(true);
    mShaderTextPaint.setTextAlign(Paint.Align.CENTER);
    mShaderTextPaint.setColor(mShaderColor);
    mShaderTextPaint.setTextSize(mShaderTextSizePx);

    mBottomTextPaint = new Paint();
    mBottomTextPaint.setDither(true);
    mBottomTextPaint.setAntiAlias(true);
    mBottomTextPaint.setTextAlign(Paint.Align.CENTER);
    mBottomTextPaint.setColor(mShaderColor);
    mBottomTextPaint.setTextSize(mShaderTextSizePx);
  }

  private void initBounds() {
    Rect lineBound = new Rect();
    mHintTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
    mHintTextHeight = lineBound.height();
    mDefaultTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
    mDefaultTextHeight = lineBound.height();
    mHighLightTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
    mHighLightTextHeight = lineBound.height();
    mShaderTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
    mShaderTextHeight = lineBound.height();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mCurrentPlayLine < 0) {
      return;
    }
    final int height = getMeasuredHeight();
    final int width = getMeasuredWidth();
    paddingTop = getPaddingTop();
    if (lyric == null || lyric.lineModels == null || lyric.lineModels.isEmpty()) {
      drawHintText(canvas, width, height);
      return;
    }
    if (mCurrentPlayLine - 1 >= 0 || mCurrentPlayLine == mLineCount) {
      drawHighLightLine(canvas, width);
    }
    if (mCurrentPlayLine < mLineCount) {
      if (mCurrentPlayLine == 0) {
        // 首行特殊处理
        mDefaultTextPaint.setTextSize(mHighLightTextSizePx);
      } else {
        mDefaultTextPaint.setTextSize(mDefaultTextSizePx);
      }
      canvas.drawText(
          lyric.lineModels.get(mCurrentPlayLine).text,
          width * 0.5f,
          mHighLightTextHeight + mLineSpace + mDefaultTextHeight - mDefaultTextPy + paddingTop,
          mDefaultTextPaint);
    }
    if (mCurrentPlayLine + 1 < mLineCount) {
      canvas.drawText(
          lyric.lineModels.get(mCurrentPlayLine + 1).text,
          width * 0.5f,
          mHighLightTextHeight
              + mDefaultTextHeight
              + mShaderTextHeight
              - mShaderTextPy
              + mLineSpace * 2
              + paddingTop,
          mShaderTextPaint);
    }

    if (mSliding && mCurrentPlayLine + 2 < mLineCount) {
      canvas.drawText(
          lyric.lineModels.get(mCurrentPlayLine + 2).text,
          width * 0.5f,
          mHighLightTextHeight
              + mDefaultTextHeight
              + mShaderTextHeight * 2
              - mShaderTextPy
              + mLineSpace * 3
              + paddingTop,
          mBottomTextPaint);
    }
  }

  private void drawHighLightLine(Canvas canvas, int width) {
    if (mode == LyricMode.LineByLine) {
      drawHighLightRowLineByLine(
          canvas,
          lyric.lineModels.get(mCurrentPlayLine - 1).text,
          width * 0.5f,
          mHighLightTextHeight - mHighLightTextPy + paddingTop);
    } else {
      float progress =
          NELyricViewHelper.getPercentAtLine(
              mCurrentTimeMillis, lyric.lineModels.get(mCurrentPlayLine - 1));
      drawHighLightRowWordByWord(
          canvas,
          lyric.lineModels.get(mCurrentPlayLine - 1).text,
          progress,
          width,
          width * 0.5f,
          mHighLightTextHeight - mHighLightTextPy + paddingTop);
    }
  }

  private void drawHintText(Canvas canvas, int width, int height) {
    canvas.drawText(mDefaultHint, width * 0.5f, (height - mHintTextHeight) * 0.5f, mHintTextPaint);
  }

  private void drawHighLightRowLineByLine(Canvas canvas, String text, float rowX, float rowY) {
    if (mCurrentPlayLine == 1 && mCurrentTimeMillis < lyric.lineModels.get(0).startTime) {
      //首行颜色特殊处理
      mHighLightTextPaint.setColor(mDefaultColor);
    } else {
      mHighLightTextPaint.setColor(mHighLightColor);
    }
    canvas.drawText(text, rowX, rowY, mHighLightTextPaint);
    mHighLightTextPaint.setColor(mHighLightColor);
  }

  private void drawHighLightRowWordByWord(
      Canvas canvas, String text, float progress, int width, float rowX, float rowY) {
    // 保存临时变量 等会儿需要还原，默认文本画笔字体大小
    float defaultTextSize = mDefaultTextPaint.getTextSize();
    mDefaultTextPaint.setTextSize(mHighLightTextPaint.getTextSize());

    int highLineWidth = (int) mDefaultTextPaint.measureText(text);
    float location = progress * highLineWidth;

    //如果歌词长于屏幕宽度就需要滚动
    if (highLineWidth > rowX * 2) {
      if (location < rowX * 2 / mScale) {
        //歌词当前播放位置未到屏幕1/mScale处不需要滚动
        rowX = (float) (highLineWidth / 2.0);
      } else {
        //歌词当前播放位置超过屏幕1/mScale处开始滚动，滚动到歌词结尾到达屏幕边缘时停止滚动。
        float offsetX = location - (rowX * 2 / mScale);
        float widthGap = highLineWidth - rowX * 2;
        if (offsetX < widthGap) {
          rowX = highLineWidth / 2.0f - offsetX;
        } else {
          rowX = highLineWidth / 2.0f - widthGap;
        }
      }
    }

    if (progress == 0) {
      // 首行特殊处理
      mDefaultTextPaint.setColor(mDefaultColor);
    } else {
      mDefaultTextPaint.setColor(mHighLightBgColor);
    }

    canvas.drawText(text, rowX, rowY, mDefaultTextPaint);

    float leftOffset = rowX - highLineWidth / 2f;

    int highWidth = (int) (progress * highLineWidth);

    if (highWidth > 1 && ((int) (rowY * 2)) > 1) {
      mHighLightTextPaint.setXfermode(mXfermode);
      canvas.drawRect(
          leftOffset,
          0,
          leftOffset + highWidth,
          (int) mHighLightTextHeight * 2,
          mHighLightTextPaint);
      mHighLightTextPaint.setXfermode(null);
    }
    mDefaultTextPaint.setColor(mDefaultColor);
    mDefaultTextPaint.setTextSize(defaultTextSize);
  }

  private void invalidateView() {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      invalidate();
    } else {
      postInvalidate();
    }
  }

  private void smoothScroll(final int toPosition) {
    mSliding = true;
    mHighLightTextPaint.setColor(mHighLightColor);
    mDefaultTextPaint.setColor(mDefaultColor);
    mShaderTextPaint.setColor(mShaderColor);
    mShaderTextPaint.setAlpha(255);
    valueAnimator = ValueAnimator.ofFloat(0, 1);
    long duration = NELyricViewHelper.getScrollDuration(lyric, mCurrentPlayLine, toPosition);
    valueAnimator.setDuration(Math.min(duration, MAX_SMOOTH_SCROLL_DURATION));
    valueAnimator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            mHighLightTextPaint.setTextSize(
                (mDefaultTextSizePx - mHighLightTextSizePx) * animation.getAnimatedFraction()
                    + mHighLightTextSizePx);
            mDefaultTextPaint.setTextSize(
                (mHighLightTextSizePx - mDefaultTextSizePx) * animation.getAnimatedFraction()
                    + mDefaultTextSizePx);
            mShaderTextPaint.setTextSize(
                (mDefaultTextSizePx - mShaderTextSizePx) * animation.getAnimatedFraction()
                    + mShaderTextSizePx);
            mHighLightTextPy =
                (mHighLightTextHeight + mLineSpace) * animation.getAnimatedFraction();
            mDefaultTextPy = (mDefaultTextHeight + mLineSpace) * animation.getAnimatedFraction();
            mShaderTextPy = (mShaderTextHeight + mLineSpace) * animation.getAnimatedFraction();
            invalidateView();
          }
        });
    valueAnimator.addListener(
        new AnimatorListenerAdapter() {

          @Override
          public void onAnimationEnd(Animator animation) {
            if (mCurrentPlayLine != toPosition) {
              mCurrentPlayLine = toPosition;
              mHighLightTextPaint.setTextSize(mHighLightTextSizePx);
              mDefaultTextPaint.setTextSize(mDefaultTextSizePx);
              mShaderTextPaint.setTextSize(mShaderTextSizePx);
              mHighLightTextPy = 0;
              mDefaultTextPy = 0;
              mShaderTextPy = 0;
              mHighLightTextPaint.setColor(mHighLightColor);
              mDefaultTextPaint.setColor(mDefaultColor);
              mShaderTextPaint.setColor(mShaderColor);
              mSliding = false;
              invalidateView();
            }
          }
        });
    valueAnimator.start();
  }

  private void scrollToCurrentTimeMillis(long time) {
    if (lyric == null || lyric.lineModels == null) {
      return;
    }
    int position = 0;
    if (mLineCount > 0) {
      for (int i = 0, size = mLineCount; i < size; i++) {
        NELyricLine lineInfo = lyric.lineModels.get(i);
        if (lineInfo != null && lineInfo.startTime > time) {
          position = i;
          break;
        }
        if (i == mLineCount - 1) {
          position = mLineCount;
        }
      }
    }
    if (position == 0) {
      position++;
    }
    if (mCurrentPlayLine != position && !mSliding) {
      smoothScroll(position);
    }
  }

  private void resetLyricInfo() {
    if (lyric != null) {
      if (lyric.lineModels != null) {
        lyric.lineModels.clear();
        lyric.lineModels = null;
      }
      lyric = null;
    }
  }

  private void resetView() {
    resetLyricInfo();
    invalidateView();
    if (valueAnimator != null) {
      valueAnimator.cancel();
    }
    mLineCount = 0;
    mDefaultTextPy = 0;
    mHighLightTextPy = 0;
    mShaderTextPy = 0;
  }

  private float getRawSize(int unit, float size) {
    Context context = getContext();
    Resources resources;
    if (context == null) {
      resources = Resources.getSystem();
    } else {
      resources = context.getResources();
    }
    return TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
  }

  private LyricMode getModeByLyric(NELyric lyric) {
    if (lyric != null && lyric.type != NELyricType.NELyricTypeLrc) {
      return LyricMode.WordByWord;
    }
    return LyricMode.LineByLine;
  }

  /**
   * 设置歌词
   *
   * @param model 歌词对象
   */
  public void loadWithLyricModel(NELyric model) {
    mCurrentPlayLine = -1;
    this.lyric = model;
    if (lyric != null) {
      mode = getModeByLyric(lyric);
      if (this.lyric.lineModels != null) {
        mLineCount = this.lyric.lineModels.size();
      }
      if (TextUtils.isEmpty(lyric.content)) {
        mDefaultHint = "";
      }
    }
    invalidateView();
  }

  /**
   * 设置歌词
   *
   * @param model 歌词对象
   * @param startTime 开始时间
   * @param endTime 结束时间
   */
  public void loadWithLyricModel(NELyric model, long startTime, long endTime) {
    mCurrentPlayLine = -1;
    this.lyric = model;
    if (lyric != null) {
      mode = getModeByLyric(lyric);
      if (this.lyric.lineModels != null && !this.lyric.lineModels.isEmpty()) {
        Iterator<NELyricLine> iterator = this.lyric.lineModels.iterator();
        while (iterator.hasNext()) {
          NELyricLine info = iterator.next();
          if (info.startTime + info.interval < startTime || info.startTime > endTime) {
            iterator.remove();
          }
        }
        mLineCount = this.lyric.lineModels.size();
      }
      if (TextUtils.isEmpty(lyric.content)) {
        mDefaultHint = "";
      }
    }
    invalidateView();
  }

  /**
   * 更新当前时间戳
   *
   * @param currentTimeMillis 当前时间戳,单位ms
   */
  public void update(long currentTimeMillis) {
    mCurrentTimeMillis = currentTimeMillis;
    scrollToCurrentTimeMillis(currentTimeMillis);
    invalidateView();
  }

  /**
   * 设置歌词展示模式
   *
   * @param lyricMode 逐行or逐字
   */
  public void setLyricMode(LyricMode lyricMode) {
    this.mode = lyricMode;
    invalidateView();
  }

  /**
   * 重置、设置歌词内容被重置后的提示内容
   *
   * @param message 提示内容
   */
  public void reset(String message) {
    mDefaultHint = message;
    resetView();
  }
}
