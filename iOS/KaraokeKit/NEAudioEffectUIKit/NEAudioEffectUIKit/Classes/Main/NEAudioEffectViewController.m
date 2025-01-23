// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEAudioEffectViewController.h"
#import <BlocksKit/BlocksKit.h>
#import <Masonry/Masonry.h>
#import "NEAERecordPanelEffectCell.h"
#import "NEAERecordPanelSlideView.h"
#import "NEAEStepView.h"
#import "NEAESwitchView.h"
#import "UIImage+AudioEffect.h"

@interface NEAudioEffectViewController () <UICollectionViewDelegate, UICollectionViewDataSource>

@property(nonatomic, strong) NEAESwitchView *earbackSwitch;
@property(nonatomic, strong) NEAERecordPanelSlideView *earbackSlider;
@property(nonatomic, strong) NEAERecordPanelSlideView *accompanySlider;
@property(nonatomic, strong) NEAERecordPanelSlideView *voiceSlider;
@property(nonatomic, strong) NEAERecordPanelSlideView *playbackVoiceSlider;
@property(nonatomic, strong) NEAEStepView *keyView;
@property(nonatomic, strong) NEAERecordPanelSlideView *strengthSlider;
@property(nonatomic, strong) UILabel *tipsLabel;
@property(nonatomic, strong) UICollectionView *collectionView;
@property(nonatomic, strong) NSArray *models;

@property(nonatomic, strong) NEAudioEffectManager *manager;

@end

@implementation NEAudioEffectViewController

- (instancetype)initWithManager:(NEAudioEffectManager *)manager {
  if ([super init]) {
    self.manager = manager;
    self.currentEffectId = -1;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.
  __weak typeof(self) weakSelf = self;
  self.manager.earbackEnableChanged = ^(BOOL enable) {
    __strong typeof(weakSelf) self = weakSelf;
    dispatch_async(dispatch_get_main_queue(), ^{
      self.earbackSwitch.switchOn.on = enable;
    });
  };

  self.manager.effectPitchChanged = ^(NSInteger effectId, int pitch) {
    __strong typeof(weakSelf) self = weakSelf;
    dispatch_async(dispatch_get_main_queue(), ^{
      if (effectId == self.currentEffectId) {
        self.keyView.step = pitch;
      }
    });
  };

  self.models = [NEAERecordMixEffectModel defaultModels];
  NERtcVoiceBeautifierType present = [self.manager getReverbPreset];
  for (NEAERecordMixEffectModel *m in self.models) {
    if (m.key == present) {
      m.selected = true;
      break;
    }
  }
  [self setupView];
  self.tipsLabel.hidden = present != kNERtcVoiceBeautifierOff;
  self.strengthSlider.hidden = present == kNERtcVoiceBeautifierOff;
}

- (void)setupView {
  // nav
  self.title = @"调音";
  self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]
      initWithImage:[[UIImage ne_imageNamed:@"reset_ico"]
                        imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal]
              style:UIBarButtonItemStylePlain
             target:self
             action:@selector(reset)];
  [self.navigationController.navigationBar
      setTitleTextAttributes:@{NSForegroundColorAttributeName : UIColor.whiteColor}];
  self.view.backgroundColor = [UIColor colorWithRed:0.192 green:0.239 blue:0.235 alpha:1];
  [self.view addSubview:self.earbackSwitch];
  [self.earbackSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.view).offset(19);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(40);
  }];

  [self.view addSubview:self.earbackSlider];
  [self.earbackSlider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.earbackSwitch.mas_bottom).offset(12);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.accompanySlider];
  [self.accompanySlider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.earbackSlider.mas_bottom).offset(28);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.voiceSlider];
  [self.voiceSlider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.accompanySlider.mas_bottom).offset(28);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.playbackVoiceSlider];
  [self.playbackVoiceSlider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.voiceSlider.mas_bottom).offset(28);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.keyView];
  [self.keyView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.playbackVoiceSlider.mas_bottom).offset(28);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(30);
  }];

  UILabel *label = [UILabel new];
  label.text = @"混响/均衡";
  label.font = [UIFont systemFontOfSize:14];
  label.textColor = [UIColor whiteColor];
  [self.view addSubview:label];
  [label mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.keyView.mas_bottom).offset(28);
    make.left.equalTo(self.view).offset(14);
    make.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.collectionView];
  [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.view).offset(14);
    make.right.equalTo(self.view).offset(-14);
    make.top.equalTo(label.mas_bottom).offset(12);
    make.height.mas_equalTo(60);
  }];
  [self.view addSubview:self.strengthSlider];
  [self.strengthSlider mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.collectionView.mas_bottom).offset(12);
    make.left.right.equalTo(self.view);
    make.height.mas_equalTo(22);
  }];

  [self.view addSubview:self.tipsLabel];
  [self.tipsLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.view).offset(14);
    make.top.bottom.right.equalTo(self.strengthSlider);
  }];
}

