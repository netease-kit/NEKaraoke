// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class KaraokeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final ArrayList<RoomModel> roomInfoList = new ArrayList<>();

  private OnItemClickListener onItemClickListener = null;

  public KaraokeListAdapter() {}

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public static class LiveItemHolder extends RecyclerView.ViewHolder {

    public ImageView ivRoomPic;

    public TextView tvAnchorName;

    public TextView tvRoomName;

    public TextView tvAudienceNum;

    public TextView tvOnSeatNum;

    public LiveItemHolder(@NonNull View itemView) {
      super(itemView);
      ivRoomPic = itemView.findViewById(R.id.iv_room_pic);
      tvAnchorName = itemView.findViewById(R.id.tv_anchor_name);
      tvRoomName = itemView.findViewById(R.id.tv_room_name);
      tvAudienceNum = itemView.findViewById(R.id.tv_audience_num);
      tvOnSeatNum = itemView.findViewById(R.id.tv_onseat_num);
    }
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View roomView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.karaoke_live_item_layout, parent, false);
    return new LiveItemHolder(roomView);
  }

  @Override
  public void onBindViewHolder(
      @NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    if (holder instanceof LiveItemHolder) {
      RoomModel liveInfo = roomInfoList.get(position);
      ((LiveItemHolder) holder).tvRoomName.setText(liveInfo.getRoomName());
      ((LiveItemHolder) holder).tvAnchorName.setText(liveInfo.getAnchorNick());
      ((LiveItemHolder) holder)
          .tvAudienceNum.setText(StringUtils.getAudienceCount(liveInfo.getAudienceCount() + 1));
      ((LiveItemHolder) holder)
          .tvOnSeatNum.setText(StringUtils.getAudienceCount(liveInfo.getOnSeatCount()));
      switch (position % 5) {
        case 0:
          ((LiveItemHolder) holder).ivRoomPic.setBackgroundResource(R.drawable.karaoke_room_bg_0);
          break;
        case 1:
          ((LiveItemHolder) holder).ivRoomPic.setBackgroundResource(R.drawable.karaoke_room_bg_1);
          break;
        case 2:
          ((LiveItemHolder) holder).ivRoomPic.setBackgroundResource(R.drawable.karaoke_room_bg_2);
          break;
        case 3:
          ((LiveItemHolder) holder).ivRoomPic.setBackgroundResource(R.drawable.karaoke_room_bg_3);
          break;
        case 4:
          ((LiveItemHolder) holder).ivRoomPic.setBackgroundResource(R.drawable.karaoke_room_bg_4);
          break;
      }

      holder.itemView.setOnClickListener(
          view -> {
            if (onItemClickListener != null) {
              onItemClickListener.onItemClick(liveInfo);
            }
          });
    }
  }

  /** 判断是否是空布局 */
  public boolean isEmptyPosition(int position) {
    return position == 0 && roomInfoList.isEmpty();
  }

  /** 更新数据 */
  public void setDataList(List<RoomModel> liveInfoList, Boolean isRefresh) {
    if (isRefresh) {
      roomInfoList.clear();
    }
    if (liveInfoList != null && !liveInfoList.isEmpty()) {
      roomInfoList.addAll(liveInfoList);
    }
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return roomInfoList.size();
  }

  public interface OnItemClickListener {
    void onItemClick(RoomModel roomModel);
  }
}
