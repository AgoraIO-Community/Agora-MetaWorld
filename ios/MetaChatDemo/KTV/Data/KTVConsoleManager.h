//
//  KTVConsoleManager.h
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/21.
//

#import <Foundation/Foundation.h>
#import "KTVAudioEffectModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface KTVConsoleManager : NSObject

@property (assign, nonatomic) BOOL originalSong;    // 切换原唱
@property (assign, nonatomic) BOOL inEarmonitoring; // 耳返
@property (assign, nonatomic) NSInteger localVoicePitch;
@property (assign, nonatomic) NSInteger recordingSignalVolume;
@property (assign, nonatomic) NSInteger accompanyVolume;
@property (strong, nonatomic) KTVAudioEffectModel *audioEffectPreset;


/// 获取单例对象
+ (instancetype)shared;

- (void)setRtcEngine:(AgoraRtcEngineKit *)rtcEngine player:(id<AgoraRtcMediaPlayerProtocol>) player;

- (void)resetConfig;

@end

NS_ASSUME_NONNULL_END