- (void)setCurrentEffectId:(NSInteger)currentEffectId {
  _currentEffectId = currentEffectId;
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.accompanySlider.slider
        slideToValue:[self.manager getAudioEffectVolumeWithEffectId:self.currentEffectId]
            animated:true];
    self.keyView.step = [self.manager getEffectPitchWithEffectId:self.currentEffectId];
  });
}

- (void)reset {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.manager resetAll];
    NERtcVoiceBeautifierType present = [self.manager getReverbPreset];
    for (NEAERecordMixEffectModel *m in self.models) {
      m.selected = m.key == present;
    }
    self.earbackSwitch.switchOn.on = [self.manager isEarbackEnable];
    [self.earbackSlider.slider slideToValue:[self.manager getEarbackVolume] animated:true];
    [self.accompanySlider.slider
        slideToValue:[self.manager getAudioEffectVolumeWithEffectId:self.currentEffectId]
            animated:true];
    [self.voiceSlider.slider slideToValue:[self.manager getRecordingSignalVolume] animated:true];
    [self.playbackVoiceSlider.slider slideToValue:[self.manager getPlaybackSignalVolume] animated:true];
    [self.strengthSlider.slider slideToValue:[self.manager getReverbIntensity] animated:true];
    self.keyView.step = [self.manager getEffectPitchWithEffectId:self.currentEffectId];

    [self.collectionView reloadData];
  });
}

#pragma mark - getter

- (NEAESwitchView *)earbackSwitch {
  if (!_earbackSwitch) {
    _earbackSwitch = [[NEAESwitchView alloc] initWithFrame:self.view.frame];
    _earbackSwitch.titleLabel.text = @"耳返";
    _earbackSwitch.detailLabel.text = @"插入耳机后可使用耳返功能";
    [_earbackSwitch.switchOn addTarget:self
                                action:@selector(earback:)
                      forControlEvents:UIControlEventValueChanged];
    _earbackSwitch.switchOn.on = [self.manager isEarbackEnable];
  }
  return _earbackSwitch;
}

- (void)earback:(UISwitch *)sender {
  if (sender.isOn) {
    if (!self.manager.isEarbackEnable) {
      int code = [self.manager enableEarback:true];
      if (code != 0 && code != 30005) {
        sender.on = false;
      }
    }
  } else {
    [self.manager enableEarback:false];
  }
}

- (NEAERecordPanelSlideView *)playbackVoiceSlider{
  if (!_playbackVoiceSlider) {
    _playbackVoiceSlider = [[NEAERecordPanelSlideView alloc] initWithMinValue:0 maxValue:100];
    _playbackVoiceSlider.slider.showRecommendValue = YES;
    _playbackVoiceSlider.slider.shakeWhenReachRecommendValue = YES;
    _playbackVoiceSlider.titleL.text = @"他人合唱音量";
    _playbackVoiceSlider.slider.defaultValue = [self.manager getPlaybackSignalVolume];
    _playbackVoiceSlider.slider.recommendValue = 10;
    __weak typeof(self) weakSelf = self;
    _playbackVoiceSlider.slider.valueChangedBlock = ^(CGFloat value) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager adjustPlaybackSignalVolume:value];
    };
  }
  return _playbackVoiceSlider;
}

- (NEAERecordPanelSlideView *)voiceSlider {
  if (!_voiceSlider) {
    _voiceSlider = [[NEAERecordPanelSlideView alloc] initWithMinValue:0 maxValue:100];
    _voiceSlider.slider.showRecommendValue = YES;
    _voiceSlider.slider.shakeWhenReachRecommendValue = YES;
    _voiceSlider.titleL.text = @"人声音量";
    _voiceSlider.slider.defaultValue = [self.manager getRecordingSignalVolume];
    _voiceSlider.slider.recommendValue = 80;
    __weak typeof(self) weakSelf = self;
    _voiceSlider.slider.valueChangedBlock = ^(CGFloat value) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager adjustRecordingSignalVolume:value];
    };
  }
  return _voiceSlider;
}

- (NEAERecordPanelSlideView *)earbackSlider {
  if (!_earbackSlider) {
    _earbackSlider = [self createSlider:@"耳返音量"];
    _earbackSlider.slider.defaultValue = [self.manager getEarbackVolume];
    __weak typeof(self) weakSelf = self;
    _earbackSlider.slider.valueChangedBlock = ^(CGFloat value) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager setEarbackVolume:value];
    };
  }
  return _earbackSlider;
}

- (NEAERecordPanelSlideView *)strengthSlider {
  if (!_strengthSlider) {
    _strengthSlider = [self createSlider:@"效果强度"];
    _strengthSlider.slider.defaultValue = [self.manager getReverbIntensity];
    _strengthSlider.hidden = true;
    __weak typeof(self) weakSelf = self;
    _strengthSlider.slider.valueChangedBlock = ^(CGFloat value) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager setReverbIntensity:value];
    };
  }
  return _strengthSlider;
}

