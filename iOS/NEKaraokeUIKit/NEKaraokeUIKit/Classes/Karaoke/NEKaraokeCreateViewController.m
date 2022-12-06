// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeCreateViewController.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeDefine.h"
#import "NEKaraokeViewController.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"
@import NEKaraokeKit;
#import <libextobjc/extobjc.h>
#import "NEKaraokeAuthorityHelper.h"
#import "NEKaraokeReachability.h"
#import "NEKaraokeToast.h"
#import "NEKaraokeUIManager.h"

@interface NEKaraokeCreateTextField : UITextField

@end

@implementation NEKaraokeCreateTextField

- (CGRect)editingRectForBounds:(CGRect)bounds {
  CGRect rect = [super editingRectForBounds:bounds];
  rect.origin.x += 16;
  return rect;
}

- (CGRect)textRectForBounds:(CGRect)bounds {
  CGRect rect = [super textRectForBounds:bounds];
  rect.origin.x += 16;
  return rect;
}

@end

@interface NEKaraokeCreateCheckBox : UIView

@property(nonatomic, assign) bool checked;

@property(nonatomic, strong) UIImageView *checkImage;
@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, strong) UILabel *detailLabel;

@property(nonatomic, copy) void (^checkChanged)(bool);

@end

@implementation NEKaraokeCreateCheckBox

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
  }
  return self;
}

- (void)setupView {
  [self addSubview:self.checkImage];
  [self.checkImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.width.height.mas_equalTo(15);
    make.top.left.equalTo(self);
  }];

  [self addSubview:self.titleLabel];
  [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.mas_equalTo(18);
    make.top.equalTo(self);
    make.left.equalTo(self.checkImage.mas_right).offset(9);
    make.right.equalTo(self);
  }];

  [self addSubview:self.detailLabel];
  [self.detailLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.mas_equalTo(12);
    make.top.equalTo(self.titleLabel.mas_bottom).offset(4);
    make.left.equalTo(self.titleLabel);
    make.right.equalTo(self);
  }];

  self.userInteractionEnabled = true;
  [self addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self
                                                                     action:@selector(check)]];
}

- (void)setChecked:(bool)checked {
  _checked = checked;
  _checkImage.image = checked ? [UIImage karaoke_imageNamed:@"checked_ico"]
                              : [UIImage karaoke_imageNamed:@"unchecked_ico"];
}

- (UIImageView *)checkImage {
  if (!_checkImage) {
    _checkImage = [[UIImageView alloc] initWithImage:[UIImage karaoke_imageNamed:@"unchecked_ico"]];
  }
  return _checkImage;
}

- (void)check {
  if (self.checkChanged) {
    self.checkChanged(self.checked);
  }
}

- (UILabel *)titleLabel {
  if (!_titleLabel) {
    _titleLabel = [UILabel new];
    _titleLabel.textColor = [UIColor whiteColor];
    _titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  }
  return _titleLabel;
}

- (UILabel *)detailLabel {
  if (!_detailLabel) {
    _detailLabel = [UILabel new];
    _detailLabel.alpha = 0.4;
    _detailLabel.textColor = [UIColor whiteColor];
    _detailLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
  }
  return _detailLabel;
}

@end

@interface NEKaraokeCreateViewController ()

@property(nonatomic, strong) UIView *nameView;
@property(nonatomic, strong) NEKaraokeCreateTextField *roomName;
@property(nonatomic, strong) NEKaraokeCreateTextField *hostName;

@property(nonatomic, strong) UIView *typeView;
@property(nonatomic, strong) NEKaraokeCreateCheckBox *aiBox;
@property(nonatomic, strong) NEKaraokeCreateCheckBox *serialBox;
@property(nonatomic, strong) NEKaraokeCreateCheckBox *ntpBox;

@property(nonatomic, strong) UIButton *createButton;
@property(nonatomic, strong) CAGradientLayer *buttonBackground;

@property(nonatomic, strong) NEKaraokeReachability *reachability;

@end

@implementation NEKaraokeCreateViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.

  self.reachability = [NEKaraokeReachability reachabilityForInternetConnection];

  // nav
  self.title = @"创建房间";
  self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc]
      initWithImage:[[UIImage karaoke_imageNamed:@"close"]
                        imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal]
              style:UIBarButtonItemStylePlain
             target:self
             action:@selector(close)];
  [self.navigationController.navigationBar
      setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
  self.view.backgroundColor = [NEKaraokeDefine createBackground];

  [self.view
      addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self
                                                                   action:@selector(endEditing)]];

  // subviews
  [self layoutNameView];
  [self layoutTypeView];

  [self.view addSubview:self.createButton];
  [self.createButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(14);
    make.right.mas_equalTo(-14);
    make.top.mas_equalTo(self.typeView.mas_bottom).offset(32);
    make.height.mas_equalTo(40);
  }];

  [self.view layoutIfNeeded];
  [self.createButton.layer insertSublayer:self.buttonBackground atIndex:0];

  [self randomName];
}

- (void)randomName {
  int a;
  srand((unsigned int)time(NULL));
  a = rand() % (1000 - 100) + 100;
  self.roomName.text = [NSString stringWithFormat:@"在线K歌 %d", a];
}

