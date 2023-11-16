// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokePickSongView.h"
#import <MJRefresh/MJRefresh.h>
#import <Masonry/Masonry.h>
#import <NEKaraokeKit/NEKaraokeKit-Swift.h>
#import <SDWebImage/SDWebImage.h>
#import "NEKaraokeLocalized.h"
#import "NEKaraokePickSongColorDefine.h"
#import "NEKaraokePickSongEngine.h"
#import "NEKaraokePointSongTableViewCell.h"
#import "NEKaraokePointedSongTableViewCell.h"
#import "NEKaraokeSongEmptyView.h"
#import "NEKaraokeSongLog.h"
#import "NEKaraokeToast.h"
#import "UIColor+Karaoke.h"
#import "UIImage+Karaoke.h"

@interface NEKaraokePickSongView () <UITableViewDelegate,
                                     UITableViewDataSource,
                                     NESongPreloadProtocol,
                                     NESongPointProtocol,
                                     UITextFieldDelegate>
// 顶部切换视图
@property(nonatomic, strong) UIView *mainTopView;
// 搜索父视图
@property(nonatomic, strong) UIView *searchMainView;
// 搜索子视图
@property(nonatomic, strong) UIView *searchSuperView;
// 搜索TextFiled
@property(nonatomic, strong) UITextField *searchTextField;
// 是否处于搜索中
@property(nonatomic, assign) BOOL isSearching;
// 搜索图标
@property(nonatomic, strong) UIImageView *searchImageView;
// 搜索清空按钮
@property(nonatomic, strong) UIButton *searchClearButton;

@property(nonatomic, strong) UITableView *pickSongsTableView;
@property(nonatomic, strong) UITableView *pickedSongsTableView;
// 点歌按钮
@property(nonatomic, strong) UIButton *pickSongButton;
// 已点按钮
@property(nonatomic, strong) UIButton *pickedSongButton;

// button底部light
@property(nonatomic, strong) UILabel *lightLabel;

// 当前是否选中点歌菜单的记录
@property(nonatomic, assign) bool pointButtonSelected;

@property(nonatomic, strong) NEKaraokeRoomInfo *detail;

@property(nonatomic, strong) NEKaraokeSongEmptyView *emptyView;

// 当前点歌数据：用于麦位申请的时候
@property(nonatomic, strong) NEKaraokeSongItem *currentOrderSong;

@end
@implementation NEKaraokePickSongView

- (instancetype)initWithFrame:(CGRect)frame detail:(NEKaraokeRoomInfo *)detail {
  self = [super initWithFrame:frame];
  if (self) {
    [self initPickSongView];
    [self refreshData];
    self.detail = detail;
    [SDImageCache sharedImageCache].config.maxMemoryCost = 1024 * 1024 * 100;
    [SDImageCache sharedImageCache].config.maxMemoryCount = 20;
    //        [SDImageCache sharedImageCache].config.shouldDecompressImages = 20;
    //        [[NEKaraoSongEngine getInstance] addKaraokeSongProtocolObserve:self];
    [[NEKaraokePickSongEngine sharedInstance] addObserve:self];
  }
  return self;
}

