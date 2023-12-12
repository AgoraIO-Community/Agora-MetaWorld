//
//  MetaSceneViewController.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//

import Foundation
import AgoraRtcKit
import MetalKit

let avatarCellSize = (UIScreen.main.bounds.size.width - 60 ) / 2

class MetaSceneViewController: UIViewController {
    
    /// main view
    var sceneView: UIView?

    /// NPC media player
    var tvPlayerMgr: MetaPlayerManager?
    
    /// view show ui
    private var metaSceneView: MetaSceneView!
    
    private lazy var audioFileReader = AVAudioFileReader()
    private var audioTimer: DispatchSourceTimer?
    private var audioSampleRate: UInt32 = 0
    private var audioChannels: UInt32 = 0
    
    private var remoteUsers: Dictionary<String, UIView> = [:]
    private var viewMode: Int = 0
    
    private var dressInfoModel: DressInfoModel = DressInfoModel()
    private var faceIndex = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(enterForeground(_:)), name:UIApplication.willEnterForegroundNotification, object: nil)
        
        guard let nib = Bundle.main.loadNibNamed("SceneView", owner: nil) else { return }
        metaSceneView = nib.first as? MetaSceneView
        metaSceneView.setupUI()
        metaSceneView.frame = self.view.frame
        metaSceneView.delegate = self
        metaSceneView.dressView?.dressViewEventDelegate = self
        self.view.addSubview(metaSceneView)
        
        setExternalAudioSource()
        MetaServiceEngine.sharedEngine.delegate = self
    }
    
    deinit {
        DLog("===========MetaSceneViewController销毁了=======")
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        guard let tempView = sceneView else { return }
        if kSceneIndex != .chatRoom && kSceneIndex != .faceCaptureChatRoom {
            metaSceneView.insertSubview(tempView, at: 0)
            if kSceneIndex == .chat {
                metaSceneView.setupRemoteVideoCV()
                metaSceneView.setupLocalVideoCV()
                metaSceneView.setupPreviewView()
            } else if kSceneIndex == .live {
                getDressInfo()
            }
        } else {
            metaSceneView.setupAvatarCV()
            metaSceneView.avatarCV.items.append(tempView)
            metaSceneView.setupAvatarView()
        }
    }
    
    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        if kSceneIndex == .chat {
            return .landscapeRight
        } else {
            return .portrait
        }
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    @objc func hideKeyboard() {
        view.endEditing(true)
    }
    
    @objc func keyboardWillShow(_ notification: NSNotification) {
        let keyboardFrame = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as! NSValue).cgRectValue
        let frame = metaSceneView.chatTextField.frame
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
    
    func setExternalAudioSource() {
        guard let path = Bundle.main.path(forResource: "test1", ofType: "wav") else { return }
        let opened = audioFileReader.audioFileOpen(path)
        if opened {
            audioSampleRate = audioFileReader.audioFileSampleRate()
            audioChannels = audioFileReader.audioFileChannels()
        }
    }
    
    func showUI() {
        if kSceneIndex == .chat {
            self.metaSceneView.showChatUI()
            self.setTVon()
        } else if kSceneIndex == .live {
            self.metaSceneView.showLiveUI()
        } else if kSceneIndex == .chatRoom || kSceneIndex == .faceCaptureChatRoom {
            self.metaSceneView.showChatRoomUI()
        }
    }
    
    /// 打开电视
    private func setTVon(){
        MetaServiceEngine.sharedEngine.createAndOpenTVPlayer { player in
            
        } playBackAllLoopsCompleted: {}
    }
    
    /// 加入房间
    private func joinChannel(){
        MetaServiceEngine.sharedEngine.joinChannel {}
    }
    
    /// 获取换装信息
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
            DLog("===== dressInfo ERROR =====")
        }
    }
    
    @IBAction func pushAudio(_ sender: UIButton) {
        guard let scene = MetaServiceEngine.sharedEngine.metaScene else { return }
        let pushQueue = DispatchQueue(label: "External push audio queue")
        let timer = DispatchSource.makeTimerSource(queue: pushQueue)
        timer.schedule(wallDeadline: .now(), repeating: 0.04)
        
        timer.setEventHandler { [weak self] in
            guard let self = self else {
                return
            }
            let data: Data? = self.audioFileReader.audioFileRead()
            if let nsData = data as NSData? {
                let pointer = UnsafeMutableRawPointer(mutating: nsData.bytes)
                let samples = self.audioFileReader.audioFileSampleRate() * self.audioFileReader.audioFileChannels() * 40 / 1000;
                scene.pushAudio(toLipSync: pointer, samples: UInt(samples), sampleRate: Int(audioSampleRate), channels: Int(audioChannels))
            }
        }
        
        timer.resume()
        audioTimer = timer
    }
    
    @IBAction func stopPushAudio(_ sender: UIButton) {
        audioFileReader.audioFileClose()
        guard let timer = audioTimer else { return }
        timer.cancel()
        
        audioTimer = nil
    }
    
    // MARK: -  meta scene UI Event
    func addSceneView() {
        let scene = MetaServiceEngine.sharedEngine.metaScene
        guard let view = scene?.createRenderView(CGRect(x: 0, y: 0, width: 120, height: 120)) else { return }
        view.layer.masksToBounds = true
        view.layer.cornerRadius = 10
        let config = AgoraMetaSceneDisplayConfig()
        config.width = Int(view.bounds.width * view.contentScaleFactor)
        config.height = Int(view.bounds.height * view.contentScaleFactor)
        
        let avatar = getRandomValueFromDictionary(dictionary: roleDic)
        let dict = ["avatarName": avatar]
        let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let extraInfo = String(data: data!, encoding: String.Encoding.utf8)
        config.extraInfo = extraInfo!.data(using: String.Encoding.utf8)
        scene?.add(view, sceneDisplayConfig: config)
        
        metaSceneView.localVideoCV.items.append(view)
        metaSceneView.localVideoCV.collectionView.reloadData()
    }
    
    private func getRandomValueFromDictionary<Key, Value>(dictionary: [Key: Value]) -> Value? {
        guard let randomKey = dictionary.keys.randomElement() else {
            return nil
        }
        return dictionary[randomKey]
    }
    
    func removeSceneView() {
        let scene = MetaServiceEngine.sharedEngine.metaScene
        let index = metaSceneView.localVideoCV.items.count - 1
        if index < 0 { return }
        let view = metaSceneView.localVideoCV.items[index] as! UIView
        scene?.remove(view)
        scene?.enableVideoCapture(view, enable: false)

        if let index = metaSceneView.localVideoCV.items.firstIndex(where: { $0 as? UIView == view }) {
            metaSceneView.localVideoCV.items.remove(at: index)
            view.removeFromSuperview()
        }
        metaSceneView.localVideoCV.collectionView.reloadData()
    }

    /// 离开场景
    private func leaveScene(){
        MetaServiceEngine.sharedEngine.setTVoff()
        MetaServiceEngine.sharedEngine.leaveChannel()
        MetaServiceEngine.sharedEngine.leaveMetaScene()
    }
    
    private func changeDress() {
        guard let rs = self.dressInfoModel.dressResources else { return }
        guard let girlrs = rs[1].resources else { return }
        metaSceneView.dressView?.scrollViewItems.removeAll()
        metaSceneView.dressView?.selectIndex = 0
        for resource in girlrs {
            metaSceneView.dressView?.scrollViewItems.append(resource.name!)
        }
        metaSceneView.dressView?.setupScrollViewDataSource()
    }
    
    private func pitchFace() {
        guard let rs = self.dressInfoModel.faceParameters else { return }
        guard let girlrs = rs[1].blendshape else { return }
        metaSceneView.dressView?.scrollViewItems.removeAll()
        metaSceneView.dressView?.selectIndex = 1
        for resource in girlrs {
            metaSceneView.dressView?.scrollViewItems.append(resource.type!)
        }
        metaSceneView.dressView?.setupScrollViewDataSource()
    }
    
    private func changeViewMode() {
        viewMode += 1
        let dict = ["viewMode": viewMode % 3]
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8)
        let dic = [
            "key": "setCamera",
            "value": str as Any
        ] as [String : Any]
        MetaServiceEngine.sharedEngine.sendMessage(dic: dic)
    }
    
}

