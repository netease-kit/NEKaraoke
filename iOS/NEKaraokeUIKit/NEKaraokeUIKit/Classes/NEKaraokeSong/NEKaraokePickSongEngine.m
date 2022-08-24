// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEKaraokePickSongEngine.h"
#import <AVFoundation/AVFoundation.h>
#import <NEKaraokeKit/NEKaraokeKit-Swift.h>
#import <YYModel/YYModel.h>
#import <NECopyrightedMedia/NECopyrightedMediaPublic.h>
#import "NEKaraokePickSongColorDefine.h"
#import "NEKaraokeSongLog.h"

static int NEPageSize = 20;

@interface NEKaraokePickSongEngine () <NESongPreloadProtocol,
                                       NEKaraokeListener,
                                       NECopyrightedEventHandler>

@property(nonatomic, strong) NSPointerArray *observeArray;

@end

@implementation NEKaraokePickSongEngine

+ (instancetype)sharedInstance {
  static dispatch_once_t onceToken;
  static NEKaraokePickSongEngine *pickSongEngine = nil;
  dispatch_once(&onceToken, ^{
    pickSongEngine = [[NEKaraokePickSongEngine alloc] init];
    [pickSongEngine initData];
    [[NEKaraokeKit shared] addKaraokeListener:pickSongEngine];
    [[NECopyrightedMedia getInstance] addPreloadProtocolObserve:pickSongEngine];
    [[NECopyrightedMedia getInstance] setEventHandler:pickSongEngine];
  });
  return pickSongEngine;
}

- (void)initData {
  self.pickSongArray = [NSMutableArray array];
  self.pickSongDownloadingArray = [NSMutableArray array];
  self.pickedSongArray = [NSMutableArray array];
  self.pageNum = 0;
  self.observeArray = [NSPointerArray weakObjectsPointerArray];
  self.currentOrderSongArray = [NSMutableArray array];
}

- (void)addObserve:(id<NESongPointProtocol>)observe {
  bool hasAdded = NO;
  for (id<NESongPointProtocol> item in self.observeArray) {
    if (item == observe) {
      hasAdded = YES;
      break;
    }
  }
  if (!hasAdded) {
    [self.observeArray addPointer:(__bridge void *)(observe)];
  }
}

- (void)setEngineObserve {
  [[NECopyrightedMedia getInstance] addPreloadProtocolObserve:self];
}

- (void)clearData {
  [self.pickSongArray removeAllObjects];
  [self.pickedSongArray removeAllObjects];
  [self.pickSongDownloadingArray removeAllObjects];
}
//获取已点数据
- (void)getKaraokeSongOrderedList:(SongListBlock)callback {
  [[NEKaraokeKit shared]
      getOrderedSongsWithCallback:^(NSInteger code, NSString *_Nullable msg,
                                    NSArray<NEKaraokeOrderSongModel *> *_Nullable orderSongs) {
        if (code != 0) {
          callback([NSError errorWithDomain:@"getKaraokeSongOrderedList" code:code userInfo:nil]);
        } else {
          self.pickedSongArray = [orderSongs mutableCopy];
          callback(nil);
        }
      }];
}

- (void)getKaraokeSongList:(SongListBlock)callback {
  [[NECopyrightedMedia getInstance]
      getSongList:nil
          pageNum:@(self.pageNum)
         pageSize:@(NEPageSize)
         callback:^(NSArray<NECopyrightedSong *> *_Nonnull songList, NSError *_Nonnull error) {
           if (error) {
             callback(error);
           } else {
             @synchronized(self) {
               NSMutableArray *tempItems = [NSMutableArray array];
               NSMutableArray *tempLoading = [NSMutableArray array];
               NSMutableArray *tempCurrentOrderingArray = [NSMutableArray array];
               for (NEKaraokeSongItem *item in self.currentOrderSongArray) {
                 [tempCurrentOrderingArray addObject:item.songId];
               }
               for (NECopyrightedSong *songItem in songList) {
                 NEKaraokeSongItem *item = [self changeCopyrightedToKaraokeSongItem:songItem];
                 if (item.hasAccompany) {
                   BOOL isDownloading = NO;
                   if ([tempCurrentOrderingArray containsObject:item.songId]) {
                     isDownloading = YES;
                   }

                   if (isDownloading) {
                     [tempLoading addObject:@"1"];
                   } else {
                     [tempLoading addObject:@"0"];
                   }
                   [tempItems addObject:item];
                 }
               }
               dispatch_async(dispatch_get_main_queue(), ^{
                 [self.pickSongArray addObjectsFromArray:tempItems];
                 [self.pickSongDownloadingArray addObjectsFromArray:tempLoading];
                 self.noMore = songList.count < 20;
                 callback(nil);
               });
             }
           }
         }];
}

