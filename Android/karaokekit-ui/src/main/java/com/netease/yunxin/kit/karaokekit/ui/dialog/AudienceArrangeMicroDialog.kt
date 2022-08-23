/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.dialog

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem
import com.netease.yunxin.kit.karaokekit.ui.R
import com.netease.yunxin.kit.karaokekit.ui.list.AudienceApplySeatListAdapter
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel

class AudienceArrangeMicroDialog : BaseBottomDialog() {
    private lateinit var tvTitle: TextView
    private lateinit var rcyAnchor: RecyclerView
    private lateinit var adapter: AudienceApplySeatListAdapter

    override fun getResourceLayout(): Int {
        return R.layout.apply_seat_dialog_layout
    }

    override fun initView(rootView: View) {
        tvTitle = rootView.findViewById(R.id.title)
        rcyAnchor = rootView.findViewById(R.id.rcv_anchor)
        super.initView(rootView)
    }

    override fun initData() {
        rcyAnchor.layoutManager = LinearLayoutManager(context)
        adapter = AudienceApplySeatListAdapter(context)
        adapter.setOnItemClickListener(object : AudienceApplySeatListAdapter.OnAudienceItemClickListener {
            override fun onCancelClick(seatItem: ApplySeatModel) {
                NEKaraokeKit.getInstance().cancelRequestSeat()
                this@AudienceArrangeMicroDialog.dismiss()
            }
        })
        rcyAnchor.adapter = adapter
        getAnchor()
        super.initData()
    }

    private fun getAnchor() {
        NEKaraokeKit.getInstance().getSeatRequestList(object : NEKaraokeCallback<List<NEKaraokeSeatRequestItem>> {
            override fun onSuccess(t: List<NEKaraokeSeatRequestItem>?) {
                val applySeatList = mutableListOf<ApplySeatModel>()
                t?.forEach {
                    applySeatList.add(ApplySeatModel(it.user, it.userName, it.icon, 0))
                }
                tvTitle.text = getString(R.string.karaoke_apply_on_seat, applySeatList.size)
                adapter.setDataList(applySeatList)
            }

            override fun onFailure(code: Int, msg: String?) {
            }
        })
    }

    override fun initParams() {
        val window = dialog?.window
        window?.let {
            it.setBackgroundDrawableResource(R.drawable.white_corner_bottom_dialog_bg)
            val params = it.attributes
            params.gravity = Gravity.BOTTOM
            // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ScreenUtils.getScreenHeight() / 2
            it.attributes = params
        }
        isCancelable = true // 设置点击外部是否消失
    }
}
