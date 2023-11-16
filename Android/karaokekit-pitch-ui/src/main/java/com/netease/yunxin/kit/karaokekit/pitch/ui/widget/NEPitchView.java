// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NELyricModelSingerOption;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchItemModel;
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEPitchModel;
import com.netease.yunxin.kit.copyrightedmedia.api.model.PartMode;
import com.netease.yunxin.kit.karaokekit.pitch.ui.R;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.DimensionUtils;
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.PitchUILog;
import java.util.ArrayList;
import java.util.List;

/** 音高UI控件 */
public class NEPitchView extends View {

  /** 默认属性值 */
  public static class PitchViewDefAttrs {
    private static final int PITCH_BASE_COLOR_ME = 0x33ffffff;
    private static final int PITCH_HIT_COLOR_ME = 0xffff3764;
    private static final int PITCH_HIT_COLOR_ANOTHER = 0xff17dcff;
    private static final int PITCH_HIT_COLOR_TOGETHER = 0xff887cff;
    private static final int MASK_SHADER_ME_START_COLOR = 0x33EC5FFF;
    private static final int MASK_SHADER_ME_END_COLOR = 0x80FF3764;
  }

  private static final String TAG = "NEPitchView";
  public static final int NOTE_SPEC = 108; // 音高特殊值，UI需要特殊显示并且不进行音高评分
  private static final int LEFT_WIDTH = DimensionUtils.dpToPx(98);
  private static final int SEPARATOR_WIDTH = DimensionUtils.dpToPx(0.5f);
  private static final int PITCH_ROUND = DimensionUtils.dpToPx(2f);
  private static final int PITCH_LINE_COUNT = 22; //0-2给指示器显示留位置，3-22给音高线
  private static final int PITCH_ADJUST = 3; //用来调节音高位置
  private static final int PITCH_VALID_MAX = 19;
  private static final int PITCH_VALID_MIN = 0;
  private static final int OCTAVE_INTERVAL = 12;
  private static final int RATIO = 4;

  private int mPitchBaseColorMe = PitchViewDefAttrs.PITCH_BASE_COLOR_ME;
  private int mPitchHitColorMe = PitchViewDefAttrs.PITCH_HIT_COLOR_ME;
  private int mLeftAreaWidth = LEFT_WIDTH;
  private int mMaskShaderMeStartColor = PitchViewDefAttrs.MASK_SHADER_ME_START_COLOR;
  private int mMaskShaderMeEndColor = PitchViewDefAttrs.MASK_SHADER_ME_END_COLOR;

  private static class HitPitchMeta {
    public int level;
    public int index;
    public int color;
    public RectF rectF;

    public HitPitchMeta(int level, int index, RectF rect) {
      this.level = level;
      this.index = index;
      this.rectF = rect;
    }
  }

  private PartMode mPartMode = PartMode.ME;
  private Bitmap mIndicatorBmp;
  private int mBmpWidth, mBmpHeight;

  private int mMeasuredHeight;

  public List<NEPitchItemModel> mDownloadedPitchList; //下载的pitch
  private List<NEPitchItemModel> mOrgPitchList; //原始音高线
  public List<NEPitchItemModel> mHitPitchList;
  private RectFPool mRectFPool = new RectFPool(); //pitch池子，用来绘制命中的pitch线时使用，防止重复创建大量对象
  private ArrayMap<Integer, Boolean> mHitSentences = new ArrayMap<Integer, Boolean>();
  private List<HitPitchMeta> mCombinePitchList = new ArrayList<>(); //需要绘制的点，相邻的点连接起来

  private int mHitCount = 0;
  private int mLineHeight; //音高线粗
  private int mIndicatorDrawPosition;

  private Paint mPaint;
  private Paint mHitPitchPaint;
  private Paint mLeftAreaPaint;
  private Paint mSeparatePaint;
  private LinearGradient mMaskShaderMe;
  private LinearGradient mMaskShaderANOTHER;
  private LinearGradient mMaskShaderTOGETHER;
  private int mMaskAlpha = 0;
  private NEPitchEffectView mPitchEffectView;
  public int pitchLineCount = PITCH_LINE_COUNT;

  private int mCurrentIndex; //当前是第几个pitch线
  private int mOffset;

  private int mIndicatorLevel; //0 - 19

  private long mCurrentTime;

  private int mScore;
  private int mScoreCount;
  private HandlerThread mHandlerThread;
  private Handler mHandler;

