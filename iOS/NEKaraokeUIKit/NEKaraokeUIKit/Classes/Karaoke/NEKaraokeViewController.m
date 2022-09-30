// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <BlocksKit/BlocksKit.h>
#import <Masonry/Masonry.h>
#import <NELyricUIKit/NELyricUIKit.h>
#import <libextobjc/extobjc.h>
#import "NEKaraokeAuthorityHelper.h"
#import "NEKaraokePickSongEngine.h"
#import "NEKaraokePickSongView.h"
#import "NEKaraokeReachability.h"
#import "NEKaraokeSongLog.h"
#import "NEKaraokeToast.h"
#import "NEKaraokeViewController+Seat.h"
#import "NEKaraokeViewController+UI.h"
#import "NEKaraokeViewController+Utils.h"
#import "UIImage+Karaoke.h"
@interface NEKaraokeViewController () <UINavigationControllerDelegate,
                                       NEKaraokeHeaderViewDelegate,
                                       NEKaraokeListener,
                                       NEKaraokeControlViewDelegate,
                                       NEKaraokeLyricActionViewDelegate,
                                       NEKaraokeInputToolBarDelegate,
                                       NEKaraokeKeyboardToolbarDelegate,
                                       NEKaraokeSendGiftViewtDelegate,
                                       NEKaraokeListener,
                                       NEKaraokeSeatViewDelegate,
                                       NEKaraokeSeatListVCDelegate,
                                       NEKaraokeCopyrightedMediaListener>
//是否打过分
@property(nonatomic, assign) BOOL hasMarked;
//是否断网
@property(nonatomic, assign) BOOL loseNetwork;
/// 网络监听器
@property(nonatomic, strong) NEKaraokeReachability *reachability;
@end

@implementation NEKaraokeViewController {
  bool _preloadSong;
  bool _mute;
}
- (instancetype)initWithRole:(NEKaraokeViewRole)role detail:(NEKaraokeRoomInfo *)detail {
  if ([super init]) {
    self.role = role;
    self.detail = detail;
    self.hidesBottomBarWhenPushed = true;
    self.audioManager = [[NEAudioEffectManager alloc] init];
    _mute = role != NEKaraokeViewRoleHost;
  }
  return self;
}
- (void)dealloc {
  [NEKaraokeLog infoLog:@"NEKaraokeKit" desc:@"NEKaraokeViewController dealloc"];
  [self.taskQueue stop];
  [[NEKaraokeKit shared] removeKaraokeListener:self];
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}
- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.
  self.navigationController.delegate = self;
  [[NEKaraokeKit shared] addKaraokeListener:self];
  //    [[NEKaraoSongEngine getInstance] addKaraokeSongProtocolObserve:self];
  [NEKaraokeAuthorityHelper checkMicAuthority];

  [self setupBgView];
  [self setupSubviews];
  [self observeKeyboard];
  [self setupTimer];
  [self checkAudioOutputDevice];

  NEJoinKaraokeParams *param = [[NEJoinKaraokeParams alloc] init];
  param.nick = [NEKaraokeUIManager sharedInstance].nickname;
  param.roomUuid = self.detail.liveModel.roomUuid;
  param.role = self.role == NEKaraokeViewRoleHost ? NEKaraokeRoleHost : NEKaraokeRoleAudience;
  param.liveRecordId = self.detail.liveModel.liveRecordId;
  @weakify(self)[[NEKaraokeKit shared]
      joinRoom:param
       options:[[NEJoinKaraokeOptions alloc] init]
      callback:^(NSInteger code, NSString *_Nullable msg, NEKaraokeRoomInfo *_Nullable info) {
        @strongify(self) self.detail = info;
        if (code != 0) {
          dispatch_async(dispatch_get_main_queue(), ^{
            [NEKaraokeToast
                showToast:[NSString stringWithFormat:@"加入房间失败 %zd %@", code, msg]];
          });
          [self close];
          return;
        }
        [self defaultOperation];
        // 获取麦位信息
        [self getSeatInfo];
        dispatch_async(dispatch_get_main_queue(), ^{
          self.headerView.count = NEKaraokeKit.shared.allMemberList.count;
        });
      }];

  // 网络监听
  [self addObserver];
}
- (void)addObserver {
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(networkStatusChange)
                                               name:kNEKaraokeReachabilityChangedNotification
                                             object:nil];
}
- (void)networkStatusChange {
  NEKaraokeNetworkStatus status = [self.reachability currentReachabilityStatus];
  if (status == NotReachable) {
    // 无网toast
    [NEKaraokeToast showToast:@"网络连接断开"];
    self.loseNetwork = true;
  } else {
    if (!self.loseNetwork) {
      return;
    }
    self.loseNetwork = false;
    // 有网查询一波
    [NEKaraokeToast showToast:@"网络重连成功"];
    [self getSeatInfo];
    [self fetchPickSongList];
    @weakify(self)
        // 拉取演唱信息
        [NEKaraokeKit.shared requestPlayingSongInfo:^(NSInteger code, NSString *_Nullable msg,
                                                      NEKaraokeSongModel *_Nullable songModel) {
          if (code != 0) {
            [NEKaraokeToast
                showToast:[NSString stringWithFormat:@"查询歌曲失败 %zd %@", code, msg]];
            return;
          }
          if (!songModel) {
            return;
          }
          if ([songModel.account isEqualToString:NEKaraokeKit.shared.localMember.account] ||
              [songModel.assistantUuid isEqualToString:NEKaraokeKit.shared.localMember.account]) {
            // 自己是主唱或者合唱，直接切歌
            [[NEKaraokeKit shared]
                nextSongWithOrderId:songModel.orderId
                           callback:^(NSInteger c, NSString *_Nullable m, id _Nullable o){

                           }];
          } else {
            if (songModel.oc_songStatus == 0 ||
                songModel.oc_songStatus == 1) {  // 暂停或者演唱中 下载歌词并展示
              [[NEKaraokeKit shared]
                  preloadSongLyric:songModel.songId
                           channel:songModel.oc_channel
                          callback:^(NSString *_Nullable content, NSString *_Nullable lyricType,
                                     NSError *_Nullable error) {
                            dispatch_async(dispatch_get_main_queue(), ^{
                              @strongify(self) if (!content.length) {
                                [self showNoLyricView:songModel];
                              }
                              else {
                                [self.lyricActionView
                                    setLyricContent:content
                                          lyricType:songModel.oc_channel == MIGU ? NELyricTypeKas
                                                                                 : NELyricTypeYrc];
                                self.lyricActionView.lyricSeekBtnHidden = true;
                                self.lyricActionView.lyricDuration = songModel.oc_songTime;
                                [self.lyricActionView
                                    showSubview:NEKaraokeLyricActionSubviewTypeLyric];
                              }
                            });
                          }];
            }
          }
        }];
  }
}
- (BOOL)checkNetwork {
  NEKaraokeNetworkStatus status = [self.reachability currentReachabilityStatus];
  if (status == NotReachable) {
    [NEKaraokeToast showToast:@"网络连接断开，请稍后重试"];
    return false;
  }
  return true;
}
- (void)setupTimer {
  self.taskQueue = [[NEKaraokeTaskQueue alloc] init];
  self.taskQueue.taskCanceledBlock = ^(NEKaraokeTask *_Nonnull task) {

  };
  @weakify(self) self.taskQueue.taskProgressBlock = ^(NEKaraokeTask *_Nonnull task) {
    @strongify(self) if (task.type == NEKaraokeTaskSoloWait) {
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView updateWaitTime:task.currentLeftTime / 1000];
      });
    }
    else if (task.type == NEKaraokeTaskChorusMatch) {
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView updateMatchTime:task.currentLeftTime / 1000];
      });
    }
    else if (task.type == NEKaraokeTaskLoadSource) {
    }
  };
  self.taskQueue.taskCompleteBlock = ^(NEKaraokeTask *_Nonnull task) {
    @strongify(self) if (task.type == NEKaraokeTaskSoloWait) {
      if ([self.localOrderSong.account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          if (self.soloAlert) {
            [self.soloAlert dismissViewControllerAnimated:true completion:nil];
          }
        });
        [NEKaraokeKit.shared
            abandonSongWithOrderId:self.localOrderSong.orderId
                          callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                            if (code == 0) {
                            }
                          }];
      }
    }
    else if (task.type == NEKaraokeTaskChorusMatch) {
      if ([self.localOrderSong.account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
        [[NEKaraokeKit shared] cancelInviteChorus:self.chorusId
                                         callback:^(NSInteger code, NSString *_Nullable msg,
                                                    NEKaraokeSongModel *_Nullable obj) {
                                           if (code == 0) {
                                           }
                                         }];
      }
    }
    else if (task.type == NEKaraokeTaskLoadSource) {
      dispatch_async(dispatch_get_main_queue(), ^{
        [NEKaraokeToast showToast:@"合唱者下载伴奏失败"];
      });
      if ([self.localOrderSong.account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
        // 等待对方下载定时器走完，直接切歌
        [[NEKaraokeKit shared]
            abandonSongWithOrderId:self.localOrderSong.orderId
                          callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj){

                          }];
      }
    }
  };

  [self.taskQueue start];
}

