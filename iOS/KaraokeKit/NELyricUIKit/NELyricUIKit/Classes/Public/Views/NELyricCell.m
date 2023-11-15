// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELyricCell.h"
#import <BlocksKit/BlocksKit.h>
#import <BlocksKit/NSTimer+BlocksKit.h>
#import <Masonry/Masonry.h>
#import <NELyricUIKit/NEGradientView.h>
#import "NELyricLine+Layout.h"

#define NELyricCellUpdateLabel(key, value) \
  self.lyricBackgroundLabel.key = value;   \
  self.lyricForegroundLabel.key = value;   \
  self.lyricShadowBackgroundLabel.key = value;

#define NELyricCellCallLabelFuc(func) \
  [self.lyricBackgroundLabel func];   \
  [self.lyricForegroundLabel func];   \
  [self.lyricShadowBackgroundLabel func];

#define UIColorFromRGBA(rgbValue, alphaValue)                          \
  [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0 \
                  green:((float)((rgbValue & 0x00FF00) >> 8)) / 255.0  \
                   blue:((float)(rgbValue & 0x0000FF)) / 255.0         \
                  alpha:alphaValue]

@interface YYLabel ()
- (CGRect)_convertRectFromLayout:(CGRect)rect;
@end

@interface NELyricCell ()

@property(nonatomic, strong, readwrite) NELyricLine *yrcLineModel;

@property(nonatomic, strong) YYLabel *lyricForegroundLabel;
@property(nonatomic, strong) YYLabel *lyricBackgroundLabel;
@property(nonatomic, strong) YYLabel *lyricShadowBackgroundLabel;
@property(nonatomic, strong) UIView *lyricShadowBackgroundLabelView;

@property(nonatomic, strong) UIView *foregroundView;
@property(nonatomic, strong) CAShapeLayer *backgroundLayer;

@property(nonatomic, strong) NSMutableArray<__kindof UIView *> *foregroundViews;

@property(nonatomic, assign) BOOL forceReload;

@property(nonatomic, strong) CADisplayLink *displayLink;

@property(nonatomic, assign) NELyricUpdateType type;

@end

@implementation NELyricCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.forceReload = NO;
    [self loadDeafult];

    [self addSubview:self.lyricShadowBackgroundLabelView];
    [self addSubview:self.lyricBackgroundLabel];
    [self addSubview:self.foregroundView];

    [self.lyricShadowBackgroundLabelView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self);
    }];

    [self.lyricBackgroundLabel mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self);
    }];

    [self.foregroundView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self);
    }];
  }
  return self;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  NELyricCellUpdateLabel(frame, CGRectMake(0, 0, self.frame.size.width, self.frame.size.height));
  CGRect lastFrame = [[self bk_associatedValueForKey:_cmd] CGRectValue];
  if ((!CGRectEqualToRect(CGRectMake(0, 0, lastFrame.size.width, lastFrame.size.height),
                          CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)) &&
       self.yrcLineModel) ||
      self.forceReload) {
    self.forceReload = NO;
    [self relayoutLyric];
    [self displayLinkPlay];
    [self bk_associateValue:[NSValue valueWithCGRect:self.frame] withKey:_cmd];
  }
}

#pragma mark - public
- (void)startAnimation {
  [self startDisplayLink];
}

- (void)pauseAnimation {
  [self stopDisplayLink];
}

- (void)resetAnimation {
  [self stopDisplayLink];
  [self yrcUpdateWithTime:0];
}

