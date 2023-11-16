// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeLocalized.h"
#import "NEKaraokeToast.h"
#import "NEKaraokeViewController+Utils.h"

@implementation NEKaraokeViewController (Utils)
- (BOOL)isAnchorWithSelf {
  return
      [NEKaraokeKit.shared.localMember.account isEqualToString:self.songModel.chorusInfo.userUuid];
}
- (BOOL)isChoristerWithSelf {
  return [NEKaraokeKit.shared.localMember.account
      isEqualToString:self.songModel.chorusInfo.assistantUuid];
}
- (NEKaraokeOrderSongResult *)covertToOrderSong:(NEKaraokeSongModel *)model {
  NEKaraokeOrderSongResult *orderSong = [NEKaraokeOrderSongResult new];
  orderSong.orderSong = [NEKaraokeOrderSongSongModel new];
  orderSong.orderSong.orderId = model.chorusInfo.orderId;
  orderSong.orderSong.songId = model.chorusInfo.songId;
  orderSong.orderSong.roomUuid = model.chorusInfo.roomUuid;
  orderSong.orderSong.songName = model.chorusInfo.songName;
  orderSong.orderSong.songCover = model.chorusInfo.songCover;
  orderSong.orderSong.singer = model.chorusInfo.singer;
  orderSong.orderSongUser = [NEKaraokeOrderSongOperatorUser new];
  orderSong.orderSongUser.userUuid = model.chorusInfo.userUuid;
  orderSong.orderSongUser.userName = model.chorusInfo.userName;
  orderSong.orderSongUser.icon = model.chorusInfo.icon;
  return orderSong;
}
- (NEKaraokeSongMode)fetchCurrentSongMode {
  NEKaraokeSongMode songMode = NEKaraokeSongModeSolo;
  NEKaraokeSingMode singMode = self.songModel.chorusInfo.singMode;
  // 副唱Id
  NSString *chorusUuid = self.songModel.chorusInfo.assistantUuid;
  if (chorusUuid.length > 0) {
    if (singMode == NEKaraokeSingModeAIChorus) {  // 智能
      NSInteger chorusType = self.songModel.chorusInfo.chorusType;
      if (chorusType == 1) {
        songMode = NEKaraokeSongModeSeaialChorus;
      } else if (chorusType == 2) {
        songMode = NEKaraokeSongModeRealTimeChorus;
      }
    } else if (singMode == NEKaraokeSingModeSerialChorus) {  // 串行
      songMode = NEKaraokeSongModeSeaialChorus;
    } else if (singMode == NEKaraokeSingModeNTPChorus) {  // 实时
      songMode = NEKaraokeSongModeRealTimeChorus;
    }
  }
  return songMode;
}

/// 自动打开麦克
- (void)autoUnmuteAudio {
  // 自己是演唱者且麦克风关闭时，打开
  if (([self.songModel.chorusInfo.userUuid
           isEqualToString:NEKaraokeKit.shared.localMember.account] ||
       [self.songModel.chorusInfo.assistantUuid
           isEqualToString:NEKaraokeKit.shared.localMember.account]) &&
      !NEKaraokeKit.shared.localMember.isAudioOn) {
    [self unmuteAudio:false];
  }
}

- (void)muteAudio:(BOOL)showToast {
  __weak typeof(self) weakSelf = self;
  [[NEKaraokeKit shared] muteMyAudio:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if (code != 0) {
        if (code == 1021) {
          // 成员不存在，在退出的时候调用会多余提示
        } else {
          [NEKaraokeToast
              showToast:[NSString stringWithFormat:@"%@ %zd %@", NELocalizedString(@"静音失败"),
                                                   code, msg]];
        }
      } else {
        __strong typeof(weakSelf) self = weakSelf;
        [self.bottomView setMicBtnSelected:true];
        if (showToast) {
          [NEKaraokeToast showToast:NELocalizedString(@"麦克风已关闭")];
        }
      }
    });
  }];
}

- (void)unmuteAudio:(BOOL)showToast {
  __weak typeof(self) weakSelf = self;
  [NEKaraokeKit.shared unmuteMyAudio:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if (code != 0) {
        [NEKaraokeToast
            showToast:[NSString
                          stringWithFormat:@"%@: %@", NELocalizedString(@"麦克风打开失败"), msg]];
      } else {
        __strong typeof(weakSelf) self = weakSelf;
        [self.bottomView setMicBtnSelected:false];
        if (showToast) {
          [NEKaraokeToast showToast:NELocalizedString(@"麦克风已打开")];
        }
      }
    });
  }];
}

- (NSString *)fetchLyricContentWithSongId:(NSString *)songId channel:(SongChannel)channel {
  return [[NEKaraokeKit shared] getLyric:songId channel:channel];
}
- (NSString *)fetchPitchContentWithSongId:(NSString *)songId channel:(SongChannel)channel {
  return [[NEKaraokeKit shared] getPitch:songId channel:channel];
}
- (NSString *)fetchOriginalFilePathWithSongId:(NSString *)songId channel:(SongChannel)channel {
  return [[NEKaraokeKit shared] getSongURI:songId channel:channel songResType:TYPE_ORIGIN];
}
- (NSString *)fetchAccompanyFilePathWithSongId:(NSString *)songId channel:(SongChannel)channel {
  return [[NEKaraokeKit shared] getSongURI:songId channel:channel songResType:TYPE_ACCOMP];
}

@end
