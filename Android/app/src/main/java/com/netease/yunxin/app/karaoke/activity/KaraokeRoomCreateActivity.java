// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.yunxin.app.karaoke.R;
import com.netease.yunxin.app.karaoke.databinding.ActivityKaraokeRoomCreateBinding;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeOptions;
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeParams;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUIConstants;
import com.netease.yunxin.kit.karaokekit.ui.activity.BaseActivity;
import com.netease.yunxin.kit.karaokekit.ui.listener.NEKaraokeCallbackWrapper;
import com.netease.yunxin.kit.karaokekit.ui.model.KaraokeRoomModel;
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig;
import com.netease.yunxin.kit.karaokekit.ui.utils.NavUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.UserInfo;
import java.util.Random;

public class KaraokeRoomCreateActivity extends BaseActivity {

  private static final String TAG = "KaraokeRoomCreateActivity";

  private static final int COUNT_SEAT = 7;
  private int mode = 0;

  private ActivityKaraokeRoomCreateBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityKaraokeRoomCreateBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    paddingStatusBarHeight(R.id.rl_root);
    initViews();
  }

  private void initViews() {
    binding.etRoomName.setText(getDefaultRoomName());
    binding.etRoomName.addTextChangedListener(new MaxLengthWatcher(15, binding.etRoomName));
    binding.tvUserName.setText(getDefaultUserName());
    binding.ivBack.setOnClickListener(v -> finish());
    binding.tvCreateRoom.setOnClickListener(v -> createRoom());
  }

  private void createRoom() {

    if (TextUtils.isEmpty(getRoomName())) {
      ToastUtils.showShort(R.string.app_room_name_can_not_empty);
      return;
    }

    getSongMode();
    NECreateKaraokeParams params =
        new NECreateKaraokeParams(
            getRoomName(),
            getUserName(),
            COUNT_SEAT,
            NEKaraokeSongMode.Companion.fromValue(mode),
            null);
    NECreateKaraokeOptions options = new NECreateKaraokeOptions();
    NEKaraokeKit.getInstance()
        .createRoom(
            params,
            options,
            new NEKaraokeCallbackWrapper<NEKaraokeRoomInfo>() {

              @Override
              public void onSuccess(@Nullable NEKaraokeRoomInfo roomInfo) {
                ALog.i(TAG, "createRoom success");
                if (roomInfo == null) {
                  ALog.d(TAG, "createRoom success but roomInfo == null");
                  return;
                }
                KaraokeRoomModel roomModel = new KaraokeRoomModel();
                roomModel.setMode(getSongMode());
                roomModel.setLiveRecordId(roomInfo.getLiveModel().getLiveRecordId());
                roomModel.setRoomUuid(roomInfo.getLiveModel().getRoomUuid());
                roomModel.setRole(NEKaraokeUIConstants.ROLE_HOST);
                roomModel.setRoomName(getRoomName());
                UserInfo userInfo = AuthorManager.INSTANCE.getUserInfo();
                String nick = "";
                if (userInfo != null) {
                  nick = userInfo.getNickname();
                }
                roomModel.setNick(nick);
                NavUtils.toKaraokeRoomPage(KaraokeRoomCreateActivity.this, roomModel);

                finish();
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.i(TAG, "createRoom failed code = " + code + " msg = " + msg);
              }
            });
  }

  private String getRoomName() {
    return binding.etRoomName.getText().toString().trim();
  }

  private String getUserName() {
    return binding.tvUserName.getText().toString().trim();
  }

  private NEKaraokeSongMode getSongMode() {
    int checkedId = binding.radioGroup.getCheckedRadioButtonId();
    if (checkedId == R.id.rb_intelligence_chorus) {
      mode = 0;
      return NEKaraokeSongMode.INTELLIGENCE;
    } else if (checkedId == R.id.rb_serial_chorus) {
      mode = 1;
      return NEKaraokeSongMode.SERIAL_CHORUS;
    } else if (checkedId == R.id.rb_realtime_chorus) {
      mode = 2;
      return NEKaraokeSongMode.REAL_TIME_CHORUS;
    }
    return NEKaraokeSongMode.SOLO;
  }

  private String getDefaultRoomName() {
    return getString(R.string.app_room_name_prefix) + " " + (new Random().nextInt(900) + 100);
  }

  private String getDefaultUserName() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getNickname();
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(0, R.anim.anim_exit);
  }

  public static class MaxLengthWatcher implements TextWatcher {

    private final int maxLen;
    private final EditText editText;

    public MaxLengthWatcher(int maxLen, EditText editText) {
      this.maxLen = maxLen;
      this.editText = editText;
    }

    public void afterTextChanged(Editable arg0) {}

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      Editable editable = editText.getText();
      int len = editable.length();

      if (len > maxLen) {
        int selEndIndex = Selection.getSelectionEnd(editable);
        String str = editable.toString();
        //截取新字符串
        String newStr = str.substring(0, maxLen);
        editText.setText(newStr);
        editable = editText.getText();

        //新字符串的长度
        int newLen = editable.length();
        //旧光标位置超过字符串长度
        if (selEndIndex > newLen) {
          selEndIndex = editable.length();
        }
        //设置新光标所在的位置
        Selection.setSelection(editable, selEndIndex);
        ToastUtils.showShort(R.string.app_room_name_length_tip);
      }
    }
  }
}
