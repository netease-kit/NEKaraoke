// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeLyricView.h"
#import <BlocksKit/BlocksKit+UIKit.h>
#import <BlocksKit/BlocksKit.h>
#import <Lottie/LOTAnimationView.h>
#import <Masonry/Masonry.h>
#import <libextobjc/extobjc.h>
#import "NEKaraokeSongLog.h"
#import "UIColor+Karaoke.h"
@interface NEKaraokeLyricView () <CompoentViewDelegate>

@property(nonatomic, strong) LOTAnimationView *waitSeekView;
@property(nonatomic, strong) NELyricView *lyricView;
@property(nonatomic, strong) NELyric *model;
@property(nonatomic, strong) UILabel *timeLabel;
@property(nonatomic, strong) UIButton *seekBtn;
@property(nonatomic, strong) NEPitchRecordComponentView *compoentView;
@property(nonatomic, assign) bool recorStart;

@end

@implementation NEKaraokeLyricView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.lyricView];
  [self.lyricView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(15);
    make.right.equalTo(self).offset(-15);
    make.top.equalTo(self).offset(27);
    make.bottom.equalTo(self).offset(-22);
  }];
  [self addSubview:self.timeLabel];
  [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.width.mas_equalTo(80);
    make.height.mas_equalTo(14);
    make.right.equalTo(self).offset(-14);
    make.bottom.equalTo(self).offset(-8);
  }];
  UIView *view = [UIView new];
  view.backgroundColor = [UIColor karaoke_colorWithHex:0x4BF4FF];
  view.layer.cornerRadius = 2.5;
  [self addSubview:view];
  [view mas_makeConstraints:^(MASConstraintMaker *make) {
    make.width.height.mas_equalTo(5);
    make.centerY.equalTo(self.timeLabel);
    make.right.equalTo(self.timeLabel.mas_left).offset(-4);
  }];
  [self addSubview:self.waitSeekView];
  @weakify(self)[self.waitSeekView mas_makeConstraints:^(MASConstraintMaker *make) {
    @strongify(self) make.centerX.equalTo(self);
    CGFloat seekWaitViewWidth = 110.f * 0.6;
    CGFloat seekWaitViewHeight = 25.f * 0.6;
    make.size.mas_equalTo(CGSizeMake(seekWaitViewWidth, seekWaitViewHeight));
    make.top.equalTo(self).offset(11);
  }];

  [self addSubview:self.compoentView];
  [self.compoentView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(15);
    make.right.equalTo(self).offset(-15);
    make.top.equalTo(self).offset(27);
    make.bottom.equalTo(self).offset(-22);
  }];

  [self addSubview:self.seekBtn];
  [self.seekBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(4);
    make.top.equalTo(self).offset(64);
    make.width.mas_equalTo(95);
    make.height.mas_equalTo(22);
  }];

  [self layoutIfNeeded];
}

- (void)setContent:(NSString *)content lyricType:(NELyricType)type {
  _content = content;
  self.model = [[NELyric alloc] initWithContent:content andType:type];
  @weakify(self)[self
      preloadLyricCompleteBlock:^{
        @strongify(self)[self.lyricView loadWithLyricModel:self.model];
      }
                      lyricList:(NSArray<NELyricLine *> *)self.model.lineModels];
}

- (void)setPath:(NSString *)path lyricType:(NELyricType)type {
  self.path = path;
  NSString *content = [[NSString alloc] initWithContentsOfFile:path
                                                      encoding:NSUTF8StringEncoding
                                                         error:nil];
  self.model = [[NELyric alloc] initWithContent:content andType:type];
  @weakify(self)[self
      preloadLyricCompleteBlock:^{
        @strongify(self)[self.lyricView loadWithLyricModel:self.model];
      }
                      lyricList:(NSArray<NELyricLine *> *)self.model.lineModels];
}

