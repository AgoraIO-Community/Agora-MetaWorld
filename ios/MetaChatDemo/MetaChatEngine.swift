//
//  MetaChatEngine.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/27.
//

import Foundation
import AgoraRtcKit

let kOnConnectionStateChangedNotifyName = NSNotification.Name(rawValue: "onConnectionStateChanged")

protocol MCSceneEngineDelegate: NSObjectProtocol {
    func joinRtcChannelSuccess()
    func onLeave()
    func onEnter()
    func onUpdateObjectPosition(id:ObjectID, position:[NSNumber], forward:[NSNumber])
    func onReceiveStreamMessage(_ data:Data)
    func onClickKTV()
    func onClickFinishKTV()
}

@objc protocol MCResourceEventDelegate: NSObjectProtocol {
    func onConnectionStateChanged(_ state: AgoraMetachatConnectionStateType, reason: AgoraMetachatConnectionChangedReasonType);
    func onGetScenesResult(_ scenes: NSMutableArray, errorCode: Int);
    func onDownloadSceneProgress(_ sceneInfo: AgoraMetachatSceneInfo?, progress: Int, state: AgoraMetachatDownloadStateType);
}


class MetaChatEngine: NSObject {
    @objc static let sharedEngine = MetaChatEngine()
    
    @objc var rtcEngine: AgoraRtcEngineKit?
    
    var localSpatial: AgoraLocalSpatialAudioKit?
    private var mvStreamId: Int = 0
    
    private let spatialAudioParams = AgoraSpatialAudioParams()
    private var remoteUsers = [UInt]()
    
    @objc var metachatKit: AgoraMetachatKit?
    @objc private (set) var currentSceneInfo: AgoraMetachatSceneInfo?
    @objc private (set) var currentAvatarInfo: AgoraMetachatAvatarInfo?
    @objc private (set) var metachatScene: AgoraMetachatScene?
    
    @objc private (set) var spatialAudioOpen = true // 空间音效
    @objc private (set) var audioBlurOpen = false // 人声模糊
    @objc private (set) var audioAirAbsorbOpen = false // 空气衰减
    
    @objc var audioRecvRange: Float = 5 {
        didSet {
            localSpatial?.setAudioRecvRange(audioRecvRange)
        }
    }
    
    @objc var distanceUnit: Float = 8.6 {
        didSet {
            localSpatial?.setDistanceUnit(distanceUnit)
        }
    }
    
    private let userInfo = AgoraMetachatUserInfo()
    
    private var roomId: String!
    
    weak var delegate: MCSceneEngineDelegate?
    @objc weak var resourceDelegate: MCResourceEventDelegate?
    
    override init() {
        super.init()
        
        let rtcEngineConfig = AgoraRtcEngineConfig()
        rtcEngineConfig.appId = KeyCenter.APP_ID
        rtcEngineConfig.areaCode = .global
        rtcEngine = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        rtcEngine?.setParameters("{\"rtc.audio.force_bluetooth_a2dp\": true}")
        #if DEBUG || TEST
        rtcEngine?.setParameters("{\"rtc.enable_debug_log\":true}")
        #endif
        // 打开dump
//        rtcEngine?.setParameters("{\"rtc.debug.enable\":true}");
//        rtcEngine?.setParameters("{\"rtc.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}");
        rtcEngine?.setParameters("{\"rtc.audio.agc.enable\":true}")
        rtcEngine?.setChannelProfile(.liveBroadcasting)
        rtcEngine?.setClientRole(.broadcaster)
    }
    
    private func createMVStream() {
        let config = AgoraDataStreamConfig()
        config.ordered = true
        config.syncWithAudio = true
        rtcEngine?.createDataStream(&mvStreamId, config: config)
    }
        
