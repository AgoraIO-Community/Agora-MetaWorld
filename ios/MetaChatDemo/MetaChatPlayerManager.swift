//
//  NPCPlayerManager.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/4.
//

import Foundation

class MetaChatPlayerManager: NSObject {
    
    private var downloadingDic: [String:Bool] = [String:Bool]()
    private (set) var remoteUrl:String!
    private (set) var displayId: MetaChatDisplayID!
    @objc private (set) var player: AgoraRtcMediaPlayerProtocol?
    private (set) var isOpenCompleted = false
    private var isFirstOpen = true
    private weak var rtcEngine: AgoraRtcEngineKit?
    /// open结束回调
    var openComplteted: ((_ player: AgoraRtcMediaPlayerProtocol, _ isFirstOpen: Bool)->())?
    /// 播放一首歌结束回调
    var playBackAllLoopsCompleted:((_ player: AgoraRtcMediaPlayerProtocol)->())?
    /// 播放进度发送变化
    var didChangedToPosition:((_ player: AgoraRtcMediaPlayerProtocol, _ postion: Int)->())?
    
    init(displayId: MetaChatDisplayID,resourceUrl url:String, metachatScene: AgoraMetachatScene?,rtcEngine: AgoraRtcEngineKit?, openCompleted: ((_ player: AgoraRtcMediaPlayerProtocol, _ isFirstOpen: Bool)->())? = nil) {
        super.init()
        
        let id = UInt32(displayId.rawValue)
        metachatScene?.enableVideoDisplay(id, enable: true)
        let npcPlayer = rtcEngine?.createMediaPlayer(with: self)
        npcPlayer?.setLoopCount(-1)
        npcPlayer?.adjustPlayoutVolume(15)
        npcPlayer?.adjustPublishSignalVolume(15)
        npcPlayer?.open(url, startPos: 0)
        self.displayId = displayId
        self.player = npcPlayer
        self.openComplteted = openCompleted
        self.rtcEngine = rtcEngine
    }
    
    func changeUrl(_ newUrl: String) {
        remoteUrl = newUrl
        player?.stop()
        player?.open(newUrl, startPos: 0)
//        openRemoteUrl(newUrl)
    }
    
    func openRemoteUrl(_ newUrl:String) {
        let localPath = MetaChatPlayerManager.localPathWithRemoteUrl(newUrl)
        if FileManager.default.fileExists(atPath: localPath) {
            player?.open(localPath, startPos: 0)
        }else{
            downloadAndOpenUrl(newUrl)
        }
    }
    
    private func downloadAndOpenUrl(_ newUrl: String) {
        let path = MetaChatPlayerManager.localPathWithRemoteUrl(newUrl)
        if downloadingDic[newUrl] == true {
            return
        }
        downloadingDic[newUrl] = true
        KTVNetworkHelper.downloadMV(newUrl, dir: MetaChatPlayerManager.ktvSongsDir()) {[weak self] err in
            self?.downloadingDic[newUrl] = false
            if err == nil {
                self?.player?.open(path, startPos: 0)
            }
        }
    }
    
    func destroy() {
        player?.stop()
        rtcEngine?.destroyMediaPlayer(player)
        if player != nil {
            DLog("播放器\(player!)销毁了")
        }
        player = nil
    }
}

extension MetaChatPlayerManager {
    static func ktvSongsDir() -> String {
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        let cachePath = paths.first
        guard let dir = cachePath?.appending("/ktvSongs/") else {
            return ""
        }
        if FileManager.default.fileExists(atPath: dir) {
            return dir
        }
        try? FileManager.default.createDirectory(at: URL(fileURLWithPath: dir), withIntermediateDirectories: true)
        return dir
    }
    
    static func localPathWithRemoteUrl(_ url:String) -> String {
        let ocUrl:NSString = url as NSString
        let filePath = ktvSongsDir().appending(ocUrl.md5())
        return filePath
    }
}


extension MetaChatPlayerManager: AgoraRtcMediaPlayerDelegate {
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted {
            playerKit.play()
            self.isOpenCompleted = true
            self.openComplteted?(playerKit, isFirstOpen)
            isFirstOpen = false
        }
        if state == .playBackAllLoopsCompleted {
            playBackAllLoopsCompleted?(playerKit)
        }
        DLog("AgoraMediaPlayerError === \(error.rawValue), state == \(state.rawValue)")
    }
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedToPosition position: Int) {
        didChangedToPosition?(playerKit, position)
    }
}
