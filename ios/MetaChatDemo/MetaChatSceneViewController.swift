//
//  MetaChatSceneViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/26.
//

import UIKit
import AgoraRtcKit
import SwiftyMenu
import MetalKit

private let kGuideShowKey = "kGuideShowKey"
// 新手引导文件名
private let kNoviceGuideFileName = "Novice guide"
// 游客模式
private let kVisitorTipFileName = "Novice guide"

private let COLLECTION_CELL_ID = "dressID"
private let kDressTypeMargin: CGFloat = 20
private let kDressTypeWidth: CGFloat = 60
private let kCellMargin: CGFloat = 10

struct UserDressInfo: Codable {
    var gender: Int
    var hair: Int
    var tops: Int
    var lower: Int
    var shoes: Int
}

class DressCollectionCell: UICollectionViewCell {
    
    var dressImageView: UIImageView!
    
}

class MockRenderView: UIView, AgoraMetaViewProtocol {
    
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
    
    @IBOutlet weak var cancelBtn: UIButton!
    
    @IBOutlet weak var storeBtn: UIButton!
    
    //    @IBOutlet weak var motionMenu: SwiftyMenu!
    
    @IBOutlet weak var chatTextField: UITextField!
    
    @IBOutlet weak var chatSendBtn: UIButton!
    
    @IBOutlet weak var addViewBtn: UIButton!
    var mainActionView: ActionView!
    var subActionView: ActionView!
    
    var delegate: handleDressInfoDelegate?
    var currentGender: Int = 1
    
    /// 渲染的render view，多次进出场景需要保持同一renderView对象
    static var renderView: (UIView & AgoraMetaViewProtocol)!
    static var avatarView: (UIView & AgoraMetaViewProtocol)!
    
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
    var collectionView: UICollectionView!
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
    
    var sceneBroadcastMode = AgoraMetachatSceneBroadcastMode.none   // 0: broadcast  1: audience
    static var frameIndex = 0
    
    private var menuItems: [String] = ["退出场景", "切换bgm"]
    private var subMenusItems: [String] = []
    private var broadcasterMenu: [Dictionary<String, Any>] = []
    private var audienceMenu: [Dictionary<String, Any>] = []
    private var subBroadcasterMenu: [Dictionary<String, Any>] = []
    private var subAudienceMenu: [Dictionary<String, Any>] = []
    
    // video collection view
    var remoteVideoCV: VideoCollectionView!
    var localVideoCV: VideoCollectionView!
    
    @IBOutlet weak var shRemoteViewBtn: UIButton!
    @IBOutlet weak var shLocalViewBtn: UIButton!
    
    //    var cameraManager: CameraManager!
    var previewView: UIView!
    var remoteUsers: Dictionary<String, UIView> = [:]
    
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
        setupCollcetionView()
        setupScrollView()
        changeIcon(2, isSelected: true)
        
