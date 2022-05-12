//
//  AgoraMetachatKit.h
//  AgoraRtcKit
//
//  Created by dingyusong on 12/4/22.
//  Copyright (c) 2022 Agora. All rights reserved.
//

#ifndef AgoraMetachatKit_h
#define AgoraMetachatKit_h

#import <Foundation/Foundation.h>
#import "AgoraRtcEngineKit.h"


/** connection state of metachat service
 */
typedef NS_ENUM(NSUInteger, AgoraMetachatConnectionStateType){
    /* The SDK is disconnected from the state metachat server. */
    AgoraMetachatConnectionStateTypeDisconnected = 1,
    /* The SDK is connecting to the state metachat server. */
    AgoraMetachatConnectionStateTypeConnecting,
    /* The SDK is connected to the state metachat server. */
    AgoraMetachatConnectionStateTypeConnected,
    /* The SDK is reconnecting to the state metachat server. */
    AgoraMetachatConnectionStateTypeReconnecting,
    /* The SDK is reconnected to the state metachat server. */
    AgoraMetachatConnectionStateTypeAborted
};

/** reason of connection state change of sync service
 */
typedef NS_ENUM(NSUInteger, AgoraMetachatConnectionChangedReasonType) {
    /* The connection state is changed. */
    AgoraMetachatConnectionChangedReasonTypeDefault = 0,
};

typedef NS_ENUM(NSUInteger, AgoraMetachatDownloadStateType) {
    AgoraMetachatDownloadStateTypeIdle = 0,
    AgoraMetachatDownloadStateTypeDownloading = 1,
    AgoraMetachatDownloadStateTypeDownloaded = 2,
    AgoraMetachatDownloadStateTypeFailed = 3,
};

typedef NS_ENUM(NSUInteger, AgoraMetachatErrorType) {
  AgoraMetachatErrorTypeOK = 0,
  AgoraMetachatErrorTypeEngineLoadFailed,
  AgoraMetachatErrorTypeSceneLoadFailed,
  AgoraMetachatErrorTypeJoinRoomFailed,
  AgoraMetachatErrorTypeSceneUnloadFailed,
};

// metachat avatar model information bound to metachat scene information
__attribute__((visibility("default"))) @interface AgoraMetachatAvatarInfo : NSObject
@property (nonatomic, assign) NSInteger avatarId;
@property (nonatomic, copy) NSString * _Nonnull avatarCode;
@property (nonatomic, copy) NSString * _Nonnull avatarName;
@property (nonatomic, copy) NSString * _Nonnull desc;
// local path of avatar model path
@property (nonatomic, copy) NSString * _Nonnull avatarPath;
// local path of avatar thumbnail
@property (nonatomic, copy) NSString * _Nonnull thumbnailPath;
@property (nonatomic, copy) NSString * _Nonnull assets;
@property (nonatomic, copy) NSString * _Nonnull extraInfo;
@end

// metachat scene information retrieved by getScenes interface
__attribute__((visibility("default"))) @interface AgoraMetachatSceneInfo : NSObject
@property (nonatomic, assign) NSInteger sceneId;
@property (nonatomic, copy) NSString * _Nonnull sceneName;
// local path of scene thumbnail
@property (nonatomic, copy) NSString * _Nonnull thumbnailPath;
@property (nonatomic, copy) NSString * _Nonnull scenePath;
@property (nonatomic, copy) NSString * _Nonnull desc;
@property (nonatomic, copy) NSString * _Nonnull sceneConfig;
@property (nonatomic, copy) NSString * _Nonnull assets;
@property (nonatomic, copy) NSString * _Nonnull extraInfo;
@property (nonatomic, copy) NSString * _Nonnull sceneVersion;
// avatar information bound to scene
@property (nonatomic, strong) NSArray<AgoraMetachatAvatarInfo *> * _Nonnull avatars;
@end

__attribute__((visibility("default"))) @interface AgoraMetachatUserInfo : NSObject
@property (nonatomic, copy) NSString * _Nonnull userId;
@property (nonatomic, copy) NSString * _Nonnull userName;
@property (nonatomic, copy) NSString * _Nonnull userIconUrl;
@end

__attribute__((visibility("default"))) @interface AgoraMetachatPositionInfo : NSObject
@property (nonatomic, strong) NSArray * _Nonnull position;
@property (nonatomic, strong) NSArray * _Nonnull forward;
@property (nonatomic, strong) NSArray * _Nonnull right;
@property (nonatomic, strong) NSArray * _Nonnull up;
@end 

