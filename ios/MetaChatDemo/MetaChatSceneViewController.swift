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

private let COLLECTION_CELL_ID = "dressID"
private let kDressTypeMargin: CGFloat = 20
private let kDressTypeWidth: CGFloat = 60
private let kCellMargin: CGFloat = 10
let SCREEN_WIDTH: CGFloat = UIScreen.main.bounds.size.width
let SCREEN_HEIGHT: CGFloat = UIScreen.main.bounds.size.height

struct UserDressInfo: Codable {
    var gender: Int
    var hair: Int
    var tops: Int
    var lower: Int
    var shoes: Int
}

protocol handleDressInfoDelegate: NSObjectProtocol {
    func storeDressInfo(_ fromMainScene: Bool)
}

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
    
    @IBOutlet var cancelBtn: UIButton!
    
    @IBOutlet var storeBtn: UIButton!
    
    
    var delegate: handleDressInfoDelegate?
    var currentGender: Int = 1
    
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
    
    // dress setting
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var containerDressView: UIView!
    
    /// 换装UI数据
    lazy var dressType: [String] = ["hairpin_icon", "clothes_icon", "trousers_icon", "shoes_icon"]
    lazy var dressTypeToDresses_girl: Dictionary<String, Any> = ["hairpin_icon": hairpins_girl, "clothes_icon": clothes_girl, "trousers_icon": trousers_girl, "shoes_icon": shoes_girl]
    lazy var clothes_girl: [String] = ["girl_tops1", "girl_tops2", "girl_tops3", "girl_tops4"]
    lazy var shoes_girl: [String] = ["girl_shoes1", "girl_shoes2", "girl_shoes3", "girl_shoes4"]
    lazy var trousers_girl: [String] = ["girl_lower1", "girl_lower2", "girl_lower3", "girl_lower4"]
    lazy var hairpins_girl: [String] = ["girl_hair1", "girl_hair2", "girl_hair3", "girl_hair4"]
    
    lazy var dressTypeToDresses_boy: Dictionary<String, Any> = ["hairpin_icon": hairpins_boy, "clothes_icon": clothes_boy, "trousers_icon": trousers_boy, "shoes_icon": shoes_boy]
    lazy var clothes_boy: [String] = ["boy_tops1", "boy_tops2"]
    lazy var shoes_boy: [String] = ["boy_shoes1", "boy_shoes2"]
    lazy var trousers_boy: [String] = ["boy_lower1", "boy_lower2"]
    lazy var hairpins_boy: [String] = ["boy_hair1", "boy_hair2"]
    
    var userDressInfo: UserDressInfo = UserDressInfo(gender: 0, hair: 0, tops: 0, lower: 0, shoes: 0)
    var selectIndex: Int = 0
    var selectIndexPath: [IndexPath] = Array(repeating: IndexPath(), count: 4)
    
    /// 设置主界面UI
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
    
    /// 设置换装UI
    func setupDressUI() {
        setupScrollView()
        changeIcon(2, isSelected: true)
        
        userDressInfo.gender = currentGender
    }
    
    /// 设置scrollView
    func setupScrollView() {
        self.scrollView.backgroundColor = .white
        var offsetX: CGFloat = kDressTypeMargin

        for dress in dressType {

            let imageView = UIImageView()
            imageView.frame = CGRect(x: 0, y: 4, width: kDressTypeWidth, height: kDressTypeWidth - 8)

            imageView.image = UIImage.init(named: dress)

            var frame = imageView.frame
            frame.origin.x = offsetX
            imageView.frame = frame

            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(imageViewClicked(withGesture:)))
            imageView.addGestureRecognizer(tapGesture)
            imageView.isUserInteractionEnabled = true

            self.scrollView.addSubview(imageView)

            offsetX += imageView.frame.size.width + kDressTypeMargin
        }
    }
    
    private func createRenderView() {
        if MetaChatSceneViewController.renderView == nil {
            MetaChatSceneViewController.renderView = MetaChatEngine.sharedEngine.metachatScene?.createRenderView(.unity, region: CGRect(x: 0, y: 0, width: SCREEN_WIDTH, height: SCREEN_HEIGHT))
            MetaChatSceneViewController.renderView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        }
        
        self.view.insertSubview(MetaChatSceneViewController.renderView, at: 0)
    }
    
    deinit {
        DLog("===========MetaChatSceneViewController销毁了=======")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUI()
        addObserver()
        setupDressUI()
        createRenderView()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        getUserDressInfo()
        MetaChatEngine.sharedEngine.enterScene(view: MetaChatSceneViewController.renderView)
    }
    
    override var shouldAutorotate: Bool {
        return true
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return [.portrait, .landscapeRight, .landscapeLeft]
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    /// 显示主界面UI
    func showUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = false
        visitorTipBack.isHidden = true
        exitButton.isHidden = false
        switchBtn.isHidden = false
        cancelBtn.isHidden = true
        storeBtn.isHidden = true
        containerDressView.isHidden = true
    }
    
    /// 显示换装UI
    func showDressUI() {
        avatarBackV.isHidden = true
        openMicB.isHidden = true
        userListB.isHidden = true
        userMicB.isHidden = true
        userSpeakerB.isHidden = true
        visitorTipBack.isHidden = true
        exitButton.isHidden = true
        switchBtn.isHidden = true
        cancelBtn.isHidden = false
        storeBtn.isHidden = false
        containerDressView.isHidden = false
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
    
    /// 获取电视位置
    private func requestTVPosition(){
        let dic = [
            "type":1,
            "params":["id":1]
        ] as [String : Any]
        MetaChatEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    /// 获取NPC桌子位置
    private func requestNPCTablePosition(){
        let dic = [
            "type":1,
            "params":["id":2]
        ] as [String : Any]
        MetaChatEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    /// 加入房间
    private func joinChannel(){
        MetaChatEngine.sharedEngine.joinRtcChannel { [weak self] in
            guard let wSelf = self else {return}
            wSelf.openMicAction(sender: wSelf.openMicB)
        }
    }
    
    /// 打开npc播放器
    private func openNPC(){
        MetaChatEngine.sharedEngine.createAndOpenNPCPlayer {[weak self] player in
            self?.requestNPCTablePosition()
        }
    }
    
    /// 打开电视
    private func setTVon(){
        MetaChatEngine.sharedEngine.createAndOpenTVPlayer { [weak self] player in
            self?.requestTVPosition()
        } playBackAllLoopsCompleted: {
            KTVDataManager.shared().makeNextAsPlaying()
        }
    }
    
    /// 电视恢复原始
    private func resetTV() {
        MetaChatEngine.sharedEngine.resetTV()
    }
    
    /// 结束k歌
    private func finishKTV(){
        songListButton.isHidden = true
        MetaChatEngine.sharedEngine.isSinging = false
        MetaChatEngine.sharedEngine.broadcastKTVFinishMessage()
        ktvVC?.dismiss(animated: true)
        KTVDataManager.shared().clear()
    }
    
    /// 显示k歌页面
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
    
    /// 离开场景
    private func leaveScene(){
        MetaChatEngine.sharedEngine.setTVoff()
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
        KTVDataManager.shared().clear()
    }
    // MARK: -  新手引导
    
    private func tryShowGuideVC(){
        let hasShow = UserDefaults.standard.bool(forKey: kGuideShowKey)
        if hasShow {
            return
        }
        UserDefaults.standard.set(true, forKey: kGuideShowKey)
        showGuideVC(title: "游客模式",fileName: kNoviceGuideFileName)
    }
    
    /// 显示新手引导
    private func showGuideVC(title:String,fileName:String) {
        let guideVC = GuideAlertViewController()
        guideVC.modalTransitionStyle = .crossDissolve
        guideVC.modalPresentationStyle = .overCurrentContext
        guideVC.title = title
        guideVC.localFileName = fileName
        self.present(guideVC, animated: true)
    }
    
    /// MARK: -  换装设置
    private func dressSetting() {
        let dict = ["gender": userDressInfo.gender,
                    "hair": userDressInfo.hair,
                    "tops": userDressInfo.tops,
                    "lower": userDressInfo.lower,
                    "shoes": userDressInfo.shoes]
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8)
        let dic = [
            "key": "dressSetting",
            "value": str as Any
        ] as [String : Any]
        MetaChatEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    /// 选择换装信息
    private func selectDressInfo(typeIndex: Int, indexPath: IndexPath) {
        switch typeIndex {
            case 0: userDressInfo.hair = indexPath.row + 1
            case 1: userDressInfo.tops = indexPath.row + 1
            case 2: userDressInfo.lower = indexPath.row + 1
            case 3: userDressInfo.shoes = indexPath.row + 1
            default:
                print("index error!")
            break
        }
    }
    
    /// 保存换装信息
    private func saveUserDressInfo() {
        var fromMainScene = false
        if kSceneIndex == 1 {
            let defaultStand = UserDefaults.standard
            let key = (currentGender == 1) ? "mc_userDressInfo_girl" : "mc_userDressInfo_boy"
            defaultStand.set(Array: [userDressInfo], key: key)
        } else {
            fromMainScene = true
        }
        
        self.exit(sender: UIButton())
        if delegate != nil && ((delegate?.responds(to: Selector.init(("storeDressInfo")))) != nil) {
            delegate?.storeDressInfo(fromMainScene)
        }
    }
    
    /// 获取本地换装信息
    private func getUserDressInfo() {
        let defaultStand = UserDefaults.standard
        let key = (currentGender == 1) ? "mc_userDressInfo_girl" : "mc_userDressInfo_boy"
        let info = defaultStand.getObject(forKey: key) as [UserDressInfo]
        if info.count > 0 {
            userDressInfo = info[0] as UserDressInfo
            
            let dict = ["gender": userDressInfo.gender,
                        "hair": userDressInfo.hair,
                        "tops": userDressInfo.tops,
                        "lower": userDressInfo.lower,
                        "shoes": userDressInfo.shoes]
            let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
            let str = String(data: value!, encoding: String.Encoding.utf8)
            let dressInfo = AgoraMetachatDressInfo()
            dressInfo.extraCustomInfo = str!.data(using: String.Encoding.utf8)
            MetaChatEngine.sharedEngine.currentDressInfo = dressInfo
        }
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
            if (kSceneIndex == 0) {
                userMicB.isHidden = false
            } else {
                userMicB.isHidden = true
            }
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
            showGuideVC(title: "游客模式", fileName: kVisitorTipFileName)
        }
    }
    
    @IBAction func hideVisitorTip(sender: UIButton) {
        visitorTipBack.isHidden = true
    }
    
    @IBAction func exit(sender: UIButton) {
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
    }
    
    @IBAction func switchScene(sender: UIButton) {
        saveUserDressInfo()
    }
    
    @IBAction func exitDressScene(_ sender: UIButton) {
        self.exit(sender: UIButton())
    }
    
    @IBAction func storeDressInfo(_ sender: UIButton) {
        saveUserDressInfo()
    }
    
    
}

extension MetaChatSceneViewController: AgoraMetachatSceneEventDelegate {
    func metachatScene(_ scene: AgoraMetachatScene, onEnumerateVideoDisplaysResult displayIds: NSMutableArray) {
        
    }
    
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
            if kSceneIndex == 1 {
                self.dressSetting()
                self.showDressUI()
            }
            self.joinChannel()
            self.setTVon()
//            self.openNPC()
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

extension MetaChatSceneViewController {
    @objc func imageViewClicked(withGesture gesture: UITapGestureRecognizer) {

        guard let view = gesture.view,
            let index: Int = self.scrollView.subviews.index(of: view)
            else { return }

        selectIndex = index
        let indexPath = IndexPath(item: 0, section: 0)

        self.collectionView.scrollToItem(at: indexPath, at: .init(rawValue: 0), animated: false)

        for i in 0..<self.dressType.count {
            let iv = self.scrollView.subviews[i]
            if iv == gesture.view {
                self.changeIcon(i, isSelected: true)
            } else {
                self.changeIcon(i, isSelected: false)
            }
        }
        
        self.collectionView.reloadData()
    }
    
    func changeIcon(_ index: Int, isSelected: Bool) {
        let iv = self.scrollView.subviews[index] as! UIImageView
        if isSelected {
            iv.image = UIImage.init(named: self.dressType[index] + "1")
        } else {
            iv.image = UIImage.init(named: self.dressType[index])
        }
    }
}

extension MetaChatSceneViewController : UICollectionViewDataSource {
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        let dressTypeToDresses = (currentGender == 1) ? self.dressTypeToDresses_girl : self.dressTypeToDresses_boy
        let data = dressTypeToDresses[self.dressType[selectIndex]] as! Array<String>
        return data.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: COLLECTION_CELL_ID, for: indexPath) as! DressCollectionCell
        let dressTypeToDresses = (currentGender == 1) ? self.dressTypeToDresses_girl : self.dressTypeToDresses_boy
        let data = dressTypeToDresses[self.dressType[selectIndex]] as! Array<String>
        cell.dressImageView.image = UIImage.init(named: data[indexPath.row])
        
        cell.dressImageView.layer.borderColor = UIColor.clear.cgColor
        cell.dressImageView.layer.borderWidth = 0.0
        
        if selectIndexPath.count > selectIndex && selectIndexPath[selectIndex] == indexPath {
            cell.dressImageView.layer.borderColor = UIColor.lightGray.cgColor
            cell.dressImageView.layer.borderWidth = 1.0
        }
        
        return cell
    }
    
}