- (void)refreshData {
  [[NEKaraokePickSongEngine sharedInstance] updateSongArray];
  __weak typeof(self) weakSelf = self;
  [[NEKaraokePickSongEngine sharedInstance] getKaraokeSongList:^(NSError *_Nullable error) {
    if (error) {
      [NEKaraokeToast showToast:NELocalizedString(@"获取歌曲列表失败")];
    } else {
      __strong typeof(weakSelf) self = weakSelf;
      __weak typeof(self) weakSelf = self;
      dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(weakSelf) self = weakSelf;
        [self.pickSongsTableView reloadData];
        [[NEKaraokePickSongEngine sharedInstance] updatePageNumber:NO];
      });
    }
  }];
  [[NEKaraokePickSongEngine sharedInstance] getKaraokeSongOrderedList:^(NSError *_Nullable error) {
    __strong typeof(weakSelf) self = weakSelf;
    __weak typeof(self) weakSelf = self;
    if (error) {
      [NEKaraokeToast showToast:NELocalizedString(@"获取已点列表失败")];
    } else {
      __strong typeof(weakSelf) self = weakSelf;
      __weak typeof(self) weakSelf = self;
      dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(weakSelf) self = weakSelf;
        [self.pickedSongButton
            setTitle:[NSString stringWithFormat:NELocalizedString(@"已点(%lu)"),
                                                [NEKaraokePickSongEngine sharedInstance]
                                                    .pickedSongArray.count]
            forState:UIControlStateNormal];
        if (!self.pointButtonSelected) {
          self.emptyView.hidden = [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count;
        }
        [self.pickedSongsTableView reloadData];
      });
    }
  }];
}
- (void)initPickSongView {
  self.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:1];
  // 顶部视图
  self.mainTopView = [[UIButton alloc] init];
  [self addSubview:self.mainTopView];
  [self.mainTopView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.equalTo(self);
    make.height.equalTo(@50);
  }];
  self.pickSongButton = [[UIButton alloc] init];
  [self.pickSongButton setTitle:NELocalizedString(@"点歌") forState:UIControlStateNormal];
  [self.pickSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:1]
                            forState:UIControlStateNormal];
  [self.pickSongButton addTarget:self
                          action:@selector(clickPickButton:)
                forControlEvents:UIControlEventTouchUpInside];
  self.pickSongButton.titleLabel.font = [UIFont systemFontOfSize:16];
  [self.mainTopView addSubview:self.pickSongButton];
  [self.pickSongButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.mainTopView);
    make.right.equalTo(self.mainTopView.mas_centerX).offset(-12);
  }];

  self.pickedSongButton = [[UIButton alloc] init];
  [self.pickedSongButton
      setTitle:[NSString
                   stringWithFormat:NELocalizedString(@"已点(%lu)"),
                                    [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count]
      forState:UIControlStateNormal];
  [self.pickedSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:0.5]
                              forState:UIControlStateNormal];
  [self.pickedSongButton addTarget:self
                            action:@selector(clickPickedButton:)
                  forControlEvents:UIControlEventTouchUpInside];
  self.pickedSongButton.titleLabel.font = [UIFont systemFontOfSize:16];
  [self.mainTopView addSubview:self.pickedSongButton];
  [self.pickedSongButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.mainTopView);
    make.left.equalTo(self.mainTopView.mas_centerX).offset(12);
  }];

  self.lightLabel = [[UILabel alloc] init];
  self.lightLabel.backgroundColor = [UIColor whiteColor];
  self.lightLabel.layer.cornerRadius = 2;
  self.lightLabel.layer.masksToBounds = YES;
  [self.mainTopView addSubview:self.lightLabel];
  [self.lightLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mainTopView);
    make.height.equalTo(@2);
    make.width.equalTo(@19);
    make.centerX.equalTo(self.pickSongButton.mas_centerX);
  }];

  UILabel *topViewBottomLightLabel = [[UILabel alloc] init];
  topViewBottomLightLabel.backgroundColor = [UIColor karaoke_colorWithHex:color_5F5F5F];
  [self addSubview:topViewBottomLightLabel];
  [topViewBottomLightLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.mainTopView.mas_bottom);
    make.height.equalTo(@1);
    make.left.right.equalTo(self);
  }];

  _pointButtonSelected = YES;

  // searchMain
  self.searchMainView = [[UIView alloc] init];
  [self addSubview:self.searchMainView];
  [self.searchMainView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(topViewBottomLightLabel.mas_bottom).offset(13);
    make.left.right.equalTo(self);
    make.height.equalTo(@48);
  }];

  self.searchSuperView = [[UIView alloc] init];
  self.searchSuperView.layer.masksToBounds = YES;
  self.searchSuperView.layer.cornerRadius = 16;
  self.searchSuperView.backgroundColor = [UIColor karaoke_colorWithHex:0x24302F];
  [self.searchMainView addSubview:self.searchSuperView];
  [self.searchSuperView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.searchMainView);
    make.left.equalTo(self.searchMainView).offset(20);
    make.right.equalTo(self.searchMainView).offset(-20);
    make.height.equalTo(@32);
  }];

  self.searchImageView =
      [[UIImageView alloc] initWithImage:[UIImage karaoke_imageNamed:@"icon_search"]];
  [self.searchSuperView addSubview:self.searchImageView];
  [self.searchImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.searchSuperView).offset(17);
    make.centerY.equalTo(self.searchSuperView);
    make.width.height.equalTo(@15);
  }];

  self.searchTextField = [[UITextField alloc] init];
  self.searchTextField.backgroundColor = [UIColor clearColor];
  NSAttributedString *attrString = [[NSAttributedString alloc]
      initWithString:NELocalizedString(@"搜索")
          attributes:@{
            NSForegroundColorAttributeName : [UIColor karaoke_colorWithHex:color_FFFFFF alpha:0.6],
            NSFontAttributeName : [UIFont systemFontOfSize:16]
          }];

  self.searchTextField.attributedPlaceholder = attrString;
  if ([self.searchTextField respondsToSelector:@selector(setReturnKeyType:)]) {
    self.searchTextField.returnKeyType = UIReturnKeySearch;
  }
  self.searchTextField.delegate = self;
  self.searchTextField.textColor = [UIColor whiteColor];
  [self.searchSuperView addSubview:self.searchTextField];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(textfieldDidChangeValue:)
                                               name:UITextFieldTextDidChangeNotification
                                             object:nil];
  [self.searchTextField mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.searchImageView.mas_right).offset(8);
    make.right.equalTo(self.searchSuperView).offset(-25);
    make.top.bottom.equalTo(self.searchSuperView);
  }];

  self.searchClearButton = [[UIButton alloc] init];
  [self.searchSuperView addSubview:self.searchClearButton];
  [self.searchClearButton setBackgroundImage:[UIImage karaoke_imageNamed:@"icon_search_cancel"]
                                    forState:UIControlStateNormal];
  self.searchClearButton.titleLabel.textAlignment = NSTextAlignmentCenter;
  self.searchClearButton.layer.masksToBounds = YES;
  self.searchClearButton.layer.cornerRadius = 8;
  [self.searchClearButton addTarget:self
                             action:@selector(clickSearchClearButton:)
                   forControlEvents:UIControlEventTouchUpInside];
  self.searchClearButton.hidden = YES;
  [self.searchClearButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.searchSuperView).offset(-5);
    make.centerY.equalTo(self.searchSuperView);
    make.height.width.equalTo(@20);
  }];

  // picksongstableView
  self.pickSongsTableView = [[UITableView alloc] init];
  [self.pickSongsTableView registerClass:[NEKaraokePointSongTableViewCell class]
                  forCellReuseIdentifier:@"Identifier"];

  self.pickSongsTableView.delegate = self;
  self.pickSongsTableView.dataSource = self;
  self.pickSongsTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
  self.pickSongsTableView.backgroundColor = [UIColor clearColor];
  [self addSubview:self.pickSongsTableView];
  [self.pickSongsTableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.searchMainView.mas_bottom).offset(8);
    make.left.bottom.right.equalTo(self);
  }];

  // pickedSongsTableView
  self.pickedSongsTableView = [[UITableView alloc] init];
  [self.pickedSongsTableView registerClass:[NEKaraokePointedSongTableViewCell class]
                    forCellReuseIdentifier:@"Identifier2"];

  self.pickedSongsTableView.delegate = self;
  self.pickedSongsTableView.dataSource = self;
  self.pickedSongsTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
  self.pickedSongsTableView.backgroundColor = [UIColor clearColor];
  [self addSubview:self.pickedSongsTableView];
  [self.pickedSongsTableView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.searchMainView.mas_bottom);
    make.left.bottom.right.equalTo(self);
  }];

  self.pickedSongsTableView.hidden = YES;

  self.emptyView = [[NEKaraokeSongEmptyView alloc] init];
  [self addSubview:self.emptyView];
  [self.emptyView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(topViewBottomLightLabel.mas_bottom);
    make.left.bottom.right.equalTo(self);
  }];

  self.emptyView.hidden = YES;

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
  self.pickSongsTableView.mj_header = mjHeader;

  self.pickSongsTableView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingBlock:^{
    __strong typeof(weakSelf) self = weakSelf;
    ;
    if ([NEKaraokePickSongEngine sharedInstance].noMore) {
      [NEKaraokeToast showToast:NELocalizedString(@"无更多内容")];
      [self.pickSongsTableView.mj_footer endRefreshing];
    } else {
      [self loadMore];
    }
  }];
}