  private boolean mIsAccompany = true;
  private int mUserRole = 1;
  public int mPitchShift = 0; //合唱的时候，需要用变调来调整音高线位置

  public NEPitchView(Context context) {
    this(context, null);
  }

  public NEPitchView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public void initParam(int userRole, boolean accompany, NEPitchModel pitchModel) {
    mUserRole = userRole;
    mIsAccompany = accompany;
    mOrgPitchList = pitchModel.pitchList;
    Drawable vectorDrawable = getResources().getDrawable(R.drawable.pitch_ui_sing_indicator);
    mIndicatorBmp =
        Bitmap.createBitmap(
            vectorDrawable.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(mIndicatorBmp);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);

    mBmpWidth = mIndicatorBmp.getWidth();
    mBmpHeight = mIndicatorBmp.getHeight();
    initLine();
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    if (visibility == View.VISIBLE) {
      initLine();
    }
  }

  private void init(Context context, AttributeSet attrs) {

    mHandlerThread = new HandlerThread("PitchHandlerThread");
    mHandlerThread.start();
    mHandler = new Handler(mHandlerThread.getLooper());

    if (attrs != null) {
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NEPitchView);
      mPitchHitColorMe =
          typedArray.getColor(
              R.styleable.NEPitchView_pitchHitColorMe, PitchViewDefAttrs.PITCH_HIT_COLOR_ME);
      mLeftAreaWidth =
          typedArray.getDimensionPixelSize(R.styleable.NEPitchView_leftAreaWidth, LEFT_WIDTH);
      mMaskShaderMeStartColor =
          typedArray.getColor(
              R.styleable.NEPitchView_maskShaderMeStartColor,
              PitchViewDefAttrs.MASK_SHADER_ME_START_COLOR);
      mMaskShaderMeEndColor =
          typedArray.getColor(
              R.styleable.NEPitchView_maskShaderMeEndColor,
              PitchViewDefAttrs.MASK_SHADER_ME_END_COLOR);
      typedArray.recycle();
    }

    mMaskShaderMe =
        new LinearGradient(
            0,
            0,
            mLeftAreaWidth,
            0,
            mMaskShaderMeStartColor,
            mMaskShaderMeEndColor,
            Shader.TileMode.CLAMP);
    mMaskShaderANOTHER =
        new LinearGradient(0, 0, mLeftAreaWidth, 0, 0x335F72FF, 0x8056C6FF, Shader.TileMode.CLAMP);
    mMaskShaderTOGETHER =
        new LinearGradient(0, 0, mLeftAreaWidth, 0, 0x335F72FF, 0x80887CFF, Shader.TileMode.CLAMP);

    mPaint = new Paint();
    mHitPitchPaint = new Paint();
    mHitPitchPaint.setColor(mPitchHitColorMe);

    mLeftAreaPaint = new Paint();
    mLeftAreaPaint.setShader(mMaskShaderMe);

    mSeparatePaint = new Paint();
    mSeparatePaint.setColor(mPitchHitColorMe);