- (void)endEditing {
  [self.view endEditing:true];
}

- (void)close {
  [self.navigationController dismissViewControllerAnimated:true completion:nil];
}

- (void)layoutNameView {
  [self.view addSubview:self.nameView];
  [self.nameView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(14);
    make.right.mas_equalTo(-14);
    if (@available(iOS 11.0, *)) {
      make.top.mas_equalTo(self.view.mas_safeAreaLayoutGuideTop).offset(16);
    } else {
      make.top.mas_equalTo(16);
    }
    make.height.mas_equalTo(192);
  }];

  UILabel *room = [[UILabel alloc] init];
  room.text = @"房间名";
  room.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  room.textColor = [UIColor whiteColor];
  room.alpha = 0.5;
  [self.nameView addSubview:room];
  [room mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.offset(20);
    make.left.offset(16);
    make.right.equalTo(self.nameView);
    make.height.mas_equalTo(16);
  }];

  [self.nameView addSubview:self.roomName];
  [self.roomName mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(room.mas_bottom).offset(8);
    make.left.offset(16);
    make.right.offset(-16);
    make.height.mas_equalTo(40);
  }];

  UILabel *user = [[UILabel alloc] init];
  user.text = @"用户名";
  user.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  user.textColor = [UIColor whiteColor];
  user.alpha = 0.5;
  [self.nameView addSubview:user];
  [user mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.roomName.mas_bottom).offset(24);
    make.left.offset(16);
    make.right.equalTo(self.nameView);
    make.height.mas_equalTo(16);
  }];
  [self.nameView addSubview:self.hostName];
  [self.hostName mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(user.mas_bottom).offset(8);
    make.left.offset(16);
    make.right.offset(-16);
    make.height.mas_equalTo(40);
  }];
}

- (void)layoutTypeView {
  [self.view addSubview:self.typeView];
  [self.typeView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(14);
    make.right.mas_equalTo(-14);
    make.top.mas_equalTo(self.nameView.mas_bottom).offset(12);
    make.height.mas_equalTo(106);
  }];

  [self.typeView addSubview:self.aiBox];
  [self.aiBox mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.typeView).offset(21);
    make.left.equalTo(self.typeView).offset(17);
    make.width.mas_equalTo(200);
    make.height.mas_equalTo(35);
  }];

  [self.typeView addSubview:self.serialBox];
  [self.serialBox mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.typeView).offset(21);
    make.right.equalTo(self.typeView).offset(-17);
    make.width.mas_equalTo(100);
    make.height.mas_equalTo(35);
  }];

  [self.typeView addSubview:self.ntpBox];
  [self.ntpBox mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.aiBox.mas_bottom).offset(16);
    make.left.equalTo(self.typeView).offset(17);
    make.width.mas_equalTo(200);
    make.height.mas_equalTo(35);
  }];
}

#pragma mark - getter

- (UIView *)nameView {
  if (!_nameView) {
    _nameView = [[UIView alloc] init];
    _nameView.backgroundColor = [NEKaraokeDefine createSubBackground];
    _nameView.layer.cornerRadius = 8;
  }
  return _nameView;
}

- (NEKaraokeCreateTextField *)roomName {
  if (!_roomName) {
    _roomName = [self createTextFieldWithPlaceHolder:@"请输入房间名"];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(textChange)
                                                 name:UITextFieldTextDidChangeNotification
                                               object:_roomName];
  }
  return _roomName;
}

- (void)textChange {
  if (self.roomName.text.length > 20) {
    [NEKaraokeToast showToast:@"房间名最多支持20个字符"];
    self.roomName.text = [self.roomName.text substringToIndex:20];
  }
}

- (NEKaraokeCreateTextField *)hostName {
  if (!_hostName) {
    _hostName = [self createTextFieldWithPlaceHolder:@"请输入用户名"];
    _hostName.text = [NEKaraokeUIManager sharedInstance].nickname;
    _hostName.textColor = [UIColor colorWithWhite:1 alpha:0.5];
    _hostName.enabled = false;
  }
  return _hostName;
}

- (NEKaraokeCreateTextField *)createTextFieldWithPlaceHolder:(NSString *)placeHolder {
  NEKaraokeCreateTextField *textField = [[NEKaraokeCreateTextField alloc] init];
  textField.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
  textField.backgroundColor = [UIColor clearColor];
  textField.textColor = [UIColor whiteColor];
  textField.layer.borderWidth = 1.0;
  textField.layer.cornerRadius = 4;
  textField.layer.borderColor = [UIColor karaoke_colorWithHex:0x383A57].CGColor;
  textField.attributedPlaceholder = [[NSAttributedString alloc]
      initWithString:placeHolder
          attributes:@{NSForegroundColorAttributeName : [UIColor karaoke_colorWithHex:0x383A57]}];
  return textField;
}

- (UIView *)typeView {
  if (!_typeView) {
    _typeView = [[UIView alloc] init];
    _typeView.backgroundColor = [NEKaraokeDefine createSubBackground];
    _typeView.layer.cornerRadius = 8;
  }
  return _typeView;
}