- (void)defaultOperation {
  // 如果是主播、上麦及 打开麦克风
  if (self.role == NEKaraokeViewRoleHost) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.bottomView setMicBtnSelected:NO];
      [self.bottomView isShowMicBtn:YES];
    });
    @weakify(self)[NEKaraokeKit.shared
        requestSeat:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
          @strongify(self) if (code == 0) {
            [self unmuteAudio:false];
          }
          else {
            [self close];
          }
        }];
  } else {
    [self fetchPickSongList];
    @weakify(self)
        // 拉取演唱信息
        [NEKaraokeKit.shared requestPlayingSongInfo:^(NSInteger code, NSString *_Nullable msg,
                                                      NEKaraokeSongModel *_Nullable songModel) {
          if (code != 0) {
            [NEKaraokeToast
                showToast:[NSString stringWithFormat:@"查询歌曲失败 %zd %@", code, msg]];
            return;
          }
          if (!songModel) {
            return;
          }
          if (songModel.oc_songStatus == 0 ||
              songModel.oc_songStatus == 1) {  // 暂停或者演唱中 下载歌词并展示
            [[NEKaraokeKit shared]
                preloadSongLyric:songModel.songId
                         channel:(int)songModel.oc_channel
                        callback:^(NSString *_Nullable content, NSString *_Nullable lyricType,
                                   NSError *_Nullable error) {
                          dispatch_async(dispatch_get_main_queue(), ^{
                            @strongify(self) if (!content.length) {
                              [self showNoLyricView:songModel];
                            }
                            else {
                              [self.lyricActionView
                                  setLyricContent:content
                                        lyricType:songModel.oc_channel == MIGU ? NELyricTypeKas
                                                                               : NELyricTypeYrc];
                              self.lyricActionView.lyricSeekBtnHidden = true;
                              self.lyricActionView.lyricDuration = songModel.oc_songTime;
                              [self.lyricActionView
                                  showSubview:NEKaraokeLyricActionSubviewTypeLyric];
                            }
                          });
                        }];
          }
        }];
  }
}

#pragma mark - private

- (void)showChooseSingViewController {
  if (![self checkNetwork]) {
    return;
  }
  UIViewController *controller = [[UIViewController alloc] init];
  controller.preferredContentSize = CGSizeMake(CGRectGetWidth([UIScreen mainScreen].bounds), 500);
  NEKaraokePickSongView *view = [[NEKaraokePickSongView alloc]
      initWithFrame:CGRectMake(0, 0, CGRectGetWidth([UIScreen mainScreen].bounds), 500)
             detail:self.detail];
  @weakify(self) view.isUserOnSeat = ^bool {
    @strongify(self) return [self isOnSeat];
  };
  controller.view = view;
  NEActionSheetNavigationController *nav =
      [[NEActionSheetNavigationController alloc] initWithRootViewController:controller];
  controller.navigationController.navigationBar.hidden = true;
  nav.dismissOnTouchOutside = YES;
  [self presentViewController:nav animated:YES completion:nil];

  @weakify(nav) @weakify(view) view.applyOnseat = ^{
    @strongify(nav) @strongify(view) UIAlertController *alert =
        [UIAlertController alertControllerWithTitle:@"仅麦上成员可点歌，先申请上麦"
                                            message:nil
                                     preferredStyle:UIAlertControllerStyleAlert];
    @weakify(
        view)[alert addAction:[UIAlertAction actionWithTitle:@"取消"
                                                       style:UIAlertActionStyleCancel
                                                     handler:^(UIAlertAction *_Nonnull action) {
                                                       @strongify(view)[view cancelApply];
                                                     }]];
    [alert
        addAction:[UIAlertAction
                      actionWithTitle:@"申请上麦"
                                style:UIAlertActionStyleDefault
                              handler:^(UIAlertAction *_Nonnull action) {
                                if (![NEKaraokeAuthorityHelper checkMicAuthority]) {  // 麦克风权限
                                  [NEKaraokeToast showToast:@"请先开启麦克风权限"];
                                  return;
                                }
                                //申请上麦
                                @strongify(view) @weakify(view)[NEKaraokeKit.shared
                                    requestSeat:^(NSInteger code, NSString *_Nullable msg,
                                                  id _Nullable obj) {
                                      @strongify(view) if (code == 0) {
                                        [view applyFaile];
                                      }
                                      else {
                                        [view applySuccess];
                                      }
                                    }];
                              }]];
    UIViewController *controller = nav;
    if (controller.presentedViewController) {
      controller = controller.presentedViewController;
    }
    [controller presentViewController:alert
                             animated:true
                           completion:^{
                           }];
  };
}

#pragma mark getter

