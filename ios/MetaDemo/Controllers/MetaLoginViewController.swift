//
//  MetaLoginViewController.swift
//  MetaDemo
//
//  Created by ZhouRui on 2023/8/18.
//

import Foundation
import AgoraRtcKit

let kOnConnectionStateChangedNotifyName = NSNotification.Name(rawValue: "onConnectionStateChanged")
let roleDic: Dictionary = ["男": "boy",
                           "女": "girl",
                           "米娜": "mina",
                           "kda": "kda",
                           "花木兰": "huamulan"]
let avatarUrlArray = ["https://accpic.sd-rtn.com/pic/test/png/2.png",
                      "https://accpic.sd-rtn.com/pic/test/png/4.png",
                      "https://accpic.sd-rtn.com/pic/test/png/1.png",
                      "https://accpic.sd-rtn.com/pic/test/png/3.png",
                      "https://accpic.sd-rtn.com/pic/test/png/6.png",
                      "https://accpic.sd-rtn.com/pic/test/png/5.png"]

class MetaLoginViewController: UIViewController {
    
    private var currentSceneId: Int = 25
    
    private var sceneVC: MetaSceneViewController!
    
    private var metaLoginView: MetaLoginView!
    
    private let libraryPath = NSHomeDirectory() + "/Library/Caches/25"
    
    var currentSceneInfo: AgoraMetaSceneInfo?
    
    var isEntering: Bool = false
    
    var fromMainScene: Bool = false
    
    var mainView: UIView?
    
    var indicatorView: UIActivityIndicatorView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        guard let nib = Bundle.main.loadNibNamed("LoginView", owner: nil) else { return }
        metaLoginView = nib.first as? MetaLoginView
        metaLoginView.frame = self.view.frame
        metaLoginView.loginViewDelegate = self
        self.view.addSubview(metaLoginView)
        metaLoginView.setupUI()
        
        indicatorView = UIActivityIndicatorView.init(frame: view.frame)
        if #available(iOS 13.0, *) {
            indicatorView?.style = UIActivityIndicatorView.Style.large
        } else {
            // Fallback on earlier versions
        }
        indicatorView?.color = UIColor.white
        view.addSubview(indicatorView!)
        
        view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(hideKeyboard)))
    }
    
    override var shouldAutorotate: Bool {
        return true
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
    
    override var prefersStatusBarHidden: Bool {
        return false
    }
    
    @objc func hideKeyboard() {
        view.endEditing(true)
    }
    
    @IBAction func selectedAvatarAction(sender: UIButton) {
        metaLoginView.selAvatarAlert.isHidden = false
    }
    
    /// 创建场景
    func createScene(_ sceneInfo: AgoraMetaSceneInfo) {
        self.metaLoginView.downloadingBack.isHidden = true
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        guard let sceneViewController = storyBoard.instantiateViewController(withIdentifier: "SceneViewController") as? MetaSceneViewController else { return }
        sceneViewController.modalPresentationStyle = .fullScreen
        MetaServiceEngine.sharedEngine.createMetaScene(sceneViewController)
        MetaServiceEngine.sharedEngine.currentSceneInfo = sceneInfo
        self.sceneVC = sceneViewController
    }
    
    /// 场景信息ready(下载完成或者本地资源加载)，可以创建场景
    func onSceneReady(_ sceneInfo: AgoraMetaSceneInfo) {
        DispatchQueue.main.async {
            self.createScene(sceneInfo)
        }
    }
}

extension MetaLoginViewController: MetaLoginViewDelegate {
    func enterScene(sceneIndex: MetaSceneIndex, role: String, userName: String, avatarIndex: Int) {
        if isEntering {
            return
        }
        isEntering = true
        
        indicatorView?.startAnimating()
        
        kSceneIndex = sceneIndex
        let _avatarIndex = avatarIndex > avatarUrlArray.count - 1 ? 0 : avatarIndex
        
        MetaServiceEngine.sharedEngine.role = roleDic[role]

        MetaServiceEngine.sharedEngine.resolution = CGSizeMake(240, 240);
        // 创建RTC Engine
        MetaServiceEngine.sharedEngine.createRtcEngine()
        // 创建meta service
        MetaServiceEngine.sharedEngine.createMetaService(userName: userName, avatarUrl: avatarUrlArray[_avatarIndex], delegate: self)
        // 获取场景信息
        MetaServiceEngine.sharedEngine.metaService?.getSceneAssetsInfo()
        
        // 设置进入不同场景
        if kSceneIndex == .chat {
            switchOrientation(isPortrait: false, isFullScreen: true)
        } else {
            switchOrientation(isPortrait: true, isFullScreen: true)
        }
    }
    
    func cancelDownload() {
        metaLoginView.downloadingBack.isHidden = true
        MetaServiceEngine.sharedEngine.metaService?.cancelDownloadSceneAssets(currentSceneId)
    }
}

extension MetaLoginViewController: AgoraMetaEventDelegate {
    func onTokenWillExpire() {
        
    }
    
