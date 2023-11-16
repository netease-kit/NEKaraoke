// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NELyricView.h"

#import <BlocksKit/BlocksKit.h>
#import <BlocksKit/UIControl+BlocksKit.h>
#import <Masonry/Masonry.h>
#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import <NELyricUIKit/NEGradientView.h>
#import <NELyricUIKit/NELyricCell.h>
#import <NELyricUIKit/NELyricLineLayout.h>
#import "NELyricLine+Layout.h"

#define UIColorFromRGBA(rgbValue, alphaValue)                          \
  [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0 \
                  green:((float)((rgbValue & 0x00FF00) >> 8)) / 255.0  \
                   blue:((float)(rgbValue & 0x0000FF)) / 255.0         \
                  alpha:alphaValue]

CGFloat NELyricViewCellPadding(NELyricViewType type) {
  if (type == NELyricViewTypeLyricPreview) {
    return 20;
  } else if (type == NELyricViewTypeChorus) {
    return 44;
  } else {
    return 24;
  }
};

CGFloat NELyricViewCellTop(NELyricViewType type) {
  if (type == NELyricViewTypeLyricPreview) {
    return 0;
  }
  return 0;
};

CGFloat NELyricViewCellBottom(NELyricViewType type) {
  if (type == NELyricViewTypeLyricPreview) {
    return 12;
  } else {
    return 6;
  }
};

CGFloat NELyricViewCellScale(NELyricViewType type) {
  if (type == NELyricViewTypeLyricPreview) {
    return 1;
  }
  return 0.85;
};

typedef enum : NSUInteger {
  NELyricViewCellAnimationTypeNone,
  NELyricViewCellAnimationTypeShow,
  NELyricViewCellAnimationTypeDismiss,
} NELyricViewCellAnimationType;

@interface NELyricViewCell : UITableViewCell <NELyricCellDelegate>

@property(nonatomic, assign) NELyricViewType type;

@property(nonatomic, copy) NSInteger (^block)(void);

//@property (nonatomic, strong) NKColorfulLabel *label;
//@property (nonatomic, strong) NKColorfulLabel *colorfulLabel;
@property(nonatomic, strong) UILabel *label;
@property(nonatomic, strong) UILabel *colorfulLabel;
@property(nonatomic, strong) NELyricCell *yrcView;
@property(nonatomic, strong) UIImageView *iconView;

@property(nonatomic, strong) NELyricLine *lineModel;
@property(nonatomic, assign) NELyricUpdateType updateType;

- (void)reloadWithYrcLineModel:(NELyricLine *)lineModel
                     animation:(NELyricViewCellAnimationType)type;

@end

@implementation NELyricViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style
              reuseIdentifier:(NSString *)reuseIdentifier
                   updateType:(NELyricUpdateType)updateType {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.contentView.backgroundColor = [UIColor clearColor];
    self.backgroundColor = [UIColor clearColor];

    [self.contentView addSubview:self.label];
    [self.contentView addSubview:self.colorfulLabel];
    [self.contentView addSubview:self.yrcView];

    [self.contentView addSubview:self.iconView];

    self.updateType = updateType;

    [self.yrcView startAnimation];
  }
  return self;
}

- (void)dealloc {
}

- (void)layoutSubviews {
  [super layoutSubviews];

  {
    self.lyricView.frame = CGRectMake(
        (self.contentView.frame.size.width -
         self.lineModel.layoutInfo.recordSingLyricCellLyricSize.width) /
            2.f,
        NELyricViewCellTop(self.type), self.lineModel.layoutInfo.recordSingLyricCellLyricSize.width,
        self.lineModel.layoutInfo.recordSingLyricCellLyricSize.height);
  }

  { [self reloadLabelFrame]; }
}

- (UIView *)lyricView {
  if (self.lineModel.words.count) {
    return self.yrcView;
  } else {
    return self.colorfulLabel;
  }
}

- (void)reloadLabelFrame {
  CGAffineTransform transform = self.label.transform;
  CGFloat top = NELyricViewCellTop(self.type);
  CGFloat w = self.lineModel.layoutInfo.recordSingLyricCellLyricSize.width + 10;
  CGFloat h = self.lineModel.layoutInfo.recordSingLyricCellLyricSize.height;
  if (!CGAffineTransformEqualToTransform(transform, CGAffineTransformIdentity)) {
    h = self.lineModel.layoutInfo.recordSingLyricCellLyricSize.height *
        NELyricViewCellScale(self.type);
    top = NELyricViewCellTop(self.type);
    w = w * NELyricViewCellScale(self.type);
  }

  CGFloat x = (self.contentView.frame.size.width - w) / 2.f;
  self.label.frame = CGRectMake(x, top, w, h);

  [self layoutIconView];
}