- (NEKaraokeHeaderView *)headerView {
  if (!_headerView) {
    _headerView = [[NEKaraokeHeaderView alloc] initWithFrame:self.view.frame];
    _headerView.delegate = self;
  }
  return _headerView;
}

- (NEKaraokeLyricActionView *)lyricActionView {
  if (!_lyricActionView) {
    _lyricActionView = [[NEKaraokeLyricActionView alloc] initWithFrame:self.view.frame];
    _lyricActionView.delegate = self;
    [_lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChooseSong];
  }
  return _lyricActionView;
}

- (NEKaraokeControlView *)controlView {
  if (!_controlView) {
    _controlView = [[NEKaraokeControlView alloc] initWithFrame:self.view.frame];
    _controlView.delegate = self;
  }
  return _controlView;
}

- (NEKaraokeSeatView *)seatView {
  if (!_seatView) {
    _seatView = [[NEKaraokeSeatView alloc] initWithFrame:self.view.frame];
    _seatView.delegate = self;
  }
  return _seatView;
}

- (NEKaraokeInputToolBar *)bottomView {
  if (!_bottomView) {
    _bottomView = [[NEKaraokeInputToolBar alloc] initWithFrame:self.view.frame
                                                      showGift:self.role != NEKaraokeViewRoleHost];
    _bottomView.delegate = self;
  }
  return _bottomView;
}

- (NEKaraokeChatView *)chatView {
  if (!_chatView) {
    _chatView = [[NEKaraokeChatView alloc] initWithFrame:self.view.frame];
  }
  return _chatView;
}

- (NEKaraokeKeyboardToolbarView *)toolBar {
  if (!_toolBar) {
    _toolBar = [[NEKaraokeKeyboardToolbarView alloc]
        initWithFrame:CGRectMake(0, self.view.frame.size.height, self.view.frame.size.width, 50)];
    _toolBar.backgroundColor = UIColor.whiteColor;
    _toolBar.cusDelegate = self;
  }
  return _toolBar;
}

#pragma mark - NEKaraokeHeaderViewDelegate
- (void)onClose {
  if (self.role != NEKaraokeViewRoleHost) {
    if ([self isOnSeat]) {  // 在麦上
      __weak typeof(self) weakSelf = self;
      [self showAlert:@"当前正在麦上\n确定退出房间?"
               cancel:@"确定"
                block:^{
                  [weakSelf close];
                }];
    } else {
      [self close];
    }
    return;
  }
  __weak typeof(self) weakSelf = self;
  [self showAlert:@"是否结束直播"
          confirm:@"确定"
            block:^{
              [weakSelf close];
            }];
}

- (void)close {
  if (self.role == NEKaraokeViewRoleHost) {
    @weakify(self)[[NEKaraokeKit shared]
        endRoom:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
          dispatch_async(dispatch_get_main_queue(), ^{
            @strongify(self) if (self.presentedViewController) {
              [self.presentedViewController dismissViewControllerAnimated:false completion:nil];
            }
            [self.navigationController dismissViewControllerAnimated:true completion:nil];
          });
        }];
  } else {
    @weakify(self)[[NEKaraokeKit shared]
        leaveRoom:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
          dispatch_async(dispatch_get_main_queue(), ^{
            @strongify(self) if (self.presentedViewController) {
              [self.presentedViewController dismissViewControllerAnimated:false completion:nil];
            }
            [self.navigationController popToRootViewControllerAnimated:true];
          });
        }];
  }
}

#pragma mark - NEKaraokeControlViewDelegate

- (void)onControlEvent:(NEKaraokeControlEventType)type {
  switch (type) {
    case NEKaraokeControlEventTypeVoice:
      self.audioViewController = [NEAudioEffectUIManager
          showAudioEffectViewController:self
                                manager:self.audioManager
                               effectId:[[NEKaraokeKit shared] currentSongIdForAudioEffect]];
      break;
    case NEKaraokeControlEventTypePause: {  // 暂停播放
      if (![self checkNetwork]) {
        return;
      }
      @weakify(self)[[NEKaraokeKit shared]
          requestPausePlayingSong:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
            dispatch_async(dispatch_get_main_queue(), ^{
              if (code != 0) {
                [NEKaraokeToast showToast:msg];
              } else {
                @strongify(self)[self.controlView selectPause:true];
              }
            });
          }];
    } break;
    case NEKaraokeControlEventTypeResume: {  // 恢复播放
      if (![self checkNetwork]) {
        return;
      }
      @weakify(self)[[NEKaraokeKit shared]
          requestResumePlayingSong:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
            dispatch_async(dispatch_get_main_queue(), ^{
              if (code != 0) {
                [NEKaraokeToast showToast:msg];
              } else {
                @strongify(self)[self.controlView selectPause:false];
              }
            });
          }];
    } break;
    case NEKaraokeControlEventTypeSwitch: {
      if (![self checkNetwork]) {
        return;
      }
      [NEKaraokeKit.shared
          nextSongWithOrderId:self.localOrderSong.orderId
                     callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                       if (code != 0) {
                         [NEKaraokeToast
                             showToast:[NSString stringWithFormat:@"切歌失败: %@", msg]];
                       }
                     }];
    } break;
    case NEKaraokeControlEventTypeOriginal:
      [[NEKaraokeKit shared] switchAccompaniment:false];
      self.audioViewController.currentEffectId =
          [[NEKaraokeKit shared] currentSongIdForAudioEffect];
      break;
    case NEKaraokeControlEventTypeAccompany:
      [[NEKaraokeKit shared] switchAccompaniment:true];
      self.audioViewController.currentEffectId =
          [[NEKaraokeKit shared] currentSongIdForAudioEffect];
      break;
    default:
      break;
  }
}
#pragma mark - NEKaraokeKeyboardToolbarDelegate
- (void)didToolBarSendText:(NSString *)text {
  if (text.length) {
    if (![self checkNetwork]) {
      return;
    }
    @weakify(self)[[NEKaraokeKit shared]
        sendTextMessage:text
               callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                 dispatch_async(dispatch_get_main_queue(), ^{
                   @strongify(self) NEKaraokeChatViewMessage *model =
                       [[NEKaraokeChatViewMessage alloc] init];
                   model.type = NEKaraokeChatViewMessageTypeNormal;
                   model.text = text;
                   model.sender = [NEKaraokeUIManager sharedInstance].nickname;
                   model.isAnchor = self.role == NEKaraokeViewRoleHost;
                   [self.chatView addMessages:@[ model ]];
                 });
               }];
  }
}

#pragma mark - NEKaraokeSendGiftViewtDelegate

- (void)didSendGift:(NEKaraokeUIGiftModel *)gift {
  if (![self checkNetwork]) {
    return;
  }
  // 发送礼物
  [self
      dismissViewControllerAnimated:true
                         completion:^{
                           [[NEKaraokeKit shared]
                               sendGift:gift.giftId
                               callback:^(NSInteger code, NSString *_Nullable msg,
                                          id _Nullable obj) {
                                 if (code != 0) {
                                   [NEKaraokeToast
                                       showToast:[NSString stringWithFormat:@"发送礼物失败 %zd %@",
                                                                            code, msg]];
                                 }
                               }];
                         }];
}