    // 创建场景完成回调
    func onCreateSceneResult(_ scene: AgoraMetaScene?, errorCode: Int) {
        if errorCode != 0 {
            DLog("create scene error: \(errorCode)")
            return
        }

        MetaServiceEngine.sharedEngine.metaScene = scene
//        let dict = ["enableBlendShapeDump": true]
//        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
//        let str = String(data: value!, encoding: String.Encoding.utf8)
//        scene?.setSceneParameters(str!)
        DispatchQueue.main.async {
            var width = self.view.frame.width
            var height = self.view.frame.height
            if kSceneIndex == .chat {
                if width < height {
                    let temp = width
                    width = height
                    height = temp
                }
            } else if kSceneIndex == .chatRoom || kSceneIndex == .faceCaptureChatRoom {
                width = avatarCellSize
                height = avatarCellSize
            }
            
            guard let view = scene?.createRenderView(CGRect(x: 0, y: 0, width: width, height: height)) else { return }
            self.mainView = view
            
            MetaServiceEngine.sharedEngine.metaScene?.enableVideoCapture(view, enable: true)
            
            MetaServiceEngine.sharedEngine.rtcEngine?.enableVideo()
            
            MetaServiceEngine.sharedEngine.enterMetaScene(view)
            view.backgroundColor = UIColor.clear
            
            self.sceneVC.sceneView = self.mainView
            if self.presentedViewController == nil {
                self.present(self.sceneVC, animated: true)
            }
        }
    }
    
    // 连接状态变化回调
    func onConnectionStateChanged(_ state: AgoraMetaConnectionStateType, reason: AgoraMetaConnectionChangedReasonType) {
        if state == .disconnected {
            DispatchQueue.main.async {
                self.indicatorView?.stopAnimating()
                self.indicatorView?.removeFromSuperview()
                self.indicatorView = nil
            }
        } else if state == .aborted {
            MetaServiceEngine.sharedEngine.leaveChannel()
            MetaServiceEngine.sharedEngine.leaveMetaScene()
            DispatchQueue.main.async {
                DLog("state == \(state.rawValue), reason == \(reason.rawValue)")
                NotificationCenter.default.post(name: kOnConnectionStateChangedNotifyName, object: nil, userInfo: ["state":state.rawValue,"reason":reason.rawValue])
                self.dismiss(animated: true, completion: nil)
            }
        }
    }
    
    func onRequestToken() {
        
    }
    
    // 获取场景资源信息回调
    func onGetSceneAssetsInfoResult(_ scenes: NSMutableArray, errorCode: Int) {
        self.isEntering = false
        
        DispatchQueue.main.async {
            self.indicatorView?.stopAnimating()
            self.indicatorView?.removeFromSuperview()
            self.indicatorView = nil
        }
        
        if errorCode != 0 {
            DispatchQueue.main.async {
                let alertController = UIAlertController.init(title: "get Scenes failed:errorcode:\(errorCode)", message:nil , preferredStyle:.alert)
                
                alertController.addAction(UIAlertAction.init(title: "确定", style: .cancel, handler: nil))
                let appDelegate = UIApplication.shared.delegate as! AppDelegate
                appDelegate.isFullScreen = false
                self.present(alertController, animated: true)
            }
            return
        }
        
        if scenes.count == 0 {
            return
        }
        
        guard let firstScene = scenes.compactMap({ $0 as? AgoraMetaSceneInfo }).first(where: { $0.sceneId == currentSceneId }) else { return }
        
        currentSceneInfo = firstScene
        
        let metaService = MetaServiceEngine.sharedEngine.metaService
        let totalSize = firstScene.totalSize / 1024 / 1024
        if metaService?.isSceneAssetsDownloaded(currentSceneId) != 1 {
            DispatchQueue.main.async {
                let alertController = UIAlertController.init(title: "下载提示", message: "首次进入Meta场景需下载\(totalSize)M数据包", preferredStyle:.alert)
                
                alertController.addAction(UIAlertAction.init(title: "下次再说", style: .cancel, handler: nil))
                alertController.addAction(UIAlertAction.init(title: "立即下载", style: .default, handler: { UIAlertAction in
                    metaService?.downloadSceneAssets(self.currentSceneId)
                    self.metaLoginView.downloadingBack.isHidden = false
                }))
                
                let appDelegate = UIApplication.shared.delegate as! AppDelegate
                appDelegate.isFullScreen = false
                
                self.present(alertController, animated: true)
            }
        }else {
            onSceneReady(firstScene)
        }
    }
    
    // 下载进度回调
    func onDownloadSceneAssetsProgress(_ sceneId: Int, progress: Int, state: AgoraMetaDownloadStateType) {
        DispatchQueue.main.async {
            self.metaLoginView.downloadingProgress.progress = Float(progress)/100.0
        }

        if state == .downloaded && currentSceneInfo != nil {
            onSceneReady(currentSceneInfo!)
        }
    }
}
