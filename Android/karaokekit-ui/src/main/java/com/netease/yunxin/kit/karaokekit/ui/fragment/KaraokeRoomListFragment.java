// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeLiveState;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeLiveModel;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomList;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.list.KaraokeListAdapter;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeRoomModel;
import com.netease.yunxin.kit.karaokekit.ui.utils.ClickUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.NavUtils;
import com.netease.yunxin.kit.karaokekit.ui.utils.SpUtils;
import com.netease.yunxin.kit.karaokekit.ui.view.FooterView;
import com.netease.yunxin.kit.karaokekit.ui.view.HeaderView;
import com.netease.yunxin.kit.login.AuthorManager;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

public class KaraokeRoomListFragment extends BaseFragment
    implements OnRefreshListener, OnLoadMoreListener {

  public static final String TAG = "KaraokeRoomListFragment";

  //每页大小
  public static final int PAGE_SIZE = 20;

  private SmartRefreshLayout refreshLayout = null;

  private KaraokeListAdapter karaokeListAdapter = null;

  //页码
  private boolean haveMore = false;

  //下一页请求页码
  private int nextPageNum = 1;

  private final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.live_list_activity_layout, container, false);
    initViews(rootView);
    initData();
    paddingStatusBarHeight(rootView);
    return rootView;
  }

  private void initViews(View rootView) {
    RecyclerView recyclerView = rootView.findViewById(R.id.rcv_live);
    refreshLayout = rootView.findViewById(R.id.refreshLayout);
    refreshLayout.setRefreshHeader(new HeaderView(getActivity()));
    refreshLayout.setRefreshFooter(new FooterView(getActivity()));
    refreshLayout.setOnRefreshListener(this);
    refreshLayout.setOnLoadMoreListener(this);
    karaokeListAdapter = new KaraokeListAdapter(getActivity());
    karaokeListAdapter.setOnItemClickListener(
        (liveList, position) -> {
          if (!NetworkUtils.isConnected(requireContext())) {
            ToastUtils.showShort(R.string.karaoke_net_error);
            return;
          }
          //goto audience page
          if (!ClickUtils.INSTANCE.isFastClick()) {
            NEKaraokeLiveModel info = liveList.get(position).getLiveModel();
            KaraokeRoomModel roomModel = new KaraokeRoomModel();
            roomModel.setRoomName(info.getLiveTopic());
            roomModel.setMode(null);
            roomModel.setRoomUuid(info.getRoomUuid());
            roomModel.setLiveRecordId(info.getLiveRecordId());
            if (AuthorManager.INSTANCE.getUserInfo() != null) {
              roomModel.setNick(AuthorManager.INSTANCE.getUserInfo().getNickname());
            }
            roomModel.setRole(NEKaraokeUIConstants.ROLE_AUDIENCE);
            NavUtils.toKaraokeRoomPage(getActivity(), roomModel);
          }
        });
    recyclerView.addItemDecoration(new MyItemDecoration());
    gridLayoutManager.setSpanSizeLookup(new MySpanSizeLookup());
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(karaokeListAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    getLiveLists(true);
  }

  private void initData() {
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
              public void onSuccess(@Nullable NEKaraokeRoomList liveListResponse) {
                if (liveListResponse == null) {
                  ALog.e(TAG, "requestLiveList onSuccess but liveListResponse == null");
                  return;
                }
                nextPageNum++;
                if (karaokeListAdapter != null) {
                  karaokeListAdapter.setDataList(liveListResponse.getList(), isRefresh);
                }
                haveMore = liveListResponse.getHasNextPage();
                if (isRefresh) {
                  refreshLayout.finishRefresh(true);
                } else {
                  if (liveListResponse.getList() == null
                      || liveListResponse.getList().size() == 0) {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                  } else {
                    refreshLayout.finishLoadMore(true);
                  }
                }
              }

              @Override
              public void onFailure(int code, String msg) {
                ALog.d("==========code:" + code);
                if (code == -1) {
                  ToastUtils.showShort(R.string.karaoke_net_error);
                }
                if (isRefresh) {
                  refreshLayout.finishRefresh(false);
                } else {
                  refreshLayout.finishLoadMore(false);
                }
              }
            });
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

  class MyItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int pixel8 = SpUtils.INSTANCE.dp2pix(getContext(), 8f);
      int pixel4 = SpUtils.INSTANCE.dp2pix(getContext(), 4f);
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
