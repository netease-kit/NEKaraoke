// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.listener;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.network.NENetworkErrorCode;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.ui.R;

public abstract class NEKaraokeCallbackWrapper<T> implements NEKaraokeCallback<T> {

  @Override
  public abstract void onSuccess(@Nullable T t);

  @Override
  public void onFailure(int code, @Nullable String msg) {
    if (code == NENetworkErrorCode.DEFAULT) {
      ToastX.showShortToast(R.string.karaoke_network_error);
    }
    onError(code, msg);
  }

  public abstract void onError(int code, @Nullable String msg);
}