- (void)refreshList {
  [[NEKaraokePickSongEngine sharedInstance] updateSongArray];
  if (self.isSearching) {
    [NEKaraokePickSongEngine sharedInstance].searchPageNum = 0;
    [self getKaraokeSearchSongsList];
  } else {
    [NEKaraokePickSongEngine sharedInstance].pageNum = 0;
    [self getKaraokeSongsList];
  }
}

- (void)loadMore {
  if (self.isSearching) {
    [self getKaraokeSearchSongsList];
  } else {
    [self getKaraokeSongsList];
  }
}

- (void)getKaraokeSongsList {
  __weak typeof(self) weakSelf = self;
  ;
  [[NEKaraokePickSongEngine sharedInstance] getKaraokeSongList:^(NSError *_Nullable error) {
    if (error) {
      dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(weakSelf) self = weakSelf;
        ;
        [self.pickSongsTableView.mj_header endRefreshing];
        [self.pickSongsTableView.mj_footer endRefreshing];
        [NEKaraokeToast showToast:NELocalizedString(@"获取歌曲列表失败")];
        if ([self.pickSongsTableView.refreshControl isRefreshing]) {
          [self.pickSongsTableView.refreshControl endRefreshing];
        }
      });
    } else {
      __strong typeof(weakSelf) self = weakSelf;
      ;
      __weak typeof(self) weakSelf = self;
      ;
      dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(weakSelf) self = weakSelf;
        ;
        [self.pickSongsTableView.mj_header endRefreshing];
        [self.pickSongsTableView.mj_footer endRefreshing];
        {
          if (self.pointButtonSelected) {
            self.emptyView.hidden = YES;
          } else {
            self.emptyView.hidden = [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count;
          }
          [[NEKaraokePickSongEngine sharedInstance] updatePageNumber:NO];
          [self.pickSongsTableView reloadData];
          if ([self.pickSongsTableView.refreshControl isRefreshing]) {
            [self.pickSongsTableView.refreshControl endRefreshing];
          }
        }
      });
    }
  }];
}

