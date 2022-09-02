// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Masonry/Masonry.h>
#import <libextobjc/extobjc.h>
#import "NEKaraokeViewController+UI.h"
#import "NEKaraokeViewController+Utils.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"
@implementation NEKaraokeViewController (Subviews)

- (void)setupBgView {
  UIImageView *backgroudView = [[UIImageView alloc] initWithFrame:self.view.frame];
  backgroudView.image = [UIImage karaoke_imageNamed:@"background"];
  backgroudView.contentMode = UIViewContentModeScaleAspectFill;
  [self.view addSubview:backgroudView];
}
- (void)setupSubviews {
  [self.view addSubview:self.headerView];
  [self.headerView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    if (@available(iOS 11.0, *)) {
      make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).offset(4);
    } else {
      make.top.equalTo(self.view).offset(4);
    }
    make.height.mas_equalTo(40);
  }];

  [self.view addSubview:self.lyricActionView];
  [self.lyricActionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(14);
    make.right.mas_equalTo(-14);
    make.top.equalTo(self.headerView.mas_bottom).offset(10);
    make.height.mas_equalTo(197);
  }];

  [self.view addSubview:self.controlView];
  [self.controlView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    make.top.equalTo(self.lyricActionView.mas_bottom).offset(12);
    make.height.mas_equalTo(60);
  }];

  [self.view addSubview:self.seatView];
  [self.seatView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    make.top.equalTo(self.controlView.mas_bottom).offset(26);
    make.height.mas_equalTo(60);
  }];

  [self.view addSubview:self.bottomView];
  [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    if (@available(iOS 11.0, *)) {
      make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
    } else {
      make.bottom.equalTo(self.view);
    }
    make.height.mas_equalTo(60);
  }];

  [self.view addSubview:self.chatView];
  [self.chatView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.view).offset(16);
    make.width.mas_equalTo(280);
    make.bottom.equalTo(self.bottomView.mas_top).offset(-4);
    make.top.equalTo(self.seatView.mas_bottom).offset(4);
  }];

  [self.view addSubview:self.toolBar];

  self.headerView.title = self.detail.liveModel.liveTopic;
  [self.bottomView configSeatWithType:self.role == NEKaraokeViewRoleHost
                                          ? NEKaraokeInputToolBarSeatTypeOption
                                          : NEKaraokeInputToolBarSeatTypeOn];
  // 进房之后，不开始唱歌前不展示 控制视图
  [self showControlView:NO];

  // 麦克风按钮默认隐藏
  [self.bottomView setMicBtnSelected:YES];
  [self.bottomView isShowMicBtn:NO];
}
- (void)showControlView:(BOOL)flag {
  self.controlView.hidden = !flag;
  if (flag) {
    [self.seatView mas_remakeConstraints:^(MASConstraintMaker *make) {
      make.left.right.equalTo(self.view);
      make.top.equalTo(self.controlView.mas_bottom).offset(26);
      make.height.mas_equalTo(60);
    }];
    return;
  }
  [self.seatView mas_remakeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    make.top.equalTo(self.lyricActionView.mas_bottom).offset(12);
    make.height.mas_equalTo(60);
  }];
}
#pragma mark-----------------------------  键盘管理  -----------------------------
- (void)observeKeyboard {
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(keyboardWillShow:)
                                               name:UIKeyboardWillShowNotification
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(keyboardWillHide:)
                                               name:UIKeyboardWillHideNotification
                                             object:nil];
}
#pragma mark - 当键盘事件
- (void)keyboardWillShow:(NSNotification *)aNotification {
  NSDictionary *userInfo = [aNotification userInfo];
  CGRect rect = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
  CGFloat keyboardHeight = rect.size.height;
  [UIView animateWithDuration:[userInfo[UIKeyboardAnimationDurationUserInfoKey] doubleValue]
                   animations:^{
                     self.toolBar.frame =
                         CGRectMake(0, self.view.frame.size.height - keyboardHeight - 50,
                                    self.view.frame.size.width, 50);
                   }];
}
- (void)keyboardWillHide:(NSNotification *)aNotification {
  [UIView animateWithDuration:0.1
                   animations:^{
                     self.toolBar.frame = CGRectMake(0, self.view.frame.size.height + 50,
                                                     self.view.frame.size.width, 50);
                   }];
}
/// 点击屏幕收起键盘
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
  [super touchesBegan:touches withEvent:event];
  [self.toolBar resignFirstResponder];
  [self.view endEditing:true];
}

#pragma mark - UINavigationControllerDelegate
- (void)navigationController:(UINavigationController *)navigationController
      willShowViewController:(UIViewController *)viewController
                    animated:(BOOL)animated {
  // 隐藏导航栏
  [self.navigationController setNavigationBarHidden:[viewController isKindOfClass:[self class]]
                                           animated:YES];
}

