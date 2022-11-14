//
//  MetaChatSceneViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/26.
//

import UIKit
import UnityFramework
import AgoraRtcKit

private let kGuideShowKey = "kGuideShowKey"
// 新手引导文件名
private let kNoviceGuideFileName = "Novice guide"
// 游客模式
private let kVisitorTipFileName = "Novice guide"

class MetaChatSceneViewController: UIViewController {
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
    
    @IBOutlet weak var songListButton: UIButton!
    
    @IBOutlet weak var visitorTextView: UITextView!
    
    @IBOutlet weak var switchBtn: UIButton!
    
    /// 渲染的render view，多次进出场景需要保持同一renderView对象
    static var renderView: (UIView & AgoraMetaViewProtocol)!
    
    private var ktvContainerSelectedIndex = 0; // ktv默认选中的index
    private var ktvChooseContainerIndex = 0; // 点歌默认选中的index
    
    /// 桌子旁边的NPC播放器
    var tvPlayerMgr: MetaChatPlayerManager?
    /// 桌子旁边的NPC播放器
    var tableNPCPlayerMgr: MetaChatPlayerManager?
    /// 移动NPC1
    var moveNPCPlayerMgr1: MetaChatPlayerManager?
    /// 移动NPC2
    var moveNPCPlayerMgr2: MetaChatPlayerManager?
    
    
    private var ktvVC: KTVContainerViewController?
    
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
        
        if let url = Bundle.main.url(forResource: "Novice guide", withExtension: "rtfd") {
            visitorTextView.attributedText = try? NSAttributedString(url: url, documentAttributes: nil)
        }
    }
    
    deinit {
        DLog("===========MetaChatSceneViewController销毁了=======")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUI()
        addObserver()
        
        var width = UIScreen.main.bounds.size.width
        var height = UIScreen.main.bounds.size.height
        if width < height {
            let temp = width
            width = height
            height = temp
        }
        if MetaChatSceneViewController.renderView == nil {
            MetaChatSceneViewController.renderView = MetaChatEngine.sharedEngine.metachatScene?.createRenderView(.unity, region: CGRect(x: 0, y: 0, width: width, height: height))
        }
        
        self.view.insertSubview(MetaChatSceneViewController.renderView, at: 0)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        MetaChatEngine.sharedEngine.enterScene(view: MetaChatSceneViewController.renderView)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        MetaChatSceneViewController.renderView.removeFromSuperview()
    }
    
    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .landscapeRight
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    func showUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = false
        visitorTipBack.isHidden = true
        exitButton.isHidden = false