- (void)updateSongArray {
  @synchronized(self) {
    [self.pickSongArray removeAllObjects];
    [self.pickSongDownloadingArray removeAllObjects];
  }
}

- (void)resetPageNumber {
  self.pageNum = 0;
  self.searchPageNum = 0;
}
- (void)updatePageNumber:(BOOL)isSearching {
  if (isSearching) {
    self.searchPageNum += 1;
  } else {
    self.pageNum += 1;
  }
}

//上下滑动刷新搜索数据
- (void)getKaraokeSearchSongList:(NSString *)searchString callback:(SongListBlock)callback {
  [[NECopyrightedMedia getInstance]
      searchSong:searchString
         pageNum:@(self.searchPageNum)
        pageSize:@(NEPageSize)
        callback:^(NSArray<NECopyrightedSong *> *_Nonnull songList, NSError *_Nonnull error) {
          if (error) {
            callback(error);
          } else {
            @synchronized(self) {
              NSMutableArray *tempItems = [NSMutableArray array];
              NSMutableArray *tempLoading = [NSMutableArray array];
              NSMutableArray *tempCurrentOrderingArray = [NSMutableArray array];
              for (NEKaraokeSongItem *item in self.currentOrderSongArray) {
                [tempCurrentOrderingArray addObject:item.songId];
              }
              for (NECopyrightedSong *songItem in songList) {
                NEKaraokeSongItem *item = [self changeCopyrightedToKaraokeSongItem:songItem];
                if (item.hasAccompany) {
                  [tempItems addObject:item];
                  BOOL isDownloading = NO;
                  if ([tempCurrentOrderingArray containsObject:item.songId]) {
                    isDownloading = YES;
                  }

                  if (isDownloading) {
                    [tempLoading addObject:@"1"];
                  } else {
                    [tempLoading addObject:@"0"];
                  }
                }
              }
              dispatch_async(dispatch_get_main_queue(), ^{
                [self.pickSongArray addObjectsFromArray:tempItems];
                [self.pickSongDownloadingArray addObjectsFromArray:tempLoading];
                self.noMore = songList.count < 20;
                callback(nil);
              });
            }
          }
        }];
}
#pragma mark <NESongPreloadProtocol>

- (void)onPreloadStart:(nonnull NSString *)songId {
  [NEKaraokeSongLog successLog:karaokeSongLog
                          desc:[NSString stringWithFormat:@"%@开始加载", songId]];
}
- (void)onPreloadProgress:(NSString *)songId progress:(float)progress {
  NEKaraokeSongItem *songItem;
  @synchronized(self) {
    for (NEKaraokeSongItem *item in self.pickSongArray) {
      if ([item.songId isEqualToString:songId]) {
        songItem = item;
        songItem.downloadProcess = progress;
        break;
      }
    }
  }

  if (songItem) {
    unsigned long index = [self.pickSongArray indexOfObject:songItem];

    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onSourceReloadIndex:process:)]) {
        [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:index inSection:0] process:progress];
      }
    }
  }
}

