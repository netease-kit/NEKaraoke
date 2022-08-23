/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.karaoke.user

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.karaoke.R
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.common.image.ImageLoader
import com.netease.yunxin.kit.karaokekit.ui.activity.BaseActivity
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig
import com.netease.yunxin.kit.login.AuthorManager
import com.netease.yunxin.kit.login.model.EventType
import com.netease.yunxin.kit.login.model.LoginCallback
import com.netease.yunxin.kit.login.model.LoginEvent
import com.netease.yunxin.kit.login.model.LoginObserver
import com.netease.yunxin.kit.login.model.UserInfo

class UserInfoActivity : BaseActivity() {
    companion object

    val tag = "UserInfoActivity"
    private val loginObserver: LoginObserver<LoginEvent> = object : LoginObserver<LoginEvent> {
        override fun onEvent(t: LoginEvent) {
            if (t.eventType == EventType.TYPE_UPDATE) {
                currentUserInfo = t.userInfo
                initUser()
            }
        }
    }
    private var currentUserInfo: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthorManager.registerLoginObserver(loginObserver)
        currentUserInfo = AuthorManager.getUserInfo()
        setContentView(R.layout.activity_user_info)
        initViews()
        paddingStatusBarHeight(findViewById(R.id.cl_root))
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthorManager.unregisterLoginObserver(loginObserver)
    }

    private fun initViews() {
        val logout = findViewById<View>(R.id.tv_logout)
        logout.setOnClickListener {
            AuthorManager.logoutWitDialog(
                this,
                object : LoginCallback<Void> {
                    override fun onSuccess(data: Void?) {
                        AuthorManager.logout(object : LoginCallback<Void> {
                            override fun onSuccess(data: Void?) {
                                ALog.d(tag, "logout success")
                            }

                            override fun onError(errorCode: Int, errorMsg: String) {
                                ALog.e(tag, "logout failed code = $errorCode, message = $errorMsg")
                            }
                        })
                        finish()
                    }

                    override fun onError(errorCode: Int, errorMsg: String) {
                        ToastUtils.showShort(getString(R.string.app_logout_error_msg))
                    }
                }
            )
        }
        val close = findViewById<View>(R.id.iv_close)
        close.setOnClickListener { finish() }
        initUser()
    }

    private fun initUser() {
        val ivUserPortrait = findViewById<ImageView>(R.id.iv_user_portrait)
        ImageLoader.with(applicationContext).circleLoad(currentUserInfo!!.avatar, ivUserPortrait)
        val tvNickname = findViewById<TextView>(R.id.tv_nick_name)
        tvNickname.text = currentUserInfo!!.nickname
    }

    override fun provideStatusBarConfig(): StatusBarConfig? {
        return StatusBarConfig.Builder()
            .statusBarDarkFont(false)
            .build()
    }
}