// click 事件
- (void)clickPickButton:(UIButton *)sender {
  self.emptyView.hidden = YES;
  [self.pickSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:1]
                            forState:UIControlStateNormal];
  [self.pickedSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:0.5]
                              forState:UIControlStateNormal];
  [self.lightLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mainTopView);
    make.height.equalTo(@2);
    make.width.equalTo(@19);
    make.centerX.equalTo(self.pickSongButton.mas_centerX);
  }];
  if (_pointButtonSelected != YES) {
    _pointButtonSelected = YES;
  }
  self.emptyView.hidden = YES;
  self.pickSongsTableView.hidden = NO;
  self.pickedSongsTableView.hidden = YES;
}

- (void)clickPickedButton:(UIButton *)sender {
  [self.pickSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:0.5]
                            forState:UIControlStateNormal];
  [self.pickedSongButton setTitleColor:[UIColor karaoke_colorWithHex:color_FFFFFF alpha:1]
                              forState:UIControlStateNormal];
  [self.lightLabel mas_remakeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mainTopView);
    make.height.equalTo(@2);
    make.width.equalTo(@19);
    make.centerX.equalTo(self.pickedSongButton.mas_centerX);
  }];
  if (_pointButtonSelected != NO) {
    _pointButtonSelected = NO;
  }
  self.emptyView.hidden = YES;
  self.pickSongsTableView.hidden = YES;
  self.pickedSongsTableView.hidden = NO;
  self.emptyView.hidden = [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count;
}
#pragma mark tableViewDelegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  //    return 10;
  if (tableView == self.pickSongsTableView) {
    return [NEKaraokePickSongEngine sharedInstance].pickSongArray.count;
  } else {
    return [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count;
  }
}
- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  if (tableView == self.pickSongsTableView) {
    // 歌曲列表页面
    NEKaraokePointSongTableViewCell *cell =
        [tableView dequeueReusableCellWithIdentifier:@"Identifier" forIndexPath:indexPath];
    if ([NEKaraokePickSongEngine sharedInstance].pickSongArray.count <= indexPath.row ||
        [NEKaraokePickSongEngine sharedInstance].pickSongDownloadingArray.count <= indexPath.row) {
      return cell;
    }
    NEKaraokeSongItem *item = [NEKaraokePickSongEngine sharedInstance].pickSongArray[indexPath.row];
    NSString *downlaodingStatus =
        [NEKaraokePickSongEngine sharedInstance].pickSongDownloadingArray[indexPath.row];
    if (item.songCover.length > 0) {
      [cell.songImageView sd_setImageWithURL:[NSURL URLWithString:item.songCover]];
    } else {
      cell.songImageView.image = [UIImage karaoke_imageNamed:@"empty_song_cover"];
    }

    cell.songLabel.text = item.songName;
    cell.progress = item.downloadProcess;
    NECopyrightedSinger *singer = item.singers.firstObject;
    if (singer) {
      cell.anchorLabel.text =
          [NSString stringWithFormat:@"%@:%@", NELocalizedString(@"歌手"), singer.singerName];
    } else {
      cell.anchorLabel.text = nil;
    }
    if (item.channel == CLOUD_MUSIC) {
      cell.resourceImageView.image = [UIImage karaoke_imageNamed:@"pointsong_clouldmusic"];
    } else if (item.channel == MIGU) {
      cell.resourceImageView.image = [UIImage karaoke_imageNamed:@"pointsong_migu"];
    } else {
      cell.resourceImageView.image = nil;
    }
    if ([downlaodingStatus isEqualToString:@"0"]) {
      cell.statueBottomLabel.hidden = YES;
      cell.statueTopLabel.hidden = YES;
      cell.downloadingLabel.hidden = YES;
      cell.pointButton.hidden = NO;
    } else {
      cell.statueBottomLabel.hidden = NO;
      cell.statueTopLabel.hidden = NO;
      cell.downloadingLabel.hidden = NO;
      cell.pointButton.hidden = YES;
    }
    __weak typeof(cell) weakCell = cell;
    __weak typeof(self) weakSelf = self;
    cell.clickPointButton = ^{
      __strong typeof(weakCell) cell = weakCell;
      __strong typeof(weakSelf) self = weakSelf;
      NSString *logInfo = [NSString stringWithFormat:@"点击开始下载文件:%@", item.songId];
      [NEKaraokeSongLog successLog:karaokeSongLog desc:logInfo];
      // 此处需要家逻辑
      // 是否在麦上
      if (self.isUserOnSeat) {
        bool isOnSeat = self.isUserOnSeat();
        if (isOnSeat) {
          [[NEKaraokePickSongEngine sharedInstance].pickSongDownloadingArray
              replaceObjectAtIndex:indexPath.row
                        withObject:@"1"];
          cell.statueBottomLabel.hidden = NO;
          cell.statueTopLabel.hidden = NO;
          cell.downloadingLabel.hidden = NO;
          cell.pointButton.hidden = YES;
          NSString *viewLogInfo =
              [NSString stringWithFormat:@"点击开始下载文件,界面变更为下载中:%@", item.songId];
          [NEKaraokeSongLog successLog:karaokeSongLog desc:viewLogInfo];
          [[NEKaraokePickSongEngine sharedInstance].currentOrderSongArray addObject:item];
          NSString *downloadingLogInfo =
              [NSString stringWithFormat:@"点击开始下载文件,下载中列表数据变更:%@", item.songId];
          [NEKaraokeSongLog successLog:karaokeSongLog desc:downloadingLogInfo];
          [[NEKaraokePickSongEngine sharedInstance] preloadSong:item.songId channel:item.channel];
        } else {
          // 申请上麦
          if (self.applyOnseat) {
            self.currentOrderSong = item;
            self.applyOnseat();
          }
        }
      }
    };
    return cell;

  } else {
    NEKaraokePointedSongTableViewCell *cell =
        [tableView dequeueReusableCellWithIdentifier:@"Identifier2" forIndexPath:indexPath];
    if ([NEKaraokePickSongEngine sharedInstance].pickedSongArray.count <= indexPath.row) {
      return cell;
    }
    NEKaraokeOrderSongResult *item =
        [NEKaraokePickSongEngine sharedInstance].pickedSongArray[indexPath.row];
    if (indexPath.row == 0) {
      cell.playingImageView.hidden = NO;
      cell.songNumberLabel.hidden = YES;
      cell.topButton.hidden = YES;
      cell.cancelButton.hidden = YES;
      cell.statueLabel.hidden = NO;
    } else {
      cell.playingImageView.hidden = YES;
      cell.songNumberLabel.hidden = NO;
      cell.statueLabel.hidden = YES;
      if ([self.detail.anchor.userUuid isEqualToString:[NEKaraokeKit shared].localMember.account]) {
        // 是主播
        if (indexPath.row > 1) {
          cell.topButton.hidden = NO;
        } else {
          cell.topButton.hidden = YES;
        }

        cell.cancelButton.hidden = NO;
      } else {
        // 是自己点的
        if ([item.orderSongUser.userUuid
                isEqualToString:[NEKaraokeKit shared].localMember.account]) {
          cell.topButton.hidden = YES;
          cell.cancelButton.hidden = NO;
        } else {
          // 其他人的歌
          cell.topButton.hidden = YES;
          cell.cancelButton.hidden = YES;
        }
      }
      cell.clickCancel = ^{
        // 正在删除的歌曲不重复删除
        if ([[NEKaraokeKit shared] isSongDeleting:item.orderSong.orderId]) {
          return;
        }
        // 点击取消
        [[NEKaraokeKit shared]
            deleteSongWithOrderId:item.orderSong.orderId
                         callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                           // 1008表示已经删过了
                           if (code != 0 && code != 1008) {
                             [NEKaraokeToast
                                 showToast:[NSString
                                               stringWithFormat:@"%@ %@",
                                                                NELocalizedString(@"删除歌曲失败"),
                                                                msg]];
                           }
                         }];
      };
      cell.clickTop = ^{
        // 正在删除的歌曲不置顶
        if ([[NEKaraokeKit shared] isSongDeleting:item.orderSong.orderId]) {
          return;
        }
        // 点击置顶
        [[NEKaraokeKit shared]
            topSongWithOrderId:item.orderSong.orderId
                      callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                        if (code != 0) {
                          [NEKaraokeToast showToast:[NSString stringWithFormat:@"%@ %@",
                                                                               NELocalizedString(
                                                                                   @"置顶歌曲失败"),
                                                                               msg]];
                        }
                      }];
      };
    }
    cell.songNumberLabel.text = [NSString stringWithFormat:@"%02d", (int)indexPath.row];
    [cell.songIconImageView sd_setImageWithURL:[NSURL URLWithString:item.orderSong.songCover]];
    cell.songNameLabel.text = [NSString stringWithFormat:@"%@", item.orderSong.songName];
    if (item.orderSongUser.icon) {
      [cell.userIconImageView sd_setImageWithURL:[NSURL URLWithString:item.orderSongUser.icon]];
    } else {
      [cell.userIconImageView setImage:[UIImage karaoke_imageNamed:@"karaoke_user_default_icon"]];
    }

    cell.userNickNameLabel.text = [NSString stringWithFormat:@"%@", item.orderSongUser.userName];
    cell.songDurationLabel.hidden = NO;
    // duration暂时不做处理
    cell.songDurationLabel.text = [self formatSeconds:item.orderSong.oc_songTime];
    // 歌曲状态 -2 已唱 -1 删除 0:等待唱 1 唱歌中
    // 状态第一行直接显示正在演唱
    cell.statueLabel.text = NELocalizedString(@"正在演唱");
    return cell;
  }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 61;
}

