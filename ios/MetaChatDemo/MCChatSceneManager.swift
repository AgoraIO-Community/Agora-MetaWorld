//
//  MCChatSceneManager.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/26.
//

import UIKit

private let kAdvertisingURL = "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/15c116aca7590992f261143935d6f2cb.mov" // 宣传片地址
private let npcTableFileName = NSLocalizedString("npcTableFileName", comment: "")
private let npc1MoveFileName = NSLocalizedString("npc1MoveFileName", comment: "")
private let npc2MoveFileName = "moveNPC2"

private let longDistancePosition: [NSNumber] = [100.0,100.0,100.0]

enum MetaChatDisplayID:Int32 {
    case tv = 0
    case npc_table = 1
    case npc_move1
    case npc_move2
}

enum KTVSteamDataMessageType:Int {
    case seek = 0
    case finish = 1
}

class MCChatSceneManager: NSObject {

    
    @objc var rtcEngine: AgoraRtcEngineKit?
    var metachatScene: AgoraMetachatScene?
    
    /// 桌子旁边的NPC播放器
    @objc var tvPlayerMgr: MetaChatPlayerManager?
    /// 桌子旁边的NPC播放器
    @objc var tableNPCPlayerMgr: MetaChatPlayerManager?
    /// 移动NPC1
    var moveNPCPlayerMgr1: MetaChatPlayerManager?
    
    private var tvPosition = longDistancePosition
    private var tableNPCPosition = longDistancePosition
    private var moveNPCPosition = longDistancePosition
    private var moveNPCMute = false
    private var tvDefaultVolume: Int32 = 25
    private var npcDefaultVolume: Int32 = 5
    
    @objc var enableSpatialAudio = true {
        didSet {
//            let params = AgoraSpatialAudioParams()
//            tvPlayerMgr?.player?.setSpatialAudioParams(params)
//            tableNPCPlayerMgr?.player?.setSpatialAudioParams(params)
        }
    }
    
    /// 是否正在唱歌
    var isSinging = false
    
    private var seekTimes: Int = 0
    
    deinit {
        DLog("=====MCChatSceneManager =====销毁了 ====")
    }
    
    func destory() {
        setTVoff()
        if isSinging {
            KTVDataManager.shared().clear()
            broadcastKTVFinishMessage()
            isSinging = false
        }
        tvPlayerMgr?.destroy()
        tableNPCPlayerMgr?.destroy()
//        moveNPCPlayerMgr1?.destroy()
        tvPlayerMgr = nil
        tableNPCPlayerMgr = nil
//        moveNPCPlayerMgr1 = nil
    }
    
    func initializeManager() {
        rtcEngine = MetaChatEngine.sharedEngine.rtcEngine
        metachatScene = MetaChatEngine.sharedEngine.metachatScene
        addObserver()
    }
    
    private func initalConsoleConfig(){
        guard let rtcEngine = self.rtcEngine, let player = self.tvPlayerMgr?.player else {
            return
        }
        let console = KTVConsoleManager.shared()
        console.setRtcEngine(rtcEngine, player: player)
    }
    
    private func addObserver() {
         NotificationCenter.default.addObserver(forName: NSNotification.Name(kPlayingMusicWillChangeNotificationName), object: nil, queue: nil) {[weak self] noti in
             if let newSong: KTVMusic = noti.userInfo?[kNewPlayingMusic] as? KTVMusic {
                 guard let mvUrl = newSong.mvUrl else {
                     KTVNetworkHelper.mv(withSongCode: newSong.songCode) {[weak self] resource in
                         if let mv =  resource?.mvList.first?.mvUrl {
                             newSong.mvUrl = mv
                             self?.playNewSong(mvUrl: mv)
                         }
                     } fail: { _ in
                     }
                     return
                 }
                 self?.playNewSong(mvUrl: mvUrl)
             }else{
                 self?.resetTV()
             }
         }
     }
     
     /// 播放新的歌曲
     private func playNewSong(mvUrl:String) {
         DLog("mvurl =======>",mvUrl)
         changeTVUrl(mvUrl)
         isSinging = true
     }
    
