// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeListViewController.h"
#import <MJRefresh/MJRefresh.h>
#import <libextobjc/extobjc.h>
#import "NEKaraokeDefine.h"
#import "NEKaraokeListEmptyView.h"
#import "NEKaraokeListViewCell.h"
#import "NEKaraokeReachability.h"
#import "NEKaraokeToast.h"
#import "NEKaraokeViewController.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"

@interface NEKaraokeListViewController () <UICollectionViewDelegate,
                                           UICollectionViewDataSource,
                                           NEKaraokeAuthListener>

@property(nonatomic, strong) UICollectionView *collectionView;
@property(nonatomic, strong) NSArray<NEKaraokeRoomInfo *> *data;
@property(nonatomic, strong) NEKaraokeListEmptyView *emptyView;

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

  self.title = @"在线K歌";
  [self.navigationController.navigationBar setTitleTextAttributes:@{
    NSForegroundColorAttributeName : [UIColor karaoke_colorWithHex:0x333333]
  }];
  self.navigationController.navigationBar.backgroundColor = [UIColor whiteColor];
  self.view.backgroundColor = [UIColor whiteColor];

  [self.view addSubview:self.collectionView];

  [self.collectionView addSubview:self.emptyView];
  self.emptyView.center = self.collectionView.center;

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

- (void)close {
  [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  [self refreshList];
}

- (void)refreshList {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:@"当前网络未连接"];
    if ([self.collectionView.mj_header isRefreshing]) {
      [self.collectionView.mj_header endRefreshing];
    }
    self.data = [NSArray new];
    [self.collectionView reloadData];
    return;
  }
  if ([NEKaraokeKit shared].isLoggedIn) {
    self.pageNum = 1;
    [self getKaraokeRoomList];
  }
}

- (void)loadMore {
  if (![self.reachability isReachable]) {
    [NEKaraokeToast showToast:@"当前网络未连接"];
    if ([self.collectionView.mj_header isRefreshing]) {
      [self.collectionView.mj_header endRefreshing];
    }
    self.data = [NSArray new];
    [self.collectionView reloadData];
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
                                       showToast:[NSString stringWithFormat:@"查询列表失败 %zd %@",
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
  @weakify(self);
  MJRefreshGifHeader *mjHeader = [MJRefreshGifHeader headerWithRefreshingBlock:^{
    @strongify(self);
    [self refreshList];
  }];
  [mjHeader setTitle:@"下拉更新" forState:MJRefreshStateIdle];
  [mjHeader setTitle:@"下拉更新" forState:MJRefreshStatePulling];
  [mjHeader setTitle:@"更新中..." forState:MJRefreshStateRefreshing];
  mjHeader.lastUpdatedTimeLabel.hidden = YES;
  [mjHeader setTintColor:[UIColor whiteColor]];
  self.collectionView.mj_header = mjHeader;

  self.collectionView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingBlock:^{
    @strongify(self);
    if (self.noMore) {
      [NEKaraokeToast showToast:@"无更多内容"];
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
    CGRect rect = CGRectMake(0, 0, CGRectGetWidth(self.view.frame),
                             CGRectGetHeight([UIScreen mainScreen].bounds));
    if (@available(iOS 11.0, *)) {
      bool kIsFullScreen = UIApplication.sharedApplication.keyWindow.safeAreaInsets.bottom > 0.0;
      rect = CGRectMake(0, 0, CGRectGetWidth(self.view.frame),
                        CGRectGetHeight([UIScreen mainScreen].bounds) - (kIsFullScreen ? 34 : 0));
    }
    _collectionView = [[UICollectionView alloc] initWithFrame:rect collectionViewLayout:layout];
    [_collectionView registerClass:[NEKaraokeListViewCell class]
        forCellWithReuseIdentifier:[NEKaraokeListViewCell description]];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
    _collectionView.showsVerticalScrollIndicator = NO;
    _collectionView.backgroundColor = [UIColor clearColor];
  }
  return _collectionView;
}

- (NEKaraokeListEmptyView *)emptyView {
  if (!_emptyView) {
    _emptyView = [[NEKaraokeListEmptyView alloc] initWithFrame:CGRectZero];
    _emptyView.hidden = false;
  }
  return _emptyView;
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
    [NEKaraokeToast showToast:@"网络异常"];
    return;
  }
  NEKaraokeRoomInfo *live = self.data[indexPath.row];
  NEKaraokeViewController *view =
      [[NEKaraokeViewController alloc] initWithRole:NEKaraokeViewRoleAudience detail:live];
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