    @objc func createMetachatKit(userName: String, avatarUrl: String) {
        
        userInfo.userId = KeyCenter.RTM_UID
        userInfo.userName = userName
        userInfo.userIconUrl = avatarUrl
        
        let metaChatconfig = AgoraMetachatConfig()
        metaChatconfig.appId = KeyCenter.APP_ID
        metaChatconfig.token = KeyCenter.RTM_TOKEN ?? ""
        metaChatconfig.userInfo = userInfo
        metaChatconfig.delegate = self
        
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        metaChatconfig.localDownloadPath = paths.first!
        metaChatconfig.rtcEngine = rtcEngine
        metachatKit = AgoraMetachatKit.sharedMetachat(with: metaChatconfig)
        createMVStream()
    }
    
    @objc func createScene(_ sceneInfo: AgoraMetachatSceneInfo, roomId:String) {
        self.roomId = roomId
        currentSceneInfo = sceneInfo
        metachatScene = metachatKit?.createScene(roomId, delegate: self)
    }
    
    @objc func enterScene(avatarInfo: AgoraMetachatAvatarInfo) {
        guard let sceneInfo = currentSceneInfo else {
            return
        }
        self.currentAvatarInfo = avatarInfo
        let avatarConfig = AgoraMetachatUserAvatarConfig()
        avatarConfig.avatarCode = avatarInfo.avatarCode
        avatarConfig.localVisible = true
        avatarConfig.remoteVisible = true
        avatarConfig.syncPosition = false
        
        metachatScene?.enter(sceneInfo, avatarConfig: avatarConfig, userInfo: userInfo)
//        metachatScene?.enter(sceneInfo, avatarConfig: avatarConfig)
        
        metachatScene?.enableUserPositionNotification(true)
    }
    
    @objc func updateAvatarInfo(_ avatarInfo: AgoraMetachatAvatarInfo, userName:String? = nil, userIconUrl: String? = nil) {
        if let currentAvatarId = currentAvatarInfo?.avatarId, currentAvatarId == avatarInfo.avatarId {
            return
        }
        currentAvatarInfo = avatarInfo
        let avatarConfig = AgoraMetachatUserAvatarConfig()
        avatarConfig.avatarCode = avatarInfo.avatarCode
        avatarConfig.localVisible = true
        avatarConfig.remoteVisible = true
        avatarConfig.syncPosition = true
        if userName != nil {
            userInfo.userName = userName!
        }
        if userIconUrl != nil {
            userInfo.userIconUrl = userIconUrl!
        }
        metachatScene?.updateLocalAvatarConfig(avatarConfig,userInfo: userInfo)
//        metachatScene?.updateLocalAvatarConfig(avatarConfig)
        metachatScene?.enableUserPositionNotification(true)
    }
    
    @objc func updateIsVisitor(isVisitor: Bool) {
        guard let avatarInfo = self.currentAvatarInfo else {
            return
        }
        
        let avatarConfig = AgoraMetachatUserAvatarConfig.init()
        avatarConfig.avatarCode = avatarInfo.avatarCode
        avatarConfig.localVisible = true;
        avatarConfig.remoteVisible = true;
        avatarConfig.syncPosition = !isVisitor;
        metachatScene?.updateLocalAvatarConfig(avatarConfig,userInfo: userInfo)
//        metachatScene?.updateLocalAvatarConfig(avatarConfig)
    }
    
    // 空间音效开关
    @objc func enableSpatialAudio(_ enable: Bool) {
        rtcEngine?.enableSpatialAudio(enable)
        spatialAudioOpen = enable
        DLog(" 空间音效开关 ---\(enable)")
    }
    
    // 人声模糊
    @objc func setRemoteUserAudioEnableBlur(_ enable: Bool) {
        audioBlurOpen = enable
        spatialAudioParams.enable_blur = AgoraRtcBoolOptional.of(enable)
        for uid in remoteUsers {
            DLog(" 人声模糊 ---\(uid)")
            rtcEngine?.setRemoteUserSpatialAudioParams(uid, params: spatialAudioParams)
        }
    }
    // 空气衰减
    @objc func setRemoteUserAudioEnableAirAbsorb(_ enable:Bool) {
        audioAirAbsorbOpen = enable
        spatialAudioParams.enable_air_absorb = AgoraRtcBoolOptional.of(enable)
        for uid in remoteUsers {
            DLog(" 空气衰减 ---\(uid)")
            rtcEngine?.setRemoteUserSpatialAudioParams(uid, params: spatialAudioParams)
        }
    }
    