- (NEAERecordPanelSlideView *)accompanySlider {
  if (!_accompanySlider) {
    _accompanySlider = [self createSlider:@"伴奏音量"];
    _accompanySlider.slider.defaultValue =
        [self.manager getAudioEffectVolumeWithEffectId:self.currentEffectId];
    __weak typeof(self) weakSelf = self;
    _accompanySlider.slider.valueChangedBlock = ^(CGFloat value) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager setAudioEffectVolumeWithEffectId:self.currentEffectId voulme:value];
    };
  }
  return _accompanySlider;
}

- (NEAEStepView *)keyView {
  if (!_keyView) {
    _keyView = [NEAEStepView
        stepViewWithTitle:@"伴奏升降调"
                      max:12
                      min:-12
                     step:[self.manager getEffectPitchWithEffectId:self.currentEffectId]];
    // TODO: 先隐藏升降调
    _keyView.hidden = true;
    __weak typeof(self) weakSelf = self;
    _keyView.valueChangedBlock = ^(NSInteger step) {
      __strong typeof(weakSelf) self = weakSelf;
      [self.manager setEffectPitchWithEffectId:self.currentEffectId pitch:(int)step];
    };
  }
  return _keyView;
}

- (NEAERecordPanelSlideView *)createSlider:(NSString *)title {
  NEAERecordPanelSlideView *slider = [[NEAERecordPanelSlideView alloc] initWithMinValue:0
                                                                               maxValue:100];
  slider.slider.showRecommendValue = YES;
  slider.slider.recommendValue = 100;
  slider.slider.shakeWhenReachRecommendValue = YES;
  slider.titleL.text = title;
  return slider;
}

- (UILabel *)tipsLabel {
  if (!_tipsLabel) {
    _tipsLabel = [[UILabel alloc] init];
    _tipsLabel.text = @"该音效不支持强度调节";
    _tipsLabel.font = [UIFont systemFontOfSize:12];
    _tipsLabel.textColor = [UIColor colorWithWhite:1 alpha:0.5];
  }
  return _tipsLabel;
}

#pragma mark - UICollectionViewDataSource

- (void)collectionView:(UICollectionView *)collectionView
    didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
  NEAERecordMixEffectModel *model = self.models[indexPath.row];
  if (model.selected) {
    model.selected = false;
    if (![self.manager setReverbPreset:kNERtcVoiceBeautifierOff]) {
      [self.manager setReverbIntensity:50];
    }
    self.tipsLabel.hidden = false;
    self.strengthSlider.hidden = true;
  } else {
    for (NEAERecordMixEffectModel *m in self.models) {
      m.selected = false;
    }
    model.selected = true;
    if (![self.manager setReverbPreset:model.key]) {
      [self.manager setReverbIntensity:50];
    }
    [self.strengthSlider.slider slideToValue:self.manager.getReverbIntensity animated:true];
    self.tipsLabel.hidden = model.key != 0;
    self.strengthSlider.hidden = model.key == 0;
  }
  [self.collectionView reloadData];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                  cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  NEAERecordPanelEffectCell *cell = [collectionView
      dequeueReusableCellWithReuseIdentifier:NSStringFromClass([NEAERecordPanelEffectCell class])
                                forIndexPath:indexPath];
  [cell updateModel:self.models[indexPath.row]];
  return cell;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return self.models.count;
}

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
  return 1;
}

- (UICollectionView *)collectionView {
  if (!_collectionView) {
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    layout.minimumLineSpacing = 8.0;
    //        layout.sectionInset = UIEdgeInsetsMake(10, 10, 10, 10);
    layout.itemSize = CGSizeMake(54, 54);

    _collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero
                                         collectionViewLayout:layout];

    _collectionView.showsHorizontalScrollIndicator = NO;
    _collectionView.showsVerticalScrollIndicator = NO;
    [_collectionView registerClass:[NEAERecordPanelEffectCell class]
        forCellWithReuseIdentifier:NSStringFromClass([NEAERecordPanelEffectCell class])];
    _collectionView.backgroundColor = [UIColor clearColor];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
  }
  return _collectionView;
}

#pragma mark - Present Size
- (CGFloat)contentViewHeight {
  //_contentHeight
  CGFloat total = 430;
  if (@available(iOS 11.0, *)) {
    total +=
        [UIApplication sharedApplication].keyWindow.rootViewController.view.safeAreaInsets.bottom;
  }
  return total;
}

- (CGSize)preferredContentSize {
  return CGSizeMake(CGRectGetWidth([UIScreen mainScreen].bounds), [self contentViewHeight]);
}

@end
