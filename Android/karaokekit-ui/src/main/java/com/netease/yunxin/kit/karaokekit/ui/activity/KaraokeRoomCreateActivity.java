// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.karaokekit.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.utils.ViewUtils;
import com.netease.yunxin.kit.entertainment.common.utils.VoiceRoomUtils;
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeOptions;
import com.netease.yunxin.kit.karaokekit.api.NECreateKaraokeParams;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeCallback;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeKit;
import com.netease.yunxin.kit.karaokekit.api.NEKaraokeSongMode;
import com.netease.yunxin.kit.karaokekit.api.model.NEKaraokeRoomInfo;
import com.netease.yunxin.kit.karaokekit.ui.NEKaraokeUI;
import com.netease.yunxin.kit.karaokekit.ui.R;
import com.netease.yunxin.kit.karaokekit.ui.databinding.ActivityKaraokeRoomCreateBinding;
import com.netease.yunxin.kit.karaokekit.ui.listener.NEKaraokeCallbackWrapper;
import com.netease.yunxin.kit.karaokekit.ui.utils.NavUtils;
import java.util.Random;

public class KaraokeRoomCreateActivity extends BaseActivity {

  private static final String TAG = "KaraokeRoomCreateActivity";

  private static final int COUNT_SEAT = 7;
  private int mode = 0;
  protected boolean isOversea = false;
  protected String cover = "";
  protected int configId;
  protected String username;
  protected String avatar;

  private ActivityKaraokeRoomCreateBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityKaraokeRoomCreateBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    paddingStatusBarHeight(binding.rlRoot);
    isOversea = getIntent().getBooleanExtra(RoomConstants.INTENT_IS_OVERSEA, false);
    configId = getIntent().getIntExtra(RoomConstants.INTENT_KEY_CONFIG_ID, 0);
    username = getIntent().getStringExtra(RoomConstants.INTENT_USER_NAME);
    avatar = getIntent().getStringExtra(RoomConstants.INTENT_AVATAR);
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
      ToastX.showShortToast(R.string.room_name_can_not_empty);
      return;
    }

    getSongMode();
    if (VoiceRoomUtils.isShowFloatView()) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.voiceroom_tip));
      builder.setMessage(getString(R.string.click_create_room_tips));
      builder.setCancelable(true);
      builder.setPositiveButton(
          getString(R.string.voiceroom_sure),
          (dialog, which) -> {
            NEKaraokeUI.getInstance()
                .exitVoiceRoom(
                    new NEKaraokeCallback<Void>() {
                      @Override
                      public void onSuccess(@Nullable Void unit) {
                        ALog.i(TAG, "exitVoiceRoom success");
                        createRoomInner();
                      }

                      @Override
                      public void onFailure(int code, @Nullable String msg) {
                        ALog.e(TAG, "exitVoiceRoom failed code:" + code + ",msg:" + msg);
                      }
                    });
            VoiceRoomUtils.stopFloatPlay();
            dialog.dismiss();
          });
      builder.setNegativeButton(
          getString(R.string.voiceroom_cancel), (dialog, which) -> dialog.dismiss());
      AlertDialog alertDialog = builder.create();
      alertDialog.show();
    } else {
      createRoomInner();
    }
  }

  private void createRoomInner() {
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
                NavUtils.toKaraokeRoomPage(
                    KaraokeRoomCreateActivity.this, username, avatar, roomInfo);

                finish();
              }

              @Override
              public void onError(int code, @Nullable String msg) {
                ALog.i(TAG, "createRoom failed code = " + code + " msg = " + msg);
                if (code == 2001) {
                  NavUtils.toAuthenticateActivity(KaraokeRoomCreateActivity.this);
                } else {
                  ToastX.showShortToast(getString(R.string.ec_join_failed_tips));
                }
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
    return getString(R.string.room_name_prefix) + " " + (new Random().nextInt(900) + 100);
  }

  private String getDefaultUserName() {
    return username;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
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
        ToastX.showShortToast(R.string.room_name_length_tip);
      }
    }
  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected ViewUtils.ModeType getStatusBarTextModeType() {
    return ViewUtils.ModeType.NIGHT;
  }
}