//        switchBtn.isHidden = false
    }
    
    func addObserver() {
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
        
        NotificationCenter.default.addObserver(forName: kOnConnectionStateChangedNotifyName, object: nil, queue: nil) {[weak self] noti in
            guard let state = noti.userInfo?["state"], let reason = noti.userInfo?["reason"] else { return }
            self?.ex_showAlert(withTitle: "exit", message: "state = \(state),reason == \(reason)") {[weak self] alert in
                self?.dismiss(animated: true, completion: nil)
            }
        }
    }
    
    /// 播放新的歌曲
    private func playNewSong(mvUrl:String) {
        DLog("mvurl =======>",mvUrl)
        MetaChatEngine.sharedEngine.changeTVUrl(mvUrl)
        MetaChatEngine.sharedEngine.isSinging = true
    }
    
    // 获取电视位置
    private func requestTVPosition(){
        let dic = [
            "type":1,
            "params":["id":1]
        ] as [String : Any]
        MetaChatEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    // 获取NPC桌子位置
    private func requestNPCTablePosition(){
        let dic = [
            "type":1,
            "params":["id":2]
        ] as [String : Any]
        MetaChatEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    // 加入房间
    private func joinChannel(){
        MetaChatEngine.sharedEngine.joinRtcChannel { [weak self] in
            guard let wSelf = self else {return}
            wSelf.openMicAction(sender: wSelf.openMicB)
        }
    }
    
    // 打开npc播放器
    private func openNPC(){
        MetaChatEngine.sharedEngine.createAndOpenNPCPlayer {[weak self] player in
            self?.requestNPCTablePosition()
        }
    }
    
    // 打开电视
    private func setTVon(){
        MetaChatEngine.sharedEngine.createAndOpenTVPlayer { [weak self] player in
            self?.requestTVPosition()
        } playBackAllLoopsCompleted: {
            KTVDataManager.shared().makeNextAsPlaying()
        }
    }
    
    // 电视恢复原始
    private func resetTV() {
        MetaChatEngine.sharedEngine.resetTV()
    }
    
    // 结束k歌
    private func finishKTV(){
        songListButton.isHidden = true
        MetaChatEngine.sharedEngine.isSinging = false
        MetaChatEngine.sharedEngine.broadcastKTVFinishMessage()
        ktvVC?.dismiss(animated: true)
        KTVDataManager.shared().clear()
    }
    
    // 显示k歌页面
    private func showKTV(){
        songListButton.isHidden = false
        MetaChatEngine.sharedEngine.isSinging = true
        
        ktvVC = KTVContainerViewController()
        ktvVC!.selectedIndexWhenDismiss = {[weak self] index, chooseIndex in
            self?.ktvContainerSelectedIndex = index
            self?.ktvChooseContainerIndex = chooseIndex
        }
        ktvVC!.defaultSelectedIndex = self.ktvContainerSelectedIndex
        ktvVC!.defaultChooseVCIndex = self.ktvChooseContainerIndex
        let navc = UINavigationController(rootViewController: ktvVC!)
        navc.modalTransitionStyle = .crossDissolve
        navc.modalPresentationStyle = .overCurrentContext
        navc.isNavigationBarHidden = true
        present(navc, animated: true)
    }
    
    // 离开场景
    private func leaveScene(){
        MetaChatEngine.sharedEngine.setTVoff()
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
//        MetaChatEngine.sharedEngine.resetMetachat()
        KTVDataManager.shared().clear()
    }
    // MARK: -  新手引导
    
    private func tryShowGuideVC(){
        let hasShow = UserDefaults.standard.bool(forKey: kGuideShowKey)
        if hasShow {
            return
        }
        UserDefaults.standard.set(true, forKey: kGuideShowKey)
        showGuideVC(title: "Novice guide",fileName: kNoviceGuideFileName)
    }
    
    // 显示新手引导
    private func showGuideVC(title:String,fileName:String) {
        let guideVC = GuideAlertViewController()
        guideVC.modalTransitionStyle = .crossDissolve
        guideVC.modalPresentationStyle = .overCurrentContext
        guideVC.title = title
        guideVC.localFileName = fileName
        self.present(guideVC, animated: true)
    }
    
    // MARK: -  UI actions
    
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
    
    @IBAction func didClickSongListButton(_ sender: UIButton) {
        showKTV()
    }
    
    
    @IBAction func muteSpeakerAction(sender: UIButton) {
        MetaChatEngine.sharedEngine.muteSpeaker(isMute: !sender.isSelected)
        sender.isSelected = !sender.isSelected
    }
    
    @IBAction func showVisitorTip(sender: UIButton) {
        if !openMicB.isSelected {
            showGuideVC(title: "Visitor tips", fileName: kVisitorTipFileName)
        }
    }
    
    @IBAction func hideVisitorTip(sender: UIButton) {
        visitorTipBack.isHidden = true
    }
    
    @IBAction func exit(sender: UIButton) {
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
//        self.dismiss(animated: true)
    }
    
    @IBAction func switchScene(sender: UIButton) {
        exit(sender: UIButton())
    }
}

extension MetaChatSceneViewController: AgoraMetachatSceneEventDelegate {
    func metachatScene(_ scene: AgoraMetachatScene, onReleasedScene errorCode: Int) {
        DispatchQueue.main.async {
            AgoraMetachatKit.destroy()
            MetaChatEngine.sharedEngine.metachatKit = nil
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onEnterSceneResult errorCode: Int) {
        DLog("进入场景===============")
        DispatchQueue.main.async {
            self.showUI()
            self.joinChannel()
            self.setTVon()
            self.openNPC()
            self.tryShowGuideVC()
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onLeaveSceneResult errorCode: Int) {
        DispatchQueue.main.async {
            MetaChatEngine.sharedEngine.resetMetachat()
            DLog("离开场景========errorCode: \(errorCode)")
        }
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
                MetaChatEngine.sharedEngine.updateSpatialForMediaPlayer(id: id, postion: position, forward: forward)
            case .didClickKTVBtn:
                DispatchQueue.main.async {
                    self?.showKTV()
                }
            case .didClickFinishKTVBtn:
                DispatchQueue.main.async {
                    self?.finishKTV()
                }
            }
        }
        
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onUserPositionChanged uid: String, posInfo: AgoraMetachatPositionInfo) {
        
        if (uid.compare(KeyCenter.RTM_UID) == .orderedSame) || (uid.compare("") == .orderedSame) {
            MetaChatEngine.sharedEngine.localSpatial?.updateSelfPosition(posInfo.position as! [NSNumber], axisForward: posInfo.forward as! [NSNumber], axisRight: posInfo.right as! [NSNumber], axisUp: posInfo.up as! [NSNumber])
            DLog("position = \(posInfo.position),forword = \(posInfo.forward),right = \(posInfo.right),up = \(posInfo.up)")
        }else {
            let remotePositionInfo = AgoraRemoteVoicePositionInfo()
            remotePositionInfo.position = posInfo.position as! [NSNumber]
            remotePositionInfo.forward = posInfo.forward as? [NSNumber]
            
            MetaChatEngine.sharedEngine.localSpatial?.updateRemotePosition(UInt(uid) ?? 0, positionInfo: remotePositionInfo)
        }
    }
}
