// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NELyricUIKit/NELyricUIKit.h>
#import <NEPitchUIKit/NEPitchUIKit.h>
#import <UIKit/UIKit.h>

@import NEKaraokeKit;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, NEKaraokeLyricActionType) {
  // 点歌
  NEKaraokeLyricActionTypeChooseSong,
  // 切换到独唱
  NEKaraokeLyricActionTypeToSolo,
  // 加入合唱
  NEKaraokeLyricActionTypeJoinChorus,
};

typedef NS_ENUM(NSInteger, NEKaraokeLyricActionSubviewType) {
  // 点歌页面
  NEKaraokeLyricActionSubviewTypeChooseSong,
  // 独唱等待页面
  NEKaraokeLyricActionSubviewTypeWait,
  // 合唱匹配页面
  NEKaraokeLyricActionSubviewTypeMatch,
  // 合唱等待页面
  NEKaraokeLyricActionSubviewTypeChorusWait,
  // 歌词页面
  NEKaraokeLyricActionSubviewTypeLyric,
  // 打分页面
  NEKaraokeLyricActionSubviewTypePitch,
  // 无歌词界面
  NEKaraokeLyricActionSubviewTypeNoLyric,
};

@protocol NEKaraokeLyricActionViewDelegate <NSObject>

- (void)onLyricAction:(NEKaraokeLyricActionType)action;

- (NSInteger)onLyricTime;

- (void)onLyricSeek:(NSInteger)seek;

@end

@interface NEKaraokeLyricActionView : UIView

@property(nonatomic, weak) id<NEKaraokeLyricActionViewDelegate> delegate;

- (void)showSubview:(NEKaraokeLyricActionSubviewType)type;

#pragma mark - 独唱等待页面属性
@property(nonatomic, copy) NSString *waitSongName;
@property(nonatomic, copy) NSString *waitUserName;
@property(nonatomic, strong) UIImage *waitUserIcon;

- (void)updateWaitTime:(NSInteger)time;

#pragma mark - 合唱匹配页面
@property(nonatomic, strong) UIImage *matchUserIcon;
@property(nonatomic, copy) NSString *matchSongName;

- (void)updateMatchTime:(NSInteger)time;
- (void)setSoloBtnHidden:(BOOL)hidden;
- (void)setJoinBtnHidden:(BOOL)hidden;
- (void)setMatchJoinBtnEnabled:(BOOL)enabled;

#pragma mark - 合唱等待页面
@property(nonatomic, copy) NSString *chorusSongName;
@property(nonatomic, copy) NSString *chorusMainUserName;
@property(nonatomic, strong) UIImage *chorusMainUserIcon;
@property(nonatomic, copy) NSString *chorusAttachUserName;
@property(nonatomic, strong) UIImage *chorusAttachUserIcon;

- (void)accompanyLoading:(BOOL)isLoading;

#pragma mark - 歌词页面
@property(nonatomic, copy) NSString *lyricPath;
@property(nonatomic, copy) NSString *lyricContent;
@property(nonatomic, assign) NSInteger lyricDuration;
@property(nonatomic, assign) bool lyricSeekBtnHidden;
- (void)updateLyric;

#pragma mark - 无歌词页面
@property(nonatomic, copy) NSString *noLyricSongName;
@property(nonatomic, strong) UIImage *noLyricUserIcon;

#pragma mark - 打分页面

@property(nonatomic, strong) NSString *UserIconUrl;

//初始化
- (void)setPitchContent:(NSString *)pitchContent
              separator:(NSString *)separator
           lyricContent:(NSString *)lyricContent
              lyricType:(NELyricType)lyricType;

//数据推送
//最后一条数据isEnd 为YES
- (void)pushAudioFrameWithFrame:(NEKaraokeAudioFrame *)frame isEnd:(BOOL)isEnd;
//是否有打分数据
- (BOOL)hasPitchConotent;
//展示最终分数
- (void)lyricActionViewLevel:(NEOpusLevel)level
                 resultModel:(NEPitchPlayResultModel *)playResultModel;

//隐藏最终分数
- (void)hideScoreView;

//暂停打分
- (void)pitchPause;

//开始打分：初始化自动启动，中间暂停才需要调用
- (void)pitchStart;

//销毁打分
- (void)pitchDestroy;
@end

NS_ASSUME_NONNULL_END
