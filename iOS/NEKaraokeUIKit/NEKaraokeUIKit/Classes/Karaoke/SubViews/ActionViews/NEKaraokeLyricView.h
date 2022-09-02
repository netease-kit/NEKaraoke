// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NELyricUIKit/NELyricUIKit.h>
#import <NEPitchUIKit/NEPitchUIKit.h>
#import <UIKit/UIKit.h>
@import NEKaraokeKit;
NS_ASSUME_NONNULL_BEGIN

@interface NEKaraokeLyricView : UIView

@property(nonatomic, copy) NSString *path;
@property(nonatomic, copy) NSString *content;
@property(nonatomic, assign) NSInteger duration;
@property(nonatomic, assign) bool seekBtnHidden;
@property(nonatomic, copy) NSInteger (^timeForCurrent)(void);
@property(nonatomic, copy) void (^seek)(NSInteger);

- (void)update;

- (void)showPitch:(BOOL)show;

- (void)setPitchContent:(NSString *_Nonnull)pitchContent
              separator:(NSString *)separator
           lyricContent:(NSString *)lyricContent
              lyricType:(NELyricType)lyricType;

//数据推送
//最后一条数据isEnd 为YES
- (void)pushAudioFrameWithFrame:(NEKaraokeAudioFrame *)frame isEnd:(BOOL)isEnd;

//是否有打分数据
- (BOOL)hasPitchConotent;
//展示最终分数
- (void)showFinalScoreView:(NEPitchPlayResultModel *)playResultModel andLevel:(NEOpusLevel)level;
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
