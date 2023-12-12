//
//  MetaServiceEngine.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//

import Foundation
import AgoraRtcKit

private let kAdvertisingURL = "https://download.agora.io/demo/test/agora_meta_ads.mov" // 宣传片地址
private let npcTableFileName = "tableNPC"
private let npc1MoveFileName = "moveNPC1"
private let npc2MoveFileName = "moveNPC2"

/// 场景index
enum MetaSceneIndex: Int {
    case live = 0
    case chat = 1
    case chatRoom = 2
    case faceCaptureChatRoom = 3
}

enum MetaDisplayID: Int {
    case tv = 0
    case npc_table = 1
    case npc_move1
    case npc_move2
}

var kSceneIndex: MetaSceneIndex = .chat

//var num = 0

protocol RTCEngineInternalDelegate: NSObject {
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int)
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason)
}

class MetaServiceEngine: NSObject {
    @objc static let sharedEngine = MetaServiceEngine()
    /// agora rtc engine
    @objc var rtcEngine: AgoraRtcEngineKit?
    /// media player nearby table
    private var tvPlayerMgr: MetaPlayerManager?
    /// meta service
    var metaService: AgoraMetaServiceKit?
    /// current scene info
    var currentSceneInfo: AgoraMetaSceneInfo?
    /// meta scene
    var metaScene: AgoraMetaScene?
    /// current user info
    var currentUserInfo: AgoraMetaUserInfo?
    /// used to set avatar info
    var localUserAvatar: AgoraMetaLocalUserAvatar?
    /// video encode resolution
    var resolution: CGSize?
    /// rtc engine custom delegate
    var delegate: RTCEngineInternalDelegate?
    /// used to render remote video
    var canvas0: AgoraRtcVideoCanvas?
    /// channel id
    var roomName: String = KeyCenter.CHANNEL_ID
    /// current avatar name
    var role: String?
    /// user name
    var userName: String?
    
    override init() {
        super.init()
    }
    
    func createRtcEngine() {
        let rtcEngineConfig = AgoraRtcEngineConfig()
        rtcEngineConfig.appId = KeyCenter.APP_ID
        rtcEngineConfig.areaCode = .global
        
        rtcEngine = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        rtcEngine?.setParameters("{\"rtc.audio.force_bluetooth_a2dp\": true}")
        rtcEngine?.setClientRole(.broadcaster)
        rtcEngine?.setChannelProfile(.liveBroadcasting)
        rtcEngine?.setVideoFrameDelegate(self)
        
        let vec = AgoraVideoEncoderConfiguration(size: resolution!, frameRate: .fps30, bitrate: AgoraVideoBitrateStandard, orientationMode: .adaptative, mirrorMode: .enabled)
        rtcEngine?.setVideoEncoderConfiguration(vec)
    }
    
    func createMetaService(userName: String, avatarUrl: String, delegate: AgoraMetaEventDelegate?) {
        self.userName = userName
        currentUserInfo = AgoraMetaUserInfo()
        currentUserInfo?.userId = KeyCenter.RTM_UID
        currentUserInfo?.userName = userName
        currentUserInfo?.userIconUrl = avatarUrl
        
        let msc = AgoraMetaServiceConfig()
        msc.appId = KeyCenter.APP_ID
        msc.rtmToken = KeyCenter.RTM_TOKEN ?? ""
        msc.userId = KeyCenter.RTM_UID
        msc.delegate = delegate
        
        let path = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        msc.localDownloadPath = path.first!
        msc.rtcEngine = self.rtcEngine
        metaService = AgoraMetaServiceKit.sharedMetaServiceWithConfig(msc)
    }
    
    func createMetaScene(_ delegate: AgoraMetaSceneEventDelegate?) {
        let config = AgoraMetaSceneConfig()
        config.delegate = delegate
        config.enableFaceCapture = false
        config.enableLipSync = true
        metaService?.createScene(config)
    }
    