- (void)doLyricAnimationWithType:(NELyricViewCellAnimationType)type {
  if (type == NELyricViewCellAnimationTypeShow || type == NELyricViewCellAnimationTypeDismiss) {
    BOOL isBig = CGAffineTransformEqualToTransform(self.label.transform, CGAffineTransformIdentity);
    if (type == NELyricViewCellAnimationTypeShow) {
      if (!isBig) {
        self.label.transform = CGAffineTransformMakeScale(NELyricViewCellScale(self.type),
                                                          NELyricViewCellScale(self.type));
      }
      self.label.hidden = !(self.lineModel.layoutInfo.recordSingLyricCellIsShowBig &&
                            self.lineModel.layoutInfo.recordSingLyricCellIsSelect);
      self.lyricView.hidden = (self.lineModel.layoutInfo.recordSingLyricCellIsSelect &&
                               self.lineModel.layoutInfo.recordSingLyricCellIsShowBig);
    } else {
      if (!isBig) {
        self.label.transform = CGAffineTransformIdentity;
      }
      self.label.hidden = NO;
      self.lyricView.hidden = YES;
    }
    [UIView animateWithDuration:0.2
        animations:^{
          self.label.transform = self.lineModel.layoutInfo.recordSingLyricCellIsShowBig
                                     ? CGAffineTransformIdentity
                                     : CGAffineTransformMakeScale(NELyricViewCellScale(self.type),
                                                                  NELyricViewCellScale(self.type));
          [self reloadLabelFrame];
        }
        completion:^(BOOL finished) {
          self.label.hidden = self.lineModel.layoutInfo.recordSingLyricCellIsShowBig &&
                              self.lineModel.layoutInfo.recordSingLyricCellIsSelect;
          self.lyricView.hidden = !(self.lineModel.layoutInfo.recordSingLyricCellIsSelect &&
                                    self.lineModel.layoutInfo.recordSingLyricCellIsShowBig);
        }];
  } else {
    [CATransaction begin];
    [CATransaction setDisableActions:YES];

    self.label.transform = self.lineModel.layoutInfo.recordSingLyricCellIsShowBig
                               ? CGAffineTransformIdentity
                               : CGAffineTransformMakeScale(NELyricViewCellScale(self.type),
                                                            NELyricViewCellScale(self.type));
    [self reloadLabelFrame];
    self.label.hidden = self.lineModel.layoutInfo.recordSingLyricCellIsSelect &&
                        self.lineModel.layoutInfo.recordSingLyricCellIsShowBig;
    self.lyricView.hidden = !(self.lineModel.layoutInfo.recordSingLyricCellIsSelect &&
                              self.lineModel.layoutInfo.recordSingLyricCellIsShowBig);

    [CATransaction commit];
  }
}

#pragma mark - NELyricCellDelegate

- (NSInteger)timeForYrcView:(NELyricCell *)yrcView {
  return self.block ? self.block() : 0;
}

#pragma mark - private

- (UIColor *)bgLabelTextColor:(BOOL)selectColor {
  if (self.type == NELyricViewTypeLyric || self.type == NELyricViewTypeLyricOther ||
      self.type == NELyricViewTypeChorus) {
    return selectColor ? [UIColor whiteColor] : [UIColor colorWithWhite:1 alpha:0.45f];
  } else if (self.type == NELyricViewTypeLyricPreview) {
    return UIColorFromRGBA(0x000E1D, 0.85);
  } else {
    return selectColor ? [UIColor whiteColor] : [UIColor colorWithWhite:1 alpha:0.45f];
  }
}

- (void)showChorusIconWithLineModel:(NELyricLine *)lineModel {
  //    self.iconView.hidden = lineModel.isSingerOptionFirst ? NO : YES;
  //    if (lineModel.singerTag == NELyricModelSingerOptionA) {
  //        self.iconView.image = [UIImage
  //        imageNamed:@"kroom_chorus_lyrics_part_male"];
  //    } else if (lineModel.singerTag == NELyricModelSingerOptionB) {
  //        self.iconView.image = [UIImage
  //        imageNamed:@"kroom_chorus_lyrics_part_female"];
  //    } else {
  //        self.iconView.image = [UIImage
  //        imageNamed:@"kroom_chorus_lyrics_part_chorus"];
  //    }
  //    [self layoutIconView];
}

