// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class KaraokeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int VIEW_TYPE_ITEM = 1;

  private static final int VIEW_TYPE_EMPTY = 0;

  private Context context;

  private ArrayList<NEKaraokeRoomInfo> liveInfos = new ArrayList<>();

  private OnItemClickListener onItemClickListener = null;

  public KaraokeListAdapter(Context context) {
    this.context = context;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public class LiveItemHolder extends RecyclerView.ViewHolder {

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

  public class EmptyViewHolder extends RecyclerView.ViewHolder {

    public EmptyViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_EMPTY) {
      View emptyView =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.list_empty_layout, parent, false);
      return new EmptyViewHolder(emptyView);
    }
    View roomView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.live_item_layout, parent, false);
    return new LiveItemHolder(roomView);
  }

  @Override
  public void onBindViewHolder(
      @NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
    if (holder instanceof LiveItemHolder) {
      NEKaraokeRoomInfo liveInfo = liveInfos.get(position);
      ((LiveItemHolder) holder).tvRoomName.setText(liveInfo.getLiveModel().getLiveTopic());
      ((LiveItemHolder) holder).tvAnchorName.setText(liveInfo.getAnchor().getNick());
      if (liveInfo.getLiveModel().getAudienceCount() != null) {
        ((LiveItemHolder) holder)
            .tvAudienceNum.setText(
                StringUtils.INSTANCE.getAudienceCount(
                    liveInfo.getLiveModel().getAudienceCount() + 1));
      }
      if (liveInfo.getLiveModel().getOnSeatCount() != null) {
        ((LiveItemHolder) holder)
            .tvOnSeatNum.setText(
                StringUtils.INSTANCE.getAudienceCount(liveInfo.getLiveModel().getOnSeatCount()));
      }
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
          new View.OnClickListener() {

            @Override
            public void onClick(View view) {
              if (onItemClickListener != null) {
                onItemClickListener.onItemClick(liveInfos, position);
              }
            }
          });
    }
  }

  /** 判断是否是空布局 */
  public boolean isEmptyPosition(int position) {
    return position == 0 && liveInfos.isEmpty();
  }

  /**
   * 更新数据
   *
   * @param liveInfoList
   * @param isRefresh
   */
  public void setDataList(List<NEKaraokeRoomInfo> liveInfoList, Boolean isRefresh) {
    if (isRefresh) {
      liveInfos.clear();
    }
    if (liveInfoList != null && liveInfoList.size() != 0) {
      liveInfos.addAll(liveInfoList);
    }
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    if (liveInfos.size() > 0) {
      return liveInfos.size();
    } else {
      return 1;
    }
  }

  public int getItemViewType(int position) {
    //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
    if (liveInfos.size() == 0) {
      return VIEW_TYPE_EMPTY;
    } else {
      //如果有数据，则使用ITEM的布局
      return VIEW_TYPE_ITEM;
    }
  }

  public interface OnItemClickListener {
    void onItemClick(ArrayList<NEKaraokeRoomInfo> liveList, int position);
  }
}