    private func joinRtcChannel(id:String, success: @escaping () -> Void) {
        rtcEngine?.setClientRole(.audience)
        
        rtcEngine?.disableVideo()

        rtcEngine?.setAudioProfile(.speechStandard, scenario: .gameStreaming)
        rtcEngine?.enableAudio()
        
        let localSpatialConfig = AgoraLocalSpatialAudioConfig()
        localSpatialConfig.rtcEngine = self.rtcEngine
        localSpatial = AgoraLocalSpatialAudioKit.sharedLocalSpatialAudio(with: localSpatialConfig)
        localSpatial?.muteLocalAudioStream(false)
        localSpatial?.muteAllRemoteAudioStreams(false)
        localSpatial?.setAudioRecvRange(audioRecvRange)
        localSpatial?.setDistanceUnit(distanceUnit)
        rtcEngine?.joinChannel(byToken: KeyCenter.rtcToken(channelID: id), channelId: id, info: nil, uid: KeyCenter.RTC_UID)
    }
    
    // 加入房间
    func joinChannel(id:String){
        joinRtcChannel(id: id) { [weak self] in
                guard let wSelf = self else {return}
                wSelf.delegate?.joinRtcChannelSuccess()
        }
    }
    
    
    func resetMetachat() {
        
        metachatScene?.destroy()
        metachatScene = nil
        
        currentSceneInfo = nil
        AgoraMetachatKit.destroy()
        metachatKit = nil
    }
    
    @objc func leaveScene() {
        AgoraLocalSpatialAudioKit.destroy()
        localSpatial = nil
        rtcEngine?.leaveChannel()
        metachatScene?.leave()
    }
    
    func openMic() {
        rtcEngine?.setClientRole(.broadcaster)
    }
    
    func closeMic() {
        rtcEngine?.setClientRole(.audience)
    }
    
    func muteMic(isMute: Bool) {
        localSpatial?.muteLocalAudioStream(isMute)
    }
    
    func muteSpeaker(isMute: Bool) {
        localSpatial?.muteAllRemoteAudioStreams(isMute)
    }
    
    // 设置播放器的空间音效
    func setUpSpatialForMediaPlayer(_ player: AgoraRtcMediaPlayerProtocol?, position: [NSNumber], forward:[NSNumber]) {
        if player == nil {
            return
        }
        let positionInfo = AgoraRemoteVoicePositionInfo()
        positionInfo.position = position
        positionInfo.forward = forward
        if let playerId = player?.getMediaPlayerId() {
            localSpatial?.updatePlayerPositionInfo(Int(playerId), positionInfo: positionInfo)
        }
    }
    
    func sendStreamMessage(_ data:Data) {
        if let ret = rtcEngine?.sendStreamMessage(mvStreamId, data: data) {
            DLog("sendStreamMessage error === \(ret) streamid = \(mvStreamId)")
        }
    }
    
}

extension MetaChatEngine: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        self.rtcEngine?.muteAllRemoteAudioStreams(true)
        delegate?.joinRtcChannelSuccess()
        rtcEngine?.setRemoteUserSpatialAudioParams(uid, params: spatialAudioParams)
        rtcEngine?.setRemoteUserSpatialAudioParams(uid, params: spatialAudioParams)
        remoteUsers.append(uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        localSpatial?.removeRemotePosition(uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        DLog("receiveStreamMessageFromUid  === 收到了消息, streamId == \(streamId)")
        self.delegate?.onReceiveStreamMessage(data)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurStreamMessageErrorFromUid uid: UInt, streamId: Int, error: Int, missed: Int, cached: Int) {
        DLog("didOccurStreamMessageErrorFromUid  === 收到了消息报错 \(streamId),error == \(error)")
    }
    
}


