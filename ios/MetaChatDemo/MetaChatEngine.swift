//
//  MetaServiceEngine.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/27.
//

import Foundation
import AgoraRtcKit

private let kAdvertisingURL = "https://download.agora.io/demo/test/agora_meta_ads.mov" // 宣传片地址
private let npcTableFileName = "tableNPC"
private let npc1MoveFileName = "moveNPC1"
private let npc2MoveFileName = "moveNPC2"

enum MetaChatSceneIndex: Int {
    case live = 0
    case chat = 1
}

var kSceneIndex: MetaChatSceneIndex = .chat

enum MetaChatDisplayID:Int32 {
    case tv = 0
    case npc_table = 1
    case npc_move1
    case npc_move2
}

enum KTVSteamDataMessageType:Int {
    case seek = 0
    case finish = 1
//    case console = 2
}

protocol RTCEngineInternalDelegate: NSObject {
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int)
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason)
}

class MetaServiceEngine: NSObject {
    @objc static let sharedEngine = MetaServiceEngine()

    @objc var rtcEngine: AgoraRtcEngineKit?
    var localSpatial: AgoraLocalSpatialAudioKit?
    
    /// 是否正在唱歌
    var isSinging = false

    private var mvStreamId: Int = 0
    
    /// 桌子旁边的NPC播放器
    private var tvPlayerMgr: MetaChatPlayerManager?
    /// 桌子旁边的NPC播放器
    private var tableNPCPlayerMgr: MetaChatPlayerManager?
    /// 移动NPC1
    private var moveNPCPlayerMgr1: MetaChatPlayerManager?
    /// 移动NPC2
    private var moveNPCPlayerMgr2: MetaChatPlayerManager?
    
    var metaService: AgoraMetaServiceKit?
    var playerName: String?
    var currentSceneInfo: AgoraMetaSceneInfo?
    var metaScene: AgoraMetaScene?
    var currentUserInfo: AgoraMetaUserInfo?
    var localUserAvatar: AgoraMetaLocalUserAvatar?
    var mockRenderView: MockRenderView?
    var braodcaster: String = ""
    var resolution: CGSize?
    var delegate: RTCEngineInternalDelegate?
    var canvas0: AgoraRtcVideoCanvas?
    
    override init() {
        super.init()
    }
    
    func createRtcEngine() {
        guard let token = KeyCenter.RTM_TOKEN else { return }
        let rtcEngineConfig = AgoraRtcEngineConfig()
        rtcEngineConfig.appId = KeyCenter.APP_ID
        rtcEngineConfig.areaCode = .global
        rtcEngine = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        // enable face detect extension
//        var res = rtcEngine?.enableExtension(withVendor: "agora_video_filters_face_capture", extension: "face_capture", enabled: true, sourceType: .primaryCamera)
//        res = rtcEngine?.setExtensionPropertyWithVendor("agora_video_filters_face_capture",
//                                                  extension: "face_capture", key: "face_capture_options", value: "{\"activationInfo\":{\"faceCapAppId\":\"0efd4ee02dd488c7c30cedd37b9b9b15\",\"faceCapAppKey\":\"e40886b37528408fe33b14871c516ed1\",\"agoraAppId\":\"4d4bf997732c4309911147503e91e338\",\"agoraRtmToken\":\"\(token)\",\"agoraUid\":\"\(KeyCenter.RTC_UID)\"},\"enable\":1}")
////
//        res = rtcEngine?.enableExtension(withVendor: "agora_video_filters_metakit", extension: "metakit", enabled: true, sourceType: .primaryCamera)

        rtcEngine?.setVideoFrameDelegate(self)
        rtcEngine?.setParameters("{\"rtc.audio.force_bluetooth_a2dp\": true}")
        rtcEngine?.setChannelProfile(.liveBroadcasting)
        rtcEngine?.setClientRole(.broadcaster)
//        rtcEngine?.enableVideo()
//        rtcEngine?.setExternalVideoSource(true, useTexture: true, sourceType: .videoFrame)

        let vec = AgoraVideoEncoderConfiguration(size: resolution!, frameRate: .fps30, bitrate: AgoraVideoBitrateStandard, orientationMode: .adaptative, mirrorMode: .enabled)
        rtcEngine?.setVideoEncoderConfiguration(vec)
        
//        rtcEngine?.startPreview()
    }
    
    func createMVStream() {
        let config = AgoraDataStreamConfig()
        config.ordered = true
        config.syncWithAudio = true
        rtcEngine?.createDataStream(&mvStreamId, config: config)
    }
        