#pragma makr format sec
- (NSString *)formatSeconds:(NSInteger)milSeconds {
  long seconds = milSeconds / 1000;
  NSString *str_minute = [NSString stringWithFormat:@"%02ld", (seconds % 3600) / 60];
  NSString *str_second = [NSString stringWithFormat:@"%02ld", seconds % 60];
  return [NSString stringWithFormat:@"%@:%@", str_minute, str_second];
}

#pragma mark NEKaraokeSongProtocol
- (void)onOrderSongRefresh {
  __weak typeof(self) weakSelf = self;
  [[NEKaraokePickSongEngine sharedInstance] getKaraokeSongOrderedList:^(NSError *_Nullable error) {
    __strong typeof(weakSelf) self = weakSelf;
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
      __strong typeof(weakSelf) self = weakSelf;
      if (error) {
        [NEKaraokeToast showToast:NELocalizedString(@"获取已点列表失败")];
      } else {
        [self.pickedSongButton
            setTitle:[NSString stringWithFormat:NELocalizedString(@"已点(%lu)"),
                                                [NEKaraokePickSongEngine sharedInstance]
                                                    .pickedSongArray.count]
            forState:UIControlStateNormal];
        if (!self.pointButtonSelected) {
          self.emptyView.hidden = [NEKaraokePickSongEngine sharedInstance].pickedSongArray.count;
        }
        [self.pickedSongsTableView reloadData];
      }
    });
  }];
}

