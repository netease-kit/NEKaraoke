// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeToast.h"
#import "NEKaraokeViewController+Seat.h"
#import "NEKaraokeViewController+UI.h"
#import "NEKaraokeViewController+Utils.h"
/// 麦位信息类型
typedef NS_ENUM(NSInteger, NEKaraokeSeatMessageType) {
  NEKaraokeSeatMessageTypeApply,
  NEKaraokeSeatMessageTypeCancelApply,
  NEKaraokeSeatMessageTypeApprove,
  NEKaraokeSeatMessageTypeReject,
  NEKaraokeSeatMessageTypeLeave
};

@implementation NEKaraokeViewController (Seat)
- (void)getSeatInfo {
  [NEKaraokeKit.shared getSeatInfo:^(NSInteger code, NSString *_Nullable msg,
                                     NEKaraokeSeatInfo *_Nullable seatInfo) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if (seatInfo) {
        [self sortSeatItems:seatInfo.seatItems];
        [self.seatView configWithSeatItems:seatInfo.seatItems hostUuid:self.detail.anchor.userUuid];
      }
    });
    // 刷新麦位唱歌展示
    [NEKaraokeKit.shared requestPlayingSongInfo:^(NSInteger code, NSString *_Nullable msg,
                                                  NEKaraokeSongModel *_Nullable model) {
      dispatch_async(dispatch_get_main_queue(), ^{
        if (model) {
          [self.seatView configSongModel:model];
        }
      });
    }];
  }];
}
- (BOOL)isOnSeat {
  BOOL flag = NO;
  for (NEKaraokeSeatItem *item in self.seatItems) {
    if ([item.user isEqualToString:NEKaraokeKit.shared.localMember.account]) {
      flag = YES;
    }
  }
  return flag;
}
- (void)sortSeatItems:(NSArray<NEKaraokeSeatItem *> *)items {
  [self.seatItems removeAllObjects];
  NSMutableArray *existArr = @[].mutableCopy;
  NSMutableArray *emptyArr = @[].mutableCopy;
  for (NEKaraokeSeatItem *item in items) {
    if (item.user.length) {
      [existArr addObject:item];
    } else {
      [emptyArr addObject:item];
    }
  }
  NSArray *arr = [existArr sortedArrayUsingComparator:^NSComparisonResult(
                               NEKaraokeSeatItem *_Nonnull obj1, NEKaraokeSeatItem *_Nonnull obj2) {
    return [@(obj1.updated) compare:@(obj2.updated)];
  }];
  [self.seatItems addObjectsFromArray:arr];
  [self.seatItems addObjectsFromArray:emptyArr];
}
- (void)fetchSeatRequestList {
  if (self.role != NEKaraokeViewRoleHost &&
      self.seatRequestType != NEKaraokeSeatRequestTypeApplying) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.bottomView configSeatUnreadNumber:0];
    });
    return;
  }
  [NEKaraokeKit.shared getSeatRequestList:^(NSInteger code, NSString *_Nullable msg,
                                            NSArray<NEKaraokeSeatRequestItem *> *_Nullable items) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if (code == 0) {
        [self.bottomView configSeatUnreadNumber:items.count];
      }
    });
  }];
}
#pragma mark-----------------------------  NEKaraokeSeatListVCDelegate  -----------------------------
- (void)cancelRequestSeat:(NEKaraokeSeatRequestItem *)item {
  [self dismissViewControllerAnimated:YES
                           completion:^{
                             [NEKaraokeKit.shared cancelRequestSeat:^(NSInteger code,
                                                                      NSString *_Nullable msg,
                                                                      id _Nullable obj) {
                               if (code != 0) {
                                 [NEKaraokeToast
                                     showToast:[NSString
                                                   stringWithFormat:@"取消申请上麦失败: %@", msg]];
                               }
                             }];
                           }];
}
- (void)rejectRequestSeat:(NEKaraokeSeatRequestItem *)item {
  [self
      dismissViewControllerAnimated:YES
                         completion:^{
                           [NEKaraokeKit.shared
                               rejectRequestSeatWithAccount:item.user
                                                   callback:^(NSInteger code,
                                                              NSString *_Nullable msg,
                                                              id _Nullable obj) {
                                                     if (code != 0) {
                                                       [NEKaraokeToast
                                                           showToast:[NSString
                                                                         stringWithFormat:@"拒绝申"
                                                                                          @"请上麦"
                                                                                          @"失败: "
                                                                                          @"%@",
                                                                                          msg]];
                                                     }
                                                   }];
                         }];
}
- (void)allowRequestSeat:(NEKaraokeSeatRequestItem *)item {
  [self
      dismissViewControllerAnimated:YES
                         completion:^{
                           [NEKaraokeKit.shared
                               approveRequestSeatWithAccount:item.user
                                                    callback:^(NSInteger code,
                                                               NSString *_Nullable msg,
                                                               id _Nullable obj) {
                                                      if (code != 0) {
                                                        [NEKaraokeToast
                                                            showToast:[NSString
                                                                          stringWithFormat:@"同意"
                                                                                           @"申请"
                                                                                           @"上麦"
                                                                                           @"失败:"
                                                                                           @" %@",
                                                                                           msg]];
                                                      }
                                                    }];
                         }];
}
- (NSString *)getUserNameWithUserUuid:(NSString *)userUuid {
  for (NEKaraokeMember *member in NEKaraokeKit.shared.allMemberList) {
    if ([member.account isEqualToString:userUuid]) {
      return member.name;
    }
  }
  return nil;
}
#pragma mark-----------------------------  NEKaraokeListener Seat  -----------------------------
/// 请求
- (void)onSeatRequestSubmitted:(NSInteger)seatIndex account:(NSString *_Nonnull)account {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeApply];
  if (self.role != NEKaraokeViewRoleHost &&
      [account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
    self.seatRequestType = NEKaraokeSeatRequestTypeApplying;
  }
  [self fetchSeatRequestList];
}

