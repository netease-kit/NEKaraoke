// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatListVC.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeSeatListCell.h"
@import NEKaraokeKit;
@interface NEKaraokeSeatListVC () <UITableViewDelegate, UITableViewDataSource>
@property(nonatomic, strong) UITableView *tableView;
@property(nonatomic, strong) NSMutableArray *datas;
@end

@implementation NEKaraokeSeatListVC

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.
  self.title = @"申请上麦(0)";
  [self.navigationController.navigationBar
      setTitleTextAttributes:@{NSForegroundColorAttributeName : UIColor.whiteColor}];

  UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleDark];
  UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:effect];
  effectView.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:0.5];
  [self.view addSubview:effectView];
  [effectView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.bottom.equalTo(self.view);
  }];

  // 导航栏下面画个分割线
  CAShapeLayer *lineLayer = [[CAShapeLayer alloc] init];
  lineLayer.strokeColor = [UIColor colorWithRed:0.371 green:0.371 blue:0.371 alpha:0.3].CGColor;
  lineLayer.lineWidth = 1.0;
  CGMutablePathRef path = CGPathCreateMutable();
  CGPathMoveToPoint(path, NULL, 0, 49);
  CGPathAddLineToPoint(path, NULL, self.view.frame.size.width, 49);
  lineLayer.path = path;
  [self.view.layer addSublayer:lineLayer];

  self.datas = @[].mutableCopy;
  [self setupSubviews];
  [self loadData];
}
- (void)setupSubviews {
  [self.view addSubview:self.tableView];
  self.tableView.backgroundColor = UIColor.clearColor;
  [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.mas_equalTo(UIEdgeInsetsMake(49, 0, 0, 0));
  }];
}
- (void)loadData {
  [NEKaraokeKit.shared getSeatRequestList:^(NSInteger code, NSString *_Nullable msg,
                                            NSArray<NEKaraokeSeatRequestItem *> *_Nullable items) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.datas addObjectsFromArray:items];
      self.title = [NSString stringWithFormat:@"申请上麦(%ld)", self.datas.count];
      [self.tableView reloadData];
    });
  }];
}
#pragma mark-----------------------------  UITableViewDataSource and  UITableViewDelegate -----------------------------
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return self.datas.count;
}
- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  NEKaraokeSeatListCell *cell =
      [tableView dequeueReusableCellWithIdentifier:@"NEKaraokeSeatListCell" forIndexPath:indexPath];
  NEKaraokeSeatRequestItem *item = self.datas[indexPath.row];
  BOOL isSelf = [item.user isEqualToString:NEKaraokeKit.shared.localMember.account];
  [cell configCellWithItem:self.datas[indexPath.row]
                     index:indexPath.row
                  isAnchor:self.isHost
                    isSelf:isSelf];
  __weak typeof(self) weakSelf = self;
  cell.cancelBlock = ^{  // 取消申请
    if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(cancelRequestSeat:)]) {
      [weakSelf.delegate cancelRequestSeat:item];
    }
  };
  cell.rejectBlock = ^{  // 拒绝
    if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(rejectRequestSeat:)]) {
      [weakSelf.delegate rejectRequestSeat:item];
    }
  };
  cell.allowBlock = ^{  // 允许
    if (weakSelf.delegate && [weakSelf.delegate respondsToSelector:@selector(allowRequestSeat:)]) {
      [weakSelf.delegate allowRequestSeat:item];
    }
  };
  return cell;
}
- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
  return 0.01;
}
- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
  return 0.01;
}
#pragma mark - Present Size
- (CGFloat)contentViewHeight {
  //_contentHeight
  CGFloat total = 300;
  if (@available(iOS 11.0, *)) {
    total +=
        [UIApplication sharedApplication].keyWindow.rootViewController.view.safeAreaInsets.bottom;
  }
  return total;
}

- (CGSize)preferredContentSize {
  return CGSizeMake(CGRectGetWidth([UIScreen mainScreen].bounds), [self contentViewHeight]);
}

#pragma mark-----------------------------  Getter  -----------------------------
- (UITableView *)tableView {
  if (!_tableView) {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStyleGrouped];
    _tableView.dataSource = self;
    _tableView.delegate = self;
    _tableView.backgroundColor = UIColor.clearColor;
    [_tableView registerClass:NEKaraokeSeatListCell.class
        forCellReuseIdentifier:@"NEKaraokeSeatListCell"];
  }
  return _tableView;
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