extension MetaSceneViewController: AgoraMetaSceneEventDelegate {
    func metaScene(_ scene: AgoraMetaScene, onReleasedScene errorCode: Int) {
        DLog("===============释放场景完成===============")
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
        DLog("===============进入场景完成===============")
        DispatchQueue.main.async {
            let dict = ["debugUnity": true]
            let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
            let str = String(data: value!, encoding: String.Encoding.utf8)
            MetaServiceEngine.sharedEngine.metaScene?.setSceneParameters(str!)
            
            MetaServiceEngine.sharedEngine.metaScene?.enableVideoCapture(self.sceneView!, enable: true)
        
            self.showUI()
            self.joinChannel()
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onLeaveSceneResult errorCode: Int) {
        DLog("===============离开场景完成===============")
        DispatchQueue.main.async {
            MetaServiceEngine.sharedEngine.destroyMetaService()
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onSceneMessageReceived message: Data) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onUserPositionChanged uid: String, posInfo: AgoraMetaPositionInfo) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onSceneVideoFrameCaptured videoFrame: AgoraOutputVideoFrame?, view: UIView) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onAddSceneViewResult view: UIView, errorCode: Int) {
        DLog("===============添加view完成===============")
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRemoveSceneViewResult view: UIView, errorCode: Int) {
        DLog("===============移除view完成===============")
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRecvMessageFromUser userId: String, message: Data) {
        do {
            let jsonData: Any = try JSONSerialization.jsonObject(with: message, options: JSONSerialization.ReadingOptions.mutableContainers)
            let jsonDict = jsonData as! [String : Any]
            
            MetaServiceEngine.sharedEngine.sendMessage(dic: jsonDict)
            
        } catch {
            DLog("======= JSON Serialization failed. ========")
        }
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRecvBroadcastMessage message: Data) {
        
    }
    
    func metaScene(_ scene: AgoraMetaScene, onRemoteUserStateChanged uid: String, userState state: AgoraMetaSceneUserStateType, extraInfo: Data?) {
        if kSceneIndex == .chatRoom || kSceneIndex == .faceCaptureChatRoom {
            DispatchQueue.main.async {
                if !self.remoteUsers.keys.contains(uid) && state == .online {
                    guard let view = scene.createRenderView(CGRect(x: 0, y: 0, width: avatarCellSize, height: avatarCellSize)) else { return }
                    view.layer.cornerRadius = 10
                    view.layer.masksToBounds = true
                    view.layer.borderWidth = 2.0
                    view.layer.borderColor = UIColor.lightGray.cgColor
                    self.remoteUsers[String(uid)] = view
                    self.metaSceneView.avatarCV.items.append(view)
                    
                    let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
                    label.text = "uid:" + String(uid)
                    label.textColor = .red
                    view.addSubview(label)
                    label.layer.zPosition = .greatestFiniteMagnitude
                    
                    let config = AgoraMetaSceneDisplayConfig()
                    config.width = 200
                    config.height = 200
                    let jsonObject = try? JSONSerialization.jsonObject(with: extraInfo ?? Data(), options: .allowFragments)
                    var dict = jsonObject as? [String: Any]
                    dict!["userId"] = uid
                    dict!["sceneIndex"] = 0
                    let data = try? JSONSerialization.data(withJSONObject: dict!, options: [])
                    let _extraInfo = String(data: data!, encoding: String.Encoding.utf8)
                    config.extraInfo = _extraInfo!.data(using: String.Encoding.utf8)
                    scene.add(view, sceneDisplayConfig: config)
                } else if self.remoteUsers.keys.contains(uid) && state == .offline {
                    let view = self.remoteUsers[String(uid)]
                    self.remoteUsers.removeValue(forKey: String(uid))
                    self.metaSceneView.avatarCV.items.removeAll(where: {$0 as? UIView == view} )
                    scene.remove(view!)
                    view?.removeFromSuperview()
                }
            }
        }
    }
}

