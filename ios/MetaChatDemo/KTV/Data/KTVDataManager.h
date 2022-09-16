//
//  KTVDataManager.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/12.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

static NSString * const kHotTypeRecommend = @"Recommend Songs";
static NSString * const kHotTypeDouyinHot =  @"TikTok hot songs";
static NSString * const kHotTypeClassic = @"Chinese Archaic Style Popular Songs";
static NSString * const kHotTypeKTV = @"KTV Popular Songs";

/// 播放的歌曲变化通知
static NSString * const kPlayingMusicWillChangeNotificationName = @"kPlayingMusicChangedNotificationName";
static NSString * const kOldPlayingMusic = @"kOldPlayingMusic"; // 取出播放结束的音乐的key
static NSString * const kNewPlayingMusic = @"kNewPlayingMusic"; // 取出将要播放的音乐的key

/// 已点歌曲列表列表发生变化
static NSString * const kLocalMusicListDidChangeNotificationName = @"kLocalMusicListDidChangeNotificationName";
static NSString * const kLocalMusicList = @"kLocalMusicList"; // 变化后的已点歌曲列表


@class KTVMusic;
@class KTVMusicHotType;


@interface KTVDataManager : NSObject

@property (nonatomic, strong, readonly) NSMutableArray<KTVMusic *> *localMusicList;

@property (nonatomic, strong, readonly) NSMutableDictionary<NSString *,NSArray<KTVMusic *> *> *cachedHotSongs;

/// 获取单例对象
+ (instancetype)shared;

/// 添加到已点歌列表
/// @param music 歌曲对象
- (void)addToLocalMusicList:(KTVMusic *)music;

/// 删除已点歌曲
/// @param music 歌曲对象
- (void)deleteLocalMusic:(KTVMusic *)music;

/// 清空数据
- (void)clear;

/// 置顶
- (void)bringMusicToTop:(KTVMusic *)music;

/// 播放下一首
- (void)makeNextAsPlaying;

/// 根据标题获取热榜类型
- (NSInteger)hotTypeForTitle:(NSString *)title;

@end

NS_ASSUME_NONNULL_END
