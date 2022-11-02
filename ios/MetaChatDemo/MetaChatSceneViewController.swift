//
//  MetaChatSceneViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/26.
//

import UIKit
import UnityFramework
import AgoraRtcKit
import SDWebImage

private let kGuideShowKey = "kGuideShowKey"
// 新手引导文件名
private let kNoviceGuideFileName = "Beginner_guide"
// 游客模式
private let kVisitorTipFileName = "Visitor_guide"


class MetaChatSceneViewController: AgoraMetaViewController, UISplitViewControllerDelegate {
    
    @IBOutlet weak var avatarBackV: UIView!
    @IBOutlet weak var avatarImageV: UIImageView!
    @IBOutlet weak var nameL: UILabel!
    @IBOutlet weak var modeL: UILabel!
    @IBOutlet weak var openMicB: UIButton!
    @IBOutlet weak var userMicB: UIButton!
    @IBOutlet weak var userSpeakerB: UIButton!
    @IBOutlet weak var songListButton: UIButton!
    @IBOutlet weak var chatButton: UIButton!
    @IBOutlet weak var guideButton: UIButton!
    @IBOutlet weak var settingButton: UIButton!
    @IBOutlet weak var visitorTipsButton: UIButton!
    @IBOutlet weak var openMicLeftCon: NSLayoutConstraint!
    
    private lazy var avatarBackGradientLayer:CALayer = {
        let layer = CAGradientLayer()
        layer.colors = [UIColor(hexRGB: 0x6730CC).cgColor, UIColor(hexRGB: 0x000000,alpha: 0.3)]
        layer.startPoint = CGPoint(x: 0, y: 0)
        layer.endPoint = CGPoint(x: 1.0, y: 0)
        layer.frame = CGRect(x: 0, y: 0, width: 180, height: 36)
        return layer
    }()
    
    private var sceneManager = MCChatSceneManager()
    
    private var ktvContainerSelectedIndex = 0; // ktv默认选中的index
    private var ktvChooseContainerIndex = 0; // 点歌默认选中的index
    
    @objc var avatarInfo: AgoraMetachatAvatarInfo!
    @objc var room: MCRoom!
    @objc var userInfo: MCUserInfo!
    