- (void)cancelApply {
  self.currentOrderSong = nil;
}
- (void)applyFaile {
  self.currentOrderSong = nil;
}
- (void)applySuccess {
  //  [[NEKaraokePickSongEngine sharedInstance]
  //      applySuccessWithSong:self.currentOrderSong
  //                  complete:^{
  //                    [[NEKaraokePickSongEngine sharedInstance].currentOrderSongArray
  //                        addObject:self.currentOrderSong];
  //
  //                    [[NEKaraokeKit shared] preloadSong:self.currentOrderSong.songId
  //                                                          observe:self];
  //                  }];
}

#pragma mark NESongPointProtocol
- (void)onSourceReloadIndex:(NSIndexPath *)index process:(float)progress {
  dispatch_async(dispatch_get_main_queue(), ^{
    if ([NEKaraokePickSongEngine sharedInstance].pickSongArray.count > index.row) {
      NEKaraokePointSongTableViewCell *cell = [self.pickSongsTableView cellForRowAtIndexPath:index];
      cell.progress = progress;
    } else {
      NSString *progressLogInfo =
          [NSString stringWithFormat:@"数据刷新导致目前列表中无下载数据,index:%@,\n progress:%.2f",
                                     index, progress];
      [NEKaraokeSongLog successLog:karaokeSongLog desc:progressLogInfo];
    }
  });
}
- (void)onSourceReloadIndex:(NSIndexPath *)index isSonsList:(BOOL)isSonsList {
  if (isSonsList) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if ([NEKaraokePickSongEngine sharedInstance].pickSongArray.count > index.row) {
        NEKaraokePointSongTableViewCell *cell =
            [self.pickSongsTableView cellForRowAtIndexPath:index];
        cell.statueBottomLabel.hidden = YES;
        cell.statueTopLabel.hidden = YES;
        cell.downloadingLabel.hidden = YES;
        cell.pointButton.hidden = NO;
      }
    });
  }
}