- (void)update {
  dispatch_async(dispatch_get_main_queue(), ^{
    //    [self.lyricView update];
    NSInteger milSecond = self.timeForCurrent();
    self.timeLabel.text = [NSString stringWithFormat:@"%@/%@", [self formatSeconds:milSecond],
                                                     [self formatSeconds:self.duration]];

    NELyricLine *line = self.model.lineModels.firstObject;
    NSInteger startTime = line.startTime;
    if (line.words.count) {
      startTime = line.words.firstObject.startTime;
    }
    [self showOrHiddenWaitSeekView:startTime - self.timeForCurrent()];
    [self showOrHiddenSeekBtn:startTime - self.timeForCurrent()];
  });
}

- (void)showPitch:(BOOL)show {
  self.lyricView.hidden = show;
  self.compoentView.hidden = !show;
  [NEKaraokeSongLog
      infoLog:karaokeSongLog
         desc:[NSString stringWithFormat:@"打分页面展示或隐藏 --- %d", self.compoentView.hidden]];
}

- (void)setPitchContent:(NSString *_Nonnull)pitchContent
              separator:(NSString *)separator
           lyricContent:(NSString *)lyricContent
              lyricType:(NELyricType)lyricType {
  //    [NEKaraokeSongLog infoLog:karaokeSongLog desc:[NSString
  //    stringWithFormat:@"打分数据初始化--- pitchContent - %@ \n lyricContent -
  //    %@",pitchContent,lyricContent]];
  self.model = [[NELyric alloc] initWithContent:lyricContent andType:lyricType];
  NSBundle *karaokeUIBundle = [NSBundle
      bundleWithPath:[[NSBundle mainBundle]
                         pathForResource:@"Frameworks/NEKaraokeUIKit.framework/NEKaraokeUIKit"
                                  ofType:@"bundle"]];
  [self.compoentView
      loadRecordDataWithPitchContent:pitchContent
                           separator:separator
                           startTime:nil
                             endTime:nil
                          LocalLyric:lyricContent
                             andType:lyricType
                             builder:^(NEPitchLayoutBuilder *_Nonnull builder) {
                               builder.emitterPathArray = @[
                                 [karaokeUIBundle pathForResource:@"pop_1.png" ofType:nil],
                                 [karaokeUIBundle pathForResource:@"pop_2.png" ofType:nil],
                                 [karaokeUIBundle pathForResource:@"pop_3.png" ofType:nil],
                               ];
                               builder.finalScorePathArray = @[
                                 [karaokeUIBundle pathForResource:@"score-s.webp" ofType:nil],
                                 [karaokeUIBundle pathForResource:@"score-ss.webp" ofType:nil],
                                 [karaokeUIBundle pathForResource:@"score-sss.webp" ofType:nil],
                               ];

                               builder.perfectScorePathArray = @[
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_0.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_1.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_2.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_3.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_4.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_5.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_6.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_7.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_8.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_9.png"
                                                           ofType:nil],
                                 [karaokeUIBundle pathForResource:@"rcd_sing_score_pic_x.png"
                                                           ofType:nil],

                               ];
                             }];

  [self.compoentView recorStart];
  _recorStart = YES;
}

//是否有打分数据
- (BOOL)hasPitchConotent {
  return [self.compoentView hasPitchConotent];
}
- (void)showFinalScoreView:(NEPitchPlayResultModel *)playResultModel andLevel:(NEOpusLevel)level {
  [self.compoentView showScoreViewWithUserData:playResultModel songLevel:level];
}

- (void)hideScoreView {
  [self.compoentView hideScoreView];
}
- (void)pushAudioFrameWithFrame:(NEKaraokeAudioFrame *)frame isEnd:(BOOL)isEnd {
  if (_recorStart) {
    if (isEnd) {
      _recorStart = NO;
    }
    NEPitchAudioData *audioData = [[NEPitchAudioData alloc] init];
    audioData.done = isEnd;
    void *data = frame.data;
    audioData.samples = &data;
    audioData.validSampleCount = frame.format.samplesPerChannel;
    audioData.timeStamp = (int32_t)self.timeForCurrent() - 10;
    audioData.sampleRate = frame.format.sampleRate;
    audioData.channelCount = frame.format.channels;
    [self.compoentView pushAudioData:audioData];
  }
}