- (NEKaraokeCreateCheckBox *)aiBox {
  if (!_aiBox) {
    _aiBox = [[NEKaraokeCreateCheckBox alloc] initWithFrame:self.view.frame];
    _aiBox.titleLabel.text = @"智能合唱";
    _aiBox.detailLabel.text = @"基于网络设备情况智能选择合唱策略";
    _aiBox.checked = true;
    @weakify(self) _aiBox.checkChanged = ^(bool isChecked) {
      if (!isChecked) {
        @strongify(self) self.aiBox.checked = true;
        self.serialBox.checked = false;
        self.ntpBox.checked = false;
      }
    };
  }
  return _aiBox;
}

- (NEKaraokeCreateCheckBox *)serialBox {
  if (!_serialBox) {
    _serialBox = [[NEKaraokeCreateCheckBox alloc] initWithFrame:self.view.frame];
    _serialBox.titleLabel.text = @"串行合唱";
    @weakify(self) _serialBox.checkChanged = ^(bool isChecked) {
      if (!isChecked) {
        @strongify(self) self.aiBox.checked = false;
        self.serialBox.checked = true;
        self.ntpBox.checked = false;
      }
    };
  }
  return _serialBox;
}

- (NEKaraokeCreateCheckBox *)ntpBox {
  if (!_ntpBox) {
    _ntpBox = [[NEKaraokeCreateCheckBox alloc] initWithFrame:self.view.frame];
    _ntpBox.titleLabel.text = @"NTP实时合唱";
    @weakify(self) _ntpBox.checkChanged = ^(bool isChecked) {
      if (!isChecked) {
        @strongify(self) self.aiBox.checked = false;
        self.serialBox.checked = false;
        self.ntpBox.checked = true;
      }
    };
  }
  return _ntpBox;
}

- (CAGradientLayer *)buttonBackground {
  if (!_buttonBackground) {
    _buttonBackground = [CAGradientLayer layer];
    NSArray *colors =
        [NSArray arrayWithObjects:(id)[[UIColor karaoke_colorWithHex:0xFF60AF] CGColor],
                                  (id)[[UIColor karaoke_colorWithHex:0xF96C6E] CGColor],
                                  (id)[[UIColor blackColor] CGColor], nil];
    [_buttonBackground setColors:colors];
    _buttonBackground.locations = @[ @0, @1 ];
    _buttonBackground.startPoint = CGPointMake(0.25, 0.5);
    _buttonBackground.endPoint = CGPointMake(0.75, 0.5);
    [_buttonBackground setFrame:CGRectMake(0, 0, CGRectGetWidth(self.createButton.frame),
                                           CGRectGetHeight(self.createButton.frame))];
    _buttonBackground.cornerRadius = 8;
  }
  return _buttonBackground;
}

- (UIButton *)createButton {
  if (!_createButton) {
    _createButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_createButton setTitle:@"创建" forState:UIControlStateNormal];
    [_createButton addTarget:self
                      action:@selector(createKaraoke)
            forControlEvents:UIControlEventTouchUpInside];
    _createButton.layer.cornerRadius = 8;
  }
  return _createButton;
}

- (void)createKaraoke {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:@"当前网络未连接"];
    return;
  }
  if (!self.roomName.text.length || !self.hostName.text.length) {
    [NEKaraokeToast showToast:@"请输入房间名与用户名"];
    return;
  }
  if (![NEKaraokeAuthorityHelper checkMicAuthority]) {  // 没有麦克风权限
    [NEKaraokeToast showToast:@"请开启麦克风权限"];
    return;
  }
  [NEKaraokeToast showLoading];
  NECreateKaraokeParams *params = [[NECreateKaraokeParams alloc] init];
  params.title = self.roomName.text;
  params.nick = self.hostName.text;
  params.seatCount = 7;
  if (self.aiBox.checked) {
    params.singMode = NEKaraokeSingModeAIChorus;
  } else if (self.serialBox.checked) {
    params.singMode = NEKaraokeSingModeSerialChorus;
  } else if (self.ntpBox.checked) {
    params.singMode = NEKaraokeSingModeNTPChorus;
  }
  self.createButton.enabled = false;
  [[NEKaraokeKit shared]
      createRoom:params
         options:[[NECreateKaraokeOptions alloc] init]
        callback:^(NSInteger code, NSString *_Nullable msg, NEKaraokeRoomInfo *_Nullable obj) {
          dispatch_async(dispatch_get_main_queue(), ^{
            [NEKaraokeToast hideLoading];
            self.createButton.enabled = true;
            if (code == 0) {
              NEKaraokeViewController *view =
                  [[NEKaraokeViewController alloc] initWithRole:NEKaraokeViewRoleHost detail:obj];
              [self.navigationController pushViewController:view animated:true];
            } else {
              [NEKaraokeToast
                  showToast:[NSString stringWithFormat:@"加入直播间失败 %zd %@", code, msg]];
            }
          });
        }];
}

- (BOOL)shouldAutorotate {
  return NO;
}
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
  return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
  return UIInterfaceOrientationPortrait;
}

@end