- (void)showAlert:(NSString *)title confirm:(NSString *)confirm block:(void (^)(void))block {
  [self showAlert:title
          confirm:confirm
      confirmType:NEKaraokeAlertTitleTypeBlue
       cancelType:NEKaraokeAlertTitleTypeGray
            block:block];
}
- (void)showAlert:(NSString *)title cancel:(NSString *)confirm block:(void (^)(void))block {
  [self showAlert:title
          confirm:confirm
      confirmType:NEKaraokeAlertTitleTypeGray
       cancelType:NEKaraokeAlertTitleTypeBlue
            block:block];
}
- (void)showAlert:(NSString *)title
          confirm:(NSString *)confirm
      confirmType:(NEKaraokeAlertTitleType)confirmType
       cancelType:(NEKaraokeAlertTitleType)cancelType
            block:(void (^)(void))block {
  dispatch_async(dispatch_get_main_queue(), ^{
    UIAlertController *alert =
        [UIAlertController alertControllerWithTitle:title
                                            message:nil
                                     preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消"
                                                           style:UIAlertActionStyleCancel
                                                         handler:nil];
    UIColor *cancelColor = [UIColor karaoke_colorWithHex:0x666666];
    if (cancelType == NEKaraokeAlertTitleTypeBlue) {
      cancelColor = [UIColor karaoke_colorWithHex:0x007AFF];
    }
    [cancelAction setValue:cancelColor forKey:@"titleTextColor"];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:confirm
                                                         style:UIAlertActionStyleDefault
                                                       handler:^(UIAlertAction *_Nonnull action) {
                                                         if (block) block();
                                                       }];
    UIColor *confirmColor = [UIColor karaoke_colorWithHex:0x007AFF];
    if (confirmType == NEKaraokeAlertTitleTypeGray) {
      confirmColor = [UIColor karaoke_colorWithHex:0x666666];
    }
    [sureAction setValue:confirmColor forKey:@"titleTextColor"];

    [alert addAction:cancelAction];
    [alert addAction:sureAction];
    UIViewController *controller = self;
    if (controller.presentedViewController) {
      controller = controller.presentedViewController;
    }
    [self presentViewController:alert
                       animated:true
                     completion:^{
                     }];
  });
}
- (void)sendChatroomNotifyMessage:(NSString *)content {
  NEKaraokeChatViewMessage *message = [[NEKaraokeChatViewMessage alloc] init];
  message.type = NEKaraokeChatViewMessageTypeNotication;
  message.notication = content;
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.chatView addMessages:@[ message ]];
  });
}

- (void)configControlView:(NEKaraokeSongMode)songMode {
  dispatch_async(dispatch_get_main_queue(), ^{
    if (self.role == NEKaraokeViewRoleHost) {  // 房主
      [self showControlView:YES];
      if ([self isAnchorWithSelf]) {  // 主唱

      } else if ([self isChoristerWithSelf]) {  // 副唱
        if (songMode == NEKaraokeSongModeSeaialChorus) {
          //                    [self.controlView enableSwitch:NO];
          [self.controlView enableOrginal:NO];
        }
      } else {  // 听众
        [self.controlView enableVoice:NO];
        [self.controlView enableOrginal:NO];
      }
    } else {                          // 非房主
      if ([self isAnchorWithSelf]) {  // 主唱
        [self showControlView:YES];
      } else if ([self isChoristerWithSelf]) {  // 副唱
        [self showControlView:YES];
        if (songMode == NEKaraokeSongModeSeaialChorus) {
          //                    [self.controlView enablePause:NO];
          //                    [self.controlView enableSwitch:NO];
          [self.controlView enableOrginal:NO];
        }
      }
    }
  });
}

- (void)showAcceptChorusLyricModule {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChorusWait];
    self.lyricActionView.chorusSongName = self.songModel.songName;
    self.lyricActionView.chorusMainUserIcon = [UIImage karaoke_imageWithUrl:self.songModel.icon];
    self.lyricActionView.chorusAttachUserIcon =
        [UIImage karaoke_imageWithUrl:self.songModel.assistantIcon];
    self.lyricActionView.chorusMainUserName = self.songModel.userName;
    self.lyricActionView.chorusAttachUserName = self.songModel.assistantName;
    [self.lyricActionView accompanyLoading:true];
  });
}
- (void)showCancelInviteLyrricModule {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.taskQueue addTask:[NEKaraokeTask defaultSoloWaitTask]];
    [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeWait];
    self.lyricActionView.waitSongName = self.songModel.songName;
    self.lyricActionView.waitUserIcon = [UIImage karaoke_imageWithUrl:self.songModel.icon];
    self.lyricActionView.waitUserName = self.songModel.userName;
    self.lyricActionView.UserIconUrl = self.songModel.icon;
  });
}

- (void)showNoLyricView:(NEKaraokeSongModel *)songModel {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.lyricActionView.noLyricUserIcon = [UIImage karaoke_imageWithUrl:songModel.icon];
    self.lyricActionView.noLyricSongName = songModel.songName;
    [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeNoLyric];
  });
}

- (void)fetchPickSongList {
  @weakify(self)[[NEKaraokeKit shared]
      getOrderedSongsWithCallback:^(NSInteger code, NSString *_Nullable msg,
                                    NSArray<NEKaraokeOrderSongModel *> *_Nullable orderSongs) {
        dispatch_async(dispatch_get_main_queue(), ^{
          @strongify(self) if (!orderSongs.count) {
            [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChooseSong];
          }
          [self.bottomView configPickSongUnreadNumber:orderSongs.count];
        });
      }];
}
@end