#pragma mark - NEKaraokeInputToolBarDelegate

- (void)clickInputToolBarAction:(NEKaraokeInputToolBarAction)action {
  switch (action) {
    case NEKaraokeInputToolBarActionInput:
      // 发消息
      [self.toolBar becomeFirstResponse];
      break;
    case NEKaraokeInputToolBarActionChooseSong:
      // 点歌
      [self showChooseSingViewController];
      break;
    case NEKaraokeInputToolBarActionMute: {
      // 静音
      [self muteAudio:true];
    } break;
    case NEKaraokeInputToolBarActionUnmute: {
      // 解除静音
      [self unmuteAudio:true];
    } break;
    case NEKaraokeInputToolBarActionGift:
      // 发送礼物
      [NEKaraokeSendGiftViewController showWithTarget:self viewController:self];
      break;
    case NEKaraokeInputToolBarActionSeat:
      if (self.role == NEKaraokeViewRoleHost) {
        NEKaraokeSeatListVC *seatListVC = [[NEKaraokeSeatListVC alloc] init];
        seatListVC.delegate = self;
        seatListVC.isHost = YES;
        NEActionSheetNavigationController *nav =
            [[NEActionSheetNavigationController alloc] initWithRootViewController:seatListVC];
        nav.dismissOnTouchOutside = YES;
        [self presentViewController:nav animated:YES completion:nil];
      } else {
        switch (self.seatRequestType) {
          case NEKaraokeSeatRequestTypeOn: {
            if ([self checkNetwork]) {
              [self
                  showAlert:@"向房主申请上麦互动"
                    confirm:@"确定"
                      block:^{
                        if (![NEKaraokeAuthorityHelper checkMicAuthority]) {
                          [NEKaraokeToast showToast:@"请先开启麦克风权限"];
                          return;
                        }
                        [NEKaraokeKit.shared requestSeat:^(NSInteger code, NSString *_Nullable msg,
                                                           id _Nullable obj) {
                          dispatch_async(dispatch_get_main_queue(), ^{
                            if (code != 0) {
                              [NEKaraokeToast
                                  showToast:[NSString stringWithFormat:@"申请上麦失败: %@", msg]];
                            }
                          });
                        }];
                      }];
            }
          } break;
          case NEKaraokeSeatRequestTypeApplying: {
            NEKaraokeSeatListVC *seatListVC = [[NEKaraokeSeatListVC alloc] init];
            seatListVC.delegate = self;
            NEActionSheetNavigationController *nav =
                [[NEActionSheetNavigationController alloc] initWithRootViewController:seatListVC];
            nav.dismissOnTouchOutside = YES;
            [self presentViewController:nav animated:YES completion:nil];
          } break;
          default: {
            [NEKaraokeKit.shared
                leaveSeat:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                  if (code != 0) {
                    [NEKaraokeToast showToast:[NSString stringWithFormat:@"下麦失败: %@", msg]];
                  }
                }];
          } break;
        }
      }
      break;
    default:
      break;
  }
}

#pragma mark - gift animation

/// 播放礼物动画
- (void)playGiftWithName:(NSString *)name {
  [self.view addSubview:self.giftAnimation];
  [self.view bringSubviewToFront:self.giftAnimation];
  [self.giftAnimation addGift:name];
}

- (NEKaraokeAnimationView *)giftAnimation {
  if (!_giftAnimation) {
    _giftAnimation = [[NEKaraokeAnimationView alloc] init];
  }
  return _giftAnimation;
}

#pragma mark - NEKaraokeListener

- (void)onMemberJoinRoom:(NSArray<NEKaraokeMember *> *)members {
  NSMutableArray *messages = [NSMutableArray array];
  for (NEKaraokeMember *member in members) {
    NEKaraokeChatViewMessage *message = [[NEKaraokeChatViewMessage alloc] init];
    message.type = NEKaraokeChatViewMessageTypeNotication;
    message.notication = [NSString stringWithFormat:@"%@ 加入房间", member.name];
    [messages addObject:message];
  }
  dispatch_async(dispatch_get_main_queue(), ^{
    self.headerView.count = NEKaraokeKit.shared.allMemberList.count;
    [self.chatView addMessages:messages];
  });
}

- (void)onMemberLeaveRoom:(NSArray<NEKaraokeMember *> *)members {
  NSMutableArray *messages = [NSMutableArray array];
  for (NEKaraokeMember *member in members) {
    NEKaraokeChatViewMessage *message = [[NEKaraokeChatViewMessage alloc] init];
    message.type = NEKaraokeChatViewMessageTypeNotication;
    message.notication = [NSString stringWithFormat:@"%@ 离开房间", member.name];
    [messages addObject:message];
    [self fetchSeatRequestList];
  }
  dispatch_async(dispatch_get_main_queue(), ^{
    self.headerView.count = NEKaraokeKit.shared.allMemberList.count;
    [self.chatView addMessages:messages];
  });
}

- (void)onRoomEnded:(enum NEKaraokeEndReason)reason {
  [[NEKaraokePickSongEngine sharedInstance].currentOrderSongArray removeAllObjects];
  dispatch_async(dispatch_get_main_queue(), ^{
    if (reason != NEKaraokeEndReasonLeaveBySelf) {
      [NEKaraokeToast showToast:@"房间关闭"];
    }
    [self close];
  });
}

- (void)onRtcChannelError:(NSInteger)code {
  if (code == 30015) {
    [self close];
  }
}

- (void)onAudioOutputDeviceChanged:(enum NEKaraokeAudioOutputDevice)device {
  if (device == NEKaraokeAudioOutputDeviceWiredHeadset ||
      device == NEKaraokeAudioOutputDeviceBluetoothHeadset) {
    [self.audioManager setCanEarbackEnable:true];
    [self.audioManager enableEarback:true];
  } else {
    [self.audioManager enableEarback:false];
    [self.audioManager setCanEarbackEnable:false];
  }
}

- (void)checkAudioOutputDevice {
  AVAudioSessionRouteDescription *route = [[AVAudioSession sharedInstance] currentRoute];
  for (AVAudioSessionPortDescription *desc in [route outputs]) {
    if ([[desc portType] isEqualToString:AVAudioSessionPortHeadphones] ||
        [[desc portType] isEqualToString:AVAudioSessionPortBluetoothLE] ||
        [[desc portType] isEqualToString:AVAudioSessionPortBluetoothHFP] ||
        [[desc portType] isEqualToString:AVAudioSessionPortBluetoothA2DP]) {
      // 有耳机
      [self.audioManager setCanEarbackEnable:true];
      [self.audioManager enableEarback:true];
      return;
    }
  }
  [self.audioManager enableEarback:false];
  [self.audioManager setCanEarbackEnable:false];
}

