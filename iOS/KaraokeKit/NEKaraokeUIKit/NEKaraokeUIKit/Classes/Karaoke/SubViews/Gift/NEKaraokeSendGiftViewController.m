// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokeSendGiftViewController.h"
#import <Masonry/Masonry.h>
#import <NEAudioEffectUIKit/NEActionSheetNavigationController.h>
#import "NEKaraokeLocalized.h"
#import "NEKaraokeSendGiftCell.h"
#import "UIColor+Karaoke.h"

@interface NEKaraokeSendGiftViewController () <UICollectionViewDelegate, UICollectionViewDataSource>

/// 代理
@property(nonatomic, weak) id<NEKaraokeSendGiftViewtDelegate> delegate;
/// 礼物展示视图
@property(nonatomic, strong) UICollectionView *collectionView;
/// 发送按钮
@property(nonatomic, strong) UIButton *sendBtn;
/// 礼物数组
@property(nonatomic, strong) NSArray<NESocialGiftModel *> *gifts;
/// 选中的礼物
@property(nonatomic, strong) NESocialGiftModel *selectedGift;

@property(nonatomic, strong) CAGradientLayer *buttonBackground;

@end

@implementation NEKaraokeSendGiftViewController

+ (void)showWithTarget:(id<NEKaraokeSendGiftViewtDelegate>)target
        viewController:(UIViewController *)viewController {
  NEKaraokeSendGiftViewController *vc = [[NEKaraokeSendGiftViewController alloc] init];
  vc.delegate = target;
  NEActionSheetNavigationController *nav =
      [[NEActionSheetNavigationController alloc] initWithRootViewController:vc];
  nav.dismissOnTouchOutside = YES;
  [viewController presentViewController:nav animated:YES completion:nil];
}

- (instancetype)init {
  if ([super init]) {
    self.gifts = [NESocialGiftModel defaultGifts];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  self.title = NELocalizedString(@"送礼物");
  [self.navigationController.navigationBar
      setTitleTextAttributes:@{NSForegroundColorAttributeName : UIColor.whiteColor}];
  self.view.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:1];

  [self setupSubView];
}

- (void)setupSubView {
  [self.view addSubview:self.collectionView];
  [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.view).offset(16);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(136);
  }];

  [self.view addSubview:self.sendBtn];
  [self.sendBtn mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.collectionView.mas_bottom).offset(8);
    make.left.equalTo(self.view).offset(20);
    make.right.equalTo(self.view).offset(-20);
    make.height.mas_equalTo(44);
    if (@available(iOS 11.0, *)) {
      make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).offset(-8);
    } else {
      make.bottom.equalTo(self.view).offset(-8);
    }
  }];

  [self.view layoutIfNeeded];
  [self.sendBtn.layer insertSublayer:self.buttonBackground atIndex:0];
}

#pragma mark - getter

- (UIButton *)sendBtn {
  if (!_sendBtn) {
    _sendBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_sendBtn setTitle:NELocalizedString(@"发送") forState:UIControlStateNormal];
    _sendBtn.layer.cornerRadius = 8;
    _sendBtn.layer.masksToBounds = YES;
    [_sendBtn addTarget:self
                  action:@selector(sendAction:)
        forControlEvents:UIControlEventTouchUpInside];
  }
  return _sendBtn;
}

- (UICollectionView *)collectionView {
  if (!_collectionView) {
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = [NEKaraokeSendGiftCell size];
    layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    layout.minimumInteritemSpacing = 8;
    layout.minimumLineSpacing = 8;
    layout.sectionInset = UIEdgeInsetsMake(0, 10, 0, 10);

    CGRect frame = CGRectMake(0, 107, self.view.frame.size.width, 136);
    _collectionView = [[UICollectionView alloc] initWithFrame:frame collectionViewLayout:layout];
    _collectionView.backgroundColor = [UIColor clearColor];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
    _collectionView.showsHorizontalScrollIndicator = NO;
    [_collectionView registerClass:[NEKaraokeSendGiftCell class]
        forCellWithReuseIdentifier:[NEKaraokeSendGiftCell description]];
    _collectionView.allowsMultipleSelection = NO;
  }
  return _collectionView;
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
    [_buttonBackground setFrame:CGRectMake(0, 0, CGRectGetWidth(self.sendBtn.frame),
                                           CGRectGetHeight(self.sendBtn.frame))];
    _buttonBackground.cornerRadius = 8;
  }
  return _buttonBackground;
}

#pragma mark - UICollectionView delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return [_gifts count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                  cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  NEKaraokeSendGiftCell *cell = [NEKaraokeSendGiftCell cellWithCollectionView:collectionView
                                                                    indexPath:indexPath
                                                                        datas:_gifts];
  return cell;
}

- (void)collectionView:(UICollectionView *)collectionView
    didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
  if ([_gifts count] > indexPath.row) {
    _selectedGift = _gifts[indexPath.row];
    _sendBtn.enabled = YES;
  }
  [self.collectionView selectItemAtIndexPath:indexPath
                                    animated:YES
                              scrollPosition:UICollectionViewScrollPositionNone];
}

- (void)sendAction:(UIButton *)sender {
  if (!_selectedGift) {
    return;
  }

  if (self.delegate && [self.delegate respondsToSelector:@selector(didSendGift:)]) {
    [self.delegate didSendGift:_selectedGift];
  }
}

#pragma mark - Present Size

- (CGFloat)contentViewHeight {
  //_contentHeight
  CGFloat total = 250;
  if (@available(iOS 11.0, *)) {
    total +=
        [UIApplication sharedApplication].keyWindow.rootViewController.view.safeAreaInsets.bottom;
  }
  return total;
}

- (CGSize)preferredContentSize {
  return CGSizeMake(CGRectGetWidth([UIScreen mainScreen].bounds), [self contentViewHeight]);
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