        userDressInfo.gender = currentGender
    }
    
    /// 设置collectionView
    func setupCollcetionView() {
        let layout = UICollectionViewFlowLayout.init()
        layout.itemSize = CGSize.init(width: 90, height: 90)
        layout.scrollDirection = .horizontal
        layout.minimumLineSpacing = 20;
        self.collectionView = UICollectionView(frame: .init(x: 0, y: 60, width: SCREEN_WIDTH, height: containerDressView.frame.size.height - 60), collectionViewLayout: layout)
        self.collectionView.backgroundColor = .white
        self.containerDressView.addSubview(self.collectionView)
        self.collectionView.dataSource = self
        self.collectionView.delegate = self
        
        self.collectionView.register(DressCollectionCell.self, forCellWithReuseIdentifier: COLLECTION_CELL_ID)
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
        if sceneBroadcastMode == .audience {
            guard let view = MetaChatEngine.sharedEngine.mockRenderView else { return }
            self.view.insertSubview(view, at: 0)
            return
        }
        
        if MetaChatSceneViewController.renderView == nil {
            MetaChatSceneViewController.renderView = MetaChatEngine.sharedEngine.metachatScene?.createRenderView(.unity, region: CGRect(x: 0, y: 0, width: SCREEN_WIDTH, height: SCREEN_HEIGHT))
            MetaChatSceneViewController.renderView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        } else {
            if (MetaChatSceneViewController.renderView.frame.size.width != self.view.frame.size.width || MetaChatSceneViewController.renderView.frame.size.height != self.view.frame.size.height) {
                MetaChatSceneViewController.renderView.frame.size.width = self.view.frame.size.width
                MetaChatSceneViewController.renderView.frame.size.height = self.view.frame.size.height
            }
        }
        
        self.view.insertSubview(MetaChatSceneViewController.renderView, at: 0)
    }
    
    deinit {
        DLog("===========MetaChatSceneViewController销毁了=======")
        
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        MetaChatEngine.sharedEngine.delegate = self
        
        setUI()
        addObserver()
        setupDressUI()
        createRenderView()
        
        getMenuItems()
        //        setupMenu()
        
        //        setupActionView()
        //        setupChatSendBtn()
        
        setupRemoteVideoCV()
        setupLocalVideoCV()
        setupPreviewView()
        createAvatarView()
        
        let hideGesture = UITapGestureRecognizer.init(target: self, action: #selector(hideKeyboard))
        hideGesture.delegate = self
        view.addGestureRecognizer(hideGesture)
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(enterForeground(_:)), name:UIApplication.willEnterForegroundNotification, object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        getUserDressInfo()
        if sceneBroadcastMode == .audience {
            guard let view = MetaChatEngine.sharedEngine.mockRenderView else { return }
            MetaChatEngine.sharedEngine.enterScene(view: view, sceneBroadcastMode: sceneBroadcastMode)
        } else {
            MetaChatEngine.sharedEngine.enterScene(view: MetaChatSceneViewController.renderView, sceneBroadcastMode: sceneBroadcastMode)
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        if sceneBroadcastMode == .none {
            guard let view = MetaChatSceneViewController.renderView else { return }
            view.removeFromSuperview()
        } else {
            guard let view = MetaChatEngine.sharedEngine.mockRenderView else { return }
            view.removeFromSuperview()
        }
    }
    
    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    @objc func hideKeyboard() {
        view.endEditing(true)
        if let subView = subActionView {
            subView.isHidden = true
        }
    }
    
    @objc func keyboardWillShow(_ notification: NSNotification) {
        let keyboardFrame = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as! NSValue).cgRectValue
        let frame = chatTextField.frame
        let offset = frame.origin.y + 70 - (self.view.frame.size.height - (keyboardFrame.height))
        UIView.beginAnimations("ResizeForKeyboard", context: nil)
        UIView.setAnimationDuration(0.3)
        if offset > 0 {
            self.view.frame = CGRectMake(0, -offset, self.view.frame.size.width, self.view.frame.size.height)
            UIView.commitAnimations()
        }
    }
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        self.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)
    }
    
    @objc func enterForeground(_ notification: NSNotification) {
        if kSceneIndex == 0 {
            self.switchOrientation(isPortrait: false, isFullScreen: true)
        } else {
            self.switchOrientation(isPortrait: true, isFullScreen: true)
        }
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
        shLocalViewBtn.isHidden = false
        shRemoteViewBtn.isHidden = false
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
    
    /// 显示人偶UI
    func showRenouBroadcastUI() {
        avatarBackV.isHidden = false
        openMicB.isHidden = false
        userListB.isHidden = true
        userMicB.isHidden = false
        userSpeakerB.isHidden = false
        visitorTipBack.isHidden = true
        exitButton.isHidden = true
        switchBtn.isHidden = true
        cancelBtn.isHidden = true
        storeBtn.isHidden = true
        containerDressView.isHidden = true
    }
    
    func showRenouAudienceUI() {
        avatarBackV.isHidden = true
        openMicB.isHidden = true
        userListB.isHidden = true
        userMicB.isHidden = false
        userSpeakerB.isHidden = true
        visitorTipBack.isHidden = true
        exitButton.isHidden = true
        switchBtn.isHidden = true
        cancelBtn.isHidden = true
        storeBtn.isHidden = true
        containerDressView.isHidden = true
        chatTextField.isHidden = false
        chatSendBtn.isHidden = false
    }
    
    //    func setupMenu() {
    //        motionMenu.delegate = self
    //        motionMenu.items = ["1", "2"]
    //        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
    //            self.motionMenu.selectedIndex = 0
    //        }
    //    }
    
    func setupActionView() {
        mainActionView = ActionView(frame: CGRectMake(view.frame.width - 110, 120, 100, 330))
        mainActionView.delegate = self
        mainActionView.items = menuItems
        view.addSubview(mainActionView)
        
        subActionView = ActionView(frame: CGRectMake(view.frame.width - 110 - 110, 120, 100, 330))
        subActionView.delegate = self
        subActionView.items = subMenusItems
        subActionView.isHidden = true
        view.addSubview(subActionView)
    }
    
    func setupChatSendBtn() {
        chatSendBtn.layer.masksToBounds = true
        chatSendBtn.layer.cornerRadius = 10
        chatSendBtn.layer.borderColor = UIColor.white.cgColor
        chatSendBtn.layer.borderWidth = 1.0
    }
    
    func getMenuItems() {
        let path = Bundle.main.path(forResource: "menus", ofType: "json")
        let url = URL(fileURLWithPath: path!)
        do {
            let data = try Data(contentsOf: url)
            let jsonData: Any = try JSONSerialization.jsonObject(with: data, options: JSONSerialization.ReadingOptions.mutableContainers)
            let jsonDict = jsonData as! [String : Any]
            if sceneBroadcastMode == .none {
                broadcasterMenu = jsonDict["broadcasterMenu"] as! [Dictionary<String, Any>]
                for dic in broadcasterMenu {
                    let name = dic["menuName"] as! String
                    menuItems.append(name)
                }
            } else {
                audienceMenu = jsonDict["audienceMenu"] as! [Dictionary<String, Any>]
                for dic in audienceMenu {
                    let name = dic["menuName"] as! String
                    menuItems.append(name)
                }
            }
        } catch {
            print("actions parse error!")
        }
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
    
    // 设置远端视频view
    func setupRemoteVideoCV() {
        remoteVideoCV = VideoCollectionView.init(frame: CGRectMake((self.view.frame.size.width - 400) / 2 - 50, 10, 400, 130))
        remoteVideoCV.isHidden = true
        remoteVideoCV.alpha = 0.9
        view.addSubview(remoteVideoCV)
    }
    
    // 设置本地视频view
    func setupLocalVideoCV() {
        localVideoCV = VideoCollectionView.init(frame: CGRectMake((self.view.frame.size.width - 270) / 2 - 50, self.view.frame.size.height - 140, 270, 130))
        localVideoCV.isHidden = true
        localVideoCV.alpha = 0.9
        view.addSubview(localVideoCV)
    }
    
    // 显示或隐藏远端视图
    @IBAction func showOrHideRemoteView(_ sender: UIButton) {
        if sender.tag % 2 == 1 {
            remoteVideoCV.isHidden = false
            shRemoteViewBtn.setTitle("隐藏远端视角", for: .normal)
        } else {
            remoteVideoCV.isHidden = true
            shRemoteViewBtn.setTitle("显示远端视角", for: .normal)
        }
        sender.tag += 1
    }
    // 显示或隐藏本地视图
    @IBAction func showOrHideLocalView(_ sender: UIButton) {
        if sender.tag % 2 == 1 {
            localVideoCV.isHidden = false
            shLocalViewBtn.setTitle("隐藏本地视角", for: .normal)
            MetaChatEngine.sharedEngine.startPreview(previewView)
            addSceneView()
        } else {
            localVideoCV.isHidden = true
            shLocalViewBtn.setTitle("显示本地视角", for: .normal)
            MetaChatEngine.sharedEngine.stopPreview()
            removeSceneView()
        }
        sender.tag += 1
    }
    
    func setupPreviewView() {
        previewView = UIView.init(frame: CGRectMake(0, 0, 120, 120))
        previewView.layer.cornerRadius = 10
        previewView.layer.masksToBounds = true
        localVideoCV.items.append(previewView!)
        let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
        label.text = "uid:" + String(KeyCenter.RTC_UID)
        label.textColor = .red
        label.layer.zPosition = .greatestFiniteMagnitude
        previewView.addSubview(label)
    }
    
    func createAvatarView() {
        if MetaChatSceneViewController.avatarView == nil {
            MetaChatSceneViewController.avatarView = MetaChatEngine.sharedEngine.metachatScene?.createRenderView(.unity, region: CGRect(x: 0, y: 0, width: 120, height: 120))
            MetaChatSceneViewController.avatarView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            MetaChatSceneViewController.avatarView.layer.cornerRadius = 10
            MetaChatSceneViewController.avatarView.layer.masksToBounds = true
        }
        localVideoCV.items.append(MetaChatSceneViewController.avatarView!)
    }
    
    func addSceneView() {
        let config = AgoraMetachatSceneDisplayConfig()
        let view = MetaChatSceneViewController.avatarView!
        config.width = Int(view.frame.width * view.layer.contentsScale)
        config.height = Int(view.frame.height * view.layer.contentsScale)
        config.extraCustomInfo = Data()
        MetaChatEngine.sharedEngine.metachatScene?.add(MetaChatSceneViewController.avatarView, sceneDisplayConfig: config)
        MetaChatEngine.sharedEngine.metachatScene?.enableVideo(MetaChatSceneViewController.avatarView, enable: true)
        localVideoCV.collectionView.reloadData()
    }
    
    func removeSceneView() {
        MetaChatSceneViewController.avatarView.removeFromSuperview()
        MetaChatEngine.sharedEngine.metachatScene?.remove(MetaChatSceneViewController.avatarView)
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
        } else if kSceneIndex == 0 {
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
            } else if kSceneIndex == 1 {
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
        MetaChatEngine.sharedEngine.stopPreview()
        removeSceneView()
        MetaChatEngine.sharedEngine.leaveRtcChannel()
        MetaChatEngine.sharedEngine.leaveScene()
        //        if sceneBroadcastMode == .audience {
        //            self.dismiss(animated: true, completion: nil)
        //        }
    }
    
    @IBAction func switchScene(sender: UIButton) {
        saveUserDressInfo()
    }
    
    @IBAction func exitDressScene(_ sender: UIButton) {
        self.exit(sender: UIButton())
        if delegate != nil && ((delegate?.responds(to: Selector.init(("storeDressInfo")))) != nil) {
            delegate?.storeDressInfo(false)
        }
    }
    
    @IBAction func storeDressInfo(_ sender: UIButton) {
        saveUserDressInfo()
    }
    
    @IBAction func sendChatMessage(_ sender: UIButton) {
        let chatMsg = chatTextField.text
        let dict = ["userId": String(KeyCenter.RTC_UID),
                    "actionId": "chat",
                    "param": chatMsg]
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8)
        let dic = [
            "key": "userAction",
            "value": str as Any
        ] as [String : Any]
        
        //        if let data = try? JSONSerialization.data(withJSONObject:dic, options: .fragmentsAllowed) {
        //            MetaChatEngine.sharedEngine.metachatScene?.sendMessage(toUser: MetaChatEngine.sharedEngine.braodcaster, message: data)
        //        }
        
        chatTextField.text = ""
    }
    
    func switchBgm(_ sender: UIButton) {
        
    }
    
    func broadcasterAction(_ index: Int, actionView: ActionView) {
        
        var curIndex = index
        var dic = Dictionary<String, Any>()
        var subMenus = [Dictionary<String, Any>]()
        var menuData = Dictionary<String, Any>()
        var value = Dictionary<String, Any>()
        var key = ""
        
        if actionView == mainActionView {
            if curIndex < 2 {
                curIndex == 0 ? exit(sender: UIButton()) : switchBgm(UIButton())
                return
            }
            curIndex -= 2
            dic = broadcasterMenu[curIndex]
            subMenus = dic["subMenus"] as? [Dictionary<String, Any>] ?? [Dictionary<String, Any>]()
            subBroadcasterMenu = subMenus
        } else {
            dic = subBroadcasterMenu[curIndex]
            subMenus = dic["subMenus"] as? [Dictionary<String, Any>] ?? [Dictionary<String, Any>]()
        }
        
        if subMenus.count == 0 {
            
            menuData = dic["menuData"] as! [String : Any]
            if menuData.isEmpty == true {
                return
            }
            key = menuData["key"] as! String
            value = menuData["value"] as! [String: Any]
            
            let data = try? JSONSerialization.data(withJSONObject: value, options: [])
            let str = String(data: data!, encoding: String.Encoding.utf8)
            let sendDic = [
                "key": key,
                "value": str as Any
            ] as [String : Any]
            
            MetaChatEngine.sharedEngine.sendMessage(dic: sendDic)
            
            return
        }
        
        subMenusItems.removeAll()
        
        for tempDic in subMenus {
            let name = tempDic["menuName"] as! String
            subMenusItems.append(name)
        }
        subActionView.items = subMenusItems
        subActionView.isHidden = false
    }
    
    func audienceAction(_ index: Int, actionView: ActionView) {
        
        var curIndex = index
        var dic = Dictionary<String, Any>()
        var subMenus = [Dictionary<String, Any>]()
        var menuData = Dictionary<String, Any>()
        var value = Dictionary<String, Any>()
        var key = ""
        
        if actionView == mainActionView {
            if curIndex < 2 {
                curIndex == 0 ? exit(sender: UIButton()) : switchBgm(UIButton())
                return
            }
            curIndex -= 2
            dic = audienceMenu[curIndex]
            subMenus = dic["subMenus"] as? [Dictionary<String, Any>] ?? [Dictionary<String, Any>]()
            subAudienceMenu = subMenus
        } else {
            dic = subAudienceMenu[curIndex]
            subMenus = dic["subMenus"] as? [Dictionary<String, Any>] ?? [Dictionary<String, Any>]()
        }

        if subMenus.count == 0 {
            
            menuData = dic["menuData"] as! [String : Any]
            if menuData.isEmpty == true {
                return
            }
            key = menuData["key"] as! String
            value = menuData["value"] as! [String: Any]
            
            let dict = ["userId": String(KeyCenter.RTC_UID),
                        "actionId": value["actionId"]!,
                        "param": String(arc4random() % 100000)] as [String : Any]
            let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
            let str = String(data: data!, encoding: String.Encoding.utf8)
            let sendDic = [
                "key": key,
                "value": str as Any
            ] as [String : Any]

//            if let data = try? JSONSerialization.data(withJSONObject:sendDic, options: .fragmentsAllowed) {
//                MetaChatEngine.sharedEngine.metachatScene?.sendMessage(toUser: MetaChatEngine.sharedEngine.braodcaster, message: data)
//            }
            
            return
        }
        
        subMenusItems.removeAll()
        
        for tempDic in subMenus {
            let name = tempDic["menuName"] as! String
            subMenusItems.append(name)
        }
        subActionView.items = subMenusItems
        subActionView.isHidden = false
    }
}