    func enterMetaScene(_ view: UIView) {
        /// sceneInfo from getSceneAssetsInfo callback
        /// if load scene from local path, this is not used
        guard let sceneInfo = currentSceneInfo else { return }
        
        /// current avatar info
        let avatarInfo = AgoraMetaAvatarModelInfo()
        for info in sceneInfo.bundles {
            if info.bundleType == .avatar {
                avatarInfo.bundleCode = info.bundleCode;
                break
            }
        }
        avatarInfo.localVisible = true
        avatarInfo.remoteVisible = true
        avatarInfo.syncPosition = true
        
        let esc = AgoraMetaEnterSceneConfig()
        esc.roomName = roomName
        esc.sceneView = view
        esc.sceneId = sceneInfo.sceneId
        
        let displayConfig = AgoraMetaSceneDisplayConfig()
        displayConfig.width = Int(view.frame.width * view.layer.contentsScale)
        displayConfig.height = Int(view.frame.height * view.layer.contentsScale)
//        if num % 2 == 1 {
//            displayConfig.width = 0
//            displayConfig.height = 0
//        }
//        num += 1
        
        var _sceneIndex = kSceneIndex.rawValue
        if kSceneIndex == .chatRoom || kSceneIndex == .faceCaptureChatRoom { _sceneIndex = 0 }
        let dict = ["sceneIndex": _sceneIndex, "avatar": role ?? "boy", "dress": [10000, 10100], "face": [["key": "eyeBlink_L", "val": 30] as [String : Any]], "2dbg": ""] as [String : Any]
        let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let extraInfo = String(data: data!, encoding: String.Encoding.utf8)
        displayConfig.extraInfo = extraInfo?.data(using: String.Encoding.utf8)
        esc.displayConfig = displayConfig
        
        localUserAvatar = metaScene?.getLocalUserAvatar()
        localUserAvatar?.setModelInfo(avatarInfo)
        localUserAvatar?.setUserInfo(currentUserInfo)
        
        let sceneOptions = AgoraMetaSceneOptions()
        sceneOptions.lipSyncMode = .normal
        sceneOptions.motionCaptureType = .faceCapture
        sceneOptions.publishBlendShape = true
        sceneOptions.autoSubscribeBlendShape = true
        metaScene?.update(sceneOptions)
        
        metaScene?.enter(esc)
    }
    
    func leaveMetaScene() {
        metaScene?.leave()
    }
    
    func destroyMetaService() {
        metaScene?.destroy()
        metaScene = nil
        currentUserInfo = nil
        currentSceneInfo = nil
    }
    
    func joinChannel(_ success: @escaping () -> Void) {
        let mediaOptions = AgoraRtcChannelMediaOptions()
        if kSceneIndex == .chatRoom || kSceneIndex == .faceCaptureChatRoom {
            mediaOptions.publishCustomAudioTrack = true
            mediaOptions.autoSubscribeAudio = true
        }
        self.rtcEngine?.joinChannel(byToken: KeyCenter.RTM_TOKEN, channelId: roomName , uid: KeyCenter.RTC_UID, mediaOptions: mediaOptions, joinSuccess: { String, UInt, Int in
            DLog("===============加入频道成功！===============")
            success()
        })
    }
    
    func leaveChannel() {
        rtcEngine?.leaveChannel()
    }
    
    /// 发送消息
    func sendMessage(dic:[String: Any]) {
        if let data = try? JSONSerialization.data(withJSONObject:dic, options: .fragmentsAllowed) {
            metaScene?.sendMessage(data)
        }
    }
    
    func startPreview(_ view: UIView) {
        if (canvas0 != nil) {
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
    
    func createAndOpenTVPlayer(resourceUrl:String = kAdvertisingURL, firstOpenCompleted: @escaping ((_ player: AgoraRtcMediaPlayerProtocol)->()), playBackAllLoopsCompleted:(()->())?) {
        tvPlayerMgr = MetaPlayerManager(displayId: .tv, resourceUrl: resourceUrl, metaScene: metaScene, rtcEngine: rtcEngine)
        tvPlayerMgr?.player?.setVideoFrameDelegate(self)
    }
    
    /// 关闭电视
    func setTVoff() {
        stopPushVideo(displayId: UInt32(MetaDisplayID.tv.rawValue))
    }
    
    /// 停止投屏
    func stopPushVideo(displayId:UInt32){
        tvPlayerMgr?.player?.stop()
        metaScene?.enableVideoDisplay(String(displayId), enable: false)
    }
}

extension MetaServiceEngine: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        self.delegate?.rtcEngine(engine, didOfflineOfUid: uid, reason: reason)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.delegate?.rtcEngine(engine, firstRemoteVideoDecodedOfUid: uid, size: size, elapsed: elapsed)
        }
    }
}

extension MetaServiceEngine: AgoraRtcMediaPlayerVideoFrameDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didReceiveVideoFrame videoFrame: AgoraOutputVideoFrame) {
//        guard let pixelBuffer = videoFrame.pixelBuffer else { return }
        
    }
}

extension MetaServiceEngine: AgoraVideoFrameDelegate {
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        return true
    }
}