    private var chatVC :MCChatViewController!
    private var ktvVC: KTVContainerViewController?

    
    deinit {
        MCRoomManager.shared.remoteDelegate(self)
        MetaChatEngine.sharedEngine.leaveScene()
        DLog("===========MetaChatSceneViewController销毁了=======")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        MetaChatEngine.sharedEngine.delegate = self
        setUI()
        addObserver()
        perform(#selector(initUnity), with: nil, afterDelay: 0)
        sceneManager.initializeManager()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        MetaChatEngine.sharedEngine.enterScene(avatarInfo: avatarInfo)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if room.master == KeyCenter.RTM_UID {
            MCRoomManager.shared.deleteRoom(room.objectId!)
        }else{
            MCRoomManager.shared.leaveRoom(room.objectId!)
        }
    }
    
    private func setUI() {
        updateUserInfo()
        modeL.text = NSLocalizedString("Visitor mode", comment: "")
        openMicB.layer.borderColor = UIColor(hexRGB: 0xffffff, alpha: 0.15).cgColor
        openMicB.layer.borderWidth = 1;
    }
    
    private func updateUserInfo() {
        nameL.text = userInfo.nickname
        let image = UIImage(named: userInfo.headImg)
        if image != nil {
            avatarImageV.image = image
        }else{
            avatarImageV.sd_setImage(with: URL(string: userInfo.headImg))
        }
    }
    
    override func unityDidUnload() {
        super.unityDidUnload()
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
    
//    override var prefersStatusBarHidden: Bool {
//        return true
//    }
    
    func showUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userMicB.isHidden = true
        userSpeakerB.isHidden = false
        chatButton.isHidden = false
        settingButton.isHidden = false
        guideButton.isHidden = false
        guideButton.titleLabel?.numberOfLines = 2
    }
    
    func addObserver() {
        NotificationCenter.default.addObserver(forName: kOnConnectionStateChangedNotifyName, object: nil, queue: nil) {[weak self] noti in
            guard let state = noti.userInfo?["state"], let reason = noti.userInfo?["reason"] else { return }
            self?.ex_showAlert(withTitle: "exit", message: "state: \(state),reason: \(reason)") {[weak self] alert in
                self?.dismiss(animated: true, completion: nil)
                self?.sceneManager.destory()
            }
        }
        MCRoomManager.shared.addDelegate(self)
        userInfo.addObserver(self, forKeyPath: "nickname", context: nil)
        userInfo.addObserver(self, forKeyPath: "badge", context: nil)
    }
    
    // 结束k歌
    private func finishKTV(){
        songListButton.isHidden = true
        ktvVC?.dismiss(animated: true)
    }
    
    // 显示k歌页面
    private func showKTV(){
        songListButton.isHidden = false
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
    
    // MARK: -  新手引导
    
    private func tryShowGuideVC(){
        let hasShow = UserDefaults.standard.bool(forKey: kGuideShowKey)
        if hasShow {
            return
        }
        UserDefaults.standard.set(true, forKey: kGuideShowKey)
        showGuideVC(title: NSLocalizedString("Beginner Guide", comment: ""),fileName: kNoviceGuideFileName)
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
    
    // 创建聊天窗口
    private func createChatVC(){
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        guard let chatVC = storyBoard.instantiateViewController(withIdentifier: "chatVC") as? MCChatViewController else { return }
        chatVC.roomId = room.objectId!
        chatVC.userInfo = userInfo
        chatVC.modalPresentationStyle = .overCurrentContext
        chatVC.delegate = self
        self.chatVC = chatVC
    }
    
    // MARK: -  UI actions
    
    @IBAction func openMicAction(sender: UIButton) {
        if sender.isSelected {
            MetaChatEngine.sharedEngine.closeMic()
            MetaChatEngine.sharedEngine.updateIsVisitor(isVisitor: true)
            sender.isSelected = false
            userMicB.isHidden = true
            modeL.text = NSLocalizedString("Visitor mode", comment: "")
//            guideButton.isHidden = false
            visitorTipsButton.isHidden = false
            avatarBackGradientLayer.removeFromSuperlayer()
            sender.backgroundColor = UIColor.clear
        }else {
            MetaChatEngine.sharedEngine.openMic()
            MetaChatEngine.sharedEngine.updateIsVisitor(isVisitor: false)
            sender.isSelected = true
            userMicB.isHidden = false
//            guideButton.isHidden = true
            visitorTipsButton.isHidden = true
            modeL.text = NSLocalizedString("Audio chat mode", comment: "")
            avatarBackV.layer.insertSublayer(avatarBackGradientLayer, at: 0)
            sender.backgroundColor = UIColor(hexRGB: 0x8850FB)
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
        sceneManager.muteAllSpeaker(!sender.isSelected)
        sender.isSelected = !sender.isSelected
    }
    
    
    @IBAction func didClickVisitorTips(_ sender: UIButton) {
        if !openMicB.isSelected {
            showGuideVC(title: NSLocalizedString("Visitor tips", comment:"") , fileName: kVisitorTipFileName)
        }
    }
    
    @IBAction func didClickGuideButton(sender: UIButton) {
        showGuideVC(title: NSLocalizedString("Beginner Guide", comment: ""),fileName: kNoviceGuideFileName)
    }
    
    @IBAction func didClickChatButton(_ sender: UIButton) {
        if chatVC == nil {
            createChatVC()
        }
        self.present(chatVC, animated: true)
    }
    
    
    @IBAction func didClickSettingButton(_ sender: UIButton) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        guard let mainVC = storyBoard.instantiateViewController(withIdentifier: "mainSettingVC") as? MCSettingMainVC else { return }
        mainVC.modalPresentationStyle = .overCurrentContext
        mainVC.userInfo = userInfo
        mainVC.room = room
        mainVC.sceneMgr = sceneManager
        mainVC.exitRoomBlock = {[weak self] in
            if self?.room.master == KeyCenter.RTM_UID  {
                self?.ex_showMCAlert(withTitle: NSLocalizedString("quit_room_alert_title", comment: ""), message: NSLocalizedString("quit_room_alert_msg", comment: ""), cancelTitle: NSLocalizedString("Cancel", comment: ""), confirmTitle: NSLocalizedString("Confirm", comment: ""), cancelHandler: {
                }, confirmHandler: {
                    self?.dismiss(animated: true)
                    self?.sceneManager.destory()
                })
            }else{
                self?.dismiss(animated: true)
                self?.sceneManager.destory()
            }
        }
        mainVC.dismissed = {[weak self] in
            self?.updateUserInfo()
        }
        self.present(mainVC, animated: true)
    }
}

extension MetaChatSceneViewController {
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "nickname" || keyPath == "badge" {
            sceneManager.unity_modifyUserInfo(name: userInfo.nickname, badge: userInfo.badge, userId: KeyCenter.RTM_UID)
        }
    }
}

extension MetaChatSceneViewController: MCSceneEngineDelegate {
    
    func joinRtcChannelSuccess() {
        openMicAction(sender: openMicB)
    }
    
    func onLeave() {
        self.unloadUnity()
    }
    
    func onEnter() {
        sceneManager.unity_sendLangCode()
        sceneManager.createAndOpenTVPlayer()
        sceneManager.createAndOpenNPCPlayer()
        showUI()
        tryShowGuideVC()
        createChatVC()
    }
    
    func onUpdateObjectPosition(id: ObjectID, position: [NSNumber], forward: [NSNumber]) {
        sceneManager.updateSpatialForMediaPlayer(id: id, postion: position, forward: forward)
    }
    
    func onReceiveStreamMessage(_ data: Data) {
        if let jsonObj = try? JSONSerialization.jsonObject(with: data, options: .fragmentsAllowed) as? [String: Any] {
            DLog("jsonObj === ",jsonObj)
            guard let t = jsonObj["t"] as? Int else { return }
            let type = KTVSteamDataMessageType(rawValue: t)
            guard let msg = jsonObj["msg"] as? [String: Any] else {return}
            switch type {
            case .seek:
                sceneManager.handleKTVSeekMessage(msg)
            case .finish:
                sceneManager.handleKTVFinishMessage()
            case .none:
                break
            }
        }
    }
    
    func onClickKTV() {
        sceneManager.startKTV()
        sceneManager.muteNPC(true)
        settingButton.isHidden = true
        openMicB.isHidden = true
        openMicLeftCon.constant = 90
        showKTV()
    }
    
    func onClickFinishKTV() {
        sceneManager.finishKTV()
        if !userSpeakerB.isSelected {
            sceneManager.muteNPC(false)
        }
        settingButton.isHidden = false
        openMicB.isHidden = false
        openMicLeftCon.constant = 139
        finishKTV()
    }
}

extension MetaChatSceneViewController: MCRoomManagerDelegate {
    func didLeave(_ roomId: String, reason aReason: MCRoomLeaveReason) {
        if (roomId == self.room.objectId || roomId == "") && aReason == .destroyed {
            self.ex_showMCAlert(withTitle:  NSLocalizedString("quit_room_notify_alert_title", comment: ""), message: nil, cancelTitle: nil, confirmTitle: NSLocalizedString("Confirm", comment: "")) {
            } confirmHandler: {[weak self] in
                self?.dismiss(animated: true)
                self?.sceneManager.destory()
            }

        }
    }
}

extension MetaChatSceneViewController: MCChatViewControllerDelegate {
    
    func chatVC(_ chatVC: MCChatViewController, didSendMessageContent content: String) {
        sceneManager.unity_modifyChatContent(content, for: KeyCenter.kEM_UserName)
    }
    
    func chatVC(_ chatVC: MCChatViewController, didReceiveMessageContent content: String, fromUserId: String) {
        sceneManager.unity_modifyChatContent(content, for: fromUserId)
    }
}
