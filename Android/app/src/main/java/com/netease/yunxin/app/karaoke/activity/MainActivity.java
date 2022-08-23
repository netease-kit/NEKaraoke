// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.karaoke.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.karaoke.Constants;
import com.netease.yunxin.app.karaoke.R;
import com.netease.yunxin.app.karaoke.databinding.ActivityMainBinding;
import com.netease.yunxin.app.karaoke.fragment.RoomListFragment;
import com.netease.yunxin.app.karaoke.fragment.UserCenterFragment;
import com.netease.yunxin.app.karaoke.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.karaokekit.ui.activity.BaseActivity;
import com.netease.yunxin.kit.karaokekit.ui.statusbar.StatusBarConfig;
import com.netease.yunxin.kit.karaokekit.ui.utils.ClickUtils;
import com.netease.yunxin.kit.login.AuthorManager;

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";
  private static final int TAB_HOME = 0;
  private static final int TAB_MINE = 1;
  private ActivityMainBinding binding;
  private RoomListFragment homeFragment;
  private UserCenterFragment userCenterFragment;
  public int curTabIndex = -1;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    ImmersionBar bar = ImmersionBar.with(this).statusBarDarkFont(true);
    bar.init();
    curTabIndex = -1;
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      NavUtils.toSplash(MainActivity.this);
      finish();
    }
    initViews();
  }

  private void initViews() {
    selectFragment(TAB_HOME);
    binding.tvHome.setOnClickListener(view -> selectFragment(TAB_HOME));
    binding.tvMine.setOnClickListener(view -> selectFragment(TAB_MINE));
    binding.ivNewLive.setOnClickListener(
        view -> {
          if (!ClickUtils.INSTANCE.isFastClick()) {
            startActivity(new Intent(MainActivity.this, KaraokeRoomCreateActivity.class));
            overridePendingTransition(R.anim.anim_enter, 0);
          }
        });
    if (AuthorManager.INSTANCE.getUserInfo() != null) {
      ImageLoader.with(MainActivity.this)
          .circleLoad(AuthorManager.INSTANCE.getUserInfo().getAvatar(), binding.ivUserAvatar);
    }
  }

  private void selectFragment(int tabIndex) {
    if (tabIndex == curTabIndex) {
      ALog.i(TAG, "tabIndex==curTabIndex = " + tabIndex);
      return;
    }
    FragmentManager supportFragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
    Fragment currentFragment = supportFragmentManager.findFragmentByTag(curTabIndex + "");
    if (currentFragment != null) {
      fragmentTransaction.hide(currentFragment);
      ALog.i(TAG, "hide:" + currentFragment);
    }
    curTabIndex = tabIndex;
    Fragment fragment = supportFragmentManager.findFragmentByTag(tabIndex + "");
    if (fragment == null) {
      switch (tabIndex) {
        case TAB_HOME:
          if (homeFragment == null) {
            homeFragment = new RoomListFragment();
          }
          fragment = homeFragment;
          break;
        case TAB_MINE:
          if (userCenterFragment == null) {
            userCenterFragment = new UserCenterFragment();
          }
          fragment = userCenterFragment;
          break;
        default:
          break;
      }
      if (fragment != null) {
        fragmentTransaction.add(R.id.fragment_container, fragment, tabIndex + "");
      }
    } else {
      ALog.i(TAG, "show:" + fragment);
      fragmentTransaction.show(fragment);
    }
    if (fragment instanceof RoomListFragment) {
      handleBottomSelectedState(true, false);
    } else if (fragment instanceof UserCenterFragment) {
      handleBottomSelectedState(false, true);
    }
    fragmentTransaction.commit();
  }

  private void handleBottomSelectedState(boolean homeSelected, boolean mineSelected) {
    binding.tvHome.setSelected(homeSelected);
    binding.tvMine.setSelected(mineSelected);
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    curTabIndex = -1;
    ALog.flush(true);
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
  }

  @Override
  protected void onKickOut() {
    AuthorManager.INSTANCE.launchLogin(MainActivity.this, Constants.MAIN_PAGE_ACTION, false);
  }
}
