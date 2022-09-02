// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class LiveBaseAdapter<T>
    extends RecyclerView.Adapter<LiveBaseAdapter.LiveViewHolder> {
  protected Context context;
  protected List<T> dataSource = new ArrayList<>();

  public LiveBaseAdapter(Context context) {
    this.context = context;
  }

  public LiveBaseAdapter(Context context, List<T> dataSource) {
    this.context = context;
    this.dataSource = dataSource;
  }

  public List<T> getDataSource() {
    return dataSource;
  }

  @NonNull
  @Override
  public LiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return onCreateViewHolder(
        LayoutInflater.from(context).inflate(getLayoutId(viewType), parent, false));
  }

  protected abstract int getLayoutId(int viewType);

  protected abstract LiveViewHolder onCreateViewHolder(View itemView);

  @Override
  public void onBindViewHolder(@NonNull LiveViewHolder holder, int position) {
    T itemData = getItem(position);
    onBindViewHolder(holder, itemData, position);
  }

  protected void onBindViewHolder(LiveViewHolder holder, T itemData, int position) {
    onBindViewHolder(holder, itemData);
  }

  protected void onBindViewHolder(LiveViewHolder holder, T itemData) {}

  public void updateDataSource(List<T> newDataSource) {
    dataSource.clear();
    if (newDataSource != null && !newDataSource.isEmpty()) {
      dataSource.addAll(newDataSource);
    }
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return dataSource.size();
  }

  protected T getItem(int position) {
    if (position < 0 || position >= getItemCount()) {
      return null;
    } else {
      return dataSource.get(position);
    }
  }

  public static class LiveViewHolder extends RecyclerView.ViewHolder {

    private final SparseArray<View> viewCache = new SparseArray<>();

    public LiveViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    public <K extends View> K getView(int viewId) {
      View result = viewCache.get(viewId);
      if (result == null) {
        result = itemView.findViewById(viewId);
        viewCache.put(viewId, result);
      }
      return (K) result;
    }
  }
}
