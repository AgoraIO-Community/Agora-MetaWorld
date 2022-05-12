//
//  AgoraSpatialAudioKit.h
//  AgoraRtcKit
//
//  Copyright (c) 2018 Agora. All rights reserved.
//

#ifndef AgoraSpatialAudioKit_h
#define AgoraSpatialAudioKit_h

#import <Foundation/Foundation.h>
#import "AgoraEnumerates.h"
#import "AgoraObjects.h"

typedef NS_ENUM(NSInteger, AgoraAudioRangeMode) { AgoraAudioRangeModeWorld = 0, AgoraAudioRangeModeTeam = 1 };

typedef NS_ENUM(NSInteger, AgoraSaeConnectionState) {
  AgoraSaeConnectionStateConnecting = 0,
  AgoraSaeConnectionStateConnected = 1,
  AgoraSaeConnectionStateDisconnected = 2,
  AgoraSaeConnectionStateReconnecting = 3,
  AgoraSaeConnectionStateReconnected = 4,
};

typedef NS_ENUM(NSInteger, AgoraSaeConnectionChangedReason) {
  AgoraSaeConnectionChangedReasonDefault = 0,
  AgoraSaeConnectionChangedReasonConnecting = 1,
  AgoraSaeConnectionChangedReasonCreateRoomFail = 2,
  AgoraSaeConnectionChangedReasonRtmDisconnect = 3,
  AgoraSaeConnectionChangedReasonAborted = 4,
  AgoraSaeConnectionChangedReasonLostSync = 5,
};

typedef NS_ENUM(NSInteger, AgoraSaeDeployRegionType) {
  AgoraSaeDeployRegionTypeCN = 0x1,
  AgoraSaeDeployRegionTypeNA = 0x2,
  AgoraSaeDeployRegionTypeEU = 0x4,
  AgoraSaeDeployRegionTypeAS = 0x8
};

__attribute__((visibility("default"))) @interface AgoraRemoteVoicePositionInfo : NSObject
@property(strong, nonatomic) NSArray<NSNumber*> * _Nonnull position;
@property(strong, nonatomic) NSArray<NSNumber*> * _Nullable forward;
@end

@class AgoraRtcEngineKit, AgoraBaseSpatialAudioKit, AgoraCloudSpatialAudioKit, AgoraLocalSpatialAudioKit;

__attribute__((visibility("default"))) @interface AgoraCloudSpatialAudioConfig : NSObject
@property(assign, nonatomic) AgoraRtcEngineKit* _Nullable rtcEngine;
/** The App ID issued to you by Agora. See [How to get the App ID](https://docs.agora.io/en/Agora%20Platform/token#get-an-app-id). Only users in apps with the same App ID can join the same channel and communicate with each other. Use an App ID to create only one AgoraRtcEngineKit instance.  To change your App ID, call [destroy]([AgoraRtcEngineKit destroy]) to `destroy` the current AgoraRtcEngineKit instance, and after `destroy` returns 0, call [sharedEngineWithConfig]([AgoraRtcEngineKit sharedEngineWithConfig:delegate:]) to create an AgoraRtcEngineKit instance with the new App ID.
 */
@property(copy, nonatomic) NSString* _Nullable appId;
/** The region for connection. This advanced feature applies to scenarios that have regional restrictions. <p>For the regions that Agora supports, see AgoraAreaCode. The area codes support bitwise operation. After specifying the region, the SDK connects to the Agora servers within that region.</p>
 */
@property(assign, nonatomic) NSUInteger deployRegion;
@end

__attribute__((visibility("default"))) @interface AgoraLocalSpatialAudioConfig : NSObject
@property(assign, nonatomic) AgoraRtcEngineKit* _Nullable rtcEngine;
@end

@protocol AgoraCloudSpatialAudioDelegate <NSObject>

- (void)csaEngineTokenWillExpire:(AgoraCloudSpatialAudioKit* _Nonnull)engine;

