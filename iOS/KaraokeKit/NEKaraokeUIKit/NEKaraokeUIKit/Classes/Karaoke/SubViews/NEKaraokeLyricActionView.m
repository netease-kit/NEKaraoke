// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeLyricActionView.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeChooseView.h"
#import "NEKaraokeChorusWaitView.h"
#import "NEKaraokeLyricView.h"
#import "NEKaraokeMatchView.h"
#import "NEKaraokeNoLyricView.h"
#import "NEKaraokeWaitView.h"

@interface NEKaraokeLyricActionView ()

@property(nonatomic, strong) NEKaraokeChooseView *chooseView;
@property(nonatomic, strong) NEKaraokeWaitView *waitView;
@property(nonatomic, strong) NEKaraokeMatchView *matchView;
@property(nonatomic, strong) NEKaraokeChorusWaitView *chorusWaitView;
@property(nonatomic, strong) NEKaraokeLyricView *lyricView;
@property(nonatomic, strong) NEKaraokeNoLyricView *noLyricView;

@end

@implementation NEKaraokeLyricActionView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  self.backgroundColor = [UIColor clearColor];
  self.layer.cornerRadius = 12;
  self.clipsToBounds = true;

  UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleDark];
  UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:effect];
  effectView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
  [self addSubview:effectView];
  [effectView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.chooseView];
  [self.chooseView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.waitView];
  [self.waitView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.matchView];
  [self.matchView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.chorusWaitView];
  [self.chorusWaitView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.lyricView];
  [self.lyricView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];

  [self addSubview:self.noLyricView];
  [self.noLyricView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self);
  }];
}

- (void)showSubview:(NEKaraokeLyricActionSubviewType)type {
  self.chooseView.hidden = true;
  self.waitView.hidden = true;
  self.chorusWaitView.hidden = true;
  self.matchView.hidden = true;
  self.lyricView.hidden = true;
  self.noLyricView.hidden = true;
  switch (type) {
    case NEKaraokeLyricActionSubviewTypeChooseSong:
      self.chooseView.hidden = false;
      break;
    case NEKaraokeLyricActionSubviewTypeWait:
      self.waitView.hidden = false;
      break;
    case NEKaraokeLyricActionSubviewTypeChorusWait:
      self.chorusWaitView.hidden = false;
      break;
    case NEKaraokeLyricActionSubviewTypeMatch:
      self.matchView.hidden = false;
      break;
    case NEKaraokeLyricActionSubviewTypeLyric:
      self.lyricView.hidden = false;
      [self.lyricView showPitch:false];
      break;
    case NEKaraokeLyricActionSubviewTypePitch:
      self.lyricView.hidden = false;
      [self.lyricView showPitch:true];
      break;
    case NEKaraokeLyricActionSubviewTypeNoLyric:
      self.noLyricView.hidden = false;
      break;
    default:
      break;
  }
}

#pragma mark - choose view

- (NEKaraokeChooseView *)chooseView {
  if (!_chooseView) {
    _chooseView = [[NEKaraokeChooseView alloc] init];
    __weak typeof(self) weakSelf = self;
    _chooseView.chooseSong = ^{
      __strong typeof(weakSelf) self = weakSelf;
      if ([self.delegate respondsToSelector:@selector(onLyricAction:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onLyricAction:NEKaraokeLyricActionTypeChooseSong];
        });
      }
    };
  }
  return _chooseView;
}

#pragma mark - wait view

- (NEKaraokeWaitView *)waitView {
  if (!_waitView) {
    _waitView = [[NEKaraokeWaitView alloc] init];
  }
  return _waitView;
}

- (void)setWaitSongName:(NSString *)waitSongName {
  _waitSongName = waitSongName;
  self.waitView.songName = waitSongName;
}

- (void)setWaitUserName:(NSString *)waitUserName {
  _waitUserName = waitUserName;
  self.waitView.userName = waitUserName;
}

- (void)setWaitUserIcon:(NSString *)waitUserIcon {
  _waitUserIcon = waitUserIcon;
  self.waitView.userIcon = waitUserIcon;
}

- (void)updateWaitTime:(NSInteger)time {
  [self.waitView updateTime:time];
}

#pragma mark - match view

- (NEKaraokeMatchView *)matchView {
  if (!_matchView) {
    _matchView = [[NEKaraokeMatchView alloc] init];
    __weak typeof(self) weakSelf = self;
    _matchView.toSolo = ^{
      __strong typeof(weakSelf) self = weakSelf;
      if ([self.delegate respondsToSelector:@selector(onLyricAction:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onLyricAction:NEKaraokeLyricActionTypeToSolo];
        });
      }
    };
    _matchView.join = ^{
      __strong typeof(weakSelf) self = weakSelf;
      if ([self.delegate respondsToSelector:@selector(onLyricAction:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.delegate onLyricAction:NEKaraokeLyricActionTypeJoinChorus];
        });
      }
    };
  }
  return _matchView;
}

- (void)setMatchSongName:(NSString *)matchSongName {
  _matchSongName = matchSongName;
  self.matchView.songName = matchSongName;
}

- (void)setMatchUserIcon:(NSString *)matchUserIcon {
  _matchUserIcon = matchUserIcon;
  self.matchView.userIcon = matchUserIcon;
}

- (void)updateMatchTime:(NSInteger)time {
  [self.matchView updateTime:time];
}

- (void)setSoloBtnHidden:(BOOL)hidden {
  [self.matchView setSoloBtnHidden:hidden];
}

