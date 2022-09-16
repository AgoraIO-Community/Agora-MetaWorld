//
//  KTVMusicResourse.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface KTVMusicMVInfo : NSObject

@property (copy, nonatomic) NSString *mvUrl;
@property (copy, nonatomic) NSString *resolution;

@end

@interface KTVMusicResource : NSObject

@property (assign, nonatomic) NSInteger lyricType;
@property (copy, nonatomic) NSString *playUrl;
@property (copy, nonatomic) NSString *lyric;
@property (strong, nonatomic) NSArray<KTVMusicMVInfo *> *mvList;

@end

NS_ASSUME_NONNULL_END
