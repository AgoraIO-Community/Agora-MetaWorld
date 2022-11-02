//
//  KTVDataManager.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import "KTVDataManager.h"
#import "KTVMusic.h"
#import "KTVMusicResource.h"
#import "MJExtension.h"

@interface KTVDataManager()

@property (nonatomic, strong) NSMutableArray<KTVMusic *> *localMusicList;

/// 缓存的歌曲列表
@property (nonatomic, strong) NSMutableDictionary<NSString *,NSArray<KTVMusic *> *> *cachedHotSongs;

/// 当前正在播放的歌曲
@property (nonatomic, strong) KTVMusic *singingMusic;

@property (nonatomic, strong) NSDictionary *hotTypeDic;

@property (nonatomic, strong) KTVMusic *nextSong;

@end

@implementation KTVDataManager


static KTVDataManager *instance = nil;

+ (instancetype)shared {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (instance == nil) {
            instance = [KTVDataManager new];
        }
    });
    return instance;
}

- (void)setSingingMusic:(KTVMusic *)singingMusic {
    // 通知将要切歌（如果singingMusic为nil，已点歌曲播放完毕）
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithCapacity:2];
    if (_singingMusic) {
        userInfo[kOldPlayingMusic] = _singingMusic;
    }
    if (singingMusic) {
        userInfo[kNewPlayingMusic] = singingMusic;
    }
    _singingMusic.isPlaying = NO;
    _singingMusic = singingMusic;
    _singingMusic.isPlaying = YES;
    [self _postWillChangePlayingMusicWithUserInfo:userInfo];
}

- (void)addToLocalMusicList:(KTVMusic *)music {
    music.isAdded = YES;
    [self.localMusicList addObject:music];
    if (self.singingMusic == nil) {
        self.singingMusic = music;
    }
    [self _handleMusicListChanged];
    DLog(@"songslist == %@",self.cachedHotSongs);
}

- (void)deleteLocalMusic:(KTVMusic *)music {
    music.isAdded = NO;
    [self.localMusicList removeObject:music];
    [self _handleMusicListChanged];
}

- (void)deleteAllLocalMusic {
    [self.localMusicList removeAllObjects];
    [self _handleMusicListChanged];
}

- (void)clear{
    [self deleteAllLocalMusic];
    [self.cachedHotSongs removeAllObjects];
    self.singingMusic = nil;
    [self _handleMusicListChanged];
}


- (NSMutableArray<KTVMusic *> *)localMusicList {
    if (!_localMusicList) {
        _localMusicList = [NSMutableArray array];
    }
    return _localMusicList;
}

- (NSMutableDictionary<NSString *,NSArray<KTVMusic *> *> *)cachedHotSongs {
    if (!_cachedHotSongs) {
        _cachedHotSongs = [NSMutableDictionary dictionary];
    }
    return _cachedHotSongs;
}


- (void)bringMusicToTop:(KTVMusic *)music {
    if (self.localMusicList.count < 3) {
        return;
    }
    [self.localMusicList removeObject:music];
    [self.localMusicList insertObject:music atIndex:1];
    [self _handleMusicListChanged];
}

- (void)makeNextAsPlaying {
    self.singingMusic.isAdded = NO;
    [self.localMusicList removeObject:self.singingMusic];
    self.singingMusic = self.localMusicList.firstObject;
    [self _handleMusicListChanged];
}

- (NSInteger)hotTypeForTitle:(NSString *)title {
    NSNumber *hotType = self.hotTypeDic[title];
    return [hotType integerValue];
}

#pragma mark - private

- (void)_handleMusicListChanged {
    [[NSNotificationCenter defaultCenter] postNotificationName:kLocalMusicListDidChangeNotificationName object:nil userInfo:@{kLocalMusicList: self.localMusicList}];
}

- (void)_postWillChangePlayingMusicWithUserInfo:(NSDictionary *)userInfo {
    [[NSNotificationCenter defaultCenter] postNotificationName:kPlayingMusicWillChangeNotificationName object:nil userInfo:userInfo];
}

#pragma mark - getter

- (NSDictionary *)hotTypeDic {
    if (!_hotTypeDic) {
        _hotTypeDic = @{
            MCLocalizedString(kHotTypeRecommend):@0,
            MCLocalizedString(kHotTypeDouyinHot):@4,
            MCLocalizedString(kHotTypeClassic):@5,
            MCLocalizedString(kHotTypeKTV):@6
        };
    }
    return _hotTypeDic;
}

@end