- (void)onMemberAudioMuteChanged:(NEKaraokeMember *)member
                            mute:(BOOL)mute
                       operateBy:(NEKaraokeMember *_Nullable)operateBy {
  if ([member.account isEqualToString:NEKaraokeKit.shared.localMember.account] && _mute != mute) {
    _mute = mute;
    [self.bottomView setMicBtnSelected:mute];
  }
  [self.seatView configWithSeatItems:self.seatItems hostUuid:self.detail.anchor.userUuid];
}

- (void)onReceiveTextMessage:(NEKaraokeChatTextMessage *)message {
  dispatch_async(dispatch_get_main_queue(), ^{
    NEKaraokeChatViewMessage *model = [[NEKaraokeChatViewMessage alloc] init];
    model.type = NEKaraokeChatViewMessageTypeNormal;
    model.text = message.text;
    model.sender = message.fromNick;
    model.isAnchor = [message.fromUserUuid isEqualToString:self.detail.liveModel.userUuid];
    [self.chatView addMessages:@[ model ]];
  });
}
- (void)onReceiveGiftWithGiftModel:(NEKaraokeGiftModel *)giftModel {
  dispatch_async(dispatch_get_main_queue(), ^{
    // 展示礼物动画
    NEKaraokeChatViewMessage *message = [[NEKaraokeChatViewMessage alloc] init];
    message.type = NEKaraokeChatViewMessageTypeReward;
    message.giftId = (int)giftModel.giftId;
    message.giftFrom = giftModel.sendNick;
    [self.chatView addMessages:@[ message ]];

    if (self.role != NEKaraokeViewRoleHost) {
      // 房主不展示礼物
      NSString *giftName = [NSString stringWithFormat:@"anim_gift_0%zd", giftModel.giftId];
      [self playGiftWithName:giftName];
    }
  });
}

