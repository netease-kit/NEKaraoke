// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <BlocksKit/BlocksKit.h>
#import "NELyricLine+Layout.h"

@interface YYLabel ()
- (CGRect)_convertRectFromLayout:(CGRect)rect;
@end

@implementation NELyricPresentLine

@end

@implementation NELyricWord (Layout)

- (void)setCacheFrame:(CGRect)cacheFrame {
  [self bk_associateValue:@(cacheFrame) withKey:@selector(cacheFrame)];
}

- (CGRect)cacheFrame {
  return [[self bk_associatedValueForKey:@selector(cacheFrame)] CGRectValue];
}

- (void)setLineModel:(NELyricPresentLine *)lineModel {
  [self bk_weaklyAssociateValue:lineModel withKey:@selector(lineModel)];
}

- (NELyricPresentLine *)lineModel {
  return [self bk_associatedValueForKey:@selector(lineModel)];
}

@end

@implementation NELyricLine (Layout)

- (void)asynLayoutSizeWithMaxSize:(CGSize)maxSize sizeBlock:(void (^)(CGSize size))sizeBlock {
  __weak typeof(self) weakSelf = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    if (weakSelf.layoutInfo.sizeLyricUpdateHandler) {
      weakSelf.layoutInfo.sizeLyricUpdateHandler(weakSelf.label, weakSelf.text);
    }
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      YYTextContainer *container = [YYTextContainer containerWithSize:maxSize];
      YYTextLayout *layoutWidth =
          [YYTextLayout layoutWithContainer:container text:weakSelf.label.attributedText.copy];
      weakSelf.layoutInfo.recordSingLyricCellLyricSize = layoutWidth.textBoundingSize;
      if (sizeBlock) {
        sizeBlock(weakSelf.layoutInfo.recordSingLyricCellLyricSize);
      }
    });
  });
}

- (void)preLayoutWithMaxWidth:(CGFloat)maxWidth complete:(void (^)(CGSize size))complete {
  if (self.lastPreLayoutWidth == maxWidth) {
    //        NELogDebug(@"lastPreLayoutWidth = > %.2f",maxWidth);
    if (complete) {
      complete(self.layoutInfo.recordSingLyricCellLyricSize);
    }
    return;
  }
  self.lastPreLayoutWidth = maxWidth;
  //    NELogDebug(@"lastPreLayoutWidth = > No %.2f",maxWidth);

  __weak typeof(self) weakSelf = self;
  [self
      asynLayoutSizeWithMaxSize:CGSizeMake(maxWidth, CGFLOAT_MAX)
                      sizeBlock:^(CGSize size) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                          __strong typeof(weakSelf) self = weakSelf;
                          if (self.layoutInfo.lyricUpdateHandler) {
                            self.layoutInfo.lyricUpdateHandler(self.label, self.text);
                          }
                          self.label.frame = CGRectMake(0, 0, size.width, size.height);
                          [self.label setNeedsDisplay];

                          // 韩文 矫正word跨行
                          NSMutableArray *reloadWords = @[].mutableCopy;
                          NSMutableArray *errorWords = @[].mutableCopy;

                          for (int i = 0; i < self.label.textLayout.lines.count; i++) {
                            YYTextLine *line = self.label.textLayout.lines[i];
                            NSInteger atWordIdx = 0;
                            NELyricWord *endWord =
                                [self wordAtIdx:line.range.location + line.range.length - 1
                                      atWordIdx:&atWordIdx];
                            if (endWord) {
                              if (atWordIdx != endWord.wordText.length - 1) {
                                // 跨行了
                                NELyricWord *startWord = [self wordAtIdx:line.range.location];
                                if (endWord == startWord) {
                                  [errorWords addObject:endWord];
                                } else {
                                  [reloadWords addObject:endWord];
                                }
                              }
                            }
                          }

                          if (errorWords.count > 0) {
                            //                NELogError(@"歌词 %@
                            //                不应该有一个词跨好几行的情况",self.text);
                          }

                          if (reloadWords.count > 0 || errorWords.count > 0) {
                            [reloadWords bk_each:^(NELyricWord *obj) {
                              NSInteger idx = [self.words indexOfObject:obj];
                              if (idx - 1 < self.words.count && idx - 1 >= 0) {
                                self.words[idx - 1].wordText = [NSString
                                    stringWithFormat:@"%@\n", self.words[idx - 1].wordText];
                              };
                            }];

                            [errorWords bk_each:^(NELyricWord *obj) {
                              obj.wordText = @"";
                            }];

                            self.text = [self.words
                                bk_reduce:@""
                                withBlock:^id(id sum, NELyricWord *obj) {
                                  return [NSString stringWithFormat:@"%@%@", sum, obj.wordText];
                                }];
                            self.label = nil;
                            self.layoutInfo.lyricTextAttrStr = nil;
                            self.lastPreLayoutWidth = -1;
                            [self preLayoutWithMaxWidth:maxWidth complete:complete];
                          } else {
                            NSMutableArray<NELyricPresentLine *> *lineModels = @[].mutableCopy;
                            for (int i = 0; i < self.label.textLayout.lines.count; i++) {
                              YYTextLine *line = self.label.textLayout.lines[i];
                              NELyricPresentLine *lineModel = [NELyricPresentLine new];

                              NELyricWord *startWord = [self wordAtIdx:line.range.location];
                              NELyricWord *endWord =
                                  [self wordAtIdx:line.range.location + line.range.length - 1];
                              if (!startWord || !endWord || self.words.count == 0) {
                                NSLog(@"Error Happen");
                                continue;
                              }
                              NSInteger idx = [self.words indexOfObject:startWord];
                              NSInteger endWordIdx = [self.words indexOfObject:endWord];
                              NSArray *words = [self.words
                                  subarrayWithRange:NSMakeRange(idx, endWordIdx - idx + 1)];

                              CGRect rect = [self.label
                                  _convertRectFromLayout:
                                      [self.label.textLayout
                                          firstRectForRange:[YYTextRange
                                                                rangeWithRange:line.range]]];
                              if (i != 0) {
                                // 修正上一行frmae
                                CGRect beforeLineFrame = lineModels[i - 1].lineFrame;
                                CGRect intersection = CGRectIntersection(beforeLineFrame, rect);
                                beforeLineFrame.size.height -= intersection.size.height;
                                lineModels[i - 1].lineFrame = beforeLineFrame;
                              }
                              [words bk_each:^(NELyricWord *obj) {
                                obj.lineModel = lineModel;
                              }];
                              lineModel.words = words;
                              lineModel.lineFrame = rect;
                              [lineModels addObject:lineModel];
                            }
                            self.presentLines = lineModels;

                            __weak typeof(self) weakSelf = self;
                            [self.presentLines bk_each:^(NELyricPresentLine *presentLineModel) {
                              __strong typeof(weakSelf) self = weakSelf;
                              for (NELyricWord *word in presentLineModel.words) {
                                NSRange range = [self rangeAtWord:word];
                                CGRect rect = [self.label
                                    _convertRectFromLayout:
                                        ([self.label.textLayout
                                            firstRectForRange:[YYTextRange rangeWithRange:range]])];
                                word.cacheFrame =
                                    CGRectIntersection(rect, presentLineModel.lineFrame);
                              }
                            }];

                            if (complete) {
                              complete(size);
                            }
                          }
                        });
                      }];
}

