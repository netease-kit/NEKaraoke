// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUtils;
import java.util.ArrayList;
import java.util.List;

public class AudienceApplySeatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int VIEW_TYPE_ITEM = 1;
  private static final int VIEW_TYPE_EMPTY = 0;
  private final Context context;
  private final List<ApplySeatModel> seatItems = new ArrayList<>();
  private OnAudienceItemClickListener onItemClickListener;

  public AudienceApplySeatListAdapter(Context context) {
    this.context = context;
  }

  /**
   * 更新数据
   *
   * @param applySeatList 数据
   */
  public void setDataList(List<ApplySeatModel> applySeatList) {
    if (applySeatList != null && !applySeatList.isEmpty()) {
      seatItems.addAll(applySeatList);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_EMPTY) {
      View emptyView =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.apply_seat_list_empty_layout, parent, false);
      emptyView.findViewById(R.id.iv_empty).setAlpha(0.2f);
      return new RecyclerView.ViewHolder(emptyView) {};
    }
    View rootView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.audience_apply_seat_list_item_layout, parent, false);
    return new LiveItemHolder(rootView);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof LiveItemHolder) {
      ApplySeatModel seatItem = seatItems.get(position);
      ((LiveItemHolder) holder).tvAnchorName.setText(seatItem.getNick());
      ImageLoader.with(context.getApplicationContext())
          .circleLoad(seatItem.getAvatar(), ((LiveItemHolder) holder).ivAnchor);
      ((LiveItemHolder) holder).tvPosition.setText(String.format("%02d", (position + 1)));
      if (KaraokeUtils.isMySelf(seatItem.getUuid())) {
        ((LiveItemHolder) holder).tvCancelOnSeat.setVisibility(View.VISIBLE);
        ((LiveItemHolder) holder)
            .tvCancelOnSeat.setOnClickListener(v -> onItemClickListener.onCancelClick(seatItem));
      } else {
        ((LiveItemHolder) holder).tvCancelOnSeat.setVisibility(View.GONE);
      }
    }
  }

  @Override
  public int getItemCount() {
    if (seatItems.size() > 0) {
      return seatItems.size();
    } else {
      return 1;
    }
  }

  @Override
  public int getItemViewType(int position) {
    // 在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
    if (seatItems.size() == 0) {
      return VIEW_TYPE_EMPTY;
    } else {
      return VIEW_TYPE_ITEM;
    }
    // 如果有数据，则使用ITEM的布局
  }

  public void setOnItemClickListener(OnAudienceItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public interface OnAudienceItemClickListener {
    void onCancelClick(ApplySeatModel seatItem);
  }

  static class LiveItemHolder extends RecyclerView.ViewHolder {
    public TextView tvPosition;
    public ImageView ivAnchor;
    public TextView tvAnchorName;
    public TextView tvCancelOnSeat;

    public LiveItemHolder(@NonNull View itemView) {
      super(itemView);
      tvPosition = itemView.findViewById(R.id.tv_position);
      ivAnchor = itemView.findViewById(R.id.iv_anchor);
      tvAnchorName = itemView.findViewById(R.id.tv_anchor_name);
      tvCancelOnSeat = itemView.findViewById(R.id.tv_cancel_on_seat);
    }
  }
}
