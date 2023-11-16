// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NEAERecordMixEffectModel : NSObject

/// 是否选择
@property(nonatomic, assign) BOOL selected;

@property(nonatomic, copy) NSString *name;

@property(nonatomic, assign) int key;

@property(nonatomic, strong) UIImage *localImage;

+ (NSArray<NEAERecordMixEffectModel *> *)defaultModels;

@end

@interface NEAERecordPanelEffectCell : UICollectionViewCell

@property(nonatomic, assign) BOOL cellSelected;

@property(readonly) UILabel *titleL;

@property(nonatomic) UIView *cornerMarkButton;

@property(readonly) NEAERecordMixEffectModel *model;

- (void)updateModel:(NEAERecordMixEffectModel *)model;

@end

NS_ASSUME_NONNULL_END