    mHitPitchList = new ArrayList();
  }

  private void initLine() {
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (mOrgPitchList != null && !mOrgPitchList.isEmpty()) {
                  int size = mOrgPitchList.size();
                  for (int i = 0; i < size; i++) {
                    setLine(mOrgPitchList.get(i));
                  }
                }
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
              }
            });
  }

  public void setDownloadPitch(List<NEPitchItemModel> pitchList) {
    if (pitchList == null) {
      return;
    }
    mDownloadedPitchList = pitchList;
    for (int i = 0, len = mDownloadedPitchList.size(); i < len; i++) {
      NEPitchItemModel pitch = mDownloadedPitchList.get(i);
      mDownloadedPitchList.get(i).index = checkIndex(pitch.startTime, pitch.duration).first;
    }
  }

  public void setPitchEffectView(NEPitchEffectView view) {
    mPitchEffectView = view;
  }

  public void setLeftAreaAlpha(@IntRange(from = 0, to = 255) int alpha) {
    mMaskAlpha = alpha;
  }

  public void addNewPitch(int startTime, int duration, float note) {

    post(
        () -> {
          NEPitchItemModel pitch;
          Pair<Integer, Integer> indexPart = checkIndex(startTime, duration);
          int index = indexPart.first;
          if (mPartMode == PartMode.ANOTHER) {
            pitch = getDownloadedPitch(startTime);
            PitchUILog.d(TAG, "addNewPitch another");
            if (pitch != null) {
              PitchUILog.d(
                  TAG,
                  "addNewPitch another, startTime: "
                      + pitch.startTime
                      + ", duration: "
                      + pitch.duration
                      + ", note: "
                      + pitch.origLevel);
            }
          } else {
            pitch = new NEPitchItemModel(startTime, startTime + duration, (int) note, index);
            pitch.part = indexPart.second;
            //            PitchUILog.d(
            //                TAG,
            //                "addNewPitch, startTime: "
            //                    + startTime
            //                    + ", duration: "
            //                    + duration
            //                    + ", note: "
            //                    + note);
          }

          if (pitch != null) {
            //说唱需要对音高进行特殊处理
            if (index >= 0) {
              int originLevel = mOrgPitchList.get(index).origLevel;
              if (originLevel == NOTE_SPEC && note > 0) {
                pitch.origLevel = originLevel;
              }
            }
            adjustHitPitch(pitch, index);
            mHitPitchList.add(pitch);

            if (pitch.index >= 0) {
              update(pitch);
            }
          }
        });
  }

  /**
   * 变调：伴奏和作品的时候有区别
   *
   * @return
   */
  private int getShift() {
    return mPitchShift;
  }

  /**
   * 对pitch进行美化, 如果相差一个半音，则修正成正确的音
   *
   * @param pitch
   * @param index
   */
  private void adjustHitPitch(NEPitchItemModel pitch, int index) {
    if (index >= 0 && index <= mOrgPitchList.size() - 1 && pitch.origLevel > 0) {
      int originLevel = mOrgPitchList.get(index).origLevel;
      if (originLevel % 12 - pitch.origLevel % 12 == (1 - getShift())) {
        pitch.origLevel++;
      } else if (originLevel % 12 - pitch.origLevel % 12 == (-1 - getShift())) {
        pitch.origLevel--;
      }
    }
  }

  /**
   * 获取原作品的pitch文件，可能取到空
   *
   * @param startTime
   * @return
   */
  public NEPitchItemModel getDownloadedPitch(int startTime) {
    if (mDownloadedPitchList == null) {
      return null;
    }
    int len = mDownloadedPitchList.size();
    for (int i = 0; i < len; i++) {
      NEPitchItemModel pitch = mDownloadedPitchList.get(i);
      if (pitch.startTime > startTime) {
        return null;
      }
      if (pitch.endTime < startTime) {
        continue;
      }
      if (pitch.startTime <= startTime && pitch.endTime >= startTime) {
        return mDownloadedPitchList.get(i);
      }
    }
    return null;
  }

  private Pair<Integer, Integer> checkIndex(int curTime, int duration) {
    int userPart = NELyricModelSingerOption.NELyricModelSingerOptionA;
    if (mOrgPitchList == null) {
      return new Pair(-1, userPart);
    }
    int size = mOrgPitchList.size();
    int pos = 0;
    while (pos < size) {
      NEPitchItemModel pitch = mOrgPitchList.get(pos);
      if ((curTime >= pitch.startTime || (curTime + duration >= pitch.startTime))
          && curTime < pitch.nextTime) {
        userPart = pitch.part;
        break;
      }
      pos++;
    }
    if (pos >= size) {
      return new Pair(-1, userPart);
    } else {
      return new Pair(pos, userPart);
    }
  }

  public void clearPitch() {
    if (mHitPitchList != null) {
      mHitPitchList.clear();
    }
    if (mDownloadedPitchList != null) {
      mDownloadedPitchList.clear();
    }
  }

  public void reset() {
    mIndicatorLevel = 0;
    postDelayed(
        () -> {
          if (mHitPitchList != null) {
            mHitPitchList.clear();
          }
          mMaskAlpha = 0;
          if (mPitchEffectView != null) {
            mPitchEffectView.pause();
            mPitchEffectView.setPosition(0);
          }
        },
        200); //结束录制后由于是异步操作，偶尔会有有效音高返回，延迟一段时间清空音高
    if (mHitPitchList != null) {
      mHitPitchList.clear();
    }
    postInvalidate();
  }

  public void setSeekTime(int seekTime) {
    PitchUILog.d(TAG, "setSeekTime:" + seekTime);
    if (mHitPitchList != null && !mHitPitchList.isEmpty()) {
      int size = mHitPitchList.size();
      PitchUILog.d(TAG, "setSeekTime mHitPitchList size:" + size);
      int pos = -1;
      for (int i = 0; i < size; i++) {
        if (mHitPitchList.get(i).startTime > seekTime) {
          pos = i;
          break;
        }
      }
      if (pos == 0) {
        mHitPitchList.clear();
      } else if (pos > 0) {
        mHitPitchList = mHitPitchList.subList(0, pos);
      }
      PitchUILog.d(TAG, "setSeekTime pos:" + pos);
      PitchUILog.d(TAG, "setSeekTime mHitPitchList.size:" + mHitPitchList.size());
      postInvalidate();
    }
  }

  public void updatePitchOffset(int time) {
    mOffset = time / RATIO;
    mCurrentIndex = checkIndex(time, 0).first;
    if (mCurrentTime != time) {
      mCurrentTime = time;
      invalidate();
    }
  }

  private void update(NEPitchItemModel hitPitch) {
    if (hitPitch == null) {
      return;
    }
    //    PitchUILog.d(TAG, "update,mCurrentTime:" + mCurrentTime);
    mOffset = (int) (mCurrentTime / RATIO);
    int newIndex = hitPitch.index;
    mCurrentIndex = newIndex;
    if (hitPitch.noteMin <= 0 && hitPitch.index < mOrgPitchList.size()) {
      NEPitchItemModel origPitch = mOrgPitchList.get(hitPitch.index);
      if (origPitch != null) {
        hitPitch.noteMin = origPitch.noteMin;
        hitPitch.noteMax = origPitch.noteMax;
        int newLevelWithKey = origPitch.origLevel + getShift();

        // 计算原始音高和演唱音高差值，并转化到-6 - +6范围
        int noteInterval =
            (newLevelWithKey % OCTAVE_INTERVAL) - (hitPitch.origLevel % OCTAVE_INTERVAL);
        if (noteInterval < -6) { // 超过了负的半个八度，升一个八度
          noteInterval += OCTAVE_INTERVAL;
        } else if (noteInterval > 6) { // 超过了正的半个八度，降一个八度
          noteInterval -= OCTAVE_INTERVAL;
        }

        int level = origPitch.optLevel - noteInterval;
        int gap = Math.abs(hitPitch.origLevel - newLevelWithKey);
        if (gap <= (OCTAVE_INTERVAL * 2 + 4)) {
          if (level < PITCH_VALID_MIN) {
            level = PITCH_VALID_MIN;
          } else if (level > PITCH_VALID_MAX) {
            level = PITCH_VALID_MAX;
          }
          mIndicatorLevel = level;
        } else {
          mIndicatorLevel =
              hitPitch.origLevel > newLevelWithKey ? PITCH_VALID_MAX : PITCH_VALID_MIN;
        }
        hitPitch.optLevel = mIndicatorLevel;
      }
    }
    setLine(hitPitch);

    invalidate();
  }

  private void setLine(NEPitchItemModel pitch) {
    int left = pitch.startTime / RATIO + mLeftAreaWidth;
    int top = (pitchLineCount - pitch.optLevel - PITCH_ADJUST) * mLineHeight;
    pitch.line = new Rect(left, top, pitch.duration / RATIO + left, mLineHeight + top);
  }

  private boolean checkInViewScope(float left, float right) {
    int width = getWidth();
    return (left > 0 && left < width)
        || (right > 0 && right < width)
        || (left < 0 && right > width); //最后一个情况为超长音高
  }

  private void drawPitchLines(Canvas canvas, int index) {
    if (mOrgPitchList == null) {
      return;
    }
    int size = mOrgPitchList.size();
    int start = index - 10;
    for (int i = (Math.max(start, 0)); i < size; i++) {
      NEPitchItemModel pitch = mOrgPitchList.get(i);
      if (pitch.line != null) {
        RectF rect = mRectFPool.getItem();
        rect.set(pitch.line);
        rect.left = pitch.line.left - mOffset;
        rect.right = pitch.line.right - mOffset;
        if (checkInViewScope((int) rect.left, (int) rect.right)) {
          canvas.drawRoundRect(rect, PITCH_ROUND, PITCH_ROUND, mPaint);
        }
        mRectFPool.recycle(rect);
      }
    }
  }

  private void drawHitLines(Canvas canvas) {
    if (mOrgPitchList == null) {
      return;
    }
    mCombinePitchList.clear();
    int size = mHitPitchList.size();
    if (size == 0) {
      return;
    }
    int start = size - 40;
    for (int i = (Math.max(start, 0)); i < size; i++) {
      if (mHitPitchList.size() <= i) { //reset的时候可能会出现index超过size的情况
        break;
      }
      NEPitchItemModel hitPitch = mHitPitchList.get(i);
      if (hitPitch != null
          && hitPitch.line != null
          && hitPitch.index >= 0
          && hitPitch.index < mOrgPitchList.size()) {
        NEPitchItemModel originPitch = mOrgPitchList.get(hitPitch.index);
        Rect rect = originPitch.line;
        if (rect == null) {
          continue;
        }

        RectF hitRect = mRectFPool.getItem();
        hitRect.set(hitPitch.line);
        hitRect.left = hitPitch.line.left - mOffset;
        hitRect.right = hitPitch.line.right - mOffset;
        boolean isPitchInScope = Rect.intersects(hitPitch.line, rect);
        if (isPitchInScope) {
          int newLevelWithKey = hitPitch.origLevel + getShift();
          int combineLen = mCombinePitchList.size();
          HitPitchMeta hitPitchMeta = null;
          if (combineLen == 0) {
            hitPitchMeta = new HitPitchMeta(newLevelWithKey, hitPitch.index, hitRect);
            mCombinePitchList.add(hitPitchMeta);
          } else { //前后两个点连接起来
            HitPitchMeta lastPitch = mCombinePitchList.get(combineLen - 1);
            if (lastPitch.level == newLevelWithKey && lastPitch.index == hitPitch.index) {
              lastPitch.rectF.right = hitRect.right;
            } else {
              hitPitchMeta = new HitPitchMeta(newLevelWithKey, hitPitch.index, hitRect);
              mCombinePitchList.add(hitPitchMeta);
            }
          }
          if (hitPitchMeta != null) {
            if (mIsAccompany) {
              hitPitchMeta.color = mPitchHitColorMe;
            } else {
              if (originPitch.part == NELyricModelSingerOption.NELyricModelSingerOptionAB) {
                hitPitchMeta.color = PitchViewDefAttrs.PITCH_HIT_COLOR_TOGETHER;
              } else if (originPitch.part != mUserRole) {
                hitPitchMeta.color = PitchViewDefAttrs.PITCH_HIT_COLOR_ANOTHER;
              } else {
                hitPitchMeta.color = mPitchHitColorMe;
              }
            }
          }

          mHitCount++;
          int hitPosition =
              ((View) getParent()).getTop()
                  + rect.top
                  + ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin;
          //                    PitchUILog.d(TAG, "processEffect, mHitCount: " + mHitCount + ",hitPosition:" + hitPosition);
          processEffect(true, hitPosition);
        } else {
          mHitCount = 0;
          processEffect(false, 0);
        }
      }
    }

    for (int i = 0, len = mCombinePitchList.size(); i < len; i++) {
      HitPitchMeta pitchMeta = mCombinePitchList.get(i);
      mHitPitchPaint.setColor(pitchMeta.color);
      canvas.drawRoundRect(pitchMeta.rectF, PITCH_ROUND, PITCH_ROUND, mHitPitchPaint);
      mRectFPool.recycle(pitchMeta.rectF);
    }
  }

  /** 显示唱中效果 */
  private void processEffect(boolean hit, int position) {

    if (hit) {
      int value = mHitCount * 10;
      setLeftAreaAlpha(Math.min(255, value));
      if (mPitchEffectView != null && mPitchEffectView.getVisibility() == View.VISIBLE) {
        mPitchEffectView.setPosition(position);
        mPitchEffectView.resume();
      }
    } else {
      setLeftAreaAlpha(0);
      if (mPitchEffectView != null && !mPitchEffectView.isPaused()) {
        mPitchEffectView.setPosition(0);
        mPitchEffectView.pause();
      }
    }
  }

  public void sentenceEnd(int line) {
    mHitSentences.put(line, false);
    if (mScoreCount > 0) {
      int score = mScore / mScoreCount;
      mHitSentences.put(line, score > 0);
    }
    mScore = 0;
    mScoreCount = 0;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
    if (mHandlerThread != null) {
      mHandlerThread.quit();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mMeasuredHeight = getMeasuredHeight();
    mLineHeight = mMeasuredHeight / pitchLineCount;
    mIndicatorDrawPosition = mMeasuredHeight - mBmpHeight;
    PitchUILog.d(
        TAG,
        "mMeasuredHeight:"
            + mMeasuredHeight
            + ",mLineWidth:"
            + mLineHeight
            + ",mIndicatorDrawPosition:"
            + mIndicatorDrawPosition);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // draw base pitch
    mPaint.setColor(mPitchBaseColorMe);
    drawPitchLines(canvas, mCurrentIndex);

    // draw hit pitch
    drawHitLines(canvas);

    //draw left area
    mLeftAreaPaint.setAlpha(mMaskAlpha);
    canvas.drawRect(0.0f, 0.0f, mLeftAreaWidth, mMeasuredHeight, mLeftAreaPaint);

    //已唱分割线
    mSeparatePaint.setStrokeWidth(SEPARATOR_WIDTH);
    mSeparatePaint.setAlpha(Math.min(mMaskAlpha + 51, 127));
    canvas.drawLine(mLeftAreaWidth, 0, mLeftAreaWidth, mMeasuredHeight, mSeparatePaint);

    //指示器
    if (mIndicatorLevel == PITCH_VALID_MAX) {
      mIndicatorDrawPosition--;
    } else if (mIndicatorLevel == PITCH_VALID_MIN) {
      mIndicatorDrawPosition++;
    } else {
      mIndicatorDrawPosition = (pitchLineCount - PITCH_ADJUST - mIndicatorLevel - 1) * mLineHeight;
    }
    mIndicatorDrawPosition =
        Math.max(0, Math.min(mMeasuredHeight - mBmpHeight, mIndicatorDrawPosition));
    mPaint.setColor(Color.WHITE);
    if (mIndicatorBmp != null) {
      canvas.drawBitmap(mIndicatorBmp, mLeftAreaWidth - mBmpWidth, mIndicatorDrawPosition, mPaint);
    }
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  public void setCustomConfig(CustomConfigBuilder config) {
    mLineHeight = (int) config.lineHeight;
    mPitchBaseColorMe = config.pitchBaseColor;
    mPitchHitColorMe = config.pitchHitColor;
    mMaskShaderMeStartColor = config.maskShaderStartColor;
    mMaskShaderMeEndColor = config.maskShaderEndColor;
    mMaskShaderMe =
        new LinearGradient(
            0,
            0,
            mLeftAreaWidth,
            0,
            mMaskShaderMeStartColor,
            mMaskShaderMeEndColor,
            Shader.TileMode.CLAMP);
    mLeftAreaPaint.setShader(mMaskShaderMe);
  }

  public static class CustomConfigBuilder {
    private float lineHeight;
    private int pitchBaseColor;
    private int pitchHitColor;
    private int maskShaderStartColor;
    private int maskShaderEndColor;

    public CustomConfigBuilder() {}

    private CustomConfigBuilder(
        float lineHeight,
        int pitchBaseColor,
        int pitchHitColor,
        int maskShaderStartColor,
        int maskShaderEndColor) {
      this.lineHeight = lineHeight;
      this.pitchBaseColor = pitchBaseColor;
      this.pitchHitColor = pitchHitColor;
      this.maskShaderStartColor = maskShaderStartColor;
      this.maskShaderEndColor = maskShaderEndColor;
    }

    public CustomConfigBuilder setLineHeight(float lineHeight) {
      this.lineHeight = lineHeight;
      return this;
    }

    public CustomConfigBuilder setPitchBaseColor(int pitchBaseColor) {
      this.pitchBaseColor = pitchBaseColor;
      return this;
    }

    public CustomConfigBuilder setPitchHitColor(int pitchHitColor) {
      this.pitchHitColor = pitchHitColor;
      return this;
    }

    public CustomConfigBuilder setMaskShaderStartColor(int maskShaderStartColor) {
      this.maskShaderStartColor = maskShaderStartColor;
      return this;
    }

    public CustomConfigBuilder setMaskShaderEndColor(int maskShaderEndColor) {
      this.maskShaderEndColor = maskShaderEndColor;
      return this;
    }

    public CustomConfigBuilder build() {
      return new CustomConfigBuilder(
          lineHeight, pitchBaseColor, pitchHitColor, maskShaderStartColor, maskShaderEndColor);
    }
  }

  static class RectFPool {
    private final ArrayList<RectF> pool = new ArrayList<>(0);

    public RectF getItem() {
      if (pool.size() > 0) {
        return pool.remove(0);
      } else {
        RectF item = new RectF();
        return item;
      }
    }

    public void recycle(RectF item) {
      pool.add(item);
    }
  }
}