@class AgoraMetachatKit;
@protocol AgoraMetachatEventDelegate <NSObject>
- (void)onConnectionStateChanged:(AgoraMetachatConnectionStateType)state reason:(AgoraMetachatConnectionChangedReasonType)reason;
- (void)onRequestToken;
- (void)onGetScenesResult:(NSMutableArray * _Nonnull)scenes errorCode:(NSInteger)errorCode;
- (void)onDownloadSceneProgress:(AgoraMetachatSceneInfo * _Nullable)sceneInfo progress:(NSInteger)progress state:(AgoraMetachatDownloadStateType)state;
@end

@class AgoraMetachatScene;
@protocol AgoraMetachatSceneEventDelegate<NSObject>
- (void)metachatScene:(AgoraMetachatScene *_Nonnull)scene onEnterSceneResult:(NSInteger)errorCode;
- (void)metachatScene:(AgoraMetachatScene *_Nonnull)scene onLeaveSceneResult:(NSInteger)errorCode;
- (void)metachatScene:(AgoraMetachatScene *_Nonnull)scene onRecvMessageFromScene:(NSData * _Nonnull)message;
- (void)metachatScene:(AgoraMetachatScene *_Nonnull)scene onUserPositionChanged:(NSString * _Nonnull)uid posInfo:(AgoraMetachatPositionInfo * _Nonnull)posInfo;
@end 

__attribute__((visibility("default"))) @interface AgoraMetachatUserAvatarConfig : NSObject
@property (nonatomic, copy) NSString * _Nonnull avatarCode;
@property (nonatomic,assign) BOOL localVisible;
@property (nonatomic,assign) BOOL remoteVisible;
@property (nonatomic,assign) BOOL syncPosition;
@end 

__attribute__((visibility("default"))) @interface AgoraMetachatConfig : NSObject
@property (nonatomic,copy) NSString * _Nonnull appId;
@property (nonatomic,copy) NSString * _Nonnull token;
@property (nonatomic,strong) AgoraMetachatUserInfo * _Nonnull userInfo;
@property (nonatomic,weak) id<AgoraMetachatEventDelegate> _Nullable delegate;
@property (nonatomic,copy) NSString * _Nonnull localDownloadPath;
@property(assign, nonatomic) AgoraRtcEngineKit* _Nullable rtcEngine;
@end 

__attribute__((visibility("default"))) @interface AgoraMetachatScene : NSObject
- (NSInteger)destroy;

- (NSInteger)enterScene:(AgoraMetachatSceneInfo *_Nonnull)sceneInfo avatarConfig:(AgoraMetachatUserAvatarConfig *_Nonnull)avatarConfig;
- (NSInteger)leaveScene;

- (NSInteger)sendMessageToScene:(NSData *_Nonnull)message;
- (NSInteger)setSceneParameters:(NSString *_Nonnull)jsonParam;
- (NSInteger)enableUserPositionNotification:(BOOL)enable;
- (NSInteger)enableVideoDisplay:(uint32_t)displayId enable:(BOOL)enable;
- (NSInteger)pushVideoFrameToDisplay:(uint32_t)displayId frame:(AgoraVideoFrame *_Nullable)frame;
- (NSInteger)updateLocalAvatarConfig:(AgoraMetachatUserAvatarConfig *_Nonnull)config;

- (void)enableMainQueueDispatch:(BOOL)enabled;
@end

__attribute__((visibility("default"))) @interface AgoraMetachatKit : NSObject
+ (instancetype _Nonnull)sharedMetachatWithConfig:(AgoraMetachatConfig* _Nonnull)config;
+ (void)destroy;

- (NSInteger)getScenes;
- (NSInteger)isSceneDownloaded:(NSInteger)sceneId;
- (NSInteger)downloadScene:(NSInteger)sceneId;
- (NSInteger)cancelDownloadScene:(NSInteger)sceneId;
- (NSInteger)cleanScene:(NSInteger)sceneId;

- (AgoraMetachatScene *_Nullable)createScene:(NSString *_Nullable)roomName delegate:(id<AgoraMetachatSceneEventDelegate>_Nonnull)delegate;
@end 

#endif /*AgoraMetachat_h*/