//暂停打分
- (void)pitchPause {
  [self.compoentView pitchPause];
}

//开始打分：初始化自动启动，中间暂停才需要调用
- (void)pitchStart {
  [self.compoentView pitchStart];
}

//销毁打分
- (void)pitchDestroy {
  [self.compoentView pitchDestroy];
}

#pragma mark - private

- (void)showOrHiddenWaitSeekView:(NSInteger)leftTime {
  if (leftTime > 0 && leftTime <= 5000) {
    self.waitSeekView.alpha = 1;
    self.waitSeekView.animationProgress = (5000 - leftTime) / 5000.f;
  } else {
    self.waitSeekView.alpha = 0;
  }
}

- (void)showOrHiddenSeekBtn:(NSInteger)leftTime {
  if (_seekBtnHidden) {
    self.seekBtn.hidden = true;
    return;
  }
  if (leftTime > 10 * 1000) {
    self.seekBtn.hidden = false;
    [self.seekBtn setTitle:[NSString stringWithFormat:@"跳过前奏(%zds)", leftTime / 1000]
                  forState:UIControlStateNormal];
  } else {
    self.seekBtn.hidden = true;
  }
}

- (NSString *)formatSeconds:(NSInteger)milSeconds {
  long seconds = milSeconds / 1000;
  NSString *str_minute = [NSString stringWithFormat:@"%02ld", (seconds % 3600) / 60];
  NSString *str_second = [NSString stringWithFormat:@"%02ld", seconds % 60];
  return [NSString stringWithFormat:@"%@:%@", str_minute, str_second];
}

- (void)preloadLyricCompleteBlock:(void (^)(void))completeBlock
                        lyricList:(NSArray<NELyricLine *> *)lyricList {
  dispatch_group_t group = dispatch_group_create();
  dispatch_queue_global_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);

  [lyricList bk_each:^(NELyricLine *obj) {
    @weakify(obj) obj.layoutInfo.lyricUpdateHandler = ^(YYLabel *_Nonnull label,
                                                        NSString *_Nonnull lyricText) {
      if (lyricText.length > 0) {
        @strongify(obj) label.numberOfLines = 0;
        if (!obj.layoutInfo.lyricTextAttrStr) {
          NSMutableParagraphStyle *style = [NSMutableParagraphStyle new];
          style.minimumLineHeight = 28;
          style.maximumLineHeight = 28;
          style.alignment = NSTextAlignmentCenter;
          NSMutableAttributedString *attr = [[NSMutableAttributedString alloc]
              initWithString:lyricText
                  attributes:@{
                    NSParagraphStyleAttributeName : style,
                    NSFontAttributeName : [UIFont systemFontOfSize:23 weight:UIFontWeightMedium],
                    NSForegroundColorAttributeName : [UIColor whiteColor]
                  }];

          obj.layoutInfo.lyricTextAttrStr = attr;
        }
        label.attributedText = obj.layoutInfo.lyricTextAttrStr;
      }
    };

    obj.layoutInfo.sizeLyricUpdateHandler =
        ^(YYLabel *_Nonnull label, NSString *_Nonnull lyricText) {
          if (lyricText.length > 0) {
            label.textColor = [UIColor blackColor];
            label.numberOfLines = 0;
            NSMutableParagraphStyle *style = [NSMutableParagraphStyle new];
            style.minimumLineHeight = 28;
            style.maximumLineHeight = 28;
            style.alignment = NSTextAlignmentCenter;
            NSAttributedString *attr = [[NSAttributedString alloc]
                initWithString:lyricText
                    attributes:@{
                      NSParagraphStyleAttributeName : style,
                      NSFontAttributeName : [UIFont systemFontOfSize:23 weight:UIFontWeightMedium]
                    }];
            label.attributedText = attr;
          }
        };

    CGFloat w = self.bounds.size.width - 20 * 2 - 2 * NELyricViewCellPadding(NELyricViewTypeLyric);
    dispatch_group_enter(group);
    dispatch_group_async(group, queue, ^{
      if (obj.words.count) {
        [obj preLayoutWithMaxWidth:w
                          complete:^(CGSize size) {
                            dispatch_group_leave(group);
                          }];
      } else {
        [obj asynLayoutSizeWithMaxSize:CGSizeMake(w, CGFLOAT_MAX)
                             sizeBlock:^(CGSize size) {
                               dispatch_group_leave(group);
                             }];
      }
    });
  }];

  dispatch_group_notify(group, queue, ^{
    dispatch_async(dispatch_get_main_queue(), ^{
      if (completeBlock) {
        completeBlock();
      }
    });
  });
}

