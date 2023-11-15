// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <YYText/YYText.h>

NS_ASSUME_NONNULL_BEGIN

@interface NELyricLineLayout : NSObject

@property(nonatomic, copy) void (^sizeLyricUpdateHandler)(YYLabel *label, NSString *lyricText);
@property(nonatomic, copy) void (^lyricUpdateHandler)(YYLabel *label, NSString *lyricText);
@property(nonatomic, strong, nullable) NSAttributedString *lyricTextAttrStr;

@property(nonatomic, assign) CGSize recordSingLyricCellLyricSize;
@property(nonatomic, assign) BOOL recordSingLyricCellIsSelect;
@property(nonatomic, assign) BOOL recordSingLyricCellIsShowBig;

@end

NS_ASSUME_NONNULL_END