- (void)csaEngine:(AgoraCloudSpatialAudioKit* _Nonnull)engine connectionDidChangedToState:(AgoraSaeConnectionState)state withReason:(AgoraSaeConnectionChangedReason)reason;

- (void)csaEngine:(AgoraCloudSpatialAudioKit* _Nonnull)engine teammateJoined:(NSUInteger)uid;

- (void)csaEngine:(AgoraCloudSpatialAudioKit* _Nonnull)engine teammateLeft:(NSUInteger)uid;
@end

__attribute__((visibility("default"))) @interface AgoraBaseSpatialAudioKit : NSObject

- (int)setMaxAudioRecvCount:(NSUInteger)maxCount;

- (int)setAudioRecvRange:(float)range;

- (int)setDistanceUnit:(float)unit;

- (int)updatePlayerPositionInfo:(NSInteger)playerId positionInfo:(AgoraRemoteVoicePositionInfo* _Nonnull)positionInfo;

- (int)updateSelfPosition:(NSArray<NSNumber*>* _Nonnull)position axisForward:(NSArray<NSNumber*>* _Nonnull)axisForward axisRight:(NSArray<NSNumber*>* _Nonnull)axisRight axisUp:(NSArray<NSNumber*>* _Nonnull)axisUp;

- (int)updateSelfPositionEx:(NSArray<NSNumber*>* _Nonnull)position
                axisForward:(NSArray<NSNumber*>* _Nonnull)axisForward
                axisRight:(NSArray<NSNumber*>* _Nonnull)axisRight
                axisUp:(NSArray<NSNumber*>* _Nonnull)axisUp
                connection:(AgoraRtcConnection * _Nonnull)connection;

- (int)muteLocalAudioStream:(BOOL)mute;

- (int)muteAllRemoteAudioStreams:(BOOL)mute;

@end

__attribute__((visibility("default"))) @interface AgoraCloudSpatialAudioKit : AgoraBaseSpatialAudioKit

+ (instancetype _Nonnull)sharedCloudSpatialAudioWithConfig:(AgoraCloudSpatialAudioConfig* _Nonnull)config delegate:(id<AgoraCloudSpatialAudioDelegate> _Nullable)delegate;

+ (void)destroy;

- (int)enableSpatializer:(BOOL)enable applyToTeam:(BOOL)applyToTeam;

- (int)setTeamId:(NSInteger)teamId;

- (int)setAudioRangeMode:(AgoraAudioRangeMode)rangeMode;

- (int)enterRoomByToken:(NSString* _Nullable)token roomName:(NSString* _Nonnull)roomName uid:(NSUInteger)uid;

- (int)renewToken:(NSString* _Nonnull)token;

- (int)exitRoom;

- (int)getTeammates:(NSArray<NSNumber*> * _Nullable * _Nonnull)uids;

- (void)enableMainQueueDispatch:(BOOL)enabled;

@end

__attribute__((visibility("default"))) @interface AgoraLocalSpatialAudioKit : AgoraBaseSpatialAudioKit

+ (instancetype _Nonnull)sharedLocalSpatialAudioWithConfig:(AgoraLocalSpatialAudioConfig* _Nonnull)config;

+ (void)destroy;

- (int)updateRemotePosition:(NSUInteger)uid positionInfo:(AgoraRemoteVoicePositionInfo* _Nonnull)posInfo;

- (int)updateRemotePositionEx:(NSUInteger)uid positionInfo:(AgoraRemoteVoicePositionInfo* _Nonnull)posInfo connection:(AgoraRtcConnection * _Nonnull)connection;

- (int) removeRemotePosition:(NSUInteger)uid;

- (int) removeRemotePositionEx:(NSUInteger)uid connection:(AgoraRtcConnection * _Nonnull)connection;

- (int) clearRemotePositions;

- (int) clearRemotePositionsEx:(AgoraRtcConnection * _Nonnull)connection;

@end

#endif /* AgoraGmeKit_h */