- (void)setJoinBtnHidden:(BOOL)hidden {
  [self.matchView setJoinBtnHidden:hidden];
}

- (void)setMatchJoinBtnEnabled:(BOOL)enabled {
  self.matchView.joinEnable = enabled;
}

#pragma mark - chorus wait view

- (NEKaraokeChorusWaitView *)chorusWaitView {
  if (!_chorusWaitView) {
    _chorusWaitView = [[NEKaraokeChorusWaitView alloc] init];
  }
  return _chorusWaitView;
}

- (void)setChorusSongName:(NSString *)chorusSongName {
  _chorusSongName = chorusSongName;
  _chorusWaitView.songName = chorusSongName;
}

- (void)setChorusMainUserIcon:(NSString *)chorusMainUserIcon {
  _chorusMainUserIcon = chorusMainUserIcon;
  _chorusWaitView.mainUserIcon = chorusMainUserIcon;
}

- (void)setChorusMainUserName:(NSString *)chorusMainUserName {
  _chorusMainUserName = chorusMainUserName;
  _chorusWaitView.mainUserName = chorusMainUserName;
}

- (void)setChorusAttachUserIcon:(NSString *)chorusAttachUserIcon {
  _chorusAttachUserIcon = chorusAttachUserIcon;
  _chorusWaitView.attachUserIcon = chorusAttachUserIcon;
}

- (void)setChorusAttachUserName:(NSString *)chorusAttachUserName {
  _chorusMainUserName = chorusAttachUserName;
  _chorusWaitView.attachUserName = chorusAttachUserName;
}

- (void)accompanyLoading:(BOOL)isLoading {
  [_chorusWaitView accompanyLoading:isLoading];
}

#pragma mark - lyric view

- (NEKaraokeLyricView *)lyricView {
  if (!_lyricView) {
    _lyricView = [[NEKaraokeLyricView alloc] initWithFrame:self.frame];
    __weak typeof(self) weakSelf = self;
    _lyricView.timeForCurrent = ^NSInteger {
      __strong typeof(weakSelf) self = weakSelf;
      if ([self.delegate respondsToSelector:@selector(onLyricTime)]) {
        return [self.delegate onLyricTime];
      } else {
        return 0;
      }
    };
    _lyricView.seek = ^(NSInteger seek) {
      __strong typeof(weakSelf) self = weakSelf;
      if ([self.delegate respondsToSelector:@selector(onLyricSeek:)]) {
        [self.delegate onLyricSeek:seek];
      }
    };
  }
  return _lyricView;
}

- (void)setLyricPath:(NSString *)lyricPath {
  _lyricPath = lyricPath;
  self.lyricView.path = lyricPath;
}

- (void)setLyricContent:(NSString *)lyricContent lyricType:(NELyricType)type {
  self.lyricContent = lyricContent;
  [self.lyricView setContent:lyricContent lyricType:type];
}

- (void)setLyricDuration:(NSInteger)lyricDuration {
  _lyricDuration = lyricDuration;
  self.lyricView.duration = lyricDuration;
}

- (void)setLyricSeekBtnHidden:(bool)lyricSeekBtnHidden {
  _lyricSeekBtnHidden = lyricSeekBtnHidden;
  self.lyricView.seekBtnHidden = lyricSeekBtnHidden;
}

- (void)updateLyric {
  [self.lyricView update];
}

#pragma mark - no lyric view

- (NEKaraokeNoLyricView *)noLyricView {
  if (!_noLyricView) {
    _noLyricView = [[NEKaraokeNoLyricView alloc] initWithFrame:self.frame];
  }
  return _noLyricView;
}

- (void)setNoLyricSongName:(NSString *)noLyricSongName {
  _noLyricSongName = noLyricSongName;
  self.noLyricView.songName = noLyricSongName;
}

- (void)setNoLyricUserIcon:(NSString *)noLyricUserIcon {
  _noLyricUserIcon = noLyricUserIcon;
  self.noLyricView.userIcon = noLyricUserIcon;
}

#pragma mark - pitchView
- (void)setPitchContent:(NSString *)pitchContent
              separator:(NSString *)separator
           lyricContent:(NSString *)lyricContent
              lyricType:(NELyricType)lyricType {
  [self.lyricView setPitchContent:pitchContent
                        separator:separator
                     lyricContent:lyricContent
                        lyricType:lyricType];
}

// 数据推送
// 最后一条数据isEnd 为YES
- (void)pushAudioFrameWithFrame:(NEKaraokeAudioFrame *)frame isEnd:(BOOL)isEnd {
  [self.lyricView pushAudioFrameWithFrame:frame isEnd:isEnd];
}
// 是否有打分数据
- (BOOL)hasPitchConotent {
  return [self.lyricView hasPitchConotent];
}
// 展示最终分数
- (void)lyricActionViewLevel:(NEOpusLevel)level
                 resultModel:(NEPitchPlayResultModel *)playResultModel {
  [self.lyricView showFinalScoreView:playResultModel andLevel:level];
}

- (void)hideScoreView {
  [self.lyricView hideScoreView];
}

// 暂停打分
- (void)pitchPause {
  [self.lyricView pitchPause];
}

// 开始打分：初始化自动启动，中间暂停才需要调用
- (void)pitchStart {
  [self.lyricView pitchStart];
}

// 销毁打分
- (void)pitchDestroy {
  [self.lyricView pitchDestroy];
}
@end
