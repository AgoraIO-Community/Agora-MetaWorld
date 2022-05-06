//
//  MetaChatSceneViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/26.
//

import UIKit
import UnityFramework
import AgoraRtcKit

class MetaChatSceneViewController: AgoraMetaViewController {
    @IBOutlet weak var avatarBackV: UIView!
    @IBOutlet weak var avatarImageV: UIImageView!
    @IBOutlet weak var nameL: UILabel!
    @IBOutlet weak var modeL: UILabel!
    @IBOutlet weak var visitorIcon: UIImageView!
    @IBOutlet weak var openMicB: UIButton!

    @IBOutlet weak var userListB: UIButton!

    @IBOutlet weak var userMicB: UIButton!

    @IBOutlet weak var userSpeakerB: UIButton!

//    @IBOutlet weak var userListBackV: UIView!
    @IBOutlet weak var userListTableV: UITableView!
    
    @IBOutlet weak var visitorTipBack: UIView!
    
    @IBOutlet weak var tip1Label: UILabel!
    
    @IBOutlet weak var tip2Label: UILabel!

    @IBOutlet weak var tip3Label: UILabel!

    @IBOutlet weak var exitButton: UIButton!
    
    func setUI() {
        openMicB.setImage(UIImage.init(named: "onbtn"), for: .normal)
        openMicB.setImage(UIImage.init(named: "offbtn"), for: .selected)
        
        userMicB.setImage(UIImage.init(named: "microphone-on"), for: .normal)
        userMicB.setImage(UIImage.init(named: "microphone-off"), for: .selected)
        
        userSpeakerB.setImage(UIImage.init(named: "voice-on"), for: .normal)
        userSpeakerB.setImage(UIImage.init(named: "voice-off"), for: .selected)
        
        nameL.text = MetaChatEngine.sharedEngine.playerName
        modeL.text = "游客模式"
        
        visitorTipBack.layer.cornerRadius = 10.0
        visitorTipBack.layer.borderWidth = 4.0
        visitorTipBack.layer.borderColor = UIColor.init(red: 63.0/255.0, green: 69.0/255.0, blue: 83.0/255.0, alpha: 1.0).cgColor;
        
        tip1Label.layer.cornerRadius = 6.0
        tip1Label.layer.borderWidth = 1.0
        tip1Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;

        tip2Label.layer.cornerRadius = 6.0
        tip2Label.layer.borderWidth = 1.0
        tip2Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;

        tip3Label.layer.cornerRadius = 6.0
        tip3Label.layer.borderWidth = 1.0
        tip3Label.layer.borderColor = UIColor.init(red: 0xc3/255.0, green: 0xc0/255.0, blue: 0xc0/255.0, alpha: 1.0).cgColor;

    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUI()
        
        perform(#selector(initUnity), with: nil, afterDelay: 0)
    }
        
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        MetaChatEngine.sharedEngine.enterScene()
    }
    
//    override func unityDidLoaded(_ view: UIView!) {
//        super.unityDidLoaded(view)
//        MetaChatEngine.sharedEngine.enterScene()
//    }
    
    override func unityDidUnload() {
        super.unityDidUnload()
        
        self.dismiss(animated: true)
    }
    
    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .landscapeRight
    }

    func showUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = false
        visitorTipBack.isHidden = true
        exitButton.isHidden = false
    }
    
    @IBAction func openMicAction(sender: UIButton) {
        if sender.isSelected {
            MetaChatEngine.sharedEngine.closeMic()
            MetaChatEngine.sharedEngine.updateIsVisitor(isVisitor: true)
            sender.isSelected = false
            userMicB.isHidden = true
            modeL.text = "游客模式"
            visitorIcon.isHidden = false
        }else {
            MetaChatEngine.sharedEngine.openMic()
            MetaChatEngine.sharedEngine.updateIsVisitor(isVisitor: false)
            sender.isSelected = true
            userMicB.isHidden = false
            modeL.text = "语聊模式"
            visitorIcon.isHidden = true
        }
    }
    
    @IBAction func muteMicAction(sender: UIButton) {
        MetaChatEngine.sharedEngine.muteMic(isMute: !sender.isSelected)
        sender.isSelected = !sender.isSelected
    }
    
    @IBAction func muteSpeakerAction(sender: UIButton) {
        MetaChatEngine.sharedEngine.muteSpeaker(isMute: !sender.isSelected)
        sender.isSelected = !sender.isSelected
    }
    
    @IBAction func showVisitorTip(sender: UIButton) {
        if !openMicB.isSelected {
            visitorTipBack.isHidden = false
        }
    }
    
    @IBAction func hideVisitorTip(sender: UIButton) {
        visitorTipBack.isHidden = true
    }
    
    @IBAction func exit(sender: UIButton) {
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
    }
}

extension MetaChatSceneViewController: AgoraMetachatSceneEventDelegate {
    func metachatScene(_ scene: AgoraMetachatScene, onEnterSceneResult errorCode: Int) {
        DispatchQueue.main.async {
            self.showUI()
            MetaChatEngine.sharedEngine.joinRtcChannel()
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onLeaveSceneResult errorCode: Int) {
        MetaChatEngine.sharedEngine.resetMetachat()
        self.unloadUnity()
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onRecvMessageFromScene message: Data) {
        
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onUserPositionChanged uid: String, posInfo: AgoraMetachatPositionInfo) {
        
        if (uid.compare(MetaChatEngine.sharedEngine.userId) == .orderedSame) || (uid.compare("") == .orderedSame) {
            MetaChatEngine.sharedEngine.localSpatial?.updateSelfPosition(posInfo.position as! [NSNumber], axisForward: posInfo.forward as! [NSNumber], axisRight: posInfo.right as! [NSNumber], axisUp: posInfo.right as! [NSNumber])
        }else {
            let remotePositionInfo = AgoraRemoteVoicePositionInfo.init()
            remotePositionInfo.position = posInfo.position as! [NSNumber]
            remotePositionInfo.forward = posInfo.forward as? [NSNumber]
            
            MetaChatEngine.sharedEngine.localSpatial?.updateRemotePosition(UInt(uid) ?? 0, positionInfo: remotePositionInfo)
        }
    }
}