    func createMetaService(userName: String, avatarUrl: String, delegate: AgoraMetaEventDelegate?) {
        playerName = userName
                
        currentUserInfo = AgoraMetaUserInfo.init()
        currentUserInfo?.userId = KeyCenter.RTM_UID
        currentUserInfo?.userName = userName
        currentUserInfo?.userIconUrl = avatarUrl
        
        let metaServiceConfig = AgoraMetaServiceConfig()
        metaServiceConfig.appId = KeyCenter.APP_ID
        metaServiceConfig.rtmToken = KeyCenter.RTM_TOKEN ?? ""
        metaServiceConfig.userId = KeyCenter.RTM_UID
        metaServiceConfig.delegate = delegate
        
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        metaServiceConfig.localDownloadPath = paths.first!
        metaServiceConfig.rtcEngine = rtcEngine
        metaService = AgoraMetaServiceKit.sharedMetaServiceWithConfig(_: metaServiceConfig)
        
        createMVStream()
    }

    func createScene(_ delegate: MetaChatSceneViewController/*, sceneBroadcastMode: AgoraMetachatSceneBroadcastMode*/) {
        let config = AgoraMetaSceneConfig()
        config.delegate = delegate
        config.enableFaceCapture = true
        config.faceCaptureCertificate = KeyCenter.FACE_CAPTURE_CERTIFICATE
        config.faceCaptureAppId = KeyCenter.FACE_CAPTURE_APP_ID
//        config.sceneBroadcastMode = sceneBroadcastMode
        metaService?.createScene(config)
    }
    
    
    func enterScene(view: UIView/*, sceneBroadcastMode: AgoraMetachatSceneBroadcastMode*/) {
        guard let sceneInfo = currentSceneInfo else {
            return
        }
        
        let avatarInfo = AgoraMetaAvatarModelInfo.init()
        for info in sceneInfo.bundles {
            if info.bundleType == .avatar {
                avatarInfo.bundleCode = info.bundleCode;
                break
            }
        }
        avatarInfo.localVisible = true
        avatarInfo.remoteVisible = true
        avatarInfo.syncPosition = true
        
        let enterSceneConfig = AgoraMetaEnterSceneConfig()
        enterSceneConfig.roomName = KeyCenter.CHANNEL_ID
        enterSceneConfig.sceneView = view
        enterSceneConfig.sceneId = sceneInfo.sceneId
//        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
//        enterSceneConfig.scenePath = paths.first! + "/15"
        let dict = ["sceneIndex": kSceneIndex.rawValue]
        let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let extraInfo = String(data: data!, encoding: String.Encoding.utf8)
        enterSceneConfig.extraInfo = extraInfo!.data(using: String.Encoding.utf8)
//        if sceneBroadcastMode == .audience {
//            enterSceneConfig.broadcaster = braodcaster
//        }
        
        localUserAvatar = metaScene?.getLocalUserAvatar()
        localUserAvatar?.setUserInfo(currentUserInfo)
        localUserAvatar?.setModelInfo(avatarInfo)

        let dict1 = ["avatar": "girl", "dress": [10000, 10100], "face": [["key": "eyeBlink_L", "val": 30] as [String : Any]], "2dbg": ""] as [String : Any]
        let data1 = try? JSONSerialization.data(withJSONObject: dict1, options: [])
        let extraInfo1 = String(data: data1!, encoding: String.Encoding.utf8)
        localUserAvatar?.setExtraInfo(extraInfo1!.data(using: String.Encoding.utf8))
        
//        metaScene?.enableBroadcast(true)
//        metaScene?.enableVideo(MetaChatSceneViewController.renderView, enable: true)
        
        metaScene?.enter(enterSceneConfig)
    }
    
    func updateIsVisitor(isVisitor: Bool) {
        
        guard let sceneInfo = currentSceneInfo else {
            return
        }
        let avatarInfo = AgoraMetaAvatarModelInfo.init()
        for info in sceneInfo.bundles {
            if info.bundleType == .avatar {
                avatarInfo.bundleCode = info.bundleCode;
                break
            }
        }
        avatarInfo.localVisible = true
        avatarInfo.remoteVisible = true
        avatarInfo.syncPosition = !isVisitor
        localUserAvatar = metaScene?.getLocalUserAvatar()
        localUserAvatar?.setUserInfo(currentUserInfo)
        localUserAvatar?.setModelInfo(avatarInfo)
        localUserAvatar?.applyInfo()
    }
    
