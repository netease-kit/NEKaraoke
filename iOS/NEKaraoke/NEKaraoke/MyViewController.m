// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "MyViewController.h"
#import <Masonry/Masonry.h>
#import <NEKaraokeUIKit/NEKaraokeUIManager.h>
#import <YXLogin/YXLogin.h>
#import "NEWebViewController.h"

@interface MyViewController () <UITableViewDelegate,
                                UITableViewDataSource,
                                UINavigationControllerDelegate>

@property(nonatomic, strong) UIImageView *iconImage;
@property(nonatomic, strong) UILabel *nameLabel;
@property(nonatomic, strong) UITableView *tableView;
@property(nonatomic, strong) UILabel *versionLabel;

@property(nonatomic, strong) NSArray *titles;
@property(nonatomic, strong) NSArray *icons;

@end

@implementation MyViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.

  if (@available(iOS 13.0, *)) {
    self.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
  }

  self.titles = @[ @"用户协议", @"隐私政策", @"了解云信" ];
  self.icons = @[ @"protol_ico", @"private_ico", @"info_ico" ];

  [NSNotificationCenter.defaultCenter addObserver:self
                                         selector:@selector(update)
                                             name:@"Login"
                                           object:nil];

  self.view.backgroundColor = [UIColor whiteColor];
  self.navigationController.delegate = self;

  UIView *headerView = [[UIView alloc] init];
  [self.view addSubview:headerView];
  [headerView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.equalTo(self.view);
    make.height.mas_equalTo(230);
  }];

  UIImageView *back = [[UIImageView alloc] init];
  back.image = [UIImage imageNamed:@"my_background"];
  [headerView addSubview:back];
  [back mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.equalTo(headerView);
    make.height.mas_equalTo(140);
  }];

  [headerView addSubview:self.iconImage];
  [self.iconImage mas_makeConstraints:^(MASConstraintMaker *make) {
    make.width.height.mas_equalTo(60);
    make.top.equalTo(back.mas_bottom).offset(-30);
    make.left.equalTo(headerView).offset(26);
  }];

  [headerView addSubview:self.nameLabel];
  [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(headerView).offset(14);
    make.top.equalTo(self.iconImage.mas_bottom).offset(12);
    make.right.bottom.equalTo(headerView);
  }];

  [self.view addSubview:self.tableView];
  [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(headerView.mas_bottom).offset(30);
    make.left.right.equalTo(self.view);
    if (@available(iOS 11.0, *)) {
      make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
    } else {
      make.bottom.equalTo(self.view);
    }
  }];

  [self.view addSubview:self.versionLabel];
  [self.versionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.view);
    if (@available(iOS 11.0, *)) {
      make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).offset(-10);
    } else {
      make.bottom.equalTo(self.view).offset(-10);
    }
  }];
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  [self update];
}

- (void)update {
  if ([AuthorManager shareInstance].isLogin) {
    YXUserInfo *info = [[AuthorManager shareInstance] getUserInfo];
    self.iconImage.image =
        [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:info.avatar]]];
    self.nameLabel.text = info.nickname;
  }
}

- (UIImageView *)iconImage {
  if (!_iconImage) {
    _iconImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"default_icon"]];
    _iconImage.clipsToBounds = true;
    _iconImage.layer.borderWidth = 1.0;
    _iconImage.layer.cornerRadius = 30;
    _iconImage.layer.borderColor = [UIColor whiteColor].CGColor;
  }
  return _iconImage;
}

- (UILabel *)nameLabel {
  if (!_nameLabel) {
    _nameLabel = [[UILabel alloc] init];
    _nameLabel.textColor = [UIColor colorWithRed:0.2 green:0.2 blue:0.2 alpha:1];
    _nameLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:22];
    _nameLabel.text = @"Nicaldai";
  }
  return _nameLabel;
}