- (void)onPreloadComplete:(NSString *)songId error:(NSError *_Nullable)preloadError {
  NSString *infoString =
      [NSString stringWithFormat:@"songid = %@;error = %@", songId,
                                 preloadError.description ? preloadError.description : @"scuuess"];
  [NEKaraokeSongLog infoLog:karaokeSongLog desc:infoString];
  //获取Item 刷新UI
  NEKaraokeSongItem *songItem;
  for (NEKaraokeSongItem *song in self.pickSongArray) {
    if ([songId isEqualToString:song.songId]) {
      songItem = song;
      break;
    }
  }

  if (songItem) {
    long index = [self.pickSongArray indexOfObject:songItem];
    [[NEKaraokePickSongEngine sharedInstance].pickSongDownloadingArray replaceObjectAtIndex:index
                                                                                 withObject:@"0"];
    //此处添加数据回调
    //回调抛出
    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onSourceReloadIndex:isSonsList:)]) {
        [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:index inSection:0] isSonsList:YES];
      }
    }
  }

  NEKaraokeSongItem *currentSongitem;
  for (NEKaraokeSongItem *song in self.currentOrderSongArray) {
    if ([songId isEqualToString:song.songId]) {
      currentSongitem = song;
      break;
    }
  }
  if (preloadError) {
    [NEKaraokeSongLog successLog:karaokeSongLog desc:@"文件加载失败"];
    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onOrderSongError:)]) {
        [obj onOrderSongError:@"文件加载失败"];
      }
    }
    if (currentSongitem) {
      [self.currentOrderSongArray removeObject:currentSongitem];
    }
    return;
  }
  [NEKaraokeSongLog successLog:karaokeSongLog desc:@"文件加载完成"];

  if (!currentSongitem) {
    return;
  }
  NEKaraokeOrderSongModel *orderSong = [NEKaraokeOrderSongModel new];
  orderSong.songId = songId;
  orderSong.songName = [NSString stringWithFormat:@"%@", currentSongitem.songName];
  orderSong.songCover = [NSString stringWithFormat:@"%@", currentSongitem.songCover];
  orderSong.songCover = [NSString stringWithFormat:@"%@", currentSongitem.songCover];
  // 获取歌曲长度
  NSString *accomp = [[NECopyrightedMedia getInstance] getSongURI:songId songResType:TYPE_ACCOMP];
  NSData *data = [NSData dataWithContentsOfFile:accomp];
  if (!data.length) {
    NSString *origin = [[NECopyrightedMedia getInstance] getSongURI:songId songResType:TYPE_ORIGIN];
    data = [NSData dataWithContentsOfFile:origin];
  }
  AVAudioPlayer *player = [[AVAudioPlayer alloc] initWithData:data error:nil];
  orderSong.oc_songTime = player.duration * 1000;

  [self.currentOrderSongArray removeObject:currentSongitem];
  [[NEKaraokeKit shared]
      orderSong:orderSong
       callback:^(NSInteger code, NSString *_Nullable msg,
                  NEKaraokeOrderSongModel *_Nullable object) {
         if (code != 0) {
           NSString *message = nil;
           if (code == SONG_ERROR_SONG_POINTED) {
             message = @"歌曲已点";
           } else if (code == SONG_ERROR_SONG_POINTED_USER_LIMIT) {
             message = @"每个用户最多点2首歌";
           } else if (code == SONG_ERROR_SONG_POINTED_ROOM_LIMIT) {
             message = @"每个房间最多点10首歌";
           } else {
             message = @"点歌失败";
           }

           //此处添加数据回调
           //回调抛出
           for (id<NESongPointProtocol> obj in self.observeArray) {
             if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
                 [obj respondsToSelector:@selector(onOrderSongError:)]) {
               [obj onOrderSongError:message];
             }
           }

         } else {
           //此处添加数据回调
           //回调抛出
           [NEKaraokeSongLog successLog:karaokeSongLog desc:@"点歌成功"];
           for (id<NESongPointProtocol> obj in self.observeArray) {
             if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
                 [obj respondsToSelector:@selector(onOrderSongError:)]) {
               [obj onOrderSongError:nil];
             }
           }
         }
       }];
}

//上麦成功数据处理
- (void)applySuccessWithSong:(NEKaraokeSongItem *)songItem complete:(void (^)(void))complete {
  if (songItem) {
    NSNumber *index = nil;
    for (NEKaraokeSongItem *item in self.pickSongArray) {
      if ([item.songId isEqualToString:songItem.songId]) {
        index = [NSNumber numberWithLong:[self.pickSongArray indexOfObject:item]];
      }
    }

    if (!index) {
      return;
    }

    [[NEKaraokePickSongEngine sharedInstance].pickSongDownloadingArray
        replaceObjectAtIndex:[index intValue]
                  withObject:@"1"];

    //此处添加数据回调
    //回调抛出
    for (id<NESongPointProtocol> obj in self.observeArray) {
      if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
          [obj respondsToSelector:@selector(onSourceReloadIndex:isSonsList:)]) {
        [obj onSourceReloadIndex:[NSIndexPath indexPathForRow:[index intValue] inSection:0]
                      isSonsList:YES];
      }
    }
    complete();
  }
}

- (NEKaraokeSongItem *)changeCopyrightedToKaraokeSongItem:(NECopyrightedSong *)songItem {
  NEKaraokeSongItem *item = [[NEKaraokeSongItem alloc] init];
  item.songId = songItem.songId;
  item.songName = songItem.songName;
  item.songCover = songItem.songCover;
  item.singers = songItem.singers;
  item.albumName = songItem.albumName;
  item.albumCover = songItem.albumCover;
  item.originType = songItem.originType;
  item.channel = songItem.channel;
  item.hasAccompany = songItem.hasAccompany;
  item.hasOrigin = songItem.hasOrigin;
  return item;
}

- (void)onTokenExpired {
  [NEKaraokeSongLog infoLog:karaokeSongLog desc:@"收到token过期回调"];
  for (id<NESongPointProtocol> obj in self.observeArray) {
    if (obj && [obj conformsToProtocol:@protocol(NESongPointProtocol)] &&
        [obj respondsToSelector:@selector(onKaraokeSongTokenExpired)]) {
      [obj onKaraokeSongTokenExpired];
    }
  }

  [NEKaraokeKit.shared getSongTokenWithCallback:^(NSInteger code, NSString *_Nullable msg,
                                                  NSString *_Nullable tokenRealTime) {
    if (code != 0) {
      [NEKaraokeSongLog errorLog:karaokeSongLog desc:@"获取动态tokne失败"];
    } else {
      [[NECopyrightedMedia getInstance] renewToken:tokenRealTime];
    }
  }];
}
@end