- (void)layoutIconView {
  if (self.lineModel.layoutInfo.recordSingLyricCellIsSelect &&
      self.lineModel.layoutInfo.recordSingLyricCellIsShowBig) {
    // 为当前走字句
    self.iconView.frame =
        CGRectMake(CGRectGetMinX(self.lyricView.frame) - CGRectGetWidth(self.iconView.frame) - 8,
                   CGRectGetMinY(self.lyricView.frame) + 3, 22, 22);
  } else {
    // 非当前走字句
    self.iconView.frame =
        CGRectMake(CGRectGetMinX(self.label.frame) - CGRectGetWidth(self.iconView.frame) - 8,
                   CGRectGetMinY(self.label.frame) + 1, 22, 22);
  }
}

#pragma mark - set & get

- (void)reloadWithYrcLineModel:(NELyricLine *)lineModel
                     animation:(NELyricViewCellAnimationType)type {
  if (self.type == NELyricViewTypeLyricPreview) {
    type = NELyricViewCellAnimationTypeNone;
  }
  if (self.lineModel != lineModel) {
    self.lineModel = lineModel;

    if (lineModel.words.count) {
      self.colorfulLabel.hidden = true;
      self.yrcView.hidden = false;
      [self.yrcView reloadWithYrcLineModel:lineModel type:self.updateType];
    } else {
      self.colorfulLabel.hidden = false;
      self.yrcView.hidden = true;
      self.colorfulLabel.text = lineModel.text;
    }
    self.label.text = lineModel.text;
    [self setNeedsLayout];
  }

  if (self.type == NELyricViewTypeChorus) {
    [self showChorusIconWithLineModel:lineModel];
  } else {
    self.iconView.hidden = YES;
  }

  [self doLyricAnimationWithType:type];
}

- (void)setType:(NELyricViewType)type {
  _type = type;
  self.yrcView.notNeedShadow = false;
  self.yrcView.notNeedShadow = type != NELyricViewTypeVideo;
  if (type == NELyricViewTypeVideo) {
    NSShadow *shadow = [NSShadow new];
    [shadow setShadowBlurRadius:2.0];
    [shadow setShadowOffset:CGSizeMake(0, 0)];
    //        self.label.shadow = shadow;
    //        self.colorfulLabel.shadow = shadow;
  } else {
    //        self.label.shadow = nil;
    //        self.colorfulLabel.shadow = nil;
  }
}

// 当前演唱底部歌词
- (UILabel *)label {
  if (!_label) {
    _label = [[UILabel alloc] initWithFrame:self.contentView.frame];
    _label.textAlignment = NSTextAlignmentCenter;
    if (!self.lineModel.words) {
      if (self.lineModel.layoutInfo.recordSingLyricCellIsSelect) {
        if (self.type == NELyricViewTypeLyricPreview) {
          _label.textColor = UIColorFromRGBA(0x000E1D, 0.64);
        } else {
          _label.textColor = UIColorFromRGBA(0x000E1D, 0.85);
        }
      } else {
        _label.textColor =
            [self bgLabelTextColor:self.lineModel.layoutInfo.recordSingLyricCellIsSelect];
      }
    } else {
      _label.textColor =
          [self bgLabelTextColor:self.lineModel.layoutInfo.recordSingLyricCellIsSelect];
    }
  }
  return _label;
}

// 逐行歌词 当前句
- (UILabel *)colorfulLabel {
  if (!_colorfulLabel) {
    _colorfulLabel = [[UILabel alloc] initWithFrame:self.contentView.frame];
    _colorfulLabel.textAlignment = NSTextAlignmentCenter;
    _colorfulLabel.numberOfLines = 0;
    if (self.type == NELyricViewTypeLyricPreview) {
      _colorfulLabel.textColor = UIColorFromRGBA(0x000E1D, 0.64);
    } else {
      _colorfulLabel.textColor = UIColorFromRGBA(0x4BF4FF, 1);
    }
  }
  return _colorfulLabel;
}

// 渐变歌词 当前句
- (NELyricCell *)yrcView {
  if (!_yrcView) {
    _yrcView = [NELyricCell new];
    _yrcView.hidden = YES;
  }
  _yrcView.delegate = self;
  return _yrcView;
}