extension MetaChatSceneViewController: AgoraMetachatSceneEventDelegate {
    func metachatScene(_ scene: AgoraMetachatScene, onEnumerateVideoDisplaysResult displayIds: NSMutableArray) {
        
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onReleasedScene errorCode: Int) {
        DispatchQueue.main.async {
            AgoraMetachatKit.destroy()
            MetaChatEngine.sharedEngine.metachatKit = nil
            self.switchOrientation(isPortrait: true, isFullScreen: true)
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onEnterSceneResult errorCode: Int) {
        DLog("进入场景===============")
        
        let dict = ["debugUnity": true]
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8)
        MetaChatEngine.sharedEngine.metachatScene?.setSceneParameters(str!)
        
        DispatchQueue.main.async {
            self.showUI()
            if kSceneIndex == 1 {
                self.dressSetting()
                self.showDressUI()
            } else if kSceneIndex == 2 {
                if self.sceneBroadcastMode == .none {
                    self.showRenouBroadcastUI()
                } else {
                    self.showRenouAudienceUI()
                }
            }
//            self.joinChannel()
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
    
    func metachatScene(_ scene: AgoraMetachatScene, onSceneVideoFrame videoFrame: AgoraOutputVideoFrame?, view: UIView) {
//        if sceneBroadcastMode == .none {
//            let vf = AgoraVideoFrame()
//            vf.format = 12
//            vf.textureBuf = videoFrame?.pixelBuffer
//            MetaChatSceneViewController.frameIndex += 1
//            vf.time = CMTimeMake(value: Int64(MetaChatSceneViewController.frameIndex), timescale: 30)
//            vf.rotation = videoFrame?.rotation ?? 0
//            MetaChatEngine.sharedEngine.rtcEngine?.pushExternalVideoFrame(vf)
//        }
        
        if view == MetaChatSceneViewController.avatarView {
            let vf = AgoraVideoFrame()
            vf.format = 12
            vf.textureBuf = videoFrame?.pixelBuffer
            MetaChatSceneViewController.frameIndex += 1
            vf.time = CMTimeMake(value: Int64(MetaChatSceneViewController.frameIndex), timescale: 30)
            vf.rotation = videoFrame?.rotation ?? 0
            MetaChatEngine.sharedEngine.rtcEngine?.pushExternalVideoFrame(vf)
        }
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onRecvBroadcastMessage message: Data) {
        
    }
    
    func metachatScene(_ scene: AgoraMetachatScene, onRecvMessageFromUser userId: String, message: Data) {
        do {
            let jsonData: Any = try JSONSerialization.jsonObject(with: message, options: JSONSerialization.ReadingOptions.mutableContainers)
            let jsonDict = jsonData as! [String : Any]
            
            MetaChatEngine.sharedEngine.sendMessage(dic: jsonDict)
            
        } catch {
            
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
        cell.dressImageView = UIImageView.init(frame: .init(x: 10, y: 10, width: 70, height: 70))
        cell.addSubview(cell.dressImageView)
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
}

extension MetaChatSceneViewController: ActionViewDelegate {
    func actionView(_ actionView: ActionView, didSelectItem item: String, atIndex index: Int) {
        if sceneBroadcastMode == .none {
            broadcasterAction(index, actionView: actionView)
        } else {
            audienceAction(index, actionView: actionView)
        }
    }
}

extension MetaChatSceneViewController: UIGestureRecognizerDelegate {
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
//        if sceneBroadcastMode == .none {
//            if touch.view == view.subviews[0] {
//                return true
//            }
//        } else {
//            if touch.view?.isKind(of: MTKView.self) == true {
//                return true
//            }
//        }
        if self.isEditing {
            return true
        }
        return false
    }
}

extension MetaChatSceneViewController: RTCEngineInternalDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int) {
        if !remoteUsers.keys.contains(String(uid)) {
            let view = UIView()
            view.layer.cornerRadius = 10
            view.layer.masksToBounds = true
            view.frame = CGRectMake(0, 0, 120, 120)
            let rvc = AgoraRtcVideoCanvas()
            rvc.uid = uid
            rvc.view = view
            rvc.renderMode = .fit
            rvc.mirrorMode = .enabled
            engine.setupRemoteVideo(rvc)
            remoteUsers[String(uid)] = view
            remoteVideoCV.items.append(view)
            
            let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
            label.text = "uid:" + String(uid)
            label.textColor = .red
            view.addSubview(label)
            label.layer.zPosition = .greatestFiniteMagnitude
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        if remoteUsers.keys.contains(String(uid)) {
            let view = remoteUsers[String(uid)]
            let rvc = AgoraRtcVideoCanvas()
            rvc.uid = uid
            rvc.view = nil
            rvc.renderMode = .fit
            rvc.mirrorMode = .enabled
            engine.setupRemoteVideo(rvc)
            remoteUsers.removeValue(forKey: String(uid))
            remoteVideoCV.items.removeAll(where: {$0 as! UIView == view} )
        }
    }
}
