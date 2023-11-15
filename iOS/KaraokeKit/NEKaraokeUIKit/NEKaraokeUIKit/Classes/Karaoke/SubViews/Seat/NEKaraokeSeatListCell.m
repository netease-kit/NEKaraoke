// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatListCell.h"
#import <Masonry/Masonry.h>
#import <SDWebImage/SDWebImage.h>
#import "NEKaraokeLocalized.h"
#import "UIImage+Karaoke.h"
@interface NEKaraokeSeatListCell ()
@property(nonatomic, strong) UILabel *numberLabel;
@property(nonatomic, strong) UIImageView *avatarImage;
@property(nonatomic, strong) UILabel *nameLabel;
@property(nonatomic, strong) UIButton *rejectBtn;
@property(nonatomic, strong) UIButton *allowBtn;
@property(nonatomic, strong) UIButton *cancelBtn;
@end

@implementation NEKaraokeSeatListCell
- (instancetype)initWithStyle:(UITableViewCellStyle)style
              reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.contentView.backgroundColor = UIColor.clearColor;
    self.backgroundColor = UIColor.clearColor;
    [self setupSubviews];
    [self makeConstrains];
  }
  return self;
}
- (void)setupSubviews {
  [self.contentView addSubview:self.numberLabel];
  [self.contentView addSubview:self.avatarImage];
  [self.contentView addSubview:self.nameLabel];
  [self.contentView addSubview:self.cancelBtn];
  [self.contentView addSubview:self.rejectBtn];
  [self.contentView addSubview:self.allowBtn];
}
- (void)makeConstrains {
  [self.numberLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(15);
    make.centerY.mas_equalTo(0);
    make.height.mas_equalTo(22);
    make.width.mas_equalTo(40);
  }];
  [self.avatarImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(30, 30));
    make.centerY.mas_equalTo(0);
    make.left.equalTo(self.numberLabel.mas_right).offset(7);
  }];
  [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.avatarImage.mas_right).offset(8);
    make.centerY.mas_equalTo(0);
    make.width.mas_equalTo(100);
  }];
  [self.cancelBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(80, 22));
    make.right.mas_equalTo(-15);
    make.centerY.mas_equalTo(0);
  }];
  [self.allowBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(80, 22));
    make.right.mas_equalTo(-15);
    make.centerY.mas_equalTo(0);
  }];
  [self.rejectBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(45, 22));
    make.right.equalTo(self.allowBtn.mas_left).offset(-10);
    make.centerY.mas_equalTo(0);
  }];
}
- (void)configCellWithItem:(NEKaraokeSeatRequestItem *)item
                     index:(NSInteger)index
                  isAnchor:(BOOL)isAnchor
                    isSelf:(BOOL)isSelf {
  self.cancelBtn.hidden = self.rejectBtn.hidden = self.allowBtn.hidden = NO;
  if (isAnchor) {
    self.cancelBtn.hidden = YES;
  } else {
    if (!isSelf) {
      self.cancelBtn.hidden = self.rejectBtn.hidden = self.allowBtn.hidden = YES;
    } else {
      self.rejectBtn.hidden = self.allowBtn.hidden = YES;
    }
  }
  self.numberLabel.text = [NSString stringWithFormat:@"%02ld", index + 1];
  [self.avatarImage sd_setImageWithURL:[NSURL URLWithString:item.icon]];
  self.nameLabel.text = item.userName ?: @"";
}
- (void)allowBtnAction {
  if (self.allowBlock) {
    self.allowBlock();
  }
}
- (void)rejectBtnAction {
  if (self.rejectBlock) {
    self.rejectBlock();
  }
}
- (void)cancelBtnAction {
  if (self.cancelBlock) {
    self.cancelBlock();
  }
}
#pragma mark-----------------------------  Getter  -----------------------------
- (UILabel *)numberLabel {
  if (!_numberLabel) {
    _numberLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _numberLabel.textColor = UIColor.whiteColor;
    _numberLabel.font = [UIFont systemFontOfSize:14];
  }
  return _numberLabel;
}
- (UIImageView *)avatarImage {
  if (!_avatarImage) {
    _avatarImage = [[UIImageView alloc] initWithFrame:CGRectZero];
    _avatarImage.layer.cornerRadius = 15;
    _avatarImage.layer.masksToBounds = YES;
  }
  return _avatarImage;
}
- (UILabel *)nameLabel {
  if (!_nameLabel) {
    _nameLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _nameLabel.textColor = UIColor.whiteColor;
    _nameLabel.font = [UIFont systemFontOfSize:16];
  }
  return _nameLabel;
}
- (UIButton *)rejectBtn {
  if (!_rejectBtn) {
    _rejectBtn = [self createBtn];
    [_rejectBtn setTitle:NELocalizedString(@"拒绝") forState:UIControlStateNormal];
    [_rejectBtn addTarget:self
                   action:@selector(rejectBtnAction)
         forControlEvents:UIControlEventTouchUpInside];
  }
  return _rejectBtn;
}
- (UIButton *)cancelBtn {
  if (!_cancelBtn) {
    _cancelBtn = [self createBtn];
    [_cancelBtn setTitle:NELocalizedString(@"取消申请") forState:UIControlStateNormal];
    [_cancelBtn addTarget:self
                   action:@selector(cancelBtnAction)
         forControlEvents:UIControlEventTouchUpInside];
  }
  return _cancelBtn;
}
- (UIButton *)allowBtn {
  if (!_allowBtn) {
    _allowBtn = [self createBtn];
    [_allowBtn setTitle:NELocalizedString(@"允许上麦") forState:UIControlStateNormal];
    [_allowBtn addTarget:self
                  action:@selector(allowBtnAction)
        forControlEvents:UIControlEventTouchUpInside];
  }
  return _allowBtn;
}

- (UIButton *)createBtn {
  UIButton *btn = [[UIButton alloc] initWithFrame:CGRectZero];
  btn.layer.cornerRadius = 11;
  btn.layer.borderWidth = 1.0;
  btn.layer.borderColor = UIColor.whiteColor.CGColor;
  btn.layer.masksToBounds = YES;
  [btn setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
  btn.titleLabel.font = [UIFont systemFontOfSize:14];
  btn.backgroundColor = UIColor.clearColor;
  return btn;
}
@end