// 合唱AB段标识
- (UIImageView *)iconView {
  if (!_iconView) {
    _iconView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 22, 22)];
  }
  return _iconView;
}

@end

@interface NELyricView () <UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate>

@property(nonatomic, strong) UITableView *tableView;
@property(nonatomic, strong, readwrite) NSArray<NELyricLine *> *lyricList;
@property(nonatomic, assign, readwrite) NSInteger lastSelect;

@property(nonatomic, strong) NELyric *lyricModel;
@property(nonatomic, assign) NSInteger startTime;
@property(nonatomic, assign) NSInteger endTime;

@property(nonatomic, strong) NEGradientView *bgGradientView;
@property(nonatomic, assign) NELyricViewType type;
@property(nonatomic, assign) BOOL shouldScroll;
@property(nonatomic, assign) NSInteger currentTime;

@end

@implementation NELyricView

- (instancetype)initWithFrame:(CGRect)frame type:(NELyricViewType)type {
  self = [super initWithFrame:frame];
  if (self) {
    self.type = type;
    self.shouldScroll = YES;
    self.scrollEnabled = YES;
    //        [self addSubview:self.bgGradientView];
    [self addSubview:self.tableView];
    self.lastSelect = -1;
    [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(self);
    }];

    //        self.layer.mask = self.bgGradientView.layer;
  }
  return self;
}

- (void)loadWithLyricModel:(NELyric *)model
                 startTime:(NSInteger)startTime
                   endTime:(NSInteger)endTime {
  self.lastSelect = -1;
  if (endTime > 0) {
    self.lyricList = [model linesAtTime:startTime toTime:endTime];
  } else {
    self.lyricList = model.lineModels;
  }
  self.lyricModel = model;
  self.startTime = startTime;
  self.endTime = endTime;

  [self update];
}

- (void)loadWithLyricModel:(NELyric *)model {
  [self loadWithLyricModel:model startTime:0 endTime:0];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  if (self.type != NELyricViewTypeLyricPreview) {
    self.tableView.tableFooterView.frame =
        CGRectMake(0, 0, self.frame.size.width, self.frame.size.height - [self lyricTopHeight]);
  } else {
    self.tableView.tableFooterView.frame = CGRectZero;
  }

  self.bgGradientView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);

  NSInteger line = self.lastSelect > 0 ? self.lastSelect : [self currentSelectIdx];
  [self updateMaskLayerWithLine:line];
}

- (CGFloat)lyricTopHeight {
  if (self.type == NELyricViewTypeLyric || self.type == NELyricViewTypeLyricOther ||
      self.type == NELyricViewTypeChorus) {
    return 34.f;
  } else if (self.type == NELyricViewTypeVideo) {
    return 36.f;
  } else {
    return 10 + self.insetHeight;
  }
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
  if (self.type == NELyricViewTypeLyricPreview) {
    [self updateMaskLayerWithLine:self.lastSelect];
  }
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
  if (self.shouldScroll) {
    self.shouldScroll = NO;
  }
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
  if (!decelerate && !self.shouldScroll) {
    self.shouldScroll = YES;
  }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
  if (!self.shouldScroll) {
    self.shouldScroll = YES;
  }
}

#pragma mark - UITableViewDelegate & UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  return 1;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return self.lyricList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  NELyricLine *itemModel =
      self.lyricList.count > indexPath.row ? self.lyricList[indexPath.row] : nil;

  NSString *cellId =
      [NSString stringWithFormat:@"NELyricViewCell_%@", NSStringFromClass([itemModel class])];

  NELyricViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
  if (!cell) {
    cell = [[NELyricViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                  reuseIdentifier:cellId
                                       updateType:self.updateType];
    __weak typeof(self) weakSelf = self;
    cell.block = ^NSInteger {
      [weakSelf update];
      return weakSelf.currentTime;
    };
  }

  cell.type = self.type;
  [cell reloadWithYrcLineModel:itemModel animation:NELyricViewCellAnimationTypeNone];

  return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  NELyricLine *itemModel =
      self.lyricList.count > indexPath.row ? self.lyricList[indexPath.row] : nil;
  CGFloat height = itemModel.layoutInfo.recordSingLyricCellLyricSize.height;
  if (itemModel.layoutInfo.recordSingLyricCellIsShowBig) {
    return height + NELyricViewCellTop(self.type) + NELyricViewCellBottom(self.type);
  } else {
    return height * NELyricViewCellScale(self.type) + NELyricViewCellTop(self.type) +
           NELyricViewCellBottom(self.type);
  }
}
#pragma mark - private

