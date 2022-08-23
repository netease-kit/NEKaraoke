// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatItemView.h"
#import <Masonry/Masonry.h>
#import "UIImage+Karaoke.h"

@interface NEKaraokeSeatItemView ()
@property(nonatomic, strong) UIImageView *bgImage;
@property(nonatomic, strong) NEKaraokeSeatItem *seatItem;
@end

@implementation NEKaraokeSeatItemView
- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setupSubviews];
    [self makeConstrains];
  }
  return self;
}
- (void)setupSubviews {
  [self addSubview:self.bgImage];
  [self addSubview:self.avatarImageView];
  [self addSubview:self.microphoneImage];
  [self addSubview:self.nameLabel];
}
- (void)makeConstrains {
  [self.bgImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(40, 40));
    make.top.mas_equalTo(0);
    make.centerX.mas_equalTo(0);
  }];
  [self.avatarImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(40, 40));
    make.centerX.mas_equalTo(0);
    make.top.mas_equalTo(0);
  }];
  [self.microphoneImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(14, 14));
    make.bottom.right.equalTo(self.avatarImageView);
  }];
  [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.avatarImageView);
    make.top.equalTo(self.avatarImageView.mas_bottom).offset(4);
    make.height.mas_equalTo(16);
  }];
}
- (void)configSeatItem:(NEKaraokeSeatItem *)seatItem
                  name:(NSString *)name
             songModel:(NEKaraokeSongModel *)songModel {
  self.seatItem = seatItem;
  self.avatarImageView.hidden = self.microphoneImage.hidden = YES;
  self.nameLabel.text = name;
  if ([songModel.account isEqualToString:seatItem.user] ||
      [songModel.assistantUuid isEqualToString:seatItem.user]) {
    self.nameLabel.text = @"演唱中";
  }

  switch (seatItem.status) {
    case NEKaraokeSeatItemStatusInitial:  // 初始化
      break;
    case NEKaraokeSeatItemStatusWaiting:  // 等待申请或接收邀请
      break;
    case NEKaraokeSeatItemStatusTaken:  // 已被占用
      self.avatarImageView.hidden = self.microphoneImage.hidden = NO;
      self.avatarImageView.userInteractionEnabled = YES;
      BOOL isOpen = [self isAudioOnWithUserUuid:seatItem.user];
      self.avatarImageView.image = [UIImage karaoke_imageWithUrl:seatItem.icon];
      self.microphoneImage.image =
          [UIImage karaoke_imageNamed:isOpen ? @"seat_mic_open" : @"seat_mic_close"];
      break;
    default:  // 关闭
      break;
  }
}
- (BOOL)isAudioOnWithUserUuid:(NSString *)userUuid {
  BOOL isOpen = NO;
  for (NEKaraokeMember *member in NEKaraokeKit.shared.allMemberList) {
    if ([member.account isEqualToString:userUuid]) {
      isOpen = member.isAudioOn;
    }
  }
  return isOpen;
}
- (void)didSelected {
  if (self.delegate && [self.delegate respondsToSelector:@selector(didSelectedSeat:seatItem:)]) {
    [self.delegate didSelectedSeat:self seatItem:self.seatItem];
  }
}
#pragma mark-----------------------------  getter  -----------------------------
- (UIImageView *)bgImage {
  if (!_bgImage) {
    _bgImage = [[UIImageView alloc] initWithImage:[UIImage karaoke_imageNamed:@"seat_init"]];
    _bgImage.userInteractionEnabled = YES;
    UITapGestureRecognizer *tapGR =
        [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(didSelected)];
    [_bgImage addGestureRecognizer:tapGR];
  }
  return _bgImage;
}
- (UIImageView *)avatarImageView {
  if (!_avatarImageView) {
    _avatarImageView = [[UIImageView alloc] initWithFrame:CGRectZero];
    _avatarImageView.userInteractionEnabled = YES;
    _avatarImageView.layer.cornerRadius = 20;
    _avatarImageView.layer.masksToBounds = YES;
    _avatarImageView.layer.borderWidth = 1;
    _avatarImageView.layer.borderColor = UIColor.whiteColor.CGColor;
    _avatarImageView.backgroundColor = [UIColor.redColor colorWithAlphaComponent:0.5];
    _avatarImageView.hidden = YES;
    UITapGestureRecognizer *tapGR =
        [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(didSelected)];
    [_avatarImageView addGestureRecognizer:tapGR];
  }
  return _avatarImageView;
}
- (UIImageView *)microphoneImage {
  if (!_microphoneImage) {
    _microphoneImage = [[UIImageView alloc] initWithFrame:CGRectZero];
    _microphoneImage.userInteractionEnabled = YES;
    _microphoneImage.layer.cornerRadius = 7;
    _microphoneImage.layer.masksToBounds = YES;
  }
  return _microphoneImage;
}
- (UILabel *)nameLabel {
  if (!_nameLabel) {
    _nameLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _nameLabel.textColor = UIColor.whiteColor;
    _nameLabel.font = [UIFont systemFontOfSize:10];
    _nameLabel.textAlignment = NSTextAlignmentCenter;
    _nameLabel.text = @"房主";
  }
  return _nameLabel;
}
@end
