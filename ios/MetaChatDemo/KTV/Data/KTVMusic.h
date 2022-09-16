//
//  KTVMusic.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/18.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface KTVMusicMV : NSObject

@property (copy, nonatomic) NSString *resolution; // 分辨率
@property (copy, nonatomic) NSString *bw; // 带宽

@end

@interface KTVMusic : NSObject

@property (assign, nonatomic) NSInteger songCode;
@property (copy, nonatomic) NSString *name;
@property (copy, nonatomic) NSString *singer;
@property (copy, nonatomic) NSString *poster;
@property (copy, nonatomic) NSString *song;
@property (strong, nonatomic)NSArray *lyricType; // 歌词类型，0xml格式， 1lrc格式，为空表示无 歌词，如: [0,1]
@property (assign, nonatomic)NSInteger type; //资源类型1:既有伴奏又有原唱的 歌曲2:只有伴奏的歌曲 3:只有原唱的歌曲4:有多音轨纯音频及其 MV资源5:没有多音轨纯音频的 纯MV资源
@property (assign, nonatomic)NSInteger pitchType;   // 1支持打分 2不支持打分
@property (assign, nonatomic) NSInteger duration; // 歌曲时长
@property (assign, nonatomic) NSInteger vendorId; // 版权方
@property (strong, nonatomic) NSArray<KTVMusicMV *> *mv;
@property (assign, nonatomic) BOOL isAdded; // 是否添加到本地歌单
@property (copy, nonatomic) NSString *playUrl;
@property (copy, nonatomic) NSString * __nullable mvUrl;
@property (assign, nonatomic) BOOL isPlaying; // 是否正在播放

@end

NS_ASSUME_NONNULL_END
