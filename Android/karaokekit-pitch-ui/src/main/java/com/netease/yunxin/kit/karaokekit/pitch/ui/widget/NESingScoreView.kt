/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.pitch.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import com.netease.yunxin.kit.copyrightedmedia.api.model.NEOpusLevel
import com.netease.yunxin.kit.karaokekit.pitch.ui.R
import com.netease.yunxin.kit.karaokekit.pitch.ui.util.ImageLoader

const val COUNT_DOWN_INTERVAL = 1000L
const val SONG_NAME_MAX_LENGTH = 18

fun String.manualEllipsize(maxLength: Int = SONG_NAME_MAX_LENGTH): String {
    return if (this.length > maxLength) {
        "${this.take(maxLength)}…"
    } else {
        this
    }
}

data class SingScoreInfo(
    val songName: String?,
    val avatarUrl: String?,
    val singerName: String?,
    val isChorus: Boolean = false
)

internal class SingScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val motionRoot: MotionLayout
    private val blurBackground: ImageView
    private val songName: CustomEllipseTextView
    private val avatar: ImageView
    private val chorusAvatar: ImageView
    private val singerName: TextView
    private val scoreAnim: ImageView
    private val finishLogo: AppCompatImageView
    private var animSSSResId = R.drawable.pitch_ui_animsss
    private var animSSResId = R.drawable.pitch_ui_animss
    private var animSResId = R.drawable.pitch_ui_anims

    companion object {
        private const val TAG = "SingScoreView"
        private const val INVALID_ANIM_RES_ID = 0
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sing_score_view, this, true)
        motionRoot = view.findViewById(R.id.motionRoot)
        blurBackground = view.findViewById(R.id.blurBackground)
        songName = view.findViewById(R.id.songName)
        avatar = view.findViewById(R.id.avatar)
        chorusAvatar = view.findViewById(R.id.chorusAvatar)
        singerName = view.findViewById(R.id.singerName)
        scoreAnim = view.findViewById(R.id.scoreAnim)
        finishLogo = view.findViewById(R.id.finishLogo)
    }

    fun setSingInfo(info: SingScoreInfo) {
        songName.text = "《" + info.songName?.manualEllipsize() + "》"
        ImageLoader.loadImg(info.avatarUrl, avatar)
        scoreAnim.isVisible = false

        motionRoot.setTransition(R.id.scoreViewSwitcher)
    }

    fun hideScoreAnim() {
        scoreAnim.isVisible = false
        scoreAnim.setImageDrawable(null)
    }

    fun updateLevel(level: NEOpusLevel, callback: ImageLoader.WebpAnimationPlayEndCallback) {
        scoreAnim.isVisible = true
        finishLogo.isVisible = false
        val animResId = when (level) {
            NEOpusLevel.NEOpusLevelSSS -> {
                animSSSResId
            }
            NEOpusLevel.NEOpusLevelSS -> {
                animSSResId
            }
            NEOpusLevel.NEOpusLevelS -> {
                animSResId
            }
            else -> {
                INVALID_ANIM_RES_ID
            }
        }
        if (animResId == INVALID_ANIM_RES_ID) {
            return
        }

        ImageLoader.loadWebpImg(animResId, scoreAnim) {
            scoreAnim.isVisible = false
            callback.onAnimationEnd()
        }
    }
}