- (void)updateMaskLayerWithLine:(NSInteger)line {
  if (self.frame.size.height <= 0) {
    return;
  }
  if (self.type == NELyricViewTypeLyricPreview) {
    if (self.tableView.contentOffset.y + self.tableView.contentInset.top <= 0) {
      CGFloat hiddenBottom = (self.frame.size.height - 36.f) / self.frame.size.height;

      self.bgGradientView.locations = @[ @(0), @(MAX(0, hiddenBottom)), @(1) ];
      self.bgGradientView.colors = @[
        [UIColor colorWithWhite:1 alpha:1], [UIColor colorWithWhite:1 alpha:1],
        [UIColor colorWithWhite:1 alpha:0]
      ];
    } else {
      CGFloat hiddenTop = (24 * 1.f) / self.frame.size.height;
      CGFloat hiddenBottom = (self.frame.size.height - 36.f) / self.frame.size.height;
      hiddenBottom = MAX(0.1, MAX(hiddenTop, hiddenBottom));
      self.bgGradientView.locations = @[ @(0), @(hiddenTop), @(hiddenBottom), @(1) ];
      self.bgGradientView.colors = @[
        [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:1],
        [UIColor colorWithWhite:1 alpha:1], [UIColor colorWithWhite:1 alpha:0]
      ];
    }

  } else if (self.type == NELyricViewTypeLyric || self.type == NELyricViewTypeLyricOther ||
             self.type == NELyricViewTypeChorus) {
    CGFloat hiddenTop = ([self lyricTopHeight] * 1.f) / self.frame.size.height;
    CGFloat noGradientTop = ([self lyricTopHeight] * 1.f + 48.f) / self.frame.size.height;
    self.bgGradientView.locations = @[ @(0), @(hiddenTop), @(hiddenTop), @(noGradientTop), @(1) ];
    self.bgGradientView.colors = @[
      [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0],
      [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0],
      [UIColor colorWithWhite:1 alpha:1]
    ];
  } else {
    NELyricLine *currentLineModel = self.lyricList.count > line ? self.lyricList[line] : nil;
    if (currentLineModel.layoutInfo.recordSingLyricCellLyricSize.height > 33) {
      CGFloat hiddenTop = ([self lyricTopHeight] * 1.f) / self.frame.size.height;
      CGFloat noGradientTop = ([self lyricTopHeight] * 1.f + 100) / self.frame.size.height;

      self.bgGradientView.locations =
          @[ @(0), @(hiddenTop), @(hiddenTop), @(noGradientTop), @(noGradientTop), @(1) ];
      self.bgGradientView.colors = @[
        [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0],
        [UIColor colorWithWhite:1 alpha:1], [UIColor colorWithWhite:1 alpha:1],
        [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0]
      ];
    } else {
      CGFloat hiddenTop = ([self lyricTopHeight] * 1.f) / self.frame.size.height;
      CGFloat noGradientTop = ([self lyricTopHeight] * 1.f + 68) / self.frame.size.height;

      self.bgGradientView.locations =
          @[ @(0), @(hiddenTop), @(hiddenTop), @(noGradientTop), @(noGradientTop), @(1) ];
      self.bgGradientView.colors = @[
        [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0],
        [UIColor colorWithWhite:1 alpha:1], [UIColor colorWithWhite:1 alpha:1],
        [UIColor colorWithWhite:1 alpha:0], [UIColor colorWithWhite:1 alpha:0]
      ];
    }
  }
}

#pragma mark - lineAniamtion

