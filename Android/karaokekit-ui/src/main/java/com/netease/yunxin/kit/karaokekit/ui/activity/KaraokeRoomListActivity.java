// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.api.RefreshLayout;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.listener.OnLoadMoreListener;
import com.netease.yunxin.kit.entertainment.common.smartrefresh.listener.OnRefreshListener;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.entertainment.common.utils.VoiceRoomUtils;
import com.netease.yunxin.kit.entertainment.common.widget.FooterView;
import com.netease.yunxin.kit.entertainment.common.widget.HeaderView;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeLiveState;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomList;
import com.netease.yunxin.kit.karaokekit.impl.utils.ScreenUtil;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.adapter.KaraokeListAdapter;
import com.netease.yunxin.kit.karaokekit.ui.databinding.ActivityKaraokeRoomListBinding;
import com.netease.yunxin.kit.karaokekit.ui.utils.KaraokeUIUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.NavUtils;

public class KaraokeRoomListActivity extends BaseActivity
    implements OnRefreshListener, OnLoadMoreListener {
  private static final String TAG = "KaraokeRoomListActivity";
  private static final String TAG_REPORT_PAGE_KARAOKE_ROOM_LIST = "page_ktv_list";
  private ActivityKaraokeRoomListBinding binding;
  //每页大小
  public static final int PAGE_SIZE = 20;
  private KaraokeListAdapter karaokeListAdapter = null;
  //页码
  private boolean haveMore = false;
  //下一页请求页码
  private int nextPageNum = 1;
  protected boolean isOversea = false;
  protected int configId;
  protected String userName;
  protected String avatar;

  private final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityKaraokeRoomListBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    paddingStatusBarHeight(binding.getRoot());
    binding.tvTitle.setText(getString(R.string.karaoke));
    binding.tvStart.setText(getString(R.string.karaoke_create_room));
    ReportUtils.report(
        KaraokeRoomListActivity.this, TAG_REPORT_PAGE_KARAOKE_ROOM_LIST, "ktv_enter");
    initIntent();
    initViews();
    initListeners();
    initData();
  }

  @Override
  protected void onResume() {
    super.onResume();
    getLiveLists(true);
  }

  private void initData() {
    getLiveLists(true);
  }

  private void initIntent() {
    isOversea = getIntent().getBooleanExtra(RoomConstants.INTENT_IS_OVERSEA, false);
    configId = getIntent().getIntExtra(RoomConstants.INTENT_KEY_CONFIG_ID, 0);
    userName = getIntent().getStringExtra(RoomConstants.INTENT_USER_NAME);
    avatar = getIntent().getStringExtra(RoomConstants.INTENT_AVATAR);
  }

  private void initViews() {
    binding.refreshLayout.setRefreshHeader(new HeaderView(this));
    binding.refreshLayout.setRefreshFooter(new FooterView(this));
    binding.refreshLayout.setOnRefreshListener(this);
    binding.refreshLayout.setOnLoadMoreListener(this);
    karaokeListAdapter = new KaraokeListAdapter();
    binding.rvRoomList.addItemDecoration(new MyItemDecoration());
    gridLayoutManager.setSpanSizeLookup(new MySpanSizeLookup());
    binding.rvRoomList.setLayoutManager(gridLayoutManager);
    binding.rvRoomList.setAdapter(karaokeListAdapter);
  }

  protected void initListeners() {
    binding.ivCreateRoom.setOnClickListener(
        v -> {
          ReportUtils.report(
              KaraokeRoomListActivity.this, TAG_REPORT_PAGE_KARAOKE_ROOM_LIST, "ktv_start_live");
          Intent intent = new Intent(this, KaraokeRoomCreateActivity.class);
          intent.putExtra(RoomConstants.INTENT_IS_OVERSEA, isOversea);
          intent.putExtra(RoomConstants.INTENT_KEY_CONFIG_ID, configId);
          intent.putExtra(RoomConstants.INTENT_USER_NAME, userName);
          intent.putExtra(RoomConstants.INTENT_AVATAR, avatar);
          startActivity(intent);
        });
    karaokeListAdapter.setOnItemClickListener(
        info -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (NetworkUtils.isConnected()) {
            handleJoinKaraokeRoom(info);
          } else {
            ToastUtils.INSTANCE.showShortToast(
                KaraokeRoomListActivity.this,
                getString(
                    com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
          }
        });
  }

  private void handleJoinKaraokeRoom(RoomModel roomModel) {

    if (VoiceRoomUtils.isShowFloatView()) {
      AlertDialog.Builder builder = new AlertDialog.Builder(KaraokeRoomListActivity.this);
      builder.setTitle(getString(R.string.voiceroom_tip));
      builder.setMessage(getString(R.string.click_roomlist_tips));
      builder.setCancelable(true);
      builder.setPositiveButton(
          getString(R.string.voiceroom_sure),
          (dialog, which) -> {
            NEKaraokeUI.getInstance()
                .exitVoiceRoom(
                    new NEKaraokeCallback<Void>() {
                      @Override
                      public void onSuccess(@Nullable Void unused) {
                        ALog.i(TAG, "exitVoiceRoom success");
                        NavUtils.toKaraokeRoomPage(
                            KaraokeRoomListActivity.this, userName, avatar, roomModel);
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ALog.e(TAG, "exitVoiceRoom failed code:" + code + ",msg:" + msg);
                      }
                    });

            dialog.dismiss();
          });
      builder.setNegativeButton(
          getString(R.string.voiceroom_cancel), (dialog, which) -> dialog.dismiss());
      AlertDialog alertDialog = builder.create();
      alertDialog.show();
    } else {
      NavUtils.toKaraokeRoomPage(KaraokeRoomListActivity.this, userName, avatar, roomModel);
    }
  }

  @Override
  public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
    if (!haveMore) {
      refreshLayout.finishLoadMoreWithNoMoreData();
    } else {
      getLiveLists(false);
    }
  }

  @Override
  public void onRefresh(@NonNull RefreshLayout refreshLayout) {
    nextPageNum = 1;
    getLiveLists(true);
  }

  private void getLiveLists(boolean isRefresh) {
    if (isRefresh) {
      nextPageNum = 1;
    }
    NEKaraokeKit.getInstance()
        .getKaraokeRoomList(
            NEKaraokeLiveState.Live,
            nextPageNum,
            PAGE_SIZE,
            new NEKaraokeCallback<NEKaraokeRoomList>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeRoomList roomList) {
                if (roomList == null) {
                  ALog.e(TAG, "requestLiveList onSuccess but liveListResponse == null");
                  return;
                }
                nextPageNum++;
                haveMore = roomList.getHasNextPage();
                if (isRefresh) {
                  if (roomList.getList() == null || roomList.getList().isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.rvRoomList.setVisibility(View.GONE);
                  } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.rvRoomList.setVisibility(View.VISIBLE);
                    karaokeListAdapter.setDataList(
                        KaraokeUIUtils.neKaraokeRoomInfos2RoomInfos(roomList.getList()), true);
                  }
                  binding.refreshLayout.finishRefresh(true);
                } else {
                  if (roomList.getList() == null || roomList.getList().isEmpty()) {
                    binding.refreshLayout.finishLoadMoreWithNoMoreData();
                  } else {
                    karaokeListAdapter.setDataList(
                        KaraokeUIUtils.neKaraokeRoomInfos2RoomInfos(roomList.getList()), false);
                    binding.refreshLayout.finishLoadMore(true);
                  }
                }
              }

              @Override
              public void onFailure(int code, String msg) {
                ALog.e("getKaraokeRoomList failed code = " + code + " msg = " + msg);
                if (code == NEKaraokeUIConstants.ERROR_NETWORK) {
                  ToastX.showShortToast(R.string.network_error);
                }
                if (isRefresh) {
                  binding.refreshLayout.finishRefresh(false);
                } else {
                  binding.refreshLayout.finishLoadMore(false);
                }
              }
            });
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  static class MyItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int pixel8 = ScreenUtil.dip2px(8f);
      int pixel4 = ScreenUtil.dip2px(4f);
      int position = parent.getChildAdapterPosition(view);
      int left;
      int right;
      if (position % 2 == 0) {
        left = pixel8;
        right = pixel4;
      } else {
        left = pixel4;
        right = pixel8;
      }
      outRect.set(left, pixel4, right, pixel4);
    }
  }

  class MySpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    @Override
    public int getSpanSize(int position) {
      // 如果是空布局，让它占满一行
      if (karaokeListAdapter.isEmptyPosition(position)) {
        return gridLayoutManager.getSpanCount();
      } else {
        return 1;
      }
    }
  }
}
