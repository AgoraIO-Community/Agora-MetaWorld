//
//  MetaChatEngine.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/27.
//

import Foundation
import AgoraRtcKit

class MetaChatEngine: NSObject {
    static let sharedEngine = MetaChatEngine()

    var rtcEngine: AgoraRtcEngineKit?
    
    override init() {
        super.init()
        
        let rtcEngineConfig = AgoraRtcEngineConfig()
        rtcEngineConfig.appId = KeyCenter.APP_ID
        rtcEngineConfig.areaCode = .global
        rtcEngine = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
    }
    
    var metachatKit: AgoraMetachatKit?

    var playerName: String?
        
    func createMetachatKit(userName: String, avatarUrl: String, delegate: AgoraMetachatEventDelegate?) {
        playerName = userName
                
        let userInfo = AgoraMetachatUserInfo.init()
        userInfo.userId = KeyCenter.RTM_UID
        userInfo.userName = userName
        userInfo.userIconUrl = avatarUrl
        
        let metaChatconfig = AgoraMetachatConfig.init()
        metaChatconfig.appId = KeyCenter.APP_ID
        metaChatconfig.token = KeyCenter.RTM_TOKEN ?? ""
        metaChatconfig.userInfo = userInfo
        metaChatconfig.delegate = delegate
        
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        metaChatconfig.localDownloadPath = paths.first!
        metaChatconfig.rtcEngine = rtcEngine
        metachatKit = AgoraMetachatKit.sharedMetachat(with: metaChatconfig)
    }
    
    var currentSceneInfo: AgoraMetachatSceneInfo?
    var metachatScene: AgoraMetachatScene?

    func createScene(_ sceneInfo: AgoraMetachatSceneInfo, delegate: AgoraMetachatSceneEventDelegate) {
        currentSceneInfo = sceneInfo
        
        metachatScene = metachatKit?.createScene(KeyCenter.CHANNEL_ID, delegate: delegate)
    }
    
    
    func enterScene() {
        guard let sceneInfo = currentSceneInfo else {
            return
        }
        
        let avatarConfig = AgoraMetachatUserAvatarConfig.init()
        avatarConfig.avatarCode = sceneInfo.avatars[0].avatarCode;
        avatarConfig.localVisible = true;
        avatarConfig.remoteVisible = true;
        avatarConfig.syncPosition = false;
        metachatScene?.enter(sceneInfo, avatarConfig: avatarConfig)
        
        metachatScene?.enableUserPositionNotification(true)
    }
    
    func updateIsVisitor(isVisitor: Bool) {
        guard let sceneInfo = currentSceneInfo else {
            return
        }
        
        let avatarConfig = AgoraMetachatUserAvatarConfig.init()
        avatarConfig.avatarCode = sceneInfo.avatars[0].avatarCode;
        avatarConfig.localVisible = true;
        avatarConfig.remoteVisible = true;
        avatarConfig.syncPosition = !isVisitor;
        metachatScene?.updateLocalAvatarConfig(avatarConfig)
    }
    
    var localSpatial: AgoraLocalSpatialAudioKit?
    
    func joinRtcChannel(success: @escaping () -> Void) {
        rtcEngine?.setClientRole(.audience)

        rtcEngine?.disableVideo()

        rtcEngine?.setAudioProfile(.speechStandard, scenario: .gameStreaming)
        rtcEngine?.enableAudio()
        
        let localSpatialConfig = AgoraLocalSpatialAudioConfig()
        localSpatialConfig.rtcEngine = self.rtcEngine
        localSpatial = AgoraLocalSpatialAudioKit.sharedLocalSpatialAudio(with: localSpatialConfig)
        localSpatial?.muteLocalAudioStream(false)
        localSpatial?.muteAllRemoteAudioStreams(false)
        localSpatial?.setAudioRecvRange(50)
        localSpatial?.setDistanceUnit(1)
        
        rtcEngine?.joinChannel(byToken: KeyCenter.RTC_TOKEN, channelId: KeyCenter.CHANNEL_ID, info: nil, uid: KeyCenter.RTC_UID, joinSuccess: { String, UInt, Int in
            self.rtcEngine?.muteAllRemoteAudioStreams(true)
            success()
        })
    }
    
    func leaveRtcChannel() {
        AgoraLocalSpatialAudioKit.destroy()
        localSpatial = nil
        rtcEngine?.leaveChannel()
    }
    
    func leaveScene() {
        metachatScene?.leave()
    }
    
    func resetMetachat() {
        metachatScene?.destroy()
        metachatScene = nil
        
        currentSceneInfo = nil
        
        playerName = nil
        
        AgoraMetachatKit.destroy()
        metachatKit = nil
    }
    
    func openMic() {
        rtcEngine?.setClientRole(.broadcaster)
    }
    
    func closeMic() {
        rtcEngine?.setClientRole(.audience)
    }
    
    func muteMic(isMute: Bool) {
        localSpatial?.muteLocalAudioStream(isMute)
//        rtcEngine?.muteLocalAudioStream(isMute)
    }
    
    func muteSpeaker(isMute: Bool) {
        localSpatial?.muteAllRemoteAudioStreams(isMute)
//        rtcEngine?.muteAllRemoteAudioStreams(isMute)
    }
}

extension MetaChatEngine: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {

    }
    
    /// callback when error occured for agora sdk, you are recommended to display the error descriptions on demand
    /// to let user know something wrong is happening
    /// Error code description can be found at:
    /// en: https://docs.agora.io/en/Voice/API%20Reference/oc/Constants/AgoraErrorCode.html
    /// cn: https://docs.agora.io/cn/Voice/API%20Reference/oc/Constants/AgoraErrorCode.html
    /// @param errorCode error code of the problem
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {

    }
    
    /// callback when a remote user is joinning the channel, note audience in live broadcast mode will NOT trigger this event
    /// @param uid uid of remote joined user
    /// @param elapsed time elapse since current sdk instance join the channel in ms
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {

    }
    
    /// callback when a remote user is leaving the channel, note audience in live broadcast mode will NOT trigger this event
    /// @param uid uid of remote joined user
    /// @param reason reason why this user left, note this event may be triggered when the remote user
    /// become an audience in live broadcasting profile
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        localSpatial?.removeRemotePosition(uid)
    }
    
    /// Reports which users are speaking, the speakers' volumes, and whether the local user is speaking.
    /// @params speakers volume info for all speakers
    /// @params totalVolume Total volume after audio mixing. The value range is [0,255].
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
    }
    
    /// Reports the statistics of the current call. The SDK triggers this callback once every two seconds after the user joins the channel.
    /// @param stats stats struct
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {

    }
    
    /// Reports the statistics of the uploading local audio streams once every two seconds.
    /// @param stats stats struct
    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        
    }
    
    /// Reports the statistics of the audio stream from each remote user/host.
    /// @param stats stats struct for current call statistics
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        
    }

}
