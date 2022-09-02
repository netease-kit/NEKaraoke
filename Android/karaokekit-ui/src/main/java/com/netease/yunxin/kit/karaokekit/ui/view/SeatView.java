// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSongModel;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.adapter.BaseAdapter;
import com.netease.yunxin.kit.karaokekit.ui.helper.SeatHelper;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.OnSeatModel;
import com.netease.yunxin.kit.karaokekit.ui.model.VoiceRoomSeat;
import com.netease.yunxin.kit.karaokekit.ui.view.seat.SeatAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeatView extends LinearLayout {

  private static final String TAG = "SeatView";

  protected static final int SEAT_MAX_COUNT = 7;

  private RecyclerView recyclerviewSeat;

  private SeatAdapter seatAdapter;

  private List<VoiceRoomSeat> roomSeatList = new ArrayList<>(SEAT_MAX_COUNT);

  protected BaseAdapter.ItemClickListener<VoiceRoomSeat> itemClickListener;

  protected BaseAdapter.ItemLongClickListener<VoiceRoomSeat> itemLongClickListener;

  public SeatView(Context context) {
    this(context, null);
  }

  public SeatView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SeatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, R.layout.layout_seat, this);
    recyclerviewSeat = findViewById(R.id.recyclerview_seat);
    initSeatView(context);
  }

  protected void initSeatView(Context context) {
    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    recyclerviewSeat.setLayoutManager(layoutManager);
    recyclerviewSeat.setClipChildren(false);
    seatAdapter = new SeatAdapter(null, context);
    recyclerviewSeat.setAdapter(seatAdapter);
  }

  public void updateSeats(List<OnSeatModel> remoteSeats) {
    ALog.i(TAG, "updateSeats remoteSeats = " + remoteSeats);
    List<OnSeatModel> onSeatModels = new ArrayList<>();
    if (remoteSeats != null) {
      for (OnSeatModel onSeatModel : remoteSeats) {
        if (onSeatModel.getStatus() == ApplySeatModel.SEAT_STATUS_ON_SEAT) {
          onSeatModels.add(onSeatModel);
        }
      }
    }
    Collections.sort(onSeatModels);
    SeatHelper.getInstance().setOnSeatItems(onSeatModels);
    roomSeatList.clear();
    for (int i = 0; i < SEAT_MAX_COUNT; i++) {
      VoiceRoomSeat voiceRoomSeat;
      if (i < onSeatModels.size()) {
        if (onSeatModels.get(i) != null) {
          voiceRoomSeat = new VoiceRoomSeat(i);
          voiceRoomSeat.setStatus(onSeatModels.get(i).getStatus());
          voiceRoomSeat.setUser(onSeatModels.get(i));
          roomSeatList.add(voiceRoomSeat);
        }
      } else if (i >= onSeatModels.size()) {
        voiceRoomSeat = new VoiceRoomSeat(i);
        roomSeatList.add(voiceRoomSeat);
      }
    }
    seatAdapter.setItems(roomSeatList);
  }

  public void setItemClickListener(BaseAdapter.ItemClickListener<VoiceRoomSeat> itemClickListener) {
    this.itemClickListener = itemClickListener;
    seatAdapter.setItemClickListener(itemClickListener);
  }

  public void setItemLongClickListener(
      BaseAdapter.ItemLongClickListener<VoiceRoomSeat> itemLongClickListener) {
    this.itemLongClickListener = itemLongClickListener;
    seatAdapter.setItemLongClickListener(itemLongClickListener);
  }

  public void updateSongModel(NEKaraokeSongModel songModel) {
    seatAdapter.updateSongModel(songModel);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    SeatHelper.getInstance().setOnSeatItems(null);
  }
}