    func joinRtcChannel(success: @escaping () -> Void) {
        let localSpatialConfig = AgoraLocalSpatialAudioConfig()
        localSpatialConfig.rtcEngine = self.rtcEngine
        localSpatial = AgoraLocalSpatialAudioKit.sharedLocalSpatialAudio(with: localSpatialConfig)
        localSpatial?.muteLocalAudioStream(false)
        localSpatial?.muteAllRemoteAudioStreams(false)
        localSpatial?.setAudioRecvRange(6)
        localSpatial?.setDistanceUnit(1)
        
       rtcEngine?.joinChannel(byToken: KeyCenter.RTC_TOKEN, channelId: KeyCenter.CHANNEL_ID, info: nil, uid: KeyCenter.RTC_UID, joinSuccess: { String, UInt, Int in
           DLog("joinChannel 回调 ======= ",String,UInt,Int)
            self.rtcEngine?.muteAllRemoteAudioStreams(true)
            success()
       })
//        let mediaOptions = AgoraRtcChannelMediaOptions()
//        mediaOptions.publishCameraTrack = true
//        rtcEngine?.joinChannel(byToken: KeyCenter.RTC_TOKEN, channelId: KeyCenter.CHANNEL_ID, uid: KeyCenter.RTC_UID, mediaOptions: mediaOptions) { String, UInt, Int in
//            DLog("joinChannel 回调 ======= ",String,UInt,Int)
//             self.rtcEngine?.muteAllRemoteAudioStreams(true)
//             success()
//        }
    }
    
    func leaveRtcChannel() {
        AgoraLocalSpatialAudioKit.destroy()
        localSpatial = nil
        if isSinging {
            broadcastKTVFinishMessage()
            isSinging = false
        }
        rtcEngine?.leaveChannel()
    }
    
    func leaveScene() {
        metaScene?.leave()
        
        tvPlayerMgr?.destroy()
        tableNPCPlayerMgr?.destroy()
        moveNPCPlayerMgr1?.destroy()
        moveNPCPlayerMgr2?.destroy()
        tvPlayerMgr = nil
        tableNPCPlayerMgr = nil
        moveNPCPlayerMgr1 = nil
        moveNPCPlayerMgr2 = nil
        
        isSinging = false
    }
    