- (void)reloadWithYrcLineModel:(NELyricLine *)lineModel type:(NELyricUpdateType)type {
  if (self.yrcLineModel == lineModel) {
    return;
  }
  self.type = type;
  self.yrcLineModel = lineModel;
  NSString *str = lineModel.text;

  [self.foregroundViews bk_each:^(UIView *obj) {
    obj.frame = CGRectZero;
  }];

  [@[ self.lyricForegroundLabel, self.lyricBackgroundLabel, self.lyricShadowBackgroundLabel ]
      bk_each:^(YYLabel *obj) {
        if (lineModel.layoutInfo.lyricUpdateHandler) {
          lineModel.layoutInfo.lyricUpdateHandler(obj, str);
        }
      }];

  if (self.backgroundLabelUpdateHandler) {
    self.backgroundLabelUpdateHandler(self.lyricBackgroundLabel);
  }
  self.lyricShadowBackgroundLabelView.backgroundColor = [UIColor colorWithWhite:0 alpha:0.5];
  NSMutableAttributedString *attr = self.lyricShadowBackgroundLabel.attributedText.mutableCopy;
  if (!self.notNeedShadow) {
    attr.yy_color = [UIColor colorWithWhite:1 alpha:1];
    NSShadow *shadow = [[NSShadow alloc] init];
    [shadow setShadowColor:[UIColor colorWithWhite:0 alpha:1]];
    [shadow setShadowBlurRadius:2.0];
    [shadow setShadowOffset:CGSizeMake(0, 0)];
    attr.yy_shadow = shadow;
    self.lyricShadowBackgroundLabelView.alpha = 1;
  } else {
    self.lyricShadowBackgroundLabelView.alpha = 0;
  }
  self.lyricShadowBackgroundLabel.attributedText = attr;

  self.forceReload = YES;
  [self setNeedsLayout];
}

- (void)dealloc {
  [self stopDisplayLink];
}

#pragma mark - timer

- (void)startDisplayLink {
  [self stopDisplayLink];
  self.displayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(displayLinkPlay)];
  [self.displayLink addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
}

- (void)displayLinkPlay {
  NSInteger time = [self.delegate respondsToSelector:@selector(timeForYrcView:)]
                       ? [self.delegate timeForYrcView:self]
                       : 0;
  [self yrcUpdateWithTime:time];
}

- (void)stopDisplayLink {
  [self.displayLink invalidate];
  self.displayLink = nil;
}

#pragma mark - private

- (void)relayoutLyric {
  [self reUpdateView];
}

- (void)reUpdateView {
  UIView *presentView = self.foregroundView;
  NSMutableArray<UIView *> *views = self.foregroundViews;

  for (int i = 0; i < self.yrcLineModel.presentLines.count; i++) {
    UIView *maskView = nil;
    if (views.count > i) {
      maskView = views[i];
    } else {
      maskView = [[NEGradientView alloc] initWithFrame:self.frame];
      [presentView addSubview:maskView];
      [views addObject:maskView];
    }
    if (self.presentViewUpdateHandler) {
      self.presentViewUpdateHandler(maskView, i);
    }
  }
}

- (void)yrcUpdateWithTime:(NSInteger)time {
  UIBezierPath *path = [UIBezierPath bezierPath];
  if (time < self.yrcLineModel.startTime ||
      time >= self.yrcLineModel.startTime + self.yrcLineModel.interval) {
    if (time < self.yrcLineModel.startTime) {
      [self.foregroundViews
          enumerateObjectsUsingBlock:^(UIView *_Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
            obj.frame = CGRectZero;
          }];
      [path appendPath:[UIBezierPath bezierPathWithRect:CGRectMake(0, 0, self.frame.size.width,
                                                                   self.frame.size.height)]];
    } else {
      [self.foregroundViews
          enumerateObjectsUsingBlock:^(UIView *_Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
            if (idx < self.yrcLineModel.presentLines.count) {
              obj.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            } else {
              obj.frame = CGRectZero;
            }
          }];
      [path appendPath:[UIBezierPath bezierPathWithRect:CGRectMake(0, 0, 0, 0)]];
    }
    self.backgroundLayer.path = path.CGPath;
    self.lyricShadowBackgroundLabelView.hidden = NO;
    return;
  }
  self.lyricShadowBackgroundLabelView.hidden = NO;
  NELyricWord *word = [self.yrcLineModel wordAtTime:time];
  if (!word) {
    return;
  }
  double p =
      word.interval == 0 ? 1 : MIN(1, (MAX(0, time - word.startTime)) / (word.interval * 1.f));

  NELyricPresentLine *lineModel = [self.yrcLineModel presentLineForWord:word];
  if (!lineModel) {
    return;
  }
  NSInteger currentIdx = [self.yrcLineModel.presentLines indexOfObject:lineModel];
  NSInteger wordIdx = [lineModel.words indexOfObject:word];

  for (int i = 0; i < self.yrcLineModel.presentLines.count; i++) {
    UIView *currentView = self.foregroundViews.count > i ? self.foregroundViews[i] : nil;
    if (i < currentIdx) {
      currentView.frame = self.yrcLineModel.presentLines[i].lineFrame;
    } else if (currentIdx == i) {
      CGRect beforeWordRect = CGRectZero;
      if (wordIdx != 0) {
        NELyricWord *startWord = self.yrcLineModel.presentLines[i].words.firstObject;
        NELyricWord *endWord = self.yrcLineModel.presentLines[i].words[wordIdx - 1];
        beforeWordRect =
            CGRectMake(startWord.cacheFrame.origin.x, startWord.cacheFrame.origin.y,
                       CGRectGetMaxX(endWord.cacheFrame) - startWord.cacheFrame.origin.x,
                       CGRectGetHeight(word.cacheFrame));
      }
      CGRect wordRect = word.cacheFrame;
      wordRect.size.width = wordRect.size.width * p;
      currentView.frame = CGRectEqualToRect(beforeWordRect, CGRectZero)
                              ? wordRect
                              : CGRectUnion(beforeWordRect, wordRect);
    } else {
      currentView.frame = CGRectZero;
    }

    CGRect pathFrame = self.yrcLineModel.presentLines[i].lineFrame;
    if (!CGRectEqualToRect(currentView.frame, CGRectZero)) {
      pathFrame = CGRectMake(CGRectGetMaxX(currentView.frame), currentView.frame.origin.y,
                             CGRectGetMaxX(pathFrame) - CGRectGetMaxX(currentView.frame),
                             pathFrame.size.height);
    }
    [path appendPath:[UIBezierPath bezierPathWithRect:pathFrame]];
  }
  self.backgroundLayer.path = path.CGPath;
}

