// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeListViewController.h"
#import <MJRefresh/MJRefresh.h>
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEKaraokeCreateViewController.h"
#import "NEKaraokeDefine.h"
#import "NEKaraokeListEmptyView.h"
#import "NEKaraokeListViewCell.h"
#import "NEKaraokeLocalized.h"
#import "NEKaraokeReachability.h"
#import "NEKaraokeToast.h"
#import "NEKaraokeViewController.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"
@import NESocialUIKit;

@interface NEKaraokeListViewController () <UICollectionViewDelegate,
                                           UICollectionViewDataSource,
                                           NEKaraokeAuthListener>

@property(nonatomic, strong) UICollectionView *collectionView;
@property(nonatomic, strong) UIButton *createLiveRoomButton;
@property(nonatomic, strong) NSArray<NEKaraokeRoomInfo *> *data;
@property(nonatomic, strong) NESocialRoomListEmptyView *emptyView;
@property(nonatomic, assign) NSInteger pageNum;
@property(nonatomic, assign) bool noMore;

@property(nonatomic, strong) NEKaraokeReachability *reachability;

@end

@implementation NEKaraokeListViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.

  self.reachability = [NEKaraokeReachability reachabilityForInternetConnection];
  [self.reachability startNotifier];

  self.title = NELocalizedString(@"在线K歌");
  [self.navigationController.navigationBar setTitleTextAttributes:@{
    NSForegroundColorAttributeName : [UIColor karaoke_colorWithHex:0x333333]
  }];
  self.navigationController.navigationBar.backgroundColor = [UIColor whiteColor];
  self.view.backgroundColor = [UIColor whiteColor];

  [self.view addSubview:self.collectionView];
  [self.collectionView addSubview:self.emptyView];
  self.emptyView.center =
      CGPointMake(self.collectionView.center.x, self.collectionView.center.y - 100);
  [self.view addSubview:self.createLiveRoomButton];
  [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.bottom.equalTo(self.view);
    make.height.mas_equalTo([UIScreen mainScreen].bounds.size.height -
                            [UIApplication sharedApplication].statusBarFrame.size.height - 44);
  }];

  [self.createLiveRoomButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.equalTo(@44);
    make.right.equalTo(self.view).offset(-17);
    make.left.equalTo(self.view).offset(17);
    make.bottom.equalTo(self.view).offset(-25);
  }];

  [self bindAction];

  [[NEKaraokeKit shared] addAuthListener:self];
}

- (void)dealloc {
  [[NEKaraokeKit shared] removeAuthListener:self];
}

- (void)onKaraokeAuthEvent:(enum NEKaraokeAuthEvent)event {
  if (event == NEKaraokeAuthEventLoggedIn) {
    [self refreshList];
  }
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  [self.navigationController setNavigationBarHidden:NO animated:YES];
  [self refreshList];
}

- (void)refreshList {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
    [self.collectionView.mj_header endRefreshing];
    [self.collectionView.mj_footer endRefreshing];
    return;
  }
  if ([NEKaraokeKit shared].isLoggedIn) {
    self.pageNum = 1;
    [self getKaraokeRoomList];
  }
}

- (void)loadMore {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
    [self.collectionView.mj_header endRefreshing];
    [self.collectionView.mj_footer endRefreshing];
    return;
  }
  self.pageNum++;
  [self getKaraokeRoomList];
}

- (void)getKaraokeRoomList {
  [[NEKaraokeKit shared]
      getKaraokeRoomListWithLiveState:NEKaraokeLiveStateLive
                              pageNum:self.pageNum
                             pageSize:20
                             callback:^(NSInteger code, NSString *_Nullable msg,
                                        NEKaraokeRoomList *_Nullable data) {
                               dispatch_async(dispatch_get_main_queue(), ^{
                                 [self.collectionView.mj_header endRefreshing];
                                 [self.collectionView.mj_footer endRefreshing];
                                 if (code == 0) {
                                   if (self.pageNum == 1) {
                                     self.data = [NSArray arrayWithArray:data.list];
                                   } else {
                                     NSMutableArray *temp =
                                         [NSMutableArray arrayWithArray:self.data];
                                     [temp addObjectsFromArray:data.list];
                                     self.data = temp;
                                   }
                                   self.noMore = data.list.count < 20;
                                   self.emptyView.hidden = self.data.count;
                                   [self.collectionView reloadData];
                                   if ([self.collectionView.mj_header isRefreshing]) {
                                     [self.collectionView.mj_header endRefreshing];
                                   }
                                 } else {
                                   [NEKaraokeToast
                                       showToast:[NSString stringWithFormat:@"%@ %zd %@",
                                                                            NELocalizedString(
                                                                                @"查询列表失败"),
                                                                            code, msg]];
                                   [self.collectionView reloadData];
                                   self.emptyView.hidden = self.data.count;
                                   if ([self.collectionView.mj_header isRefreshing]) {
                                     [self.collectionView.mj_header endRefreshing];
                                   }
                                 }
                               });
                             }];
}

#pragma mark - MJ