extension MetaSceneViewController: MetaSceneEventDelegate {
    func onMetaSceneEvent(event: MetaSceneEvent) {
        switch event {
        case .addSceneView:
            self.addSceneView()
            break
        case .removeSceneView:
            self.removeSceneView()
            break
        case .leaveScene:
            self.leaveScene()
            break
        case .changeDress:
            self.changeDress()
            break
        case .pitchFace:
            self.pitchFace()
            break
        case .changeViewMode:
            self.changeViewMode()
            break
        }
    }
}

extension MetaSceneViewController: RTCEngineInternalDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int) {
        if kSceneIndex == .chat {
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
                metaSceneView.remoteVideoCV.items.append(view)
                
                let label = UILabel(frame: CGRectMake(5, 5, 100, 20))
                label.text = "uid:" + String(uid)
                label.textColor = .red
                view.addSubview(label)
                label.layer.zPosition = .greatestFiniteMagnitude
            }
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        if kSceneIndex == .chat {
            if remoteUsers.keys.contains(String(uid)) {
                let view = remoteUsers[String(uid)]
                let rvc = AgoraRtcVideoCanvas()
                rvc.uid = uid
                rvc.view = nil
                rvc.renderMode = .fit
                rvc.mirrorMode = .enabled
                engine.setupRemoteVideo(rvc)
                remoteUsers.removeValue(forKey: String(uid))
                metaSceneView.remoteVideoCV.items.removeAll(where: {$0 as? UIView == view} )
            }
        }
    }
    
}