- (UILabel *)versionLabel {
  if (!_versionLabel) {
    _versionLabel = [[UILabel alloc] init];
    _versionLabel.textColor = [UIColor colorWithRed:0.2 green:0.2 blue:0.2 alpha:0.5];
    _versionLabel.textAlignment = NSTextAlignmentCenter;
    NSDictionary *dict = [[NSBundle mainBundle] infoDictionary];
    NSString *version = [dict objectForKey:@"CFBundleShortVersionString"];
    NSString *build = [dict objectForKey:@"CFBundleVersion"];
    _versionLabel.text = [NSString stringWithFormat:@"version: %@ build: %@", version, build];
  }
  return _versionLabel;
}

- (UITableView *)tableView {
  if (!_tableView) {
    _tableView = [UITableView new];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.scrollEnabled = false;
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
    [btn setTitle:@"退出登录" forState:UIControlStateNormal];
    [btn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    btn.frame = CGRectMake(0, 0, self.view.frame.size.width, 80);
    [btn addTarget:self action:@selector(logout) forControlEvents:UIControlEventTouchUpInside];
    _tableView.tableFooterView = btn;
  }
  return _tableView;
}

- (void)logout {
  [[AuthorManager shareInstance]
      logoutWithConfirm:nil
         withCompletion:^(YXUserInfo *_Nullable userinfo, NSError *_Nullable error) {
           if (error == nil) {
             [[NEKaraokeUIManager sharedInstance] logoutWithCallback:^(NSInteger code,
                                                                       NSString *_Nullable msg,
                                                                       id _Nullable obj) {
               if (code == 0) {
                 dispatch_async(dispatch_get_main_queue(), ^{
                   [[AuthorManager shareInstance] startLoginWithCompletion:^(
                                                      YXUserInfo *_Nullable userinfo,
                                                      NSError *_Nullable error) {
                     [NEKaraokeUIManager.sharedInstance
                         loginWithAccount:userinfo.accountId
                                    token:userinfo.accessToken
                                 nickname:userinfo.nickname
                                 callback:^(NSInteger code, NSString *_Nullable msg,
                                            id _Nullable objc) {
                                   dispatch_async(dispatch_get_main_queue(), ^{
                                     self.iconImage.image = [UIImage
                                         imageWithData:[NSData
                                                           dataWithContentsOfURL:
                                                               [NSURL
                                                                   URLWithString:userinfo.avatar]]];
                                     self.nameLabel.text = userinfo.nickname;
                                   });
                                 }];
                   }];
                 });
               }
             }];
           } else {
           }
         }];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  [tableView deselectRowAtIndexPath:indexPath animated:false];
  NSString *urlString = @"";
  switch (indexPath.row) {
    case 0:
      urlString = @"https://yunxin.163.com/clauses";
      break;
    case 1:
      urlString = @"https://yunxin.163.com/clauses?serviceType=3";
      break;
    case 2:
      urlString = @"https://netease.im/";
      break;
    default:
      break;
  }

  NEWebViewController *web = [[NEWebViewController alloc] initWithUrlString:urlString];
  web.title = self.titles[indexPath.row];
  web.hidesBottomBarWhenPushed = true;
  [self.navigationController pushViewController:web animated:true];
}

- (nonnull UITableViewCell *)tableView:(nonnull UITableView *)tableView
                 cellForRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"UITableViewCell"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                  reuseIdentifier:@"UITableViewCell"];
  }
  cell.textLabel.text = self.titles[indexPath.row];
  cell.imageView.image = [UIImage imageNamed:self.icons[indexPath.row]];
  cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
  return cell;
}

- (NSInteger)tableView:(nonnull UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return self.titles.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 52;
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

#pragma mark - UINavigationControllerDelegate
- (void)navigationController:(UINavigationController *)navigationController
      willShowViewController:(UIViewController *)viewController
                    animated:(BOOL)animated {
  // 隐藏导航栏
  [self.navigationController setNavigationBarHidden:[viewController isKindOfClass:[self class]]
                                           animated:YES];
}

@end
