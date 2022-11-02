//
//  KTVNetworkHelper.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class KTVMusic,KTVMusicResource;

@interface KTVNetworkHelper : NSObject

+ (void)requestWithURI:(NSString *)URI params:(NSDictionary *)params success:(void(^)(_Nullable id responseObj)) success fail:(void(^)( NSError * _Nullable err)) fail;

+ (void)songsForHotType:(NSNumber *)hotType Success:(void(^)( NSArray<KTVMusic *> * _Nullable songList)) success fail:(void(^)( NSError * _Nullable err)) fail;

+ (void)searchSongsWithKey:(NSString *)key success:(void(^)( NSArray<KTVMusic *> * _Nullable songList)) success fail:(void(^)( NSError * _Nullable err)) fail;

+ (void)mvWithSongCode:(NSInteger)songCode Success:(void(^)(KTVMusicResource * _Nullable resouce)) success fail:(void(^)( NSError * _Nullable err)) fail;


+ (void)downloadMV:(NSString *)url dir:(NSString *)dir completionHandler:(void(^)(NSError *_Nullable err)) completion;

@end

NS_ASSUME_NONNULL_END