- (void)update {
  if (self.lyricList.count == 0) {
    return;
  }
  NSInteger time = self.startTime;
  if (self.timeForCurrent) {
    time = self.timeForCurrent() + self.startTime;
  }
  self.currentTime = time;

  NELyricLine *lineModel = [self.lyricModel lineAtTime:time];
  NSInteger line = [self.lyricList indexOfObject:lineModel];
  if (line == NSNotFound) {
    return;
  }

  NSInteger waitTime = self.waitTimeForCurrent ? self.waitTimeForCurrent() : 0;
  if (waitTime > 0) {
    self.timeAtInterlude(waitTime);
  } else {
    if (self.lastSelect > 0 && self.lastSelect < self.lyricList.count && self.timeAtInterlude) {
      NSInteger currentLineTime = self.lyricList[self.lastSelect].startTime;
      NSInteger beforeLineEndTime = self.lyricList[self.lastSelect - 1].startTime +
                                    self.lyricList[self.lastSelect - 1].interval;
      // 大于7000ms 才算间奏
      if (time < currentLineTime && beforeLineEndTime <= time &&
          currentLineTime - beforeLineEndTime >= 7000) {
        self.timeAtInterlude(currentLineTime - time);
      }
    }
  }
  if (self.lastSelect != line) {
    if (self.lastSelect >= 0 && self.lyricList.count > self.lastSelect &&
        self.lyricList.count > line && [self.tableView numberOfSections] > 0 &&
        [self.tableView numberOfRowsInSection:0] > line) {
      NELyricLine *currentLineModel =
          self.lyricList.count > self.lastSelect ? self.lyricList[self.lastSelect] : nil;

      self.lastSelect = line;

      if (self.shouldScroll) {
        __weak typeof(self) weakSelf = self;
        [self
            doLyricAnimationWithBlock:^(NELyricViewCell *cell) {
              if (cell.lineModel == lineModel) {
                [cell reloadWithYrcLineModel:cell.lineModel
                                   animation:NELyricViewCellAnimationTypeShow];
              } else if (cell.lineModel == currentLineModel) {
                [cell reloadWithYrcLineModel:cell.lineModel
                                   animation:NELyricViewCellAnimationTypeDismiss];
              } else {
                [cell reloadWithYrcLineModel:cell.lineModel
                                   animation:NELyricViewCellAnimationTypeNone];
              }
            }
            extensionBlock:^{
              [weakSelf scrollToLine:line];
            }];
      } else {
        [self updateLineModel];
        [self.tableView reloadData];
      }
    } else {
      self.lastSelect = line;
      [self updateLineModel];
      [self.tableView reloadData];
      [self scrollToLine:0];
    }
  }
}