- (NELyricPresentLine *)presentLineForWord:(NELyricWord *)word {
  if (word.lineModel) {
    return word.lineModel;
  }
  NSArray<NELyricPresentLine *> *presentLines = self.presentLines;
  return
      [presentLines bk_select:^BOOL(NELyricPresentLine *obj) {
        return [obj.words containsObject:word];
      }].firstObject;
}

#pragma mark - set & get

- (void)setLastPreLayoutWidth:(CGFloat)lastPreLayoutWidth {
  [self bk_associateValue:@(lastPreLayoutWidth) withKey:@selector(lastPreLayoutWidth)];
}

- (CGFloat)lastPreLayoutWidth {
  return [[self bk_associatedValueForKey:@selector(lastPreLayoutWidth)] doubleValue];
}

- (void)setPresentLines:(NSArray<NELyricPresentLine *> *)presentLines {
  return [self bk_associateValue:presentLines withKey:@selector(presentLines)];
}

- (NSArray<NELyricPresentLine *> *)presentLines {
  return [self bk_associatedValueForKey:@selector(presentLines)];
}

- (void)setLayoutInfo:(NELyricLineLayout *)layoutInfo {
  [self bk_associateValue:layoutInfo withKey:@selector(layoutInfo)];
}

- (NELyricLineLayout *)layoutInfo {
  NELyricLineLayout *info = [self bk_associatedValueForKey:@selector(layoutInfo)];
  if (!info) {
    info = [NELyricLineLayout new];
    [self setLayoutInfo:info];
  }
  return info;
}

- (void)setLabel:(YYLabel *)label {
  [self bk_associateValue:label withKey:@selector(label)];
}

- (YYLabel *)label {
  YYLabel *label = [self bk_associatedValueForKey:@selector(label)];
  if (!label) {
    label = [YYLabel new];
    [self setLabel:label];
  }
  return label;
}

@end