- (void)onReceiveChorusMessage:(NEKaraokeChorusActionType)actionType
                     songModel:(NEKaraokeSongModel *)songModel {
  self.songModel = songModel;
  switch (actionType) {
    case NEKaraokeChorusActionTypeInvite: {  // 收到合唱邀请
      self.chorusId = songModel.chorusId;
      self.localOrderSong = [self covertToOrderSong:songModel];
      [self.taskQueue addTask:[NEKaraokeTask defaultChorusMatchTask]];
      // 主唱不操作
      if ([self isAnchorWithSelf]) break;
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeMatch];
        [self.lyricActionView setSoloBtnHidden:true];
        [self.lyricActionView setJoinBtnHidden:false];
        [self.lyricActionView setMatchJoinBtnEnabled:true];
        self.lyricActionView.matchSongName = songModel.songName;
        self.lyricActionView.matchUserIcon = [UIImage karaoke_imageWithUrl:songModel.icon];
        [self.lyricActionView updateMatchTime:10];
      });
    } break;
    case NEKaraokeChorusActionTypeAgreeInvite: {
      [self.taskQueue addTask:[NEKaraokeTask defaultLoadSourceTask]];
      // 接受邀请歌词模块
      [self showAcceptChorusLyricModule];
    } break;
    case NEKaraokeChorusActionTypeCancelInvite: {
      // 取消邀请 歌词模块
      [self showCancelInviteLyrricModule];
      if ([self isAnchorWithSelf]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          @weakify(self) self.soloAlert =
              [UIAlertController alertControllerWithTitle:@"轮到你了"
                                                  message:@"唱歌时请【佩戴耳机】效果会更好哦"
                                           preferredStyle:UIAlertControllerStyleAlert];
          [self.soloAlert
              addAction:[UIAlertAction
                            actionWithTitle:@"放弃"
                                      style:UIAlertActionStyleCancel
                                    handler:^(UIAlertAction *_Nonnull action) {
                                      @strongify(self)[[NEKaraokeKit shared]
                                          abandonSongWithOrderId:self.localOrderSong.orderId
                                                        callback:^(NSInteger code,
                                                                   NSString *_Nullable msg,
                                                                   id _Nullable obj){

                                                        }];
                                    }]];
          [self.soloAlert
              addAction:[UIAlertAction
                            actionWithTitle:@"确定"
                                      style:UIAlertActionStyleDefault
                                    handler:^(UIAlertAction *_Nonnull action) {
                                      @strongify(self)[self.taskQueue removeTask];
                                      [NEKaraokeKit.shared
                                          requestPlaySongWithOrderId:self.localOrderSong.orderId
                                                            chorusId:nil
                                                                 ext:nil
                                                            callback:^(NSInteger code,
                                                                       NSString *_Nullable msg,
                                                                       id _Nullable obj) {
                                                              if (code != 0) {
                                                                [NEKaraokeToast
                                                                    showToast:[NSString
                                                                                  stringWithFormat:
                                                                                      @"开始"
                                                                                      @"唱歌"
                                                                                      @"失败:"
                                                                                      @" %@",
                                                                                      msg]];
                                                              }
                                                            }];
                                    }]];
          UIViewController *controller = self;
          if (controller.presentedViewController) {
            controller = controller.presentedViewController;
          }
          [controller presentViewController:self.soloAlert
                                   animated:true
                                 completion:^{
                                 }];
        });
      }
    } break;
    case NEKaraokeChorusActionTypeReady: {  // 准备好合唱了
      [self.taskQueue removeTask];
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView accompanyLoading:NO];
      });
      // 只有主唱才会调开始唱歌接口
      if (![self isAnchorWithSelf]) break;
      self.localOrderSong = [self covertToOrderSong:songModel];
      [NEKaraokeKit.shared
          requestPlaySongWithOrderId:songModel.orderId
                            chorusId:self.chorusId
                                 ext:nil
                            callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                              if (code != 0) {
                                [NEKaraokeToast
                                    showToast:[NSString stringWithFormat:@"开始唱歌失败: %@", msg]];
                              }
                            }];
    } break;
    case NEKaraokeChorusActionTypeStartSong: {
      self.time = 0;
      [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 正在演唱歌曲《%@》",
                                                                 songModel.actionOperator.userName,
                                                                 songModel.songName]];
      [self.taskQueue removeTask];
      NSString *originPath = [self fetchOriginalFilePathWithSongId:songModel.songId
                                                           channel:(int)songModel.oc_channel];
      NSString *accompanyPath = [self fetchAccompanyFilePathWithSongId:songModel.songId
                                                               channel:(int)songModel.oc_channel];
      // 主唱ID
      NSString *anchorUuid = songModel.account;
      NSString *chorusUuid = songModel.assistantUuid;
      // 当前模式
      NEKaraokeSongMode songMode = [self fetchCurrentSongMode];
      NSString *lyric = [self fetchLyricContentWithSongId:songModel.songId
                                                  channel:(int)songModel.oc_channel];
      // 自己独唱 展示打分
      if (songMode == NEKaraokeSongModeSolo && [self isAnchorWithSelf]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          self.hasMarked = NO;
          if (lyric.length) {
            [self.lyricActionView
                setPitchContent:[self fetchPitchContentWithSongId:songModel.songId
                                                          channel:(int)songModel.oc_channel]
                      separator:@","
                   lyricContent:lyric
                      lyricType:songModel.oc_channel == MIGU ? NELyricTypeKas : NELyricTypeYrc];
            [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypePitch];
            self.lyricActionView.lyricSeekBtnHidden = false;
          } else {
            [self showNoLyricView:songModel];
          }
        });
      } else {
        // 开始合唱，展示歌词页，独唱展示打分，合唱展示歌词
        dispatch_async(dispatch_get_main_queue(), ^{
          if (lyric.length) {
            [self.lyricActionView
                setLyricContent:lyric
                      lyricType:songModel.oc_channel == MIGU ? NELyricTypeKas : NELyricTypeYrc];
            [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeLyric];
            self.lyricActionView.lyricSeekBtnHidden = true;
          } else {
            [self showNoLyricView:songModel];
          }
        });
      }

      dispatch_async(dispatch_get_main_queue(), ^{
        NSInteger duration = songModel.oc_songTime;
        if (duration <= 0) {
          duration = [NEKaraokeKit.shared getEffectDuration];
        }
        self.lyricActionView.lyricDuration = duration;
        // 操作区重置
        [self.controlView reset];
      });
      // 自动打开麦克
      [self autoUnmuteAudio];

      // 展示控制视图
      [self configControlView:songMode];
      // 听众不播放
      if ([self isAudience:songModel]) break;
      //开始唱歌前添加保护 ：最终打分 + 图片解码时间超过3s ，隐藏打分View
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView hideScoreView];
      });
      // 默认设置一把采集音量
      [self.audioManager adjustRecordingSignalVolume:[self.audioManager getRecordingSignalVolume]];
      NSData *origin = [NSData dataWithContentsOfFile:originPath];
      NSData *accompany = [NSData dataWithContentsOfFile:accompanyPath];
      dispatch_async(dispatch_get_main_queue(), ^{
        if (self.controlView.isOrginalEnabled) {
          if (!origin.length && accompany.length) {
            // 只有伴奏
            [self.controlView selectOrginal:false];
            [self.controlView enableOrginal:false];
          } else if (origin.length && !accompany.length) {
            // 只有原唱
            [self.controlView selectOrginal:true];
            [self.controlView enableOrginal:false];
          }
        }
      });

      [NEKaraokeKit.shared
          playSongWithOriginPath:originPath
                   accompanyPath:accompanyPath
                          volume:[self.audioManager
                                     getAudioEffectVolumeWithEffectId:
                                         NEKaraokeKit.shared.currentSongIdForAudioEffect]
                   anchorAccount:anchorUuid
                   chorusAccount:chorusUuid
                  startTimeStamp:3000  // 实时需要3秒，跟副唱同步
                          anchor:[self isAnchorWithSelf]
                            mode:songMode
                        callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj){
                        }];
    } break;
    case NEKaraokeChorusActionTypePauseSong: {
      [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 暂停《%@》",
                                                                 songModel.actionOperator.userName,
                                                                 songModel.songName]];
      // 听众且不是房主
      if ([self isAudience:songModel] && self.role != NEKaraokeViewRoleHost) break;
      NEKaraokeSongMode songMode = [self fetchCurrentSongMode];
      switch (songMode) {
        case NEKaraokeSongModeSolo:
        case NEKaraokeSongModeSeaialChorus: {  // 独唱串行合唱
          dispatch_async(dispatch_get_main_queue(), ^{
            [self.controlView selectPause:YES];
          });
        } break;
        case NEKaraokeSongModeRealTimeChorus: {  // 实时
          // 1s后更新UI
          dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC),
                         dispatch_get_main_queue(), ^{
                           [self.controlView selectPause:YES];
                         });
        } break;
        default:
          break;
      }

      // 自己独唱 暂停打分
      if (songMode == NEKaraokeSongModeSolo && [self isAnchorWithSelf]) {
        [self.lyricActionView pitchPause];
      }
    } break;
    case NEKaraokeChorusActionTypeResumeSong: {
      [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 正在演唱歌曲《%@》",
                                                                 songModel.actionOperator.userName,
                                                                 songModel.songName]];
      // 听众且不是房主
      if ([self isAudience:songModel] && self.role != NEKaraokeViewRoleHost) break;
      NEKaraokeSongMode songMode = [self fetchCurrentSongMode];
      switch (songMode) {
        case NEKaraokeSongModeSolo:
        case NEKaraokeSongModeSeaialChorus: {
          dispatch_async(dispatch_get_main_queue(), ^{
            [self.controlView selectPause:NO];
          });
        } break;
        case NEKaraokeSongModeRealTimeChorus: {
          // 1s后更新UI
          dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC),
                         dispatch_get_main_queue(), ^{
                           [self.controlView selectPause:NO];
                         });
        } break;
        default:
          break;
      }

      // 自己独唱 暂停打分
      if (songMode == NEKaraokeSongModeSolo && [self isAnchorWithSelf]) {
        [self.lyricActionView pitchStart];
      }
    } break;
    case NEKaraokeChorusActionTypeEndSong: {
      self.time = 0;
      self.songModel = nil;
      // 结束，展示下一首歌的界面
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChooseSong];
        [self showControlView:NO];
      });
    } break;
    case NEKaraokeChorusActionTypeAbandon: {
      self.time = 0;
      self.songModel = nil;
      [self.taskQueue removeTask];
      // 结束，展示下一首歌的界面
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChooseSong];
      });
    } break;
    case NEKaraokeChorusActionTypeNext: {  // 开始播放 150
      self.time = 0;
      [[NEKaraokeKit shared]
          preloadSongLyric:songModel.songId
                   channel:(int)songModel.oc_channel
                  callback:^(NSString *_Nullable content, NSString *_Nullable lyricType,
                             NSError *_Nullable error){
                  }];
      [self.taskQueue removeTask];
      if ([songModel.account isEqualToString:NEKaraokeKit.shared.localMember.account]) {  // 是自己
        // 本地保存
        self.localOrderSong = [self covertToOrderSong:songModel];
        if (NEKaraokeKit.shared.allMemberList.count > 1) {
          // 有其他人在就去邀请
          dispatch_async(dispatch_get_main_queue(), ^{
            [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeMatch];
            [self.lyricActionView setSoloBtnHidden:false];
            [self.lyricActionView setJoinBtnHidden:true];
            [self.lyricActionView setMatchJoinBtnEnabled:false];
            self.lyricActionView.matchSongName = self.localOrderSong.songName;
            self.lyricActionView.matchUserIcon = [UIImage karaoke_imageWithUrl:songModel.icon];
            [self.lyricActionView updateMatchTime:10];
          });
          // 邀请
          [NEKaraokeKit.shared
              inviteChorusWithOrderId:songModel.orderId
                             callback:^(NSInteger code, NSString *_Nullable msg,
                                        NEKaraokeSongModel *_Nullable songModel) {
                               if (code != 0) {
                                 [NEKaraokeToast
                                     showToast:[NSString
                                                   stringWithFormat:@"邀请合唱失败: %@", msg]];
                               }
                             }];
        } else {
          // 独唱
          dispatch_async(dispatch_get_main_queue(), ^{
            [self.taskQueue addTask:[NEKaraokeTask defaultSoloWaitTask]];
            [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeWait];
            self.lyricActionView.waitSongName = songModel.songName;
            self.lyricActionView.waitUserIcon = [UIImage karaoke_imageWithUrl:songModel.icon];
            self.lyricActionView.waitUserName = songModel.userName;
            @weakify(self) self.soloAlert =
                [UIAlertController alertControllerWithTitle:@"轮到你了"
                                                    message:@"唱歌时请【佩戴耳机】效果会更好哦"
                                             preferredStyle:UIAlertControllerStyleAlert];
            [self.soloAlert
                addAction:[UIAlertAction
                              actionWithTitle:@"放弃"
                                        style:UIAlertActionStyleCancel
                                      handler:^(UIAlertAction *_Nonnull action) {
                                        @strongify(self)[[NEKaraokeKit shared]
                                            abandonSongWithOrderId:self.localOrderSong.orderId
                                                          callback:^(NSInteger code,
                                                                     NSString *_Nullable msg,
                                                                     id _Nullable obj){

                                                          }];
                                      }]];
            [self.soloAlert
                addAction:
                    [UIAlertAction
                        actionWithTitle:@"确定"
                                  style:UIAlertActionStyleDefault
                                handler:^(UIAlertAction *_Nonnull action) {
                                  @strongify(self)[self.taskQueue removeTask];
                                  [NEKaraokeKit.shared
                                      requestPlaySongWithOrderId:self.localOrderSong.orderId
                                                        chorusId:nil
                                                             ext:nil
                                                        callback:^(NSInteger code,
                                                                   NSString *_Nullable msg,
                                                                   id _Nullable obj) {
                                                          if (code != 0) {
                                                            [NEKaraokeToast
                                                                showToast:
                                                                    [NSString
                                                                        stringWithFormat:@"开"
                                                                                         @"始"
                                                                                         @"唱"
                                                                                         @"歌"
                                                                                         @"失"
                                                                                         @"败:"
                                                                                         @" %"
                                                                                         @"@",
                                                                                         msg]];
                                                          }
                                                        }];
                                }]];
            UIViewController *controller = self;
            if (controller.presentedViewController) {
              controller = controller.presentedViewController;
            }
            [controller presentViewController:self.soloAlert
                                     animated:true
                                   completion:^{
                                   }];
          });
        }
      }
    } break;
    default:
      break;
  }
}
- (void)onSongPlayingPosition:(uint64_t)postion {
  if (postion < self.time) return;
  // 出现position突变
  if (postion - self.time > 1000) {
    [NEKaraokeLog
        infoLog:@"NEKaraokeKit"
           desc:[NSString stringWithFormat:@"NEKaraokeViewController onSongPlayingPosition "
                                           @"large postion:%llu time:%zd",
                                           postion, self.time]];
  }
  self.time = postion;
  [self.lyricActionView updateLyric];
}