extension MetaChatSceneViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
        if indexPath == selectIndexPath[selectIndex] {
            let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
            dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
            dressCell.dressImageView.layer.borderWidth = 0.0
            
            selectIndexPath[selectIndex] = IndexPath()
            return
        }
        
        if indexPath != selectIndexPath[selectIndex] && !(selectIndexPath[selectIndex].isEmpty) {
            let tempIndexPath = selectIndexPath[selectIndex]
            let dressCell = collectionView.cellForItem(at: tempIndexPath) as! DressCollectionCell
            dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
            dressCell.dressImageView.layer.borderWidth = 0.0
        }
        
        let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
        dressCell.dressImageView.layer.borderColor = UIColor.lightGray.cgColor
        dressCell.dressImageView.layer.borderWidth = 1.0
        
        selectIndexPath[selectIndex] = indexPath
        
        self.selectDressInfo(typeIndex: selectIndex, indexPath: indexPath)
        self.dressSetting()
    }
    
    func collectionView(_ collectionView: UICollectionView, didDeselectItemAt indexPath: IndexPath) {
        let dressCell = collectionView.cellForItem(at: indexPath) as! DressCollectionCell
        dressCell.dressImageView.layer.borderColor = UIColor.clear.cgColor
        dressCell.dressImageView.layer.borderWidth = 0.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let cellWidth = (SCREEN_WIDTH - 3 * kCellMargin) / 4
        return CGSize(width: cellWidth, height: cellWidth);
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets.init(top: 0, left: 0, bottom: 0, right: 0)
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return kCellMargin
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return kCellMargin
    }
}

class DressCollectionCell: UICollectionViewCell {
    
    @IBOutlet weak var dressImageView: UIImageView!
    
}
