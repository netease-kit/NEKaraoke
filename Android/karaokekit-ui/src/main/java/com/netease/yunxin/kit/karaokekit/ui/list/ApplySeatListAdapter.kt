/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.karaokekit.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.image.ImageLoader
import com.netease.yunxin.kit.karaokekit.ui.R
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel

open class ApplySeatListAdapter(private val context: Context?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var seatItems: ArrayList<ApplySeatModel> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    // Live ViewHolder
    internal class LiveItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvPosition: TextView = itemView.findViewById(R.id.tv_position)
        var ivAnchor: ImageView = itemView.findViewById(R.id.iv_anchor)
        var tvAnchorName: TextView = itemView.findViewById(R.id.tv_anchor_name)
        var tvAgreeOnSeat: TextView = itemView.findViewById(R.id.tv_agree_on_seat)
        var tvRefuseOnSeat: TextView = itemView.findViewById(R.id.tv_refuse_on_seat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_EMPTY) {
            val emptyView = LayoutInflater.from(parent.context)
                .inflate(R.layout.apply_seat_list_empty_layout, parent, false)
            emptyView.findViewById<View?>(R.id.iv_empty).alpha = 0.2f
            return object : RecyclerView.ViewHolder(emptyView) {}
        }
        val rootView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.apply_seat_list_item_layout, parent, false)
        return LiveItemHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LiveItemHolder) {
            val seatItem = seatItems[position]
            holder.tvAnchorName.text = seatItem.nick
            ImageLoader.with(context?.applicationContext)
                .circleLoad(seatItem.avatar, holder.ivAnchor)
            holder.tvPosition.text = String.format("%02d", (position + 1))
            holder.tvAgreeOnSeat.setOnClickListener {
                onItemClickListener?.onAgreeClick(seatItem)
            }
            holder.tvRefuseOnSeat.setOnClickListener {
                onItemClickListener?.onRefuseClick(seatItem)
            }
        }
    }

    /**
     * 更新数据
     *
     * @param applySeatList
     */
    open fun setDataList(applySeatList: List<ApplySeatModel>) {
        if (applySeatList.isNotEmpty()) {
            seatItems.addAll(applySeatList)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (seatItems.size > 0) {
            seatItems.size
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        return if (seatItems.size == 0) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_ITEM
        }
        // 如果有数据，则使用ITEM的布局
    }

    interface OnItemClickListener {
        fun onAgreeClick(seatItem: ApplySeatModel)
        fun onRefuseClick(seatItem: ApplySeatModel)
    }

    companion object {
        const val VIEW_TYPE_ITEM = 1
        const val VIEW_TYPE_EMPTY = 0
    }

    init {
        seatItems = ArrayList()
    }
}
