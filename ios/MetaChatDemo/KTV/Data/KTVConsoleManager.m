//
//  KTVConsoleManager.m
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/7/21.
//

#import "KTVConsoleManager.h"
#import "MetaChatDemo-Swift.h"

@interface KTVConsoleManager()

@property (nonatomic, weak) AgoraRtcEngineKit *rtcEngine;
@property (nonatomic, weak) id<AgoraRtcMediaPlayerProtocol> tvPlayer;


@end

@implementation KTVConsoleManager

static KTVConsoleManager *instance = nil;

+ (instancetype)shared{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (instance == nil) {
            instance = [KTVConsoleManager new];
        }
    });
    return instance;
}

- (void)setRtcEngine:(AgoraRtcEngineKit *)rtcEngine player:(id<AgoraRtcMediaPlayerProtocol>) player {
    self.rtcEngine = rtcEngine;
    self.tvPlayer = player;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self originalSetting];
    }
    return self;
}

- (void)originalSetting{
    _originalSong = YES;
    
    _inEarmonitoring = NO;
//    The value ranges from -12 to 12
    _localVoicePitch = 0;
    /*
    * 0: Mute
    * 100: Original volume
    * 400: (Maximum) Four times the original volume with signal clipping protection
     */
    _recordingSignalVolume = 100;
    /*
    * 0: mute;
    * 100: original volume;
    * 400: Up to 4 times the original volume (with built-in overflow protection).
     */
    _accompanyVolume = 100;
}

- (void)setOriginalSong:(BOOL)originalSong {
    _originalSong = originalSong;
    int ret =  [self.tvPlayer selectAudioTrack:_originalSong ? 0 : 1];
    DLog(@"ret === %d, %s",ret,__func__);
}

- (void)setInEarmonitoring:(BOOL)inEarmonitoring {
    _inEarmonitoring = inEarmonitoring;
    [self.rtcEngine enableInEarMonitoring:inEarmonitoring];
}

- (void)setLocalVoicePitch:(NSInteger)localVoicePitch {
    _localVoicePitch = localVoicePitch;
//    [self.rtcEngine setLocalVoicePitch:localVoicePitch];
    [self.tvPlayer setAudioPitch:localVoicePitch];
}

- (void)setRecordingSignalVolume:(NSInteger)recordingSignalVolume {
    _recordingSignalVolume = recordingSignalVolume;
    [self.rtcEngine adjustRecordingSignalVolume:recordingSignalVolume];
}

- (void)setAccompanyVolume:(NSInteger)accompanyVolume{
    _accompanyVolume = accompanyVolume;
    int ret = [self.tvPlayer adjustPlayoutVolume:(int)accompanyVolume];
    DLog("ret == %d",ret);
}

- (void)setAudioEffectPreset:(KTVAudioEffectModel *)audioEffectPreset {
    _audioEffectPreset = audioEffectPreset;
    [self.rtcEngine setAudioEffectPreset:audioEffectPreset.preset];
}

- (void)resetConfig{
    DLog(@"当前是否是原唱？ %@ ",_originalSong ? @"原唱" : @"伴奏");
    int ret =  [self.tvPlayer selectAudioTrack:_originalSong ? 0 : 1];
    DLog(@"ret === %d",ret);
    [self.rtcEngine enableInEarMonitoring:_inEarmonitoring];
    [self.rtcEngine setLocalVoicePitch:_localVoicePitch];
    [self.rtcEngine adjustRecordingSignalVolume:_recordingSignalVolume];
    [self.tvPlayer adjustPlayoutVolume:(int)_accompanyVolume];
    [self.tvPlayer adjustPublishSignalVolume:(int)_accompanyVolume];
    [self.rtcEngine setAudioEffectPreset:_audioEffectPreset.preset];
}

@end
