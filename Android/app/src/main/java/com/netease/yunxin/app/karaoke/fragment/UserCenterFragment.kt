/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.karaoke.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.netease.yunxin.app.karaoke.BuildConfig
import com.netease.yunxin.app.karaoke.Constants
import com.netease.yunxin.app.karaoke.R
import com.netease.yunxin.app.karaoke.utils.NavUtils
import com.netease.yunxin.kit.alog.ALog
import com.netease.yunxin.kit.common.image.ImageLoader
import com.netease.yunxin.kit.karaokekit.ui.fragment.BaseFragment
import com.netease.yunxin.kit.login.AuthorManager
import com.netease.yunxin.kit.login.model.EventType
import com.netease.yunxin.kit.login.model.LoginCallback
import com.netease.yunxin.kit.login.model.LoginEvent
import com.netease.yunxin.kit.login.model.LoginObserver
import com.netease.yunxin.kit.login.model.UserInfo

class UserCenterFragment : BaseFragment() {
    companion object {
        private const val TAG = "UserCenterFragment"
    }

    private val loginObserver: LoginObserver<LoginEvent> = object : LoginObserver<LoginEvent> {
        override fun onEvent(t: LoginEvent) {
            if (t.eventType == EventType.TYPE_UPDATE) {
                currentUserInfo = t.userInfo
                initUser(rootView)
            }
        }
    }
    private var currentUserInfo: UserInfo?
    private lateinit var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthorManager.registerLoginObserver(loginObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthorManager.unregisterLoginObserver(loginObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_user_center, container, false)
        initViews(rootView)
        return rootView
    }

    private fun initViews(rootView: View) {
        initUser(rootView)
        val aboutApp = rootView.findViewById<View>(R.id.tv_app_about)
        aboutApp.setOnClickListener {
            NavUtils.toBrowsePage(
                requireActivity(),
                getString(R.string.app_about),
                Constants.URL_YUNXIN
            )
        }
        val userAgreement = rootView.findViewById<View>(R.id.tv_user_agreement)
        userAgreement.setOnClickListener {
            NavUtils.toBrowsePage(
                requireActivity(),
                getString(R.string.app_user_agreement),
                Constants.URL_USER_POLICE
            )
        }

        val privacyPolicy = rootView.findViewById<View>(R.id.tv_privacy_policy)
        privacyPolicy.setOnClickListener {
            NavUtils.toBrowsePage(
                requireActivity(),
                getString(R.string.app_privacy_policy),
                Constants.URL_PRIVACY
            )
        }

        val tvLogout = rootView.findViewById<View>(R.id.tv_logout)
        tvLogout.setOnClickListener {
            activity?.let {
                AuthorManager.logoutWitDialog(
                    it,
                    object : LoginCallback<Void> {
                        override fun onSuccess(data: Void?) {
                            AuthorManager.logout(object : LoginCallback<Void> {
                                override fun onSuccess(data: Void?) {
                                    ALog.d(TAG, "logout success")
                                }

                                override fun onError(errorCode: Int, errorMsg: String) {
                                    ALog.e(TAG, "logout failed code = $errorCode, message = $errorMsg")
                                }
                            })
                        }

                        override fun onError(errorCode: Int, errorMsg: String) {
                            ToastUtils.showShort(getString(R.string.app_logout_error_msg))
                        }
                    }
                )
            }
        }
        val tvVersion = rootView.findViewById<TextView>(R.id.tv_app_version)
        tvVersion.text = BuildConfig.VERSION_NAME
    }

    private fun initUser(rootView: View?) {
        if (currentUserInfo == null) {
            if (activity != null) {
                requireActivity().finish()
            }
            return
        }
        val ivUserPortrait = rootView!!.findViewById<ImageView>(R.id.iv_user_portrait)
        ImageLoader.with(context).circleLoad(currentUserInfo!!.avatar, ivUserPortrait)
        val tvUserName = rootView.findViewById<TextView>(R.id.tv_user_name)
        tvUserName.text = currentUserInfo!!.nickname
    }

    init {
        currentUserInfo = AuthorManager.getUserInfo()
    }
}
