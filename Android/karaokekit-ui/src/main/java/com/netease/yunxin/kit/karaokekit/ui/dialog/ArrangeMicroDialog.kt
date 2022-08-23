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
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit
import com.netease.yunxin.kit.karaokekit.ui.R
import com.netease.yunxin.kit.karaokekit.ui.list.ApplySeatListAdapter
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel

open class ArrangeMicroDialog : BaseBottomDialog() {
    private lateinit var tvTitle: TextView
    private lateinit var rcyAnchor: RecyclerView
    private lateinit var adapter: ApplySeatListAdapter
    private var applySeatList: List<ApplySeatModel>? = null

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
        adapter = ApplySeatListAdapter(context)
        adapter.setOnItemClickListener(object : ApplySeatListAdapter.OnItemClickListener {
            override fun onAgreeClick(seatItem: ApplySeatModel) {
                NEKaraokeKit.getInstance().approveRequestSeat(seatItem.uuid)
                this@ArrangeMicroDialog.dismiss()
            }

            override fun onRefuseClick(seatItem: ApplySeatModel) {
                NEKaraokeKit.getInstance().rejectRequestSeat(seatItem.uuid)
                this@ArrangeMicroDialog.dismiss()
            }
        })
        rcyAnchor.adapter = adapter
        tvTitle.text = getString(R.string.karaoke_apply_on_seat, applySeatList?.size ?: 0)
        applySeatList?.let { adapter.setDataList(it) }
        super.initData()
    }

    open fun setDateList(list: List<ApplySeatModel>) {
        applySeatList = list
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