#pragma mark - getter

- (NELyricView *)lyricView {
  if (!_lyricView) {
    _lyricView = [[NELyricView alloc] initWithFrame:self.frame type:NELyricViewTypeLyric];
    _lyricView.updateType = NELyricUpdateTypeLine;
    _lyricView.scrollEnabled = NO;
    @weakify(self) _lyricView.timeForCurrent = ^NSInteger {
      @strongify(self) return self.timeForCurrent();
    };
  }
  return _lyricView;
}

- (UILabel *)timeLabel {
  if (!_timeLabel) {
    _timeLabel = [UILabel new];
    _timeLabel.textColor = [UIColor colorWithWhite:1 alpha:0.5];
    _timeLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
  }
  return _timeLabel;
}

- (LOTAnimationView *)waitSeekView {
  if (!_waitSeekView) {
    NSString *path =
        [[NSBundle mainBundle] pathForResource:@"Frameworks/NEKaraokeUIKit.framework/NEKaraokeUIKit"
                                        ofType:@"bundle"];
    _waitSeekView = [LOTAnimationView animationNamed:@"self_wait_data"
                                            inBundle:[NSBundle bundleWithPath:path]];
    _waitSeekView.alpha = 0;
  }
  return _waitSeekView;
}

- (NEPitchRecordComponentView *)compoentView {
  if (!_compoentView) {
    _compoentView = [[NEPitchRecordComponentView alloc] initWithFrame:self.frame];
    _compoentView.delegate = self;
  }
  return _compoentView;
}

- (UIButton *)seekBtn {
  if (!_seekBtn) {
    _seekBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_seekBtn setTitle:@"跳过前奏(18s)" forState:UIControlStateNormal];
    _seekBtn.layer.cornerRadius = 11;
    _seekBtn.clipsToBounds = true;
    _seekBtn.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
    _seekBtn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    _seekBtn.hidden = true;
    @weakify(self)[_seekBtn bk_whenTapped:^{
      @strongify(self) if (self.model.lineModels.count > 0) {
        NELyricLine *line = self.model.lineModels.firstObject;
        NSInteger startTime = line.startTime;
        if (line.words.count) {
          startTime = line.words.firstObject.startTime;
        }
        self.seek(startTime - 5 * 1000);
        if (self.compoentView && !self.compoentView.hidden) {
          [self.compoentView seekTime:(startTime - 5 * 1000)];
          [self performSelector:@selector(delayMethod) withObject:nil afterDelay:0.1f];
        }
      }
    }];
  }
  return _seekBtn;
}

#pragma mark CompoentViewDelegate

- (NSInteger)timeForCompoentView:(NEPitchRecordComponentView *)compoentView {
  return self.timeForCurrent();
}

- (void)delayMethod {
  [self.compoentView resetPitch];
}
@end