- (void)onOrderSongError:(NSString *)errorMessage {
  if (errorMessage && errorMessage.length > 0) {
    [NEKaraokeToast showToast:errorMessage];
  }
}

#pragma mark textFiledDelegate
- (void)textfieldDidChangeValue:(NSNotification *)notification {
  UITextField *textField = notification.object;
  if (textField.text.length > 0) {
    self.searchClearButton.hidden = NO;
  } else {
    self.searchClearButton.hidden = YES;
  }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
  // 按下搜索
  // 收回键盘
  [self endEditing:YES];
  if (self.searchTextField.text.length <= 0) {
    return YES;
  }
  // 请求接口
  self.isSearching = YES;
  [[NEKaraokePickSongEngine sharedInstance] resetPageNumber];
  [[NEKaraokePickSongEngine sharedInstance] updateSongArray];
  [self getKaraokeSearchSongsList];
  return YES;
}
- (void)getKaraokeSearchSongsList {
  __weak typeof(self) weakSelf = self;
  ;
  [[NEKaraokePickSongEngine sharedInstance]
      getKaraokeSearchSongList:self.searchTextField.text
                      callback:^(NSError *_Nullable error) {
                        __strong typeof(weakSelf) self = weakSelf;
                        ;
                        __weak typeof(self) weakSelf = self;
                        ;
                        dispatch_async(dispatch_get_main_queue(), ^{
                          __strong typeof(weakSelf) self = weakSelf;
                          ;
                          [self.pickSongsTableView.mj_header endRefreshing];
                          [self.pickSongsTableView.mj_footer endRefreshing];
                          if (error) {
                            if ([self.pickSongsTableView.refreshControl isRefreshing]) {
                              [self.pickSongsTableView.refreshControl endRefreshing];
                            }
                            [self.pickSongsTableView reloadData];
                            [NEKaraokeToast showToast:NELocalizedString(@"没有找到合适的结果")];
                          } else {
                            [[NEKaraokePickSongEngine sharedInstance] updatePageNumber:YES];
                            [self.pickSongsTableView reloadData];
                            if ([self.pickSongsTableView.refreshControl isRefreshing]) {
                              [self.pickSongsTableView.refreshControl endRefreshing];
                            }
                            if ([[NEKaraokePickSongEngine sharedInstance] pickSongArray].count <=
                                0) {
                              [NEKaraokeToast showToast:NELocalizedString(@"没有找到合适的结果")];
                            }
                          }
                        });
                      }];
}

- (void)clickSearchClearButton:(id)sender {
  self.searchTextField.text = @"";
  self.searchClearButton.hidden = YES;
  if (!self.isSearching) {
    [self endEditing:YES];
    return;
  }

  self.isSearching = NO;
  [[NEKaraokePickSongEngine sharedInstance] updateSongArray];
  [[NEKaraokePickSongEngine sharedInstance] resetPageNumber];
  [self refreshList];
}
- (void)dealloc {
  [[NEKaraokePickSongEngine sharedInstance] resetPageNumber];
  [[SDImageCache sharedImageCache] clearMemory];
}

- (void)onKaraokeSongTokenExpired {
  [NEKaraokeToast showToast:NELocalizedString(@"版权token过期，请稍后再试")];
}
@end