- (void)onRecordingAudioFrameWithFrame:(NEKaraokeAudioFrame *)frame {
  if (_hasMarked) {
    return;
  }
  BOOL isEnd = NO;
  if (self.lyricActionView.lyricDuration > 0 && self.time > 0 &&
      self.lyricActionView.lyricDuration < (self.time + 2000)) {
    isEnd = YES;
  }

  [self.lyricActionView pushAudioFrameWithFrame:frame isEnd:isEnd];
  if (isEnd) {
    _hasMarked = YES;
  }
}
- (void)onSongPlayingCompleted {
  //此处添加展示打分 添加延迟
  //判断是否为独唱
  NEKaraokeSongMode songMode = [self fetchCurrentSongMode];
  // 自己独唱 展示打分
  if ([self isAnchorWithSelf]) {
    if (songMode == NEKaraokeSongModeSolo && [self needShowFinalScoreView]) {
      [self showFinalScoreView];
      // 5秒延迟操作
      double delayInSeconds = 5.0;
      dispatch_time_t popTime =
          dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
      dispatch_after(popTime, dispatch_get_main_queue(), ^(void) {
        [NEKaraokeSongLog successLog:karaokeSongLog desc:@"隐藏打分"];
        //隐藏歌词组件
        [self.lyricActionView showSubview:NEKaraokeLyricActionSubviewTypeChooseSong];
        [self showControlView:NO];
        [self.lyricActionView hideScoreView];
        [NEKaraokeKit.shared
            requestStopPlayingSong:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
              if (code != 0) {
                [NEKaraokeToast showToast:[NSString stringWithFormat:@"停止播放失败: %@", msg]];
              }
            }];
      });
    } else {
      [NEKaraokeKit.shared
          requestStopPlayingSong:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
            if (code != 0) {
              [NEKaraokeToast showToast:[NSString stringWithFormat:@"停止播放失败: %@", msg]];
            }
          }];
    }
  }
}

