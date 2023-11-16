// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.dialog;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeSeatRequestItem;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.list.AudienceApplySeatListAdapter;
import com.netease.yunxin.kit.karaokekit.ui.model.ApplySeatModel;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;

public class AudienceArrangeMicroDialog extends BaseBottomDialog {

  private TextView tvTitle;
  private RecyclerView rcyAnchor;
  private AudienceApplySeatListAdapter adapter;

  @Override
  protected int getResourceLayout() {
    return R.layout.apply_seat_dialog_layout;
  }

  @Override
  protected void initView(View rootView) {
    tvTitle = rootView.findViewById(R.id.title);
    rcyAnchor = rootView.findViewById(R.id.rcv_anchor);
    super.initView(rootView);
  }

  @Override
  protected void initData() {
    rcyAnchor.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new AudienceApplySeatListAdapter(getContext());
    adapter.setOnItemClickListener(
        seatItem -> {
          NEKaraokeKit.getInstance()
              .cancelRequestSeat(
                  new NEKaraokeCallback<Unit>() {

                    @Override
                    public void onSuccess(@Nullable Unit unit) {}

                    @Override
                    public void onFailure(int code, @Nullable String msg) {
                      if (code == NEKaraokeUIConstants.ERROR_NETWORK && getContext() != null) {
                        ToastUtils.INSTANCE.showShortToast(
                            getContext(), getContext().getString(R.string.karaoke_network_error));
                      }
                    }
                  });
          AudienceArrangeMicroDialog.this.dismiss();
        });
    rcyAnchor.setAdapter(adapter);
    getAnchor();
    super.initData();
  }

  private void getAnchor() {
    NEKaraokeKit.getInstance()
        .getSeatRequestList(
            new NEKaraokeCallback<List<NEKaraokeSeatRequestItem>>() {

              @Override
              public void onFailure(int code, @Nullable String msg) {}

              @Override
              public void onSuccess(
                  @Nullable List<NEKaraokeSeatRequestItem> neKaraokeSeatRequestItems) {
                List<ApplySeatModel> applySeatList = new ArrayList<>();
                if (neKaraokeSeatRequestItems != null) {
                  for (NEKaraokeSeatRequestItem item : neKaraokeSeatRequestItems) {
                    if (item != null) {
                      applySeatList.add(
                          new ApplySeatModel(
                              item.getUser(), item.getUserName(), item.getIcon(), 0));
                    }
                  }
                }
                tvTitle.setText(getString(R.string.karaoke_apply_on_seat, applySeatList.size()));
                adapter.setDataList(applySeatList);
              }
            });
  }

  @Override
  protected void initParams() {
    Window window = null;
    if (getDialog() != null) {
      window = getDialog().getWindow();
    }
    if (window != null) {
      window.setBackgroundDrawableResource(R.drawable.white_corner_bottom_dialog_bg);
      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.BOTTOM;
      // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = ScreenUtils.getDisplayHeight() / 2;
      window.setAttributes(params);
    }
    setCancelable(true); // 设置点击外部是否消失
  }
}