// 取消请求
- (void)onSeatRequestCancelled:(NSInteger)seatIndex account:(NSString *_Nonnull)account {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeCancelApply];
  if (self.role != NEKaraokeViewRoleHost &&
      [account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
    self.seatRequestType = NEKaraokeSeatRequestTypeOn;
  }
  [self fetchSeatRequestList];
}
// 同意请求
- (void)onSeatRequestApproved:(NSInteger)seatIndex
                      account:(NSString *_Nonnull)account
                    operateBy:(NSString *_Nonnull)operateBy {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeApprove];

  if (self.role != NEKaraokeViewRoleHost &&
      [account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.bottomView isShowMicBtn:YES];
    });
    self.seatRequestType = NEKaraokeSeatRequestTypeDown;
    [self unmuteAudio:true];
  }
  [self fetchSeatRequestList];
}
// 拒绝请求
- (void)onSeatRequestRejected:(NSInteger)seatIndex
                      account:(NSString *_Nonnull)account
                    operateBy:(NSString *_Nonnull)operateBy {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeReject];
  if (self.role != NEKaraokeViewRoleHost &&
      [account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
    self.seatRequestType = NEKaraokeSeatRequestTypeOn;
  }
  [self fetchSeatRequestList];
}
- (void)onSeatLeave:(NSInteger)seatIndex account:(NSString *_Nonnull)account {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeLeave];
  if (self.role == NEKaraokeViewRoleHost) return;
  if ([account isEqualToString:NEKaraokeKit.shared.localMember.account]) {  // 自己
    // 下麦不展示麦克风按钮
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.bottomView isShowMicBtn:NO];
    });
    if ([account isEqualToString:self.songModel.account] ||
        [account isEqualToString:self.songModel.assistantUuid]) {  // 下麦
                                                                   // 自己是主唱或者副唱,
                                                                   // 停止播放
      [NEKaraokeKit.shared
          requestStopPlayingSong:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj){
          }];
    }
    self.seatRequestType = NEKaraokeSeatRequestTypeOn;
    [self muteAudio:false];
  }
}