extension MetaChatEngine: AgoraMetachatEventDelegate {
    func onConnectionStateChanged(_ state: AgoraMetachatConnectionStateType, reason: AgoraMetachatConnectionChangedReasonType) {
        if Thread.isMainThread {
            resourceDelegate?.onConnectionStateChanged(state, reason: reason)
        }else{
            DispatchQueue.main.async { [weak self] in
                self?.resourceDelegate?.onConnectionStateChanged(state, reason: reason)
            }
        }
        DispatchQueue.main.async {
            NotificationCenter.default.post(name: kOnConnectionStateChangedNotifyName, object: nil, userInfo: ["state":state.rawValue,"reason":reason.rawValue])
        }
    }
    
    func onRequestToken() {
    }
    
    func onGetScenesResult(_ scenes: NSMutableArray, errorCode: Int) {
        if Thread.isMainThread {
            resourceDelegate?.onGetScenesResult(scenes, errorCode: errorCode)
        }else{
            DispatchQueue.main.async { [weak self] in
                self?.resourceDelegate?.onGetScenesResult(scenes, errorCode: errorCode)
            }
        }
    }
    
    func onDownloadSceneProgress(_ sceneInfo: AgoraMetachatSceneInfo?, progress: Int, state: AgoraMetachatDownloadStateType) {
        if Thread.isMainThread {
            resourceDelegate?.onDownloadSceneProgress(sceneInfo, progress: progress, state: state)
        }else{
            DispatchQueue.main.async { [weak self] in
                self?.resourceDelegate?.onDownloadSceneProgress(sceneInfo, progress: progress, state: state)
            }
        }
    }
}

extension MetaChatEngine: AgoraMetachatSceneEventDelegate {
    
    func metachatScene(_ scene: AgoraMetachatScene, onEnterSceneResult errorCode: Int) {
        DLog("进入场景===============")
        DispatchQueue.main.async {
            self.joinChannel(id: self.roomId)
            self.delegate?.onEnter()
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onLeaveSceneResult errorCode: Int) {
        resetMetachat()
        DLog("离开场景========errorCode: \(errorCode)")
        delegate?.onLeave()
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onRecvMessageFromScene message: Data) {
        
        guard let json: [String: Any] = try? JSONSerialization.jsonObject(with: message) as? [String: Any] else {
            DLog("json = nil")
            return
        }
        DLog("json = ", json)
        guard let msgStr = json["message"] as? String else { return }
        CustomMessageHandler.shared.handleMessage(msgStr) {[weak self] ret in
            switch ret {
            case .objectPosition(let id, let position, let forward):
                self?.delegate?.onUpdateObjectPosition(id: id, position: position, forward: forward)
            case .didClickKTVBtn:
                DispatchQueue.main.async {
                    self?.delegate?.onClickKTV()
                }
            case .didClickFinishKTVBtn:
                DispatchQueue.main.async {
                    self?.delegate?.onClickFinishKTV()
                }
            }
        }
        
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onUserPositionChanged uid: String, posInfo: AgoraMetachatPositionInfo) {
        
        if (uid.compare(KeyCenter.RTM_UID) == .orderedSame) || (uid.compare("") == .orderedSame) {
            localSpatial?.updateSelfPosition(posInfo.position as! [NSNumber], axisForward: posInfo.forward as! [NSNumber], axisRight: posInfo.right as! [NSNumber], axisUp: posInfo.up as! [NSNumber])
            DLog("position = \(posInfo.position),forword = \(posInfo.forward),right = \(posInfo.right),up = \(posInfo.up)")
        }else {
            let remotePositionInfo = AgoraRemoteVoicePositionInfo()
            remotePositionInfo.position = posInfo.position as! [NSNumber]
            remotePositionInfo.forward = posInfo.forward as? [NSNumber]
            
            localSpatial?.updateRemotePosition(UInt(uid) ?? 0, positionInfo: remotePositionInfo)
        }
    }
}
