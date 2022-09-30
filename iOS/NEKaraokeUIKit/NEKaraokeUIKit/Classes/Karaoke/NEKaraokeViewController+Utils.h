// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeViewController.h"
NS_ASSUME_NONNULL_BEGIN

@interface NEKaraokeViewController (Utils)
/// 自己是否是主唱
- (BOOL)isAnchorWithSelf;
/// 自己是否是副唱
- (BOOL)isChoristerWithSelf;
/// 聊天室消息songModel 转 点歌台OrderSong
- (NEKaraokeOrderSongModel *)covertToOrderSong:(NEKaraokeSongModel *)model;
/// 当前的模式
- (NEKaraokeSongMode)fetchCurrentSongMode;
/// 自动打开麦克
- (void)autoUnmuteAudio;

/// 打开关闭麦克风，封装了与按钮的状态同步
- (void)muteAudio:(BOOL)showToast;
- (void)unmuteAudio:(BOOL)showToast;

/// 通过songId获取歌词内容
- (NSString *)fetchLyricContentWithSongId:(NSString *)songId channel:(SongChannel)channel;

/// 通过songId获取打分内容
- (NSString *)fetchPitchContentWithSongId:(NSString *)songId channel:(SongChannel)channel;

/// 通过songId获取原唱地址
- (NSString *)fetchOriginalFilePathWithSongId:(NSString *)songId channel:(SongChannel)channel;

/// 通过songId获取伴奏地址
- (NSString *)fetchAccompanyFilePathWithSongId:(NSString *)songId channel:(SongChannel)channel;

@end

NS_ASSUME_NONNULL_END