- (void)onSeatKicked:(NSInteger)seatIndex
             account:(NSString *_Nonnull)account
           operateBy:(NSString *_Nonnull)operateBy {
  [self sendSeatMessageToChatroom:account messageType:NEKaraokeSeatMessageTypeLeave];

  if (self.role == NEKaraokeViewRoleHost) return;
  if ([account isEqualToString:NEKaraokeKit.shared.localMember.account]) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.bottomView isShowMicBtn:NO];
    });
    self.seatRequestType = NEKaraokeSeatRequestTypeOn;
    [self muteAudio:false];
  }
}

- (void)onSeatListChanged:(NSArray<NEKaraokeSeatItem *> *)seatItems {
  [self sortSeatItems:seatItems];
  [self.seatView configWithSeatItems:seatItems hostUuid:self.detail.anchor.userUuid];
}

// 聊天室发麦位消息
- (void)sendSeatMessageToChatroom:(NSString *)userUuid
                      messageType:(NEKaraokeSeatMessageType)msgType {
  NSString *userName = [self getUserNameWithUserUuid:userUuid];
  if (!userName.length) return;
  NSString *content = @"";
  switch (msgType) {
    case NEKaraokeSeatMessageTypeApply:
      content = [NSString stringWithFormat:@"%@ 申请上麦", userName];
      break;
    case NEKaraokeSeatMessageTypeCancelApply:
      content = [NSString stringWithFormat:@"%@ 取消申请上麦", userName];
      break;
    case NEKaraokeSeatMessageTypeApprove:
      content = [NSString stringWithFormat:@"%@ 已上麦", userName];
      break;
    case NEKaraokeSeatMessageTypeReject:
      content = [NSString stringWithFormat:@"房主拒绝 %@ 申请上麦", userName];
      break;
    default:
      content = [NSString stringWithFormat:@"%@ 已下麦", userName];
      break;
  }
  [self sendChatroomNotifyMessage:content];
}

#pragma mark-----------------------------  NEKaraokeSeatViewDelegate  -----------------------------
- (void)didSelected:(NEKaraokeSeatView *)seatView seatItem:(NEKaraokeSeatItem *)seatItem {
  switch (seatItem.status) {
    case NEKaraokeSeatItemStatusInitial: {       // 初始化
      if (self.role != NEKaraokeViewRoleHost) {  // 不是房主申请上麦
        // 如果在麦上  不展示
        if ([self isOnSeat]) break;
        [self showAlert:@"向房主申请上麦互动"
                confirm:@"确定"
                  block:^{
                    [NEKaraokeKit.shared
                        requestSeat:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                          if (code != 0) {
                            [NEKaraokeToast
                                showToast:[NSString stringWithFormat:@"申请上麦失败: %@", msg]];
                          }
                        }];
                  }];
      }
    } break;
    case NEKaraokeSeatItemStatusTaken: {  // 已经有人
      if (self.role == NEKaraokeViewRoleHost &&
          ![NEKaraokeKit.shared.localMember.account isEqualToString:seatItem.user]) {  // 房主 踢人
        [self showAlert:@"是否踢他下麦"
                 cancel:@"确定"
                  block:^{
                    [NEKaraokeKit.shared
                        kickSeatWithAccount:seatItem.user
                                   callback:^(NSInteger code, NSString *_Nullable msg,
                                              id _Nullable obj) {
                                     if (code != 0) {
                                       [NEKaraokeToast
                                           showToast:[NSString stringWithFormat:@"麦位踢人失败: %@",
                                                                                msg]];
                                     }
                                   }];
                  }];
      } else if (self.role != NEKaraokeViewRoleHost &&
                 [NEKaraokeKit.shared.localMember.account
                     isEqualToString:seatItem.user]) {  // 主动下麦
        [self showAlert:@"当前正在麦上\n确定要下麦"
                 cancel:@"确定"
                  block:^{
                    [NEKaraokeKit.shared
                        leaveSeat:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                          if (code != 0) {
                            [NEKaraokeToast
                                showToast:[NSString stringWithFormat:@"主动下麦失败: %@", msg]];
                          }
                        }];
                  }];
      }
    } break;
    default:
      break;
  }
}
@end
