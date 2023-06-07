//
//  MetaChatSceneViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/26.
//

import UIKit
import AgoraRtcKit
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

class MockRenderView: UIView {
    
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
    @IBOutlet weak var slider: UISlider!
    var mainActionView: ActionView!
    var subActionView: ActionView!
    
    var delegate: handleDressInfoDelegate?
    var currentGender: Int = 1
    
//    /// 渲染的render view，多次进出场景需要保持同一renderView对象
//    static var renderView: UIView!
//    static var avatarView: UIView!
    
    var sceneView: UIView?
    var avatarView: UIView?
    
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
    
//    var sceneBroadcastMode = AgoraMetachatSceneBroadcastMode.none   // 0: broadcast  1: audience
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
    
    private var dressInfoModel: DressInfoModel = DressInfoModel()
    private var dressView: ZRCustomView?
    
    @IBOutlet weak var dressBtn: UIButton!
    @IBOutlet weak var faceBtn: UIButton!
    var faceIndex = 0
    
    /// 设置主界面UI
    func setUI() {
        openMicB.setImage(UIImage.init(named: "onbtn"), for: .normal)
        openMicB.setImage(UIImage.init(named: "offbtn"), for: .selected)
        userMicB.setImage(UIImage.init(named: "microphone-on"), for: .normal)
        userMicB.setImage(UIImage.init(named: "microphone-off"), for: .selected)
        
        userSpeakerB.setImage(UIImage.init(named: "voice-on"), for: .normal)
        userSpeakerB.setImage(UIImage.init(named: "voice-off"), for: .selected)
        
        nameL.text = MetaServiceEngine.sharedEngine.playerName
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
    
    private func createRenderView() {

    }
    
    deinit {
        DLog("===========MetaChatSceneViewController销毁了=======")
        
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        MetaServiceEngine.sharedEngine.delegate = self
        
        setUI()
        addObserver()
        createRenderView()
        getDressInfo()
        
        getMenuItems()
        //        setupMenu()
        
        //        setupActionView()
        //        setupChatSendBtn()
        
        setupRemoteVideoCV()
        setupLocalVideoCV()
        setupPreviewView()
        
        getDressInfo()
        setupDressView()
        
        guard let tempView = sceneView else { return }
        view.insertSubview(tempView, at: 0)
        
        let hideGesture = UITapGestureRecognizer.init(target: self, action: #selector(hideKeyboard))
        hideGesture.delegate = self
        view.addGestureRecognizer(hideGesture)
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(enterForeground(_:)), name:UIApplication.willEnterForegroundNotification, object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        guard let view = self.sceneView else { return }
        MetaServiceEngine.sharedEngine.enterScene(view: view/*view: MetaChatSceneViewController.renderView, sceneBroadcastMode: sceneBroadcastMode*/)
    }
    
    override func viewDidDisappear(_ animated: Bool) {

    }
    
    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        if #available(iOS 15.0, *) {
            return [.portrait, .landscapeRight]
        } else {
            return .portrait
        }
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
        if kSceneIndex == .chat {
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
        shLocalViewBtn.isHidden = false
        shRemoteViewBtn.isHidden = false
        dressBtn.isHidden = true
        faceBtn.isHidden = true
        dressView?.isHidden = true
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
        shLocalViewBtn.isHidden = true
        shRemoteViewBtn.isHidden = true
        dressBtn.isHidden = false
        faceBtn.isHidden = false
        dressView?.isHidden = false
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
//        let path = Bundle.main.path(forResource: "menus", ofType: "json")
//        let url = URL(fileURLWithPath: path!)
//        do {
//            let data = try Data(contentsOf: url)
//            let jsonData: Any = try JSONSerialization.jsonObject(with: data, options: JSONSerialization.ReadingOptions.mutableContainers)
//            let jsonDict = jsonData as! [String : Any]
//            if sceneBroadcastMode == .none {
//                broadcasterMenu = jsonDict["broadcasterMenu"] as! [Dictionary<String, Any>]
//                for dic in broadcasterMenu {
//                    let name = dic["menuName"] as! String
//                    menuItems.append(name)
//                }
//            } else {
//                audienceMenu = jsonDict["audienceMenu"] as! [Dictionary<String, Any>]
//                for dic in audienceMenu {
//                    let name = dic["menuName"] as! String
//                    menuItems.append(name)
//                }
//            }
//        } catch {
//            print("actions parse error!")
//        }
    }
    
    func setupDressView() {
        dressView = ZRCustomView(frame: CGRectMake(0, self.view.frame.height - 300, self.view.frame.width, 300))
        dressView?.isHidden = true
        self.view.addSubview(dressView!)
        dressView?.scrollViewLabelClickBlock = { value in
            if self.dressView?.selectIndex == 0 {
                guard let rs = self.dressInfoModel.dressResources else { return }
                guard let girlrs = rs[1].resources else { return }
                self.dressView?.collectionViewItems.removeAll()
                guard let assets = girlrs[Int(value)].assets else { return }
                for asset in assets {
                    let bundlePath = Bundle.main.path(forResource: "girl", ofType: "bundle")!
                    guard let customBundle = Bundle(path: bundlePath) else { continue }
                    let path = customBundle.path(forResource: String(asset), ofType: "jpg")
                    self.dressView?.collectionViewItems.append(path!)
                }
            } else {
                guard let rs = self.dressInfoModel.faceParameters else { return }
                guard let girlrs = rs[1].blendshape else { return }
                self.dressView?.collectionViewItems.removeAll()
                guard let assets = girlrs[Int(value)].shapes else { return }
                for asset in assets {
                    self.dressView?.collectionViewItems.append(asset.ch!)
                }
            }
        }
        dressView?.collectionViewCellClickBlock = { value in
            if self.dressView?.selectIndex == 0 {
                guard let rs = self.dressInfoModel.dressResources else { return }
                guard let girlrs = rs[1].resources else { return }
                guard let assets = girlrs[self.dressView!.getDressIndex()].assets else { return }
                
                //        let dic1 = ["id": [10002]]
                //        let data1 = try? JSONSerialization.data(withJSONObject: dic1, options: [])
                //        let str1 = String(data: data1!, encoding: String.Encoding.utf8)
                
                let dic1 = ["id": [assets[Int(value)]]]
                let data1 = try? JSONSerialization.data(withJSONObject: dic1, options: [])
                let str1 = String(data: data1!, encoding: String.Encoding.utf8)
                let sendDic = [
                    "key": "updateDress",
                    "value": str1 as Any
                ] as [String : Any]
//                let data2 = try? JSONSerialization.data(withJSONObject: sendDic, options: [])
//                let str2 = String(data: data2!, encoding: String.Encoding.utf8)
                MetaServiceEngine.sharedEngine.sendMessage(dic: sendDic)
            } else {
                self.faceIndex = value
            }
        }
        dressView?.sliderValueChangedBlock = { value in
            guard let rs = self.dressInfoModel.faceParameters else { return }
            guard let girlrs = rs[1].blendshape else { return }
            guard let assets = girlrs[self.dressView!.getFaceIndex()].shapes else { return }
            
            //        let dic = ["value": [["key": "EB_updown_1", "value": sender.value * 100]]]
            //        let data = try? JSONSerialization.data(withJSONObject: dic, options: [])
            //        let str = String(data: data!, encoding: String.Encoding.utf8)
            let dic = ["value": [["key": String(assets[self.faceIndex].key!), "value": value * 100]]]
            let data = try? JSONSerialization.data(withJSONObject: dic, options: [])
            let str = String(data: data!, encoding: String.Encoding.utf8)
            let sendDic = [
                "key": "updateFace",
                "value": str as Any
            ] as [String : Any]
//            let data2 = try? JSONSerialization.data(withJSONObject: sendDic, options: [])
//            let str2 = String(data: data2!, encoding: String.Encoding.utf8)
            MetaServiceEngine.sharedEngine.sendMessage(dic: sendDic)
        }
    }

    func getDressInfo() {
        guard let path = Bundle.main.path(forResource: "AaManifest", ofType: "txt") else { return }
        let localData = NSData.init(contentsOfFile: path)! as Data
        do {
            let dressInfo = try JSONDecoder().decode(DressInfoModel.self, from: localData)
            if let dressResources = dressInfo.dressResources {
                self.dressInfoModel.dressResources = dressResources
            }
            if let faceParameters = dressInfo.faceParameters {
                self.dressInfoModel.faceParameters = faceParameters
            }
        } catch {
            debugPrint("===== dressInfo ERROR =====")
        }
    }
    
    @IBAction func changeDress(_ sender: UIButton) {
        guard let rs = self.dressInfoModel.dressResources else { return }
        guard let girlrs = rs[1].resources else { return }
        self.dressView?.scrollViewItems.removeAll()
        self.dressView?.selectIndex = 0
        for resource in girlrs {
            self.dressView?.scrollViewItems.append(resource.name!)
        }
        self.dressView?.setupScrollViewDataSource()
    }
    
    @IBAction func changeBeauty(_ sender: UIButton) {
        guard let rs = self.dressInfoModel.faceParameters else { return }
        guard let girlrs = rs[1].blendshape else { return }
        self.dressView?.scrollViewItems.removeAll()
        self.dressView?.selectIndex = 1
        for resource in girlrs {
            self.dressView?.scrollViewItems.append(resource.type!)
        }
        self.dressView?.setupScrollViewDataSource()
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
            MetaServiceEngine.sharedEngine.startPreview(previewView)
            addSceneView()
        } else {
            localVideoCV.isHidden = true
            shLocalViewBtn.setTitle("显示本地视角", for: .normal)
            MetaServiceEngine.sharedEngine.stopPreview()
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
    
    func addSceneView() {
        let scene = MetaServiceEngine.sharedEngine.metaScene
        if avatarView == nil {
            guard let view = scene?.createRenderView(CGRect(x: 100, y: 100, width: 120, height: 120)) else { return }
            avatarView = view
        }
        avatarView?.layer.masksToBounds = true
        avatarView?.layer.cornerRadius = 10
        let config = AgoraMetaSceneDisplayConfig()
        config.width = 200
        config.height = 200
        scene?.add(avatarView!, sceneDisplayConfig: config)
        scene?.enableVideoCapture(avatarView!, enable: true)
        
        if localVideoCV.items.contains(where: { item in
            return item as? UIView == avatarView
        }) == false {
            self.localVideoCV.items.append(avatarView!)
        }
        
        localVideoCV.collectionView.reloadData()
    }
    
    func removeSceneView() {
        guard let view = avatarView else { return }
        let scene = MetaServiceEngine.sharedEngine.metaScene
        scene?.remove(view)
        scene?.enableVideoCapture(view, enable: false)

        if let index = localVideoCV.items.firstIndex(where: { $0 as? UIView == view }) {
            localVideoCV.items.remove(at: index)
        }
        localVideoCV.collectionView.reloadData()
    }
    
    /// 播放新的歌曲
    private func playNewSong(mvUrl:String) {
        DLog("mvurl =======>",mvUrl)
        MetaServiceEngine.sharedEngine.changeTVUrl(mvUrl)
        MetaServiceEngine.sharedEngine.isSinging = true
    }
    
    /// 获取电视位置
    private func requestTVPosition(){
//        let dic = [
//            "type":1,
//            "params":["id":1]
//        ] as [String : Any]
//        MetaServiceEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    /// 获取NPC桌子位置
    private func requestNPCTablePosition(){
//        let dic = [
//            "type":1,
//            "params":["id":2]
//        ] as [String : Any]
//        MetaServiceEngine.sharedEngine.sendMessage(dic: dic)
    }
    
    /// 加入房间
    private func joinChannel(){
        MetaServiceEngine.sharedEngine.joinRtcChannel { [weak self] in
            guard let wSelf = self else {return}
            wSelf.openMicAction(sender: wSelf.openMicB)
        }
    }
    
    /// 打开npc播放器
    private func openNPC(){
        MetaServiceEngine.sharedEngine.createAndOpenNPCPlayer {[weak self] player in
            self?.requestNPCTablePosition()
        }
    }
    
    /// 打开电视
    private func setTVon(){
        MetaServiceEngine.sharedEngine.createAndOpenTVPlayer { [weak self] player in
            self?.requestTVPosition()
        } playBackAllLoopsCompleted: {
            KTVDataManager.shared().makeNextAsPlaying()
        }
    }
    
    /// 电视恢复原始
    private func resetTV() {
        MetaServiceEngine.sharedEngine.resetTV()
    }
    
    /// 结束k歌
    private func finishKTV(){
        songListButton.isHidden = true
        MetaServiceEngine.sharedEngine.isSinging = false
        MetaServiceEngine.sharedEngine.broadcastKTVFinishMessage()
        ktvVC?.dismiss(animated: true)
        KTVDataManager.shared().clear()
    }
    
    /// 显示k歌页面
    private func showKTV(){
        songListButton.isHidden = false
        MetaServiceEngine.sharedEngine.isSinging = true
        
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
        MetaServiceEngine.sharedEngine.setTVoff()
        MetaServiceEngine.sharedEngine.leaveRtcChannel()
        MetaServiceEngine.sharedEngine.leaveScene()
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
    
    // MARK: -  UI actions
    
    @IBAction func openMicAction(sender: UIButton) {
        if sender.isSelected {
            MetaServiceEngine.sharedEngine.closeMic()
            MetaServiceEngine.sharedEngine.updateIsVisitor(isVisitor: true)
            sender.isSelected = false
            userMicB.isHidden = true
            modeL.text = "游客模式"
            visitorIcon.isHidden = false
        }else {
            MetaServiceEngine.sharedEngine.openMic()
            MetaServiceEngine.sharedEngine.updateIsVisitor(isVisitor: false)
            sender.isSelected = true
            if (kSceneIndex == .chat) {
                userMicB.isHidden = false
            } else if kSceneIndex == .live {
                userMicB.isHidden = true
            }
            modeL.text = "语聊模式"
            visitorIcon.isHidden = true
        }
    }
    
    @IBAction func muteMicAction(sender: UIButton) {
        MetaServiceEngine.sharedEngine.muteMic(isMute: !sender.isSelected)
        sender.isSelected = !sender.isSelected
    }
    
    @IBAction func didClickSongListButton(_ sender: UIButton) {
        showKTV()
    }
    
    
    @IBAction func muteSpeakerAction(sender: UIButton) {
        MetaServiceEngine.sharedEngine.muteSpeaker(isMute: !sender.isSelected)
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
        MetaServiceEngine.sharedEngine.stopPreview()
        removeSceneView()
        MetaServiceEngine.sharedEngine.leaveRtcChannel()
        MetaServiceEngine.sharedEngine.leaveScene()
        //        if sceneBroadcastMode == .audience {
        //            self.dismiss(animated: true, completion: nil)
        //        }
    }
    
    @IBAction func switchScene(sender: UIButton) {

    }
    
    @IBAction func exitDressScene(_ sender: UIButton) {
        self.exit(sender: UIButton())
    }
    
    @IBAction func storeDressInfo(_ sender: UIButton) {

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
        //            MetaServiceEngine.sharedEngine.metaScene?.sendMessage(toUser: MetaServiceEngine.sharedEngine.braodcaster, message: data)
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
            
            MetaServiceEngine.sharedEngine.sendMessage(dic: sendDic)
            
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
//                MetaServiceEngine.sharedEngine.metaScene?.sendMessage(toUser: MetaServiceEngine.sharedEngine.braodcaster, message: data)
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

extension MetaChatSceneViewController: AgoraMetaSceneEventDelegate {
    func metaScene(_ scene: AgoraMetaScene, onAddSceneViewResult view: UIView, errorCode: Int) {
        print("====== addSceneViewResult ====== errorCode: ", errorCode)
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRemoveSceneViewResult view: UIView, errorCode: Int) {
        print("====== onRemoveSceneViewResult ====== errorCode: ", errorCode)
    }
    
    func metaScene(_ scene: AgoraMetaScene, onEnumerateVideoDisplaysResult displayIds: NSMutableArray) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onReleasedScene errorCode: Int) {
        DispatchQueue.main.async {
            AgoraMetaServiceKit.destroy()
            MetaServiceEngine.sharedEngine.metaService = nil
            AgoraRtcEngineKit.destroy()
            MetaServiceEngine.sharedEngine.rtcEngine = nil
            self.switchOrientation(isPortrait: true, isFullScreen: true)
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onEnterSceneResult errorCode: Int) {
        DLog("进入场景===============")
        
        let dict = ["debugUnity": true]
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8)
        MetaServiceEngine.sharedEngine.metaScene?.setSceneParameters(str!)
        
        DispatchQueue.main.async {
            if kSceneIndex == .chat {
                self.showUI()
            } else if kSceneIndex == .live {
                self.showDressUI()
            }
//            self.joinChannel()
            self.setTVon()
//            self.openNPC()
            self.tryShowGuideVC()
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onLeaveSceneResult errorCode: Int) {
        DispatchQueue.main.async {
            MetaServiceEngine.sharedEngine.resetMetachat()
            DLog("离开场景========errorCode: \(errorCode)")
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onSceneMessageReceived message: Data) {
        
        guard let json: [String: Any] = try? JSONSerialization.jsonObject(with: message) as? [String: Any] else {
            DLog("json = nil")
            return
        }
        guard let msgStr = json["message"] as? String else { return }
        CustomMessageHandler.shared.handleMessage(msgStr) {[weak self] ret in
            switch ret {
            case .objectPosition(let id, let position, let forward):
                MetaServiceEngine.sharedEngine.updateSpatialForMediaPlayer(id: id, postion: position, forward: forward)
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
    
    func metaScene(_ scene: AgoraMetaScene, onUserPositionChanged uid: String, posInfo: AgoraMetaPositionInfo) {
        
        if (uid.compare(KeyCenter.RTM_UID) == .orderedSame) || (uid.compare("") == .orderedSame) {
            MetaServiceEngine.sharedEngine.localSpatial?.updateSelfPosition(posInfo.position as! [NSNumber], axisForward: posInfo.forward as! [NSNumber], axisRight: posInfo.right as! [NSNumber], axisUp: posInfo.up as! [NSNumber])
//            DLog("position = \(posInfo.position),forword = \(posInfo.forward),right = \(posInfo.right),up = \(posInfo.up)")
        }else {
            let remotePositionInfo = AgoraRemoteVoicePositionInfo()
            remotePositionInfo.position = posInfo.position as! [NSNumber]
            remotePositionInfo.forward = posInfo.forward as? [NSNumber]
            
            MetaServiceEngine.sharedEngine.localSpatial?.updateRemotePosition(UInt(uid) ?? 0, positionInfo: remotePositionInfo)
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onSceneVideoFrameCaptured videoFrame: AgoraOutputVideoFrame?, view: UIView) {
        if view == avatarView {
            let vf = AgoraVideoFrame()
            vf.format = 12
            vf.textureBuf = videoFrame?.pixelBuffer
            MetaChatSceneViewController.frameIndex += 1
            vf.time = CMTimeMake(value: Int64(MetaChatSceneViewController.frameIndex), timescale: 30)
            vf.rotation = videoFrame?.rotation ?? 0
            MetaServiceEngine.sharedEngine.rtcEngine?.pushExternalVideoFrame(vf)
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRecvBroadcastMessage message: Data) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRecvMessageFromUser userId: String, message: Data) {
        do {
            let jsonData: Any = try JSONSerialization.jsonObject(with: message, options: JSONSerialization.ReadingOptions.mutableContainers)
            let jsonDict = jsonData as! [String : Any]
            
            MetaServiceEngine.sharedEngine.sendMessage(dic: jsonDict)
            
        } catch {
            
        }
    }
}

extension MetaChatSceneViewController: ActionViewDelegate {
    func actionView(_ actionView: ActionView, didSelectItem item: String, atIndex index: Int) {
//        if sceneBroadcastMode == .none {
//            broadcasterAction(index, actionView: actionView)
//        } else {
//            audienceAction(index, actionView: actionView)
//        }
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