- (void)bindAction {
  __weak typeof(self) weakSelf = self;
  ;
  MJRefreshGifHeader *mjHeader = [MJRefreshGifHeader headerWithRefreshingBlock:^{
    __strong typeof(weakSelf) self = weakSelf;
    ;
    [self refreshList];
  }];
  [mjHeader setTitle:NELocalizedString(@"下拉更新") forState:MJRefreshStateIdle];
  [mjHeader setTitle:NELocalizedString(@"下拉更新") forState:MJRefreshStatePulling];
  [mjHeader setTitle:NELocalizedString(@"更新中...") forState:MJRefreshStateRefreshing];
  mjHeader.lastUpdatedTimeLabel.hidden = YES;
  [mjHeader setTintColor:[UIColor whiteColor]];
  self.collectionView.mj_header = mjHeader;

  self.collectionView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingBlock:^{
    __strong typeof(weakSelf) self = weakSelf;
    ;
    if (self.noMore) {
      [NEKaraokeToast showToast:NELocalizedString(@"无更多内容")];
      [self.collectionView.mj_footer endRefreshing];
    } else {
      [self loadMore];
    }
  }];
}

#pragma mark - getter

- (UICollectionView *)collectionView {
  if (!_collectionView) {
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = [NEKaraokeListViewCell size];
    layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    layout.minimumInteritemSpacing = 8;
    layout.minimumLineSpacing = 8;
    layout.sectionInset = UIEdgeInsetsMake(8, 8, 8, 8);

    _collectionView = [[UICollectionView alloc] initWithFrame:self.view.bounds
                                         collectionViewLayout:layout];
    [_collectionView registerClass:[NEKaraokeListViewCell class]
        forCellWithReuseIdentifier:[NEKaraokeListViewCell description]];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
    _collectionView.showsVerticalScrollIndicator = NO;
    _collectionView.backgroundColor = [UIColor clearColor];
    if (@available(iOS 11.0, *)) {
      _collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
  }
  return _collectionView;
}

- (NESocialRoomListEmptyView *)emptyView {
  if (!_emptyView) {
    _emptyView = [[NESocialRoomListEmptyView alloc] initWithFrame:CGRectZero];
  }
  return _emptyView;
}

- (UIButton *)createLiveRoomButton {
  if (!_createLiveRoomButton) {
    _createLiveRoomButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_createLiveRoomButton setTitle:NELocalizedString(@"创建房间") forState:UIControlStateNormal];
    [_createLiveRoomButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    _createLiveRoomButton.backgroundColor = [UIColor colorWithRed:0.2 green:0.494 blue:1 alpha:1];
    [_createLiveRoomButton addTarget:self
                              action:@selector(createRoomAction)
                    forControlEvents:UIControlEventTouchUpInside];
    _createLiveRoomButton.layer.cornerRadius = 22;
  }
  return _createLiveRoomButton;
}

- (void)createRoomAction {
  NEKaraokeCreateViewController *create = [[NEKaraokeCreateViewController alloc] init];
  [self.navigationController pushViewController:create animated:true];
}

#pragma mark - UICollectionView delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return self.data.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                  cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  return [NEKaraokeListViewCell cellWithCollectionView:collectionView
                                             indexPath:indexPath
                                                 datas:self.data];
}

- (void)collectionView:(UICollectionView *)collectionView
    didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
    return;
  }

  if ([NEKaraokeUIManager sharedInstance].canContinueAction &&
      ![NEKaraokeUIManager sharedInstance].canContinueAction()) {
    return;
  };

  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"karaokeEnter" object:nil]];
  NEKaraokeRoomInfo *roomInfoModel = self.data[indexPath.row];
  if ([NEKaraokeUIManager.sharedInstance.delegate respondsToSelector:@selector(inOtherRoom)] &&
      [NEKaraokeUIManager.sharedInstance.delegate inOtherRoom]) {
    // 已经在其他房间中，比如语聊房
    UIAlertController *alert = [UIAlertController
        alertControllerWithTitle:NELocalizedString(@"提示")
                         message:NELocalizedString(@"是否退出当前房间进入其他房间")
                  preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:NELocalizedString(@"取消")
                                              style:UIAlertActionStyleCancel
                                            handler:nil]];
    [alert addAction:[UIAlertAction actionWithTitle:NELocalizedString(@"确认")
                                              style:UIAlertActionStyleDefault
                                            handler:^(UIAlertAction *_Nonnull action) {
                                              if ([NEKaraokeUIManager.sharedInstance.delegate
                                                      respondsToSelector:@selector
                                                      (leaveOtherRoomWithCompletion:)]) {
                                                [NEKaraokeUIManager.sharedInstance.delegate
                                                    leaveOtherRoomWithCompletion:^{
                                                      dispatch_async(dispatch_get_main_queue(), ^{
                                                        [self joinRoom:roomInfoModel];
                                                      });
                                                    }];
                                              }
                                            }]];
    [self presentViewController:alert animated:true completion:nil];
  } else {
    [self joinRoom:roomInfoModel];
  }
}

- (void)joinRoom:(NEKaraokeRoomInfo *)roomInfoModel {
  NEKaraokeViewController *view =
      [[NEKaraokeViewController alloc] initWithRole:NEKaraokeViewRoleAudience detail:roomInfoModel];
  [self.navigationController pushViewController:view animated:true];
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