- (void)scrollToLine:(NSInteger)line {
  if ([self.tableView numberOfRowsInSection:0] > line) {
    CGFloat y = 0;
    for (int i = 0; i < line && i < self.lyricList.count; i++) {
      y += [self tableView:self.tableView
          heightForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
    }

    CGFloat lineHeight = [self tableView:self.tableView
                 heightForRowAtIndexPath:[NSIndexPath indexPathForRow:line inSection:0]];

    if (self.type == NELyricViewTypeLyricPreview) {
      CGFloat offset = self.tableView.contentOffset.y;
      if (y > offset + self.tableView.contentInset.top &&
          y + lineHeight - offset < self.tableView.frame.size.height) {
      } else {
        if (y <= offset + self.tableView.contentInset.top) {
          [self.tableView setContentOffset:CGPointMake(0, y - self.tableView.contentInset.top)
                                  animated:NO];
        } else {
          [self.tableView
              setContentOffset:CGPointMake(0, y - self.tableView.frame.size.height + lineHeight)
                      animated:NO];
        }
      }
    } else {
      [self.tableView setContentOffset:CGPointMake(0, y - self.tableView.contentInset.top)
                              animated:NO];
    }
    //
    [self updateMaskLayerWithLine:line];
  }
}

- (NSInteger)currentSelectIdx {
  CGPoint point = [self convertPoint:CGPointMake(100, [self lyricTopHeight]) toView:self.tableView];
  NSIndexPath *indexPath = [self.tableView indexPathForRowAtPoint:point];
  if (indexPath) {
    return indexPath.row;
  }
  if (self.lyricList.count == 0) {
    return 0;
  }

  CGRect frame = [self.tableView
      rectForRowAtIndexPath:[NSIndexPath indexPathForRow:self.lyricList.count - 1 inSection:0]];
  if (point.y > CGRectGetMaxY(frame)) {
    return self.lyricList.count - 1;
  } else {
    return 0;
  }
}

- (BOOL)lyricSelectIdx:(NSInteger)idx {
  return self.lastSelect == idx;
}

- (BOOL)shouldShowBigLyric:(NSInteger)idx {
  return [self lyricSelectIdx:idx];
}

- (void)updateLineModel {
  for (int idx = 0; idx < self.lyricList.count; idx++) {
    NELyricLine *obj = self.lyricList[idx];

    if (self.type == NELyricViewTypeLyricPreview) {
      obj.layoutInfo.recordSingLyricCellIsSelect = idx <= self.lastSelect;
      obj.layoutInfo.recordSingLyricCellIsShowBig = idx <= self.lastSelect;
    } else {
      obj.layoutInfo.recordSingLyricCellIsSelect = idx == self.lastSelect;
      obj.layoutInfo.recordSingLyricCellIsShowBig = [self shouldShowBigLyric:idx];
    }
  }
}

- (void)seekToMove:(NSInteger)idx
    seekActionBlock:(void (^)(NSInteger targetTime))seekActionBlock
    doUIActionBlock:(void (^)(void))doUIActionBlock {
  self.lastSelect = idx;
  NELyricLine *model =
      self.lyricList.count > self.lastSelect ? self.lyricList[self.lastSelect] : nil;
  if (model) {
    NSInteger targetTime = model.startTime - self.startTime;
    if (seekActionBlock) {
      seekActionBlock(targetTime);
    }
  }

  dispatch_async(dispatch_get_main_queue(), ^{
    [self scrollToLine:self.lastSelect];
    if (doUIActionBlock) {
      doUIActionBlock();
    }
  });
}

- (void)adjustSelectIdx {
  NSInteger idx = [self currentSelectIdx];
  if (idx != self.lastSelect) {
    self.lastSelect = idx;
    [self adjustLinePresent];
  } else {
    [self updateLineModel];
  }
}

- (void)adjustLinePresent {
  [self updateLineModel];
  [self.tableView.visibleCells bk_each:^(__kindof NELyricViewCell *obj) {
    [obj reloadWithYrcLineModel:obj.lineModel animation:NELyricViewCellAnimationTypeNone];
  }];
}

- (void)doLyricAnimationWithBlock:(void (^)(NELyricViewCell *cell))animationBlock
                   extensionBlock:(void (^)(void))extensionBlock {
  [self updateLineModel];
  __weak typeof(self) weakSelf = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    [weakSelf.tableView.visibleCells bk_each:^(__kindof NELyricViewCell *obj) {
      if (animationBlock) {
        animationBlock(obj);
      }
    }];
    [UIView animateWithDuration:0.2
                     animations:^{
                       if (@available(iOS 11.0, *)) {
                         [weakSelf.tableView
                             performBatchUpdates:^{
                               if (extensionBlock) {
                                 extensionBlock();
                               }
                             }
                                      completion:^(BOOL finished){
                                      }];
                       } else {
                         [weakSelf.tableView beginUpdates];
                         if (extensionBlock) {
                           extensionBlock();
                         }
                         [weakSelf.tableView endUpdates];
                       }
                     }];
  });
}

#pragma mark - set & get

- (void)setScrollEnabled:(bool)scrollEnabled {
  _scrollEnabled = scrollEnabled;
  self.tableView.scrollEnabled = scrollEnabled;
}

- (void)setInsetHeight:(CGFloat)insetHeight {
  if (self.type == NELyricViewTypeLyricPreview) {
    _insetHeight = insetHeight;
    self.tableView.contentInset = UIEdgeInsetsMake([self lyricTopHeight], 0, 0, 0);
  }
}

- (UITableView *)tableView {
  if (!_tableView) {
    _tableView = [UITableView new];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.scrollsToTop = NO;
    _tableView.scrollEnabled = self.scrollEnabled;
    _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    _tableView.tableFooterView = [UIView new];
    _tableView.backgroundColor = [UIColor clearColor];
    _tableView.estimatedSectionFooterHeight = 0;
    _tableView.contentInset = UIEdgeInsetsMake([self lyricTopHeight], 0, 0, 0);
    _tableView.estimatedRowHeight = 0;
    _tableView.estimatedSectionHeaderHeight = 0;
    if (@available(iOS 11, *)) {
      _tableView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    _tableView.showsVerticalScrollIndicator = NO;
    _tableView.showsHorizontalScrollIndicator = NO;
  }
  return _tableView;
}

- (NEGradientView *)bgGradientView {
  if (!_bgGradientView) {
    _bgGradientView = [[NEGradientView alloc] initWithFrame:self.frame];
    ;
    _bgGradientView.direction = NEGradientViewDirectionVertical;
    _bgGradientView.userInteractionEnabled = NO;
  }
  return _bgGradientView;
}

@end