- (void)loadDeafult {
  self.presentViewUpdateHandler = ^(NEGradientView *_Nonnull view, NSInteger line) {
    view.direction = NEGradientViewDirectionHorizontal;
    view.locations = @[ @0.1, @0.5, @1.0 ];
    //        view.colors = @[UIColorFromRGBA(0x00BDFF, 1),
    //        UIColorFromRGBA(0x009EFF, 1), UIColorFromRGBA(0x925BFF, 1)];
    view.colors = @[
      UIColorFromRGBA(0x4BF4FF, 1), UIColorFromRGBA(0x4BF4FF, 1), UIColorFromRGBA(0x4BF4FF, 1)
    ];
  };

  self.backgroundLabelUpdateHandler = ^(YYLabel *_Nonnull label) {
    NSMutableAttributedString *attr = label.attributedText.mutableCopy;
    attr.yy_color = [UIColor whiteColor];
    label.attributedText = attr;
  };
}

#pragma mark - get & set

- (CAShapeLayer *)backgroundLayer {
  if (!_backgroundLayer) {
    _backgroundLayer = [CAShapeLayer new];
  }
  return _backgroundLayer;
}

- (UIView *)foregroundView {
  if (!_foregroundView) {
    _foregroundView = [UIView new];
    _foregroundView.layer.mask = self.lyricForegroundLabel.layer;
  }
  return _foregroundView;
}

- (NSMutableArray<__kindof UIView *> *)foregroundViews {
  if (!_foregroundViews) {
    _foregroundViews = [NSMutableArray new];
  }
  return _foregroundViews;
}

- (YYLabel *)lyricForegroundLabel {
  if (!_lyricForegroundLabel) {
    _lyricForegroundLabel = [YYLabel new];
  }
  return _lyricForegroundLabel;
}

- (YYLabel *)lyricBackgroundLabel {
  if (!_lyricBackgroundLabel) {
    _lyricBackgroundLabel = [YYLabel new];
    _lyricBackgroundLabel.layer.mask = self.backgroundLayer;
  }
  return _lyricBackgroundLabel;
}

- (YYLabel *)lyricShadowBackgroundLabel {
  if (!_lyricShadowBackgroundLabel) {
    _lyricShadowBackgroundLabel = [YYLabel new];
  }
  return _lyricShadowBackgroundLabel;
}

- (UIView *)lyricShadowBackgroundLabelView {
  if (!_lyricShadowBackgroundLabelView) {
    _lyricShadowBackgroundLabelView = [UIView new];
    _lyricShadowBackgroundLabelView.layer.mask = self.lyricShadowBackgroundLabel.layer;
    _lyricShadowBackgroundLabelView.hidden = YES;
  }
  return _lyricShadowBackgroundLabelView;
}
@end