/// 是否是听众
- (BOOL)isAudience:(NEKaraokeSongModel *)songModel {
  NSString *userUuid = NEKaraokeKit.shared.localMember.account;
  if ([songModel.account isEqualToString:userUuid] ||
      [songModel.assistantUuid isEqualToString:userUuid]) {
    return NO;
  }
  return YES;
}
#pragma mark - NEKaraokeLyricActionViewDelegate
- (void)onLyricAction:(NEKaraokeLyricActionType)action {
  switch (action) {
    case NEKaraokeLyricActionTypeChooseSong:
      // 点歌
      [self showChooseSingViewController];
      break;
    case NEKaraokeLyricActionTypeToSolo: {
      [self.taskQueue removeTask];
      if (![self checkNetwork]) {
        return;
      }
      [NEKaraokeKit.shared
          requestPlaySongWithOrderId:self.localOrderSong.orderId
                            chorusId:nil
                                 ext:nil
                            callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                              if (code != 0) {
                                [NEKaraokeToast
                                    showToast:[NSString stringWithFormat:@"开始唱歌失败: %@", msg]];
                              }
                            }];
    } break;
    case NEKaraokeLyricActionTypeJoinChorus: {
      if (![self checkNetwork]) {
        return;
      }
      if (![self isOnSeat]) {
        [self showAlert:@"仅麦上成员才能加入合唱"
                confirm:@"申请上麦"
                  block:^{
                    if (![NEKaraokeAuthorityHelper checkMicAuthority]) {  // 麦克风权限
                      [NEKaraokeToast showToast:@"请先开启麦克风权限"];
                      return;
                    }
                    [NEKaraokeKit.shared
                        requestSeat:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                          if (code != 0) {
                            [NEKaraokeToast
                                showToast:[NSString stringWithFormat:@"申请上麦失败: %@", msg]];
                          }
                        }];
                  }];
        return;
      }
      // 主动加入合唱
      @weakify(self)[NEKaraokeKit.shared
          joinChorusWithChorusId:self.chorusId
                        callback:^(NSInteger code, NSString *_Nullable msg,
                                   NEKaraokeSongModel *_Nullable songModel) {
                          if (code == 0) {
                            NSInteger singMode = [songModel oc_singMode];
                            if (singMode == 0 || singMode == 2) {  //下载全部内容
                              @strongify(self) self->_preloadSong = true;
                              [[NEKaraokeKit shared] preloadSong:songModel.songId
                                                         channel:(int)songModel.oc_channel
                                                         observe:self];
                            } else {  // 下载歌词
                              @weakify(self)[[NEKaraokeKit shared]
                                  preloadSongLyric:songModel.songId
                                           channel:(int)songModel.oc_channel
                                          callback:^(NSString *_Nullable content,
                                                     NSString *_Nullable lyricType,
                                                     NSError *_Nullable error) {
                                            if (!error) {
                                              dispatch_async(dispatch_get_main_queue(), ^{
                                                @strongify(self)[self.lyricActionView
                                                    setLyricContent:content
                                                          lyricType:songModel.oc_channel == MIGU
                                                                        ? NELyricTypeKas
                                                                        : NELyricTypeYrc];
                                              });
                                              // 发送准备
                                              [NEKaraokeKit.shared
                                                  chorusReadyWithChorusId:songModel.chorusId
                                                                 callback:^(
                                                                     NSInteger code,
                                                                     NSString *_Nullable msg,
                                                                     NEKaraokeSongModel
                                                                         *_Nullable songModel) {
                                                                   if (code != 0) {
                                                                     [NEKaraokeToast
                                                                         showToast:
                                                                             [NSString
                                                                                 stringWithFormat:
                                                                                     @"加入合唱失败"
                                                                                     @": %@",
                                                                                     msg]];
                                                                   }
                                                                 }];
                                            }
                                          }];
                            }
                          } else {
                            [NEKaraokeToast showToast:[NSString stringWithFormat:@"加入合唱失败 "
                                                                                 @"%zd %@",
                                                                                 code, msg]];
                          }
                        }];
    } break;
    default:
      break;
  }
}

- (NSInteger)onLyricTime {
  return self.time;
}

- (void)onLyricSeek:(NSInteger)seek {
  [[NEKaraokeKit shared] setPlayingPositionWithPosition:seek];
}

#pragma mark-----------------------------  Getter and Setter  -----------------------------
- (void)setSeatRequestType:(NEKaraokeSeatRequestType)seatRequestType {
  _seatRequestType = seatRequestType;
  NEKaraokeInputToolBarSeatType seatType = NEKaraokeInputToolBarSeatTypeOn;
  switch (seatRequestType) {
    case NEKaraokeSeatRequestTypeOn:
      seatType = NEKaraokeInputToolBarSeatTypeOn;
      break;
    case NEKaraokeSeatRequestTypeApplying:
      seatType = NEKaraokeInputToolBarSeatTypeOption;
      break;
    default:
      seatType = NEKaraokeInputToolBarSeatTypeDown;
      break;
  }
  [self.bottomView configSeatWithType:seatType];
}
- (NSMutableArray<NEKaraokeSeatItem *> *)seatItems {
  if (!_seatItems) {
    _seatItems = @[].mutableCopy;
  }
  return _seatItems;
}
#pragma mark-----------------------------  NESongPreloadProtocol  -----------------------------
- (void)karaoke_onPreloadComplete:(NSString *)songId
                          channel:(SongChannel)channel
                            error:(NSError *)error {
  if (_preloadSong) {
    _preloadSong = false;
    if (!error) {
      [NEKaraokeKit.shared
          chorusReadyWithChorusId:self.chorusId
                         callback:^(NSInteger code, NSString *_Nullable msg,
                                    NEKaraokeSongModel *_Nullable songModel) {
                           if (code != 0) {
                             [NEKaraokeToast
                                 showToast:[NSString stringWithFormat:@"加入合唱失败: %@", msg]];
                           }
                         }];
    } else {
      [NEKaraokeToast showToast:[NSString stringWithFormat:@"下载歌曲失败 %@", error.description]];
    }
  }
}

#pragma mark-----------------------------  NEKaraokeSongProtocol  -----------------------------
/// 列表变更
- (void)onSongListChanged {
  [self fetchPickSongList];
}

/// 点歌
- (void)onSongOrdered:(NEKaraokeOrderSongModel *)song {
  [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 点了《%@》",
                                                             song.actionOperator.userName,
                                                             song.songName]];
}
- (void)onSongDeleted:(NEKaraokeOrderSongModel *)song {
  [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 取消《%@》",
                                                             song.actionOperator.userName,
                                                             song.songName]];
}

- (void)onSongTopped:(NEKaraokeOrderSongModel *)song {
  [self sendChatroomNotifyMessage:[NSString stringWithFormat:@"%@ 置顶《%@》",
                                                             song.actionOperator.userName,
                                                             song.songName]];
}

- (void)onNextSong:(NEKaraokeOrderSongModel *)song {
  [self sendChatroomNotifyMessage:[NSString
                                      stringWithFormat:@"%@ 已切歌", song.actionOperator.userName]];
}

- (BOOL)shouldAutorotate {
  return NO;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
  return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
  return UIInterfaceOrientationPortrait;
}

#pragma mark 展示最后打分
/// 是否展示
- (BOOL)needShowFinalScoreView {
  return [self.lyricActionView hasPitchConotent];
}
- (void)showFinalScoreView {
  __block NEPitchPlayResultModel *playResultModel = [[NEPitchPlayResultModel alloc] init];
  playResultModel.nickName = [self.localOrderSong.userName mutableCopy];
  playResultModel.songName = [self.localOrderSong.songName mutableCopy];
  playResultModel.headerUrl = [self.localOrderSong.icon mutableCopy];

  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    @weakify(self)[[NEPitchSongScore getInstance]
        getFinalScoreComplete:^(NSError *_Nullable error,
                                NEPitchRecordSingInfo *_Nullable pitchRecordSingInfo) {
          __block long markValue = pitchRecordSingInfo.chorusFinalMark.totalValue /
                                   pitchRecordSingInfo.availableLyricCount;
          __block NEOpusLevel markLevel = NEOpusLevelC;
          dispatch_async(dispatch_get_main_queue(), ^{
            if (markValue > 90) {
              markLevel = NEOpusLevelSSS;
            } else if (markValue > 80) {
              markLevel = NEOpusLevelSS;
            } else {
              markLevel = NEOpusLevelS;
            }
            //结束 开始打分
            [NEKaraokeSongLog successLog:karaokeSongLog desc:@"展示打分"];
            @strongify(self)[self.lyricActionView lyricActionViewLevel:markLevel
                                                           resultModel:playResultModel];
          });
        }];
  });
}

- (NEKaraokeReachability *)reachability {
  if (!_reachability) {
    _reachability = [NEKaraokeReachability reachabilityForInternetConnection];
  }
  return _reachability;
}
@end
