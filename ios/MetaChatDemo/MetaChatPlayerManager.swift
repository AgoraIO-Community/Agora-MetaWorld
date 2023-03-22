//
//  NPCPlayerManager.swift
//  MetaChatDemo
//
//  Created by FanPengpeng on 2022/8/4.
//

import Foundation
import AgoraRtcKit

class MetaChatPlayerManager: NSObject {
    
    private (set) var displayId: MetaChatDisplayID!
    private (set) var player: AgoraRtcMediaPlayerProtocol?
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
        
        metachatScene?.enableVideoDisplay("1", enable: true)
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
    
    func changeTVUrl(_ newUrl: String) {
        player?.stop()
        player?.open(newUrl, startPos: 0)
//        mediaPlayer?.switchSrc(newUrl, syncPts: false)
    }
    
    func destroy() {
        player?.stop()
        rtcEngine?.destroyMediaPlayer(player)
        player = nil
    }
}


extension MetaChatPlayerManager: AgoraRtcMediaPlayerDelegate {
    
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
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
    
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo position: Int) {
        didChangedToPosition?(playerKit, position)
    }
}
