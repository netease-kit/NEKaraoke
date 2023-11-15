// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAERecordPanelEffectCell.h"
#import <Masonry/Masonry.h>
#import "UIColor+AudioEffect.h"
#import "UIImage+AudioEffect.h"

static const NSInteger kInsect = 4;

@implementation NEAERecordMixEffectModel

+ (NSArray<NEAERecordMixEffectModel *> *)defaultModels {
  NEAERecordMixEffectModel *m1 = [[NEAERecordMixEffectModel alloc] init];
  //    m1.name = @"默认";
  m1.key = 0;
  m1.localImage = [UIImage ne_imageNamed:@"effect_default"];

  NEAERecordMixEffectModel *m2 = [[NEAERecordMixEffectModel alloc] init];
  //    m2.name = @"KTV";
  m2.key = 7;
  m2.localImage = [UIImage ne_imageNamed:@"effect_ktv"];

  NEAERecordMixEffectModel *m3 = [[NEAERecordMixEffectModel alloc] init];
  //    m3.name = @"教堂";
  m3.key = 9;
  m3.localImage = [UIImage ne_imageNamed:@"effect_church"];

  NEAERecordMixEffectModel *m4 = [[NEAERecordMixEffectModel alloc] init];
  //    m4.name = @"圆润";
  m4.key = 2;
  m4.localImage = [UIImage ne_imageNamed:@"effect_mellow"];

  NEAERecordMixEffectModel *m5 = [[NEAERecordMixEffectModel alloc] init];
  //    m5.name = @"Live";
  m5.key = 11;
  m5.localImage = [UIImage ne_imageNamed:@"effect_live"];

  NEAERecordMixEffectModel *m6 = [[NEAERecordMixEffectModel alloc] init];
  //    m6.name = @"悠远";
  m6.key = 10;
  m6.localImage = [UIImage ne_imageNamed:@"effect_distant"];

  NEAERecordMixEffectModel *m7 = [[NEAERecordMixEffectModel alloc] init];
  //    m7.name = @"清澈";
  m7.key = 3;
  m7.localImage = [UIImage ne_imageNamed:@"effect_clear"];

  NEAERecordMixEffectModel *m8 = [[NEAERecordMixEffectModel alloc] init];
  //    m8.name = @"低沉";
  m8.key = 1;
  m8.localImage = [UIImage ne_imageNamed:@"effect_low"];

  return @[ m1, m2, m3, m4, m5, m6, m7, m8 ];
}

@end

@interface NEAERecordPanelEffectCell ()

@property(nonatomic, strong) UIButton *checkButton;

@property(nonatomic, strong) UIView *backView;

@property(nonatomic, strong) UILabel *titleL;

@property(nonatomic, strong) UIImageView *backgroundImageV;

@property(nonatomic, strong) NEAERecordMixEffectModel *model;

@property(nonatomic, assign) BOOL layoutFinished;

@end

@implementation NEAERecordPanelEffectCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadUI];
  }
  return self;
}

+ (CGFloat)height {
  return 54;
}

- (CGFloat)backViewHeight {
  return [NEAERecordPanelEffectCell height];
}

- (void)loadUI {
  [self.contentView addSubview:self.backView];
  [self.backView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.center.equalTo(self.contentView);
    make.size.mas_equalTo(CGSizeMake([self backViewHeight], [self backViewHeight]));
  }];
  self.backView.layer.cornerRadius = [self backViewHeight] * 0.5;
  self.backView.clipsToBounds = YES;
  [self.contentView addSubview:self.checkButton];
  [self.checkButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.mas_equalTo(self.backView);
    make.bottom.mas_equalTo(self.backView).offset(2);
    make.size.mas_equalTo(CGSizeMake(18, 12));
  }];
  [self.contentView addSubview:self.cornerMarkButton];
  [self.cornerMarkButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.backView.mas_right).mas_offset(6.f);
    make.top.equalTo(self.backView.mas_top);
    make.height.mas_equalTo(12.f);
  }];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  if (!self.layoutFinished) {
    self.backgroundImageV.layer.cornerRadius = ([self backViewHeight] - kInsect * 2) * 0.5;
    self.backgroundImageV.clipsToBounds = YES;
    self.layoutFinished = YES;
  }
}

- (void)updateModel:(NEAERecordMixEffectModel *)model {
  _model = model;

  if (model.selected) {
    self.checkButton.layer.opacity = 1;
    self.backView.layer.borderColor = [UIColor whiteColor].CGColor;
  } else {
    self.checkButton.layer.opacity = 0;
    self.backView.layer.borderColor = [UIColor clearColor].CGColor;
  }
  self.cellSelected = model.selected;
  if (self.cellSelected) {
    self.checkButton.layer.opacity = 1;
    self.backView.layer.borderColor = [UIColor whiteColor].CGColor;
  } else {
    self.checkButton.layer.opacity = 0;
    self.backView.layer.borderColor = [UIColor clearColor].CGColor;
  }
  self.titleL.text = model.name;
  self.backgroundImageV.image = model.localImage;
}

#pragma mark - getter

- (UIButton *)checkButton {
  if (!_checkButton) {
    _checkButton = [UIButton new];
    [_checkButton setImage:[UIImage ne_imageNamed:@"rcd_sing_icn_adjust_reverberation_selected"]
                  forState:UIControlStateNormal];
    _checkButton.backgroundColor = [UIColor whiteColor];
    _checkButton.layer.opacity = 0;
    _checkButton.layer.cornerRadius = 6;
    _checkButton.clipsToBounds = YES;
  }
  return _checkButton;
}

- (UIView *)backView {
  if (!_backView) {
    _backView = [UIView new];
    _backView.backgroundColor = [UIColor clearColor];
    [_backView addSubview:self.backgroundImageV];
    _backView.layer.borderWidth = 2;
    _backView.layer.borderColor = [UIColor clearColor].CGColor;
    [_backView addSubview:self.titleL];

    [self.titleL mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(_backView).inset(kInsect);
    }];

    [self.backgroundImageV mas_makeConstraints:^(MASConstraintMaker *make) {
      make.edges.equalTo(_backView).inset(kInsect);
    }];
  }
  return _backView;
}

- (UILabel *)titleL {
  if (!_titleL) {
    _titleL = [UILabel new];
    _titleL.font = [UIFont systemFontOfSize:11 weight:UIFontWeightMedium];
    _titleL.textColor = [UIColor whiteColor];
    _titleL.textAlignment = NSTextAlignmentCenter;
    _titleL.backgroundColor = [UIColor clearColor];
    _titleL.layer.shadowOpacity = 0.3;
    _titleL.layer.shadowColor = [UIColor colorWithWhite:0 alpha:0.3].CGColor;
  }
  return _titleL;
}

- (UIImageView *)backgroundImageV {
  if (!_backgroundImageV) {
    _backgroundImageV = [UIImageView new];
    _backgroundImageV.backgroundColor = [UIColor clearColor];
  }
  return _backgroundImageV;
}

@end
