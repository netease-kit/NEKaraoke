// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSeatView.h"
#import <Masonry/Masonry.h>
#import "NEKaraokeSeatItemCell.h"
#import "NEKaraokeSeatItemView.h"
@interface NEKaraokeSeatView () <UICollectionViewDataSource,
                                 UICollectionViewDelegate,
                                 NEKaraokeSeatItemCellDelegate,
                                 NEKaraokeListener>
@property(nonatomic, strong) UICollectionView *collectionView;
@property(nonatomic, strong) NSMutableArray<NEKaraokeSeatItem *> *datas;
@property(nonatomic, copy) NSString *hostUuid;
@property(nonatomic, strong) NEKaraokeSongModel *songModel;
@end

@implementation NEKaraokeSeatView

- (instancetype)initWithFrame:(CGRect)frame {
  if ([super initWithFrame:frame]) {
    [self setupView];
    [self makeContrains];
  }
  return self;
}

- (void)setupView {
  self.backgroundColor = UIColor.clearColor;
  [self addSubview:self.collectionView];
  [NEKaraokeKit.shared addKaraokeListener:self];
}
- (void)makeContrains {
  [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.mas_equalTo(15);
    make.right.mas_equalTo(-15);
    make.top.bottom.mas_equalTo(0);
  }];
}
- (void)configSongModel:(NEKaraokeSongModel *)songModel {
  self.songModel = songModel;
  [self.collectionView reloadData];
}
- (void)configWithSeatItems:(NSArray<NEKaraokeSeatItem *> *)items hostUuid:(NSString *)uuid {
  self.hostUuid = uuid;
  [self.datas removeAllObjects];
  NSMutableArray *existArr = @[].mutableCopy;
  NSMutableArray *emptyArr = @[].mutableCopy;
  for (NEKaraokeSeatItem *item in items) {
    if (item.user.length) {
      [existArr addObject:item];
    } else {
      [emptyArr addObject:item];
    }
  }
  NSArray *arr = [existArr sortedArrayUsingComparator:^NSComparisonResult(
                               NEKaraokeSeatItem *_Nonnull obj1, NEKaraokeSeatItem *_Nonnull obj2) {
    return [@(obj1.updated) compare:@(obj2.updated)];
  }];
  [self.datas addObjectsFromArray:arr];
  [self.datas addObjectsFromArray:emptyArr];
  [self.collectionView reloadData];
}
#pragma mark-----------------------------  UICollectionViewDataSource  -----------------------------
- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return self.datas.count;
}
- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                           cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  NEKaraokeSeatItemCell *cell =
      [collectionView dequeueReusableCellWithReuseIdentifier:@"NEKaraokeSeatItemCell"
                                                forIndexPath:indexPath];
  cell.delegate = self;
  NEKaraokeSeatItem *item = self.datas[indexPath.row];
  [cell configCellWithInfo:item
                      name:[item.user isEqualToString:self.hostUuid]
                               ? @"房主"
                               : [NSString stringWithFormat:@"%ld", indexPath.row + 1]
                 songModel:self.songModel];
  return cell;
}
#pragma mark-----------------------------  NEKaraokeSeatItemCellDelegate  -----------------------------
- (void)didSelectedCell:(NEKaraokeSeatItem *)seatItem {
  if (self.delegate && [self.delegate respondsToSelector:@selector(didSelected:seatItem:)]) {
    [self.delegate didSelected:self seatItem:seatItem];
  }
}
- (void)onReceiveChorusMessage:(enum NEKaraokeChorusActionType)actionType
                     songModel:(NEKaraokeSongModel *)songModel {
  switch (actionType) {
    case NEKaraokeChorusActionTypeReady:
    case NEKaraokeChorusActionTypeStartSong:
    case NEKaraokeChorusActionTypePauseSong:
    case NEKaraokeChorusActionTypeResumeSong: {
      self.songModel = songModel;
    } break;
    case NEKaraokeChorusActionTypeEndSong: {
      self.songModel = nil;
    } break;
    default:
      break;
  }
  [self.collectionView reloadData];
}

#pragma mark-----------------------------  Getter  -----------------------------
- (UICollectionView *)collectionView {
  if (!_collectionView) {
    CGFloat lineSpecing = (UIScreen.mainScreen.bounds.size.width - 30 - 42 * 7) / 6;
    //        lineSpecing = lineSpecing >= 10 ? lineSpecing : 10;
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = CGSizeMake(42, 60);
    layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    layout.minimumLineSpacing = lineSpecing;
    layout.minimumInteritemSpacing = 0.1;
    layout.sectionInset = UIEdgeInsetsMake(0, 0, 0, 0);
    _collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero
                                         collectionViewLayout:layout];
    _collectionView.backgroundColor = [UIColor clearColor];
    _collectionView.showsVerticalScrollIndicator = NO;
    _collectionView.showsHorizontalScrollIndicator = NO;
    _collectionView.dataSource = self;
    _collectionView.delegate = self;
    _collectionView.bounces = NO;
    _collectionView.clipsToBounds = NO;
    [_collectionView registerClass:[NEKaraokeSeatItemCell class]
        forCellWithReuseIdentifier:@"NEKaraokeSeatItemCell"];
    _collectionView.layer.masksToBounds = YES;
  }
  return _collectionView;
}
- (NSMutableArray<NEKaraokeSeatItem *> *)datas {
  if (!_datas) {
    _datas = @[].mutableCopy;
  }
  return _datas;
}
@end
