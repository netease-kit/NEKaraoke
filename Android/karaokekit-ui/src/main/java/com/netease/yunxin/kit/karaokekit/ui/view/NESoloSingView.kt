/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.karaokekit.lyric.model.NELyric
import com.netease.yunxin.kit.karaokekit.lyric.model.NELyric.NELyricType
import com.netease.yunxin.kit.karaokekit.pitch.api.model.NEOpusLevel
import com.netease.yunxin.kit.karaokekit.pitch.api.model.NEPitchAudioData
import com.netease.yunxin.kit.karaokekit.pitch.api.model.NEPitchRecordSingInfo
import com.netease.yunxin.kit.karaokekit.pitch.ui.model.NEKTVPlayResultModel
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ImageLoader
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.LyricUtil
import com.netease.yunxin.kit.karaokekit.ui.databinding.KaraokeSoloSingViewBinding
import java.io.IOException

/**
 * 独唱模块UI，包含音准打分、跳过前奏、歌词、最终得分
 */
class NESoloSingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    companion object {
        private const val TAG = "NESoloSingView"
    }

    private var lyric: NELyric? = null
    private var skipPreludeClick: OnSkipPreludeClickListener? = null
    private var soloSingViewBinding: KaraokeSoloSingViewBinding =
        KaraokeSoloSingViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        soloSingViewBinding.skipPreludeView.apply {
            setSkipPreludeClickListener(object :
                    NESkipPreludeView.OnSkipPreludeClickListener {
                    override fun onSkipPrelude(seekTime: Int) {
                        skipPreludeClick?.onSkipPrelude(seekTime)
                        visibility = INVISIBLE
                        ALog.d(TAG, "onSkipPrelude,seekTime:$seekTime")
                    }

                    override fun onPreludeEnd() {
                        visibility = INVISIBLE
                        ALog.d(TAG, "onPreludeEnd")
                    }
                })
        }
    }

    fun showLyricAndScore(
        lyricContent: String?,
        midiContent: String?,
        lyricType: NELyricType
    ) {
        try {
            var separator = ","
            if (lyricType == NELyricType.NELyricTypeQrc) {
                separator = " "
            }
            lyric = NELyric.initWithContent(lyricContent, lyricType)
            soloSingViewBinding.skipPreludeView.apply {
                lyric?.let {
                    setupLyric(lyric!!)
                }
            }
            soloSingViewBinding.gradeView.loadRecordDataWithPitchContent(
                midiContent,
                separator,
                0,
                LyricUtil.getEndTimeMillis(lyric),
                lyricContent,
                lyricType
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun update(timestamp: Long) {
        soloSingViewBinding.gradeView.update(timestamp)
    }

    fun seek(startTime: Long) {
        ALog.d(TAG, "seek,startTime:$startTime")
        soloSingViewBinding.gradeView.seekTime(startTime)
    }

    fun setOnSkipPreludeClickListener(skipPreludeClick: OnSkipPreludeClickListener) {
        this.skipPreludeClick = skipPreludeClick
    }

    fun showFinalScore(
        userData: NEKTVPlayResultModel,
        finalScore: NEPitchRecordSingInfo,
        callback: ImageLoader.WebpAnimationPlayEndCallback
    ) {
        ALog.d(TAG, "userData:$userData,finalScore:$finalScore")
        var level = finalScore.finalMark?.totalValue?.toInt() ?: 0
        if (finalScore.availableLyricCount == 0) {
            level = 0
        } else {
            level /= finalScore.availableLyricCount
        }
        val opusLevel: NEOpusLevel = when {
            level > 90 -> {
                NEOpusLevel.NEOpusLevelSSS
            }
            level > 80 -> {
                NEOpusLevel.NEOpusLevelSS
            }
            else -> {
                NEOpusLevel.NEOpusLevelS
            }
        }
        soloSingViewBinding.gradeView.showScoreViewWithUserData(
            userData,
            opusLevel,
            callback
        )
        pause()
    }

    fun pause() {
        ALog.d(TAG, "pause")
        soloSingViewBinding.gradeView.pitchPause()
    }

    fun hideScoreView() {
        ALog.d(TAG, "hideScoreView")
        soloSingViewBinding.gradeView.hideScoreView()
    }

    fun pushAudioData(audioData: NEPitchAudioData) {
//        ALog.d(TAG, "pushAudioData,audioData:$audioData")
        soloSingViewBinding.gradeView.pushAudioData(audioData)
    }

    fun start() {
        ALog.d(TAG, "start")
        soloSingViewBinding.gradeView.pitchStart()
    }

    fun resetMidi() {
        ALog.d(TAG, "resetMidi")
        soloSingViewBinding.gradeView.resetMidi()
    }

    fun needShowFinalScore(): Boolean {
        return soloSingViewBinding.gradeView.needShowFinalScore()
    }

    interface OnSkipPreludeClickListener {
        fun onSkipPrelude(seekTime: Int)
    }
}