    // 创建并打开电视播放器
    func createAndOpenTVPlayer(resourceUrl:String = kAdvertisingURL) {
        tvPlayerMgr = MetaChatPlayerManager(displayId: .tv, resourceUrl: resourceUrl, metachatScene: metachatScene, rtcEngine: rtcEngine)
        tvPlayerMgr?.player?.setVideoFrameDelegate(self)
        tvPlayerMgr?.player?.adjustPlayoutVolume(tvDefaultVolume)
        tvPlayerMgr?.player?.adjustPublishSignalVolume(tvDefaultVolume)
        tvPlayerMgr?.openComplteted = { [weak self] player, isfirstOpen in
            if isfirstOpen {
                player.mute(true)
                self?.requestTVPosition()
                KTVConsoleManager.shared().resetConfig()
                KTVConsoleManager.shared().accompanyVolume = Int(self?.tvDefaultVolume ?? 100)
            }
        }
        
        tvPlayerMgr?.playBackAllLoopsCompleted = { _ in
            KTVDataManager.shared().makeNextAsPlaying()
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
    
    func createAndOpenNPCPlayer(){
        func filePathWithName(fileName:String) -> String{
            guard let url = Bundle.main.path(forResource: fileName, ofType: nil) else { return ""}
            return url
        }
        tableNPCPlayerMgr = MetaChatPlayerManager(displayId: .npc_table, resourceUrl: filePathWithName(fileName: npcTableFileName), metachatScene: metachatScene, rtcEngine: rtcEngine, openCompleted: { [weak self] player, _  in
            player.mute(true)
            self?.requestNPCTablePosition()
        })
        tableNPCPlayerMgr?.player?.adjustPlayoutVolume(npcDefaultVolume)
        tableNPCPlayerMgr?.player?.adjustPublishSignalVolume(npcDefaultVolume)
//        moveNPCPlayerMgr1 = MetaChatPlayerManager(displayId: .npc_move1, resourceUrl: filePathWithName(fileName: npc1MoveFileName), metachatScene: metachatScene, rtcEngine: rtcEngine, openCompleted: { player, _ in
//            player.mute(true)
//        })
    }
    
    func muteNPC(_ mute: Bool) {
        moveNPCMute = mute
        let postion = mute ? longDistancePosition : tableNPCPosition
        MetaChatEngine.sharedEngine.setUpSpatialForMediaPlayer(tableNPCPlayerMgr?.player, position: postion, forward: [0,0,1])
//        let movePostion = mute ? longDistancePosition : moveNPCPosition
//        MetaChatEngine.sharedEngine.setUpSpatialForMediaPlayer(moveNPCPlayerMgr1?.player, position: movePostion, forward: [0,0,1])
    }
    
    func muteAllSpeaker(_ mute: Bool) {
        if isSinging == false {
            muteNPC(mute)
        }
        let postion = mute ? longDistancePosition : tvPosition
        MetaChatEngine.sharedEngine.setUpSpatialForMediaPlayer(tvPlayerMgr?.player, position: postion, forward: [0,0,1])
    }
    
    func startKTV(){
        KTVDataManager.shared().clear()
        isSinging = true
    }
    
    func finishKTV() {
        isSinging = false
        broadcastKTVFinishMessage()
        KTVDataManager.shared().clear()
        resetTV()
        // 关闭耳返
        KTVConsoleManager.shared().inEarmonitoring = false
    }
    
    func updateSpatialForMediaPlayer(id:ObjectID, postion: [NSNumber], forward:[NSNumber] = [0,0,1]) {
        var player: AgoraRtcMediaPlayerProtocol?
        
        switch id {
        case .tv:
            player = tvPlayerMgr?.player
            tvPosition = postion
        case .npcTable:
            player = tableNPCPlayerMgr?.player
            tableNPCPosition = postion
        case .npcMove1:
            DLog("npcMove1 deleted")
//            if moveNPCPlayerMgr1?.isOpenCompleted ?? false {
//                player = moveNPCPlayerMgr1?.player
//            }
//            moveNPCPosition = postion
//            if moveNPCMute {
//                return
//            }
        case .npcMove2:
            DLog("npcMove2 deleted")
        }
        if enableSpatialAudio == false {
            return
        }
        MetaChatEngine.sharedEngine.setUpSpatialForMediaPlayer(player, position: postion, forward: forward)
    }
    
   func changeTVUrl(_ newUrl: String) {
       tvPlayerMgr?.changeUrl(newUrl)
   }
   
   func resetTV() {
       changeTVUrl(kAdvertisingURL)
   }
   
    func setTVoff() {
       stopPushVideo(displayId: UInt32(MetaChatDisplayID.tv.rawValue))
   }
    
    // MARK: - public
    
    // 停止投屏
    func stopPushVideo(displayId:UInt32){
        tvPlayerMgr?.player?.stop()
        metachatScene?.enableVideoDisplay(displayId, enable: false)
    }
}

/// 发消息给unity
extension MCChatSceneManager {
    
    // 发送消息给unity
    func unity_sendMessage(dic:[String: Any]) {
        if let data = try? JSONSerialization.data(withJSONObject:dic, options: .fragmentsAllowed) {
            metachatScene?.sendMessage(toScene: data)
        }
    }
    
    // 获取电视位置
    private func requestTVPosition(){
        let dic = [
            "type":1,
            "params":["id":1]
        ] as [String : Any]
        unity_sendMessage(dic: dic)
    }
    
    // 获取NPC桌子位置
    private func requestNPCTablePosition(){
        let dic = [
            "type":1,
            "params":["id":2]
        ] as [String : Any]
        unity_sendMessage(dic: dic)
    }
    
    // 修改用户信息
    func unity_modifyUserInfo(name: String, badge: String, userId: String){
        let dic = [
            "type":4,
            "params":["userId":userId,"badge":badge,"name":name]
        ] as [String : Any]
        unity_sendMessage(dic: dic)
    }
    
    // 聊天消息
    func unity_modifyChatContent(_ content:String, for userId: String) {
        let dic = [
            "type":5,
            "params":["userId":userId,"content":content]
        ] as [String : Any]
        unity_sendMessage(dic: dic)
    }
    
    // 语言类型
    func unity_sendLangCode() {
        let langCode = Locale.current.languageCode
        let dic = [
            "type":6,
            "params":["lang":langCode]
        ] as [String : Any]
        DLog("langCode == \(langCode ?? "")")
        unity_sendMessage(dic: dic)
    }
}


extension MCChatSceneManager {
    
    // 广播一条k歌结束的消息
    func broadcastKTVFinishMessage() {
        broadcastKTVMessage(type: .finish, message: [:])
    }
    
    //  广播一条歌曲同步的消息
    func broadcastKTVSeekMessage( mvUrl:String, position:Int) {
        let isOriginal = KTVConsoleManager.shared().originalSong
        let localVoicepitch = KTVConsoleManager.shared().localVoicePitch
        let accompanyVolumn = KTVConsoleManager.shared().accompanyVolume
        let audioEffect = KTVConsoleManager.shared().audioEffectPreset.preset
        let console = ["origin":isOriginal,"pitch":localVoicepitch,"accompany": accompanyVolumn,"effect":audioEffect.rawValue] as [String : Any]
        let json = ["mv":mvUrl,"pos":position,"console": console,"time":Date().timeIntervalSince1970] as [String : Any]
        broadcastKTVMessage(type: .seek, message: json)
    }
    
    // 广播k歌消息
    private func broadcastKTVMessage(type:KTVSteamDataMessageType, message:[String: Any]) {
        let json = [
            "t": type.rawValue,
            "msg": message
        ] as [String : Any]
        if let data = try? JSONSerialization.data(withJSONObject:json , options: .fragmentsAllowed) {
            MetaChatEngine.sharedEngine.sendStreamMessage(data)
        }
    }
    
    func handleKTVSeekMessage(_ message:[String: Any])  {
        // 如果正在唱歌 不接收别人的seek消息
        if isSinging {
            return
        }
        let mv: String = message["mv"] as! String
        var pos: Int = message["pos"] as! Int
        let diff = 500
//        if let time: TimeInterval = message["time"] as? TimeInterval {
//            diff = Int((Date().timeIntervalSince1970 - time) * 1000)
//        }
        pos = pos + diff
        let mediaPlayer = tvPlayerMgr?.player
        if mediaPlayer?.getPlaySrc() == mv || mediaPlayer?.getPlaySrc() == MetaChatPlayerManager.localPathWithRemoteUrl(mv) {
            guard let currentPos = mediaPlayer?.getPosition() else { return }
            seekTimes += 1
            if seekTimes < 5 {
                return
            }
            if abs(currentPos - pos) > 1000  {
                mediaPlayer?.seek(toPosition: pos)
                DLog("seek ==== currentPos = \(currentPos), pos = \(pos),diff = \(diff)")
            }
            DLog(" xxxx currentPos = \(currentPos), pos = \(pos)")
        }else{
            mediaPlayer?.stop()
            mediaPlayer?.open(mv, startPos: pos)
//            tvPlayerMgr?.openRemoteUrl(mv)
            seekTimes = 0
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
        let pitch: Int = message["pitch"] as! Int
        let accompany: Int = message["accompany"] as! Int
        let effect: Int = message["effect"] as! Int
        tvPlayer?.selectAudioTrack(isOrigin ? 0: 1)
        tvPlayer?.setAudioPitch(pitch)
        tvPlayer?.adjustPlayoutVolume(Int32(accompany))
        tvPlayer?.adjustPublishSignalVolume(Int32(accompany))
        self.rtcEngine?.setAudioEffectPreset(AgoraAudioEffectPreset(rawValue: effect)!)
    }
}

extension MCChatSceneManager: AgoraRtcMediaPlayerVideoFrameDelegate {
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didReceive videoFrame: AgoraOutputVideoFrame) {
        guard let pixelBuffer = videoFrame.pixelBuffer, let data = LibyuvHelper.i420Buffer(of: pixelBuffer) else {
            return
        }
        
        let vf = AgoraVideoFrame()
        vf.format = 1
        vf.strideInPixels = Int32(videoFrame.width)
        vf.height = Int32(videoFrame.height)
        vf.dataBuf = data
        metachatScene?.pushVideoFrame(toDisplay: UInt32(MetaChatDisplayID.tv.rawValue), frame: vf)
//        DLog("didReceive videoFrame:  width = \(videoFrame.width), height = \(videoFrame.height)", Thread.current,"time === ", Date.timeIntervalSinceReferenceDate)
    }
}