extension MetaSceneViewController: UIGestureRecognizerDelegate {
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        if self.isEditing {
            return true
        }
        return false
    }
}

extension MetaSceneViewController: DressViewEventDelegate {
    func sliderValueChanged(_ value: Float) {
        guard let rs = self.dressInfoModel.faceParameters else { return }
        guard let girlrs = rs[1].blendshape else { return }
        guard let assets = girlrs[metaSceneView.dressView!.getFaceIndex()].shapes else { return }
        
        //        let dic = ["value": [["key": "EB_updown_1", "value": sender.value * 100]]]
        //        let data = try? JSONSerialization.data(withJSONObject: dic, options: [])
        //        let str = String(data: data!, encoding: String.Encoding.utf8)
        let dic = ["value": [["key": String(assets[self.faceIndex].key!), "value": value * 100] as [String : Any]]]
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
    
    func scrollViewLabelClick(_ index: Int) {
        if metaSceneView.dressView?.selectIndex == 0 {
            guard let rs = self.dressInfoModel.dressResources else { return }
            guard let girlrs = rs[1].resources else { return }
            metaSceneView.dressView?.collectionViewItems.removeAll()
            guard let assets = girlrs[Int(index)].assets else { return }
            for asset in assets {
                let bundlePath = Bundle.main.path(forResource: "girl", ofType: "bundle")!
                guard let customBundle = Bundle(path: bundlePath) else { continue }
                let path = customBundle.path(forResource: String(asset), ofType: "jpg")
                metaSceneView.dressView?.collectionViewItems.append(path!)
            }
        } else {
            guard let rs = self.dressInfoModel.faceParameters else { return }
            guard let girlrs = rs[1].blendshape else { return }
            metaSceneView.dressView?.collectionViewItems.removeAll()
            guard let assets = girlrs[Int(index)].shapes else { return }
            for asset in assets {
                metaSceneView.dressView?.collectionViewItems.append(asset.ch!)
            }
        }
    }
    
    func collectionViewCellClick(_ index: Int) {
        if metaSceneView.dressView?.selectIndex == 0 {
            guard let rs = self.dressInfoModel.dressResources else { return }
            guard let girlrs = rs[1].resources else { return }
            guard let assets = girlrs[metaSceneView.dressView!.getDressIndex()].assets else { return }
            
            //        let dic1 = ["id": [10002]]
            //        let data1 = try? JSONSerialization.data(withJSONObject: dic1, options: [])
            //        let str1 = String(data: data1!, encoding: String.Encoding.utf8)
            
            let dic1 = ["id": [assets[Int(index)]]]
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
            self.faceIndex = index
        }
    }
    
    
}
