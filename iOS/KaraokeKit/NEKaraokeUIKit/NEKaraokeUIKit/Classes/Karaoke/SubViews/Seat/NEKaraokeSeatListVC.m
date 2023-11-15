// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatListVC.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeLocalized.h"
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
  self.title = [NSString stringWithFormat:@"%@(0)", NELocalizedString(@"申请上麦")];
  [self.navigationController.navigationBar
      setTitleTextAttributes:@{NSForegroundColorAttributeName : UIColor.whiteColor}];

  self.view.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:1];
  self.datas = @[].mutableCopy;
  [self setupSubviews];
  [self loadData];
}
- (void)setupSubviews {
  [self.view addSubview:self.tableView];
  self.tableView.backgroundColor = UIColor.clearColor;
  [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.mas_equalTo(UIEdgeInsetsMake(0, 0, 0, 0));
  }];
}
- (void)loadData {
  [NEKaraokeKit.shared getSeatRequestList:^(NSInteger code, NSString *_Nullable msg,
                                            NSArray<NEKaraokeSeatRequestItem *> *_Nullable items) {
    dispatch_async(dispatch_get_main_queue(), ^{
      [self.datas addObjectsFromArray:items];
      self.title =
          [NSString stringWithFormat:@"%@(%ld)", NELocalizedString(@"申请上麦"), self.datas.count];
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
    __strong typeof(self) strongSelf = weakSelf;
    if (strongSelf.delegate &&
        [strongSelf.delegate respondsToSelector:@selector(cancelRequestSeat:)]) {
      [strongSelf.delegate cancelRequestSeat:item];
    }
  };
  cell.rejectBlock = ^{  // 拒绝
    __strong typeof(self) strongSelf = weakSelf;
    if (strongSelf.delegate &&
        [strongSelf.delegate respondsToSelector:@selector(rejectRequestSeat:)]) {
      [strongSelf.delegate rejectRequestSeat:item];
    }
  };
  cell.allowBlock = ^{  // 允许
    __strong typeof(self) strongSelf = weakSelf;
    if (strongSelf.delegate &&
        [strongSelf.delegate respondsToSelector:@selector(allowRequestSeat:)]) {
      [strongSelf.delegate allowRequestSeat:item];
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