    func resetMetachat() {
        
        metaScene?.destroy()
        metaScene = nil
        
        currentSceneInfo = nil
        
        playerName = nil
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
    
    func startPreview(_ view: UIView) {
        if canvas0 != nil {
            canvas0 = nil
        }
        canvas0 = AgoraRtcVideoCanvas()
        canvas0?.uid = KeyCenter.RTC_UID
        canvas0?.renderMode = .hidden
        canvas0?.view = view
        canvas0?.position = .postCaptureOrigin
        rtcEngine?.setupLocalVideo(canvas0)
        rtcEngine?.startPreview()
    }
    
    func stopPreview() {
        rtcEngine?.stopPreview()
    }
    
    // 创建并打开电视播放器
    func createAndOpenTVPlayer(resourceUrl:String = kAdvertisingURL, firstOpenCompleted: @escaping ((_ player: AgoraRtcMediaPlayerProtocol)->()), playBackAllLoopsCompleted:(()->())?) {
        tvPlayerMgr = MetaChatPlayerManager(displayId: .tv, resourceUrl: resourceUrl, metaScene: metaScene, rtcEngine: rtcEngine)
        tvPlayerMgr?.player?.setVideoFrameDelegate(self)
        tvPlayerMgr?.openComplteted = { player, isfirstOpen in
            if isfirstOpen {
                player.mute(true)
                firstOpenCompleted(player)
            }
            KTVConsoleManager.shared().resetConfig()
        }

        tvPlayerMgr?.playBackAllLoopsCompleted = { _ in
            playBackAllLoopsCompleted?()
        }
        tvPlayerMgr?.didChangedToPosition = { [weak self] player, position in
            guard let wSelf  = self else { return }
            if wSelf.isSinging {
                let mvUrl = player.getPlaySrc()
                wSelf.broadcastKTVSeekMessage(mvUrl: mvUrl, position: position)
            }
        }
        initalConsoleConfig()
    }
    
    func createAndOpenNPCPlayer(tableNPCOpenCompleted: @escaping ((_ player: AgoraRtcMediaPlayerProtocol)->())){
        func filePathWithName(fileName:String) -> String{
            guard let url = Bundle.main.path(forResource: fileName, ofType: "m4a") else { return ""}
            return url
        }
        tableNPCPlayerMgr = MetaChatPlayerManager(displayId: .npc_table, resourceUrl: filePathWithName(fileName: npcTableFileName), metaScene: metaScene, rtcEngine: rtcEngine, openCompleted: { player, _  in
            player.mute(true)
            tableNPCOpenCompleted(player)
        })
        moveNPCPlayerMgr1 = MetaChatPlayerManager(displayId: .npc_move1, resourceUrl: filePathWithName(fileName: npc1MoveFileName), metaScene: metaScene, rtcEngine: rtcEngine, openCompleted: { player, _ in
            player.mute(true)
        })
        moveNPCPlayerMgr2 = MetaChatPlayerManager(displayId: .npc_move2, resourceUrl: filePathWithName(fileName: npc2MoveFileName), metaScene: metaScene, rtcEngine: rtcEngine, openCompleted: { player, _ in
            player.mute(true)
        })
    }
    
    func changeTVUrl(_ newUrl: String) {
        tvPlayerMgr?.changeTVUrl(newUrl)
    }
    
    // 初始化控制台的设置
    private func initalConsoleConfig(){
        guard let rtcEngine = self.rtcEngine, let player = self.tvPlayerMgr?.player else {
            return
        }
        let console = KTVConsoleManager.shared()
        console.setRtcEngine(rtcEngine, player: player)
    }
    
    func resetTV() {
        changeTVUrl(kAdvertisingURL)
    }
    
    func setTVoff() {
        stopPushVideo(displayId: UInt32(MetaChatDisplayID.tv.rawValue))
    }
    
    // 停止投屏
    func stopPushVideo(displayId:UInt32){
        tvPlayerMgr?.player?.stop()
        metaScene?.enableVideoDisplay(String(displayId), enable: false)
    }
    
    // 发送消息
    func sendMessage(dic:[String: Any]) {
        if let data = try? JSONSerialization.data(withJSONObject:dic, options: .fragmentsAllowed) {
            metaScene?.sendMessage(data)
        }
    }
    
    // 设置电视的空间音效
    private func setUpSpatialForMediaPlayer(_ player: AgoraRtcMediaPlayerProtocol?, position: [NSNumber], forward:[NSNumber]) {
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
    
    func updateSpatialForMediaPlayer(id:ObjectID, postion: [NSNumber], forward:[NSNumber] = [0,0,1]) {
        var player: AgoraRtcMediaPlayerProtocol?
        
        switch id {
        case .tv:
            player = tvPlayerMgr?.player
        case .npcTable:
            player = tableNPCPlayerMgr?.player
        case .npcMove1:
            if moveNPCPlayerMgr1?.isOpenCompleted ?? false {
                player = moveNPCPlayerMgr1?.player
            }
        case .npcMove2:
            if moveNPCPlayerMgr2?.isOpenCompleted ?? false {
                player = moveNPCPlayerMgr2?.player
            }
        }
        setUpSpatialForMediaPlayer(player, position: postion, forward: forward)
    }
    
    // 广播一条歌曲同步的消息
    func broadcastKTVFinishMessage() {
        broadcastKTVMessage(type: .finish, message: [:])
    }
    
    // 广播一条k歌结束的消息
    func broadcastKTVSeekMessage( mvUrl:String, position:Int) {
        let isOriginal = KTVConsoleManager.shared().originalSong
        let localVoicepitch = KTVConsoleManager.shared().localVoicePitch
        let accompanyVolumn = KTVConsoleManager.shared().accompanyVolume
        let audioEffect = KTVConsoleManager.shared().audioEffectPreset.preset
        let console = ["origin":isOriginal,"pitch":localVoicepitch,"accompany": accompanyVolumn,"effect":audioEffect.rawValue] as [String : Any]
        let json = ["mv":mvUrl,"pos":position,"console": console] as [String : Any]
        broadcastKTVMessage(type: .seek, message: json)
    }
    
   @objc func broadcastKTVConsoleMessage(isOriginal:Bool, localVoicepitch: Double, accompanyVolumn:Int, audioEffect: AgoraAudioEffectPreset) {
//        let json = ["origin":isOriginal,"pitch":localVoicepitch,"accompany":accompanyVolumn,"effect":audioEffect.rawValue] as [String : Any]
//        broadcastKTVMessage(type: .console, message: json)
    }
    
    // 广播k歌消息
    private func broadcastKTVMessage(type:KTVSteamDataMessageType, message:[String: Any]) {
        let json = [
            "t": type.rawValue,
            "msg": message
        ] as [String : Any]
        if let data = try? JSONSerialization.data(withJSONObject:json , options: .fragmentsAllowed) {
            if let ret = rtcEngine?.sendStreamMessage(mvStreamId, data: data) {
                DLog("sendStreamMessage error === \(ret) streamid = \(mvStreamId) jsonStr == \(json)")
            }
        }
    }
    
    func handleKTVSeekMessage(_ message:[String: Any])  {
        // 如果正在唱歌 不接收别人的seek消息
        if isSinging {
            return
        }
        let mv: String = message["mv"] as! String
        let pos: Int = message["pos"] as! Int
        let mediaPlayer = tvPlayerMgr?.player
        if mediaPlayer?.getPlaySrc() == mv {
            guard let currentPos = mediaPlayer?.getPosition() else { return }
            if abs(currentPos - pos) > 3000  {
                mediaPlayer?.seek(toPosition: pos)
            }
        }else{
            mediaPlayer?.stop()
            mediaPlayer?.open(mv, startPos: pos)
        }
        
        if let console = message["console"] as? [String: Any] {
            handleConsoleChangedMessage(console)
        }
        DLog("receiveStreamMessageFromUid message == \(message) 当前线程 == \(Thread.current)")
    }
    
    func handleKTVFinishMessage(){
        resetTV()
    }
    
    func handleConsoleChangedMessage(_ message: [String: Any]){
        let tvPlayer = tvPlayerMgr?.player
        let isOrigin: Bool = message["origin"] as! Bool
        let pitch: Double = message["pitch"] as! Double
        let accompany: Int = message["accompany"] as! Int
        let effect: Int = message["effect"] as! Int
        tvPlayer?.selectAudioTrack(isOrigin ? 0: 1)
        self.rtcEngine?.setLocalVoicePitch(pitch)
        tvPlayer?.adjustPlayoutVolume(Int32(accompany))
        tvPlayer?.adjustPublishSignalVolume(Int32(accompany))
        self.rtcEngine?.setAudioEffectPreset(AgoraAudioEffectPreset(rawValue: effect)!)
    }
}

extension MetaServiceEngine: AgoraRtcEngineDelegate {

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        localSpatial?.removeRemotePosition(uid)
        self.delegate?.rtcEngine(engine, didOfflineOfUid: uid, reason: reason)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        DLog("receiveStreamMessageFromUid  === 收到了消息, streamId == \(streamId)")
        if let jsonObj = try? JSONSerialization.jsonObject(with: data, options: .fragmentsAllowed) as? [String: Any] {
            DLog("jsonObj === ",jsonObj)
            guard let t = jsonObj["t"] as? Int else { return }
            let type = KTVSteamDataMessageType(rawValue: t)
            guard let msg = jsonObj["msg"] as? [String: Any] else {return}
            switch type {
            case .seek:
                handleKTVSeekMessage(msg)
            case .finish:
                handleKTVFinishMessage()
//            case .console:
//                handleConsoleChangedMessage(msg)
            case .none:
                break
            }
           
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurStreamMessageErrorFromUid uid: UInt, streamId: Int, error: Int, missed: Int, cached: Int) {
        DLog("didOccurStreamMessageErrorFromUid  === 收到了消息报错 \(streamId),error == \(error)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        DLog("didJoinedOfUid  === 加入的uid \(uid)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStateChangedOfUid uid: UInt, state: AgoraVideoRemoteState, reason: AgoraVideoRemoteReason, elapsed: Int) {
//        if state == .starting {
//            braodcaster = String(uid)
//            let rvc = AgoraRtcVideoCanvas()
//            rvc.uid = uid
//            rvc.view = mockRenderView
//            rvc.renderMode = .fit
//            rvc.mirrorMode = .enabled
//            rtcEngine?.setupRemoteVideo(rvc)
//        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            self.delegate?.rtcEngine(engine, firstRemoteVideoDecodedOfUid: uid, size: size, elapsed: elapsed)
        }
    }
}

extension MetaServiceEngine: AgoraRtcMediaPlayerVideoFrameDelegate {
    
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didReceiveVideoFrame videoFrame: AgoraOutputVideoFrame) {
        guard let pixelBuffer = videoFrame.pixelBuffer, let data = LibyuvHelper.i420Buffer(of: pixelBuffer) else {
            return
        }
        
        let vf = AgoraVideoFrame()
        vf.format = 1
        vf.strideInPixels = Int32(videoFrame.width)
        vf.height = Int32(videoFrame.height)
        vf.dataBuf = data
        metaScene?.pushVideoFrame(toDisplay: "1", frame: vf)
        DLog("didReceive videoFrame:  width = \(videoFrame.width), height = \(videoFrame.height)", Thread.current,"time === ", Date.timeIntervalSinceReferenceDate)
    }
}

extension MetaServiceEngine: AgoraVideoFrameDelegate {
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
//        if let faceStr = videoFrame.metaInfo["KEY_FACE_CAPTURE"] as? String {
//
//            let dict = ["key": "faceCapture",
//                        "value": faceStr
//            ]
//            sendMessage(dic: dict)
//        }
        return true
    }
}
